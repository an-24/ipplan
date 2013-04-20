package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProfileServiceAsync {

	void setUserData(PUserWrapper data, AsyncCallback<Void> callback);
	void checkUser(String name, String email, AsyncCallback<Boolean> callback);

}
