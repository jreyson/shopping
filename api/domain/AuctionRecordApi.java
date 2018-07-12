package com.shopping.api.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_auction_record")
public class AuctionRecordApi extends IdEntity implements Serializable{

	/**
	 * @author:akangah
	 * @description:商品拍卖记录的实体类
	 */
	private static final long serialVersionUID = 1L;
	public AuctionRecordApi(){
		
	}
	@OneToOne(cascade = { javax.persistence.CascadeType.REMOVE })
	private User contend_user;//当前加价的用户
	private Date plus_price_time;//加价的时间
	private BigDecimal current_auction_price;//拍卖品的当前价格
	public User getContend_user() {
		return contend_user;
	}
	public void setContend_user(User contend_user) {
		this.contend_user = contend_user;
	}
	public Date getPlus_price_time() {
		return plus_price_time;
	}
	public void setPlus_price_time(Date plus_price_time) {
		this.plus_price_time = plus_price_time;
	}
	public BigDecimal getCurrent_auction_price() {
		return current_auction_price;
	}
	public void setCurrent_auction_price(BigDecimal current_auction_price) {
		this.current_auction_price = current_auction_price;
	}
}
