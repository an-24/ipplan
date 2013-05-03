package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatabaseServiceAsync {
	void open(String sessId, AsyncCallback<PUserWrapper> callback);
}
