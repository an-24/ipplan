package com.cantor.ipplan.db.ud;

// Generated 12.04.2013 19:58:46 by Hibernate Tools 3.4.0.CR1

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.core.IdGetter;
import com.cantor.ipplan.shared.StatusWrapper;

/**
 * Status generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "STATUS")
public class Status implements java.io.Serializable,IdGetter,DataBridge<StatusWrapper> {

	private int statusId;
	private PUserIdent puser;
	private String statusName;
	private int statusDayLimit;

	public Status() {
	}

	public Status(int statusId, PUserIdent puser, String statusName, int statusDayLimit) {
		this.statusId = statusId;
		this.puser = puser;
		this.statusName = statusName;
		this.statusDayLimit = statusDayLimit;
	}

	public Status(int statusId, PUserIdent puser, String statusName,
			Set<Bargain> bargains) {
		this.statusId = statusId;
		this.puser = puser;
		this.statusName = statusName;
	}

	@Id
	@GenericGenerator(name="newRec", strategy="com.cantor.ipplan.core.IdGenerator")
	@GeneratedValue(generator = "newRec")
	@Column(name = "STATUS_ID", unique = true, nullable = false)
	public int getStatusId() {
		return this.statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_ID", nullable = false)
	public PUserIdent getPuser() {
		return this.puser;
	}

	public void setPuser(PUserIdent puser) {
		this.puser = puser;
	}

	@Column(name = "STATUS_NAME", nullable = false, length = 60)
	public String getStatusName() {
		return this.statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@Column(name = "STATUS_DAYLIMIT")
	public int getStatusDayLimit() {
		return statusDayLimit;
	}

	public void setStatusDayLimit(int statusDayLimit) {
		this.statusDayLimit = statusDayLimit;
	}


	@Transient
	@Override
	public int getId() {
		return statusId;
	}


	@Override
	public void fetch(boolean deep) {
		Hibernate.initialize(getPuser());
		
	}

	@Override
	public void fromClient(StatusWrapper data) {
		statusId = data.statusId;
		statusName = data.statusName;
		statusDayLimit = data.statusDayLimit;
		puser = new PUserIdent();
		puser.setPuserId(data.puser_owner_id);
	}

	@Override
	public StatusWrapper toClient() {
		StatusWrapper wrap = new StatusWrapper();
		wrap.statusId =statusId;
		wrap.statusName = statusName;
		wrap.puser_owner_id =  getPuser().getId();
		return wrap;
	}

}
