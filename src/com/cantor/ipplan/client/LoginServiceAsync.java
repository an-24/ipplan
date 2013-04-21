package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
	void isLogged(AsyncCallback<PUserWrapper> callback);
	void login(String nameOrEmail, String pswd, String device,
			AsyncCallback<PUserWrapper> callback);
	void logout(AsyncCallback<Void> callback);
}
