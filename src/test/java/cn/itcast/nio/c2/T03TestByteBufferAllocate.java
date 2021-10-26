package cn.itcast.nio.c2;

import java.nio.ByteBuffer;

/**
 * 分配空间方法，不能动态调整
 * 后面netty做了增强， 就可以动态调整了
 */
public class T03TestByteBufferAllocate {
    public static void main(String[] args) {

        System.out.println(ByteBuffer.allocate(16).getClass());
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
        /*
        分配的空间位置不同
        是否进行垃圾回收，其实也是上面的关系
        allocate --- class java.nio.HeapByteBuffer
            - java 堆内存，读写效率较低，
            - 受到 GC 的影响 (这里主要是零拷贝)
        allocateDirect --- class java.nio.DirectByteBuffer (netty会对此进行封装。)
            - 直接内存，读写效率高（少一次拷贝），不会受 GC 影响。
            - 因为要调用操作系统函数，分配的效率低，而且要进行释放，否则可能堆溢出
         */
    }
}
