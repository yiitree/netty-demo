package cn.itcast.nio.c3;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 遍历文件夹
 */
public class TestFilesWalkFileTree {

    public static void main(String[] args) throws IOException {
//        Files.delete(Paths.get("D:\\Snipaste-1.16.2-x64 - 副本"));

        /**
         * 文件删除，进入的时候由于里面有文件，所以不能删除文件夹，
         * 因此可以在进入的时候先删除文件，
         * 退出的时候再删除文件夹
         * 不会进入回收站，直接删除
         */
        Files.walkFileTree(Paths.get(
                // 遍历起点
                "D:\\Snipaste-1.16.2-x64 - 副本"),
                // 遍历后要如何处理 --- 访问者模式，遍历工作交给别人，遍历的时候操作交给自己
                new SimpleFileVisitor<Path>() {

                    /**
                     * 进入的时候删除文件
                     * @param file
                     * @param attrs
                     * @return
                     * @throws IOException
                     */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 删除文件
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

                    /**
                     * 删除文件夹
                     * @param dir
                     * @param exc
                     * @return
                     * @throws IOException
                     */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 删除文件夹
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static void m2() throws IOException {
        AtomicInteger jarCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\Program Files\\Java\\jdk1.8.0_91"), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar")) {
                    System.out.println(file);
                    jarCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("jar count:" +jarCount);
    }

    private static void m1() throws IOException {
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\Program Files\\Java\\jdk1.8.0_91"), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("====>"+dir);
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
        System.out.println("dir count:" +dirCount);
        System.out.println("file count:" +fileCount);
    }

    /**
     * 匿名内部来引用局部遍历，必须要是final的，因此不能直接用int
     * @throws IOException
     */
    private static void m3() throws IOException {
        // 匿名内部来引用局部遍历，必须要是final的，因此不能直接用int

        // 多少个按照jar结尾的文件
        AtomicInteger jarCount = new AtomicInteger();
        // 文件夹数量
        AtomicInteger dirCount = new AtomicInteger();
        // 文件数量
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\Program Files\\Java\\jdk1.8.0_91"), new SimpleFileVisitor<Path>(){
            /**
             * 遍历之前
             * @param dir
             * @param attrs
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir);
                fileCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            /**
             * 遍历的时候
             * @param file
             * @param attrs
             * @return
             * @throws IOException
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
             * @param file
             * @param exc
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.out.println(file);
                fileCount.incrementAndGet();
                return super.visitFileFailed(file, exc);
            }

            /**
             * 遍历之后处理
             * @param dir
             * @param exc
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        });
        System.out.println("dir count:" +dirCount);
        System.out.println("file count:" +fileCount);
    }



}
