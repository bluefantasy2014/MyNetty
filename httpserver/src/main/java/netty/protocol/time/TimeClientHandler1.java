package netty.protocol.time;

import java.util.Date;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netty.common.utils.MyByteBufUtil;
/*
 * Fix 拆包问题。 先分配一个4个字节的Buffer，每次把stream中过来的数据放入这个buffer，只有当满足4个字节的时候再进行处理。 
 * */
public class TimeClientHandler1 extends ChannelInboundHandlerAdapter {
	private static final Logger LOG = Logger.getLogger(TimeClientHandler1.class);
	private ByteBuf buf;

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		buf = ctx.alloc().buffer(4); // (1)
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		buf.release();  
		buf = null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf m = (ByteBuf) msg;
		
		MyByteBufUtil.printDebugInfo(m);
		
		buf.writeBytes(m);  
		LOG.info("client recv msg:" + ByteBufUtil.prettyHexDump(m));
		m.release();

		if (buf.readableBytes() >= 4) {  
			long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
			System.out.println(new Date(currentTimeMillis));
			ctx.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}