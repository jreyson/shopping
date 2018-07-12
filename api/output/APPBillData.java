package com.shopping.api.output;

import java.io.Serializable;

import com.shopping.foundation.domain.User;

public class APPBillData implements Serializable{
	/**
	 * @author:gaohao
	 * @description:输出app端月收入清单
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 9121748155113845959L;
	private Object chubeiMoney;//储备金
	private Object daogouMoney;//导购金
	private Object danbaoMoney;//担保金
	private Object zhaoshangMoney;//招商金
	private Object xianjiMoney;//衔级金
	private Object fenhongMoney;//分红金
	private Object zengguMoney;//赠股金
	private Object moneySum;//总收入
	private Object userHuoKuan;//卖家货款
	private Object userExpenditure;//提现
	private User user;
	public Object getChubeiMoney() {
		return chubeiMoney;
	}
	public void setChubeiMoney(Object chubeiMoney) {
		this.chubeiMoney = chubeiMoney;
	}
	public Object getDaogouMoney() {
		return daogouMoney;
	}
	public void setDaogouMoney(Object daogouMoney) {
		this.daogouMoney = daogouMoney;
	}
	public Object getDanbaoMoney() {
		return danbaoMoney;
	}
	public void setDanbaoMoney(Object danbaoMoney) {
		this.danbaoMoney = danbaoMoney;
	}
	public Object getZhaoshangMoney() {
		return zhaoshangMoney;
	}
	public void setZhaoshangMoney(Object zhaoshangMoney) {
		this.zhaoshangMoney = zhaoshangMoney;
	}
	public Object getXianjiMoney() {
		return xianjiMoney;
	}
	public void setXianjiMoney(Object xianjiMoney) {
		this.xianjiMoney = xianjiMoney;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Object getMoneySum() {
		return moneySum;
	}
	public void setMoneySum(Object moneySum) {
		this.moneySum = moneySum;
	}
	public Object getUserHuoKuan() {
		return userHuoKuan;
	}
	public void setUserHuoKuan(Object userHuoKuan) {
		this.userHuoKuan = userHuoKuan;
	}
	public Object getUserExpenditure() {
		return userExpenditure;
	}
	public void setUserExpenditure(Object userExpenditure) {
		this.userExpenditure = userExpenditure;
	}
	public Object getFenhongMoney() {
		return fenhongMoney;
	}
	public void setFenhongMoney(Object fenhongMoney) {
		this.fenhongMoney = fenhongMoney;
	}
	public Object getZengguMoney() {
		return zengguMoney;
	}
	public void setZengguMoney(Object zengguMoney) {
		this.zengguMoney = zengguMoney;
	}
}
