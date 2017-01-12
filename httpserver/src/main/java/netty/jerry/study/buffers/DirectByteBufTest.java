package netty.jerry.study.buffers;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DirectByteBufTest {

	private static final Logger LOG = LoggerFactory.getLogger(HeapByteBufTest.class);
	
	public static void main(String [] args){
		PropertyConfigurator.configure("log4j.properties");
		byte[] data = {1,2,3,4}; 
		
		ByteBuf directBuf = Unpooled.directBuffer(100);  
		LOG.debug("Whether the directBuf is array-backed? :" + directBuf.hasArray());
		ByteBuf copiedBuf = Unpooled.copiedBuffer(data); 
		LOG.debug("Whether the copiedBuf is array-backed? :" + copiedBuf.hasArray());
		ByteBuf wrappedBuf = Unpooled.wrappedBuffer(data);  
		LOG.debug("Whether the wrappedBuf is array-backed? :" + wrappedBuf.hasArray());
	}

}
