package cn.itcast.nio.c2;

/**
 * ByteBuffer和字符串相互转换
 */
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferString {
    public static void main(String[] args) {
        // -------------- String 转化 byte --------------
        // 1. 字符串转为 ByteBuffer   String.getBytes()
        // 不会自动转化模式，此时还是写模式
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes());
        debugAll(buffer1);

        // 2. Charset
        // 会自动转化模式，此时会自动转化为读模式
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        debugAll(buffer2);

        // 3. wrap
        // nio提供的工具类，也会自动转化为读模式
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        // -------------- byte 转化 String --------------
        // 4. 转为字符串 也会自动转化读模式
        String str1 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str1);

        // 转化读模式
        buffer1.flip();

        String str2 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str2);

    }
}
