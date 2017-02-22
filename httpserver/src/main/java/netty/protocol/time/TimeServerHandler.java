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
 * 注意： 此类中有两个channelActive方法的实现，2中方法实现方式不一样。 主要为了做实验。 运行时可以选择任何一种方式。 
 * */
public class TimeServerHandler extends ChannelInboundHandlerAdapter  {

	private static final Logger LOG = Logger.getLogger(TimeServerHandler.class);

	/*将一个int型的时间分成2部分发送给Client。 
	 * */
    @Override
   	public void channelActive(ChannelHandlerContext ctx) throws Exception {
       	final ByteBuf time = ctx.alloc().buffer(4);  
       	
       	int currentTime = (int)(System.currentTimeMillis() / 1000L + 2208988800L);
       	time.writeInt(currentTime); 
       	LOG.info("print the time hex: " + ByteBufUtil.prettyHexDump(time));
       	//将bytes中的4个字节分别写入到time，time1中
       	ByteBuf part1 = time.copy(0,2);  //前2个字节
       	ByteBuf part2 = time.copy(2,2); //后2个字节
       	
       	//本方法需要负责release
       	time.release(); 
       	
       	LOG.info("print the part1 hex: " + ByteBufUtil.prettyHexDump(part1));
       	LOG.info("print the part2 hex: " + ByteBufUtil.prettyHexDump(part2));
       	
       	LOG.debug("print refcount 1:" + time.refCnt() + "," + part1.refCnt() + "," + part2.refCnt());
        final ChannelFuture f = ctx.writeAndFlush(part1);  
        LOG.info("sleep 2 second to wait for part1 to be wrote");
        
        Thread.sleep(2000);
     	LOG.info("print refcount 2:" + time.refCnt() + "," + part1.refCnt() + "," + part2.refCnt());
     	final ChannelFuture f1 = ctx.writeAndFlush(part2);
     	LOG.info("print refcount 3:" + time.refCnt() + "," + part1.refCnt() + "," + part2.refCnt());
       	
    	f1.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f1 == future;
                ctx.close();
            }
        }); 
   	}
    
    /*
     * 这种方式是最简单的方式。 
     * */
//    @Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//    	final ByteBuf time = ctx.alloc().buffer(4);  
//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//
//        final ChannelFuture f = ctx.writeAndFlush(time);  
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                assert f == future;
//                ctx.close();
//            }
//        });  
//	}


	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
