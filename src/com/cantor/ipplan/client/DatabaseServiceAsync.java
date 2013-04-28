package com.cantor.ipplan.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatabaseServiceAsync {
	void create(String name, String userEmail, AsyncCallback<String> callback);
	void open(String name, String userEmail, AsyncCallback<String> callback);
	void close(String key, AsyncCallback<Void> callback);
}
