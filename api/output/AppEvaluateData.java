package com.shopping.api.output;

import java.io.Serializable;

import com.shopping.api.domain.evaluate.AppraiseMessageEntity;

public class AppEvaluateData implements Serializable{

	/**
	 * @author:gaohao
	 * @description:输出app端评价数据
	 * @classType:中转类
	 */	
	private static final long serialVersionUID = 1L;
	private Integer praiseNum;//好评
	private Integer commonlyNum;//好评
	private Integer badNum;//好评
	private Integer evaluateSum;//好评
	private AppraiseMessageEntity evaluate;//最新一条评价
	private Integer deposit;//保证金
	public AppEvaluateData() {
		super();
	}
	public AppEvaluateData(Integer praiseNum, Integer commonlyNum,
			Integer badNum, Integer evaluateSum,
			AppraiseMessageEntity evaluate, Integer deposit) {
		super();
		this.praiseNum = praiseNum;
		this.commonlyNum = commonlyNum;
		this.badNum = badNum;
		this.evaluateSum = evaluateSum;
		this.evaluate = evaluate;
		this.deposit = deposit;
	}

	public Integer getPraiseNum() {
		return praiseNum;
	}
	public void setPraiseNum(Integer praiseNum) {
		this.praiseNum = praiseNum;
	}
	public Integer getCommonlyNum() {
		return commonlyNum;
	}
	public void setCommonlyNum(Integer commonlyNum) {
		this.commonlyNum = commonlyNum;
	}
	public Integer getBadNum() {
		return badNum;
	}
	public void setBadNum(Integer badNum) {
		this.badNum = badNum;
	}
	public Integer getEvaluateSum() {
		return evaluateSum;
	}
	public void setEvaluateSum(Integer evaluateSum) {
		this.evaluateSum = evaluateSum;
	}
	public AppraiseMessageEntity getEvaluate() {
		return evaluate;
	}
	public void setEvaluate(AppraiseMessageEntity evaluate) {
		this.evaluate = evaluate;
	}
	public Integer getDeposit() {
		return deposit;
	}
	public void setDeposit(Integer deposit) {
		this.deposit = deposit;
	}
}
