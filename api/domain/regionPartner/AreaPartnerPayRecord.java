package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_areaPartnerPayRecord")
public class AreaPartnerPayRecord extends IdEntity {

	/**
	 * 区域合伙人生成购买信息临时表
	 */
	private static final long serialVersionUID = 1L;
	private String orderNum;// 随机订单号
	private String payType;// 支付类型
	private Integer payStatus;// 支付状态
	private Date payTime;// 支付时间
	private Double payMoney;// 支付金额
	@ManyToOne
	private User user;// 购买用户
	@ManyToOne
	private AreaGradeOfUser areaGradeOfUser;// 所属地区
	@ManyToOne
	private AreaSiteRankConfig areaSiteRankConfig;// 购买类型
	@ManyToOne
	private BuMen buMen;
	@Column(columnDefinition="bit COMMENT '奖励是否已经发放'")
	private boolean rewardStatus;
	@Column(columnDefinition="varchar(20) COMMENT '拓展经费'")
	private Double funds;
	public AreaPartnerPayRecord() {
		super();
	}

	public AreaPartnerPayRecord(Date addTime) {
		super(addTime);
	}

	public AreaPartnerPayRecord(Date addTime, String orderNum,
			Integer payStatus, Double payMoney, User user,
			AreaGradeOfUser areaGradeOfUser, BuMen buMen,
			AreaSiteRankConfig areaSiteRankConfig) {
		super(addTime);
		this.orderNum = orderNum;
		this.payStatus = payStatus;
		this.payMoney = payMoney;
		this.user = user;
		this.areaGradeOfUser = areaGradeOfUser;
		this.areaSiteRankConfig = areaSiteRankConfig;
		this.buMen = buMen;
	}

	public String getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Double getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(Double payMoney) {
		this.payMoney = payMoney;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public AreaGradeOfUser getAreaGradeOfUser() {
		return areaGradeOfUser;
	}

	public void setAreaGradeOfUser(AreaGradeOfUser areaGradeOfUser) {
		this.areaGradeOfUser = areaGradeOfUser;
	}

	public AreaSiteRankConfig getAreaSiteRankConfig() {
		return areaSiteRankConfig;
	}

	public void setAreaSiteRankConfig(AreaSiteRankConfig areaSiteRankConfig) {
		this.areaSiteRankConfig = areaSiteRankConfig;
	}

	public BuMen getBuMen() {
		return buMen;
	}

	public void setBuMen(BuMen buMen) {
		this.buMen = buMen;
	}

	public boolean isRewardStatus() {
		return rewardStatus;
	}

	public void setRewardStatus(boolean rewardStatus) {
		this.rewardStatus = rewardStatus;
	}

	public Double getFunds() {
		return funds;
	}

	public void setFunds(Double funds) {
		this.funds = funds;
	}
}
