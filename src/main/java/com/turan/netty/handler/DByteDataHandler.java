package com.turan.netty.handler;

import com.turan.example.protocol.message.dev.D_ByteData;
import com.turan.service.DByteDataService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DByteDataHandler extends SimpleChannelInboundHandler<D_ByteData>
{
    private DByteDataService byteDataService;

    public DByteDataHandler()
    {
    }

    public DByteDataHandler(DByteDataService byteDataService)
    {
        this.byteDataService = byteDataService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, D_ByteData msg) throws Exception
    {
        byteDataService.handleByteData(msg);
    }
}
