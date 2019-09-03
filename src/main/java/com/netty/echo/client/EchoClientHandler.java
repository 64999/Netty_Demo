package com.netty.echo.client;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * 客户端工作内容
 * 连接服务器
 * 发送消息
 * 发送的每个消息，等待和接收从服务器返回的同样的信息
 * 关闭连接
 *
 * SimpleChannelInboundHandler vs. ChannelInboundHandler
 * 何时用这两个要看具体业务的需要。在客户端，当 channelRead0() 完成，
 * 我们已经拿到的入站的信息。当方法返回时，SimpleChannelInboundHandler 会小心的
 * 释放对 ByteBuf（保存信息） 的引用。而在 EchoServerHandler,我们需要将入站的
 * 信息返回给发送者，由于 write() 是异步的，在 channelRead() 返回时，可能还没有完成。
 * 所以，我们使用 ChannelInboundHandlerAdapter,无需释放信息。最后在 channelReadComplete()
 * 我们调用 ctxWriteAndFlush() 来释放信息。
 */
@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf>{

    /**
     * 服务器连接建立后被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当被通知该chnnel是活动的时候就发送消息
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks", CharsetUtil.UTF_8));
    }

    /**
     * 数据后从服务器接收到调用
     * 由服务器发送的消息可以以块的形式被接收
     * 唯一要保证的是，该字节将按照它们发送的顺序分别被接收
     * @param channelHandlerContext
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        //记录接收到的信息
        System.out.println("client recived:"+byteBuf.toString((CharsetUtil.UTF_8)));
    }

    /**
     * 捕获一个异常时调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();;
        ctx.close();
    }
}
