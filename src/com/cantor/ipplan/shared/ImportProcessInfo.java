package com.cantor.ipplan.shared;

import java.io.Serializable;

public class ImportProcessInfo  implements Serializable,com.google.gwt.user.client.rpc.IsSerializable  {
	
	public static final int TOKEN_EXPIRED = -1;
	public static final int TOKEN_NOTFOUND = -2; 
	
	private int allCountRecord;
	private int syncCountRecord;
	private int error = 0;
	
	public ImportProcessInfo() {
		
	}

	public ImportProcessInfo(int error) {
		this.error = error;
	}
	
	public ImportProcessInfo(int allCountRecord, int syncCountRecord) {
		this.allCountRecord = allCountRecord;
		this.syncCountRecord = syncCountRecord;
	}
	
	public int getAllCountRecord() {
		return allCountRecord;
	}
	public int getSyncCountRecord() {
		return syncCountRecord;
	}

	public int getError() {
		return error;
	}
}
