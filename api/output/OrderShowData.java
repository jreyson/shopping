package com.shopping.api.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Express;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.User;
/**
 *@author:akangah
 *@description:在app端点击结算购物车,生成订单展示界面时需要的数据
 *@classType:中转类
 */
public class OrderShowData implements Serializable{
	private static final long serialVersionUID = 1L;
	public OrderShowData(){
		super();
	}
	private User user;//当前登陆的用户
	private List<StoreCart> storeCartList=new ArrayList<StoreCart>();//店铺购物车集合,以店铺购物车为展示单位
	private List<Express> expressList=new ArrayList<Express>();//快递费用(根据店铺设置的数据来看,从shopping_transport表中查看)
	private List<Address> addrs=new ArrayList<Address>();//当前登陆用户的地址列表
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public List<StoreCart> getStoreCartList() {
		return storeCartList;
	}
	public void setStoreCartList(List<StoreCart> storeCartList) {
		this.storeCartList = storeCartList;
	}
	public List<Express> getExpressList() {
		return expressList;
	}
	public void setExpressList(List<Express> expressList) {
		this.expressList = expressList;
	}
	public List<Address> getAddrs() {
		return addrs;
	}
	public void setAddrs(List<Address> addrs) {
		this.addrs = addrs;
	}
}
