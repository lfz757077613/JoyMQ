import com.google.common.base.Stopwatch;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class SimpleTest {

    //50mb
    private long fileSize = 1024 * 1024 * 100;
    private File randomAccessFile;
    private File outputStreamFile;
    private File fileChannel;
    private File mappedByteBufferFile;

    @Test
    public void rwfile32() throws IOException {
        int byteSize = 32;
        writeRandomAccessFile(byteSize);
        writeFileOutputStream(byteSize);
        writeFileChannel(byteSize);
        writeMappedByteBuffer(byteSize);
        readRandomAccessFile(byteSize);
        readFileChannel(byteSize);
        readMappedByteBuffer(byteSize);
    }

    //force 对性能影响很大，应该单独测试
    @Test
    public void writeAsyncForce32() throws IOException {
        int byteSize = 32;
        writeAsyncForceFileChannel(byteSize);
        writeAsyncForceMappedByteBuffer(byteSize);
    }

    @Test
    public void rwfile64() throws IOException {
        int byteSize = 64;
        writeRandomAccessFile(byteSize);
        writeFileOutputStream(byteSize);
        writeFileChannel(byteSize);
        writeMappedByteBuffer(byteSize);
        readRandomAccessFile(byteSize);
        readFileChannel(byteSize);
        readMappedByteBuffer(byteSize);
    }

    @Test
    public void writeAsyncForce64() throws IOException {
        int byteSize = 64;
        writeAsyncForceFileChannel(byteSize);
        writeAsyncForceMappedByteBuffer(byteSize);
    }

    @Test
    public void rwfile4096() throws IOException {
        System.out.println("写文件大小为" + fileSize / 1024 / 1024 + "mb，每次写入32byte,64byte,4KB,8KB进行测试");
        System.gc();
        randomAccessFile = getFile("RandomAccessFile", true);
        outputStreamFile = getFile("FileOutputStream", true);
        fileChannel = getFile("FileChannel", true);
        mappedByteBufferFile = getFile("MappedByteBuffer", true);
        int byteSize = 4096;
        writeRandomAccessFile(byteSize);
        writeFileOutputStream(byteSize);
        writeFileChannel(byteSize);
        writeMappedByteBuffer(byteSize);
        readRandomAccessFile(byteSize);
        readFileChannel(byteSize);
        readMappedByteBuffer(byteSize);
    }

    @Test
    public void writeAsyncForce4096() throws IOException {
        int byteSize = 4096;
        writeAsyncForceFileChannel(byteSize);
        writeAsyncForceMappedByteBuffer(byteSize);
    }

    @Test
    public void rwfile8192() throws IOException {
        int byteSize = 8192;
        writeRandomAccessFile(byteSize);
        writeFileOutputStream(byteSize);
        writeFileChannel(byteSize);
        writeMappedByteBuffer(byteSize);
        readRandomAccessFile(byteSize);
        readFileChannel(byteSize);
        readMappedByteBuffer(byteSize);
    }

    @Test
    public void writeAsyncForce8192() throws IOException {
        int byteSize = 8192;
        writeAsyncForceFileChannel(byteSize);
        writeAsyncForceMappedByteBuffer(byteSize);
    }

    private void writeRandomAccessFile(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        RandomAccessFile ra = new RandomAccessFile(randomAccessFile, "rw");
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        int count = 0;
        while (count < fileSize) {
            count += arr.length;
            ra.write(arr);
        }
        logFormat("writeRandomAccessFile", lenth, stopwatch);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> IOUtils.closeQuietly(ra)));
    }

    private void readRandomAccessFile(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        RandomAccessFile ra = new RandomAccessFile(randomAccessFile, "r");
        while (true) {
            byte[] arr = new byte[lenth];
            int len = ra.read(arr);
            if (len == -1) {
                break;
            }
        }
        logFormat("readRandomAccessFile", lenth, stopwatch);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> IOUtils.closeQuietly(ra)));
    }

    private void writeFileOutputStream(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        FileOutputStream fo = new FileOutputStream(outputStreamFile);
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        int count = 0;
        while (count < fileSize) {
            count += arr.length;
            fo.write(arr);
        }
        //fo.flush();
        logFormat("writeFileOutputStream", lenth, stopwatch);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> IOUtils.closeQuietly(fo)));
    }

    private void writeFileChannel(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        FileChannel fc = new RandomAccessFile(fileChannel, "rw").getChannel();
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        int count = 0;
        ByteBuffer buf = ByteBuffer.wrap(arr);
        while (count < fileSize) {
            count += arr.length;
            buf.clear();
            buf.put(arr);
            buf.flip();
            while (buf.hasRemaining()) {
                fc.write(buf);
            }
        }
        logFormat("writeFileChannel", lenth, stopwatch);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fc.force(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private void readFileChannel(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        FileChannel fc = new RandomAccessFile(fileChannel, "rw").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(lenth);
        while (true) {
            int len = fc.read(byteBuffer);
            byteBuffer.clear();
            if (len == -1) {
                break;
            }
        }
        logFormat("readFileChannel", lenth, stopwatch);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fc.force(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private void writeMappedByteBuffer(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        MappedByteBuffer mb = new RandomAccessFile(mappedByteBufferFile, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        int count = 0;
        while (count < mb.capacity()) {
            count += arr.length;
            mb.put(arr);
        }
        logFormat("writeMappedByteBuffer", lenth, stopwatch);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> mb.force()));
    }

    private void readMappedByteBuffer(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        MappedByteBuffer mb = new RandomAccessFile(mappedByteBufferFile, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        byte[] arr = new byte[lenth];
        while (mb.hasRemaining()) {
            mb.get(arr);
            arr = new byte[lenth];
        }
        logFormat("readMappedByteBuffer", lenth, stopwatch);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> mb.force()));
    }

    private static volatile int curLen = 0;
    private int page4 = 1024 * 4 * 4;

    private void writeAsyncForceFileChannel(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        FileChannel fc = new RandomAccessFile(fileChannel, "rw").getChannel();
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            for (; ; ) {
                if (curLen < page4 && curLen % page4 == 0) {
                    try {
                        fc.force(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (latch.getCount() == 0) {
                    break;
                }
            }
        }).start();
        ByteBuffer buf = ByteBuffer.wrap(arr);
        while (curLen < fileSize) {
            curLen += arr.length;
            buf.clear();
            buf.put(arr);
            buf.flip();
            while (buf.hasRemaining()) {
                fc.write(buf);
            }
        }
        curLen = 0;
        latch.countDown();
        logFormat("writeAsyncForceFileChannel", lenth, stopwatch);
    }

    private static volatile int curLen2 = 0;

    private void writeAsyncForceMappedByteBuffer(int lenth) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        MappedByteBuffer mb = new RandomAccessFile(mappedByteBufferFile, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        byte[] arr = new byte[lenth];
        Arrays.fill(arr, (byte) 2);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            for (; ; ) {
                if (curLen2 > page4 && curLen2 % page4 == 0) {
                    mb.force();
                }
                if (latch.getCount() == 0) {
                    break;
                }
            }
        }).start();
        while (curLen2 < mb.capacity()) {
            curLen2 += arr.length;
            mb.put(arr);
        }
        curLen2 = 0;
        latch.countDown();
        logFormat("writeAsyncForceMappedByteBuffer", lenth, stopwatch);
    }

    private void logFormat(String method, int length, Stopwatch stopwatch) {
        System.out.println(String.format("%s, %s.byte ,cost：%s", method, length, stopwatch));
    }

    private File getFile(String type, boolean isCreate) throws IOException {
        String fileName = String.format(System.getProperty("user.home") + "/%s-%s.txt", type, System.currentTimeMillis());
        File file = new File(fileName);
        if (!file.exists()) file.createNewFile();
        return file;
    }
}
