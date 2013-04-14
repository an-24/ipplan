package com.cantor.ipplan.client;

import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
	void isLogged(AsyncCallback<PUser> callback);
	void login(String nameOrEmail, String pswd, AsyncCallback<PUser> callback);
}
