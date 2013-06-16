package com.cantor.ipplan.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;

public class OAuth2 {

	private String providerURI;
	private String clientId;
	private String scope;
	private String redirectURI = null;
	private Window window = null;
	private EventOnCloseWindow closeEvent;
	
	public OAuth2(String providerURI, String clientId, String scope) {
		this(providerURI,clientId,scope,null);
	}
	
	public OAuth2(String providerURI, String clientId, String scope,
			String redirectURI) {
		this.providerURI = providerURI;
		this.clientId = clientId;
		this.scope = scope;
		this.redirectURI = redirectURI;
		register();
	}
	
	public void login(EventOnCloseWindow cb) {
		setCloseEvent(cb);
		window = openWindow(toRequestAuthorizationCodeUrl(false));
	}

	public void loginOffline(EventOnCloseWindow cb) {
		setCloseEvent(cb);
		window = openWindow(toRequestAuthorizationCodeUrl(true));
	}

	public void setCloseEvent(EventOnCloseWindow cb) {
    	closeEvent = cb;
	}
	
	private String toRequestAuthorizationCodeUrl(boolean offline) {
		StringBuilder sb = new StringBuilder(providerURI)
    		.append("?").append("client_id").append("=").append(encode(clientId))
    		.append("&").append("response_type").append("=").append("code")
    		.append("&").append("scope").append("=").append(encode(scope))
    		.append("&").append("redirect_uri").append("=").append(redirectURI==null?"https://localhost":encode(redirectURI))
    		.append("&").append("approval_prompt=force");
		if(offline) sb.append("&").append("access_type=offline");
		return sb.toString();
	}
	
    static public native String encode(String url) /*-{
      var regexp = /%20/g;
      return encodeURIComponent(url).replace(regexp, "+");
    }-*/;

    static public native String decode(String url) /*-{
      var regexp = /\+/g;
      return decodeURIComponent(url.replace(regexp, "%20"));
    }-*/;

    private native void register() /*-{
    	var self = this;
    	$wnd.doLogin = $entry(function() {
      		self.@com.cantor.ipplan.client.OAuth2::finish()();
    	});
  	}-*/;

    static private native Window openWindow(String url) /*-{
	    return $wnd.open(url, 'popupWindow', 'width=800,height=600');
	}-*/;

    void finish() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			public void execute() {
		    	window = null;
		    	if(closeEvent!=null) closeEvent.onCloseWindow();
			}
		});	
    }
    
    static interface EventOnCloseWindow {
    	public void onCloseWindow();
    }
    
    static public final class Window extends JavaScriptObject {
    	
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
