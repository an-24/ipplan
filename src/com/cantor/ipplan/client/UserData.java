package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserData extends Ipplan {

	protected PUserWrapper user;
	
	static {
    	tokenForms.put("main", FormMain.class);
    	INIT_TOKEN = "main";
    }

	
	public void onModuleLoad() {
		String initToken = History.getToken();
		if(!initToken.isEmpty()) {
			// проверка на запрос доступа к сессии
			String[] tokens = initToken.split("=");
			if(tokens[0].equals("session")) {
				openDatabase(tokens[1]);
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
	

	private void openDatabase(String sessId) {
		DatabaseServiceAsync service = GWT.create(DatabaseService.class);
		service.open(sessId, new AsyncCallback<PUserWrapper>() {
			public void onSuccess(PUserWrapper result) {
				UserData.this.user = result;
				History.newItem(INIT_TOKEN);
			}
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}



}
