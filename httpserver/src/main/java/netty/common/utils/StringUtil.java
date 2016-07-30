package netty.common.utils;

import java.util.Map;

public class StringUtil {
	public static boolean isEmpty(String str)
	{
		if (str == null || str.trim().length() <= 0)
		{
			return true;
		}
		return false;
	}
	
   public static void parseQueryParam(String queryString, Map<String,String> queryStringMap) {
    	if ( !isEmpty(queryString) ) {
	        String[] queryStringArray = queryString.split("&");
	        for (String qs : queryStringArray) {
	            String[] queryStringParam = qs.split("=");
	            String key = queryStringParam[0];
	            String value = "";
	            if (queryStringParam.length > 1)
	                value = queryStringParam[1];
	            queryStringMap.put(key, value);
	        }
    	}
    }
}