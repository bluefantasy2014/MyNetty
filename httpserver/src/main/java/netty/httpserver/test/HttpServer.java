package netty.httpserver.test;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpServer {
	private static final Logger LOG = Logger.getLogger(HttpServer.class);
	
	private static ChannelHandler createInitializer(){
		return new ChannelInitializer<SocketChannel>() {
        	@Override
    	    public void initChannel(SocketChannel ch) throws Exception {
    	        ChannelPipeline pipeline = ch.pipeline();
    	        pipeline.addLast("decoder", new HttpRequestDecoder());
    	        pipeline.addLast("encoder", new HttpResponseEncoder());
    	        pipeline.addLast("handler", new ServeJsonFileHandler());
    	    }
		};
	}
	
	public static void main(String[] args) {
		int port = 10000;
		
		PropertyConfigurator.configure("log4j.properties");
		
		EventLoopGroup acceptor = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptor, workers)
             .channel(NioServerSocketChannel.class)
             .childHandler(createInitializer());

			try {
				Channel channel = serverBootstrap.bind(port).sync().channel();
				channel.closeFuture().sync();
			} catch (Throwable t){
            	LOG.error(t,t.fillInStackTrace());
            }
        } finally {
            acceptor.shutdownGracefully();
            workers.shutdownGracefully();
        }
	}
}