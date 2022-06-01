package cn.itcast.mytest;

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
 * 没有selector版本，
 * 此时问题：
 * 1. 如果有一万个连接，但是只有三四个发送数据，此时遍历List<SocketChannel> channels还是需要全部遍历，导致浪费性能
 * 2. 如果大家都没发送数据，那线程就回空转，这样导致cpu压力100%
 * 优化：
 * 1. 如果可以吧一万个连接中，有数据发送的三四个连接单独再保存到一个list，后面遍历的时候就只遍历这几个，就可以解决一直遍历的问题
 * 2. 这样还有一个好处，如果正在发送连接的list中灭有数据，表示大家只是建立连接，没有数据，线程就可以先阻塞住，等有连接线程在运行，可以防止空转
 */
@Slf4j
public class T02NIOServer {

    public static void main(String[] args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8888));
        List<SocketChannel> channels = new ArrayList<>();
        while (true){
            SocketChannel sc = ssc.accept(); // 非阻塞
            if(sc != null){
                log.debug("连接建立完毕，{}",sc);
                sc.configureBlocking(false);
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                final int read = channel.read(buffer); // 非阻塞
                if(read >0){
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("读取完毕，{}",sc);
                }
            }
        }
    }



}
