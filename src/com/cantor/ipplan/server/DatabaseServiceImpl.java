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

import java.io.File;
import java.text.MessageFormat;

import javax.servlet.http.HttpSession;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.BackupManager;
import org.firebirdsql.management.FBBackupManager;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.cantor.ipplan.client.DatabaseService;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.PUserWrapper;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

	private boolean newDBFlag = false;
	
	/**
	 *  После удачного open в сессии два атрибута
	 *  (PUserWrapper) loginUser - пользователь в профиле
	 *  (int) userId - идентификатор пользователя в персональнойБД
	 *  @param sessId - идентификатор сессии на UP сервере
	 */
	@Override
	public PUserWrapper open(String sessId) throws Exception {
		PUserWrapper u = checkAccess(sessId);
		String url = openOrCreateStore(u.puserDbname,u.puserEmail);
		createSessionFactory(url);
		int userId = makeUser(u.puserEmail);
		HttpSession sess = this.getThreadLocalRequest().getSession();
		sess.setAttribute("userId", userId);
		return u;
	}


	private PUserWrapper checkAccess(String sessId) throws Exception {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUserWrapper u = (PUserWrapper) sess.getAttribute("loginUser");
		if (sess.isNew() || u==null ) {
			// проводим проверку через сервер UP
			String host = getServletConfig().getInitParameter("loginCallBack");
			LoginService login = (LoginService) SyncProxy.newProxyInstance(LoginService.class, host,"login");
			u = login.isAccessDatabase(sessId);
			if(u==null)
				throw new Exception("Доступ к базе данных запрещен");
			sess.setAttribute("loginUser",u);
		};
		return u;
	}

	private synchronized String openOrCreateStore(String name, String userEmail) throws Exception {
		String dir = getServletConfig().getInitParameter("storeLocation");
		if(dir==null)
			throw new Exception("Неверная кофигурация сервера. storeLocation not found. ");
		File root = new File(dir);
		if(!root.exists() || !root.isDirectory())
			throw new Exception(MessageFormat.format("Неверная кофигурация сервера. Directory {0} not found.",new Object[]{dir}));
		
		File userroot = new File(root,name);
		boolean newCatalog = !userroot.exists();
		
		String dbName;
		if(newCatalog) {
			userroot.mkdir();
			dbName = createDatabase(root,name);
			newDBFlag  = true;
		} else {
			dbName = getCurrentDBName(root, name);
		}
		return "jdbc:firebirdsql:localhost:"+dbName;
	}

	private String createDatabase(File root, String name) throws Exception {
		try {
			String dbname = getCurrentDBName(root, name);
			BackupManager restoreManager = new FBBackupManager(GDSType.getType("PURE_JAVA"));
			restoreManager.setHost("localhost");
			restoreManager.setPort(3050);
			restoreManager.setUser("SYSDBA");
			restoreManager.setPassword("masterkey");
			restoreManager.setLogger(System.out);
			restoreManager.setVerbose(true);
			restoreManager.setDatabase(dbname);
			restoreManager.setBackupPath(root.getAbsolutePath()+File.separatorChar+"pattern.fbk");
			restoreManager.restoreDatabase();
			return dbname;
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	private void createSessionFactory(String url) throws Exception {
		SessionFactory sessionFactory = (SessionFactory) this.getThreadLocalRequest().getSession().getAttribute("sessionFactory");
		if(sessionFactory==null) {
			// конфигурируем hibername
	    	Configuration cfg = new Configuration().configure();
	    	cfg.setProperty(Environment.CONNECTION_PROVIDER, "com.cantor.ipplan.server.UserDataPoolConnection");
	    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
	    	UserDataPoolConnection pool = (UserDataPoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
	    	pool.setPool(url);
	    	sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	    	// устанавливаем в сессии
	    	this.getThreadLocalRequest().getSession().setAttribute("sessionFactory", sessionFactory);
		}
	}

	
	private int makeUser(String userEmail) throws Exception {
		SessionFactory sessionFactory = (SessionFactory) this.getThreadLocalRequest().getSession().getAttribute("sessionFactory");
		// проверка наличия пользователя
    	Session session = sessionFactory.openSession();
    	try {
    		PUserIdent user;
    		Query q = session.createQuery("select u from PUserIdent u where u.puserLogin=:login");
			q.setString("login", userEmail);
			user =  (PUserIdent) q.uniqueResult();
    		if(user == null) {
    			// добавим
    			Transaction tx = session.beginTransaction();
    			try {
    				user = new PUserIdent();
    				user.setPuserLogin(userEmail);
    				if(newDBFlag) user.setPuserId(PUserIdent.USER_ROOT_ID); else {
    					PUserIdent own = (PUserIdent) session.get(PUserIdent.class, PUserIdent.USER_ROOT_ID);
    					if(own==null) user.setPuserId(PUserIdent.USER_ROOT_ID); else user.setOwner(own);
    				}
    				session.save(user);
    				tx.commit();
    			} catch (Exception e) {
    				tx.rollback();
    				throw e;
				}
    		}
    		return user.getId();
    	} finally {
    		session.close();
    	}
	}

	private String getCurrentDBName(File root, String name) {
		return root.getAbsolutePath()+File.separatorChar+name+File.separatorChar+"current.fdb";
	}

}
