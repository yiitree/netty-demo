package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * 服务器端
 * 解决半包黏包问题
 */
@Slf4j
public class Server5 {

    /**
     * 方案五
     */
    public static void main(String[] args) throws IOException {
        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2. 建立 selector 和 channel 的联系（注册）
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // key 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        ssc.bind(new InetSocketAddress(8888));
        while (true) {
            // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
            selector.select();
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); // accept, read
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iter.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                if (key.isAcceptable()) { // 如果是 accept
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16); // attachment
                    // 将一个 byteBuffer 作为附件关联到 selectionKey 上
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey:{}", scKey);
                    // 如果是 read
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的channel
                        // 获取 selectionKey 上关联的附件
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer); // 如果是正常断开，read 的方法的返回值是 -1
                        if(read == -1) {
                            key.cancel();
                        } else {
                            split(buffer);
                            // 需要扩容
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                // 切换读模式
                                buffer.flip();
                                newBuffer.put(buffer); // 0123456789abcdef3333\n
                                // 替换sc附件
                                key.attach(newBuffer);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();  // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                    }
                }
            }
        }
    }


    /**
     * 方案五
     */
    public static void main2(String[] args) throws IOException {

        // 创建服务器 Channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 创建 Selector 选择器（里面保存着多个SelectionKey）
        Selector selector = Selector.open();

        // 创建 SelectionKey
        SelectionKey sscKey = serverSocketChannel.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8888));

        while (true) {
            // 开启 selector 循环监听，如果没有就一直循环监听，如果有就执行下面步骤
            selector.select();
            // 执行下面说明这一组 selector 有事件发生

            // 由于不知道是哪一个有事件发生，因此要遍历所有 selectionKey，依次处理
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                // 每次遍历都要删除这个 selectedKey
                iter.remove();

                // 是否有 accept 事件发生
                if (selectionKey.isAcceptable()) {
                    // 从selectionKey中拿到之前绑定的 serverSocketChannel
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);

                    // 下面就是服务读取信息操作
                    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                    // 将一个 byteBuffer 作为附件关联到 selectionKey 上
                    SelectionKey scKey = socketChannel.register(selector, 0, byteBuffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", socketChannel);
                    log.debug("scKey:{}", scKey);

                // 是否有 read 事件发生
                } else if (selectionKey.isReadable()) {
                    try {
                        // 拿到触发事件的 channel
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        // 获取 selectionKey 上关联的附件ByteBuffer --- 每一个SelectionKey都绑定一个ByteBuffer附件
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = socketChannel.read(byteBuffer);
                        // 发送玩不正常断开
                        if(read == -1) {
                            selectionKey.cancel();
                        } else {
                            split(byteBuffer);
                            // 需要扩容
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                // 切换读模式
                                byteBuffer.flip();
                                // 0123456789abcdef3333\n
                                newBuffer.put(byteBuffer);
                                // 替换sc附件
                                selectionKey.attach(newBuffer);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                        selectionKey.cancel();
                    }
                }
            }
        }
    }


    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact(); // 0123456789abcdef  position 16 limit 16
    }

}
