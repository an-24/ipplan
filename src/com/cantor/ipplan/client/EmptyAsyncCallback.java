package com.cantor.ipplan.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EmptyAsyncCallback implements AsyncCallback<Void> {

	public EmptyAsyncCallback(){
	}
	
	@Override
	public void onFailure(Throwable caught) {
	}

	@Override
	public void onSuccess(Void result) {
	}

}
