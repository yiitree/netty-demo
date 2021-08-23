package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8888));
//        SocketAddress address = sc.getLocalAddress();
        System.out.println("waiting...");
//        sc.close();
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
