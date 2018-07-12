package com.shopping.api.domain.integralRecharge;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appMySelf_integralDenomination")
public class IntegralRechargeListEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:app==>我的模块里面用于获取支付宝和微信的积分充值的面额大小
	 */
	private static final long serialVersionUID = 1L;
	private int counts;//分数
	private double current_price;//钱数
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public double getCurrent_price() {
		return current_price;
	}
	public void setCurrent_price(double current_price) {
		this.current_price = current_price;
	}
}
