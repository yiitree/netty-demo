package cn.itcast.nio.c2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * 假如一个文件里面有三个单词，每个单词大小已知，读取分别得到这三个单词？
 * 分散读取
 * 使用一个channel，分别读取到三个ByteBuffer里
 */
public class T06TestScatteringReads {
    public static void main(String[] args) {
        try (FileChannel channel = new RandomAccessFile("words.txt", "r").getChannel()) {
            ByteBuffer b1 = ByteBuffer.allocate(3); // 每一个都创建已知字节大小
            ByteBuffer b2 = ByteBuffer.allocate(3);
            ByteBuffer b3 = ByteBuffer.allocate(5);
            // 直接一次性读取三个ByteBuffer里
            channel.read(new ByteBuffer[]{b1, b2, b3});
            b1.flip();
            b2.flip();
            b3.flip();
            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
        }
    }
}
