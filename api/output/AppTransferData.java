package com.shopping.api.output;

import java.io.Serializable;

public class AppTransferData implements Serializable{
	/**
	 * @author:gaohao
	 * @description:输出app端中转的数据
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 1L;
	private Object firstData;
	private Object secondData;
	private Object thirdData;
	private Object fourthData;
	private Object fifthData;
	public AppTransferData() {
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
	public Object getThirdData() {
		return thirdData;
	}
	public void setThirdData(Object thirdData) {
		this.thirdData = thirdData;
	}
	public Object getFourthData() {
		return fourthData;
	}
	public void setFourthData(Object fourthData) {
		this.fourthData = fourthData;
	}
	public Object getFifthData() {
		return fifthData;
	}
	public void setFifthData(Object fifthData) {
		this.fifthData = fifthData;
	}
}
