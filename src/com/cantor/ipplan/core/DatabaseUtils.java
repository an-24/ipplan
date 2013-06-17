package com.cantor.ipplan.core;

import javax.servlet.ServletContext;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DatabaseUtils {
	
	public static void startHibernateSessionForProfile(ServletContext ctx) throws Exception {
		final String SESSION_FACTORY_ID =  "sessionFactory";
		if(ctx.getAttribute(SESSION_FACTORY_ID)==null) {
	    	Configuration cfg = new Configuration().configure();
	    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
	    	PoolConnection pool = (PoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
	    	pool.setPool(ctx.getInitParameter("profileDBUrl"));
	    	SessionFactory sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	    	ctx.setAttribute(SESSION_FACTORY_ID, sessionFactory);
		}
	}

}
