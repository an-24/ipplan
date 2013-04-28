package com.cantor.ipplan.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserData extends Ipplan {

	protected String keyDB;
	
	static {
    	tokenForms.put("main", FormMain.class);
    	INIT_TOKEN = "main";
    }

	
	public void onModuleLoad() {
		String initToken = History.getToken();
		if(!initToken.isEmpty()) {
			String[] tokens = initToken.split(",");
			if(tokens[0].equals("create")) {
				createDatabase(tokens[1],tokens[2]);
				return;
			} else
			if(tokens[0].equals("open")) {
				openDatabase(tokens[1],tokens[2]);
				return;
			};
				
		}
		super.onModuleLoad();
	}

	public void refreshForm(final Class type) {
		//unknown form
		if(type==null) {
			getRootInHTML().clear();
		} else
		// login
		if(type==FormLogin.class) {
			FormMain f = new FormMain(this, getRootInHTML());
			f.show();
		};		
	}
	
	private void createDatabase(String db, String user) {
		DatabaseServiceAsync service = GWT.create(DatabaseService.class);
		service.create(db, user, new AsyncCallback<String>() {
			public void onSuccess(String result) {
				UserData.this.keyDB = result;
				History.newItem(INIT_TOKEN);
			}
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}

	private void openDatabase(String db, String user) {
		DatabaseServiceAsync service = GWT.create(DatabaseService.class);
		service.open(db, user, new AsyncCallback<String>() {
			public void onSuccess(String result) {
				UserData.this.keyDB = result;
				History.newItem(INIT_TOKEN);
			}
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}



}
