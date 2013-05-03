package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	public PUserWrapper login(String nameOrEmail, String pswd, String device);
	public PUserWrapper isLogged();
	public void logout();
	public void changePassword(String newPswd) throws Exception;
	public String openDatabase() throws Exception;
	public PUserWrapper isAccessDatabase(String sessionId);

}
