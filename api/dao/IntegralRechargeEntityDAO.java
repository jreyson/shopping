package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.core.base.GenericDAO;
@Repository("IntegralRechargeEntityDAO")
public class IntegralRechargeEntityDAO extends GenericDAO<IntegralRechargeEntity> {
	public IntegralRechargeEntityDAO(){
		super();
	}
}
