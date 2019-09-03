package com.netty.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    public EchoServer(int port){
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage:"+EchoServer.class.getSimpleName()
            +"<port>");
            return;
        }
        int port = Integer.parseInt(args[0]);//1
        //呼叫服务器的start方法
        new EchoServer(port).start();
    }

    public void start() throws Exception{
        //创建EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();//3
        try {
            //创建ServerBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    //指定nio使用chnnel
                    .channel(NioServerSocketChannel.class)
                    //设置socket地址使用所选的端口
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //添加echoserverhandler到channel的channelpipelin
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            //绑定的服务器，sync等待服务器关闭
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println(EchoServer.class.getName()+"started and listen on"+future.channel().localAddress());
            //关闭channel和块，直到他被关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            //关机eventgrouploop，释放所有资源
            group.shutdownGracefully().sync();
        }
    }
}
