package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 服务器端
 * 首先创建channel进行连接，channel把数据读取到buffer中
 * 然后创建byteBuffer，读取到程序中
 */
@Slf4j
public class Server4 {

    /**
     * 方案四：非阻塞式
     * 使用Selector，用于监视是否有其他事件发生，
     * 这样就不用一直while循环等待，白白消耗资源
     *
     * 半包、粘包问题：
     * 还是会出现一个问题，客户端发送的消息，假如比较长，byteBuffer可能存放不下，此时就会造成一个问题：
     * 消息被分割，尤其是汉字，两个字符长度，有可能会被从中间分割
     */
    public static void main(String[] args) throws IOException {

        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();

        // 获得channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 建立 selector 和 channel 的联系（注册）
        // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);

        // key 只关注 accept 事件
        /*
        设置连接事件：
        accept：会在有连接请求时触发
        connect：是客户端连接建立后触发
        read：客户端发送了数据，可以读到触发
        write：可写事件
         */
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        ssc.bind(new InetSocketAddress(8888));

        while (true) {
            // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞, 事件发生后要么处理，要么取消，不能置之不理
            selector.select();

            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            // accept, read
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                // 这个key只是用来监视事件的
                SelectionKey key = iter.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iter.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                // 如果是 accept
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 建立连接 ---> 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);

                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey:{}", scKey);
                // 如果是 read ---> 两种情况：在断开连接的时候还是会发送一次read事件
                } else if (key.isReadable()) {
                    try {
                        // 拿到触发事件的channel
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = channel.read(buffer);
                        // 正常断开
                        if(read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
//                            debugAll(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 因为客户端断开了,因此需要将 key 取消
                        // （从 selector 的 keys 集合中真正删除 key）
                        key.cancel();
                    }
                }
            }
        }
    }



    /**
     * 方案四：非阻塞式
     * 使用Selector，用于监视是否有其他事件发生，
     * 这样就不用一直while循环等待，白白消耗资源
     *
     * 半包、粘包问题：
     * 还是会出现一个问题，客户端发送的消息，假如比较长，byteBuffer可能存放不下，此时就会造成一个问题：
     * 消息被分割，尤其是汉字，两个字符长度，有可能会被从中间分割
     */
    public static void main1(String[] args) throws IOException {

        // 获得channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();

        // 2. 建立 selector 和 channel 的联系（注册）
        // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);

        // key 只关注 accept 事件
        /*
        设置连接事件：
        accept：会在有连接请求时触发
        connect：是客户端连接建立后触发
        read：客户端发送了数据，可以读到触发
        write：可写事件
         */
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8888));

        while (true) {
            // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞, 事件发生后要么处理，要么取消，不能置之不理
            selector.select();

            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            // accept, read
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                // 这个key只是用来监视事件的
                SelectionKey key = iter.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iter.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                // 如果是 accept
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 建立连接 ---> 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);

                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey:{}", scKey);
                    // 如果是 read ---> 两种情况：在断开连接的时候还是会发送一次read事件
                } else if (key.isReadable()) {
                    try {
                        // 拿到触发事件的channel
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = channel.read(buffer);
                        // 正常断开
                        if(read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
//                            debugAll(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 因为客户端断开了,因此需要将 key 取消
                        // （从 selector 的 keys 集合中真正删除 key）
                        key.cancel();
                    }
                }
            }
        }
    }



}
