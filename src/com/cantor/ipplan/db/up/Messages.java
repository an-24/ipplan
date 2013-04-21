package com.cantor.ipplan.db.up;

// Generated 20.04.2013 18:23:50 by Hibernate Tools 3.4.0.CR1

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
import javax.persistence.Transient;

import com.cantor.ipplan.core.DataBridge;

/**
 * Messages generated by hbm2java
 */
@Entity
@Table(name = "MESSAGES")
public class Messages implements java.io.Serializable {

	@Transient
	public static final int MT_JOIN_TO_OWNER = 0;
	
	private int messagesId;
	private PUser puserByPuserRId;
	private PUser puserByPuserSId;
	private Date messagesDate;
	private String messagesText;
	private int messagesType;

	public Messages() {
	}

	public Messages(int messagesId, PUser puserByPuserSId, Date messagesDate,
			String messagesText, int messagesType) {
		this.messagesId = messagesId;
		this.puserByPuserSId = puserByPuserSId;
		this.messagesDate = messagesDate;
		this.messagesText = messagesText;
		this.messagesType = messagesType;
	}

	public Messages(int messagesId, PUser puserByPuserRId,
			PUser puserByPuserSId, Date messagesDate, String messagesText,
			int messagesType) {
		this.messagesId = messagesId;
		this.puserByPuserRId = puserByPuserRId;
		this.puserByPuserSId = puserByPuserSId;
		this.messagesDate = messagesDate;
		this.messagesText = messagesText;
		this.messagesType = messagesType;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "MESSAGES_ID", unique = true, nullable = false)
	public int getMessagesId() {
		return this.messagesId;
	}

	public void setMessagesId(int messagesId) {
		this.messagesId = messagesId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_R_ID")
	public PUser getPuserByPuserRId() {
		return this.puserByPuserRId;
	}

	public void setPuserByPuserRId(PUser puserByPuserRId) {
		this.puserByPuserRId = puserByPuserRId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_S_ID", nullable = false)
	public PUser getPuserByPuserSId() {
		return this.puserByPuserSId;
	}

	public void setPuserByPuserSId(PUser puserByPuserSId) {
		this.puserByPuserSId = puserByPuserSId;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "MESSAGES_DATE", nullable = false, length = 10)
	public Date getMessagesDate() {
		return this.messagesDate;
	}

	public void setMessagesDate(Date messagesDate) {
		this.messagesDate = messagesDate;
	}

	@Column(name = "MESSAGES_TEXT", nullable = false, length = 256)
	public String getMessagesText() {
		return this.messagesText;
	}

	public void setMessagesText(String messagesText) {
		this.messagesText = messagesText;
	}

	@Column(name = "MESSAGES_TYPE", nullable = false)
	public int getMessagesType() {
		return this.messagesType;
	}

	public void setMessagesType(int messagesType) {
		this.messagesType = messagesType;
	}

}
