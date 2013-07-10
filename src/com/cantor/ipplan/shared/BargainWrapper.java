package com.cantor.ipplan.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cantor.ipplan.db.ud.Bargain;
import com.cantor.ipplan.db.ud.Bargaincosts;


@SuppressWarnings("serial")
public class BargainWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {

	public int bargainId;
	public String bargainName;
	public ContractWrapper contract;
	public CustomerWrapper customer;
	public PUserWrapper puser;
	public BargainWrapper rootBargain;
	public StatusWrapper status;
	public int bargainVer;
	public Date bargainStart;
	public Date bargainFinish;
	public Integer bargainRevenue;
	public Integer bargainPrepayment;
	public Integer bargainCosts;
	public Integer bargainPaymentCosts;
	public Integer bargainFine;
	public Integer bargainTax;
	public Integer bargainHead;
	public Date bargainCreated;
	public CalendarWrapper calendar;
	public String bargainNote;
	
	public Set<BargaincostsWrapper> bargaincostses = new HashSet<BargaincostsWrapper>(0);
	public Set<TaskWrapper> tasks = new HashSet<TaskWrapper>(0);
	public Set<AgreedWrapper> agreeds = new HashSet<AgreedWrapper>(0);
	public List<BargainShortInfo> timeline = new ArrayList<BargainShortInfo>(0);
	public Set<FileLinksWrapper> flinks = new HashSet<FileLinksWrapper>(0);
	
	public Attention attention;
	public boolean isnew;
	private boolean dirty=false;
	
	
	public BargainWrapper(){
		super();
		bargainCreated = new Date();
	}
	
	public BargainWrapper(String text) {
		this();
		bargainName = text;
	}

	public String getFullName() {
		return rootBargain.bargainId+". "+bargainName;
	}
	
	public void modify() {
		dirty = true;
	}
	
	public void saveCompleted() {
		dirty = false;
		isnew = false;
	}

	public void cancel() {
		dirty = false;
	}
	
	public boolean isNew() {
		return isnew;
	}

	public boolean isDirty() {
		return dirty;
	}


	public int getProfit() {
		int v = bargainRevenue!=null?bargainRevenue:0; 
		v-= (bargainCosts!=null?bargainCosts:0);
		v-= (bargainFine!=null?bargainFine:0);
		v-= (bargainTax!=null?bargainTax:0);
		return v;   
	}

	public int getMargin() {
		int v = bargainRevenue!=null?bargainRevenue:0; 
		v-= (bargainCosts!=null?bargainCosts:0);
		return v;
	}

	public int calcTax() {
		if(bargainRevenue==null) return 0;
		//6% от дохода
		if(puser.puserTaxtype==1) return ((Long)Math.round(0.06*bargainRevenue)).intValue(); else
			if(puser.puserTaxtype==2) 
				return ((Long)Math.max(Math.round(0.15*getMargin()), Math.round(0.01*bargainRevenue))).intValue();
		return 0;
	}

	static final int MILLISSECOND_PER_DAY = 24 * 60 * 60 * 1000;
	
	public Attention calcAttention() {
		Attention at = new Attention();
		int daycount;
		switch (status.statusId ) {
		case StatusWrapper.COMPLETION:
		case StatusWrapper.EXECUTION:
			daycount = (int) ((bargainFinish.getTime()-new Date().getTime())/MILLISSECOND_PER_DAY);
			if(daycount<0) {
				at.type = 3;
				at.message = "Срок истек "+(-daycount)+" дн. назад"; 
				return at;
			} else 
			if (daycount<5){
				at.type = 2;
				at.message = "Осталось "+daycount+" дн.";
				return at;
			}
			break;
		default:
			daycount = (int) ((bargainFinish.getTime()-new Date().getTime())/MILLISSECOND_PER_DAY);
			if(daycount<0) {
				at.type = 3;
				at.message = "Срок истек "+(-daycount)+" дн. назад"; 
				return at;
			}; 
		}
		return null;
	}

	public BargainWrapper copy() {
		BargainWrapper wrap = new BargainWrapper();
		wrap.bargainId = bargainId;
		wrap.bargainName = bargainName;
		wrap.contract = contract!=null?contract.copy():null; 
		wrap.customer = customer!=null?customer.copy():null;
		wrap.puser = puser;  //no copy
		wrap.status = status.clone();
		wrap.bargainVer =  bargainVer;
		wrap.rootBargain = rootBargain;
		wrap.bargainStart = bargainStart;
		wrap.bargainFinish = bargainFinish;
		wrap.bargainRevenue = bargainRevenue;
		wrap.bargainPrepayment = bargainPrepayment;
		wrap.bargainCosts = bargainCosts;
		wrap.bargainPaymentCosts = bargainPaymentCosts;
		wrap.bargainFine = bargainFine;
		wrap.bargainTax = bargainTax;
		wrap.bargainHead = bargainHead;
		wrap.bargainCreated = bargainCreated;
		
		wrap.bargaincostses = new HashSet<BargaincostsWrapper>(0);
		for (BargaincostsWrapper bcw : bargaincostses) 
			wrap.bargaincostses.add(bcw.copy());
		
		
		wrap.tasks = new HashSet<TaskWrapper>(0);
		for (TaskWrapper bcw : tasks) 
			wrap.tasks.add(bcw.copy());
		
		wrap.flinks  = new HashSet<FileLinksWrapper>(0);
		for (FileLinksWrapper flnk : flinks) 
			wrap.flinks.add(flnk.copy());
		
		
		wrap.isnew = isnew;
		wrap.dirty = dirty; 
		return wrap;
	}

	
}
