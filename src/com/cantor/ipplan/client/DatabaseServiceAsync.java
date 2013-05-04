package com.cantor.ipplan.client;

import java.util.List;

import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatabaseServiceAsync {
	void open(String sessId, AsyncCallback<PUserWrapper> callback);
	void isLogged(AsyncCallback<PUserWrapper> callback);
	void attention(AsyncCallback<List<BargainWrapper>> callback);
}
