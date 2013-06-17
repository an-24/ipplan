package com.cantor.ipplan.db.ud;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
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
	public static final int USER_ROOT_ID = -1;
	
	private int puserId;
	private PUserIdent owner;
	private String puserLogin;
	private int puserTaxtype;
	private String puserGoogleToken;
	private String puserGoogleRefreshToken;
	private Integer puserGoogleExpiresIn;
	private Date puserGoogleGranted;
	private Date puserContactLastsync;
	private Date puserCalendarLastsync;
	private int puserContactSyncDuration;
	private int puserCalendarSyncDuration;

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

	@Column(name = "PUSER_GOOGLE_TOKEN", length = 100)
	public String getPuserGoogleToken() {
		return this.puserGoogleToken;
	}

	public void setPuserGoogleToken(String puserGoogleToken) {
		this.puserGoogleToken = puserGoogleToken;
	}

	@Column(name = "PUSER_GOOGLE_REFRESH_TOKEN", length = 100)
	public String getPuserGoogleRefreshToken() {
		return this.puserGoogleRefreshToken;
	}

	public void setPuserGoogleRefreshToken(String puserGoogleToken) {
		this.puserGoogleRefreshToken = puserGoogleToken;
	}
	
	@Column(name = "PUSER_GOOGLE_EXPIRES_IN")
	public Integer getPuserGoogleExpiresIn() {
		return (puserGoogleExpiresIn==null)?0:this.puserGoogleExpiresIn;
	}

	public void setPuserGoogleExpiresIn(Integer puserGoogleExpiresIn) {
		this.puserGoogleExpiresIn = puserGoogleExpiresIn;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PUSER_GOOGLE_GRANTED", length = 19)
	public Date getPuserGoogleGranted() {
		return this.puserGoogleGranted;
	}

	public void setPuserGoogleGranted(Date puserGoogleGranted) {
		this.puserGoogleGranted = puserGoogleGranted;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PUSER_CONTACT_LASTSYNC", length = 19)
	public Date getPuserContactLastsync() {
		return this.puserContactLastsync;
	}

	public void setPuserContactLastsync(Date puserContactLastsync) {
		this.puserContactLastsync = puserContactLastsync;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PUSER_CALENDAR_LASTSYNC", length = 19)
	public Date getPuserCalendarLastsync() {
		return this.puserCalendarLastsync;
	}

	@Column(name = "PUSER_CALENDAR_SYNC_DURATION", nullable = false)
	public int getPuserCalendarSyncDuration() {
		return puserCalendarSyncDuration;
	}
	
	public void setPuserCalendarLastsync(Date puserCalendarLastsync) {
		this.puserCalendarLastsync = puserCalendarLastsync;
	}
	

	@Column(name = "PUSER_CONTACT_SYNC_DURATION", nullable = false)
	public int getPuserContactSyncDuration() {
		return puserContactSyncDuration;
	}

	public void setPuserContactSyncDuration(int puserContactSyncDuration) {
		this.puserContactSyncDuration = puserContactSyncDuration;
	}


	public void setPuserCalendarSyncDuration(int puserCalendarSyncDuration) {
		this.puserCalendarSyncDuration = puserCalendarSyncDuration;
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
		wrap.puserContactSyncDuration = puserContactSyncDuration;
		wrap.puserCalendarSyncDuration = puserCalendarSyncDuration; 
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
		puserContactSyncDuration = data.puserContactSyncDuration;
		puserCalendarSyncDuration = data.puserCalendarSyncDuration; 
	}

	@Override
	public void fetch(boolean deep) {
		Hibernate.initialize(owner);
	}
}
