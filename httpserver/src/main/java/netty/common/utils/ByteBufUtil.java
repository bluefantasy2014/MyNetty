package netty.common.utils;

import org.apache.commons.codec.binary.Hex;

public class ByteBufUtil {
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
}
