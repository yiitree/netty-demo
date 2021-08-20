package cn.itcast.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: 曾睿
 * @Date: 2021/8/20 11:00
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("data.txt", "rw")) {
            // 准备输出输入流
            FileChannel channel = file.getChannel();
            // 准备缓冲区，划分10个字节
            ByteBuffer buffer = ByteBuffer.allocate(10);
            // 使用循环
            do {
                // 从 channel 读取数据，写到 buffer,返回的为读到的字节输，读取到-1表示读取完毕
                int len = channel.read(buffer);

                log.debug("读到字节数：{}", len);
                if (len == -1) {
                    break;
                }
                // 切换 buffer 读模式
                buffer.flip();
                // 检查是否有剩余的
                while(buffer.hasRemaining()) {
                    // 默认buffer.get()读一个字节 --- 强转化为字符
                    log.debug("{}", (char)buffer.get());
                }
                // 切换 buffer 写模式
                buffer.clear();
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
