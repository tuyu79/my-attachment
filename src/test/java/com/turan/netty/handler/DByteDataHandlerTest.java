package com.turan.netty.handler;

import com.turan.example.protocol.message.dev.D_ByteData;
import com.turan.netty.codec.NettyDecoder;
import com.turan.service.DByteDataService;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DByteDataHandlerTest
{
    @Spy
    private T808InstructionHandler instructionHandler;
    @Spy
    @InjectMocks
    private DByteDataHandler byteDataHandler;
    @Mock
    private DByteDataService byteDataService;

    @Test
    public void whenByteStreamMessageThenInvokeDByteDataHandler()
    {
        D_ByteData d_byteData = new D_ByteData();
        d_byteData.setFilename("测试文件.jpg");
        d_byteData.setOffset(0);
        d_byteData.setLen(3);
        d_byteData.setBody(new byte[]{0x01,0x02,0x03});

        EmbeddedChannel channel = new EmbeddedChannel(
                new NettyDecoder(),
                instructionHandler,
                byteDataHandler
        );

        channel.writeInbound(Unpooled.wrappedBuffer(d_byteData.array()));

        verify(byteDataService,times(1)).handleByteData(any(D_ByteData.class));
    }
}