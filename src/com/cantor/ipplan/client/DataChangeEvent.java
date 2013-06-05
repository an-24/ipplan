package com.cantor.ipplan.client;

public interface DataChangeEvent<T> {
	public boolean onBeforePost();
	public void onAfterPost();
	public boolean onBeforeDelete(T c);
	public void onAfterDelete(T c);
}
