package com.cantor.ipplan.shared;

import com.cantor.ipplan.db.ud.PUserIdent;

@SuppressWarnings("serial")
public class StatusWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {

	public int statusId;
	public int puser_owner_id;
	public String statusName;
}
