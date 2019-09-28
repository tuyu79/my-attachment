package com.turan.service;
import com.turan.bo.AttachmentTaskBo;
import com.turan.bo.FileTaskBo;
import com.turan.dao.AttachmentFileTaskDAO;
import com.turan.dao.AttachmentTaskDAO;
import com.turan.enums.AttachmentTaskStatus;
import com.turan.enums.FileTaskStatus;
import com.turan.example.protocol.item.FileDataPacket;
import com.turan.example.protocol.message.dev.D_1210;
import com.turan.example.protocol.message.dev.D_1211;
import com.turan.example.protocol.message.dev.D_1212;
import com.turan.example.protocol.message.platform.P_8001;
import com.turan.example.protocol.message.platform.P_9212;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import com.turan.po.AttachmentFileTask;
import com.turan.po.AttachmentFileTaskExample;
import com.turan.po.AttachmentTask;
import com.turan.po.AttachmentTaskExample;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class T808InstructionService
{
    private static final int MAKEUP_PACKET_MAX_SIZE = 1024;

    private static final int COMMON_RESP_SUCCESS = 0;
    private static final int COMMON_RESP_FAILURE = 1;

    private Map<String /* 平台报警唯一id */, AttachmentTaskBo /* 报警上传任务 */> attachmentTaskTable = new ConcurrentHashMap<>();
    private AtomicInteger MSG_UID = new AtomicInteger(0);

    @Autowired
    private AttachmentTaskDAO taskDAO;
    @Autowired
    private AttachmentFileTaskDAO fileTaskDAO;

    public void handleInstructionData(Channel channel, T808Message msg)
    {
        int msgId = msg.getHeader().getMsgId();
        switch (msgId)
        {
            case D_1210.MSG_ID:
                handle1210(channel, msg);
                break;
            case D_1211.MSG_ID:
                handle1211(channel, msg);
                break;
            case D_1212.MSG_ID:
                handle1212(channel, msg);
                break;
        }
    }

    private void handle1210(Channel channel, T808Message msg)
    {

        D_1210 d_1210 = new D_1210();
        d_1210.fill(msg.getBody());

        log.info("received 0x1210,head: [{}],body: [{}]", msg.getHeader(), d_1210);

        AttachmentTask task = getTaskRecord(d_1210.getPlatformAlarmUid());
        if(task == null)
        {
            log.warn("task record not exist,platformAlarmUid : [{}]", d_1210.getPlatformAlarmUid());
            commonResp(channel, msg.getHeader(), COMMON_RESP_FAILURE);
            return;
        }

        AttachmentTaskBo attachmentTaskBo = new AttachmentTaskBo();
        attachmentTaskBo.setTaskNo(task.getTaskNo());
        attachmentTaskBo.setTerminalId(d_1210.getTerminalId());
        attachmentTaskBo.setPlatformAlarmUid(d_1210.getPlatformAlarmUid());
        attachmentTaskBo.setUploadType(d_1210.getUploadType());
        attachmentTaskBo.setTotalCount(d_1210.getFileNum());

        if (attachmentTaskTable.putIfAbsent(attachmentTaskBo.getPlatformAlarmUid(), attachmentTaskBo) == null) // 添加是否成功
        {
            updateTaskStatus(d_1210.getPlatformAlarmUid(), AttachmentTaskStatus.WAITING_UPLOAD, AttachmentTaskStatus.UPLOADING);
            commonResp(channel, msg.getHeader(), COMMON_RESP_SUCCESS);
        } else
        {
            log.warn("add to attachment task table error,header: [{}],task: [{}]", msg.getHeader(), attachmentTaskBo);
            commonResp(channel, msg.getHeader(), COMMON_RESP_FAILURE);
        }
    }

    private void handle1211(Channel channel, T808Message msg)
    {
        D_1211 d_1211 = new D_1211();
        d_1211.fill(msg.getBody());

        log.info("received 0x1211,header: [{}],body: [{}]", msg.getHeader(), d_1211);

        String platformAlarmUid = readPlatformAlarmUid(d_1211.getFileName());

        AttachmentTaskBo attachmentTaskBo = attachmentTaskTable.get(platformAlarmUid);
        if (attachmentTaskBo == null)
        {
            log.warn("attachment task not exist,header: [{}],body: [{}]", msg.getHeader(), d_1211);
            commonResp(channel, msg.getHeader(), COMMON_RESP_FAILURE);
            return;
        }

        FileTaskBo fileTaskBo = new FileTaskBo();
        fileTaskBo.setPlatformAlarmUid(platformAlarmUid);
        fileTaskBo.setFilename(d_1211.getFileName());
        fileTaskBo.setFileType(d_1211.getFileType());
        fileTaskBo.setTotalSize(d_1211.getSize());

        if (attachmentTaskBo.getFileTaskTable().putIfAbsent(fileTaskBo.getFilename(), fileTaskBo) == null)
        {
            Long fileTaskNo = insertFileTask(attachmentTaskBo.getTaskNo(), fileTaskBo);
            fileTaskBo.setFileTaskNo(fileTaskNo);
            commonResp(channel, msg.getHeader(), COMMON_RESP_SUCCESS);
        } else
        {
            log.warn("add to file task table error,header: [{}],task: [{}]", msg.getHeader(), fileTaskBo);
            commonResp(channel, msg.getHeader(), COMMON_RESP_FAILURE);
        }
    }

    private void handle1212(Channel channel, T808Message msg)
    {
        D_1212 d_1212 = new D_1212();
        d_1212.fill(msg.getBody());

        log.info("received 0x1212,head: [{}],body: [{}]", msg.getHeader(), d_1212);

        String platformAlarmUid = readPlatformAlarmUid(d_1212.getFilename());
        AttachmentTaskBo attachmentTaskBo = attachmentTaskTable.get(platformAlarmUid);

        if (attachmentTaskBo == null)
        {
            log.warn("attachment task not exist,filename: [{}]", d_1212.getFilename());
            return;
        }

        FileTaskBo fileTaskBo = attachmentTaskBo.getFileTaskTable().get(d_1212.getFilename());
        if (Objects.isNull(fileTaskBo))
        {
            log.warn("file task not exist,filename: [{}]", d_1212.getFilename());
            return;
        }

        int result;

        if (fileTaskBo.getTotalSize() == fileTaskBo.getReceivedSize().get())
        {
            result = 0x00;
            onFileComplete(attachmentTaskBo,fileTaskBo);
            reply9212(channel, msg.getHeader().getMobile(), d_1212, result, new ArrayList<>());
        } else
        {
            List<FileDataPacket> packets = makeupPackets(fileTaskBo);
            result = 0x01;
            reply9212(channel, msg.getHeader().getMobile(), d_1212, result, packets);
        }

    }

    private AttachmentTask getTaskRecord(String platformAlarmUid)
    {
        AttachmentTaskExample example = new AttachmentTaskExample();
        example.createCriteria().andPlatformAlarmUidEqualTo(Long.parseLong(platformAlarmUid));

        List<AttachmentTask> attachmentTasks = taskDAO.selectByExample(example);
        if(!CollectionUtils.isEmpty(attachmentTasks))
        {
            return attachmentTasks.get(0);
        }

        return null;
    }

    private void updateTaskStatus(String platformAlarmUid, AttachmentTaskStatus from, AttachmentTaskStatus to)
    {
        AttachmentTaskExample example = new AttachmentTaskExample();
        example.createCriteria()
                .andPlatformAlarmUidEqualTo(Long.parseLong(platformAlarmUid))
                .andStatusEqualTo(from.getCode());


        AttachmentTask task = new AttachmentTask();
        task.setStatus(to.getCode());

        taskDAO.updateByExampleSelective(task,example);
    }

    private Long insertFileTask(Long taskNo,FileTaskBo fileTaskBo)
    {
        AttachmentFileTask fileTask = new AttachmentFileTask();
        fileTask.setFileTaskNo(getTaskNo());
        fileTask.setAttachmentTaskNo(taskNo);
        fileTask.setFileName(fileTaskBo.getFilename());
        fileTask.setFileType(fileTaskBo.getFileType());
        fileTask.setFileSize(fileTaskBo.getTotalSize());
        fileTask.setIsCompleted(FileTaskStatus.INCOMPLETED.getCode());
        fileTask.setCreateAt(new Date());
        fileTask.setUpdateAt(new Date());

        fileTaskDAO.insert(fileTask);

        return fileTask.getFileTaskNo();
    }

    public String readPlatformAlarmUid(String fileName)
    {
        String[] splits = fileName.split("\\_|\\.");
        return splits[4]; // 根据文件名称规则获取平台报警唯一id
    }

    private void commonResp(Channel channel, Header devHeader, int result)
    {
        P_8001 p_8001 = new P_8001();
        p_8001.setDevMsgId(devHeader.getMsgId());
        p_8001.setDevMsgUid(devHeader.getMsgUid());
        p_8001.setResult(result);

        T808Message message = new T808Message();
        message.setBody(p_8001.array());

        int bodyAttr = 0;
        bodyAttr |= p_8001.array().length; // 不加密，不分包

        Header replyHeader = MessageUtil.composeHeader(P_8001.MSG_ID, bodyAttr, devHeader.getMobile(), (short) getMsgUid(), null);
        message.setHeader(replyHeader);

        log.info("reply common resp , header: [{}] , body: [{}]", replyHeader, p_8001);
        channel.writeAndFlush(message);
    }

    private int getMsgUid()
    {
        return MSG_UID.getAndIncrement();
    }

    private List<FileDataPacket> makeupPackets(FileTaskBo fileTaskBo)
    {
        List<FileDataPacket> packets = new ArrayList<>();

        List<FileDataPacket> receivedList = fileTaskBo.getCompletedPackets();

        // 写入的包顺序可能为乱序
        receivedList.sort(new Comparator<FileDataPacket>()
        {
            @Override
            public int compare(FileDataPacket o1, FileDataPacket o2)
            {
                return (int) ((o1.getOffset() - o2.getOffset()));
            }
        });

        FileDataPacket prePacket = null;
        FileDataPacket nextPacket = null;

        // 处理已接收到的包
        for (int i = 0; i < receivedList.size(); i++)
        {
            if (i == 0)
            {
                FileDataPacket firstPacket = receivedList.get(i);
                prePacket = firstPacket;

                if (firstPacket.getOffset() != 0)
                {
                    FileDataPacket makeupPacket = new FileDataPacket();
                    makeupPacket.setOffset(0);
                    makeupPacket.setLen(firstPacket.getOffset());
                    packets.add(makeupPacket);
                }
                continue;
            }

            nextPacket = receivedList.get(i); // 获取后一个包

            long prePos = prePacket.getOffset() + prePacket.getLen();
            long nextOffset = nextPacket.getOffset();

            if (nextOffset == prePos) // 如果前一个包的offset+len 和后一个包的offset相等，则没有丢包
            {
                prePacket = nextPacket;
                continue;
            }

            // 丢包的offset为前一个包的offset+len
            FileDataPacket makeupPacket = new FileDataPacket();
            makeupPacket.setOffset(prePos);
            makeupPacket.setLen(nextOffset - prePos);
            packets.add(makeupPacket);

            prePacket = nextPacket;
        }

        // 处理未收到的包
        if (receivedList.size() > 0)
        {
            FileDataPacket lastPacket = receivedList.get(receivedList.size() - 1);
            if (fileTaskBo.getTotalSize() - (lastPacket.getOffset() + lastPacket.getLen()) > 0)
            {
                FileDataPacket makeupPacket = new FileDataPacket();
                makeupPacket.setOffset(lastPacket.getOffset() + lastPacket.getLen());
                makeupPacket.setLen(fileTaskBo.getTotalSize() - (lastPacket.getOffset() + lastPacket.getLen()));
                packets.add(makeupPacket);
            }
        } else
        {
            FileDataPacket makeupPacket = new FileDataPacket();
            makeupPacket.setOffset(0);
            makeupPacket.setLen(fileTaskBo.getTotalSize());
            packets.add(makeupPacket);
        }

        // 对超过 MAKEUP_PACKET_MAX_SIZE 的包进行分包处理
        for (int i = 0; i < packets.size(); i++)
        {
            FileDataPacket packet = packets.get(i);

            if (packet.getLen() <= MAKEUP_PACKET_MAX_SIZE)
            {
                continue;
            }

            packets.remove(packet);

            long tempOffset = packet.getOffset();
            long tempLen = packet.getLen();
            while (tempLen - MAKEUP_PACKET_MAX_SIZE >= 0) // 大于等于进行分包处理
            {
                FileDataPacket dividePacket = new FileDataPacket();
                dividePacket.setOffset(tempOffset);
                dividePacket.setLen(MAKEUP_PACKET_MAX_SIZE);
                packets.add(dividePacket);

                tempOffset += MAKEUP_PACKET_MAX_SIZE;
                tempLen -= MAKEUP_PACKET_MAX_SIZE;
            }

            if (tempLen > 0) // 最后一个包
            {
                FileDataPacket tempLastPacket = new FileDataPacket();
                tempLastPacket.setOffset(tempOffset);
                tempLastPacket.setLen(tempLen);
                packets.add(tempLastPacket);
            }
        }

        return packets;
    }

    private void onFileComplete(AttachmentTaskBo attachmentTaskBo,FileTaskBo fileTaskBo)
    {
        updateFileTaskStatus(fileTaskBo.getFileTaskNo(),FileTaskStatus.INCOMPLETED,FileTaskStatus.COMPLETED);

        int completeCount = attachmentTaskBo.getCompleteCount().incrementAndGet();
        log.info("file task complete,completeCount: [{}],totalCount: [{}]", completeCount, attachmentTaskBo.getTotalCount());
        if (completeCount == attachmentTaskBo.getTotalCount())
        {
            log.info("attachment task complete,completeCount: [{}],totalCount: [{}]", completeCount, attachmentTaskBo.getTotalCount());
            // 更新任务状态
            updateAttachmentTaskStatus(attachmentTaskBo.getTaskNo(),AttachmentTaskStatus.UPLOADING,AttachmentTaskStatus.SUCCESS);
            attachmentTaskTable.remove(attachmentTaskBo.getPlatformAlarmUid());
        }
    }

    private void updateAttachmentTaskStatus(Long taskNo, AttachmentTaskStatus from, AttachmentTaskStatus to)
    {
       AttachmentTaskExample example = new AttachmentTaskExample();
       example.createCriteria().andTaskNoEqualTo(taskNo)
               .andStatusEqualTo(from.getCode());

       AttachmentTask task = new AttachmentTask();
       task.setStatus(to.getCode());

       taskDAO.updateByExampleSelective(task,example);
    }

    private void updateFileTaskStatus(Long fileTaskNo, FileTaskStatus from, FileTaskStatus to)
    {
        AttachmentFileTaskExample example = new AttachmentFileTaskExample();
        example.createCriteria()
                .andFileTaskNoEqualTo(fileTaskNo)
                .andIsCompletedEqualTo(from.getCode());

        AttachmentFileTask fileTask = new AttachmentFileTask();
        fileTask.setIsCompleted(to.getCode());

        fileTaskDAO.updateByExampleSelective(fileTask,example);
    }

    private void reply9212(Channel channel, String mobile, D_1212 d_1212, int result, List<FileDataPacket> packets)
    {
        P_9212 p_9212 = new P_9212();
        p_9212.setNameLen(d_1212.getNameLen());
        p_9212.setFileName(d_1212.getFilename());
        p_9212.setFileType(d_1212.getFileType());
        p_9212.setMakeupNum(packets.size());
        p_9212.setFileDataPackets(packets);
        p_9212.setResult(result);

        int bodyAttr = 0;
        bodyAttr |= p_9212.array().length; // 不加密，不分包

        Header replyHeader = MessageUtil.composeHeader(P_9212.MSG_ID, bodyAttr, mobile, MessageUtil.sequence(), null);
        T808Message replyMsg = MessageUtil.composePacket(replyHeader, p_9212.array());

        log.info("reply 9212,header: [{}],body: [{}]", replyHeader, p_9212);
        channel.writeAndFlush(replyMsg);
    }

    public AttachmentTaskBo getTaskByPlatformAlarmUid(String platformAlarmUid)
    {
        return attachmentTaskTable.get(platformAlarmUid);
    }

    public Long getTaskNo()
    {
        // todo turan 获取全局唯一id
        return 1L;
    }
}
