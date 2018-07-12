package com.shopping.api.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_payment_userSelfPaymentDefault")
public class UserSelfPaymentDefault extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:用户自己支付方式的实体类
	 */
	private static final long serialVersionUID = 1L;
	public UserSelfPaymentDefault(){
		super();
	}
	public UserSelfPaymentDefault(Date date,int is_default,User user,PaymentWayVariety paymentWayDefault){
		super.setAddTime(date);
		super.setDeleteStatus(false);
		this.is_default=is_default;
		this.user=user;
		this.paymentWayVariety=paymentWayDefault;
	}
	@Column(columnDefinition = "INT(3) default 0")
	private int is_default;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private PaymentWayVariety paymentWayVariety;
	public int getIs_default() {
		return is_default;
	}
	public void setIs_default(int is_default) {
		this.is_default = is_default;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public PaymentWayVariety getPaymentWayVariety() {
		return paymentWayVariety;
	}
	public void setPaymentWayVariety(PaymentWayVariety paymentWayVariety) {
		this.paymentWayVariety = paymentWayVariety;
	}
	
}
