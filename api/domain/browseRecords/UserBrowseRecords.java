package com.shopping.api.domain.browseRecords;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_userBrowseRecords")
public class UserBrowseRecords extends IdEntity{

	/**
	 * @author gaohao
	 *保存用户浏览商品记录
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private Goods goods;
	private Date lastAccessTime;
	public UserBrowseRecords() {
		super();
	}
	public UserBrowseRecords(User user, Goods goods,Date addTime,Date lastAccessTime) {
		super(addTime);
		this.user = user;
		this.goods = goods;
		this.lastAccessTime = lastAccessTime;
	}

	public UserBrowseRecords(Date addTime) {
		super(addTime);
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public Date getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	
}
