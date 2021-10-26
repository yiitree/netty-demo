package cn.itcast.nio.c2;

import java.nio.ByteBuffer;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * buffer读取方法
 */
public class T04TestByteBufferRead {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd' ,'e'});
        // 切换为读模式
        buffer.flip();

        System.out.println("----------------rewind从头开始读---------------");

        // 先调用读取到4的位置
        buffer.get(new byte[4]);
        debugAll(buffer);

        System.out.println("---------------- rewind 把position设置为0 ---------------");
        // 头从开始读取 - 其实就是把position转为0
        buffer.rewind();
        System.out.println((char)buffer.get());

        System.out.println("---------------mark & reset----------------");

        // mark & reset 就是做一个标记，反复读
        // mark 做一个标记，记录 position 位置，
        // reset 是将 position 重置到 mark 的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        System.out.println("---------------mark----------------");
        // 加标记，索引2 的位置
        buffer.mark();
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        System.out.println("---------------reset----------------");
        // 将 position 重置到索引 2
        buffer.reset();
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        System.out.println("---------------get(i)----------------");
        // 按照索引读，不会改变指针位置。get(i) 不会改变读索引的位置
        System.out.println((char) buffer.get(3));
        debugAll(buffer);
    }
}
