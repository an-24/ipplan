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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.BackupManager;
import org.firebirdsql.management.FBBackupManager;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.transform.Transformers;

import com.cantor.ipplan.client.DatabaseService;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.core.IdGenerator;
import com.cantor.ipplan.db.ud.Bargain;
import com.cantor.ipplan.db.ud.Costs;
import com.cantor.ipplan.db.ud.Customer;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.db.ud.Status;
import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

	private boolean newDBFlag = false;
	
	
	public DatabaseServiceImpl() {
		super();
	}
	
	//TODO при смене email нужно синхронизировать данные по id пользователя
	/**
	 *  После удачного open в сессии два атрибута
	 *  (PUserWrapper) loginUser - пользователь в профиле
	 *  (int) userId - идентификатор пользователя в персональнойБД
	 *  @param sessId - идентификатор сессии на UP сервере
	 */
	@Override
	public PUserWrapper open(String sessId) throws Exception {
		PUserWrapper u = checkAccess(sessId);
		SessionFactory sessionFactory = getSessionFactory();
		if(sessionFactory==null) {
			String url = openOrCreateStore(u.puserDbname,u.puserEmail);
			createSessionFactory(url);
			int userId = makeUser(u);
			HttpSession sess = this.getThreadLocalRequest().getSession();
			sess.setAttribute("userId", userId);
		}
		return u;
	}

	@Override
	public PUserWrapper isLogged() {
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory!=null?getLoginUser():null;
	}

	@Override
	public List<BargainWrapper> attention() throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
		List<BargainWrapper> list = new ArrayList<BargainWrapper>();
    	Session session = sessionFactory.openSession();
    	try {
    		String hsq = "select b from Bargain b where b.bargainHead=1";
    		int usrid = getUserId();
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			hsq+=" and b.puser.puserId="+usrid;
    		// со статусом = 
    		hsq+="and (";
    		// все Приостановленные
    		hsq+=" (b.status.statusId="+StatusWrapper.SUSPENDED+")";
    		// за N дней, до финиша, если Выполнение
    		hsq+=" or (b.status.statusId= "+StatusWrapper.EXECUTION+" and b.bargainFinish-current_timestamp<b.status.statusDayLimit)";
    		// за N дней, до финиша, если Выполнение
    		hsq+=" or (b.status.statusId= "+StatusWrapper.COMPLETION+" and b.bargainFinish-current_timestamp<b.status.statusDayLimit)";
    		hsq+=")\n";
    		// самые близкие по плану
    		hsq+=" order by b.bargainFinish, b.bargainRevenue desc";
    		
    		Query q = session.createQuery(hsq);
    		q.setMaxResults(5);
    		List<Bargain> bargains = q.list();
    		for (Bargain b : bargains) {
    			BargainWrapper wrap = b.toClient();
    			wrap.attention =  b.makeAttention();
    			list.add(wrap);
    		}
    		return list;
    		
    	} finally {
    		session.close();
    	}
	}

	@Override
	public BargainTotals[] getTotals() throws Exception {
		checkAccess();
		BargainTotals b,bold;
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
    		Query q;
    		String sql;
    		// запрашиваем HEAD данные
    		sql = 
    		  	  "select count(b.bargain_id) \"count\",sum(b.bargain_revenue) \"revenue\",sum(b.bargain_prepayment) \"prepayment\","+
    	          "sum(b.bargain_costs) \"costs\", sum(b.bargain_payment_costs) \"paymentCosts\", sum(b.bargain_fine) \"fine\", sum(b.bargain_tax) \"tax\" "+
    			  "from bargain b "+
    			  "where b.bargain_head=1 AND "+
    			  "b.status_id in ("+StatusWrapper.EXECUTION+','+StatusWrapper.COMPLETION+','+StatusWrapper.SUSPENDED+')';
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			sql+=" AND b.puser_id="+usrid;
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
    		b = (BargainTotals) q.uniqueResult();
    		// запрашиваем начальные данные
    		sql = 
      		  	  "select count(b.bargain_id) \"count\", sum(b.bargain_revenue) \"revenue\",sum(b.bargain_prepayment) \"prepayment\","+
      	          "sum(b.bargain_costs) \"costs\", sum(b.bargain_payment_costs) \"paymentCosts\", sum(b.bargain_fine) \"fine\", sum(b.bargain_tax) \"tax\" "+
      			  "from bargain b "+
      			  "where b.bargain_id=b.root_bargain_id AND "+
      			  "b.status_id in ("+StatusWrapper.EXECUTION+','+StatusWrapper.COMPLETION+','+StatusWrapper.SUSPENDED+')';
      		if(usrid != PUserIdent.USER_ROOT_ID)
      			sql+=" AND b.puser_id="+usrid;
      		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
    		bold = (BargainTotals) q.uniqueResult();
    		
    		return new BargainTotals[]{b,bold};
    	} finally {
    		session.close();
    	}
	}

	@Override
	public BargainWrapper newBargain(String name, int status) throws Exception {
		checkAccess();
		Bargain b = newEmptyBargain(name,status);
		putTempBargain(b);
		BargainWrapper bw = b.toClient();
		return bw;
	}

	@Override
	public BargainWrapper newBargain(String name, int startStatus, Date start,
			Date finish) throws Exception {
		checkAccess();
		Bargain b = newEmptyBargain(name,startStatus);
		b.setBargainStart(start);
		b.setBargainFinish(finish);
		putTempBargain(b);
		BargainWrapper bw = b.toClient();
		bw.attention = b.makeAttention();
		return bw;
	}

	@Override
	public List<BargainWrapper> getTemporalyBargains() throws Exception {
		checkAccess();
		HashMap<Integer, Bargain> bl = getTempBargains();
		List<BargainWrapper> bwl = new ArrayList<BargainWrapper>();
		for (Bargain b : bl.values()) {
			BargainWrapper bw = b.toClient();
			bw.attention = b.makeAttention();
			bwl.add(bw);
		}
		return bwl;
	}
	@Override
	public BargainWrapper editBargain(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		Bargain b = (Bargain) session.load(Bargain.class, id);
    		putTempBargain(b);
    		BargainWrapper bw = b.toClient();
    		bw.attention = b.makeAttention();
    		return bw;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public boolean deleteBargain(int id) throws Exception {
		checkAccess();
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dropTemporalyBargain(int id) {
		HashMap<Integer, Bargain> bl =  getTempBargains();
		bl.remove(id);
	}

	@Override
	public void saveTemporalyBargain(BargainWrapper bw) throws Exception {
		checkAccess();
		Bargain b = new Bargain();
		b.fromClient(bw);
		putTempBargain(b);
	}

	@Override
	public BargainWrapper saveBargain(BargainWrapper bargain, boolean drop)
			throws Exception {
		checkAccess();
		
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Bargain b = (Bargain) session.get(Bargain.class, bargain.bargainId);
				boolean isnew = false;
				if(b==null) {
					// новая запись
					b = new Bargain();
					// на всякий случай перепишем, 
					// мало ли что придет с клиента
					bargain.bargainVer = 0;
					bargain.bargainHead = 1;
					bargain.bargainCreated = new Date();
					bargain.puser = getLoginUser();
					isnew = true;
				} else
					session.update(b);
					
				b.fromClient(bargain);
				if(isnew) {
					// root - он же
					b.setRootBargain(b); 
					session.save(b);
				};

				// если не сбрасываем, то читаем вновь добавленный объект, тчобы возвратить 
				if(!drop) {
					bargain = b.toClient();
					bargain.attention = b.makeAttention();
				}	
				
				b.saveCompleted();
				bargain.saveCompleted();
				
				tx.commit();
				if(drop) dropTemporalyBargain(bargain.bargainId); else
					putTempBargain(b);
				// возвратим вновь добавленный объект 
				if(!drop) return bargain; else return null;
				
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
		
	}

	@Override
	public List<CustomerWrapper> findCustomer(String query) {
		List<CustomerWrapper> list = new ArrayList<CustomerWrapper>();
		if(isLogged()==null) return list;
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
      		String sql = "select C.customer_id \"CustomerId\", C.customer_name \"CustomerName\"," +
      				     "       C.customer_lookup_key \"CustomerLookupKey\" from customer C where ";
      		sql+="UPPER(C.customer_name) like :q";
			Query q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(Customer.class));
			q.setParameter("q", "%"+query.toUpperCase()+"%");
			List<Customer> lc = q.list();
			for (Customer customer : lc) 
				list.add(customer.toClient());
    		return list;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<CostsWrapper> findCost(String query) {
		List<CostsWrapper> list = new ArrayList<CostsWrapper>();
		if(isLogged()==null) return list;
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
      		String sql = "select C.costs_id \"CostsId\", C.costs_name \"CostsName\"," +
      				     "       C.costs_sortcode \"CostsSortcode\" from costs C where ";
      		sql+="UPPER(C.costs_name) like :q";
      		sql+=" order by C.costs_sortcode";
			Query q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(Costs.class));
			q.setParameter("q", "%"+query.toUpperCase()+"%");
			List<Costs> lc = q.list();
			for (Costs c : lc) 
				list.add(c.toClient());
    		return list;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<StatusWrapper> getAllStatuses() {
		List<StatusWrapper> list = new ArrayList<StatusWrapper>();
		if(isLogged()==null) return list;
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Query q = session.createQuery("from Status");
			List<Status> lc = q.list();
			for (Status st : lc) 
				list.add(st.toClient());
    		return list;
    	} finally {
    		session.close();
    	}
	}
	
	private Bargain newEmptyBargain(String name, int status) {
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		Bargain b = new Bargain();
    		b.setBargainCreated(new Date());
    		b.setBargainName(name);
    		b.setBargainHead(1);
    		b.setPuser(new PUserIdent(getLoginUser()));
    		Status st = (Status) session.load(Status.class,status);
    		st.fetch(true);
    		b.setStatus(st);
    		b.setBargainId(IdGenerator.generatorId(sessionFactory,session));
    		b.fetch(true);
    		b.setNew(true);
    		b.setDirty(true);
    		return b;
    	} finally {
    		session.close();
    	}
	}

	private void putTempBargain(Bargain b) {
		HashMap<Integer, Bargain> bl =  getTempBargains();
		bl.put(b.getId(),b);
	}

	private HashMap<Integer, Bargain> getTempBargains() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		HashMap<Integer, Bargain> bl =  (HashMap) sess.getAttribute("tmp_bargain_list");
		if(bl==null) {
			bl = new HashMap<Integer, Bargain>();
			sess.setAttribute("tmp_bargain_list", bl);
		}
		return bl;
	}

	private PUserWrapper checkAccess(String sessId) throws Exception {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		PUserWrapper u = getLoginUser();
		if (sess.isNew() || u==null ) {
			// проводим проверку через сервер UP
			String host = getServletConfig().getInitParameter("loginCallBack");
			LoginService login = (LoginService) SyncProxy.newProxyInstance(LoginService.class, host,"login");
			u = login.isAccessDatabase(sessId);
			if(u==null)
				throw new Exception("Доступ к базе данных запрещен");
			setLoginUser(u);
		};
		return u;
	}
	
	private void checkAccess() throws Exception {
		if(isLogged()==null)
			throw new Exception("Доступ запрещен");
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
		SessionFactory sessionFactory = getSessionFactory();
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


	private SessionFactory getSessionFactory() {
		return (SessionFactory) this.getThreadLocalRequest().getSession().getAttribute("sessionFactory");
	}
	
	private PUserWrapper getLoginUser() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		return (PUserWrapper) sess.getAttribute("loginUser");
	}

	private void setLoginUser(PUserWrapper u) {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		sess.setAttribute("loginUser",u);
	}
	
	private int makeUser(PUserWrapper u) throws Exception {
		SessionFactory sessionFactory = getSessionFactory();
		// проверка наличия пользователя
    	Session session = sessionFactory.openSession();
    	try {
    		PUserIdent user;
    		Query q = session.createQuery("select u from PUserIdent u where u.puserLogin=:login");
			q.setString("login", u.puserEmail);
			user =  (PUserIdent) q.uniqueResult();
    		if(user == null) {
    			// добавим
    			Transaction tx = session.beginTransaction();
    			try {
    				user = new PUserIdent();
    				user.setPuserLogin(u.puserEmail);
    				user.setPuserTaxtype(u.puserTaxtype);
    				if(newDBFlag) user.setPuserId(PUserIdent.USER_ROOT_ID); else {
    					PUserIdent own = (PUserIdent) session.get(PUserIdent.class, PUserIdent.USER_ROOT_ID);
    					if(own==null) user.setPuserId(PUserIdent.USER_ROOT_ID); else user.setOwner(own);
    				}
    				session.save(user);
    				// добавим статусы: только для root
    				if(user.getPuserId()==PUserIdent.USER_ROOT_ID)
    					makeDefaultStatuses(session,user);
    				
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

	private void makeDefaultStatuses(Session session, PUserIdent user) {
		Status s;
		s = new Status(StatusWrapper.PRIMARY_CONTACT,user,"Первичный контакт",0);
		session.save(s);
		s = new Status(StatusWrapper.TALK,user,"Переговоры",0);
		session.save(s);
		s = new Status(StatusWrapper.DECISION_MAKING,user,"Принимают решение",0);
		session.save(s);
		s = new Status(StatusWrapper.RECONCILIATION_AGREEMENT,user,"Согласование договора",0);
		session.save(s);
		s = new Status(StatusWrapper.EXECUTION,user,"Исполнение",Bargain.EXECUTE_WARNING_DURATION_LIMIT);
		session.save(s);
		s = new Status(StatusWrapper.SUSPENDED,user,"Приостановлена",0);
		session.save(s);
		s = new Status(StatusWrapper.COMPLETION,user,"Завершение",Bargain.COMPLETION_WARNING_DURATION_LIMIT);
		session.save(s);
		s = new Status(StatusWrapper.CLOSE_OK,user,"Закрыта успешно",0);
		session.save(s);
		s = new Status(StatusWrapper.CLOSE_FAIL,user,"Закрыта без результата",0);
		session.save(s);
		
	}

	private String getCurrentDBName(File root, String name) {
		return root.getAbsolutePath()+File.separatorChar+name+File.separatorChar+"current.fdb";
	}

	private int getUserId() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		return (Integer) sess.getAttribute("userId");
	}
	
	public PUserIdent getUser() throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
			PUserIdent user = (PUserIdent) session.load(PUserIdent.class,usrid);
			return user;
    	} finally {
    		session.close();
    	}
	}
	public void updateGoogleLastSync() throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				PUserIdent user = getUser();
				user.setPuserContactLastsync(new Date());
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    		
    	} finally {
    		session.close();
    	}
	}	

	public void saveToken(OAuthToken token) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
			Transaction tx = session.beginTransaction();
			try {
				PUserIdent user = (PUserIdent) session.load(PUserIdent.class,usrid);
				user.setPuserGoogleToken(token.getValue());
				user.setPuserGoogleExpiresIn(token.getExpiresIn());
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    		
    	} finally {
    		session.close();
    	}
	}

	public OAuthToken getToken() throws Exception {
		PUserIdent user = getUser();
		return new OAuthToken(user.getPuserGoogleToken(),user.getPuserGoogleExpiresIn());
	}
	
	@Override
	public ImportProcessInfo syncContacts() throws Exception {
		ContactsImport importer = new ContactsImport("kav@gelicon.biz", "327-894-234-789");
		List<ContactEntry> entrys = importer.getAllEntrys();
		for (ContactEntry entry :entrys) {
			if (entry.hasName()) {
			      Name name = entry.getName();
			      if (name.hasFullName()) {
			        String fullNameToDisplay = name.getFullName().getValue();
			        if (name.getFullName().hasYomi()) {
			          fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
			        }
			      System.out.println("\\\t\\\t" + fullNameToDisplay);
			      } else {
			        System.out.println("\\\t\\\t (no full name found)");
			      }
			      if (name.hasNamePrefix()) {
			        System.out.println("\\\t\\\t" + name.getNamePrefix().getValue());
			      } else {
			        System.out.println("\\\t\\\t (no name prefix found)");
			      }
			      if (name.hasGivenName()) {
			        String givenNameToDisplay = name.getGivenName().getValue();
			        if (name.getGivenName().hasYomi()) {
			          givenNameToDisplay += " (" + name.getGivenName().getYomi() + ")";
			        }
			        System.out.println("\\\t\\\t" + givenNameToDisplay);
			      } else {
			        System.out.println("\\\t\\\t (no given name found)");
			      }
			      if (name.hasAdditionalName()) {
			        String additionalNameToDisplay = name.getAdditionalName().getValue();
			        if (name.getAdditionalName().hasYomi()) {
			          additionalNameToDisplay += " (" + name.getAdditionalName().getYomi() + ")";
			        }
			        System.out.println("\\\t\\\t" + additionalNameToDisplay);
			      } else {
			        System.out.println("\\\t\\\t (no additional name found)");
			      }
			      if (name.hasFamilyName()) {
			        String familyNameToDisplay = name.getFamilyName().getValue();
			        if (name.getFamilyName().hasYomi()) {
			          familyNameToDisplay += " (" + name.getFamilyName().getYomi() + ")";
			        }
			        System.out.println("\\\t\\\t" + familyNameToDisplay);
			      } else {
			        System.out.println("\\\t\\\t (no family name found)");
			      }
			      if (name.hasNameSuffix()) {
			        System.out.println("\\\t\\\t" + name.getNameSuffix().getValue());
			      } else {
			        System.out.println("\\\t\\\t (no name suffix found)");
			      }
			} else {
			      System.out.println("\t (no name found)");
			}
			if(entry.hasOccupation()) {
			    System.out.println("Occupation: "+entry.getOccupation().getValue());
			} else  {
			      System.out.println("(no occupation)");
			}
			System.out.println("Organizations:");
			if(entry.hasOrganizations()) {
				for (Organization o : entry.getOrganizations()) {
				    System.out.println("\t"+o);
				    System.out.println("\t name: "+o.getOrgName());
				    System.out.println("\t departament: "+o.getOrgDepartment());
				    System.out.println("\t position: "+o.getOrgTitle());
				    if(o.hasWhere()) {
					    System.out.println("\t place: "+o.getWhere().getValueString());
				    }
					
				}
			} else  {
			      System.out.println("\t (no organization)");
			}
			
			System.out.println("Email addresses:");
			for (Email email : entry.getEmailAddresses()) {
			    System.out.print(" " + email.getAddress());
			    if (email.getRel() != null) {
			        System.out.print(" rel:" + email.getRel());
			    }
			    if (email.getLabel() != null) {
			        System.out.print(" label:" + email.getLabel());
			    }
			    if (email.getPrimary()) {
			        System.out.print(" (primary) ");
			    }
			    System.out.print("\n");
			};
			System.out.println("IM addresses:");
		    for (Im im : entry.getImAddresses()) {
		    	System.out.print(" " + im.getAddress());
		    	if (im.getLabel() != null) {
		    		System.out.print(" label:" + im.getLabel());
		    	}
		    	if (im.getRel() != null) {
		    		System.out.print(" rel:" + im.getRel());
		    	}
		    	if (im.getProtocol() != null) {
		    		System.out.print(" protocol:" + im.getProtocol());
		    	}
		    	if (im.getPrimary()) {
		    		System.out.print(" (primary) ");
		    	}
		    	System.out.print("\n");
		    }
			System.out.println("Phones:");
			for (PhoneNumber phone : entry.getPhoneNumbers()) {
			    System.out.print("  phone: " + phone.getPhoneNumber());
			    if (phone.getRel() != null) {
			        System.out.print(" rel:" + phone.getRel());
			    }
			    if (phone.getLabel() != null) {
			        System.out.print(" label:" + phone.getLabel());
			    }
			    if (phone.getPrimary()) {
			        System.out.print(" (primary) ");
			    }
			    System.out.print("\n");
			};
		    
		    		    
			System.out.println("Groups:");
			for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
				String groupHref = group.getHref();
			    System.out.println("  Id: " + groupHref);
			};
			System.out.println("Extended Properties:");
			for (ExtendedProperty property : entry.getExtendedProperties()) {
				if (property.getValue() != null) {
			        System.out.println("  " + property.getName() + "(value) = " +
			            property.getValue());
			    } else if (property.getXmlBlob() != null) {
			        System.out.println("  " + property.getName() + "(xmlBlob)= " +
			            property.getXmlBlob().getBlob());
			    }
			}
			Link photoLink = entry.getContactPhotoLink();
			String photoLinkHref = photoLink.getHref();
			System.out.println("Photo Link: " + photoLinkHref);
			if (photoLink.getEtag() != null) {
			      System.out.println("Contact Photo's ETag: " + photoLink.getEtag());
			}
			System.out.println("Contact's ETag: " + entry.getEtag());
		}
		return new ImportProcessInfo(entrys.size(),0);
	}










}
