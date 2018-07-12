package com.shopping.api.domain.integralDeposit;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "shopping_appMySelf_integralDeposit")
public class IntegralDepositEntity extends IdEntity implements Serializable{
	/**
	 * @author:gaohao
	 * @description:app==>用户理财实体类
	 */
	private static final long serialVersionUID = 1L;
	private Long depositOrderNum;//流水号
	private Integer orderStatus;//订单状态
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;//理财用户
	@ManyToOne(fetch = FetchType.LAZY)
	private IntegralDepositListEntity integralDepositListEntity;//理财类型
	private Integer depositQuantity;//存放金额
	private Date endTime;//到期时间
	private Integer depositStatus;//到期状态(0:未到期；1：到期)
	private Double depositInterest;//回报利息
	private Double depositAll;//回报本息
	private String rechargeWay;//充值方式:微信充值还是支付宝充值,weixin/alipay
	
	public IntegralDepositEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IntegralDepositEntity(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public Long getDepositOrderNum() {
		return depositOrderNum;
	}
	public void setDepositOrderNum(Long depositOrderNum) {
		this.depositOrderNum = depositOrderNum;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public IntegralDepositListEntity getIntegralDepositListEntity() {
		return integralDepositListEntity;
	}
	public void setIntegralDepositListEntity(
			IntegralDepositListEntity integralDepositListEntity) {
		this.integralDepositListEntity = integralDepositListEntity;
	}
	public Integer getDepositQuantity() {
		return depositQuantity;
	}
	public void setDepositQuantity(Integer depositQuantity) {
		this.depositQuantity = depositQuantity;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Integer getDepositStatus() {
		return depositStatus;
	}
	public void setDepositStatus(Integer depositStatus) {
		this.depositStatus = depositStatus;
	}
	public Double getDepositInterest() {
		return depositInterest;
	}
	public void setDepositInterest(Double depositInterest) {
		this.depositInterest = depositInterest;
	}
	public Double getDepositAll() {
		return depositAll;
	}
	public void setDepositAll(Double depositAll) {
		this.depositAll = depositAll;
	}
	public Integer getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getRechargeWay() {
		return rechargeWay;
	}
	public void setRechargeWay(String rechargeWay) {
		this.rechargeWay = rechargeWay;
	}
	
}
