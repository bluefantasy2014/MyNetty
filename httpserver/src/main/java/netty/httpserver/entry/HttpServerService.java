package netty.httpserver.entry;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netty.httpserver.HttpServer;
import netty.httpserver.handlers.WXServerAuthHandler;


public class HttpServerService {
	private static final Logger LOG = LoggerFactory.getLogger(HttpServerService.class);
	private static final int PORT = 8090;
	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		HttpServer server  = new HttpServer();
		server.register("/wx/auth", new WXServerAuthHandler());
		server.register("/wx/auth/pt", new WXServerAuthHandler());
		try
		{
			server.start(PORT);
		}
		catch (Exception e)
		{
			LOG.error("start service failed: {}", e.getMessage());
		}
	}
	

}
