package com.shopping.api.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_auction_details")
public class AuctionDetailsApi extends IdEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:商品拍卖详情的实体类
	 */
	private static final long serialVersionUID = 1L;
	public AuctionDetailsApi(){
		
	}
	private Double add_range;//每次加价的幅度
	private BigDecimal start_auction_price;//拍卖品的起拍价
	private BigDecimal reserve_auction_price;//拍卖品的保留价
	private BigDecimal current_auction_price;//拍卖品的当前价格
	@Column(columnDefinition = "INT(3) default 0")
	private int auction_status;//拍卖品的状态0表示正在拍卖,1,表示拍卖品流拍2,表示拍卖品已成功被拍走
	private Date auction_finish_time;//拍卖品的完成时间,由卖家自己设定
	@ManyToMany(fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.REMOVE })
	private List<User> joined_user;//拍卖品加入的用户(报名的用户)
	private Double ensure_gold;//拍卖所需要的保证金,由卖家自己设定,拍卖此次产品,买家需要缴纳多少钱的保证金
	private int is_required_gold;//是否必须要保证金0表示需要不需要保证金,1,需要保证金
	@Column(columnDefinition = "INT(3) default 0")
	private int add_price_times;
	public Double getAdd_range() {
		return add_range;
	}
	public void setAdd_range(Double add_range) {
		this.add_range = add_range;
	}
	public int getAdd_price_times() {
		return add_price_times;
	}
	public void setAdd_price_times(int add_price_times) {
		this.add_price_times = add_price_times;
	}
	public int getIs_required_gold() {
		return is_required_gold;
	}
	public void setIs_required_gold(int is_required_gold) {
		this.is_required_gold = is_required_gold;
	}
	public Double getEnsure_gold() {
		return ensure_gold;
	}
	public void setEnsure_gold(Double ensure_gold) {
		this.ensure_gold = ensure_gold;
	}
	public int getAuction_status() {
		return auction_status;
	}
	public void setAuction_status(int auction_status) {
		this.auction_status = auction_status;
	}
	public BigDecimal getStart_auction_price() {
		return start_auction_price;
	}
	public void setStart_auction_price(BigDecimal start_auction_price) {
		this.start_auction_price = start_auction_price;
	}
	public BigDecimal getReserve_auction_price() {
		return reserve_auction_price;
	}
	public void setReserve_auction_price(BigDecimal reserve_auction_price) {
		this.reserve_auction_price = reserve_auction_price;
	}
	public BigDecimal getCurrent_auction_price() {
		return current_auction_price;
	}
	public void setCurrent_auction_price(BigDecimal current_auction_price) {
		this.current_auction_price = current_auction_price;
	}
	public Date getAuction_finish_time() {
		return auction_finish_time;
	}
	public void setAuction_finish_time(Date auction_finish_time) {
		this.auction_finish_time = auction_finish_time;
	}
	public List<User> getJoined_user() {
		return joined_user;
	}
	public void setJoined_user(List<User> joined_user) {
		this.joined_user = joined_user;
	}
}
