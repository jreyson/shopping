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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appMySelf_integralDepositName")
public class IntegralDepositListEntity extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>我的模块里面用于获取理财类型以及利率
	 */
	private static final long serialVersionUID = 1L;
	private Double annualRate;//年利率
	private Double dailyRate;//日利率
	private Integer days;//天数
	private String title;//理财标题描述(第一商城3月理财)
	private String purchaseThreshold;//购买门槛
	private String riskInfo;//风险描述(中低风险)
	private String annualInfo;//年化描述(七日年化)
	@ManyToOne(fetch = FetchType.LAZY)
	private IntegralDepositInfo integralDepositInfo;
	
	public IntegralDepositListEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IntegralDepositListEntity(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public Double getAnnualRate() {
		return annualRate;
	}
	public void setAnnualRate(Double annualRate) {
		this.annualRate = annualRate;
	}
	public Double getDailyRate() {
		return dailyRate;
	}
	public void setDailyRate(Double dailyRate) {
		this.dailyRate = dailyRate;
	}
	public Integer getDays() {
		return days;
	}
	public void setDays(Integer days) {
		this.days = days;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPurchaseThreshold() {
		return purchaseThreshold;
	}
	public void setPurchaseThreshold(String purchaseThreshold) {
		this.purchaseThreshold = purchaseThreshold;
	}
	public String getRiskInfo() {
		return riskInfo;
	}
	public void setRiskInfo(String riskInfo) {
		this.riskInfo = riskInfo;
	}
	public String getAnnualInfo() {
		return annualInfo;
	}
	public void setAnnualInfo(String annualInfo) {
		this.annualInfo = annualInfo;
	}
	public IntegralDepositInfo getIntegralDepositInfo() {
		return integralDepositInfo;
	}
	public void setIntegralDepositInfo(IntegralDepositInfo integralDepositInfo) {
		this.integralDepositInfo = integralDepositInfo;
	}
}
