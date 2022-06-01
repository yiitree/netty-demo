package cn.itcast.mytest;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static cn.itcast.nio.c2.ByteBufferUtil.debugRead;

/**
 * 阻塞io
 * 即使加上线程池，但是线程池总有满的时候
 * 而且万一大家都上线，但是大家都不发送数据，此时线程池就占完了，而且全都卡主了
 */
@Slf4j
public class T01BIOServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8888));
        while (true){
            log.debug("准备建立连接");
            SocketChannel channel = ssc.accept();// 阻塞
            log.debug("连接建立完毕，{}",channel);
            new Thread(()->{
                try {
                    handle(channel);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
    public static void handle(SocketChannel channel) throws Exception {
        log.debug("准备开始读取数据，{}",channel);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        channel.read(buffer);// 阻塞
        buffer.flip();
        debugRead(buffer);
        buffer.clear();
        log.debug("读取完毕，{}",channel);
    }
}
