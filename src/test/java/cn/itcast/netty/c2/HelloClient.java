package cn.itcast.netty.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * 客户端
 */
public class HelloClient {

    public static void main(String[] args) throws InterruptedException {
        // 1. 启动类
        new Bootstrap()
                // 2. 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 3. 选择客户端 channel 实现
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(
                        new ChannelInitializer<NioSocketChannel>() {
                            // 在连接建立后被调用
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // 对发送的内容进行编码，把字符串转化为byteBuffer
                                ch.pipeline().addLast(new StringEncoder());
                            }
                        })
                // 5. 连接到服务器
                .connect(new InetSocketAddress("localhost", 8080))

                .sync()
                .channel()
                // 6. 向服务器发送数据
                .writeAndFlush("hello, world");
    }

}
