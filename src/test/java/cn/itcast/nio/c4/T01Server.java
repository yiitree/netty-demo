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
public class T01Server {

    /**
     * 方案一：阻塞式
     * 创建一个线程来进行连接
     * Socket        - ServerSocket         java.net下面实现socket通信的类
     * SocketChannel - ServerSocketChannel  java.nio支持异步通信
     *
     * 服务器必须先建立ServerSocket或者ServerSocketChannel 来等待客户端的连接
     * 客户端必须建立相对应的Socket或者SocketChannel来与服务器建立连接
     * 服务器接受到客户端的连接受，再生成一个Socket或者SocketChannel与此客户端通信
     * 本质：
     * ServerSocketChannel 是用于监听是否有连接
     * SocketChannel 真正连接通信
     *
     * 服务端
     * 由于是要时刻监听，因此先创建ServerSocketChannel 进行监听
     * 监听有客户端请求后，再创建SocketChannel进行连接
     *
     * 客户端
     * 由于是主动访问，因此只需要SocketChannel
     */
    public static void main(String[] args) throws IOException {

        // 0.创建一个byteBuffer来缓存每次连接客户端访问的数据
        // 由于只是创建一个buffer，因此是单线程处理
        // 共享一个ByteBuffer缓存空间
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2.绑定监听端口
        ssc.bind(new InetSocketAddress(8888));

        // 3.建立连接的集合（主要是为了连接可以复用）
        List<SocketChannel> channels = new ArrayList<>();

        // 因为服务器需要一直保持启动，因此要一直监听建立连接
        while (true){
            // 4.accept建立客户端连接（tcp三次握手）,
            // 由于希望连接可以多次调用，因此放在while循环中
            // 返回读写通道，用SocketChannel来进行读写操作
            log.debug("准备建立连接");
            // accept默认是阻塞方法，由于没有连接请求，因此线程就停止运行了，等待连接建立
            SocketChannel sc = ssc.accept();

            log.debug("连接建立完毕，{}",sc);
            channels.add(sc);


            // 这个循环是为了两个客户端都在连接，但是都卡在
            for (SocketChannel channel : channels) {
                // 5.接收客户端发来的数据，读取到ByteBuffer中
                log.debug("准备开始读取数据，{}",sc);
                // read也是阻塞方法，客户端没有发送请求，就一直等待 - 这里读取数据只能为一次缓存大小的数据
                channel.read(buffer);

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

    /**
     * 方案一：阻塞式
     * 创建一个线程来进行连接
     */
    public static void main1(String[] args) throws IOException {
        // 1.创建服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8888));
        // 3.建立连接的集合（主要是为了连接可以复用）
        List<SocketChannel> channels = new ArrayList<>();

        // 所有channel共享一个ByteBuffer缓存空间
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 因为服务器需要一直保持启动，因此要一直监听建立连接
        while (true) {
            // 4.accept建立客户端连接（tcp三次握手）,
            // 由于希望连接可以多次调用，因此放在while循环中
            // 返回读写通道，用SocketChannel来进行读写操作
            // 等待客户端进行访问，需要tcp三次握手
            log.debug("等待客户端访问");
            SocketChannel socketChannel = serverSocketChannel.accept(); // 阻塞方法
            log.debug("已于客户端建立连接，{}", socketChannel);

            // 把socketChannel保存到list数组中，这样处理和等待连接互不影响
            // 每一个http请求都创建一个对象的channel进行绑定
            channels.add(socketChannel);
            for (SocketChannel channel : channels) {
                // 5.接收客户端发来的数据，读取到ByteBuffer中
                log.debug("准备开始读取数据，{}",socketChannel);
                // read也是阻塞方法，客户端没有发送请求，就一直等待
                channel.read(buffer);

                // 切换读模式
                buffer.flip();
                // 读取数据
                debugRead(buffer);
                // 读取完毕，切换成写模式
                buffer.clear();
                log.debug("读取完毕，{}",socketChannel);
            }
        }
    }


}
