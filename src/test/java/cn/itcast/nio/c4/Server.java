package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.itcast.nio.c2.ByteBufferUtil.debugRead;

/**
 * 服务器端
 * 首先创建channel进行连接，channel把数据读取到buffer中
 * 然后创建byteBuffer，读取到程序中
 */
@Slf4j
public class Server {

    /**
     * 方案一：阻塞式
     * 创建一个线程来进行连接
     */
    public static void main(String[] args) throws IOException {

        // 0.创建一个byteBuffer来缓存每次连接客户端访问的数据
        // 由于只是创建一个buffer，因此是单线程处理
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2.绑定监听端口
        ssc.bind(new InetSocketAddress(8888));

        // 3.建立连接的集合（主要是为了连接可以复用）
        List<SocketChannel> channels = new ArrayList<>();

        while (true){
            // 4.accept建立客户端连接（tcp三次握手）,
            // 由于希望连接可以多次调用，因此放在while循环中
            // 返回读写通道，用SocketChannel来进行读写操作
            log.debug("准备建立连接");
            SocketChannel sc = ssc.accept(); // accept默认是阻塞方法，由于没有连接请求，因此线程就停止运行了，等待连接建立
            log.debug("连接建立完毕，{}",sc);
            channels.add(sc);

            for (SocketChannel channel : channels) {
                // 5.接收客户端发来的数据，读取到ByteBuffer中
                log.debug("准备开始读取数据，{}",sc);
                channel.read(buffer); // read也是阻塞方法，客户端没有发送请求，就一直等待
                // 切换读模式
                buffer.flip();
                // 读取数据
                debugRead(buffer);
                // 读取完毕，切换成写模式
                buffer.clear();
                log.debug("读取完毕，{}",sc);
            }
        }
    }

}
