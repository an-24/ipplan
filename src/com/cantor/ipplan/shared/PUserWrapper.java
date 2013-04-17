package com.cantor.ipplan.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PUserWrapper implements java.io.Serializable {
	public int puserId;
	public PUserWrapper owner;
	public String puserEmail;
	public String puserLogin;
	public String puserPswd;
	public String puserDbname;
	public int puserBoss;
	public Date puserCreated;
	public Date puserLastaccess;
	public String puserLastaccessDevice;
	public int puserLock;
	public String puserLockReason;
	public int puserTrial;
	public Set<SyncWrapper> syncs = new HashSet<SyncWrapper>(0);
	public Set<PaymentsWrapper> paymentses = new HashSet<PaymentsWrapper>(0);
	public Set<PUserWrapper> children = new HashSet<PUserWrapper>(0);
}
