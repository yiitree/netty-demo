package cn.itcast.nio.c2;

import java.nio.ByteBuffer;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * 02、读写模式切换测试
 * buffer.clean()
 * buffer.compact()
 */
public class T02TestByteBufferReadWrite {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61); // 'a'
        debugAll(buffer);
        buffer.put(new byte[]{0x62, 0x63, 0x64}); // b  c  d
        debugAll(buffer);
//        System.out.println(buffer.get());

        buffer.flip(); // 要先改为写模式，调整指针位置
        System.out.println(buffer.get());
        debugAll(buffer);

        buffer.compact();// 调用压缩模式 - 只是把没有写的数据往前移动，后面写的时候回自动替换没有移动的
        debugAll(buffer);
        buffer.put(new byte[]{0x65, 0x6f});
        debugAll(buffer);
    }
}
