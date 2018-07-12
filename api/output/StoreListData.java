package com.shopping.api.output;

import java.io.Serializable;

import com.shopping.foundation.domain.Store;
/**
 * @author:akangah
 * @description:以StoreListData类为单位输出店铺列表
 * @classType:中转类
 */
public class StoreListData implements Serializable {
	private static final long serialVersionUID = 1L;
	public StoreListData(){
		super();
	}
	private Store store;//对应的一个店铺
	private Long  salesNum;//一个店铺所有的出单销量
	private Long  goodsTotal;//一个店铺所有的商品
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
	public Long getSalesNum() {
		return salesNum;
	}
	public void setSalesNum(Long salesNum) {
		this.salesNum = salesNum;
	}
	public Long getGoodsTotal() {
		return goodsTotal;
	}
	public void setGoodsTotal(Long goodsTotal) {
		this.goodsTotal = goodsTotal;
	}
}
