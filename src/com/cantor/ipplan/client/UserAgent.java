package com.cantor.ipplan.client;

public class UserAgent {
	/**
	* Gets the name of the used browser.
	*/
	public static native String getBrowserName() /*-{
	    return navigator.userAgent;
	}-*/;

	/**
	* Returns true if the current browser is Chrome.
	*/
	public static boolean isChromeBrowser() {
	    return getBrowserName().toLowerCase().contains("chrome");
	}

	/**
	* Returns true if the current browser is Firefox.
	*/
	public static boolean isFirefoxBrowser() {
	    return getBrowserName().toLowerCase().contains("firefox");
	}

	/**
	* Returns true if the current browser is IE (Internet Explorer).
	*/
	public static boolean isIEBrowser() {
	    return getBrowserName().toLowerCase().contains("msie");
	}

	public static boolean isOpera() {
	    return getBrowserName().toLowerCase().contains("opera");
	}
	
	public static boolean isIPhone() {
	    return getBrowserName().contains("iPhone");
	}
}
