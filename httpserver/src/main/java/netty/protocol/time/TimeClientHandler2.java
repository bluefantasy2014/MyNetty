package netty.protocol.time;

import java.util.Date;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netty.common.utils.MyByteBufUtil;
/*
 * 这个版本是有错误的版本，原因是channelRead方法中每次被调用的时候数据都是不一样的。正确的做法是需要将每次的数据都累积起来，等到长度>4的时候再进行处理。
 * 而不是像本方法中的如果长度<4则直接扔掉数据。这是不对的。 
 * 
 * */
public class TimeClientHandler2 extends ChannelInboundHandlerAdapter {
	private static final Logger LOG = Logger.getLogger(TimeClientHandler2.class);
    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf m = (ByteBuf) msg;  
		
		MyByteBufUtil.printDebugInfo(m);
		
		if (m.readableBytes() >= 4) {
			try {
				long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
				LOG.debug(new Date(currentTimeMillis));
				ctx.close();
			} finally {
				m.release();
			}
		} else {
			LOG.debug("Waiting for more bytes");
		}
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}