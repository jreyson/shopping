package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_areaCommonposition")
public class AreaCommonposition extends IdEntity{

	/**
	 * 区域顺序位
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(20) COMMENT 'Banner位序列'")
	private String sequence;//Common位序列
	@OneToOne(fetch = FetchType.LAZY)
	private Goods goods;//需要展示的商品(包括banner和普通位)
	@OneToOne(fetch = FetchType.LAZY)
	private Goods defaultGoods;//需要展示的默认商品
	@Column(name="purchase_timeDuan", columnDefinition = "INT(3) default 0 not null",nullable = false)
	private int purchase_timeDuan;//付费购买的数量
	@Temporal(TemporalType.TIMESTAMP)
	private Date start_time;//开始时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date flush_time;//付费到期关闭的时间点
	private String position_name;//位置名字,比如Common位置1,Common位置2
	private boolean is_can_buy;//标志该空位是否可以购买
	private double commonPosition_price;//Common位价钱
	@ManyToOne(fetch = FetchType.LAZY)
	private AreaHomePageConfig areaHomePageConfig;//付费上首页配置
	public AreaCommonposition() {
		super();
	}
	public AreaCommonposition(Date addTime) {
		super(addTime);
	}
	public AreaCommonposition(Goods goods, int purchase_timeDuan,
			Date start_time, Date flush_time, boolean is_can_buy) {
		super();
		this.goods = goods;
		this.purchase_timeDuan = purchase_timeDuan;
		this.start_time = start_time;
		this.flush_time = flush_time;
		this.is_can_buy = is_can_buy;
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
	public Goods getDefaultGoods() {
		return defaultGoods;
	}
	public void setDefaultGoods(Goods defaultGoods) {
		this.defaultGoods = defaultGoods;
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
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public boolean isIs_can_buy() {
		return is_can_buy;
	}
	public void setIs_can_buy(boolean is_can_buy) {
		this.is_can_buy = is_can_buy;
	}
	public double getCommonPosition_price() {
		return commonPosition_price;
	}
	public void setCommonPosition_price(double commonPosition_price) {
		this.commonPosition_price = commonPosition_price;
	}
	public AreaHomePageConfig getAreaHomePageConfig() {
		return areaHomePageConfig;
	}
	public void setAreaHomePageConfig(AreaHomePageConfig areaHomePageConfig) {
		this.areaHomePageConfig = areaHomePageConfig;
	}
}
