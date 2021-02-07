package cn.laifuzhi.joymq.broker.store;

import cn.laifuzhi.joymq.broker.config.StaticConfig;
import cn.laifuzhi.joymq.common.model.SendMsgReq;
import cn.laifuzhi.joymq.common.utils.Crc32;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static cn.laifuzhi.joymq.broker.store.MsgFile.FILE_SIZE;

@Slf4j
@Component
public class MsgService {
    @Resource
    private StaticConfig staticConfig;

    private ReentrantLock lock = new ReentrantLock();
    private File runningFile;
    private CopyOnWriteArrayList<MsgFile> msgFileList;
    @PostConstruct
    private void init() throws IOException {
        File msgFileDir = new File(SystemUtils.USER_HOME + File.separator + staticConfig.getMsgLogStoreDirInHome());
        if (msgFileDir.mkdirs()) {
            log.info("create msg file dir:{}", msgFileDir.getAbsolutePath());
        }
        msgFileList = Lists.newCopyOnWriteArrayList();
        File[] msgFiles = msgFileDir.listFiles();
        if (msgFiles == null) {
            throw new IOException("msg file dir listFiles null dir:" + msgFileDir.getAbsolutePath());
        }
        Arrays.sort(msgFiles);
        for (File file : msgFiles) {
            if (file.length() != FILE_SIZE) {
                throw new IOException("msg file size wrong file:" + file.getAbsolutePath());
            }
            MsgFile mappedFile = new MsgFile(file);
            msgFileList.add(mappedFile);
            log.info("load msg file success file:{}", file.getAbsolutePath());
        }
        runningFile = new File(SystemUtils.USER_HOME + File.separator + staticConfig.getBrokerRunningFile());
        if (!runningFile.exists()) {
            Files.createParentDirs(runningFile);
            if (!runningFile.createNewFile()) {
                throw new IOException("runningFile create fail file:" + runningFile.getAbsolutePath());
            }
            normalRecover();
            return;
        }
        errorRecover();
    }

    @PreDestroy
    private void destroy() throws IOException {
        for (MsgFile msgFile : msgFileList) {
            msgFile.close();
        }
        if (!runningFile.delete()) {
            log.error("runningFile delete fail file:{}", runningFile.getAbsolutePath());
        }
        log.info("MsgService shutdown");
    }

    private void loadMsgFile() throws IOException {

    }

    private void normalRecover() throws IOException {
        MsgFile lastMsgFile = msgFileList.get(msgFileList.size() - 1);

    }

    private void errorRecover() throws IOException {

    }

    public static void main(String[] args) throws IOException {
//        File file = new File(System.getProperty("user.home") + "/joymq/msg/");
//        System.out.println(file.getAbsolutePath());
//        System.out.println(file.mkdirs());
//        System.out.println(file.exists());
        File file = new File(System.getProperty("user.home") + "/joymq/msg/11");
        Files.createParentDirs(file);
        file.createNewFile();
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
//        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
//        fileChannel.close();
//        PlatformDependent.freeDirectBuffer(mappedByteBuffer);
//        System.out.println();
        byte[] bytes = StringUtils.repeat("赖", 1024).getBytes();
        byte[] bytes1 = StringUtils.repeat("赖", 512).getBytes();
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
        byteBuf.writeBytes(bytes);
        for (int i = 0; i < 100000; i++) {
            byteBuf.writeBytes(fileChannel, byteBuf.readableBytes());
            fileChannel.force(false);
            byteBuf.resetReaderIndex();
        }
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        ByteBuffer wrap1 = ByteBuffer.wrap(bytes1);
        for (int i = 0; i < 100000; i++) {
            fileChannel.write(wrap);
            fileChannel.force(false);
            wrap.clear();
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
        }
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            fileChannel.write(wrap);
            fileChannel.force(false);
            wrap.clear();
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    public boolean writeMsg(SendMsgReq sendMsgReq) {
        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                log.error("writeMsg tryLock fail reqfrom:{} dataId:{}", sendMsgReq.getReqFrom(), sendMsgReq.getDataId());
                return false;
            }
            try {

            } finally {
                lock.unlock();
            }
            return true;
        } catch (Exception e) {
            log.error("saveMsg error", e);
            return false;
        }
    }

    public boolean flushMsg(SendMsgReq sendMsgReq) {
        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                log.error("saveMsg tryLock fail");
                return false;
            }
            try {

            } finally {
                lock.unlock();
            }
            return true;
        } catch (Exception e) {
            log.error("saveMsg error", e);
            return false;
        }
    }
}
