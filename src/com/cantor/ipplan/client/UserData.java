package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserData extends Ipplan {

	private DatabaseServiceAsync dbservice;
	//protected String sessionId;
	
	static {
    	tokenForms.put("main", FormMain.class);
    	INIT_TOKEN = "main";
    }

	
	public void onModuleLoad() {
		// проверка
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
		if(user==null) {
			reguestLogin();
			return;
		}	
	}


	@SuppressWarnings("rawtypes")
	public void refreshForm(Class type, String session_id) {
		if(user==null) return;
		// пробуем определить номер вкладки у main
		int numTab = 0;
		String t = History.getToken();
		if(t!=null && t.startsWith("main")) {
			type = FormMain.class;
			int n = t.indexOf('.');
			if(n>=0) { 		
				numTab = Integer.valueOf(t.substring(n+1));
				// форма была уже создана
				if(FormMain.currentForm!=null) {
					FormMain.currentForm.selectTab(numTab);
					return;
				}
			}	
		};
		//unknown form
		if(type==null) {
			getRootInHTML().clear();
		} else
		// login
		if(type==FormMain.class) {
			FormMain f = new FormMain(this, getRootInHTML(),user,numTab);
			f.show();
		};		
	}

	private DatabaseServiceAsync getDataBaseService() {
		if(dbservice!=null) return dbservice; 
		dbservice = GWT.create(DatabaseService.class);
		return dbservice;
	}
	
	private void reguestLogin() {
		History.newItem("",false);
		DatabaseServiceAsync service = getDataBaseService();
		service.isLogged(new AsyncCallback<PUserWrapper>() {
			public void onSuccess(PUserWrapper result) {
				if(result==null) {
					FormMain.quit();
				} else {
					setUser(result);
					History.newItem(INIT_TOKEN);
				}
			}
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}



	private void openDatabase(final String sessId) {
		DatabaseServiceAsync service = getDataBaseService();
		service.open(sessId, new AsyncCallback<PUserWrapper>() {
			public void onSuccess(PUserWrapper result) {
				UserData.this.user = result;
				Cookies.setCookie("sid", sessId);
				History.newItem(INIT_TOKEN);
			}
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}





}
