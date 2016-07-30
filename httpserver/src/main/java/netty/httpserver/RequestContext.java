package netty.httpserver;

import io.netty.handler.codec.http.HttpRequest;

public class RequestContext {

	private String uri;
    private String params = null;
    private HttpRequest httpRequest;
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setParams(String params) {
        this.params = params;
    }
    
    public String getParams() {
        return params;
    }
    
    public void setHttpRequest(HttpRequest httpRequest) {
    	this.httpRequest = httpRequest;
    }
    
    public HttpRequest getHttpRequest() {
    	return httpRequest;
    }
    
}
