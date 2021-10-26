package cn.itcast.nio.c2;

import javax.xml.stream.events.Characters;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * 案例： 黏包和半包
 * 黏包:
 *  发送的时候一次性发送多个，一起到服务器缓冲区，服务器读取数据的时候一次性多个数据一起读取，因此会把两个包数据连接一起读取。
 * 半包：
 *  由于缓冲区大小有限，因此服务器读取的时候可能会把一条数据分两次ByteBuffer读取
 * 问题：其实两个情况都是会导致一个问题，就是一条数据会被分开读
 *
 *  网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
 *  但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
 *      Hello,world\n
 *      I'm zhangsan\n
 *      How are you?\n
 *  变成了下面的两个 byteBuffer (黏包，半包)
 *      Hello,world\nI'm zhangsan\nHo
 *      w are you?\n
 *  现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据
 */
public class T08TestByteBufferExam {

    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);// 读取一次数据
        source.put("w are you?\n".getBytes());
        split(source);// 再读取一次数据
    }

    /**
     * 不能一次性获取，因此要实时接收
     */
    private static void split(ByteBuffer source) {
        // 切换为读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                // 就是每次读取的时候创建一个新的byteBuffer，然后每次都拷贝数据
                // 消息长度
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact();
    }
}
