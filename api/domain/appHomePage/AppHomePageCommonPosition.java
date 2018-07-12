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
@Table(name = "shopping_appHome_commonPosition")
public class AppHomePageCommonPosition extends IdEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:app首页普通空位的购买
	 */
	private static final long serialVersionUID = 1L;
	public AppHomePageCommonPosition(){
		
	}
	private String sequence;//common位序列
	@OneToOne(fetch = FetchType.LAZY)
	private Goods goods;//需要展示的商品(包括banner和普通位)
	@OneToOne(fetch = FetchType.LAZY)
	private Goods defaultGoods;//需要展示的默认商品
	@Column(name="purchase_timeDuan", columnDefinition = "INT(3) default 0 not null",nullable = false)
	private int purchase_timeDuan;//付费购买的天数
	@Temporal(TemporalType.TIMESTAMP)
	private Date start_time;//开始时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date flush_time;//付费到期关闭的时间点
	private String position_name;//位置名字,比如banner位置1,banner位置2
	private boolean is_can_buy;//标志该空位是否可以购买
	private double commonPosition_price;//banner位价钱
	public Date getFlush_time() {
		return flush_time;
	}
	public Goods getDefaultGoods() {
		return defaultGoods;
	}
	public void setDefaultGoods(Goods defaultGoods) {
		this.defaultGoods = defaultGoods;
	}
	public void setFlush_time(Date flush_time) {
		this.flush_time = flush_time;
	}
	public double getCommonPosition_price() {
		return commonPosition_price;
	}
	public void setCommonPosition_price(double commonPosition_price) {
		this.commonPosition_price = commonPosition_price;
	}
	public boolean getIs_can_buy() {
		return is_can_buy;
	}
	public void setIs_can_buy(boolean is_can_buy) {
		this.is_can_buy = is_can_buy;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public int getPurchase_timeDuan() {
		return purchase_timeDuan;
	}
	public void setPurchase_timeDuan(int purchase_timeDuan) {
		this.purchase_timeDuan = purchase_timeDuan;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
}
