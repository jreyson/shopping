package com.shopping.api.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.ApiIdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_goods")
public class GoodsApi extends ApiIdEntity implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String goods_name;
	private String goods_details;
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_price;
	private String goods_store_id;
	private int goods_status;
	private double ctj;
	
	public String getGoods_store_id() {
		return goods_store_id;
	}
	public void setGoods_store_id(String goods_store_id) {
		this.goods_store_id = goods_store_id;
	}
	public String getGoods_name() {
		return goods_name;
	}
	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}
	public String getGoods_details() {
		return goods_details;
	}
	public void setGoods_details(String goods_details) {
		this.goods_details = goods_details;
	}
	public BigDecimal getGoods_price() {
		return goods_price;
	}
	public void setGoods_price(BigDecimal goods_price) {
		this.goods_price = goods_price;
	}
	public int getGoods_status() {
		return goods_status;
	}
	public void setGoods_status(int goods_status) {
		this.goods_status = goods_status;
	}
	public double getCtj() {
		return ctj;
	}
	public void setCtj(double ctj) {
		this.ctj = ctj;
	}

	
}
