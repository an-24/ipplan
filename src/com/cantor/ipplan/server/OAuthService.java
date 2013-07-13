package com.cantor.ipplan.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.shared.Utils;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class OAuthService extends HttpServlet {
	
	private static String GOOGLE_CLIENT_SECRET;
	private static String GOOGLE_CLIENT_REDIRECTURI;
	
	@Override
	public void init() {
		GOOGLE_CLIENT_SECRET = getServletContext().getInitParameter("GoogleSecretKey");
		GOOGLE_CLIENT_REDIRECTURI = getServletContext().getInitParameter("GoogleRedirectUri");
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	         throws IOException,ServletException {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		PrintWriter writer = response.getWriter();
		writer.append("<html><head><script type=\"text/javascript\">");
		// exchange code on token
		if(code!=null) {
			try {
				HttpURLConnection conn = newConnection(Utils.GOOGLE_TOKEN_URL);
				try {
					conn.setConnectTimeout(10000);
					String message =new StringBuilder("code").append('=').append(URLEncoder.encode(code, "utf-8"))
							.append("&").append("client_id").append('=').append(URLEncoder.encode(Utils.GOOGLE_CLIENT_ID, "utf-8"))
							.append("&").append("client_secret").append('=').append(URLEncoder.encode(GOOGLE_CLIENT_SECRET, "utf-8"))
							.append("&").append("redirect_uri").append('=').append(URLEncoder.encode(GOOGLE_CLIENT_REDIRECTURI, "utf-8"))
							.append("&").append("grant_type=authorization_code")
							.toString();
					
					PrintWriter pw = new PrintWriter(conn.getOutputStream());
					pw.append(message);
					pw.flush();
					pw.close(); //sending
					
					//Get Response	
					String resp = readResponse(conn);
					Gson gson = new Gson();
					OAuthToken token = gson.fromJson(resp,OAuthToken.class);
					if(token!=null && token.exists()) {
						token.setGranted(new Date());
						
						DatabaseServiceImpl dbservice = new DatabaseServiceImpl(request.getSession());
						// проверка на token для drive
						if(state!=null && state.startsWith("drive")) {
							int tdisk = new Integer(state.split("=")[1]);
							dbservice.saveTokenDrive(tdisk, token);
						} else
						// иначе для других сервисов	
							dbservice.saveToken(token);
						
						writer.append("window.opener.doLogin();");
					}
				} finally {
					conn.disconnect();
				}
			} catch (Exception e) {
				Ipplan.error(e);
			}
		}
		writer.append("window.close();</script></head><body></body></html>");
		writer.flush();
	}
	
	public OAuthToken refreshToken(OAuthToken token) throws Exception {
		HttpURLConnection conn = newConnection(Utils.GOOGLE_TOKEN_URL);
		try {
			conn.setConnectTimeout(10000);
			String message =new StringBuilder("refresh_token").append('=').append(URLEncoder.encode(token.getRefreshToken(), "utf-8"))
					.append("&").append("client_id").append('=').append(URLEncoder.encode(Utils.GOOGLE_CLIENT_ID, "utf-8"))
					.append("&").append("client_secret").append('=').append(URLEncoder.encode(GOOGLE_CLIENT_SECRET, "utf-8"))
					.append("&").append("grant_type=refresh_token")
					.toString();
			
			PrintWriter pw = new PrintWriter(conn.getOutputStream());
			pw.append(message);
			pw.flush();
			pw.close(); //sending
			
			//Get Response	
			String resp = readResponse(conn);
			Gson gson = new Gson();
			OAuthToken newtoken = gson.fromJson(resp,OAuthToken.class);
			newtoken.setRefreshToken(token.getRefreshToken());
			newtoken.setGranted(new Date());
			return newtoken;
		} finally {
			conn.disconnect();
		}
	}

	public boolean validateToken(OAuthToken token) throws Exception {
		
		HttpURLConnection conn = newConnection(Utils.GOOGLE_TOKENINFO_URL+"?access_token="+token.getValue());
		try {
		    conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();
			
			//Get Response	
			String resp = readResponse(conn);
			Gson gson = new Gson();
			TokenInfoResponse info = gson.fromJson(resp,TokenInfoResponse.class);
			if(info!=null && info.expires_in!=null && info.expires_in>0) 
				return true;
			
		} finally {
			conn.disconnect();
		}
		
		return false;
	}
	
	public void revokeToken(OAuthToken token) throws Exception {
		URL url = new URL(Utils.GOOGLE_REVOKE_URL+"?token="+token.getValue());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestMethod("GET");
		try {
			conn.connect();
			readResponse(conn);
		} finally {
			conn.disconnect();
		}
	}
	
	public HttpURLConnection newConnection(String targetURL) throws Exception {
		URL url = new URL(targetURL);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	    connection.setUseCaches (false);
	    connection.setDoInput(true);
	    connection.setDoOutput(true);
	    return connection;
	}
	
	public String readResponse(HttpURLConnection con) throws Exception {
		InputStreamReader is = new InputStreamReader(con.getInputStream());
		StringBuilder sb=new StringBuilder();
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while(read != null) {
		    sb.append(read);
		    read =br.readLine();

		}
		is.close();
		return sb.toString();		
	}
	
	class TokenInfoResponse {
		String issued_to;
		String audience;
		String scope;
		Integer expires_in;
		String access_type;
	}
}
