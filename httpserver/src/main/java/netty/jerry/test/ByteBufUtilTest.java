package netty.jerry.test;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class ByteBufUtilTest {
private static final Logger LOG = LoggerFactory.getLogger(HeapByteBufTest.class);
	
	public static void main(String [] args){
		PropertyConfigurator.configure("log4j.properties");
		byte[] data = {(byte) 255,2,3,4}; 
		
		ByteBuf heapBuf = Unpooled.wrappedBuffer(data);  
		LOG.info(ByteBufUtil.hexDump(heapBuf));  
		
		byte[] array = heapBuf.array(); 
		for (int i=0; i<array.length; ++i){
			System.out.println(array[i]);
		}
		
		String name = "史纪军";
		heapBuf = Unpooled.wrappedBuffer(name.getBytes());  
		LOG.info(ByteBufUtil.hexDump(heapBuf)); 
	}
}
