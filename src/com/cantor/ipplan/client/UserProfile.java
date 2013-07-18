package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserProfile extends Ipplan {

    static {
    	tokenForms.put("login", FormLogin.class);
    	tokenForms.put("profile", FormProfile.class);
    	tokenForms.put("enter", FormLoginRoute.class);
    	INIT_TOKEN = "login";
    }
    
	public void onModuleLoad() {
		super.onModuleLoad();
	}

	public void refreshForm(final Class<? extends Form> type,final String sessionId) {
		//unknown form
		if(type==null) {
			getRootInHTML().clear();
		} else
		// login
		if(type==FormLogin.class) {
			FormLogin f = new FormLogin(this, getRootInHTML());
			f.show();
		} else 
			// check login user
			if(this.user==null) {
				obtainUser(type, sessionId); 
			} else 
				// profile
				if(type==FormProfile.class) {
					FormProfile f = new FormProfile(this, getRootInHTML(),this.user);
					f.show();
				} else
					if(type==FormLoginRoute.class) {
						FormLoginRoute.route(this.user);
					};
	}

	private void obtainUser(final Class<? extends Form> type,
			final String sessionId) {
		LoginServiceAsync service = GWT.create(LoginService.class);
		// нужно подменить сессию
		if(sessionId!=null)
			Cookies.setCookie("JSESSIONID", sessionId);
			
		service.isLogged(new AsyncCallback<PUserWrapper>() {
			@Override
			public void onSuccess(PUserWrapper user) {
				if(user==null) {
					History.newItem(INIT_TOKEN);
				} else {
					// перегрузим стр, чтобы  сессия в url не торчала
					if(sessionId!=null) {
						History.newItem("profile");
						return;
					}	
					setUser(user);
					refreshForm(type,null);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});
	}
}
