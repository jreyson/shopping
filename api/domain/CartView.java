package com.shopping.api.domain;

import java.io.Serializable;
import java.util.List;

import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Express;
import com.shopping.foundation.domain.StoreCart;

public class CartView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int count;
	private StoreCart sc;
	private String store_id;
	private List<Express> expressList;
	List<Address> addrs;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public StoreCart getSc() {
		return sc;
	}
	public void setSc(StoreCart sc) {
		this.sc = sc;
	}

	public String getStore_id() {
		return store_id;
	}
	public void setStore_id(String store_id) {
		this.store_id = store_id;
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
