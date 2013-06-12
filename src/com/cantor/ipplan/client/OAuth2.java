package com.cantor.ipplan.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;

public class OAuth2 {

	private String providerURI;
	private String clientId;
	private String scope;
	private String redirectURI = null;
	
	public OAuth2(String providerURI, String clientId, String scope) {
		this(providerURI,clientId,scope,null);
	}
	
	public OAuth2(String providerURI, String clientId, String scope,
			String redirectURI) {
		this.providerURI = providerURI;
		this.clientId = clientId;
		this.scope = scope;
		this.redirectURI = redirectURI;
	}
	
	public void login(Callback<String, Throwable> callback) {
		openWindow(toRequestAuthorizationCodeUrl());
	}
	
	private String toRequestAuthorizationCodeUrl() {
		    return new StringBuilder(providerURI)
		    	.append("?").append("client_id").append("=").append(encode(clientId))
		        .append("&").append("response_type").append("=").append("code")
		        .append("&").append("scope").append("=").append(encode(scope))
		        .append("&").append("redirect_uri").append("=").append(redirectURI==null?"https://localhost":encode(redirectURI))
		        .toString();
		  }
	
    static public native String encode(String url) /*-{
      var regexp = /%20/g;
      return encodeURIComponent(url).replace(regexp, "+");
    }-*/;

    static public native String decode(String url) /*-{
      var regexp = /\+/g;
      return decodeURIComponent(url.replace(regexp, "%20"));
    }-*/;

    static private native Window openWindow(String url) /*-{
	    return window.open(url, 'popupWindow', 'width=800,height=600');
	}-*/;

    static final class Window extends JavaScriptObject {
        protected Window() {
        }

        native boolean isOpen() /*-{
          return !this.closed;
        }-*/;

        native void close() /*-{
          this.close();
        }-*/;
      }
}
