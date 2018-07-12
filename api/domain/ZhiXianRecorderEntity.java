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
@Table(name = "shopping_zhixian_recorder")
public class ZhiXianRecorderEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:我的职衔记录的实体类
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	@ManyToOne(fetch = FetchType.LAZY)
	private User myselfUser;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private ZhiXianEntity zhixian;
	public ZhiXianRecorderEntity(){
		super();
	}
	public User getMyselfUser() {
		return myselfUser;
	}
	public void setMyselfUser(User myselfUser) {
		this.myselfUser = myselfUser;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ZhiXianEntity getZhixian() {
		return zhixian;
	}
	public void setZhixian(ZhiXianEntity zhixian) {
		this.zhixian = zhixian;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
