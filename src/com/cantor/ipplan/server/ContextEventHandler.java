package com.cantor.ipplan.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class ContextEventHandler implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
    	Configuration cfg = new Configuration().configure();
    	SessionFactory sessionFactory = cfg.buildSessionFactory();
    	event.getServletContext().setAttribute("sessionFactory", sessionFactory);
	}

}
