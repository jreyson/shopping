package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_area")
public class AreaApi extends IdEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String areaName;
	private int sequence;
	   private int level;
	   @ManyToOne
	   private AreaApi parent;
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public AreaApi getParent() {
		return parent;
	}
	public void setParent(AreaApi parent) {
		this.parent = parent;
	}
	   
}
