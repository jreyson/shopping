package com.shopping.api.domain.recommend;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Store;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_recommend_store")
public class RecommendStore extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>店铺搜索推荐店铺
	 */
	private static final long serialVersionUID = 1L;

	public RecommendStore() {
		super();
	}
	@OneToOne(fetch=FetchType.LAZY)
	private Store store;

	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
}
