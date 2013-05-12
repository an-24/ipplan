package com.cantor.ipplan.db.ud;

// Generated 12.04.2013 19:58:46 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.core.IdGetter;
import com.cantor.ipplan.shared.Attention;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.StatusWrapper;

/**
 * Bargain generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "BARGAIN", uniqueConstraints = @UniqueConstraint(columnNames = {
		"ROOT_BARGAIN_ID", "BARGAIN_VER" }))
public class Bargain implements java.io.Serializable,DataBridge<BargainWrapper>,IdGetter {
	private int bargainId;
	private String bargainName;
	private PUserIdent puser;
	private Bargain rootBargain;
	private Status status;
	private Customer customer;
	private Contract contract;
	private int bargainVer = 0;
	private Date bargainStart;
	private Date bargainFinish;
	private Integer bargainRevenue;
	private Integer bargainPrepayment;
	private Integer bargainCosts;
	private Integer bargainPaymentCosts;
	private Integer bargainFine;
	private Integer bargainTax;
	private Integer bargainHead;
	public Date bargainCreated;

	private Set<Bargaincosts> bargaincostses = new HashSet<Bargaincosts>(0);
	private Set<Bargain> bargains = new HashSet<Bargain>(0);
	private Set<Agreed> agreeds = new HashSet<Agreed>(0);
	
	private boolean newState;
	private boolean dirty;

	// количество дней, за котщрое нужно предупреждать, из статуса EXECUTION
	public static final Integer EXECUTE_WARNING_DURATION_LIMIT = 10;
	// количество дней, за котщрое нужно предупреждать, из статуса COMPLETION
	public static final Integer COMPLETION_WARNING_DURATION_LIMIT = 5;

	public Bargain() {
	}

	public Bargain(int bargainId, Customer customer, PUserIdent puser,
			Bargain bargain, int bargainVer, Date bargainStart,
			Date bargainFinish) {
		this.bargainId = bargainId;
		this.customer = customer;
		this.puser = puser;
		this.rootBargain = bargain;
		this.bargainVer = bargainVer;
		this.bargainStart = bargainStart;
		this.bargainFinish = bargainFinish;
	}

	public Bargain(int bargainId, Contract contract, Customer customer,
			PUserIdent puser, Bargain bargain, Status status, int bargainVer,
			Date bargainStart, Date bargainFinish, Integer bargainRevenue,
			Integer bargainPrepayment, Integer bargainCosts,
			Integer bargainPaymentCosts, Integer bargainFine,
			Integer bargainTax, Set<Bargaincosts> bargaincostses, Set<Bargain> bargains,
			Set<Agreed> agreeds) {
		this.bargainId = bargainId;
		this.contract = contract;
		this.customer = customer;
		this.puser = puser;
		this.rootBargain = bargain;
		this.status = status;
		this.bargainVer = bargainVer;
		this.bargainStart = bargainStart;
		this.bargainFinish = bargainFinish;
		this.bargainRevenue = bargainRevenue;
		this.bargainPrepayment = bargainPrepayment;
		this.bargainCosts = bargainCosts;
		this.bargainPaymentCosts = bargainPaymentCosts;
		this.bargainFine = bargainFine;
		this.bargainTax = bargainTax;
		this.bargaincostses = bargaincostses;
		this.bargains = bargains;
		this.agreeds = agreeds;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "BARGAIN_ID", unique = true, nullable = false)
	public int getBargainId() {
		return this.bargainId;
	}

	public void setBargainId(int bargainId) {
		this.bargainId = bargainId;
	}

	@Column(name = "BARGAIN_NAME", nullable = false, length = 220)
	public String getBargainName() {
		return bargainName;
	}

	public void setBargainName(String bargainName) {
		this.bargainName = bargainName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTRACT_ID")
	public Contract getContract() {
		return this.contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMER_ID", nullable = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_ID", nullable = false)
	public PUserIdent getPuser() {
		return this.puser;
	}

	public void setPuser(PUserIdent puser) {
		this.puser = puser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROOT_BARGAIN_ID", nullable = false)
	public Bargain getRootBargain() {
		return this.rootBargain;
	}

	public void setRootBargain(Bargain bargain) {
		this.rootBargain = bargain;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STATUS_ID")
	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Column(name = "BARGAIN_VER", nullable = false)
	public int getBargainVer() {
		return this.bargainVer;
	}

	public void setBargainVer(int bargainVer) {
		this.bargainVer = bargainVer;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BARGAIN_START", nullable = false, length = 10)
	public Date getBargainStart() {
		return this.bargainStart;
	}

	public void setBargainStart(Date bargainStart) {
		this.bargainStart = bargainStart;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BARGAIN_FINISH", nullable = false, length = 10)
	public Date getBargainFinish() {
		return this.bargainFinish;
	}

	public void setBargainFinish(Date bargainFinish) {
		this.bargainFinish = bargainFinish;
	}

	@Column(name = "BARGAIN_REVENUE")
	public Integer getBargainRevenue() {
		return this.bargainRevenue;
	}

	public void setBargainRevenue(Integer bargainRevenue) {
		this.bargainRevenue = bargainRevenue;
	}

	@Column(name = "BARGAIN_PREPAYMENT")
	public Integer getBargainPrepayment() {
		return this.bargainPrepayment;
	}

	public void setBargainPrepayment(Integer bargainPrepayment) {
		this.bargainPrepayment = bargainPrepayment;
	}

	@Column(name = "BARGAIN_COSTS")
	public Integer getBargainCosts() {
		return this.bargainCosts;
	}

	public void setBargainCosts(Integer bargainCosts) {
		this.bargainCosts = bargainCosts;
	}

	@Column(name = "BARGAIN_PAYMENT_COSTS")
	public Integer getBargainPaymentCosts() {
		return this.bargainPaymentCosts;
	}

	public void setBargainPaymentCosts(Integer bargainPaymentCosts) {
		this.bargainPaymentCosts = bargainPaymentCosts;
	}

	@Column(name = "BARGAIN_FINE")
	public Integer getBargainFine() {
		return this.bargainFine;
	}

	public void setBargainFine(Integer bargainFine) {
		this.bargainFine = bargainFine;
	}

	@Column(name = "BARGAIN_TAX")
	public Integer getBargainTax() {
		return this.bargainTax;
	}

	public void setBargainTax(Integer bargainTax) {
		this.bargainTax = bargainTax;
	}

	@Column(name = "BARGAIN_HEAD")
	public Integer getBargainHead() {
		return bargainHead;
	}

	public void setBargainHead(Integer bargainHead) {
		this.bargainHead = bargainHead;
	}

	@Column(name = "BARGAIN_CREATED", nullable = false)
	public Date getBargainCreated() {
		return bargainCreated;
	}

	public void setBargainCreated(Date bargainCreated) {
		this.bargainCreated = bargainCreated;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bargain")
	public Set<Bargaincosts> getBargaincostses() {
		return this.bargaincostses;
	}

	public void setBargaincostses(Set<Bargaincosts> bargaincostses) {
		this.bargaincostses = bargaincostses;
	}

	@Transient
	public Set<Bargain> getBargains() {
		return this.bargains;
	}

	public void setBargains(Set<Bargain> bargains) {
		this.bargains = bargains;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bargain")
	public Set<Agreed> getAgreeds() {
		return this.agreeds;
	}

	public void setAgreeds(Set<Agreed> agreeds) {
		this.agreeds = agreeds;
	}

	@Override
	public BargainWrapper toClient() {
		BargainWrapper wrap = new BargainWrapper();
		wrap.bargainId = bargainId;
		wrap.bargainName = bargainName;
		//wrap.contract;
		wrap.customer = customer==null?null:customer.toClient();
		wrap.puser = puser.toClient();
		//wrap.bargain;
		wrap.status = status.toClient();
		wrap.bargainVer =  bargainVer;
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
		
		wrap.isnew = newState;
		if(dirty) wrap.modify();

		return wrap;
	}

	@Override
	public void fromClient(BargainWrapper data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetch(boolean deep) {
		Hibernate.initialize(this.getContract());
		Hibernate.initialize(this.getCustomer());
		Hibernate.initialize(this.getPuser());
		Hibernate.initialize(this.getRootBargain());
		Hibernate.initialize(this.getStatus());
		Hibernate.initialize(this.getBargaincostses());
		Hibernate.initialize(this.getAgreeds());
		if(deep) {
			if(this.getContract()!=null) this.getContract().fetch(deep);
			if(this.getCustomer()!=null) this.getCustomer().fetch(deep);
			if(this.getPuser()!=null) this.getPuser().fetch(deep);
			if(this.getRootBargain()!=null) this.getRootBargain().fetch(deep);
			if(this.getStatus()!=null) this.getStatus().fetch(deep);
		}
	}

	@Transient
	private static final int MILLISSECOND_PER_DAY = 24 * 60 * 60 * 1000;
	
	/*
	 * Метод генерирует предупреждение. Внимание, не проверяет условия
	 */
	public Attention makeAttention() {
		Attention at = new Attention();
		int daycount;
		switch (status.getId()) {
		case StatusWrapper.SUSPENDED:
			at.type = 1;
			at.message = "Необходимо согласование";
			break;
		case StatusWrapper.COMPLETION:
		case StatusWrapper.EXECUTION:
			daycount = (int) ((bargainFinish.getTime()-new Date().getTime())/MILLISSECOND_PER_DAY);
			if(daycount<0) {
				at.type = 3;
				at.message = "Срок истек "+daycount+" дн. назад"; 
			} else {
				at.type = 2;
				at.message = "Осталось "+daycount+" дн.";
			}
			break;
		default:
			return null;
		}
		return at;
	}

	@Transient
	@Override
	public int getId() {
		return bargainId;
	}

	@Transient
	public boolean isNew() {
		return newState;
	}

	public void setNew(boolean newState) {
		this.newState = newState;
	}

	@Transient
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
