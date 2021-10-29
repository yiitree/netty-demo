package cn.itcast.nio.c3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 文件复制，两个channel传输数据
 * 注意：传输文件大小最多传2g
 */
public class T01TestFileChannelTransferTo {
    public static void main(String[] args) {

        try (
                FileChannel from = new FileInputStream("data.txt").getChannel();
                FileChannel to = new FileOutputStream("to.txt").getChannel()
        ) {
            // 效率高，使用了transferTo的，底层会利用操作系统的零拷贝进行优化, 一次性最大传输2g数据
            long size = from.size();
            // left 变量代表还剩余多少字节
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + " left:" + left);
                left -= from.transferTo((size - left), left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
