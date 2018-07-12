package com.shopping.api.domain.integralRecharge;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appMySelf_integralRecharge")
public class IntegralRechargeEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:app==>我的模块里面用于实现支付宝和微信的积分充值功能
	 */
	private static final long serialVersionUID = 1L;
	public IntegralRechargeEntity(){
		super();
	}
	private Long runningWaterNum;//流水号
	private Integer orderStatus;//订单状态
	private Double rechargeQuantity;//充值金额大小
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;//充值用户
	private String rechargeExplain;//充值说明
	private String rechargeWay;//充值方式:微信充值还是支付宝充值,weixin/alipay
	public String getRechargeExplain() {
		return rechargeExplain;
	}
	public void setRechargeExplain(String rechargeExplain) {
		this.rechargeExplain = rechargeExplain;
	}
	public String getRechargeWay() {
		return rechargeWay;
	}
	public void setRechargeWay(String rechargeWay) {
		this.rechargeWay = rechargeWay;
	}
	public Long getRunningWaterNum() {
		return runningWaterNum;
	}
	public void setRunningWaterNum(Long runningWaterNum) {
		this.runningWaterNum = runningWaterNum;
	}
	public Integer getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}
	public Double getRechargeQuantity() {
		return rechargeQuantity;
	}
	public void setRechargeQuantity(Double rechargeQuantity) {
		this.rechargeQuantity = rechargeQuantity;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
