package com.shopping.api.output;

import java.io.Serializable;
import java.util.List;

import com.shopping.foundation.domain.PredepositLog;

public class AppBillsDataTemp implements Serializable {
	/**
	 * @author:gaohao
	 * @description:输出app端的账单的数据
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 1L;
	public AppBillsDataTemp(){
		super();
	}
	private String beginTime;
	private String endTime;
	private List<PredepositLog> predepositLogList;
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public List<PredepositLog> getPredepositLogList() {
		return predepositLogList;
	}
	public void setPredepositLogList(List<PredepositLog> predepositLogList) {
		this.predepositLogList = predepositLogList;
	} 
	
}
