package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 客户端，用户和服务器建立连接
 */
public class Client {

//    public static void main(String[] args) throws IOException {
//        SocketChannel sc = SocketChannel.open();
//        sc.connect(new InetSocketAddress("localhost", 8888));
////        SocketAddress address = sc.getLocalAddress();
//        System.out.println("waiting...");
////        sc.close();
//    }


    public static void main(String[] args) throws IOException {
        // 创建socket用于连接服务器
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8888));
        // 连接服务器
        SocketAddress address = sc.getLocalAddress();
// sc.write(Charset.defaultCharset().encode("hello\nworld\n"));
//        sc.write(Charset.defaultCharset().encode("0123\n456789abcdef"));
        sc.write(Charset.defaultCharset().encode("0123456789abcdef3333\n"));
        System.in.read();
    }


//    /**
//     * 客户端
//     * @param args
//     * @throws IOException
//     */
//    public static void main2(String[] args) throws IOException {
//        // 创建一个socketChannel
//        SocketChannel sc = SocketChannel.open();
//        // 创建连接，指定访问连接 地址+端口
//        sc.connect(new InetSocketAddress("localhost", 8080));
//
////        SocketAddress address = sc.getLocalAddress();
//        System.out.println("waiting...");
////        sc.close();
//    }
}
