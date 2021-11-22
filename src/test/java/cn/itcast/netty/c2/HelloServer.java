package cn.itcast.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * 服务端
 */
public class HelloServer {

    public static void main(String[] args) {

        // 1. 服务端的启动器，负责组装 netty 组件，启动服务器，类似springboot，直接启动，很方便。
        new ServerBootstrap()
                // 添加组件
                // 2. BossEventLoop, WorkerEventLoop(selector,thread), group 组
                // NioEventLoopGroup -
                // EventLoop：包含selector + 一个线程 - 一个线程负责一个事件
                .group(new NioEventLoopGroup())
                // 3. 选择 服务器的 ServerSocketChannel 实现
                // NioServerSocketChannel其实就是netty对ServerSocketChannel的一个封装
                // 还有OIO BIO
                .channel(NioServerSocketChannel.class)
                // 4. 处理事件的分工
                // boss 负责处理连接
                // worker(child) 负责处理读写，决定了 worker(child) 能执行哪些操作（handler）
                // 其实就是指定worker执行什么逻辑
                .childHandler(
                        // 5. channel 代表和客户端进行数据读写的通道
                        // Initializer 初始化，负责添加别的 handler
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // 6. 添加具体 handler
                                // LoggingHandler
                                ch.pipeline().addLast(new LoggingHandler());
                                // 解码handler：把接收的 ByteBuf 转换为字符串
                                ch.pipeline().addLast(new StringDecoder());
                                // 自定义 handler - 把读取的数据打印出来
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    // 读事件
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 打印上一步转换好的字符串
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                // 7. 绑定监听端口
                .bind(8080);

    }

}
