package com.cantor.ipplan.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.core.DatabaseUtils;
import com.cantor.ipplan.db.up.Messages;
import com.cantor.ipplan.db.up.PUser;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet  implements LoginService {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
    	try {
    		DatabaseUtils.startHibernateSessionForProfile(config.getServletContext());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public PUserWrapper login(String nameOrEmail, String pswd, String device) {
		if(nameOrEmail==null || nameOrEmail.isEmpty()) {
			Ipplan.error("Ошибка входа в систему пользователя: имя пустое");
			return null;
		}
		
		HttpSession oldsess = this.getThreadLocalRequest().getSession(false);
		if(oldsess!=null) 
			oldsess.invalidate();
		
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		Transaction tx = session.beginTransaction();
    		try {
    			Query q = session.createQuery("select u from PUser u "+
    		                                  "where (u.puserLogin=:login OR u.puserEmail=:login)AND u.puserPswd=:pswd");
    			q.setString("login", nameOrEmail);
    			q.setString("pswd", hashPassword(pswd));
    			PUser u = (PUser) q.uniqueResult();
    			if(u!=null) {
    				// fetch lazy
    				u.fetch(true);
    				
    				HttpSession sess = this.getThreadLocalRequest().getSession();
    				sess.setAttribute("user", u);
    				
    				PUserWrapper uclient = u.toClient(); 
    				// добавим недостающие данные
    				setDataClient(session,u,uclient);
    				
    				// модмфицируем lastaccess
    				u.setPuserLastaccess(new Date());
    				u.setPuserLastaccessDevice(device);
    				//session.update(u);
    				
        			tx.commit();
    				
    				return uclient;
    			}	
    			tx.commit();
    		} catch (Exception e) {
    			tx.rollback();
    			Ipplan.error("Ошибка входа в систему пользователя "+nameOrEmail,e);
			}
    	} finally {
    		session.close();
    	}
		return null;
	}

	@Override
	public void logout() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUser u = (PUser) sess.getAttribute("user");
		if(u!=null) sess.removeAttribute("user");
	}

	@Override
	public PUserWrapper isLogged() {
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		HttpSession sess = this.getThreadLocalRequest().getSession();
			if (sess.isNew()) return null;
			PUser u = (PUser) sess.getAttribute("user");
			if(u==null) return null; else {
    			session.refresh(u);
				PUserWrapper uclient = u.toClient();
				setDataClient(session,u,uclient);
				return uclient;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public void changePassword(String newPswd) throws Exception {
		PUser user = checkLogin();
		if(newPswd.length()<7) 
			throw new Exception("Пароль слишком короткий");
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		Transaction tx = session.beginTransaction();
    		try {
    			user.setPuserPswd(hashPassword(newPswd));
    			session.update(user);
    			tx.commit();
    		} catch (Exception e) {
    			tx.rollback();
    			Ipplan.error(e);
    			throw new Exception("Ошибка смены пароля");
			}
    	} finally {
    		session.close();
    	}
	}

	private PUser checkLogin() throws Exception {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUser u = (PUser) sess.getAttribute("user");
		if (sess.isNew() || u==null ) 
			throw new Exception("Необходимо войти в систему");
		return u;
	}
	
	private String hashPassword(String pswd) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(pswd.getBytes(), 0, pswd.length());
		pswd = new BigInteger(1, digest.digest()).toString(16);
		return pswd;
	}


	private void setDataClient(Session session, PUser user, PUserWrapper uclient) {
		// добавляем подчиненных, которые еще не согласились tempflag = true
		List<Messages> lm = Messages.getListMessagesBySender(session, user, Messages.MT_JOIN_TO_OWNER);
		for (Messages m : lm) {
			PUserWrapper ruser = new PUserWrapper(m.getPuserByPuserRId().getPuserLogin(),m.getPuserByPuserRId().getPuserEmail());
			ruser.tempflag = true;
			if(uclient.findChildById(ruser.puserId)!=null)
				uclient.children.add(ruser);
		}
		// ищем последнее системное сообщение направленное этому пользователю
		Messages m = Messages.getLastMessageTo(session, user, Messages.MT_JOIN_TO_OWNER);
		uclient.lastSystemMessage = m!=null?m.toClient():null;
		
	}

	@Override
	public String openDatabase() throws Exception {
		PUser user = checkLogin();
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		Transaction tx = session.beginTransaction();
    		try {
				session.update(user);
    			// расчет доступности пользователя 
    			calcLockFlag(user);
    			if(user.getPuserLock()!=0) {
    				// доступнсоть могла изменится
    				tx.commit();
    				throw new Exception("База данных заблокирована. Причина: "+user.getPuserLockReason());
    			}
    			// вновь создаваемая база данных
    			boolean newdb = (user.getOwner()==null && user.getPuserDbname().isEmpty());
    			if(newdb) {
    				byte[] code = new byte[16];
    				new SecureRandom().nextBytes(code);
    				user.setPuserDbname(new BigInteger(1,code).toString(16));
    			} else {
    				if(user.getOwner()!=null && user.getOwner().getPuserDbname().isEmpty())
        				throw new Exception("База данных еще не подготовлена. Обратитесь к лицу, которому подчиняетесь");
    				if(user.getOwner()!=null)
    					user.setPuserDbname(user.getOwner().getPuserDbname());
    			}
    			String host = getServletConfig().getInitParameter("dataServer");
    			if(host==null)
    				throw new Exception("Неверная кофигурация сервера. dataServer not found. ");
    			String redirectUrl = host+"#session="+getThreadLocalRequest().getSession().getId();
    			tx.commit();
    			return redirectUrl;
    		} catch (Exception e) {
    			if(tx.isActive()) {
    				tx.rollback();
        			Ipplan.error(e);
    			}
    			throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public PUserWrapper isAccessDatabase(String sessionId) {
		PUser user = IpplanSessionListener.getAuthUserBySession(sessionId);
		return (user==null)?null:user.toClient();
	}

	private void calcLockFlag(PUser user)  throws Exception {
		//TODO проверка условий предоставления доступа (оплата и т.д.)
	}

/*	
	private boolean isAccessDatabase(String dbName, String userEmail) {
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		SQLQuery q = session.createSQLQuery("select u.* "+
    					"from PUSER u left outer join PUSER own on u.owner_puser_id = own.puser_id "+
    					"where (u.PUSER_EMAIL=:login and u.PUSER_LOCK=0) AND "+
    					"((own.puser_id is null AND u.puser_dbname =:db) OR (own.puser_id is not null AND own.puser_dbname =:db AND own.PUSER_LOCK=0))");
			q.setString("login", userEmail);
			q.setString("db", dbName);
    		return q.uniqueResult()!=null;
    	} finally {
    		session.close();
    	}
	}
*/

}
