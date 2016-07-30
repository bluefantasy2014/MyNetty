package netty.httpserver.handlers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import netty.common.utils.StringUtil;
import netty.httpserver.HttpHandler;


public class WXServerAuthHandler extends HttpHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(WXServerAuthHandler.class);
	
	@Override
	public void handle(String request, JSONObject response)
	{
		LOG.info("request: {}", request);
		makeRsp(response, "success");
	}

	@Override
	public void handle(String request, StringBuffer response) {
		LOG.info("request 1 : {}", request);
		Map <String,String> paras = new HashMap<String,String>(); 
		StringUtil.parseQueryParam(request, paras);
		response.append(paras.get("echostr")); 
	}
}
