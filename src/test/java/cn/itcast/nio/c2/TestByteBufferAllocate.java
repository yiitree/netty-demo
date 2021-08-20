package cn.itcast.nio.c2;

import java.nio.ByteBuffer;

/**
 * 分配空间方法，不能动态调整
 */
public class TestByteBufferAllocate {
    public static void main(String[] args) {

        System.out.println(ByteBuffer.allocate(16).getClass());
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
        /*
        allocate --- class java.nio.HeapByteBuffer
            - java 堆内存，读写效率较低，受到 GC 的影响 (这里主要是零拷贝)
        allocateDirect --- class java.nio.DirectByteBuffer
            - 直接内存，读写效率高（少一次拷贝），不会受 GC 影响，分配的效率低。
            因为要调用操作系统函数，而且要进行释放，否则可能堆溢出
            netty会对此进行封装。
         */
    }
}
