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
public class T02Server1 {

    /**
     * 方案二：非阻塞式
     * 还是创建一个线程，但是这个线程一直while循环
     * 此时有个问题：就是很消耗资源，需要一直循环遍历
     */
    public static void main(String[] args) throws IOException {

        // 0.创建一个byteBuffer来缓存每次连接客户端访问的数据
        // 由于只是创建一个buffer，因此是单线程处理
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置为非阻塞模式
        /*
        这里可以解解决客户端建立连接问题，所有连接都建立，保存在一个list中，但是这些连接不一定会立刻发送数据，
        只是先进行保存。读取连接数据的时候让另一个线程来执行，互补影响
        相当于大厅招待员，把客户先带到座位上，点餐让另外线程来，一个线程只干一件事
         */
        ssc.configureBlocking(false);// 默认为阻塞模式

        // 2.绑定监听端口
        ssc.bind(new InetSocketAddress(8888));

        // 3.建立连接的集合（主要是为了连接可以复用）
        List<SocketChannel> channels = new ArrayList<>();

        while (true){
            // 4.accept建立客户端连接（tcp三次握手）,
            // 由于希望连接可以多次调用，因此放在while循环中
            // 返回读写通道，用SocketChannel来进行读写操作
//            log.debug("准备建立连接");
            // 如果没有建立，sc为null - 这里相当于一直判断，是否有连接建立，否则就一直判断是否为空
            /*
            这里相当于一个服务员一直来回问用户是否有要点餐，就非常繁忙，并且有一个问题，
            这个服务员在接收一个客户点餐数据的时候就无法再干别的事情，其他人无法点餐
             */
            SocketChannel sc = ssc.accept();
            if(sc != null){
                log.debug("连接建立完毕，{}",sc);
                sc.configureBlocking(false);// 把socketChannel也设置为非阻塞模式 - 此时read也是非阻塞
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                // 5.接收客户端发来的数据，读取到ByteBuffer中
//                log.debug("准备开始读取数据，{}",sc);
                // read此时也变成非阻塞模式
                final int read = channel.read(buffer); // 也变成了非阻塞，如果没有数据返回0
                if(read >0){
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

}
