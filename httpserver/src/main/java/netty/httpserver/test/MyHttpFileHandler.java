package netty.httpserver.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import netty.common.utils.MyFileUtil;
import netty.common.utils.StringUtil;
import netty.common.utils.WebSocketHelper;

public class MyHttpFileHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger LOG = Logger.getLogger(MyHttpFileHandler.class);
	private static String fileName = "index.html"; 
	private Map<String,String> requestParas ; 
	private boolean zeroCopyFileMode = false; 
	
	private static MimetypesFileTypeMap mimeTypesMap;
	   
	static {
		if (mimeTypesMap == null) {
			InputStream is = MyHttpFileHandler.class.getResourceAsStream("/META-INF/server.mime.types");
			if (is != null) {
				mimeTypesMap = new MimetypesFileTypeMap(is);
			} else {
				LOG.error("Cannot load mime types!");
			}
		}
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
				fileName = requestParas.get("filename"); 
			}
			if ("true".equals(requestParas.get("zerocopy"))){
				zeroCopyFileMode = true;
			}
		}
		
		if (msg instanceof HttpContent) {
			// 测试死锁，但是死锁没发生。why？？？？ java doc 上说会有死锁。。
			// http://netty.io/4.0/api/io/netty/channel/ChannelFuture.html

			// LOG.info("Before testing");
			// ChannelFuture future = ctx.channel().close();
			// future.awaitUninterruptibly();
			// LOG.info("After testing");

			LastHttpContent trailingMsg = (LastHttpContent) msg;
			long startTime = System.currentTimeMillis();
			LOG.info("Start time: " + startTime + ", zeroCopyFileMode:" + zeroCopyFileMode);

			ChannelFuture lastContentFuture;
			if (zeroCopyFileMode) {
				sendZeroCopyFileResponse(ctx,fileName); 
			} else { // 读取文本文件并拼装成字符串，如果是二进制文件,就会显示乱码。 
				sendStringMsgResponse(ctx,fileName,trailingMsg.decoderResult().isSuccess()); 
			}
			
			// Write the end marker.
			lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			
			lastContentFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) {
					future.channel().close();
					LOG.info("Spent time: " + (System.currentTimeMillis() - startTime));
				}
			});
		}
	} 
	
	protected void setContentTypeHeader(HttpResponse response, File file) {
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

	// 构建一个FullHttpResponse，包含传入的String参数，并写入ctx。
	private void sendZeroCopyFileResponse(ChannelHandlerContext ctx,String fileName) throws IOException {
		File file = new File(fileName);
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}

		long fileLength = raf.length();
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		HttpUtil.setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		WebSocketHelper.setDateAndCacheHeaders(response, file);
		// Write the initial line and the header.
		ctx.write(response);
		// Write the content.
		ChannelFuture sendFileFuture;
		sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength),ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) { // total unknown
					LOG.error(future.channel() + " Transfer progress: " + progress);
				} else {
					LOG.error(future.channel() + " Transfer progress: " + progress + " / " + total);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				LOG.error(future.channel() + " Transfer complete.");
			}
		});
	}

	// 构建一个FullHttpResponse，包含传入的String参数，并写入ctx。
	private void sendStringMsgResponse(ChannelHandlerContext ctx, String fileName, boolean decoderSuccess) {
		String responseMsg = readJsonFileAsResponse(fileName);
		StringBuilder sb = new StringBuilder();
		sb.append("{\"resCode\":\"000000\",\"rawResponse\":").append(responseMsg).append("}");
		
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,decoderSuccess ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
				Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8));
		
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		ctx.write(response);
	}
	
	private String readJsonFileAsResponse(String fileName){
		List<String> lines = MyFileUtil.readFileAsLines(fileName); 
		StringBuilder sb = new StringBuilder(); 
		for (String s:lines){
			sb.append(s); 
		}
		return sb.toString(); 
	}
	
	 protected void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
	      FullHttpResponse response = new DefaultFullHttpResponse(
	            HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
	      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

	      // Close the connection as soon as the error message is sent.
	      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	   }
}
