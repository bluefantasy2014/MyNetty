package netty.common.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import netty.protocol.time.TimeClientHandler2;

public class MyByteBufUtil {
	private static final Logger LOG = Logger.getLogger(MyByteBufUtil.class);
	
	public static void printHex(byte[] bytes){
		for (byte b:bytes){
			System.out.format("%x", b); 
		}
	}
	
	public static void printHex1(byte[] bytes){
		System.out.println( Hex.encodeHexString(bytes) );
	}
	
	public static void main(String[] args) {
		printHex("史纪军".getBytes()); 
		System.out.println(); 
		printHex1("史纪军".getBytes()); 
	}
	
	public static void printDebugInfo(ByteBuf bf){
		LOG.debug("-----------------------------------");
		LOG.debug("The identityHashCode:" + System.identityHashCode(bf));
		LOG.debug("The className of msg:" + bf.getClass().getCanonicalName());
		LOG.debug("The hexDump of msg:" + ByteBufUtil.hexDump(bf));
		LOG.debug("The referenceCount of msg:" + bf.refCnt());
		LOG.debug("-----------------------------------");
	}
}
