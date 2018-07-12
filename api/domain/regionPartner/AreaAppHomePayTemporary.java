package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_areaAppHomePayTemporary")
public class AreaAppHomePayTemporary extends IdEntity{

	/**
	 * 区域首页付费购买记录表
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private Goods goods;//需要展示的商品(包括banner和普通位)
	@Column(name="purchase_timeDuan", columnDefinition = "INT(3) default 0 not null",nullable = false)
	private int purchase_timeDuan;//付费购买的数量
	@Temporal(TemporalType.TIMESTAMP)
	private Date start_time;//开始时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date flush_time;//付费到期关闭的时间点
	private String vacantPositionType;//标记购买那种类型,是banner位还是common位
	private double total;//要付钱的总价
	@Column(columnDefinition = "INT(3) default 0")
	private int orderStatus;//付费状态
	private String payType;//付费类型
	private String orderNum;//订单号
	private Date payTime;//支付时间
	@ManyToOne
	private AreaBannerposition areaBannerposition;
	@ManyToOne
	private User user;//购买的用户
	@ManyToOne
	private AreaCommonposition areaCommonposition;
	@ManyToOne
	private User partnerUser;//区域合伙人
	@ManyToOne
	private AreaHomePageConfig areaHomePageConfig;
	private Date closeTime;//订单失效时间
	public AreaAppHomePayTemporary() {
		super();
	}
	public AreaAppHomePayTemporary(Date addTime) {
		super(addTime);
	}
	public AreaAppHomePayTemporary(Goods goods, int purchase_timeDuan,
			Date start_time, Date flush_time, String vacantPositionType,
			double total, int orderStatus, String payType,Date addTime,User user) {
		super(addTime);
		this.goods = goods;
		this.purchase_timeDuan = purchase_timeDuan;
		this.start_time = start_time;
		this.flush_time = flush_time;
		this.vacantPositionType = vacantPositionType;
		this.total = total;
		this.orderStatus = orderStatus;
		this.payType = payType;
		this.user = user;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public String getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
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
	public String getVacantPositionType() {
		return vacantPositionType;
	}
	public void setVacantPositionType(String vacantPositionType) {
		this.vacantPositionType = vacantPositionType;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public AreaBannerposition getAreaBannerposition() {
		return areaBannerposition;
	}
	public void setAreaBannerposition(AreaBannerposition areaBannerposition) {
		this.areaBannerposition = areaBannerposition;
	}
	public AreaCommonposition getAreaCommonposition() {
		return areaCommonposition;
	}
	public void setAreaCommonposition(AreaCommonposition areaCommonposition) {
		this.areaCommonposition = areaCommonposition;
	}
	public User getPartnerUser() {
		return partnerUser;
	}
	public void setPartnerUser(User partnerUser) {
		this.partnerUser = partnerUser;
	}
	public AreaHomePageConfig getAreaHomePageConfig() {
		return areaHomePageConfig;
	}
	public void setAreaHomePageConfig(AreaHomePageConfig areaHomePageConfig) {
		this.areaHomePageConfig = areaHomePageConfig;
	}
	public Date getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}
}
