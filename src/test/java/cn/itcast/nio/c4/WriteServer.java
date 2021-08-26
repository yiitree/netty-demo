package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 可写事件
 * 服务器端 向 客户端 写数据
 */
public class WriteServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                // 如果是Accept事件就建立连接
                if (key.isAcceptable()) {
                    // 因为ServiceSocketChannel只有一个，所以可以直接获取ssc.accept()
                    // 如果是SocketChannel就不可以，因为有多个
                    SocketChannel sc = ssc.accept();

                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, 0, null);
                    sckey.interestOps(SelectionKey.OP_READ);
                    // 1. 向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 50000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());

//                    // 进行多次写入 --- 这样不好，因为会阻塞，如果没发完就会一直阻塞循环发送，直至发送完毕为止
//                    while (buffer.hasRemaining()){
//                        int write = sc.write(buffer);
//                        System.out.println(write);
//                    }

                    // 2. 写入到channel，不能一次性写完，因此要进行判断。返回值代表实际写入的字节数
                    int write = sc.write(buffer);
                    System.out.println(write);
                    // 3. 判断是否有剩余内容 --- 如果一次性没有发送完毕，就改为设置关注可写事件
                    if (buffer.hasRemaining()) {
                        // 4. 关注可写事件         1                     4
                        // 先拿到原来的关注的时间，然后修改为关注读事件
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
//                        sckey.interestOps(sckey.interestOps() | SelectionKey.OP_WRITE);
                        // 5. 把未写完的数据挂到 sckey 上，是为了保存到最新数据
                        sckey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer); // 这里不需要再判断是否写完，因为已经while循环了，下次重新进入if判断
                    System.out.println(write);
                    // 6. 清理操作
                    if (!buffer.hasRemaining()) {
                        // 需要清除buffer
                        key.attach(null);
                        //不需关注可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }

    public static void main2(String[] args) throws Exception{

        // 1.开启一个服务器端
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置服务端不阻塞
        ssc.configureBlocking(false);

        // 2.创建一个选择器
        Selector selector = Selector.open();
        // 设置选择器监听事件
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        // 设置选择器的监听端口
        ssc.bind(new InetSocketAddress(8888));

        while(true){
            // 3.选择器开始监听
            selector.select();
            // 使用迭代器遍历所有的监听器中的监听key
            // 因为使用完一个要删除，所以使用迭代器模式，遍历删除
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()){
                // 设置一个临时变量保存key，然后删除，防止后面忘记删除
                SelectionKey key = iter.next();
                // 使用完毕要删除
                iter.remove();

                // 4.处理事件，根据监听事件的类别进行处理
                // 如果是Accept事件 --- 建立连接
                if(key.isAcceptable()){
                    // 是accept事件后，调用服务器进行连接
                    // 因为我们只有一个服务器，所以直接从选择器中拿到服务器的channel
                    SocketChannel sc = ssc.accept();
                    // 设置服务器不阻塞
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, 0, null);
                    sckey.interestOps(SelectionKey.OP_READ);

                    // 5.向客户端发送数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 500000; i++) {
                        sb.append("a");
                    }
                    // 把要发送的数据写到buffer中
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());

                    // 发送数据
                    int write = sc.write(buffer);
                    System.out.println(write);
                    // 检查是否发送完毕
                    if(buffer.hasRemaining()) {
                        // 没有发送完毕，把附件替换buffer
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        sckey.attach(buffer);
                    }
                // 如果是可写事件
                }else if(key.isWritable()){
                    // 从附件中拿到buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    System.out.println(write);
                    // 发送完毕，进行清理
                    if (!buffer.hasRemaining()) {
                        // 需要清除buffer
                        key.attach(null);
                        //不需关注可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                    }

                }

            }
        }
    }

}
