package com.cantor.ipplan.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.db.up.Messages;
import com.cantor.ipplan.db.up.PUser;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet  implements LoginService {

	@Override
	public PUserWrapper login(String nameOrEmail, String pswd, String device) {
		if(nameOrEmail==null || nameOrEmail.isEmpty()) {
			Ipplan.error("Ошибка входа в систему пользователя: имя пустое");
			return null;
		}
		
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		Transaction tx = session.beginTransaction();
    		try {
    			Query q = session.createQuery("select u from PUser u "+
    		                                  "where (u.puserLogin=:login OR u.puserEmail=:login)AND u.puserPswd=:pswd");
    			q.setString("login", nameOrEmail);
    			q.setString("pswd", hashPassword(pswd));
    			List<PUser> l = q.list();
    			if(l.size()>0) {
    				PUser u = l.get(0);
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
    				session.update(u);
    				
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
				PUser u1 = (PUser) session.merge(u);
				// добавим недостающие данные
				PUserWrapper uclient = u1.toClient();
				setDataClient(session,u1,uclient);
				
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
			uclient.children.add(ruser);
		}
		// ищем последнее системное сообщение направленное этому пользователю
		Messages m = Messages.getLastMessageTo(session, user, Messages.MT_JOIN_TO_OWNER);
		uclient.lastSystemMessage = m!=null?m.toClient():null;
		
	}

}
