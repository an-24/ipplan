package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
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
	void editBargain(int id, AsyncCallback<BargainWrapper> callback);
	void deleteBargain(int id, AsyncCallback<Boolean> callback);
	void dropTemporalyBargain(int id, AsyncCallback<Void> callback);
	void saveBargain(BargainWrapper bargain, boolean drop,
			AsyncCallback<BargainWrapper> callback);
	void saveTemporalyBargain(BargainWrapper bargain,
			AsyncCallback<Void> callback);
	void findCustomer(String query,
			AsyncCallback<List<CustomerWrapper>> callback);
	void getAllStatuses(AsyncCallback<List<StatusWrapper>> callback);
	void findCost(String newtext,AsyncCallback<List<CostsWrapper>> asyncCallback);
	void syncContacts(AsyncCallback<ImportProcessInfo> callback);
	void refreshGoogleToken(AsyncCallback<Void> callback);
	void setContactsAutoSync(int duration, AsyncCallback<Void> callback);
	void setCalendarAutoSync(int duration, AsyncCallback<Void> callback);
	void syncCalendar(AsyncCallback<ImportProcessInfo> callback);
}
