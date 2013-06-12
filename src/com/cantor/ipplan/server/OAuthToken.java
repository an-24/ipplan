package com.cantor.ipplan.server;

public class OAuthToken {
	private String access_token;
	private String token_type;
	private int expires_in;
	
	public OAuthToken(){
		
	}

	public OAuthToken(String access_token, String token_type, int expires_in) {
		this.access_token = access_token;
		this.token_type = token_type;
		this.expires_in = expires_in;
	}

	public OAuthToken(String access_token, int expires_in) {
		this(access_token,"Bearer",expires_in);
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
}
