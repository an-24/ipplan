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
import com.cantor.ipplan.db.up.PUser;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet  implements LoginService {

	@Override
	public PUserWrapper login(String nameOrEmail, String pswd) {
		
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
    			if(l.size()==0) return null; else
    			{
    				PUser u = l.get(0);
    				// fetch lazy
    				u.fetch(true);
    				
    				HttpSession sess = this.getThreadLocalRequest().getSession();
    				sess.setAttribute("user", u);
    				
    				PUserWrapper uclient = u.toClient(); 
    				
    				// модмфицируем lastaccess
    				u.setPuserLastaccess(new Date());
    				session.update(u);
        			tx.commit();
    				
    				return uclient;
    			}	
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
	public PUserWrapper isLogged() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		if (sess.isNew()) return null;
		PUser u = (PUser) sess.getAttribute("user");
		return u==null?null:u.toClient();
	}
	
	private String hashPassword(String pswd) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(pswd.getBytes(), 0, pswd.length());
		pswd = new BigInteger(1, digest.digest()).toString(16);
		return pswd;
	}

	@Override
	public void logout() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUser u = (PUser) sess.getAttribute("user");
		if(u!=null) sess.removeAttribute("user");
	}

}
