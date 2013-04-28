package com.cantor.ipplan.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;

public class GWTCall {
	static HashMap<String,String> policyMap = new HashMap<String, String>();
	static {
		policyMap.put("com.cantor.ipplan.client.LoginService", "703C9E1B4E6FA76105953C36C58804C6");
	}
	
	private String uriBase;
	private String path;
	private Class intf;
	private String methodname;
	
	
	
	public GWTCall(String url, String path, Class intf, String methodname) {
		super();
		this.uriBase = url;
		if(!uriBase.endsWith("/")) uriBase += "/";
		this.path = path;
		this.intf = intf;
		this.methodname = methodname;
	}
	
	public Object invoke(Object[] args) throws Exception {
		String policyStrongName = policyMap.get(intf.getName());
		if(policyStrongName==null)
			throw new Exception("Policy not found for "+intf.getClass().getName());
		
		StringBuffer data = new StringBuffer();
		// protol GWT-RPC
		// from https://docs.google.com/document/d/1eG0YocsYYbNAtivkLtcaiEE5IOF5u4LUol8-LL0TIKU/edit?pli=1#
		// from http://blog.gdssecurity.com/labs/2009/10/8/gwt-rpc-in-a-nutshell.html
		data.append("7").append('|');  // version
		data.append("0").append('|');  // flags are set.
		data.append(4+2*args.length).append('|');    			// 0: count string in string table
		data.append(uriBase).append('|');    					// 1: uri
		data.append(policyStrongName).append('|');   			// 2: policy
		data.append(intf.getName()).append('|');    	// 3: service name
		data.append(methodname).append('|');    				// 4: method name
		for (int i = 0; i < args.length; i++) {                 // arg types
			data.append(args[i].getClass().getName()).append('|');
		}
		for (int i = 0; i < args.length; i++) {					// arg values
			data.append(args[i].toString()).append('|');
		}
		// define call
		data.append("1|2|3|4|");
		// args type and value
		data.append(args.length).append('|');    				 
		for (int i = 0; i < args.length; i++)                   
			data.append(5+i).append('|');
		for (int i = 0; i < args.length; i++) {                   
			data.append(5+args.length+i);
			if(i<args.length-1) data.append('|');
		}	
		
		String requestData = data.toString(); 
		URL url = new URL(uriBase+path);
/*		
		URL url = new URL(urlbase.getProtocol(),urlbase.getHost(),urlbase.getPort(),
				urlbase.getPath()+path+'/'+urlbase.getQuery());
*/		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty(RpcRequestBuilder.STRONG_NAME_HEADER, GWT.HOSTED_MODE_PERMUTATION_STRONG_NAME);
		connection.setRequestProperty(RpcRequestBuilder.MODULE_BASE_HEADER, uriBase);
		connection.setRequestProperty("Content-Type", "text/x-gwt-rpc; charset=utf-8");
		connection.setRequestProperty("Content-Length", "" + requestData.getBytes("UTF-8").length);
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(requestData);
	    writer.flush();
	    writer.close();
	    
	    int statusCode = connection.getResponseCode();
	    InputStream is = connection.getInputStream();
	    ByteArrayOutputStream response = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int len;
	    while ((len = is.read(buffer)) > 0) response.write(buffer, 0, len);
	    String sResponse = response.toString("UTF8");
		
		return null;
	}

}
