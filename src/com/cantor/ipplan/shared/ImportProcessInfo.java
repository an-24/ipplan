package com.cantor.ipplan.shared;

import java.io.Serializable;

public class ImportProcessInfo  implements Serializable,com.google.gwt.user.client.rpc.IsSerializable  {
	private int allCountRecord;
	private int syncCountRecord;
	
	public ImportProcessInfo() {
		
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
}
