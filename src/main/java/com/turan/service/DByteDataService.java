package com.turan.service;

import com.google.common.base.Joiner;
import com.turan.bo.AttachmentTaskBo;
import com.turan.bo.FileTaskBo;
import com.turan.config.AttachmentConfig;
import com.turan.example.protocol.item.FileDataPacket;
import com.turan.example.protocol.message.dev.D_ByteData;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class DByteDataService
{

    @Autowired
    private T808InstructionService instructionService;
    @Autowired
    private AttachmentConfig attachmentConfig;

    private static final String DATE_FORMAT = "yyyyMMdd";

    public void handleByteData(D_ByteData data)
    {
        String platformAlarmUid = instructionService.readPlatformAlarmUid(data.getFilename());
        AttachmentTaskBo attachmentTaskBo = instructionService.getTaskByPlatformAlarmUid(platformAlarmUid);

        if (attachmentTaskBo == null)
        {
            log.warn("attachment task not exist,filename: [{}]", data.getFilename());
            return;
        }

        FileTaskBo fileTaskBo = attachmentTaskBo.getFileTaskTable().get(data.getFilename());

        if (Objects.isNull(fileTaskBo))
        {
            log.warn("file task not exist,filename: [{}]", data.getFilename());
            return;
        }

        onFileData(fileTaskBo, data);
    }

    private void onFileData(FileTaskBo fileTaskBo, D_ByteData data)
    {
        FileDataPacket packet = new FileDataPacket();
        packet.setOffset(data.getOffset());
        packet.setLen(data.getLen());

        synchronized (fileTaskBo) // 避免重复写入
        {
            if (fileTaskBo.getCompletedPackets().contains(packet))
            {
                log.warn("duplicated packet,data: [{}]", data);
                return;
            }
        }

        writeToFile(fileTaskBo.getPlatformAlarmUid(),data);

        fileTaskBo.getCompletedPackets().add(packet);
        AtomicLong receivedSize = fileTaskBo.getReceivedSize();
        receivedSize.compareAndSet(receivedSize.get(), receivedSize.get() + data.getLen());
    }

    private void writeToFile(String platformAlarmUid, D_ByteData data)
    {
        // /home/data/20190814/platformAlarmUid/filename
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        String path = Joiner.on(File.separator).join(attachmentConfig.getFileBasePath(),
                dateFormat.format(new Date()), platformAlarmUid);

        File fileDir = new File(path);
        if(!fileDir.exists() && !fileDir.mkdirs())
        {
            log.error("mkdir error,fileDir : [{}]",fileDir);
            return;
        }

        String filePath = path + File.separator + data.getFilename();
        try
        {
            new File(filePath).createNewFile();
        } catch (IOException e)
        {
            log.error("IOException! filename : [{}]", path, e);
        }

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath,"rw"))
        {
            FileChannel channel = randomAccessFile.getChannel();
            channel.position(data.getOffset());

            channel.write(Unpooled.wrappedBuffer(data.getBody()).nioBuffer());
        } catch (FileNotFoundException e)
        {
            log.error("FileNotFoundException! filename : [{}]", path, e);
        } catch (IOException e)
        {
            log.error("IOException! filename : [{}]", path, e);
        }

        log.info("write to file: [{}]", data);
    }
}
