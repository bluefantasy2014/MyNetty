package netty.httpserver.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import netty.common.utils.MyFileUtil;
import netty.common.utils.StringUtil;

public class ServeJsonFileHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger LOG = Logger.getLogger(ServeJsonFileHandler.class);
	private String jsonFileName = "bankcardreport.json"; 
	private Map<String,String> requestParas ; 
	private boolean zeroCopyFileMode = false; 
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest)msg; 
			LOG.info("url:" + request.getUri());
			URI uri = new URI(request.getUri()); 
			LOG.info("query uri: " + uri.getQuery()); 
			requestParas = StringUtil.parseQueryParamAsMap(uri.getQuery());
			if (requestParas.get("filename") != null){
				jsonFileName = requestParas.get("filename"); 
			}
			
			if ("true".equals(requestParas.get("zerocopy"))){
				zeroCopyFileMode = true;
			}
		}
		
		if (msg instanceof HttpContent) {
			//测试死锁，但是死锁没发生。why？？？？ java doc 上说会有死锁。。
			// http://netty.io/4.0/api/io/netty/channel/ChannelFuture.html
			
//			LOG.info("Before testing");
//			ChannelFuture future = ctx.channel().close();
//			future.awaitUninterruptibly(); 
//			LOG.info("After testing");
			
			
			LastHttpContent trailingHeaders = (LastHttpContent) msg;
			long startTime = System.currentTimeMillis(); 
			LOG.info("Start time: " + startTime + ", zeroCopyFileMode:" + zeroCopyFileMode);
			
			//此处有问题。。以后慢慢看看是什么原因。。？？？ 
			if (zeroCopyFileMode){
				File file = new File(jsonFileName);
				LOG.info("Isfile: " + file.isFile());
				RandomAccessFile raf = new RandomAccessFile(file, "r");
		        long fileLength = raf.length();

				HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK );
			  
				response.headers().setContentLength(response,fileLength);
//			        setContentTypeHeader(response, file);
//			        setDateAndCacheHeaders(response, file);
				response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
				ctx.channel().write(response); 
				ctx.channel().write(new DefaultFileRegion(raf.getChannel(), 0, fileLength)); 
			}else {
				String responseMsg = readJsonFileAsResponse(jsonFileName); 
				StringBuilder sb =  new StringBuilder(); 
				sb.append("{\"resCode\":\"000000\",\"rawResponse\":").append(responseMsg).append("}"); 
				sendResponse(sb.toString(),ctx,trailingHeaders); 
			}
			
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
		        @Override
		        public void operationComplete(ChannelFuture future) {
		            future.channel().close();
		            LOG.info("Spent time: " + (System.currentTimeMillis() - startTime ));
		        }
		    });
		}
	} 
	
	private void sendResponse(String responseMsg, ChannelHandlerContext ctx, HttpObject httpObj) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				httpObj.getDecoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
				Unpooled.copiedBuffer(responseMsg, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		ctx.write(response);
	}
	
	private String readJsonFileAsResponse(String jsonFileName){
		List<String> lines = MyFileUtil.readFileAsLines(jsonFileName); 
		StringBuilder sb = new StringBuilder(); 
		for (String s:lines){
			sb.append(s); 
		}
		return sb.toString(); 
	}
}
