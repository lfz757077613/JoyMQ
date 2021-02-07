package cn.laifuzhi.joymq.broker.store;

import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

@Slf4j
public class MsgFile implements Closeable {
    public static final int FILE_SIZE = 1024 * 1024 * 1024;
    private File file;
    private long initialOffset;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private volatile long flushPosition;

    public MsgFile(File file) throws IOException {
        this.file = file;
        this.initialOffset = Long.parseLong(file.getName());
        this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
        this.mappedByteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0, FILE_SIZE);
        this.flushPosition = FILE_SIZE;
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
        PlatformDependent.freeDirectBuffer(mappedByteBuffer);
    }
}
