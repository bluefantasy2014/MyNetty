package netty.httpserver.test;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import netty.common.utils.MyFileUtil;

public class ServeJsonFileHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger LOG = Logger.getLogger(ServeJsonFileHandler.class);
	private static String jsonFileName = "123.json"; 

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest)msg; 
			LOG.info("url:" + request.getUri());
		}
		
		if (msg instanceof HttpContent) {
			LastHttpContent trailingHeaders = (LastHttpContent) msg;
			String responseMsg = readJsonFileAsResponse(jsonFileName); 
			sendResponse(responseMsg,ctx,trailingHeaders); 
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
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
