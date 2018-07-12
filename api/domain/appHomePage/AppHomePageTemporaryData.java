package com.shopping.api.domain.appHomePage;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appHome_temporaryData")
public class AppHomePageTemporaryData extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:用于记录app首页支付宝和微信支付时所需要的临时数据
	 */
	private static final long serialVersionUID = 1L;
	public AppHomePageTemporaryData(){
		
	}
	private long goodsId;//需要展示的商品(包括banner和普通位)
	@Column(name="purchase_timeDuan", columnDefinition = "INT(3) default 0 not null",nullable = false)
	private int purchase_timeDuan;//付费购买的天数
	@Temporal(TemporalType.TIMESTAMP)
	private Date start_time;//开始时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date flush_time;//付费到期关闭的时间点
	private boolean is_can_buy;//标志该空位是否可以购买
	private String vacantPositionType;//标记购买那种类型,是banner位还是common位
	private long vacantPositionId;
	private double total;//要付钱的总价
	@Column(columnDefinition = "INT(3) default 0")
	private int orderStatus;
	private String orderNum;//订单号
	public String getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public long getVacantPositionId() {
		return vacantPositionId;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getVacantPositionType() {
		return vacantPositionType;
	}
	public void setVacantPositionType(String vacantPositionType) {
		this.vacantPositionType = vacantPositionType;
	}
	public void setVacantPositionId(long vacantPositionId) {
		this.vacantPositionId = vacantPositionId;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
	public int getPurchase_timeDuan() {
		return purchase_timeDuan;
	}
	public void setPurchase_timeDuan(int purchase_timeDuan) {
		this.purchase_timeDuan = purchase_timeDuan;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Date getFlush_time() {
		return flush_time;
	}
	public void setFlush_time(Date flush_time) {
		this.flush_time = flush_time;
	}
	public boolean getIs_can_buy() {
		return is_can_buy;
	}
	public void setIs_can_buy(boolean is_can_buy) {
		this.is_can_buy = is_can_buy;
	}
}
