package cn.itcast.nio.c4.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketClient {

    public static void main(String[] args) {
        try {
            // 要发送的消息
            String sendMsg = "客户端发送的消息";

            // 获取服务器的地址
            InetAddress addr = InetAddress.getByName("localhost");

            // 创建packet包对象，封装要发送的包数据和服务器地址和端口号
            DatagramPacket packet = new DatagramPacket(sendMsg.getBytes(), sendMsg.getBytes().length, addr, 8088);

            // 创建Socket对象
            DatagramSocket socket = new DatagramSocket();

            // 发送消息到服务器
            socket.send(packet);

            // 关闭socket
            socket.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
