/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cantor.ipplan.server;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.ProfileService;
import com.cantor.ipplan.db.up.Messages;
import com.cantor.ipplan.db.up.PUser;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ProfileServiceImpl extends RemoteServiceServlet implements ProfileService {

	@Override
	public void setUserData(PUserWrapper data,int joinAction) throws Exception {
		PUser user = checkLogin();
		
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		Transaction tx = session.beginTransaction();
    		try {
    			user.setPuserLogin(data.puserLogin);
    			user.setPuserEmail(data.puserEmail);
    			user.setPuserTarif(data.puserTarif);
    			// устанавливаем Босс-аккаунт
    			if(data.puserBoss!=0 && user.getPuserTarif()<=0)
    				throw new Exception("Профиль не может быть изменен, так как для включения "+
    			                        "опции Босс-аккаунт требуется другой тарифный план (начиная со Стандартного)."); 
    			user.setPuserBoss(data.puserBoss);

    			// модификация списка подчиненых
    			
				// удаляем которых нет
    			for (PUser child : user.getChildren()) {
    				if(!containsChildren(child,data.children)) {
    					user.getChildren().remove(child);
    					child.setOwner(null);
    					session.update(child);
    				}	
				}
    			// добавляем новых
    			for (PUserWrapper u: data.children) 
    			if (!u.tempflag) {
    				PUser pu = getUserByEmail(session,u.puserEmail);
    				sendMessageToUser(session,user,pu,"Подтвердите Вашу готовность к тому, "+
    								  "чтобы стать подчиненным пользователю "+user.getFullName(),Messages.MT_JOIN_TO_OWNER);
				}
    			// установление подчиненности
    			if(joinAction!=0) {
    				if(joinAction==1) {
    					PUser own = (PUser) session.load(PUser.class, data.lastSystemMessage.sender.puserId);
    					user.setOwner(own);
    				}
    				// удаление сообщения
    				deleteMessage(session,data.lastSystemMessage.messageId);
    			}
    			
    			session.update(user);
    			tx.commit();
    		} catch (Exception e) {
    			tx.rollback();
    			Ipplan.error(e);
    			throw new Exception("Ошибка изменения профиля пользователя "+user.getFullName()+": "+e.getMessage());
			}
    	} finally {
    		session.close();
    	}
	}


	@Override
	public boolean checkUser(String name, String email) throws Exception {
		checkLogin();
		
		SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
    	Session session = sessionFactory.openSession();
    	try {
    		try {
    			Query q = session.createQuery("select u from PUser u "+
    		                                  "where u.puserLogin=:login AND u.puserEmail=:email");
    			q.setString("login", name);
    			q.setString("email", email);
    			PUser u = (PUser) q.uniqueResult();
    			return u!=null;
    		} catch (Exception e) {
    			Ipplan.error(e);
			}
    	} finally {
    		session.close();
    	}
		return false;
	}

	private void deleteMessage(Session session, int messageId) {
		Query q = session.createQuery("delete from Messages where messagesId=:messagesId");
		q.setInteger("messagesId", messageId);
		q.executeUpdate();
	}
	
	private PUser checkLogin() throws Exception {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUser u = (PUser) sess.getAttribute("user");
		if (sess.isNew() || u==null ) 
			throw new Exception("Необходимо войти в систему");
		return u;
	}

	private boolean containsChildren(PUser child, Set<PUserWrapper> children) {
		for (PUserWrapper u: children) 
			if(u.puserEmail.equalsIgnoreCase(child.getPuserEmail())) return true;
		return false;
	}

	private void sendMessageToUser(Session session, PUser sender, PUser reciever, String txt, int type) {
		Messages m = new Messages();
		m.setMessagesDate(new Date());
		m.setPuserByPuserSId(sender);
		m.setPuserByPuserRId(reciever);
		m.setMessagesText(txt);
		m.setMessagesType(type);
		session.save(m);
	}

	private PUser getUserByEmail(Session session, String email) {
		Query q = session.createQuery("select u from PUser u "+
                "where u.puserEmail=:email");
		q.setString("email", email);
		return (PUser) q.uniqueResult();
	}
}
