package com.turan.service;

import com.turan.bo.AttachmentTaskBo;
import com.turan.bo.FileTaskBo;
import com.turan.dao.AttachmentFileTaskDAO;
import com.turan.dao.AttachmentTaskDAO;
import com.turan.example.protocol.item.AttachFileItem;
import com.turan.example.protocol.item.FileDataPacket;
import com.turan.example.protocol.message.activesafe.AlarmMarkUid;
import com.turan.example.protocol.message.dev.D_1210;
import com.turan.example.protocol.message.dev.D_1211;
import com.turan.example.protocol.message.dev.D_1212;
import com.turan.example.protocol.message.dev.D_ByteData;
import com.turan.example.protocol.message.platform.P_8001;
import com.turan.example.protocol.message.platform.P_9212;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import com.turan.po.AttachmentTask;
import com.turan.po.AttachmentTaskExample;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class T808InstructionServiceTest
{

    @InjectMocks
    private T808InstructionService instructionService;
    @Mock
    private AttachmentTaskDAO taskDAO;
    @Mock
    private AttachmentFileTaskDAO fileTaskDAO;

    private T808Message message1210;
    private T808Message message1211;
    private T808Message message1212;
    private D_ByteData d_byteData1;
    private D_ByteData d_byteData2;
    private List<AttachmentTask> tasks;
    private EmbeddedChannel channel;

    private static final String platformAlarmUuid = "99999";
    private static final String fileName = "00_0_6401_1_" + platformAlarmUuid + ".jpg";

    @Before
    public void init() throws Exception
    {
        channel = new EmbeddedChannel();

        AttachmentTask task = new AttachmentTask();
        task.setTaskNo(123456L);

        tasks = new ArrayList<>();
        tasks.add(task);

        int fileNameLen = fileName.getBytes(MessageUtil.DEFAULT_CHARSET).length;
        int fileSize = 6;

        //===============================1210==========================================

        message1210 = new T808Message();

        Header header = new Header();
        header.setMsgId(D_1210.MSG_ID);
        header.setMobile("13452202456");
        header.setMsgUid(1);
        header.setPkgDivideInfo(null);

        D_1210 d_1210 = new D_1210();
        d_1210.setTerminalId("123");
        d_1210.setPlatformAlarmUid(platformAlarmUuid);
        d_1210.setUploadType(0x00);
        d_1210.setFileNum(1);

        AlarmMarkUid alarmMark = new AlarmMarkUid();
        alarmMark.setDevId("123");
        alarmMark.setDate(new Date());
        alarmMark.setSequence(1);
        alarmMark.setFileNum(1);
        alarmMark.setReserve(0);

        d_1210.setAlarmMarkUid(alarmMark);

        List<AttachFileItem> fileItems = Lists.newArrayList();
        AttachFileItem fileItem = new AttachFileItem();
        fileItem.setNameLen(fileNameLen);
        fileItem.setFileName(fileName);
        fileItem.setSize(fileSize);
        fileItems.add(fileItem);

        d_1210.setFileItems(fileItems);

        header.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, d_1210.array().length));

        message1210.setHeader(header);
        message1210.setBody(d_1210.array());


        //=============================1211==========================================

        message1211 = new T808Message();

        Header header1 = new Header();
        header1.setMsgId(D_1211.MSG_ID);
        header1.setMobile("13452202456");
        header1.setMsgUid(1);
        header1.setPkgDivideInfo(null);

        D_1211 d_1211 = new D_1211();
        d_1211.setNameLen(fileNameLen);
        d_1211.setFileName(fileName);
        d_1211.setFileType(0x00);
        d_1211.setSize(fileSize);

        header1.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, d_1211.array().length));

        message1211.setHeader(header1);
        message1211.setBody(d_1211.array());


        //=============================码流数据==========================================
        d_byteData1 = new D_ByteData();
        d_byteData1.setFilename(fileName);
        d_byteData1.setOffset(0);
        d_byteData1.setLen(3);
        d_byteData1.setBody(new byte[]{0x01, 0x02, 0x03});

        d_byteData2 = new D_ByteData();
        d_byteData2.setFilename(fileName);
        d_byteData2.setOffset(3);
        d_byteData2.setLen(3);
        d_byteData2.setBody(new byte[]{0x01, 0x02, 0x03});

        //=============================1212==========================================

        message1212 = new T808Message();

        Header header2 = new Header();
        header2.setMsgId(D_1212.MSG_ID);
        header2.setMobile("13452202456");
        header2.setMsgUid(1);
        header2.setPkgDivideInfo(null);

        D_1212 d_1212 = new D_1212();
        d_1212.setNameLen(fileNameLen);
        d_1212.setFilename(fileName);
        d_1212.setFileType(0x00);
        d_1212.setSize(fileSize);

        header2.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, d_1211.array().length));

        message1212.setHeader(header2);
        message1212.setBody(d_1212.array());
    }

    @Test
    public void when1210MsgAndAttachmentTaskNotExistThenRespFail()
    {
        instructionService.handleInstructionData(channel, message1210);

        T808Message result = (T808Message) channel.readOutbound();

        P_8001 p_8001 = new P_8001();
        p_8001.fill(result.getBody());

        assertThat(p_8001.getResult(), is(1));
    }

    @Test
    public void when1210MsgAndAttachmentTaskExistThenRespSuccess()
    {
        doReturn(tasks).when(taskDAO).selectByExample(any(AttachmentTaskExample.class));

        instructionService.handleInstructionData(channel, message1210);

        T808Message result = (T808Message) channel.readOutbound();

        P_8001 p_8001 = new P_8001();
        p_8001.fill(result.getBody());

        assertThat(p_8001.getResult(), is(0));
    }

    @Test
    public void whenDuplicate1210ThenRespFail()
    {
        doReturn(tasks).when(taskDAO).selectByExample(any(AttachmentTaskExample.class));

        instructionService.handleInstructionData(channel, message1210);
        channel.readOutbound();
        instructionService.handleInstructionData(channel, message1210);

        T808Message result = (T808Message) channel.readOutbound();

        P_8001 p_8001 = new P_8001();
        p_8001.fill(result.getBody());

        assertThat(p_8001.getResult(), is(1));
    }

    @Test
    public void when1211MsgThenRespSuccess()
    {
        when1210MsgAndAttachmentTaskExistThenRespSuccess();

        instructionService.handleInstructionData(channel, message1211);

        T808Message resp1211 = (T808Message) channel.readOutbound();
        P_8001 p_8001 = new P_8001();
        p_8001.fill(resp1211.getBody());

        assertThat(p_8001.getResult(), is(0));
    }

    @Test
    public void whenDuplicate1211ThenRespFail()
    {
        when1211MsgThenRespSuccess();
        instructionService.handleInstructionData(channel, message1211);

        T808Message resp1211 = (T808Message) channel.readOutbound();
        P_8001 p_8001 = new P_8001();
        p_8001.fill(resp1211.getBody());

        assertThat(p_8001.getResult(), is(1));
    }

    @Test
    public void when1212MsgAndFileFinishedThenSuccess()
    {
        when1211MsgThenRespSuccess();

        AttachmentTaskBo taskBo = instructionService.getTaskByPlatformAlarmUid(platformAlarmUuid);
        FileTaskBo fileTaskBo = taskBo.getFileTaskTable().get(fileName);
        fileTaskBo.setReceivedSize(new AtomicLong(fileTaskBo.getTotalSize()));

        instructionService.handleInstructionData(channel,message1212);

        T808Message resp9212 = (T808Message) channel.readOutbound();
        P_9212 p_9212 = new P_9212();
        p_9212.fill(resp9212.getBody());

        assertThat(p_9212.getResult(), is(0));
        assertThat(p_9212.getMakeupNum(), is(0));
        assertThat(p_9212.getFileDataPackets().size(), is(0));
    }

    @Test
    public void when1212MsgAndFileNotFinishedThenMakeup()
    {
        when1211MsgThenRespSuccess();

        AttachmentTaskBo taskBo = instructionService.getTaskByPlatformAlarmUid(platformAlarmUuid);
        FileTaskBo fileTaskBo = taskBo.getFileTaskTable().get(fileName);
        fileTaskBo.setReceivedSize(new AtomicLong(3));

        FileDataPacket packet = new FileDataPacket();
        packet.setOffset(1);
        packet.setLen(3);

        fileTaskBo.setCompletedPackets(Lists.list(packet));

        instructionService.handleInstructionData(channel,message1212);

        T808Message resp9212 = (T808Message) channel.readOutbound();
        P_9212 p_9212 = new P_9212();
        p_9212.fill(resp9212.getBody());

        assertThat(p_9212.getResult(), is(1));
        assertThat(p_9212.getMakeupNum(), is(2));

        FileDataPacket makeupPacket = new FileDataPacket();
        makeupPacket.setOffset(0);
        makeupPacket.setLen(1);

        FileDataPacket makeupPacket2 = new FileDataPacket();
        makeupPacket2.setOffset(4);
        makeupPacket2.setLen(2);

        assertThat(p_9212.getFileDataPackets().size(), is(2));
        assertThat(p_9212.getFileDataPackets().get(0), is(makeupPacket));
        assertThat(p_9212.getFileDataPackets().get(1), is(makeupPacket2));
    }
}