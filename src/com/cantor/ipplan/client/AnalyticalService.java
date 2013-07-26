package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.ChartOptions;
import com.cantor.ipplan.shared.DistributeCost;
import com.cantor.ipplan.shared.DistributeStaff;
import com.cantor.ipplan.shared.DynamicMonthSeries;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("analytical")
public interface AnalyticalService extends RemoteService {
	
	public List<DynamicMonthSeries> dynamicMonthData(Date start, Date finish, Integer[] statuses, ChartOptions options) throws Exception;
	public List<DistributeStaff> distributeStaffs(Date start, Date finish, int status, ChartOptions options) throws Exception;
	public List<DistributeStaff> effectiveSales(Date start, Date finish, ChartOptions options) throws Exception;
	public List<DistributeCost> distributeCosts(Date start, Date finish, ChartOptions options) throws Exception;

}
