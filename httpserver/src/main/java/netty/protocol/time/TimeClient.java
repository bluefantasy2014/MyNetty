package netty.protocol.time;

import org.apache.log4j.PropertyConfigurator;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap(); // (1)
			b.group(workerGroup); // (2)
			b.channel(NioSocketChannel.class); // (3)
			b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					//没考虑拆包问题的版本
					//ch.pipeline().addLast(new TimeClientHandler());
					
					//考虑拆包的版本
					ch.pipeline().addLast(new TimeClientHandler1());
					
					//自己写的有问题的版本
					//ch.pipeline().addLast(new TimeClientHandler2());
				}
			});

			// Start the client.
			ChannelFuture f = b.connect(host, port).sync(); // (5)

			// Wait until the connection is closed.
			ChannelFuture closeFuture = f.channel().closeFuture(); 
			closeFuture.addListener(new ChannelFutureListener(){
				 @Override
		            public void operationComplete(ChannelFuture future) {
		                System.out.println("Client Channel is close!");
		            }
			} ); 
			closeFuture.sync();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}
