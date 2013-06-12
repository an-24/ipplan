package com.cantor.ipplan.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.shared.Utils;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class OAuthService extends HttpServlet {
	private static final String GOOGLE_CLIENT_SECRET = "XABbXf5iX9vwBHMHSHAtGztQ";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	         throws IOException,ServletException {
		String code = request.getParameter("code");
		PrintWriter writer = response.getWriter();
		writer.append("<html><body><script>window.close();</script></body></html>");
		writer.flush();
		// exchange code on token
		if(code!=null) {
			try {
				HttpURLConnection conn = newConnection(Utils.GOOGLE_TOKEN_URL);
				try {
					conn.setConnectTimeout(10000);
					String message =new StringBuilder("code").append('=').append(URLEncoder.encode(code, "utf-8"))
							.append("&").append("client_id").append('=').append(URLEncoder.encode(Utils.GOOGLE_CLIENT_ID, "utf-8"))
							.append("&").append("client_secret").append('=').append(URLEncoder.encode(GOOGLE_CLIENT_SECRET, "utf-8"))
							.append("&").append("redirect_uri").append('=').append(URLEncoder.encode(Utils.REDIRECT_URI, "utf-8"))
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
					
					new DatabaseServiceImpl().saveToken(token);
					//Ipplan.info(resp);
				} finally {
					conn.disconnect();
				}
			} catch (Exception e) {
				Ipplan.error(e);
			}
			
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
	
}
