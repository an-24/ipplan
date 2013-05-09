package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatabaseServiceAsync {
	void open(String sessId, AsyncCallback<PUserWrapper> callback);
	void isLogged(AsyncCallback<PUserWrapper> callback);
	void attention(AsyncCallback<List<BargainWrapper>> callback);
	void getTotals(AsyncCallback<BargainTotals[]> callback);
	void newBargain(String name, int startStatus,
			AsyncCallback<BargainWrapper> callback);
	void newBargain(String name, int startStatus, Date start, Date finish,
			AsyncCallback<BargainWrapper> callback);
	void getTemporalyBargains(AsyncCallback<List<BargainWrapper>> callback);
}
