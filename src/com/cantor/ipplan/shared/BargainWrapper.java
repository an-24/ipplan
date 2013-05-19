package com.cantor.ipplan.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("serial")
public class BargainWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {

	public int bargainId;
	public String bargainName;
	public ContractWrapper contract;
	public CustomerWrapper customer;
	public PUserWrapper puser;
	public BargainWrapper bargain;
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
	public Set<BargaincostsWrapper> bargaincostses = new HashSet<BargaincostsWrapper>(0);
	public Set<BargainWrapper> bargains = new HashSet<BargainWrapper>(0);
	public Set<AgreedWrapper> agreeds = new HashSet<AgreedWrapper>(0);
	
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
		return bargainId+". "+bargainName;
	}
	
	public void modify() {
		dirty = true;
	}
	
	public void saveCompleted() {
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
	
}
