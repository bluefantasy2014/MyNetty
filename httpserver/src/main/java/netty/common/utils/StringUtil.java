package netty.common.utils;

import java.util.HashMap;
import java.util.Map;

public class StringUtil {
	public static boolean isEmpty(String str)
	{
		if (str == null || str.trim().length() <= 0)
		{
			return true;
		}else {
			return false;
		}
	}
	
   
    /*解析httpRequest 的URI中的查询参数为Map*/
	public static Map<String, String> parseQueryParamAsMap(String queryURI) {
		Map<String, String> map = new HashMap<String, String>();

		if (isEmpty(queryURI))
			return map;
		
		String[] paras = queryURI.split("&");
		for (String s : paras) {
			String[] params = s.split("=");
			map.put(params[0], params[1]);
		}
		return map;
	}
}