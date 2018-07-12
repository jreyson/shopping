package com.shopping.api.domain.power;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_app_seedatapower")
public class AppSeeDataPower extends IdEntity{

	/**
	  * @author:gaohao
	 * @description:app==>用户查看数据实况的权限
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private User user;
	public AppSeeDataPower() {
		super();
	}
	public AppSeeDataPower(Date addTime) {
		super(addTime);
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
