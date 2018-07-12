package com.shopping.api.output;

import java.io.Serializable;
import java.util.List;

import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;

public class AppStorePageData implements Serializable{
	/**
	 * @author:gaohao
	 * @description:输出app端店铺首页的数据
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 1L;
	public AppStorePageData() {
		super();
	}
	private Store store;//店铺
	private User user;//用户信息
	private ZhiWei zhiWei;//用户职位
	private BuMen bumen;//用户部门
	private Integer bail;//店铺保证金
	private Integer sale;//店铺销量
	private Integer money;//店铺3月销售额
	private Integer goodNum;//店铺商品个数
	private List<Goods> goods;//店铺商品
	private ZhiXianEntity zhiXian;//职衔
	public ZhiXianEntity getZhiXian() {
		return zhiXian;
	}
	public void setZhiXian(ZhiXianEntity zhiXian) {
		this.zhiXian = zhiXian;
	}
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ZhiWei getZhiWei() {
		return zhiWei;
	}
	public void setZhiWei(ZhiWei zhiWei) {
		this.zhiWei = zhiWei;
	}
	public BuMen getBumen() {
		return bumen;
	}
	public void setBumen(BuMen bumen) {
		this.bumen = bumen;
	}
	public Integer getBail() {
		return bail;
	}
	public void setBail(Integer bail) {
		this.bail = bail;
	}
	public Integer getSale() {
		return sale;
	}
	public void setSale(Integer sale) {
		this.sale = sale;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	public Integer getGoodNum() {
		return goodNum;
	}
	public void setGoodNum(Integer goodNum) {
		this.goodNum = goodNum;
	}
	public List<Goods> getGoods() {
		return goods;
	}
	public void setGoods(List<Goods> goods) {
		this.goods = goods;
	}
}
