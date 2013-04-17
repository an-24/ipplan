package com.cantor.ipplan.core;

public interface DataBridge<C> {
	public C toClient();
	public void fromClient(C data);
	public void fetch(boolean deep);
}
