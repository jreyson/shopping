package com.shopping.api.domain.materialCircle;

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
@Table(name = "shopping_mc_generalizeGoods")
public class GeneralizeGoods extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈推广商品,此表的主键不在任何表里面存储
	 */
	private static final long serialVersionUID = 1L;
	public GeneralizeGoods(){
		
	}
	@ManyToOne(fetch = FetchType.LAZY)
	private Goods generalizeGoods;//推广商品
	@ManyToOne(fetch = FetchType.LAZY)
	private User  user;//推广商品的用户
	public Goods getGeneralizeGoods() {
		return generalizeGoods;
	}
	public void setGeneralizeGoods(Goods generalizeGoods) {
		this.generalizeGoods = generalizeGoods;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
