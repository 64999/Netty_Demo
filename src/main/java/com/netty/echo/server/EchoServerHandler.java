package com.netty.echo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 一个服务器的Handler：这个组件实现了服务器的业务逻辑，决定了连接创建后和接收到信息后该如何处理
 * BootStraping：这个是配置服务器的启动代码，最少需要设置服务器绑定的端口，用来监听连接请求
 * @sharable:标识这类的实例可以在channel里共享
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter{

    /**
     * 每个信息入站都会调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf in = (ByteBuf) msg;
        //输出日志信息
        System.out.println("service received:"+in.toString(CharsetUtil.UTF_8));
        //将所接收到的消息返回给发送者。注意，这里还没有冲刷数据
        ctx.write(in);
    }

    /**
     * 通知处理器最后的channelread是当批处理的最后一条消息调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //冲刷所有待审消息到远程节点。关闭通道后，操作完成
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 读操作时捕获异常时调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //打印异常
        cause.printStackTrace();
        //关闭通道
        ctx.close();
    }
}
