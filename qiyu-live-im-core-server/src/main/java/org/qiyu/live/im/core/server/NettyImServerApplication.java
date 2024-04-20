package org.qiyu.live.im.core.server;

import io.netty.channel.Channel;
import org.qiyu.live.im.core.server.common.ImMsgDecoder;
import org.qiyu.live.im.core.server.common.ImMsgEncoder;
import org.qiyu.live.im.core.server.handler.ImServerCoreHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class NettyImServerApplication {
    private int port;
    private AtomicInteger connectCount = new AtomicInteger(0);

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //设置启动端口
    public void startApplication(int port) throws InterruptedException {
        setPort(port);
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new ImMsgDecoder());
                channel.pipeline().addLast(new ImMsgEncoder());
                channel.pipeline().addLast(new ImServerCoreHandler());
            }
//            @Override
//            protected void initChannel(NioSocketChannel ch) throws Exception {
//                System.out.println("连接" + connectCount.getAndIncrement() + "初始化");
//            }

        });
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        //netty的优雅关闭并不是很靠谱的机制
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("安全销毁线程池");
        }));
        System.out.println("netty服务启动成功，绑定端口:" + port);
        channelFuture.channel().closeFuture().sync();
    }

    public static void main(String[] args) throws InterruptedException {
         NettyImServerApplication  nettyServer= new NettyImServerApplication();
         nettyServer.startApplication(9090);
    }
}
