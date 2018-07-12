package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.countBuy.CountOrderDomain;

public interface ICountOrderService {
	
	public abstract CountOrderDomain getObjById(Long Id);
	
	public abstract boolean save(CountOrderDomain countOrderDomain);
	
	public abstract boolean update(CountOrderDomain countOrderDomain);
	
	public abstract boolean remove(Serializable countOrderId);
	
	public abstract List<CountOrderDomain> query(String paramString, Map<String, String> paramMap, int paramInt1, int paramInt2);
}
