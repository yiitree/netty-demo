package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {

        // 设置一个boss线程，只负责连接 监听accept事件
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        // 1. 创建固定数量的 worker 并初始化
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }

        AtomicInteger index = new AtomicInteger();

        while(true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // round robin 轮询
                    // boss 调用 初始化 selector , 启动 worker-0
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }


    /**
     * 静态内部类
     * Worker类，实际处理的类，每一个worker都有一个线程
     */
    static class Worker implements Runnable{

        /** worker类都有一个线程做监听处理 */
        private Thread thread;
        /** worker的选择器 */
        private Selector selector;
        /** worker线程的名字 */
        private String name;
        // 还未初始化
        private volatile boolean start = false;

        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name) {
            this.name = name;
        }

        /**
         * 初始化线程，和 selector
         * 相当于单例模式，防止创建多个
         * @param sc
         * @throws IOException
         */
        public void register(SocketChannel sc) throws IOException {
            if(!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }
            // 唤醒 select 方法 boss
            selector.wakeup();
            // boss
            sc.register(selector, SelectionKey.OP_READ, null);
        }

        @Override
        public void run() {
            while(true) {
                try {
                    // worker-0  阻塞 监听是否有事件发生
                    selector.select();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        // 监听读事件
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("read...{}", channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
