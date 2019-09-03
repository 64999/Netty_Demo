package com.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HeartBeatClient {

    private Random random = new Random();

    public Channel channel;

    public Bootstrap bootstrap;

    protected String host = "127.0.0.1";
    protected int port = 9876;

    public static void main(String[] args) throws Exception {
        HeartBeatClient client = new HeartBeatClient();
        client.run();
        client.sendData();
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleClientInitializer(HeartBeatClient.this));
            doConncet();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //发送数据
    public void sendData() throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String readLine = bufferedReader.readLine();
            switch (readLine){
                case "close" :
                    channel.close();
                    break;
                default:
                    channel.writeAndFlush(bufferedReader.readLine());
                    break;

            }
        }
    }

    public void doConncet() {
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture channelFuture = bootstrap.connect(host, port);

        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                    channel = channelFuture.channel();
                    System.out.println("connect server is successfully");
                }else{
                    System.out.println("Faild to connect server,try connect after 10s");
                    channelFuture.channel().eventLoop().schedule(new Runnable() {
                        public void run() {
                            doConncet();
                        }
                    },10, TimeUnit.SECONDS);
                }
            }
        });
    }

    private class SimpleClientInitializer extends ChannelInitializer<SocketChannel> {

        private HeartBeatClient client;

        public SimpleClientInitializer(HeartBeatClient client) {
            this.client = client;
        }

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new IdleStateHandler(0, 5, 0));
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("handler", new HeartClientHandler(client));
        }
    }
}
