package netty.discard.protocol;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
	private static final Logger LOG = Logger.getLogger(DiscardServerHandler.class);

	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException { // (2)
        // Discard the received data silently.
    	ByteBuf bb = (ByteBuf)msg; 
    	int size = bb.readableBytes();
    	byte[] data = new byte[size]; 
    	bb.readBytes(data, 0, size); 
    	LOG.info(new String(data,"utf-8"));
        ((ByteBuf) msg).release(); // (3)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}