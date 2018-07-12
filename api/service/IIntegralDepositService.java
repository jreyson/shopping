package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;

public interface IIntegralDepositService {
	public boolean remove(Serializable id);
	
	public abstract List<IntegralDepositEntity> query(String paramString, Map paramMap, int paramInt1, int paramInt2);
	
	public abstract boolean save(IntegralDepositEntity params);
	
	public abstract boolean update(IntegralDepositEntity params);
}
