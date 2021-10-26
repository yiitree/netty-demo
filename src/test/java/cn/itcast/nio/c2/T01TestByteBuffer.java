package cn.itcast.nio.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 01、byteBuffer读取数据
 */
@Slf4j
public class T01TestByteBuffer {

    public static void main(String[] args) {
        // 获取FileChannel：1. 输入输出流， 2. RandomAccessFile
        // FileChannel channel = new RandomAccessFile("words.txt", "r").getChannel()
        // FileChannel channel = new FileInputStream("data.txt").getChannel()
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            // 每次读取缓冲区大小，因此使用while循环，多次读取
            while(true) {
                // 从 channel 读取数据，向 buffer 写入，len表示读取到的字节数，直到返回结果为-1表示读取完毕
                int len = channel.read(buffer);
                log.debug("读取到的字节数 {}", len);
                if(len == -1) { // 没有内容了
                    break;
                }
                // 打印 buffer 的内容
                // 切换至读模式
                buffer.flip();
                // 是否还有剩余未读数据
                while(buffer.hasRemaining()) {
                    // 读取细节
                    byte b = buffer.get();
                    log.debug("实际字节 {}", (char) b);
                }
                // 每一次channel读取完毕后要切换为写模式
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
