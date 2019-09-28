package com.turan.netty.handler;

import com.turan.example.protocol.item.AttachFileItem;
import com.turan.example.protocol.message.activesafe.AlarmMarkUid;
import com.turan.example.protocol.message.dev.D_1210;
import com.turan.example.protocol.message.dev.D_1211;
import com.turan.example.protocol.message.dev.D_1212;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import com.turan.netty.codec.NettyDecoder;
import com.turan.service.T808InstructionService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class T808InstructionHandlerTest
{

    @InjectMocks
    private T808InstructionHandler instructionHandler;
    @Mock
    private T808InstructionService instructionService;

    private EmbeddedChannel channel;

    private T808Message message1210;
    private T808Message message1211;
    private T808Message message1212;

    @Before
    public void init() throws Exception
    {
        channel = new EmbeddedChannel(
                new NettyDecoder(),
                instructionHandler
        );

        String platformAlarmUuid = "99999";
        String fileName = "00_0_6401_1_" + platformAlarmUuid + ".jpg";
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
    public void when1210MsgThenInvokeInstructionHandler()
    {
        channel.writeInbound(Unpooled.wrappedBuffer(message1210.array()));

        verify(instructionService, times(1)).handleInstructionData(any(Channel.class), any(T808Message.class));
    }

    @Test
    public void when1211MsgThenInvokeInstructionHandler()
    {
        channel.writeInbound(Unpooled.wrappedBuffer(message1211.array()));

        verify(instructionService, times(1)).handleInstructionData(any(Channel.class), any(T808Message.class));
    }

    @Test
    public void when1212MsgThenInvokeInstructionHandler()
    {
        channel.writeInbound(Unpooled.wrappedBuffer(message1212.array()));

        verify(instructionService, times(1)).handleInstructionData(any(Channel.class), any(T808Message.class));
    }
}