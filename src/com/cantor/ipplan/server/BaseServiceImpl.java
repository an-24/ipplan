package com.cantor.ipplan.server;


import javax.servlet.http.HttpSession;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class BaseServiceImpl extends RemoteServiceServlet {


	private HttpSession session =  null;

	public BaseServiceImpl() {
		this(null);
	}

	public BaseServiceImpl(HttpSession session) {
		super();
		this.session = session; 
	}
	
	protected SessionFactory getSessionFactory() {
		return (SessionFactory) this.getSession().getAttribute("sessionFactory");
	}

	protected void createSessionFactory(String url) throws Exception {
		SessionFactory sessionFactory = getSessionFactory();
		if(sessionFactory==null) {
			// конфигурируем hibername
	    	Configuration cfg = new Configuration().configure();
	    	cfg.setProperty(Environment.CONNECTION_PROVIDER, "com.cantor.ipplan.server.UserDataPoolConnection");
	    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
	    	UserDataPoolConnection pool = (UserDataPoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
	    	pool.setPool(url);
	    	sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	    	// устанавливаем в сессии
	    	this.getSession().setAttribute("sessionFactory", sessionFactory);
		}
	}



	public HttpSession getSession() {
		return session==null?getThreadLocalRequest().getSession():session;
	}
	
	protected PUserWrapper getLoginUser() {
		HttpSession sess = this.getSession();
		return (PUserWrapper) sess.getAttribute("loginUser");
	}

	protected void setLoginUser(PUserWrapper u) {
		HttpSession sess = this.getSession();
		sess.setAttribute("loginUser",u);
	}
	
	
	private PUserWrapper isLogged() {
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory!=null?getLoginUser():null;
	}

	public void checkAccess() throws Exception {
		if(isLogged()==null)
			throw new Exception("Доступ запрещен");
	}
	
	protected int getUserId() {
		HttpSession sess = this.getSession();
		return (Integer) sess.getAttribute("userId");
	}
}
