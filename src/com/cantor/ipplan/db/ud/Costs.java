package com.cantor.ipplan.db.ud;

// Generated 12.04.2013 19:58:46 by Hibernate Tools 3.4.0.CR1

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.shared.CostsWrapper;

/**
 * Costs generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "COSTS", uniqueConstraints = @UniqueConstraint(columnNames = "COSTS_NAME"))
public class Costs implements java.io.Serializable, DataBridge<CostsWrapper> {

	private int costsId;
	private int costsSortcode;
	private String costsName;

	public Costs() {
	}

	public Costs(int costsId, int costsSortcode, String costsName) {
		this.costsId = costsId;
		this.costsSortcode = costsSortcode;
		this.costsName = costsName;
	}

	public Costs(int costsId, int costsSortcode, String costsName,
			Set<Bargaincosts> bargaincostses) {
		this.costsId = costsId;
		this.costsSortcode = costsSortcode;
		this.costsName = costsName;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "COSTS_ID", unique = true, nullable = false)
	public int getCostsId() {
		return this.costsId;
	}

	public void setCostsId(int costsId) {
		this.costsId = costsId;
	}

	@Column(name = "COSTS_SORTCODE", nullable = false)
	public int getCostsSortcode() {
		return this.costsSortcode;
	}

	public void setCostsSortcode(int costsSortcode) {
		this.costsSortcode = costsSortcode;
	}

	@Column(name = "COSTS_NAME", unique = true, nullable = false, length = 120)
	public String getCostsName() {
		return this.costsName;
	}

	public void setCostsName(String costsName) {
		this.costsName = costsName;
	}

	@Override
	public CostsWrapper toClient() {
		CostsWrapper c = new CostsWrapper();
		c.costsId = costsId;
		c.costsName = costsName;
		c.costsSortcode = c.costsSortcode;
		return c;
	}

	@Override
	public void fromClient(CostsWrapper data) {
		costsId = data.costsId;
		costsName = data.costsName;
		costsSortcode = data.costsSortcode;
	}

	@Override
	public void fetch(boolean deep) {
	}


}
