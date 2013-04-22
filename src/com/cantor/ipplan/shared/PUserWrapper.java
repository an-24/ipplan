package com.cantor.ipplan.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.google.gwt.i18n.shared.CustomDateTimeFormat;

public class PUserWrapper implements java.io.Serializable {
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
	
}
