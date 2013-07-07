package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.ChartOptions;
import com.cantor.ipplan.shared.DistributeCost;
import com.cantor.ipplan.shared.DistributeStaff;
import com.cantor.ipplan.shared.DynamicMonthSeries;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnalyticalServiceAsync {

	void dynamicMonthData(Date start, Date finish, Integer[] statuses,
			ChartOptions options,
			AsyncCallback<List<DynamicMonthSeries>> callback);

	void distributeStaffs(Date start, Date finish, int status,
			ChartOptions options, AsyncCallback<List<DistributeStaff>> callback);

	void effectiveSales(Date start, Date finish, ChartOptions options,
			AsyncCallback<List<DistributeStaff>> callback);

	void distributeCosts(Date start, Date finish, ChartOptions options,
			AsyncCallback<List<DistributeCost>> callback);

}
