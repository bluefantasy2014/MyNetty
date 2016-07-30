package netty.httpserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class HttpRouter {

	private static final Logger LOG = LoggerFactory.getLogger(HttpRouter.class);
	
	private static final Map<String, HttpHandler> handlers = new HashMap<String, HttpHandler>();
	
	public static void register(String path, HttpHandler handler) {
        handlers.put(path, handler);
    }
    
    public static void route(RequestContext request, JSONObject response) {
    	String uri = request.getUri();
    	if (handlers.containsKey(uri)) {
    		HttpHandler handler = handlers.get(uri);
            handler.handle(request.getParams(), response);
    	} else {
    		LOG.warn("unkown uri: " + uri);
    		response.put("ret", ErrorCode.URI_UNKNOWN_ERROR);
    		response.put("msg", "unkown uri");
    	}
    }
    
    public static void routePlainText(RequestContext request, StringBuffer response) {
    	String uri = request.getUri();
    	if (handlers.containsKey(uri)) {
    		HttpHandler handler = handlers.get(uri);
            handler.handle(request.getParams(), response);
    	} else {
    		LOG.warn("unkown uri: " + uri);
    		response.append("ret:" + ErrorCode.URI_UNKNOWN_ERROR);
    		response.append("msg" +  "unkown uri");
    	}
    }
    
    public static boolean checkUri(String uri) {
    	return handlers.containsKey(uri);
    }
	
}
