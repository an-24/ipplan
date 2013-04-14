package com.cantor.ipplan.client;

import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	public PUser login(String nameOrEmail, String pswd);
	public PUser isLogged();

}
