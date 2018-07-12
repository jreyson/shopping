package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_person_mall_photo_url")
public class PersonMallPhotoUrl extends IdEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:个人商城美片url的实体类
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY,cascade = { javax.persistence.CascadeType.REMOVE })
	private User user;//当前的用户
	private String href_url;//这条记录对应的url
	@OneToOne(fetch = FetchType.LAZY,cascade = { javax.persistence.CascadeType.REMOVE })
	private Goods goods;//保存对应的商品
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getHref_url() {
		return href_url;
	}
	public void setHref_url(String href_url) {
		this.href_url = href_url;
	}
}
