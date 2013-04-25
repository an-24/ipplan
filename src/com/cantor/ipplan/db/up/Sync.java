package com.cantor.ipplan.db.up;

// Generated 13.04.2013 9:53:14 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.Hibernate;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.shared.SyncWrapper;

/**
 * Sync generated by hbm2java
 */
@Entity
@Table(name = "SYNC")
public class Sync implements java.io.Serializable, DataBridge<SyncWrapper>  {

	private int syncId;
	private PUser puser;
	private String syncImei;
	private Date syncLast;

	public Sync() {
	}

	public Sync(int syncId, PUser puser, String syncImei, Date syncLast) {
		this.syncId = syncId;
		this.puser = puser;
		this.syncImei = syncImei;
		this.syncLast = syncLast;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "SYNC_ID", unique = true, nullable = false)
	public int getSyncId() {
		return this.syncId;
	}

	public void setSyncId(int syncId) {
		this.syncId = syncId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_ID", nullable = false)
	public PUser getPuser() {
		return this.puser;
	}

	public void setPuser(PUser puser) {
		this.puser = puser;
	}

	@Column(name = "SYNC_IMEI", nullable = false, length = 20)
	public String getSyncImei() {
		return this.syncImei;
	}

	public void setSyncImei(String syncImei) {
		this.syncImei = syncImei;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "SYNC_LAST", nullable = false, length = 10)
	public Date getSyncLast() {
		return this.syncLast;
	}

	public void setSyncLast(Date syncLast) {
		this.syncLast = syncLast;
	}

	@Override
	public SyncWrapper toClient() {
		SyncWrapper wrapper =  new SyncWrapper(this.syncId,
				this.syncImei,this.syncLast);
		return wrapper;
	}

	@Override
	public void fromClient(SyncWrapper data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetch(boolean deep) {
		Hibernate.initialize(this.getPuser());
	}

}
