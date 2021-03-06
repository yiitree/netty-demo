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
 * 处理事件
 */
@Slf4j
public class T04Server3 {


    /**
     * 方案三：非阻塞式 --- 建立事件（建立连接）、取消事件（取消连接）
     * 使用Selector，用于监视是否有事件发生，
     * 如果有时间发生，就唤醒线程(channel)处理事件，
     * 这样就不用一直while循环等待，白白消耗资源
     *
     */
    public static void main(String[] args) throws IOException {

        // 1. 创建 selector, 用于管理channel
        Selector selector = Selector.open();

        // 2. 创建 ssc
        ServerSocketChannel ssc = ServerSocketChannel.open();


        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8888));

        // 3. 把ssc注册到selector
        SelectionKey sscKey = ssc.register(selector, 0, null); // 此时保存到selector中
        log.debug("sscKey:{}", sscKey);

        // 4. 指定sscKey监听的事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);

        while (true) {
            // 5.selector持续监听 没有事件发生，线程阻塞;
            // 有事件，线程才会恢复运行(就是为了解决一直while循环，浪费资源问题)
            // 并且此时会把ssc加入到selectedKeys()集合中
            selector.select(); // 查询到事件，此时复制一份保存到selectedKeys

            // 6.处理事件，由于一个selector是一个set，里面可以有多个key，因此要遍历是哪个key处理accept事件
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题，因此选择迭代器遍历
            while (iter.hasNext()) {

                // 7.这个key只是用来监视事件的
                SelectionKey key = iter.next();
                log.debug("key: {}", key);

                // 处理key 时，要从 selector 中删除，否则还存在selectedKeys中，并且不监听任何事件
                iter.remove();

                // 如果是accept事件
                if(key.isAcceptable()){
                    // 8.处理
                    ServerSocketChannel ssc1 = (ServerSocketChannel) key.channel();
                    // 9.建立连接 --- 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                    SocketChannel sc = ssc1.accept(); // 处理事件，并且把这个对象绑定是事件个关闭，然后吧事件关闭，但对象还在
                    // 建立连接后就继续处理
                    sc.configureBlocking(false);
                    // 再把sc绑定到selector上  为了sc也不阻塞，也交给selector
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);

                    // 如果是读事件
                }else if(key.isReadable()){
                    try {
                        // 拿到触发事件的channel
                        SocketChannel sc1 = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = sc1.read(buffer);
                        if(read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
//                            debugAll(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                        key.cancel();
                    }
                }
            }
        }
    }



    /**
     * 处理事件
     * 方案四：非阻塞式
     * 使用Selector，用于监视是否有事件发生，
     * 如果有时间发生，就唤醒线程(channel)处理事件，
     * 这样就不用一直while循环等待，白白消耗资源
     */
    public static void main1(String[] args) throws IOException {
        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();

        // 2. 创建 channel 并设置为非阻塞，这里其实就先只创建一个channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 3. 建立 selector 和 channel 的联系（把channel注册到selector上）
        // 返回：SelectionKey类似管理员 就是将来事件发生后，通过它可以知道 1.是什么事件 2.哪个channel的事件
        //                             selector    不关注任何时间      null
        SelectionKey sscKey = ssc.register(selector, 0, null);

        /*
        4. 指定这个管理员应该关注哪个事件  key 只关注 accept 事件
            accept：会在有连接请求时触发
            connect：是客户端连接建立后触发
            read：客户端发送了数据，可以读到触发
            write：可写事件
         */
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        ssc.bind(new InetSocketAddress(8888));

        while (true) {
            /*
            5. select 方法, 没有事件发生，线程阻塞，
                           有事件，线程才会恢复运行
                           (就是为了解决一直while循环，浪费资源问题)
            select 在事件未处理时，它不会阻塞, 事件发生后要么处理，要么取消，不能置之不理
             */
            selector.select();

            // 6. 处理事件, selectedKeys 内部包含了所有发生的事件
            // 获取所有可用事件集合
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题，
            // 所以要在集合遍历的时候删除元素，因此选择迭代器遍历
            while (iter.hasNext()) {

                // 7.这个key只是用来监视事件的
                SelectionKey key = iter.next();
                log.debug("key: {}", key);

                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iter.remove();

                // 如果是accept事件
                if(key.isAcceptable()){
                    // 8.处理
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 9.建立连接 --- 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                    SocketChannel sc = channel.accept();
                    // 建立连接后就继续处理
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_ACCEPT);
                    log.debug("{}", sc);

                // 如果是读事件
                }else if(key.isReadable()){
                    try {
                        // 拿到触发事件的channel
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = channel.read(buffer);
                        if(read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
//                            debugAll(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                        key.cancel();
                    }
                }
            }
        }
    }

}
