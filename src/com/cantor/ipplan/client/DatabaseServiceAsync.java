package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.SearchInfo;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.TaskWrapper;
import com.cantor.ipplan.shared.TasktypeWrapper;
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
	void deleteBargain(List<BargainWrapper> list, AsyncCallback<Void> asyncCallback);
	void dropTemporalyBargain(int id, AsyncCallback<Void> callback);
	void saveBargain(BargainWrapper bargain, boolean drop,
			AsyncCallback<BargainWrapper> callback);
	void saveTemporalyBargain(BargainWrapper bargain,AsyncCallback<Void> callback);
	void findCustomer(String query,
			AsyncCallback<List<CustomerWrapper>> callback);
	void getAllStatuses(AsyncCallback<List<StatusWrapper>> callback);
	void findCost(String newtext,AsyncCallback<List<CostsWrapper>> asyncCallback);
	void syncContacts(AsyncCallback<ImportExportProcessInfo> callback);
	void refreshGoogleToken(AsyncCallback<Void> callback);
	void setContactsAutoSync(int duration, AsyncCallback<Void> callback);
	void setCalendarAutoSync(int duration, AsyncCallback<Void> callback);
	void syncCalendar(AsyncCallback<ImportExportProcessInfo> callback);
	void addCustomer(CustomerWrapper value,
			AsyncCallback<CustomerWrapper> callback);
	void deleteCustomer(int id, AsyncCallback<Boolean> callback);
	void updateCustomer(CustomerWrapper value, AsyncCallback<Void> callback);
	void deleteCustomer(List<CustomerWrapper> list, AsyncCallback<Void> callback);
	void findBargain(String text, Date date, boolean allUser, boolean[] stats,
			AsyncCallback<List<BargainWrapper>> callback);
	void prevBargainVersion(int id, AsyncCallback<BargainWrapper> callback);
	void nextBargainVersion(int id, AsyncCallback<BargainWrapper> callback);
	void getTasktypes(AsyncCallback<List<TasktypeWrapper>> callback);
	void getTask(int bargainId, AsyncCallback<List<TaskWrapper>> callback);
	void executedTask(int id, AsyncCallback<Void> callback);
	void deleteTask(int id, AsyncCallback<Boolean> callback);
	void addTask(TaskWrapper task, AsyncCallback<TaskWrapper> callback);
	void updateTask(TaskWrapper task, AsyncCallback<TaskWrapper> callback);
	void isNewVersionBargain(BargainWrapper bargain, boolean savestate,
			AsyncCallback<Boolean> callback);
	void getConfig(String name, AsyncCallback<String> callback);
	void searchFile(int typeDrive, String searchStr,
			AsyncCallback<SearchInfo> callback);
	void exit(AsyncCallback<Void> callback);
}
