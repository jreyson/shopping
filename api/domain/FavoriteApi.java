package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_favorite")
public class FavoriteApi extends IdEntity implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int type;
	 @ManyToOne
	   private GoodsApi goods;
	 
	   @ManyToOne
	   private StoreApi store;
	 
	   @ManyToOne
	   private UserApi user;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public GoodsApi getGoods() {
		return goods;
	}

	public void setGoods(GoodsApi goods) {
		this.goods = goods;
	}

	public StoreApi getStore() {
		return store;
	}

	public void setStore(StoreApi store) {
		this.store = store;
	}

	public UserApi getUser() {
		return user;
	}

	public void setUser(UserApi user) {
		this.user = user;
	}
	   
	   
}
