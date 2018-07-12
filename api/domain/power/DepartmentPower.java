package com.shopping.api.domain.power;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_departmentPower")
public class DepartmentPower extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>用户查看部门未任命的人员权限
	 */
	private static final long serialVersionUID = 1L;
	@OneToOne
	private User user;
	public DepartmentPower() {
		super();
	}
	public DepartmentPower(Date addTime) {
		super(addTime);
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
