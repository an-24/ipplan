package com.cantor.ipplan.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class CostsWrapper implements Serializable, IsSerializable {
	public int costsId;
	public int costsSortcode;
	public String costsName;
}
