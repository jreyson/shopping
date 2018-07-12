package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;

public interface IIntegralRechargeService {
	public boolean remove(Serializable id);
	
	public abstract List<IntegralRechargeEntity> query(String paramString, Map paramMap, int paramInt1, int paramInt2);
	
	public abstract boolean save(IntegralRechargeEntity params);
	
	public abstract boolean update(IntegralRechargeEntity params);
}
