package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class IntegralDepositServiceImpl implements IIntegralDepositService{
	@Resource(name = "integralDepositEntityDao")
	private IGenericDAO<IntegralDepositEntity> integralDepositEntityDAO;
	@Override
	public boolean remove(Serializable id) {
		try {
			integralDepositEntityDAO.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false;
	}

	@Override
	public List<IntegralDepositEntity> query(String paramString, Map paramMap,
			int paramInt1, int paramInt2) {		
		return integralDepositEntityDAO.query(paramString, paramMap, paramInt1, paramInt2);
	}

	@Override
	public boolean save(IntegralDepositEntity params) {
		try {
			integralDepositEntityDAO.save(params);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(IntegralDepositEntity params) {
		try {
			integralDepositEntityDAO.update(params);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
