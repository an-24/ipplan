package com.cantor.ipplan.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.core.PoolConnection;
import com.cantor.ipplan.db.ud.PUserIdent;

@SuppressWarnings("deprecation")
public class UserTask extends TimerTask {

	static private HashMap<String,UserTask> allTasks = new HashMap<String, UserTask>();
	private PUserIdent user;
	private SessionFactory sessionFactory;
	 

	public UserTask(PUserIdent user, SessionFactory sessionFactory) {
		this.user = user;
		this.sessionFactory = sessionFactory;
	}
	
	static public void startAll(ServletContext ctx) {
		String storedir = ctx.getInitParameter("storeLocation");
		if(storedir==null) {
			Ipplan.error("Parameter 'storeLocation' not found. Change web.xml needed");
			return;
		}

		File root = new File(storedir);
		if(!root.exists() || !root.isDirectory()) {
			Ipplan.error(MessageFormat.format("Directory {0} not found.",new Object[]{storedir}));
			return;
		}			
		// сканирум каталог
		File[] children = root.listFiles();
		for (int i = 0; i < children.length; i++) {
			try {
				// если директория, то точно база данных
				// сессия Hibernate ресурсоемкая штука, поэтому попробуем
				// сделать проверку через обычный коннект
				if(children[i].isDirectory()) {
					String url ="jdbc:firebirdsql:localhost:"+
							root.getAbsolutePath()+File.separatorChar+children[i].getName()+File.separatorChar+"current.fdb";
					Ipplan.info("start UserTask for "+url);
					Connection conn= DriverManager.getConnection(url,"sysdba","masterkey");
					try {
						SessionFactory sessionFactory = null;
						Session session = null;
				    	try {
							Statement sql = conn.createStatement();
							ResultSet rs = sql.executeQuery("select * from PUSER");
							while(rs.next()) {
								int id = rs.getInt("PUSER_ID");
								int cntdur = rs.getInt("PUSER_CONTACT_SYNC_DURATION");
								int caldur = rs.getInt("PUSER_CALENDAR_SYNC_DURATION");
								if(cntdur>0 || caldur>0) {
									if(session==null) {
										// создаем session hibernate
								    	Configuration cfg = new Configuration().configure();
								    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
								    	PoolConnection pool = (PoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
										pool.setPool(url,ctx.
												getInitParameter("user"),
								    			ctx.getInitParameter("password"));
								    	sessionFactory = cfg.buildSessionFactory(serviceRegistry);
								    	session = sessionFactory.openSession();
									}
								    // узнаем пользователя
									PUserIdent user = (PUserIdent) session.load(PUserIdent.class, id);
									UserTask.startNewTask(user,sessionFactory);
								}
							};
				    	} finally {
				    		if(session!=null) session.close();
				    	}
					} finally {
						conn.close();
						conn = null;
					}
				}
			} catch (Exception e) {
				Ipplan.error(e);
			}
		};
		
	}
	
	static public void startNewTask(PUserIdent user,SessionFactory sessionFactory) {
		synchronized (allTasks) {
			UserTask oldut = allTasks.get(user.getPuserLogin());
			if(oldut!=null) {
				oldut.cancel();
				allTasks.remove(oldut);
				oldut = null;
			};
			if(user.getPuserContactSyncDuration()!=0 || user.getPuserCalendarSyncDuration()!=0) {
				int ONCE_PER_HALFHOUR = 5*60*1000; // один раз в 5 минут
				UserTask ut = new UserTask(user, sessionFactory);
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(ut, new Date().getTime()+ONCE_PER_HALFHOUR, ONCE_PER_HALFHOUR);
				allTasks.put(user.getPuserLogin(), ut);
			}
		}
	} 
	
	@Override
	public void run() {
		HttpSession sess = new HelperSessionImp();
		sess.setAttribute("userId", user.getId());
		sess.setAttribute("sessionFactory", sessionFactory);
		sess.setAttribute("loginUser",user);
		DatabaseServiceImpl dbs = new DatabaseServiceImpl(sess);
		Date currt = new Date();
		// обновляем token
		try {
			// проверяем истекает ли время token и
			// обновляем его
			OAuthToken token = dbs.getToken();
			if(!token.canRefresh())
				throw new Exception("Token cannot be refresh. 'refresh_token' is null");
			if(token.isExpired()) dbs.refreshGoogleToken();
			// если истекло по контактам
			// вызываем синхронизацию
			if(user.getPuserContactSyncDuration()>0)
			if(currt.getTime()>(user.getPuserContactLastsync().getTime()+user.getPuserContactSyncDuration()*1000)) {
				Ipplan.info("sync contacts process [user="+user.getPuserLogin()+"]...");
				dbs.syncContacts();
			}
			// если истекло по календарю
			// вызываем синхронизацию
			if(currt.getTime()>(user.getPuserCalendarLastsync().getTime()+user.getPuserCalendarSyncDuration()*1000)) {
				Ipplan.info("sync calendar process [user="+user.getPuserLogin()+"]...");
				dbs.syncCalendar();
			}
		} catch (Exception e) {
			Ipplan.error(e);
		}
	}
	
	// helper session
	class HelperSessionImp implements HttpSession {
		
	    private Hashtable<String,Object> attributes = new Hashtable<String,Object>();

		HelperSessionImp() {
		}

		@Override
		public long getCreationTime() {
			return 0;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public long getLastAccessedTime() {
			return 0;
		}

		@Override
		public int getMaxInactiveInterval() {
			return 0;
		}

		@Override
		public HttpSessionContext getSessionContext() {
			return null;
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public Object getValue(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return attributes.keys();
		}

		@Override
		public ServletContext getServletContext() {
			return null;
		}

		@Override
		public void setMaxInactiveInterval(int interval) {
		}

		@Override
		public String[] getValueNames() {
			return null;
		}

		@Override
		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}

		@Override
		public void putValue(String name, Object value) {
		}

		@Override
		public void removeAttribute(String name) {
			attributes.remove(name);
		}

		@Override
		public void removeValue(String name) {
		}

		@Override
		public void invalidate() {
		}

		@Override
		public boolean isNew() {
			return false;
		}
		
	}
	
	

}
