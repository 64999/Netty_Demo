package com.netty.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Random;

public class HeartBeatServerHandler extends SimpleChannelInboundHandler<String>{

    //失败计数器：未收到client请求
    private int unRecvPingTimes = 0;

    //定义服务端没有收到心跳的最大次数
    private static final int MAX_UN_RECV_PING_TIMES = 3;

    private Random random = new Random(System.currentTimeMillis());

    protected void channelRead0(ChannelHandlerContext context, String msg) throws Exception {

        if(msg != null && msg.equals("Heartbeat")){
            System.out.println("客户端"+context.channel().remoteAddress()+"心跳信息");
        }else{
            System.out.println("客户端----请求信息---："+msg);
            String resp = "---------:"+random.nextInt(1000);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("一个客户端已连接... ...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("一个客户端断开连接... ...");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("---服务端----{reader_idel}读超时");
                //失败连接次数大于等于3次的时候，关闭连接，等待client连接
                if(unRecvPingTimes >= MAX_UN_RECV_PING_TIMES){
                    System.out.println("---服务端---{读超时，关闭channer}");
                    //连续超过N次未收到client的ping消息，那么关闭通道，等待client重连
                    ctx.close();
                }else{
                    unRecvPingTimes ++;
                }
            }else{
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
