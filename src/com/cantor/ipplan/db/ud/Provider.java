package com.cantor.ipplan.db.ud;

// Generated 08.07.2013 10:15:45 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Provider generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "PROVIDER", uniqueConstraints = @UniqueConstraint(columnNames = "PROVIDER_NAME"))
public class Provider implements java.io.Serializable {

	private int providerId;
	private String providerName;
	private String providerToken;
	private String providerRefreshToken;
	private Integer providerExpiresIn;
	private Date providerGranted;

	public Provider() {
	}

	public Provider(int providerId, String providerName) {
		this.providerId = providerId;
		this.providerName = providerName;
	}

	public Provider(int providerId, String providerName, String providerToken,
			String providerRefreshToken, Integer providerExpiresIn,
			Date providerGranted, Set<Filelinks> filelinkses) {
		this.providerId = providerId;
		this.providerName = providerName;
		this.providerToken = providerToken;
		this.providerRefreshToken = providerRefreshToken;
		this.providerExpiresIn = providerExpiresIn;
		this.providerGranted = providerGranted;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "PROVIDER_ID", unique = true, nullable = false)
	public int getProviderId() {
		return this.providerId;
	}

	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}

	@Column(name = "PROVIDER_NAME", unique = true, nullable = false, length = 60)
	public String getProviderName() {
		return this.providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	@Column(name = "PROVIDER_TOKEN", length = 100)
	public String getProviderToken() {
		return this.providerToken;
	}

	public void setProviderToken(String providerToken) {
		this.providerToken = providerToken;
	}

	@Column(name = "PROVIDER_REFRESH_TOKEN", length = 100)
	public String getProviderRefreshToken() {
		return this.providerRefreshToken;
	}

	public void setProviderRefreshToken(String providerRefreshToken) {
		this.providerRefreshToken = providerRefreshToken;
	}

	@Column(name = "PROVIDER_EXPIRES_IN")
	public Integer getProviderExpiresIn() {
		return this.providerExpiresIn;
	}

	public void setProviderExpiresIn(Integer providerExpiresIn) {
		this.providerExpiresIn = providerExpiresIn;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PROVIDER_GRANTED", length = 19, nullable = true)
	public Date getProviderGranted() {
		return this.providerGranted;
	}

	public void setProviderGranted(Date providerGranted) {
		this.providerGranted = providerGranted;
	}

}
