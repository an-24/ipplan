package com.cantor.ipplan.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet  implements LoginService {

	@Override
	public PUser login(String nameOrEmail, String pswd) {
		
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		//Transaction tx = session.beginTransaction();
    		try {
    			Query q = session.createQuery("select u from PUser u "+
    		                                  "where (u.puserLogin=:login OR u.puserEmail=:login)AND u.puserPswd=:pswd");
    			q.setString("login", nameOrEmail);
    			q.setString("pswd", hashPassword(pswd));
    			List l = q.list();
    			if(l.size()==0) return null; else
    			{
    				PUser u = (PUser) l.get(0);
    				HttpSession sess = this.getThreadLocalRequest().getSession();
    				sess.setAttribute("user", u);
    				return u;
    			}	
    			//tx.commit();
    		} catch (Exception e) {
    			//tx.rollback();
    			Ipplan.error("Ошибка входа в систему пользователя "+nameOrEmail,e);
			}
    	} finally {
    		session.close();
    	}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PUser isLogged() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		if (sess.isNew()) return null;
		return (PUser) sess.getAttribute("user");
	}
	
	private String hashPassword(String pswd) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(pswd.getBytes(), 0, pswd.length());
		
		pswd = new BigInteger(1, digest.digest()).toString(16);
		Ipplan.info(pswd);
		return pswd;
	}

}
