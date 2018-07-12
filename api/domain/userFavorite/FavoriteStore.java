package com.shopping.api.domain.userFavorite;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_favorite_store")
public class FavoriteStore extends IdEntity{

	/**
	 * 用户收藏店铺
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private User user;
	@ManyToOne
	private Store store;
	public FavoriteStore() {
		super();
	}
	public FavoriteStore(Date addTime) {
		super(addTime);
	}
	public FavoriteStore(Date addTime,User user, Store store) {
		super(addTime);
		this.user = user;
		this.store = store;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
}
