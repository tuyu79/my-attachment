package com.turan.config;

import com.turan.netty.NettyServer;
import com.turan.netty.handler.DByteDataHandler;
import com.turan.netty.handler.T808InstructionHandler;
import com.turan.service.DByteDataService;
import com.turan.service.T808InstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class NettyServerConfig
{
    @Autowired
    private DByteDataService dByteDataService;
    @Autowired
    private T808InstructionService instructionService;
    @Autowired
    private AttachmentConfig attachmentConfig;

    @PostConstruct
    public void start()
    {
        DByteDataHandler dByteDataHandler = new DByteDataHandler(dByteDataService);
        T808InstructionHandler instructionHandler = new T808InstructionHandler(instructionService);

        NettyServer nettyServer = new NettyServer(dByteDataHandler,instructionHandler,attachmentConfig.getPort()) ;
        nettyServer.start();
    }
}
