package com.cantor.ipplan.shared;

import java.util.Date;

public class SyncWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable  {
	private Integer id;
	private String imei;
    private Date last;

    public SyncWrapper(Integer id, String imei, Date last) {
		this.id = id;
		this.imei = imei;
		this.last = last;
	}
    
    public SyncWrapper() {
    	
    }

    public Integer getId() {
		return id;
	}

	public String getImei() {
		return imei;
	}

	public Date getLast() {
		return last;
	}
}
