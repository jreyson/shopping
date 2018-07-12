package com.shopping.api.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;


public class GoodsListApi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<GoodsApi> goods_list;

	public List<GoodsApi> getGoods_list() {
		return goods_list;
	}

	public void setGoods_list(List<GoodsApi> goods_list) {
		this.goods_list = goods_list;
	}
	
	

}
