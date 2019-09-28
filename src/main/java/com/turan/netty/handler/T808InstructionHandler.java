package com.turan.netty.handler;

import com.turan.example.protocol.structure.T808Message;
import com.turan.service.T808InstructionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class T808InstructionHandler extends SimpleChannelInboundHandler<T808Message>
{

    private T808InstructionService instructionService;

    public T808InstructionHandler()
    {
    }

    public T808InstructionHandler(T808InstructionService instructionService)
    {
        this.instructionService = instructionService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, T808Message msg) throws Exception
    {
        instructionService.handleInstructionData(ctx.channel(),msg);
    }
}
