package com.shopping.api.domain.countBuy;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_count_price")
public class CountPriceDomain extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:app购买点数的实体类 
	 */
	private static final long serialVersionUID = 1L;
	public CountPriceDomain(){
		
	}
	private double current_price;//当前价
	private double quondam_price;//原来的价
	private int counts;//点数
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public double getCurrent_price() {
		return current_price;
	}
	public void setCurrent_price(double current_price) {
		this.current_price = current_price;
	}
	public double getQuondam_price() {
		return quondam_price;
	}
	public void setQuondam_price(double quondam_price) {
		this.quondam_price = quondam_price;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
}
