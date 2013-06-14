package com.cantor.ipplan.server;

import java.util.Date;

public class OAuthToken {
	private String access_token = null;
	private String refresh_token = null;
	private String token_type;
	private int expires_in; // in second
	private Date granted;  	
	
	public OAuthToken(){
		
	}

	public OAuthToken(String access_token, String refresh_token, String token_type, int expires_in, Date granted) {
		this.access_token = access_token;
		this.refresh_token = refresh_token;
		this.token_type = token_type;
		this.expires_in = expires_in;
		this.granted = granted;
	}

	public OAuthToken(String access_token, String refresh_token, int expires_in, Date granted) {
		this(access_token,refresh_token,"Bearer",expires_in,granted);
	}

	public String getValue() {
		return access_token;
	}

	public String getTokenType() {
		return token_type;
	}

	public int getExpiresIn() {
		return expires_in;
	}

	public void setGranted(Date d) {
		granted = d;
		
	}

	public Date getGranted() {
		return granted;
	}

	public boolean exists() {
		return access_token!=null; 
	}

	public boolean isExpired() {
		return new Date().getTime()>(granted.getTime()+expires_in*1000);
	}

	public String getRefreshToken() {
		return refresh_token;
	}

	public void setRefreshToken(String refresh_token) {
		this.refresh_token = refresh_token;
	}

}
