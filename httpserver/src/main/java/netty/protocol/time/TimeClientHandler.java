package netty.protocol.time;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/*
 * 没有考虑拆包的问题。但是由于一个包里只包含一个int数据，所以很难发现这个问题。 
 * */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        try {
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            //JerryQ: 如果服务端close了这个channel，但是Client端没有close的话，这个channel还是被close了。怎么回事？及时注释掉这一行，client还是
            //会发出FIN命令，为何？ 
            ctx.close();  
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}