package com.cantor.ipplan.db.ud;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.core.IdGetter;
import com.cantor.ipplan.shared.PUserWrapper;


@SuppressWarnings("serial")
@Entity
@Table(name = "PUSER")
@DynamicUpdate
public class PUserIdent implements java.io.Serializable, IdGetter, DataBridge<PUserWrapper> {
	
	@Transient
	public static final int USER_ROOT_ID = 1;
	
	private int puserId;
	private PUserIdent owner;
	private String puserLogin;
	private int puserTaxtype;

	public PUserIdent() {
		super();
	}
	
	public PUserIdent(int puserId, PUserIdent owner, String puserLogin) {
		super();
		this.puserId = puserId;
		this.owner = owner;
		this.puserLogin = puserLogin;
	}

	public PUserIdent(PUserWrapper pattern) {
		super();
		fromClient(pattern);
	}
	
	
	@Id
	@GenericGenerator(name="newRec", strategy="com.cantor.ipplan.core.IdGenerator")
	@GeneratedValue(generator = "newRec")
	@Column(name = "PUSER_ID", unique = true, nullable = false)
	public int getPuserId() {
		return this.puserId;
	}

	public void setPuserId(int puserId) {
		this.puserId = puserId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OWNER_PUSER_ID")
	public PUserIdent getOwner() {
		return this.owner;
	}

	public void setOwner(PUserIdent puser) {
		this.owner = puser;
	}

	@Column(name = "PUSER_LOGIN", nullable = false, length = 60)
	public String getPuserLogin() {
		return this.puserLogin;
	}

	public void setPuserLogin(String puserLogin) {
		this.puserLogin = puserLogin;
	}

	@Column(name = "PUSER_TAXTYPE", nullable = false)
	public int getPuserTaxtype() {
		return puserTaxtype;
	}

	public void setPuserTaxtype(int puserTaxtype) {
		this.puserTaxtype = puserTaxtype;
	}
	
	@Transient
	@Override
	public int getId() {
		return this.puserId;
	}

	@Override
	public PUserWrapper toClient() {
		PUserWrapper wrap = new PUserWrapper();
		wrap.puserId = puserId;
		wrap.puserEmail = puserLogin;
		wrap.puserTaxtype = puserTaxtype;
		wrap.owner = owner==null?null:owner.toClient();
		return wrap;
	}

	@Override
	public void fromClient(PUserWrapper data) {
		puserId = data.puserId;
		if(data.owner!=null) {
			if(owner==null) owner = new PUserIdent();
			owner.fromClient(data.owner); 
		}
		puserLogin = data.puserLogin;
		puserTaxtype = data.puserTaxtype;
	}

	@Override
	public void fetch(boolean deep) {
		// TODO Auto-generated method stub
		
	}
}
