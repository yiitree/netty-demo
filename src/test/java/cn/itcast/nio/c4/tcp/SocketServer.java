package cn.itcast.nio.c4.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    public static void main(String[] args) throws IOException {
        // 创建服务端socket
        ServerSocket serverSocket = new ServerSocket(8088);
        // 创建客户端socket
        Socket socket;

        //循环监听等待客户端的连接
        while (true) {
            // 监听客户端
            socket = serverSocket.accept();

            ServerThread thread = new ServerThread(socket);
            thread.start();

            System.out.println("当前客户端的IP：" + socket.getInetAddress().getHostAddress());
        }
    }

}
