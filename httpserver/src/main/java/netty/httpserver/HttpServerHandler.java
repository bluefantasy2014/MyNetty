package netty.httpserver;

import java.net.InetSocketAddress;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_MAX_AGE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	enum ResponseType {
		JSON,PlainText,XML
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpServerHandler.class);
	
	protected HttpRequest request;
	protected final StringBuilder reqbody = new StringBuilder();
	protected final StringBuilder rspbody = new StringBuilder();
	protected String clientIp;
	protected String uriPath;
	protected String queryParams;
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;
			URI uri = new URI(request.getUri());
			uriPath = uri.getPath();
			queryParams = uri.getQuery();
            clientIp = getClientIp(ctx);
            
//			new Exception().printStackTrace(System.out);
            LOG.info("request uri: " + uriPath);
            LOG.info("clientIp: " + clientIp);
			LOG.info("query params: " + queryParams);
			
			if (!HttpRouter.checkUri(uriPath)) {
                sendError(ctx, NOT_FOUND);
                return;
            }
			
			if (!request.getMethod().equals(HttpMethod.GET) 
					&& !request.getMethod().equals(HttpMethod.POST)
					&& !request.getMethod().equals(HttpMethod.OPTIONS)) {
				sendError(ctx, METHOD_NOT_ALLOWED);
                LOG.warn("HttpMethod " + request.getMethod() + " not allowed");
                return;
			}
			
			if (request.getMethod().equals(HttpMethod.OPTIONS)) {
                sendOptionsRsp(ctx);
            }
			
			if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
			
			reqbody.setLength(0);
            rspbody.setLength(0);
		}
		
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;
			ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                reqbody.append(content.toString(CharsetUtil.UTF_8));
            }
            
//        	new Exception().printStackTrace(System.out);
            
            if (msg instanceof LastHttpContent) {
            	LastHttpContent trailer = (LastHttpContent) msg;
            	try {
	            	   processPlainText();
                } catch (Throwable e) {
                	rspbody.append(String.format("{\"ret\":%d,\"msg\":\"uncatched exception: %s\"}", ErrorCode.UNKOWN_EXCEPTION, e.getMessage()));
                    LOG.error(e.getMessage(), e);
                }
            	if (!writeResponse(trailer, ctx)) {
                    // If keep-alive is off, close the connection once the
                    // content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
		}
	}
	
	//返回值为普通的非json格式
	public void processPlainText() {
		StringBuffer response = new StringBuffer();
		if ((request.getMethod().equals(HttpMethod.GET) && StringUtils.isEmpty(queryParams)) 
				|| (request.getMethod().equals(HttpMethod.POST) && reqbody.length()==0)) {
			response.append("ret:" + ErrorCode.PARAM_ERROR);
			response.append("msg:" + "parameter is empty");
			rspbody.append(response);
			return;
		}
		
		RequestContext req = new RequestContext();
		req.setHttpRequest(request);
        req.setUri(uriPath);
        if (request.getMethod().equals(HttpMethod.GET)) {
        	req.setParams(queryParams);
		} else {
			req.setParams(reqbody.toString());
			LOG.info("reqbody: "+reqbody.toString());
		}
        
        long start = System.currentTimeMillis();
        HttpRouter.routePlainText(req, response);
        long spend = System.currentTimeMillis() - start;
        LOG.info("uri:" + uriPath + ", spend:" + spend + "ms");
        
		rspbody.append(response.toString());
	}
	
	//默认是json格式的返回值  NOT USED
	public void process() {
		JSONObject response = new JSONObject();
		if ((request.getMethod().equals(HttpMethod.GET) && StringUtils.isEmpty(queryParams)) 
				|| (request.getMethod().equals(HttpMethod.POST) && reqbody.length()==0)) {
			response.put("ret", ErrorCode.PARAM_ERROR);
			response.put("msg", "parameter is empty");
			rspbody.append(response.toJSONString());
			return;
		}
		
		RequestContext req = new RequestContext();
		req.setHttpRequest(request);
        req.setUri(uriPath);
        if (request.getMethod().equals(HttpMethod.GET)) {
        	req.setParams(queryParams);
		} else {
			req.setParams(reqbody.toString());
			LOG.info("reqbody: "+reqbody.toString());
		}
        
        long start = System.currentTimeMillis();
        HttpRouter.route(req, response);
        long spend = System.currentTimeMillis() - start;
        LOG.info("uri:" + uriPath + ", spend:" + spend + "ms");
        
		rspbody.append(response.toJSONString());
	}
	
	public boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		LOG.info("rsp= " + rspbody.toString());
		
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, currentObj
				.getDecoderResult().isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(
				rspbody.toString(), CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
		
		if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
	}
	
	private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }
	
	private void sendOptionsRsp(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, request.headers().get(ORIGIN));
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, HttpMethod.POST);
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "accept, content-type");
        response.headers().set(ACCESS_CONTROL_MAX_AGE, "1728000");
        response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8");
        
        ctx.write(response);
    }
	
	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		LOG.warn("sendError: " + status.toString());
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
				Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
	private String getClientIp(ChannelHandlerContext ctx) {
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = insocket.getAddress().getHostAddress();
		String XFF = request.headers().get("X-Forwarded-For");
		if (!StringUtils.isEmpty(XFF)) {
			String[] arr = XFF.split(",");
			if (arr.length > 0) {
				clientIp = arr[0].trim();
			}
		}
		return clientIp;
	}

}
