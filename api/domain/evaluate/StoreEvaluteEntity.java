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
@Table(name = "shopping_pj_storeEvalute",catalog = "shopping")
public class StoreEvaluteEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:店铺评价表
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private StartsExplainEntity logisticsEvaluation;
	@ManyToOne(fetch = FetchType.LAZY)
	private StartsExplainEntity serviceAttitude;
	@ManyToOne(fetch = FetchType.LAZY)
	private	Store	storeEvalute;
	public StartsExplainEntity getLogisticsEvaluation() {
		return logisticsEvaluation;
	}
	public void setLogisticsEvaluation(StartsExplainEntity logisticsEvaluation) {
		this.logisticsEvaluation = logisticsEvaluation;
	}
	public StartsExplainEntity getServiceAttitude() {
		return serviceAttitude;
	}
	public void setServiceAttitude(StartsExplainEntity serviceAttitude) {
		this.serviceAttitude = serviceAttitude;
	}
	public Store getStoreEvalute(){
		return storeEvalute;
	}
	public void setStoreEvalute(Store storeEvalute) {
		this.storeEvalute = storeEvalute;
	}
}
