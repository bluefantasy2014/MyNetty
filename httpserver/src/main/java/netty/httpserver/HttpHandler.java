package netty.httpserver;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
//local change for testing 
public abstract class HttpHandler {
 
	//for json return type
	public abstract void handle(String request, JSONObject response);
	//for plaintext return typr
	public abstract void handle(String request, StringBuffer response);
    
	protected String rspDataType() {
    	return "data";
    }
    
    protected void makeErrRsp(JSONObject response, int errCode, String msg) {
    	response.put("ret", errCode);
    	response.put("msg", msg);
    }
    //for json
    protected void makeRsp(JSONObject response, Object data) {
    	response.put("ret", 0);
    	response.put(rspDataType(), data);
    }
    
    protected static Map<String,String> parseKVString(String queryString) {
    	Map<String,String> queryStringMap = new HashMap<String,String>();
    	if (queryString != null && queryString.length() > 0) {
    		String[] queryStringSplit = queryString.split("&");
    		for (String qs : queryStringSplit) {
    			String[] queryStringParam = qs.split("=");
    			String key = queryStringParam[0];
    			String value = "";
    			if (queryStringParam.length > 1) {
                    value = queryStringParam[1];
    			}
                queryStringMap.put(key, value);
    		}
    	}
        return queryStringMap;
    }
    
}
