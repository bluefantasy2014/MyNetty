package netty.jerry.study.clientServerShareEventLoop;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class ClientServerShareEventLoop {
	public static void main(String[] args) {
		EventLoopGroup group = new NioEventLoopGroup(); 
		Bootstrap cb = new Bootstrap(); 
		cb.group(group);
		
		//share same EventLoopGroup to minimize thread and latency??
		ServerBootstrap sb = new ServerBootstrap(); 
		sb.group(group);
	}
}
