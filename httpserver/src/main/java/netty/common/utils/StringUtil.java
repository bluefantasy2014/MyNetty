package netty.common.utils;

import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
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
	
	/* Convert unicode to UTF8 String. 
	 * 汉字 “史” 对应的unicode 为  \u53F2 
	 * 如果输入这个unicode，输出就是 汉字  史
	 * */
	public static String unicodeToUtf8(String unicode){
		try {
			byte[] utf8 = unicode.getBytes("UTF-8");
			return new String(utf8,"UTF-8"); 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(unicodeToUtf8("\u53F2")); 
	}
}