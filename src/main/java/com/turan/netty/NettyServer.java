package com.turan.netty;

import com.turan.netty.codec.NettyDecoder;
import com.turan.netty.codec.NettyEncoder;
import com.turan.netty.handler.DByteDataHandler;
import com.turan.netty.handler.T808InstructionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyServer
{

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup handlerExecutorGroup; // 执行处理编解码，连接管理的handler

    private static final int EVENT_LOOP_BOSS_THREADS = 1;
    private static final int EVENT_LOOP_WORKER_THREADS = 3;
    private static final int HANDLER_WORKER_THREADS = 1;

    private DByteDataHandler dByteDataHandler;
    private T808InstructionHandler instructionHandler;
    private int port;

    public NettyServer(DByteDataHandler dByteDataHandler, T808InstructionHandler instructionHandler, int port)
    {
        this.dByteDataHandler = dByteDataHandler;
        this.instructionHandler = instructionHandler;
        this.port = port;

        serverBootstrap = new ServerBootstrap();

        this.bossGroup = new NioEventLoopGroup(EVENT_LOOP_BOSS_THREADS, new ThreadFactory()
        {
            AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, String.format("NettyNIOBoss_%d_%d", EVENT_LOOP_BOSS_THREADS, threadIndex.getAndIncrement()));
            }
        });
        this.workerGroup = new NioEventLoopGroup(EVENT_LOOP_WORKER_THREADS, new ThreadFactory()
        {
            AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, String.format("NettyNIOWorker_%d_%d", EVENT_LOOP_WORKER_THREADS, threadIndex.getAndIncrement()));
            }
        });
        this.handlerExecutorGroup = new DefaultEventExecutorGroup(HANDLER_WORKER_THREADS, new ThreadFactory()
        {
            AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, String.format("NettyHandlerExecutor_%d_%d", HANDLER_WORKER_THREADS, threadIndex.getAndIncrement()));
            }
        });
    }

    public void start()
    {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        ch.pipeline()
                                .addLast(handlerExecutorGroup,
                                        new NettyEncoder(),
                                        new NettyDecoder(),
                                        new IdleStateHandler(0, 0, 120),
                                        instructionHandler,
                                        dByteDataHandler
                                        // todo turan 连接状态管理器
                                );
                    }
                });

        try
        {
            serverBootstrap.bind().sync();
            log.info("Netty server start success,port : {}", port);
        } catch (InterruptedException e1)
        {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
    }
}
