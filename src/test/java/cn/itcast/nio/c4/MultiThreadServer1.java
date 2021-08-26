package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

@Slf4j
public class MultiThreadServer1 {
    public static void main(String[] args) throws IOException {

        // 创建一个服务器
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 创建一个boss选择器，只监听accept事件
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
                    // 建立连接
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // round robin 轮询
                    // boss 调用 初始化 selector ,启动 worker-0
                    // 调用work进行初始化
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

        /** 消息队列，两个消息之间传递数据 */
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
            // 向队列中添加任务，但是这个任务并没有马上执行
            queue.add(() -> {
                try{
                    sc.register(selector, SelectionKey.OP_READ, null);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            // 唤醒selector方法 --- 类似interrupt方法，相当于一个票，可以先唤醒再执行，也可以先阻塞再唤醒
            selector.wakeup();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    // worker-0  阻塞 监听是否有事件发生
                    selector.select();
                    // work-0 从队列中取任务
                    Runnable task = queue.poll();
                    if(task != null){
                        // 执行任务 --- 在这里执行work的注册
                        task.run();
                    }

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
