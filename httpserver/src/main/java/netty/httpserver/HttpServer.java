package netty.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {

	private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);
	
	private int workerThreadNum = 0;
	private final SslContext sslCtx;
	
	public HttpServer() {
		this(0, null);
	}
	
	public HttpServer(int workThreadNum) {
		this(workThreadNum, null);
	}
	
	public HttpServer(int workThreadNum, SslContext sslCtx) {
		this.workerThreadNum = workThreadNum;
		this.sslCtx = sslCtx;
	}
	
	public void start(int port) throws Exception {
		LOG.info("start http server, port:"+port);
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadNum);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.SO_REUSEADDR, true);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
            	@Override
        	    public void initChannel(SocketChannel ch) throws Exception {
        	        ChannelPipeline pipeline = ch.pipeline();
        	        if (sslCtx != null) {
        	            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        	        }
        	        pipeline.addLast("decoder", new HttpRequestDecoder());
        	        pipeline.addLast("encoder", new HttpResponseEncoder());
        	        pipeline.addLast("deflater", new HttpContentCompressor());
        	        pipeline.addLast("handler", new HttpServerHandler());
        	    }
             });

            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
	
	public void register(String path, HttpHandler handler) {
		HttpRouter.register(path, handler);
	}
	
}
