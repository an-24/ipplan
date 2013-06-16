package com.cantor.ipplan.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.core.PoolConnection;

public class ContextEventHandler implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		event.getServletContext().removeAttribute("sessionFactory");
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
    	try {
	    	Configuration cfg = new Configuration().configure();
	    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
	    	PoolConnection pool = (PoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
	    	//TODO from web.xml
			pool.setPool("jdbc:firebirdsql:localhost:D:\\Database\\IPPLAN_UP.FDB");
	    	SessionFactory sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	    	event.getServletContext().setAttribute("sessionFactory", sessionFactory);
	    	// старт задач по расписанию
	    	UserTask.startAll(event.getServletContext());
		} catch (Exception e) {
			Ipplan.error(e);
		}
    	
	}

}
