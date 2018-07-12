package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.PlusPriceGoodsRecordEntity;
import com.shopping.api.domain.integralDeposit.IntegralDepositListEntity;

public interface IIntegralDepositListService {
	
	public abstract List<IntegralDepositListEntity> query(String paramString, Map paramMap, int paramInt1, int paramInt2);
	
	public IntegralDepositListEntity getObjById(Long IntegralDepositListId);
}
