package com.cantor.ipplan.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;

import com.cantor.ipplan.client.AnalyticalService;
import com.cantor.ipplan.shared.ChartOptions;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.DistributeCost;
import com.cantor.ipplan.shared.DistributeStaff;
import com.cantor.ipplan.shared.DynamicMonthData;
import com.cantor.ipplan.shared.DynamicMonthSeries;
import com.cantor.ipplan.shared.StatusWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class AnalyticalServiceImpl extends BaseServiceImpl implements
		AnalyticalService {
	
	@Override
	public List<DynamicMonthSeries> dynamicMonthData(Date start, Date finish, Integer[] statuses, ChartOptions options) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		List<DynamicMonthSeries> series =  new ArrayList<DynamicMonthSeries>();
    		int usrid = getUserId();
    		Query q;
    		String sql = 
    		  	  "select count(DISTINCT rb.bargain_id) \"count\","+
    		      " sum(b.bargain_revenue) \"revenue\","+
    		      " sum(b.bargain_costs) \"costs\","+
    		      " sum(b.bargain_tax) \"tax\","+
    		      " sum(b.bargain_fine) \"fine\","+
    		  	  " b.status_id \"statusId\", EXTRACT(YEAR from b.bargain_created) \"year\", EXTRACT(MONTH from b.bargain_created) \"month\" "+
    			  "from bargain b "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
    			  "where b.bargain_visible=1 AND b.bargain_created between :dstart AND :dfinish AND "+
    			  "b.status_id in ("+Joiner.on(',').join(Lists.newArrayList(statuses))+") ";
    		if(usrid != PUserIdent.USER_ROOT_ID || !options.all)
    			sql+=" AND b.puser_id="+usrid;
    		if(options.excludeSelf)
    			sql+=" AND b.puser_id<>"+usrid;
    		if(options.onlyHead) {
    			sql+=" AND b.bargain_head=1";
    		}
    		// важно, что сделка начиналась с первичного контакта
    		// так как это ПРОДАЖИ!
    		if(options.sales)
    			sql+=" AND "+StatusWrapper.PRIMARY_CONTACT+"=(select min(b1.status_id) from bargain b1 where b1.root_bargain_id=b.root_bargain_id) ";
    		sql+=" group by b.status_id, EXTRACT(YEAR from b.bargain_created),EXTRACT(MONTH from b.bargain_created)";
    		
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DynamicMonthData.class));
      		q.setParameter("dstart", start);
      		q.setParameter("dfinish", finish);
      		
      		List<DynamicMonthData> rawdata = q.list();
      		// раскладываем по сериям
      		HashMap<Integer, DynamicMonthSeries> index = new HashMap<Integer, DynamicMonthSeries>();
      		for (DynamicMonthData point : rawdata) {
      			DynamicMonthSeries seria = index.get(point.getStatusId());
      			if(seria==null) {
      				seria = new DynamicMonthSeries();
      				seria.statusId = point.getStatusId();
      				series.add(seria);
      				index.put(point.getStatusId(), seria);
      			}
      			seria.data.add(point);
			}
      		// выравниваем - добавляяем нули
      		while(start.before(finish)) {
      			int m = start.getMonth()+1;
      			int y = start.getYear()+1900;
      			for (DynamicMonthSeries seria : series) {
      				if(!findPointByMonth(seria,m,y)) {
      					DynamicMonthData point = new DynamicMonthData();
      					point.setStatusId(seria.statusId);
      					point.setMonth((short) m);
      					point.setYear((short) y);
      					seria.data.add(point);
      				}
				}
      			addMonthsToDate(start,1);
      		}	
      		
      		return series;
      		
    	} finally {
    		session.close();
    	}
	}
	
	private static boolean findPointByMonth(DynamicMonthSeries seria,int month, int year) {
		for (DynamicMonthData d : seria.data) {
			if(d.getMonth()==month && d.getYear()==year) return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private static void addMonthsToDate(Date date, int months) {
	    if (months != 0) {
	    	int month = date.getMonth();
		    int year = date.getYear();

		    int resultMonthCount = year * 12 + month + months;
		    int resultYear = resultMonthCount / 12;
		    int resultMonth = resultMonthCount - resultYear * 12;

		    date.setMonth(resultMonth);
		    date.setYear(resultYear);
		}
	}

	@Override
	public List<DistributeStaff> distributeStaffs(Date start, Date finish, int status, ChartOptions options) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
    		Query q;
    		String sql = 
    		  	  "select count(DISTINCT rb.bargain_id) \"int\",sum(b.bargain_revenue) \"double\", b.puser_id \"userId\", u.puser_login \"userLogin\" "+
    			  "from bargain b inner join puser u on u.puser_id = b.puser_id "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
    			  "where b.bargain_visible=1 AND b.bargain_created between :dstart AND :dfinish AND "+
    			  "b.status_id = :status ";
    		if(usrid != PUserIdent.USER_ROOT_ID || !options.all)
    			sql+=" AND b.puser_id="+usrid;
    		if(options.excludeSelf)
    			sql+=" AND b.puser_id<>"+usrid;
    		// важно, что сделка начиналась с первичного контакта
    		// так как это ПРОДАЖИ!
   			sql+=" AND "+StatusWrapper.PRIMARY_CONTACT+"=(select min(b1.status_id) from bargain b1 where b1.root_bargain_id=b.root_bargain_id) ";
    		sql+=" group by b.puser_id, u.puser_login";
    		
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DistributeStaff.class));
      		q.setParameter("dstart", start);
      		q.setParameter("dfinish", finish);
      		q.setParameter("status", status);
      		
      		List<DistributeStaff> series = q.list();
      		return series;
      		
    	} finally {
    		session.close();
    	}
	}

	@Override
	public List<DistributeStaff> effectiveSales(Date start, Date finish,
			ChartOptions options) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
    		Query q;
    		// execution
    		String sql = 
    		  	  "select count(DISTINCT rb.bargain_id) \"int\", b.puser_id \"userId\", u.puser_login \"userLogin\" "+
    			  "from bargain b inner join puser u on u.puser_id = b.puser_id "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
    			  "where b.bargain_visible=1 AND b.bargain_created between :dstart AND :dfinish AND "+
    			  "b.status_id = "+StatusWrapper.EXECUTION;
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			sql+=" AND b.puser_id="+usrid;
    		// важно, что сделка начиналась с первичного контакта
    		// так как это ПРОДАЖИ!
   			sql+=" AND "+StatusWrapper.PRIMARY_CONTACT+"=(select min(b1.status_id) from bargain b1 where b1.root_bargain_id=b.root_bargain_id) ";
    		sql+=" group by b.puser_id, u.puser_login";
    		
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DistributeStaff.class));
      		q.setParameter("dstart", start);
      		q.setParameter("dfinish", finish);
      		
      		HashMap<Integer, DistributeStaff> seriesExecution = makeDistributeHash(q.list());
      		
    		// fault
    		sql = "select count(DISTINCT rb.bargain_id) \"int\", b.puser_id \"userId\", u.puser_login \"userLogin\" "+
      			  "from bargain b inner join puser u on u.puser_id = b.puser_id "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
      			  "where b.bargain_visible=1 AND b.bargain_created between :dstart AND :dfinish AND "+
      			  "b.status_id = "+StatusWrapper.CLOSE_FAULT;
      		if(usrid != PUserIdent.USER_ROOT_ID)
      			sql+=" AND b.puser_id="+usrid;
      		// важно, что сделка начиналась с первичного контакта
      		// так как это ПРОДАЖИ!
     			sql+=" AND "+StatusWrapper.PRIMARY_CONTACT+"=(select min(b1.status_id) from bargain b1 where b1.root_bargain_id=b.root_bargain_id) ";
      		sql+=" group by b.puser_id, u.puser_login";
      		
      		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DistributeStaff.class));
        		q.setParameter("dstart", start);
        		q.setParameter("dfinish", finish);
      		
        	HashMap<Integer, DistributeStaff> seriesFault = makeDistributeHash(q.list());
        	
        	
    		// primary
    		sql = "select count(DISTINCT rb.bargain_id) \"int\", b.puser_id \"userId\", u.puser_login \"userLogin\" "+
    			  "from bargain b inner join puser u on u.puser_id = b.puser_id "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
    			  "where b.bargain_visible=1 AND b.bargain_created between :dstart AND :dfinish AND "+
    			  "b.status_id = "+StatusWrapper.PRIMARY_CONTACT;
    		if(usrid != PUserIdent.USER_ROOT_ID)
    			sql+=" AND b.puser_id="+usrid;
    		sql+=" group by b.puser_id, u.puser_login";
    		
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DistributeStaff.class));
      		q.setParameter("dstart", start);
      		q.setParameter("dfinish", finish);
    		
          	List<DistributeStaff> series = q.list();
      		
      		
          	for (DistributeStaff staff : series) {
				if(staff.getInt()>0) {
					double value = 0;
					DistributeStaff executed = seriesExecution.get(staff.getUserId());
					if(executed!=null) value+=executed.getInt();
					DistributeStaff faulted = seriesFault.get(staff.getUserId());
					if(faulted!=null) value-=executed.getInt();
					staff.setDoubleValue(value/staff.getInt());
				}
			}
          	
      		
      		return series;
      		
    	} finally {
    		session.close();
    	}
	}

	private HashMap<Integer, DistributeStaff> makeDistributeHash(List<DistributeStaff> list) {
		HashMap<Integer, DistributeStaff> hash = new HashMap<Integer, DistributeStaff>();
		for (DistributeStaff ds : list) hash.put(ds.getUserId(), ds);
		return hash;
	}

	@Override
	public List<DistributeCost> distributeCosts(Date start, Date finish,
			ChartOptions options) throws Exception {
		checkAccess();
		SessionFactory sessionFactory = getSessionFactory();
    	Session session = sessionFactory.openSession();
    	try {
    		int usrid = getUserId();
    		Query q;
    		String sql = 
    		  	  "select sum(bc.bargaincosts_value) \"doubleValue\", c.costs_id \"costsId\", c.costs_name \"costsName\" "+
    			  "from bargaincosts bc inner join costs c on c.costs_id = bc.costs_id "+
    			  "     inner join bargain b on b.bargain_id = bc.bargain_id "+
    			  "     inner join bargain rb on rb.bargain_id = b.root_bargain_id "+
    			  "where b.bargain_visible=1 AND b.bargain_head=1 AND b.bargain_created between :dstart AND :dfinish ";
    		if(usrid != PUserIdent.USER_ROOT_ID || !options.all)
    			sql+=" AND b.puser_id="+usrid;
    		if(options.excludeSelf)
    			sql+=" AND b.puser_id<>"+usrid;
    		sql+=" group by c.costs_id, c.costs_name";
    		
    		q = session.createSQLQuery(sql).setResultTransformer(Transformers.aliasToBean(DistributeCost.class));
      		q.setParameter("dstart", start);
      		q.setParameter("dfinish", finish);
      		
      		List<DistributeCost> series = q.list();
      		return series;
      		
    	} finally {
    		session.close();
    	}
	}

}
