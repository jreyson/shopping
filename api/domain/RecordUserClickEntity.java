package com.shopping.api.domain;

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
@Table(name = "shopping_user_clickapps")
public class RecordUserClickEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:记录用户点击次数的实体类
	 */
	private static final long serialVersionUID = 1L;
	public RecordUserClickEntity(){
		super();
	}
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
