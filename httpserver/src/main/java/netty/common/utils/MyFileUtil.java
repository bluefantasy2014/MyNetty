package netty.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;


public class MyFileUtil {
	 private static final Logger LOG = Logger.getLogger(MyFileUtil.class);
	 
	 public static ArrayList<String> readFileAsLines(String fileName) {
	    	ArrayList<String> lines = new ArrayList<String>();
	        BufferedReader bufReader = null;
	        try {
	            bufReader = new BufferedReader(new FileReader(new File(fileName)));
	            String tempString = null;
	            while ((tempString = bufReader.readLine()) != null) {
	            	lines.add(tempString);
	            }
	            bufReader.close();
	        } catch (Exception e) {
	        	LOG.error(e,e.fillInStackTrace());
	            lines.clear();
	        } finally {
	            if (bufReader != null) {
	                try {
	                    bufReader.close();
	                } catch (IOException e1) {
	                	LOG.error(e1,e1.fillInStackTrace());
	                }
	            }
	        }
	        return lines;
	    }
}
