package netty.jerry.study.buffers;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/*Get familiar with Heap ByteBuf in Netty. 
 * 
 * Heap ByteBuf is the most used type that store in data in the heap space of the JVM. 
 * */
public class HeapByteBufTest {
	private static final Logger LOG = LoggerFactory.getLogger(HeapByteBufTest.class);
	
	public static void main(String [] args){
		PropertyConfigurator.configure("log4j.properties");
		byte[] data = {1,2,3,4}; 
		
		//heapBuf.hasArray() : 是否是array-backed
		ByteBuf heapBuf = Unpooled.wrappedBuffer(data);  
		LOG.debug("Whether the ByteBuf is array-backed? :" + heapBuf.hasArray());
		ByteBuf heapBuf1 = Unpooled.buffer(2);
		heapBuf1.writeBytes("ffadfdasfdafdasfdafdasfdafdafdasfdafdasf".getBytes()); 
		heapBuf1.clear();
		heapBuf1.writeBytes("ffadfdasfdafdasfdafda".getBytes()); 
		LOG.info("heapBuf1.hasArray: " + heapBuf1.hasArray()); 
		
		if (heapBuf.hasArray()){
			byte[] array = heapBuf.array(); 
			int offset = heapBuf.arrayOffset();
			int length = heapBuf.readableBytes(); 
			
			LOG.debug(""+ offset + "," + length);
		}
		
	}
}
