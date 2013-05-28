package com.cantor.ipplan.shared;

import com.cantor.ipplan.db.ud.PUserIdent;

@SuppressWarnings("serial")
public class StatusWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {

	public static final int PRIMARY_CONTACT = 1;
	public static final int TALK = 10;
	public static final int DECISION_MAKING = 20;
	public static final int RECONCILIATION_AGREEMENT = 30;
	public static final int EXECUTION = 40;
	public static final int SUSPENDED = 50;
	public static final int COMPLETION = 60;
	public static final int CLOSE_OK = 100;
	public static final int CLOSE_FAIL = 99;
	
	public int statusId;
	public int puser_owner_id;
	public String statusName;
	public int statusDayLimit;
}
