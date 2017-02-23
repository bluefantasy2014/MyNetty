package netty.protocol.time;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/*
 * 注意： 此类使用POJO对象来处理业务逻辑 。
 * */
public class TimeServerHandlerPojo extends ChannelInboundHandlerAdapter  {

	private static final Logger LOG = Logger.getLogger(TimeServerHandlerPojo.class);

    @Override
   	public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	 ChannelFuture f = ctx.writeAndFlush(new UnixTime());
    	 f.addListener(ChannelFutureListener.CLOSE);
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
