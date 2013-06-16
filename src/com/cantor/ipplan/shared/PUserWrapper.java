package com.cantor.ipplan.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class PUserWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int puserId;
	public PUserWrapper owner;
	public String puserEmail;
	public String puserLogin;
	public String puserDbname;
	public int puserBoss;
	public Date puserCreated;
	public Date puserLastaccess;
	public String puserLastaccessDevice;
	public int puserLock;
	public String puserLockReason;
	public int puserTarif;
	public Set<SyncWrapper> syncs = new HashSet<SyncWrapper>(0);
	public Set<PaymentsWrapper> paymentses = new HashSet<PaymentsWrapper>(0);
	public Set<PUserWrapper> children = new HashSet<PUserWrapper>(0);
	public int puserFlags;
	public MessageWrapper lastSystemMessage;
	public boolean tempflag;
	public int puserTaxtype;
	public int puserContactSyncDuration;
	public int puserCalendarSyncDuration;
	
	public PUserWrapper() {
	}
	
	public PUserWrapper(String name,String email) {
		puserLogin = name;
		puserEmail = email;
	}
	

	public String getFullName() {
		if(puserLogin==null) return puserEmail;else
			return puserLogin+"("+puserEmail+")";
	}

	public Object findChildById(int id) {
		for (PUserWrapper item : children) {
			if(item.puserId==id) return item;
		}
		return null;
	}
	
	public boolean isDatabaseCreateNeeded() {
		return puserDbname.isEmpty() || (owner!=null && owner.puserDbname.isEmpty());
	}

	public boolean isAllowedCreateDatabase() {
		return owner==null;
	}
	
	public String getDatabaseName() {
		return (owner==null)?puserDbname:owner.puserDbname;
	}
	
}
