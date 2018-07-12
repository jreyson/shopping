package com.shopping.api.domain.userBill;

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
@Table(name = "shopping_user_monthly_bill")
public class UserMonthlyBill extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>用户月账单统计
	 */
	private static final long serialVersionUID = -6323332205070982912L;
	private Double chubeiMoney;//储备金
	private Double daogouMoney;//导购金
	private Double danbaoMoney;//担保金
	private Double zhaoshangMoney;//招商金
	private Double xianjiMoney;//衔级金
	private Double fenhongMoney;//分红金
	private Double zengguMoney;//赠股金
	private Double moneySum;//总收入
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	public UserMonthlyBill() {
		super();
	}
	public Object getChubeiMoney() {
		return chubeiMoney;
	}
	public Double getDaogouMoney() {
		return daogouMoney;
	}
	public void setDaogouMoney(Double daogouMoney) {
		this.daogouMoney = daogouMoney;
	}
	public Double getDanbaoMoney() {
		return danbaoMoney;
	}
	public void setDanbaoMoney(Double danbaoMoney) {
		this.danbaoMoney = danbaoMoney;
	}
	public Double getZhaoshangMoney() {
		return zhaoshangMoney;
	}
	public void setZhaoshangMoney(Double zhaoshangMoney) {
		this.zhaoshangMoney = zhaoshangMoney;
	}
	public Double getXianjiMoney() {
		return xianjiMoney;
	}
	public void setXianjiMoney(Double xianjiMoney) {
		this.xianjiMoney = xianjiMoney;
	}
	public Double getMoneySum() {
		return moneySum;
	}
	public void setMoneySum(Double moneySum) {
		this.moneySum = moneySum;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setChubeiMoney(Double chubeiMoney) {
		this.chubeiMoney = chubeiMoney;
	}
	public Double getFenhongMoney() {
		return fenhongMoney;
	}
	public void setFenhongMoney(Double fenhongMoney) {
		this.fenhongMoney = fenhongMoney;
	}
	public Double getZengguMoney() {
		return zengguMoney;
	}
	public void setZengguMoney(Double zengguMoney) {
		this.zengguMoney = zengguMoney;
	}
	
}
