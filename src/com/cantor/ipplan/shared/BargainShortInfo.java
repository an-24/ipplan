package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.util.Date;
import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class BargainShortInfo implements IsSerializable, Serializable {
	public int bargainId;
	public StatusWrapper status;
	public int bargainVer;
	public Date bargainCreated;
	public String bargainNote;

	public BargainShortInfo() {
		
	}
	
	public BargainShortInfo(BargainWrapper b) {
		bargainId = b.bargainId;
		status = b.status;
		bargainVer = b.bargainVer;
		bargainCreated = b.bargainCreated;
		bargainNote = b.bargainNote;
	}
}
