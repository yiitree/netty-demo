package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 监听事件
 */
@Slf4j
public class T03Server2 {

    /**
     * 方案三：非阻塞式 --- 建立事件（建立连接）、取消事件（取消连接）
     * 使用Selector，用于监视是否有事件发生，
     * 如果有时间发生，就唤醒线程(channel)处理事件，
     * 这样就不用一直while循环等待，白白消耗资源
     */
    public static void main(String[] args) throws IOException {

        // 1. 创建 selector, 可以管理多个channel
        Selector selector = Selector.open();

        // 2. 创建 ssc
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置非阻塞
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8888));

        // 3. 把ssc注册到selector
        // 返回：SelectionKey类似管理员 就是将来事件发生后，通过它可以知道 1.是什么事件 2.哪个channel的事件
        //                          selector    关注的事件，0表示不关注任何事件      null
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("sscKey:{}", sscKey);

        /*
        4. 指定sscKey监听的事件
            accept：服务器端事件，会在客户端发起连接请求时触发
            connect：客户端事件，客户端连接服务器成功后，客户端触发
            read：服务器事件，客户端发送了数据，可以读到触发
            write：可写事件（后面会专门进行说明）
         */
        sscKey.interestOps(SelectionKey.OP_ACCEPT);

        while (true) {
            // 5.selector持续监听 没有事件发生，线程阻塞; 有事件，线程才会恢复运行
            // (就是为了解决一直while循环，浪费资源问题)
            selector.select();

            // 6.处理事件，由于一个selector是一个set，里面可以有多个key，因此要遍历是哪个key处理accept事件
            // - 因为一个服务器只有一个selector，但是每有一个客户端连接就创建一个ssc
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题，
            // 所以要在集合遍历的时候删除元素，因此选择迭代器遍历
            while (iter.hasNext()) {
                // 7.这个key只是用来监视事件的
                SelectionKey key = iter.next();
                log.debug("key: {}", key);

                // 8.处理
                ServerSocketChannel ssc1 = (ServerSocketChannel) key.channel();
                // 9.建立连接 --- 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                SocketChannel sc = ssc1.accept();
                // 建立连接后就继续处理

//                // 取消处理
//                key.cancel();

            }
        }
    }


    /**
     * 方案三：非阻塞式 --- 建立事件（建立连接）、取消事件（取消连接）
     * 使用Selector，用于监视是否有事件发生，
     * 如果有时间发生，就唤醒线程(channel)处理事件，
     * 这样就不用一直while循环等待，白白消耗资源
     */
    public static void main1(String[] args) throws IOException {

        // 1. 创建服务端channel用于监听
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 创建 selector 其实是一个容器，保存SelectionKey
        Selector selector = Selector.open();

        // 3. 创建SelectionKey 管理服务器channel --- 主要是为了唤醒channel
        SelectionKey sscKey = ssc.register(selector, 0, null);

        // 3.1 设置SelectionKey关注事件类型
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        // 3.2 设置SelectionKey监听端口
        ssc.bind(new InetSocketAddress(8888));

        while (true) {
            // 4. 监听selector是否被唤醒
            selector.select();

            // 5. 循环遍历selector中的SelectionKey是否有事件发生
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                // 6.这个key只是用来监视事件的
                SelectionKey key = iter.next();

                // 8.从SelectionKey中
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                // 9.建立连接 --- 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                SocketChannel sc = channel.accept();
                // 建立连接后就继续处理

//                // 取消处理
//                key.cancel();

            }
        }
    }

}
