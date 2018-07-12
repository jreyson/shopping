package com.shopping.api.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shopping.foundation.domain.User;

public class ZhiWeiTemp implements Serializable{
	/**
	 * @author:akangah
	 * @description:输出职位管理模块中获取到的下属成员的数据
	 * @classType:中转类
	 */
	private static final long serialVersionUID = 1L;
	public ZhiWeiTemp(){
		super();
	}
	private List<UserTempData> subordinateList=new ArrayList<UserTempData>();
	private List<UserTempData> deputyPositionList=new ArrayList<UserTempData>();
	public List<UserTempData> getSubordinateList() {
		return subordinateList;
	}
	public void setSubordinateList(List<UserTempData> subordinateList) {
		this.subordinateList = subordinateList;
	}
	public List<UserTempData> getDeputyPositionList() {
		return deputyPositionList;
	}
	public void setDeputyPositionList(List<UserTempData> deputyPositionList) {
		this.deputyPositionList = deputyPositionList;
	}
}
