package com.cantor.ipplan.server;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import org.hibernate.service.ServiceRegistry;
import org.hibernate.transform.Transformers;

import com.cantor.ipplan.client.DatabaseService;
import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.core.IdGenerator;
import com.cantor.ipplan.core.Utils;
import com.cantor.ipplan.db.ud.Bargain;
import com.cantor.ipplan.db.ud.Calendar;
import com.cantor.ipplan.db.ud.Costs;
import com.cantor.ipplan.db.ud.Customer;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.db.ud.Status;
import com.cantor.ipplan.db.ud.Task;
import com.cantor.ipplan.db.ud.Tasktype;
import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.TaskWrapper;
import com.cantor.ipplan.shared.TasktypeWrapper;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.OrgTitle;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

	private boolean newDBFlag = false;
	private HttpSession session =  null;
	
	public DatabaseServiceImpl() {
		super();
	}
	
	public DatabaseServiceImpl(HttpSession session) {
		this();
		this.session = session;
	}
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
    	// старт задач по расписанию
		// в потоке, чтобы не мешало сервлету
    	try {
    		new Thread() {
    			public void run() {
    				UserTask.startAll(config.getServletContext());
    			}
    		}.start();
    		
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	private HttpSession getSession() {
		return session==null?getThreadLocalRequest().getSession():session;
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
		PUserWrapper uw = checkAccess(sessId);
		setLoginUser(uw);
		
		SessionFactory sessionFactory = getSessionFactory();
		if(sessionFactory==null) {
			String url = openOrCreateStore(uw.puserDbname,uw.puserEmail);
			createSessionFactory(url);
			PUserIdent user = makeUser(uw);
			HttpSession sess = this.getSession();
			sess.setAttribute("userId", user.getId());
			// с сервера авторизации приходит
			// не полностью заполненный профиль пользователя
			// нужно слить
			mergeUserData(uw,user);
		}
		
		return uw;
	}

	private void mergeUserData(PUserWrapper target, PUserIdent source) {
		target.puserContactSyncDuration = source.getPuserContactSyncDuration();
		target.puserCalendarSyncDuration = source.getPuserCalendarSyncDuration();
		//.. и т.д.
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
    	Session session = sessionFactory.openSession();
		List<BargainWrapper> list = new ArrayList<BargainWrapper>();
    	try {
    		String hsq = "select b from Bargain b where b.bargainHead=1 and b.bargainVisible=1";
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

	@SuppressWarnings("deprecation")
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
    			  "where b.bargain_head=1 AND b.bargain_visible=1 AND EXTRACT(YEAR from bargain_start)=:year AND "+
    			  "b.status_id in ("+StatusWrapper.EXECUTION+','+StatusWrapper.COMPLETION+','+StatusWrapper.SUSPENDED+')';
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			sql+=" AND b.puser_id="+usrid;
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
      		q.setParameter("year", 1900+new Date().getYear());
    		b = (BargainTotals) q.uniqueResult();
    		// запрашиваем начальные данные
    		sql = 
      		  	  "select count(b.bargain_id) \"count\", sum(b.bargain_revenue) \"revenue\",sum(b.bargain_prepayment) \"prepayment\","+
      	          "sum(b.bargain_costs) \"costs\", sum(b.bargain_payment_costs) \"paymentCosts\", sum(b.bargain_fine) \"fine\", sum(b.bargain_tax) \"tax\" "+
      			  "from bargain b "+
      			  "where b.bargain_id=b.root_bargain_id AND b.bargain_visible=1 AND EXTRACT(YEAR from bargain_start)=:year AND "+
      			  " exists (select * from bargain b1 where b1.root_bargain_id=b.root_bargain_id AND b1.bargain_head=1 AND "+
      			  "b1.bargain_visible=1 AND b1.status_id in ("+StatusWrapper.EXECUTION+','+StatusWrapper.COMPLETION+','+StatusWrapper.SUSPENDED+"))";
      		if(usrid != PUserIdent.USER_ROOT_ID)
      			sql+=" AND b.puser_id="+usrid;
      		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
      		q.setParameter("year", 1900+new Date().getYear());
    		bold = (BargainTotals) q.uniqueResult();
    		// ход продаж
    		// по продажным статусам
    		sql = 
      		  	  "select count(b.bargain_id) \"count\",sum(b.bargain_revenue) \"revenue\",sum(b.bargain_prepayment) \"prepayment\","+
      	          "sum(b.bargain_costs) \"costs\", sum(b.bargain_payment_costs) \"paymentCosts\", sum(b.bargain_fine) \"fine\", sum(b.bargain_tax) \"tax\", "+
      	          "b.status_id \"statusId\""+
      			  "from bargain b "+
      			  "where b.bargain_head=1 AND b.bargain_visible=1 AND EXTRACT(YEAR from bargain_start)=:year AND "+
      			  "b.status_id in ("+StatusWrapper.PRIMARY_CONTACT+','+StatusWrapper.TALK+','+StatusWrapper.DECISION_MAKING+','+StatusWrapper.RECONCILIATION_AGREEMENT+") "+
      			  "group by b.status_id "+
      			  "order by b.status_id";
      		if(usrid != PUserIdent.USER_ROOT_ID)
      			sql+=" AND b.puser_id="+usrid;
      		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
        	q.setParameter("year", 1900+new Date().getYear());
        	List<BargainTotals> listSales = q.list();
        	// по статусу "Закючивших контракт"
    		sql = "select count(b.bargain_id) \"count\",sum(b.bargain_revenue) \"revenue\",sum(b.bargain_prepayment) \"prepayment\","+
        	      "sum(b.bargain_costs) \"costs\", sum(b.bargain_payment_costs) \"paymentCosts\", sum(b.bargain_fine) \"fine\", sum(b.bargain_tax) \"tax\" "+
        	      "from bargain b "+
        	      "where b.bargain_visible=1 AND b.bargain_head=1 AND EXTRACT(YEAR from bargain_start)=:year AND "+
        	      "b.status_id>"+StatusWrapper.RECONCILIATION_AGREEMENT+" AND b.status_id<>"+StatusWrapper.CLOSE_FAIL+" AND "+
        	      StatusWrapper.PRIMARY_CONTACT+"=(select min(b1.status_id) from bargain b1 where b1.root_bargain_id=b.root_bargain_id)";
      		if(usrid != PUserIdent.USER_ROOT_ID)
      			sql+=" AND b.puser_id="+usrid;
      		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(BargainTotals.class));
        	q.setParameter("year", 1900+new Date().getYear());
    		BargainTotals bSaleConfirmed = (BargainTotals) q.uniqueResult();
    		bSaleConfirmed.setStatusId(StatusWrapper.EXECUTION);
    		
        	BargainTotals[] totals = new BargainTotals[2+listSales.size()+1];
        	totals[0] = b;
        	totals[1] = bold;
        	int i = 1;
        	for (BargainTotals t : listSales) 
        		totals[++i] = t;
        	totals[totals.length-1] = bSaleConfirmed;
        	
    		return totals;
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
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			HashMap<Integer, Bargain> bl = getTempBargains();
			List<BargainWrapper> bwl = new ArrayList<BargainWrapper>();
			for (Bargain b : bl.values()) {
				BargainWrapper bw = b.toClient();
				bw.attention = b.makeAttention();
				fillTaskList(b.getBargainId(), session, bw);
				bwl.add(bw);
			}
			return bwl;
    	} finally {
    		session.close();
    	}
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
    		// получаем tasks
			fillTaskList(id, session, bw);
    		return bw;
    	} finally {
    		session.close();
    	}
	}

	private void fillTaskList(int id, Session sess, BargainWrapper bw) {
		Calendar cal = (Calendar) sess.get(Calendar.class, id);
		if(cal!=null) {
			Query q = sess.createQuery("from Task t where t.calendar.bargain.bargainId=:id order by t.taskDeadline");
			q.setParameter("id", id);
			List<Task> list = q.list();
			for (Task task : list) 
				bw.tasks.add(task.toClient());
		}
	}

	@Override
	public void dropTemporalyBargain(int id) {
		HashMap<Integer, Bargain> bl =  getTempBargains();
		Bargain removedb = bl.remove(id);
		if(removedb!=null)
			getSavedBargainChain().remove(removedb.getRootBargain().getBargainId());
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
				Bargain newverb = null;
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
				} else {
					session.update(b);
					b.fetch(true);
					// проверяем на новую версию
					newverb = new Bargain();
					newverb.fromClient(bargain);
					/*
					 * Определяем нужна ли новая версия. Условие создания новой версии:
					 * - изменения в данных сделки
					 * - root не содержится в списке ранее сохраненных
					 *   (из этого списка root-сделки удаляются в dropTemporalyBargain) 
					 */
					if(newverb.equals(b) || 
					   getSavedBargainChain().containsKey(b.getRootBargain().getBargainId())) newverb = null;
				}	
				if(newverb==null) b.fromClient(bargain);else {
					newverb.fromClient(bargain);
					resetBargainHeads(session,newverb.getRootBargain().getBargainId());
					newverb.nextVersion(getLastVersion(session,b.getRootBargain()));
				}
				if(isnew) {
					// root - он же
					b.setRootBargain(b); 
					session.save(b);
				} else {
					// генерация новой версии
					if(newverb!=null) {
						newverb.setRootBargain(b.getRootBargain());
						session.save(newverb);
					}	
				}
				// сохранение tasks
				if(bargain.tasks.size()>0) {
					int id = (newverb==null)?b.getBargainId():newverb.getBargainId();
					// почистим старые задачи
					if(!isnew && newverb==null) clearTaskBargain(session, id);
					// проеряем на наличие календаря
					Calendar cal = (Calendar) session.get(Calendar.class, id);
					if(cal==null) {
						cal = new Calendar(newverb==null?b:newverb);
						session.save(cal);
					}	
					// сохраняем список задач
					for (TaskWrapper tw : bargain.tasks) {
						Task t = new Task();
						t.fromClient(tw);
						t.setCalendar(cal); // нужно в случае, когда cal создается заново
						session.save(t);
					}
				}
				
				// если не сбрасываем, то читаем вновь добавленный объект, тчобы возвратить 
				if(!drop) {
					if(newverb==null) {
						bargain = b.toClient(); 
						bargain.attention = b.makeAttention();
					} else  {
						bargain = newverb.toClient(); 
						bargain.attention = newverb.makeAttention();
					}
	    			fillTaskList(b.getBargainId(), session, bargain);
				}	
				
				b.saveCompleted();
				if(newverb!=null) newverb.saveCompleted();
				bargain.saveCompleted();
				
				tx.commit();
				
				if(drop) dropTemporalyBargain(b.getBargainId()); else {
					if(newverb!=null) {
						dropTemporalyBargain(b.getBargainId());
						putTempBargain(newverb);
						/* добавляем в сохраненные, чтобы
						* не порождать новые версии в рамках одной сессии 
						* работы со root-сделкой
						*/ 
						Bargain r = newverb.getRootBargain();
						getSavedBargainChain().put(r.getBargainId(), r);
					} else
						putTempBargain(b);
				}
				// возвратим вновь добавленный объект
				return bargain;
				
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
		
	}

	private void clearTaskBargain(Session sess, int bargainId) {
   		String sql = "delete from task where bargain_id=:id";
   		sess.createSQLQuery(sql).
			setParameter("id", bargainId).
			executeUpdate();
	}

	private void resetBargainHeads(Session sess, int rootId) {
   		String sql = "update bargain set bargain_head=0 where root_bargain_id=:id";
   		sess.createSQLQuery(sql).
   			setParameter("id", rootId).
   			executeUpdate();
	}

	private int getLastVersion(Session session, Bargain rootBargain) {
		String sql = "select max(b.bargain_ver) from bargain b where b.root_bargain_id=:id";
		SQLQuery q = session.createSQLQuery(sql);
		q.setParameter("id", rootBargain.getBargainId());
		return (Integer) q.uniqueResult();
	}

	@Override
	public List<CustomerWrapper> findCustomer(String query) {
		List<CustomerWrapper> list = new ArrayList<CustomerWrapper>();
		if(isLogged()==null) return list;
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
      		String sql = "select C.customer_id \"CustomerId\", C.customer_name \"CustomerName\"," +
      				     "       C.customer_lookup_key \"CustomerLookupKey\","+
      				     "       C.CUSTOMER_PRIMARY_EMAIL \"CustomerPrimaryEmail\","+
      				     "       C.CUSTOMER_EMAILS \"CustomerEmails\","+
      				     "       C.CUSTOMER_PRIMARY_PHONE \"CustomerPrimaryPhone\","+
      				     "       C.CUSTOMER_PHONES \"CustomerPhones\","+
      				     "       C.CUSTOMER_COMPANY \"CustomerCompany\","+
      				     "       C.CUSTOMER_POSITION \"CustomerPosition\","+
      				     "       C.CUSTOMER_BIRTHDAY \"CustomerBirthday\","+
      				     "       C.CUSTOMER_LASTUPDATE \"CustomerLastupdate\""+
      				     " from customer C where C.CUSTOMER_VISIBLE=1 and (";
      		sql+="UPPER(C.customer_name) like :q or ";
      		sql+="UPPER(C.CUSTOMER_PRIMARY_EMAIL) like :q or ";
      		sql+="UPPER(C.CUSTOMER_EMAILS) like :q or ";
      		sql+="C.CUSTOMER_PRIMARY_PHONE like :q or ";
      		sql+="C.CUSTOMER_PHONES like :q or ";
      		sql+="UPPER(C.CUSTOMER_COMPANY) like :q or ";
      		sql+="UPPER(C.CUSTOMER_POSITION) like :q ";
      		//sql+="C.CUSTOMER_BIRTHDAY like :q";
      		sql+=")";
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
		HttpSession sess = this.getSession();
		HashMap<Integer, Bargain> bl =  (HashMap) sess.getAttribute("tmp_bargain_list");
		if(bl==null) {
			bl = new HashMap<Integer, Bargain>();
			sess.setAttribute("tmp_bargain_list", bl);
		}
		return bl;
	}
	
	private HashMap<Integer, Bargain> getSavedBargainChain() {
		HttpSession sess = this.getSession();
		HashMap<Integer, Bargain> bl =  (HashMap) sess.getAttribute("saved_bargain_list");
		if(bl==null) {
			bl = new HashMap<Integer, Bargain>();
			sess.setAttribute("saved_bargain_list", bl);
		}
		return bl;
	}

	private PUserWrapper checkAccess(String sessId) throws Exception {
		HttpSession sess = this.getSession();
		PUserWrapper u = getLoginUser();
		if (sess.isNew() || u==null ) {
			// проводим проверку через сервер UP
			String host = getServletConfig().getInitParameter("loginCallBack");
			LoginService login = (LoginService) SyncProxy.newProxyInstance(LoginService.class, host,"login");
			u = login.isAccessDatabase(sessId);
			if(u==null)
				throw new Exception("Доступ к базе данных запрещен");
		};
		return u;
	}
	
	private void checkAccess() throws Exception {
		if(isLogged()==null)
			throw new Exception("Доступ запрещен");
	}


	private synchronized String openOrCreateStore(String name, String userEmail) throws Exception {
		String dir = getServletContext().getInitParameter("storeLocation");
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
			// создаем бд из pattern.fbk
			dbName = createDatabase(root,name);
			// переносим файл конфигурации
			File cfg = new File(userroot,"config.xml");
			if(cfg.createNewFile())
				Utils.copyFile(new File(root,"config.xml"), cfg); else
				throw new Exception("Не могу создать файл config.xml");
			
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
	    	this.getSession().setAttribute("sessionFactory", sessionFactory);
		}
	}


	private SessionFactory getSessionFactory() {
		return (SessionFactory) this.getSession().getAttribute("sessionFactory");
	}
	
	private PUserWrapper getLoginUser() {
		HttpSession sess = this.getSession();
		return (PUserWrapper) sess.getAttribute("loginUser");
	}

	private void setLoginUser(PUserWrapper u) {
		HttpSession sess = this.getSession();
		sess.setAttribute("loginUser",u);
	}
	
	private PUserIdent makeUser(PUserWrapper u) throws Exception {
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
    		return user;
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
		HttpSession sess = this.getSession();
		return (Integer) sess.getAttribute("userId");
	}
	
	private PUserIdent getUser() throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
			PUserIdent user = (PUserIdent) session.load(PUserIdent.class,usrid);
			user.fetch(true);
			return user;
    	} finally {
    		session.close();
    	}
	}
	
	private void updateGoogleLastSync() throws Exception {
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
				user.setPuserGoogleToken(token==null?null:token.getValue());
				user.setPuserGoogleRefreshToken(token==null?null:token.getRefreshToken());
				user.setPuserGoogleExpiresIn(token==null?null:token.getExpiresIn());
				user.setPuserGoogleGranted(token==null?null:token.getGranted());
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
		return new OAuthToken(user.getPuserGoogleToken(),user.getPuserGoogleRefreshToken(), 
							  user.getPuserGoogleExpiresIn(), user.getPuserGoogleGranted());
	}

	@Override
	public void refreshGoogleToken() throws Exception {
		checkAccess();
		OAuthToken token = new OAuthService().refreshToken(getToken());
		saveToken(token);
	}
	
	@Override
	public ImportExportProcessInfo syncContacts() throws Exception {
		checkAccess();
		
		OAuthToken token = getToken();
		if(!token.exists())
			return new ImportExportProcessInfo(ImportExportProcessInfo.TOKEN_NOTFOUND);
		
		if(token.isExpired()) 
			if(token.canRefresh())
				return new ImportExportProcessInfo(ImportExportProcessInfo.TOKEN_EXPIRED); else
				return new ImportExportProcessInfo(ImportExportProcessInfo.TOKEN_NOTFOUND);
		
		ContactsImport importer = new ContactsImport(token);
		List<ContactEntry> entrys = importer.getAllEntrys(getUser().getPuserContactLastsync());
		if(importer.getLastError()==ContactsImport.NO_AUTH_TOKEN) {
			saveToken(null);
			return new ImportExportProcessInfo(ImportExportProcessInfo.TOKEN_NOTFOUND);
		}	
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();

    	HashMap<String,Customer> customers = getCustomersFromGoogle(session);
    	Date currdt = new Date();
    	
    	ImportExportProcessInfo pi = new ImportExportProcessInfo();
    	try {
			Transaction tx = session.beginTransaction();
			try {
	    		//------------------------
	    		// Google-> Ipplan
	    		//------------------------
				int insertСount = 0;
				int updateCount = 0;
				int count = 0;
				for (ContactEntry entry :entrys) {
					// без имени не записываем
					if(!entry.hasName()) continue;
					Customer c = customers.get(entry.getId());
					if(c==null) {
						c = new Customer();
						c.setCustomerLookupKey(entry.getId());
						contactEntryToCustomer(importer,entry,c);
						session.save(c);
						insertСount++;
					} else {
						contactEntryToCustomer(importer,entry,c);
						updateCount++;
					};	
				    if ( count % 20 == 0 ) {
				        session.flush();
				        session.clear();
				    }
					count++;
				}
				pi.setImportAllCount(count);
				pi.setImportUpdate(updateCount);
				pi.setImportInsert(insertСount);
				Ipplan.info("Google->Ipplan update="+updateCount+" insert="+insertСount);
	    		//------------------------
	    		// Ipplan->Google 
	    		//------------------------
				insertСount = 0;
				updateCount = 0;
				count = 0;
				// делаем hash для поиска
				List<ContactEntry> allentrys = importer.getAllEntrys();
				HashMap<String, ContactEntry> map = new HashMap<String, ContactEntry>();
				for (ContactEntry entry : allentrys) 
					map.put(entry.getId(), entry);
				// -
				List<Customer> outcustomers = getCustomersToGoogle(session);
				for (Customer c : outcustomers) {
					if (c.getCustomerLookupKey()==null) {
						ContactEntry entry = new ContactEntry();
						customerToContactEntry(importer,c,entry);
						ContactEntry createdContact = importer.getService().insert(new URL("https://www.google.com/m8/feeds/contacts/default/full"), entry);
						c.setCustomerLookupKey(createdContact.getId());
						insertСount++;
					} else {
						ContactEntry entry = map.get(c.getCustomerLookupKey());
						if(entry!= null) {
							customerToContactEntry(importer,c,entry);
							URL editUrl = new URL(entry.getEditLink().getHref());
							importer.getService().update(editUrl, entry);
							updateCount++;
						}
					}
					c.setCustomerLastupdate(null);
					count++;
				}
				pi.setExportAllCount(count);
				pi.setExportUpdate(updateCount);
				pi.setExportInsert(insertСount);
				Ipplan.info("Ipplan->Google update="+updateCount+" insert="+insertСount);
				// фиксируем дату синхронизации
				PUserIdent user = getUser();
				session.update(user);
				user.setPuserContactLastsync(currdt);
				
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
			
    	} finally {
    		session.close();
    	}
		return pi;
	}

	private void customerToContactEntry(ContactsImport importer, Customer c, ContactEntry entry) {
		String cn = c.getCustomerName();
		Name name = new Name();
		name.setFullName(new FullName(cn, null));
		String[] cnames = cn.split(" ");
		// предполагаем, что пользователь вводил Ф И О
		name.setFamilyName(new FamilyName(cnames[0],null));
		if(cnames.length>1) {
			name.setGivenName(new GivenName(cnames[1],null));
			if(cnames.length>2) 
				name.setAdditionalName(new AdditionalName(cnames[2], null));
		}
		entry.setName(name);
		//ДР
		Date dt = c.getCustomerBirthday();
		if(dt!=null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			entry.setBirthday(new Birthday(df.format(dt)));
		}
		// company
		if(c.getCustomerCompany()!=null) {
			Organization o = new Organization();
			o.setOrgName(new OrgName(c.getCustomerCompany(),null));
			if(c.getCustomerPosition()!=null)
				o.setOrgTitle(new OrgTitle(c.getCustomerPosition()));
			o.setPrimary(true);
			entry.addOrganization(o);
		}
		//e-mail
		entry.getEmailAddresses().clear();
		if(c.getCustomerPrimaryEmail()!=null) {
			Email e = new Email();
			e.setAddress(c.getCustomerPrimaryEmail());
			e.setRel("http://schemas.google.com/g/2005#work");
			e.setPrimary(true);
			entry.addEmailAddress(e);
		}
		if(c.getCustomerEmails()!=null) {
			String[] ems = c.getCustomerEmails().split(",");
			for (int i = 0; i < ems.length; i++) {
				Email e = new Email();
				e.setAddress(ems[i]);
				e.setRel("http://schemas.google.com/g/2005#other");
				entry.addEmailAddress(e);
			}
		}
		// phones
		entry.getPhoneNumbers().clear();
		if(c.getCustomerPrimaryPhone()!=null) {
			PhoneNumber p = new PhoneNumber();
			p.setPhoneNumber(c.getCustomerPrimaryPhone());
			p.setRel("http://schemas.google.com/g/2005#work");
			p.setPrimary(true);
			entry.addPhoneNumber(p);
		}
		if(c.getCustomerPhones()!=null) {
			String[] ems = c.getCustomerPhones().split(",");
			for (int i = 0; i < ems.length; i++) {
				PhoneNumber p = new PhoneNumber();
				p.setPhoneNumber(ems[i]);
				p.setRel("http://schemas.google.com/g/2005#other");
				entry.addPhoneNumber(p);
			}
		}
		
	}

	private void contactEntryToCustomer(ContactsImport importer, ContactEntry entry, Customer c) {
		// name
		String fullNameToDisplay = "";
		Name name = entry.getName();
	    // пробуем по полям
		if (name.hasNamePrefix()) 
	    	fullNameToDisplay = name.getNamePrefix().getValue();
	    
	    if (name.hasFamilyName()) {
	    	if (fullNameToDisplay.length()>0) fullNameToDisplay += " ";
	    	fullNameToDisplay += name.getFamilyName().getValue();
	        if (name.getFamilyName().hasYomi()) 
	        	fullNameToDisplay += " (" + name.getFamilyName().getYomi() + ")";
	    };
	    if (name.hasGivenName()) {
	    	if (fullNameToDisplay.length()>0) fullNameToDisplay += " ";
	    	fullNameToDisplay += name.getGivenName().getValue();
		    if (name.getGivenName().hasYomi()) 
		    	fullNameToDisplay += " (" + name.getGivenName().getYomi() + ")";
		}		    
	    if (name.hasAdditionalName()) {
	    	if (fullNameToDisplay.length()>0) fullNameToDisplay += " ";
	    	fullNameToDisplay += name.getAdditionalName().getValue();
	        if (name.getAdditionalName().hasYomi()) 
	        	fullNameToDisplay += " (" + name.getAdditionalName().getYomi() + ")";
	    }
	    if (name.hasNameSuffix()) {
	    	if (fullNameToDisplay.length()>0) fullNameToDisplay += " ";
	    	fullNameToDisplay += name.getNameSuffix().getValue();
	    }
		// перекроес если не структурно записано
	    if (fullNameToDisplay.isEmpty() && name.hasFullName()) {
	        fullNameToDisplay = name.getFullName().getValue();
	        if (name.getFullName().hasYomi()) 
	          fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
	    };
		c.setCustomerName(fullNameToDisplay);
		// ДР
		if(entry.hasBirthday()) {
			String v = entry.getBirthday().getValue();
			Date dt = null;
			try {
				dt = new SimpleDateFormat("yyyy-MM-dd").parse(v);
			} catch (Exception e) {
				try {
					dt = new SimpleDateFormat("--MM-dd").parse(v);
				} catch (ParseException e1) {
				}
			}
			if(dt!=null) c.setCustomerBirthday(dt);
		}
		// company
		Organization o = null;      
		if(entry.hasOrganizations()) {
			for (Organization o1 : entry.getOrganizations()) {
				if(o1.getPrimary()){
					o = o1;
					break;
				};
				if(o==null) o = o1;
			}	
		};
		if(o!=null) {
			c.setCustomerCompany(o.getOrgName()!=null?o.getOrgName().getValue():null);
			c.setCustomerPosition(o.getOrgTitle()!=null?o.getOrgTitle().getValue():null);
		};	    

		// email
		Email primaryemail = null;
		if(entry.hasEmailAddresses()) {
			for (Email email : entry.getEmailAddresses()) {
				if(email.getPrimary()){
					primaryemail = email;
					break;
				};
				if(primaryemail==null) primaryemail = email;
			}	
		};
		if(primaryemail!=null)
			c.setCustomerPrimaryEmail(primaryemail.getAddress());
		String allmail = "";
		for (Email email : entry.getEmailAddresses()) {
			if(email!=primaryemail) {
				if(allmail.length()>0) allmail += ",";
				allmail += email.getAddress();
			}	
		}
		if(allmail.length()>0)
			c.setCustomerEmails(allmail);

		// phones
		PhoneNumber primaryphone = null;
		if(entry.hasPhoneNumbers()) {
			for (PhoneNumber p : entry.getPhoneNumbers()) {
				if(p.getPrimary()){
					primaryphone = p;
					break;
				};
				if(primaryphone==null) primaryphone = p;
			}	
		};
		if(primaryphone!=null) {
			String s ="";
			if(primaryphone.getLabel()!=null) s+=primaryphone.getLabel()+":";
			s+=primaryphone.getPhoneNumber();
			c.setCustomerPrimaryPhone(s);
		}	
		
		String allphones = "";
		for (PhoneNumber p : entry.getPhoneNumbers()) {
			if(p!=primaryphone) {
				if(allphones.length()>0) allphones += ",";
				String s ="";
				if(p.getLabel()!=null) s+=p.getLabel()+":";
				s+=p.getPhoneNumber();
				allphones += s;
			}	
		}
		if(allphones.length()>0)
			c.setCustomerPhones(allphones);
		
		//TODO
		// photo не работает пока, error 401
/*		
		Link photoLink = entry.getContactPhotoLink();
		if (photoLink.getEtag() != null) {
			try {
				GDataRequest r = importer.getService().createLinkQueryRequest(photoLink);
				r.setEtag(photoLink.getEtag());
				InputStream in = r.getResponseStream();
				System.out.println("Photo link "+in.available());
			} catch (Exception e) {
				Ipplan.warning("Photo link error "+photoLink.getHref());
			};
		};
*/		
		
/*		
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
*/		
		
	    
	}

	private HashMap<String, Customer> getCustomersFromGoogle(Session sess) {
		HashMap<String, Customer> map = new HashMap<String, Customer>();
		String hsq = "select c from Customer c where c.customerLookupKey is not null";
		Query q = sess.createQuery(hsq);
		List<Customer> l = q.list();
		for (Customer customer : l) 
			map.put(customer.getCustomerLookupKey(), customer);
		return map;
	}

	private List<Customer> getCustomersToGoogle(Session sess) {
   		String hsq = "select c from Customer c where c.customerVisible=1 and (c.customerLookupKey is null or c.customerLastupdate is not null)";
   		Query q = sess.createQuery(hsq);
   		return q.list();
	}
	
	@Override
	public void setContactsAutoSync(int durationClass) throws Exception {
		checkAccess();
		// именно класс, чтобы никто не смог установить некорректное значение
		int duration = 0;
		switch (durationClass) {
			case 1: //полчаса 
				duration = 30*60;
			break;
			case 2: //час 
				duration = 60*60;
			break;
			case 3: //сутки 
				duration = 24*60*60;
			break;
		}
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				PUserIdent user = getUser();
				session.update(user);
				// login user и реальный из БД разсинхронизированы
				user.setPuserContactSyncDuration(duration);
				getLoginUser().puserContactSyncDuration = duration;
				
				tx.commit();
				UserTask.startNewTask(user,sessionFactory);
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    		
    	} finally {
    		session.close();
    	}
	}

	@Override
	public void setCalendarAutoSync(int durationClass) throws Exception {
		checkAccess();
		// именно класс, чтобы никто не смог установить некорректное значение
		int duration = 0;
		switch (durationClass) {
			case 1: //полчаса 
				duration = 30*60;
			break;
			case 2: //час 
				duration = 60*60;
			break;
			case 3: //сутки 
				duration = 24*60*60;
			break;
		}
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				PUserIdent user = getUser();
				
				// login user и реальный из БД разсинхронизированы
				user.setPuserCalendarSyncDuration(duration);
				getLoginUser().puserCalendarSyncDuration = duration;
				
				tx.commit();
				UserTask.startNewTask(user,sessionFactory);
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    		
    	} finally {
    		session.close();
    	}
	}

	@Override
	public  ImportExportProcessInfo syncCalendar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CustomerWrapper addCustomer(CustomerWrapper cw) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Customer c = new Customer();
				c.fromClient(cw);
				// вновь добавляемы всегда виден
				c.setCustomerVisible(1);
				session.save(c);
				tx.commit();
		    	return c.toClient();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    		
    	} finally {
    		session.close();
    	}
	}

	@Override
	public void updateCustomer(CustomerWrapper cw) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Customer c = (Customer) session.load(Customer.class, cw.customerId);
				c.fromClient(cw);
				// дл того, чтобы попала в синхронизацию
				c.setCustomerLastupdate(new Date());
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	private boolean deleteCustomerInner(int id, Session session) {
		Customer c = (Customer) session.get(Customer.class, id);
		if(c==null || c.getCustomerVisible()==0) return false;
		c.setCustomerVisible(0);
		return true;
	}
	
	@Override
	public boolean deleteCustomer(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				boolean res = deleteCustomerInner(id, session);
				tx.commit();
				return res;
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}


	@Override
	public void deleteCustomer(List<CustomerWrapper> list) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				for (CustomerWrapper cw : list)
					deleteCustomerInner(cw.customerId, session);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<BargainWrapper> findBargain(String text, Date finish,
			boolean allUser, boolean[] stats) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
		List<BargainWrapper> list = new ArrayList<BargainWrapper>();
    	try {
    		String hsq = "select b from Bargain b where b.bargainHead=1 and b.bargainVisible=1";
    		int usrid = getUserId();
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			hsq+=" and b.puser.puserId="+usrid;
    		// фильтры -------------------
    		// по пользователю
    		if(usrid == PUserIdent.USER_ROOT_ID && !allUser)
    			hsq+=" and b.puser.puserId="+PUserIdent.USER_ROOT_ID;
    		// по перескчению дат
			hsq+=" and b.bargainFinish>=:start";
			hsq+=" and b.bargainStart<=:finish";
    		// по тексту
			if(text!=null && !text.isEmpty())
			hsq+=" and ("+"UPPER(b.bargainName) like :txt"+
			     " or  UPPER(b.customer.customerName) like :txt"+
			     " or UPPER(b.customer.customerPrimaryEmail) like :txt"+
		     	 " or UPPER(b.customer.customerEmails) like :txt"+
		     	 " or b.customer.customerPrimaryPhone like :txt"+
		     	 " or b.customer.customerPhones like :txt"+
		     	 " or UPPER(b.customer.customerCompany) like :txt"+
		     	 " or UPPER(b.customer.customerPosition) like :txt"+
		     	 " or b.bargainRevenue = :numb"+
		     	 ")";
		     // по состоянию
			if(stats!=null) {
				String s="";
					//в работе
				if(stats[0]) 
		    		s+=" not(b.status.statusId in ("+StatusWrapper.CLOSE_FAIL+","+
		    					StatusWrapper.CLOSE_OK+","+StatusWrapper.SUSPENDED+"))";
					//выполненные
				if(stats[1]) { 
					if(!s.isEmpty()) s+=" or "; 
					s+="b.status.statusId="+StatusWrapper.CLOSE_OK;
				};	
					//просроченные
				if(stats[2]) { 
					if(!s.isEmpty()) s+=" or "; 
					s+="b.bargainFinish-current_timestamp<0";
				}	
					//несогласованные
				if(stats[3]) { 
					if(!s.isEmpty()) s+=" or "; 
					s+="b.status.statusId="+StatusWrapper.SUSPENDED;
				}	
				if(!s.isEmpty()) hsq+=" and("+s+")";
			};
    		// самые близкие по плану
    		hsq+=" order by b.bargainFinish";
    		
    		Query q = session.createQuery(hsq);
    		q.setParameter("start", new Date(finish.getYear(),0,1));
    		q.setParameter("finish", finish);
			if(text!=null && !text.isEmpty()) {
				q.setParameter("txt", "%"+text.toUpperCase()+"%");
	    		q.setParameter("numb",null);
				try {
					double value = Double.parseDouble(text.replaceAll("\\s","").replace(',', '.'));
					q.setParameter("numb", new Long(Math.round(value*100)).intValue());
					
				} catch(NumberFormatException e) {
					// since
				}	
			}	
    		
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
	public void deleteBargain(List<BargainWrapper> list) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				for (BargainWrapper cw : list)
					deleteBargainInner(cw.bargainId, session);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public boolean deleteBargain(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				boolean r = deleteBargainInner(id, session);
				tx.commit();
				return r;
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	private boolean deleteBargainInner(int id, Session session) {
		Bargain c = (Bargain) session.get(Bargain.class, id);
		if(c==null || c.getBargainVisible()==0) return false;
		c.setBargainVisible(0);
		return true;
	}

	@Override
	public BargainWrapper prevBargainVersion(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		String hsq = "select b1 from Bargain b, Bargain b1 where b.bargainId=:id"+
    					 " and b1.rootBargain=b.rootBargain and b1.bargainVer=b.bargainVer-1";
    		Query q = session.createQuery(hsq);
    		q.setParameter("id", id);
    		Bargain b = (Bargain) q.uniqueResult();
    		if(b!=null) {
    			BargainWrapper bw = b.toClient();
    			fillTaskList(b.getBargainId(), session, bw);
    			return bw;
    		}else return null;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public BargainWrapper nextBargainVersion(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		String hsq = "select b1 from Bargain b, Bargain b1 where b.bargainId=:id"+
					 " and b1.rootBargain=b.rootBargain and b1.bargainVer=b.bargainVer+1";
    		Query q = session.createQuery(hsq);
    		q.setParameter("id", id);
    		Bargain b = (Bargain) q.uniqueResult();
    		if(b!=null) {
    			BargainWrapper bw = b.toClient();
    			fillTaskList(b.getBargainId(), session, bw);
    			return bw;
    		}else return null;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<TasktypeWrapper> getTasktypes() throws Exception {
		checkAccess();
		List<TasktypeWrapper> result =  new ArrayList<TasktypeWrapper>();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		Query q = session.createQuery("from Tasktype tt order by tt.tasktypeId");
    		List<Tasktype> list = q.list();
    		for (Tasktype tt : list) 
				result.add(tt.toClient());
			return result;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<TaskWrapper> getTask(int bargainId) throws Exception {
		checkAccess();
		List<TaskWrapper> result =  new ArrayList<TaskWrapper>();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		Query q = session.createQuery("from Task t where t.calendar.bargain.bargainId=:id order by t.taskDeadline");
    		q.setParameter("id", bargainId);
    		List<Task> list = q.list();
    		for (Task tt : list) 
				result.add(tt.toClient());
			return result;
    	} finally {
    		session.close();
    	}
	}

	@Override
	public TaskWrapper addTask(TaskWrapper task) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Task t = new Task(); 
				t.fromClient(task);
				// проеряем на наличие календаря
				int rootbargainId = t.getCalendar().getBargain().getBargainId();
				Calendar cal = (Calendar) session.get(Calendar.class, rootbargainId);
				if(cal==null)
					session.save(t.getCalendar());
				session.save(t);
				tx.commit();
				return t.toClient();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public TaskWrapper updateTask(TaskWrapper task) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Task t = (Task) session.load(Task.class, task.taskId); 
				t.fromClient(task);	
				tx.commit();
				return t.toClient();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}
	
	@Override
	public void executedTask(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Transaction tx = session.beginTransaction();
			try {
				Task t = (Task) session.load(Task.class, id);
				t.setTaskExecuted(1);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}

	@Override
	public boolean deleteTask(int id) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
			Task t = (Task) session.get(Task.class, id);
			if(t==null) return false;
			Transaction tx = session.beginTransaction();
			try {
				session.delete(t);
				tx.commit();
				return true;
			} catch (Exception e) {
				tx.rollback();
				throw e;
			}
    	} finally {
    		session.close();
    	}
	}



}
