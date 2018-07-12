package com.shopping.api.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.integralDeposit.IntegralDepositListEntity;
import com.shopping.api.service.IIntegralDepositListService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class IntegralDepositListServiceImpl implements
		IIntegralDepositListService {
	@Resource(name="integralDepositListEntityDao")
	private IGenericDAO<IntegralDepositListEntity> integralDepositListDao;
	@Override
	public List<IntegralDepositListEntity> query(String paramString,
			Map paramMap, int paramInt1, int paramInt2) {
		return integralDepositListDao.query(paramString, paramMap, paramInt1, paramInt2);
	}
	@Override
	public IntegralDepositListEntity getObjById(Long IntegralDepositListId) {
		return integralDepositListDao.get(IntegralDepositListId);
	}

}
