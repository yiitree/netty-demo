package cn.itcast.mytest;

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
 * 还是存在的问题：
 * 处理事件的时候，没发相应其他事件，eg：
 * 如果是游戏客户端，假如有很多客户端进行连接，正在处理，后面又来人来登录，此时就没发进行连接
 * 优化1：处理逻辑使用线程池，但是总有占满的时候
 * 优化2：创建两个线程池
 *  bossGroup：只负责连接
 *  workGroup：负责处理事件
 */
@Slf4j
public class T03NIOSelectorServer {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8888));
        // 把服务端channel注册到selector
        SelectionKey sscKey = ssc.register(selector, 0, null);
        /*
         指定服务端channel监听的事件
            accept：服务器端事件，会在客户端发起连接请求时触发
            connect：客户端事件，客户端连接服务器成功后，客户端触发
            read：服务器事件，客户端发送了数据，可以读到触发
            write：可写事件（后面会专门进行说明）一般是用于服务器给客户端进行发送数据
         */
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        while (true) {
            // 检测是否有正在运行的channel，没有就会阻塞，有就会继续执行
            selector.select();
            // 遍历正在运行的channel集合
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                // 如果是accept事件
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 建立连接 ---> 其实就是处理连接，如果没有accept()表示不进行处理，Select就会一直循环等待处理
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    // 如果是 read ---> 两种情况：在断开连接的时候还是会发送一次read事件
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 如果是正常断开，read 的方法的返回值是 -1
                        int read = channel.read(buffer);
                        // 正常断开
                        if(read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
    }

}
