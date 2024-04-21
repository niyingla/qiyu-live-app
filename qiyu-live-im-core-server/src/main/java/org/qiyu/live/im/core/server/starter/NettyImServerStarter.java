package org.qiyu.live.im.core.server.starter;

import io.netty.channel.nio.NioEventLoopGroup;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.ImMsgDecoder;
import org.qiyu.live.im.core.server.common.ImMsgEncoder;
import org.qiyu.live.im.core.server.handler.ImServerCoreHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class NettyImServerStarter implements InitializingBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyImServerStarter.class);
    
    //要监听的端口
    @Value("${qiyu.im.tcp.port}")
    private int port;
    @Resource
    ImServerCoreHandler imServerCoreHandler;

    @Resource
    Environment environment;

    
    //基于Netty去启动一个java进程，绑定监听的端口
    public void startApplication() throws InterruptedException {
        //处理accept事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理read&write事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        //netty初始化相关的handler
        bootstrap.childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                //打印日志，方便观察
                LOGGER.info("初始化连接渠道NettyImServerStarter");
                //设计消息体ImMsg
                //添加编解码器
                channel.pipeline().addLast(new ImMsgEncoder());
                LOGGER.info("初始化连接渠道NettyImServerStarter1");
                channel.pipeline().addLast(new ImMsgDecoder());
                LOGGER.info("初始化连接渠道NettyImServerStarter2");
                //设置这个netty处理handler
                channel.pipeline().addLast(imServerCoreHandler);

            }
        });
        LOGGER.info("初始化连接渠道NettyImServerStarter3");
        //基于JVM的钩子函数去实现优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
        //            String registryIp = InetAddress.getLocalHost().getHostAddress();
        String registryIp = environment.getProperty("DUBBO_IP_TO_REGISTRY");//部署时我们使用这条语句获取ip地址
        String registryPort = environment.getProperty("DUBBO_PORT_TO_REGISTRY");
        System.out.println(registryIp + ":" + registryPort);
        if(StringUtils.isEmpty(registryIp) || StringUtils.isEmpty(registryPort)) {
            throw new IllegalArgumentException("启动参数中的注册端口和注册ip不能为空");
        }
        ChannelHandlerContextCache.setServerIpAddress(registryIp + ":" + registryPort);
        System.out.println(ChannelHandlerContextCache.getServerIpAddress());
        LOGGER.info("Netty服务启动成功，机器启动ip和dubbo服务端口为{}", registryIp + ":" + registryPort);
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        LOGGER.info("Netty服务启动成功，监听端口为{}", port);
        //这里会阻塞主线程，实现服务长期开启的效果
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startApplication();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "qiyu-live-im-server").start();
    }
}