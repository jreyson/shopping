package com.shopping.api.output;

import java.io.Serializable;
import java.util.List;

public class AppAdminData implements Serializable{

	/**
	 * @author:gaohao
	 * @description:输出app端管理员数据时况功能的数据
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 1L;
	private Object firstData;
	private Object secondData;
	private Object sortData;
	public AppAdminData() {
		super();
	}
	public Object getFirstData() {
		return firstData;
	}

	public void setFirstData(Object firstData) {
		this.firstData = firstData;
	}

	public Object getSecondData() {
		return secondData;
	}

	public void setSecondData(Object secondData) {
		this.secondData = secondData;
	}
	public Object getSortData() {
		return sortData;
	}
	public void setSortData(Object sortData) {
		this.sortData = sortData;
	}
}
