package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class DynamicMonthSeries implements Serializable, IsSerializable {
	public int statusId;
	public List<DynamicMonthData> data = new ArrayList<DynamicMonthData>();
}
