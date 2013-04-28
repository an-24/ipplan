package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserProfile extends Ipplan {

	private PUserWrapper user;	
    static {
    	tokenForms.put("login", FormLogin.class);
    	tokenForms.put("profile", FormProfile.class);
    	INIT_TOKEN = "login";
    }
    
	public void onModuleLoad() {
		super.onModuleLoad();
	}

	public void setUser(PUserWrapper user) {
		this.user = user;
	}
    
	public void refreshForm(final Class type) {
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
				LoginServiceAsync service = GWT.create(LoginService.class);
				service.isLogged(new AsyncCallback<PUserWrapper>() {
					@Override
					public void onSuccess(PUserWrapper user) {
						if(user==null) {
							History.newItem(INIT_TOKEN);
						} else {
							setUser(user);
							refreshForm(type);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						showError(caught);
					}
				});
			} else 
			// profile
			if(type==FormProfile.class) {
				FormProfile f = new FormProfile(this, getRootInHTML(),this.user);
				f.show();
			};
	}
}
