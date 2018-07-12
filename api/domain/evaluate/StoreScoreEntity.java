package com.shopping.api.domain.evaluate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Store;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_pj_StoreScore",catalog = "shopping")
public class StoreScoreEntity extends IdEntity{
	/**
	 * @author:akangah
	 * @description:店铺评分表
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private Store store;
	@Column(columnDefinition = "Float default 0  COMMENT '店铺平均分'")
	private Float storeAverageScore;
	@Column(columnDefinition = "Float default 0  COMMENT '店铺物流平均分'")
	private Float storeExpressAverageScore;
	@Column(columnDefinition = "INT(8) unsigned zerofill default 0  COMMENT '店铺评价人数'")
	private Integer storeEvalutePerNum;
	@Column(nullable=false,columnDefinition="char(100)  COMMENT '平均分对应的说明字段'")
	private String filedExplainShow;
	@Column(columnDefinition="char(100)  COMMENT '物流平均分对应的说明字段'")
	private String expressSiledExplainShow;
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
	public Float getStoreAverageScore() {
		return storeAverageScore;
	}
	public void setStoreAverageScore(Float storeAverageScore) {
		this.storeAverageScore = storeAverageScore;
	}
	public Integer getStoreEvalutePerNum() {
		return storeEvalutePerNum;
	}
	public void setStoreEvalutePerNum(Integer storeEvalutePerNum) {
		this.storeEvalutePerNum = storeEvalutePerNum;
	}
	public String getFiledExplainShow() {
		return filedExplainShow;
	}
	public void setFiledExplainShow(String filedExplainShow) {
		this.filedExplainShow = filedExplainShow;
	}
	public Float getStoreExpressAverageScore() {
		return storeExpressAverageScore;
	}
	public void setStoreExpressAverageScore(Float storeExpressAverageScore) {
		this.storeExpressAverageScore = storeExpressAverageScore;
	}
	public String getExpressSiledExplainShow() {
		return expressSiledExplainShow;
	}
	public void setExpressSiledExplainShow(String expressSiledExplainShow) {
		this.expressSiledExplainShow = expressSiledExplainShow;
	}
}
