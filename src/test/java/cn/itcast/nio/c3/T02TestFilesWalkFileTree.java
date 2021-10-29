package cn.itcast.nio.c3;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 遍历文件夹
 */
public class T02TestFilesWalkFileTree {

    public static void main(String[] args) throws IOException {
//        Files.delete(Paths.get("D:\\Snipaste-1.16.2-x64 - 副本"));
        /*
         * 访问者模式，帮你做遍历，但是遍历后的处理交给调用者编写。
         *
         * 文件删除，进入的时候由于里面有文件，所以不能删除文件夹，
         * 因此可以在进入的时候先删除文件，
         * 退出的时候再删除文件夹
         * 不会进入回收站，直接删除
         */
        Files.walkFileTree(
                // 遍历起点
                Paths.get("D:\\Snipaste-1.16.2-x64 - 副本"),
                // 遍历后要如何处理 --- 访问者模式，遍历工作交给别人，遍历的时候操作交给自己
                new SimpleFileVisitor<Path>() {
                    /**
                     * 如果是文件夹
                     * 进入的时候删除文件
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // 删除文件
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    /**
                     * 如果是文件
                     * 删除文件夹
                     */
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        // 删除文件夹
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                }
        );
    }

//    public static void main(String[] args) throws IOException {
//        m2();
//    }

    // 遍历文件的个数
    private static void m2() throws IOException {
        // 使用AtomicInteger并不是为了线程安全
        // 由于是静态内部类，访问外部类的时候必须要是final的，因此无法直接使用int，需要进行包装成对象，然后直接修改对象内容，就是栈不改，改堆空间
        AtomicInteger jarCount = new AtomicInteger();
        final int[] i = {0};
        Files.walkFileTree(
                Paths.get("C:\\Program Files\\Java\\jdk1.8.0_221"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".jar")) {
                            System.out.println(file);
                            jarCount.incrementAndGet();
                            i[0]++;
                        }
                        return super.visitFile(file, attrs);
                    }
                });
        System.out.println("jar count:" + jarCount);
        System.out.println("i count:" + i[0]);
    }

    private static void m1() throws IOException {
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(
                Paths.get("C:\\Program Files\\Java\\jdk1.8.0_91"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println("====>" + dir);
                        dirCount.incrementAndGet();
                        return super.preVisitDirectory(dir, attrs);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file);
                        fileCount.incrementAndGet();
                        return super.visitFile(file, attrs);
                    }
                });
        System.out.println("dir count:" + dirCount);
        System.out.println("file count:" + fileCount);
    }

    /**
     * 匿名内部来引用局部遍历，必须要是final的，因此不能直接用int
     */
    private static void m3() throws IOException {
        // 匿名内部来引用局部遍历，必须要是final的，因此不能直接用int

        // 多少个按照jar结尾的文件
        AtomicInteger jarCount = new AtomicInteger();
        // 文件夹数量
        AtomicInteger dirCount = new AtomicInteger();
        // 文件数量
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(
                Paths.get("C:\\Program Files\\Java\\jdk1.8.0_91"),
                new SimpleFileVisitor<Path>() {
                    /**
                     * 遍历之前
                     */
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println(dir);
                        fileCount.incrementAndGet();
                        return super.preVisitDirectory(dir, attrs);
                    }

                    /**
                     * 遍历的时候
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".jar")) {
                            System.out.println(file);
                            jarCount.incrementAndGet();
                        }
                        System.out.println(file);
                        fileCount.incrementAndGet();
                        return super.visitFile(file, attrs);
                    }

                    /**
                     * 遍历失败时处理
                     */
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        System.out.println(file);
                        fileCount.incrementAndGet();
                        return super.visitFileFailed(file, exc);
                    }

                    /**
                     * 遍历之后处理
                     */
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return super.postVisitDirectory(dir, exc);
                    }
                });
        System.out.println("dir count:" + dirCount);
        System.out.println("file count:" + fileCount);
    }


}
