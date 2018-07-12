package com.shopping.api.domain.userBill;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_user_week_activity")
public class UserWeekActivity extends IdEntity{

	/**
	 * @author:gaohao
	 * @description:app==>统计每周的订单总金额以及邀请人数，为每周晋升职位做记录
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private User user;
	private Double moneySum;//订单总金额
	private Integer inviteNum;//邀请人数
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Double getMoneySum() {
		return moneySum;
	}
	public void setMoneySum(Double moneySum) {
		this.moneySum = moneySum;
	}
	public Integer getInviteNum() {
		return inviteNum;
	}
	public void setInviteNum(Integer inviteNum) {
		this.inviteNum = inviteNum;
	}
}
