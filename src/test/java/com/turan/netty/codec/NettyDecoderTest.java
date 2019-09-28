package com.turan.netty.codec;

import com.turan.example.protocol.message.dev.D_0100;
import com.turan.example.protocol.message.dev.D_ByteData;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NettyDecoderTest
{
    @Test
    public void whenT808ByteMessageThenReadT808Object()
    {
        T808Message message = new T808Message();

        Header header = new Header();
        header.setMsgId(D_0100.MSG_ID);
        header.setMobile("13452202456");
        header.setMsgUid(1);
        header.setPkgDivideInfo(null);

        D_0100 d_0100 = new D_0100();
        d_0100.setProvinceId(1);
        d_0100.setCityId(1);
        d_0100.setManufacturer("MD5");
        d_0100.setTerminalType("D5X");
        d_0100.setTerminalId("123456");
        d_0100.setPlateColor(2);
        d_0100.setVehicleMark("测A0002");

        header.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, d_0100.array().length));

        message.setHeader(header);
        message.setBody(d_0100.array());

        EmbeddedChannel channel = new EmbeddedChannel(new NettyDecoder());

        channel.writeInbound(Unpooled.wrappedBuffer(message.array()));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertEquals(message,readInboundMsg);
    }

    @Test
    public void whenByteStreamMessageThenReadDByteObject()
    {
        D_ByteData d_byteData = new D_ByteData();
        d_byteData.setFilename("测试文件.jpg");
        d_byteData.setOffset(0);
        d_byteData.setLen(3);
        d_byteData.setBody(new byte[]{0x01,0x02,0x03});

        EmbeddedChannel channel = new EmbeddedChannel(new NettyDecoder());

        channel.writeInbound(Unpooled.wrappedBuffer(d_byteData.array()));

        D_ByteData readInboundMsg = (D_ByteData) channel.readInbound();
        assertEquals(d_byteData,readInboundMsg);
    }
}