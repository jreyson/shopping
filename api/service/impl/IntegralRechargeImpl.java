package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.api.service.IIntegralRechargeService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class IntegralRechargeImpl implements IIntegralRechargeService {
	@Resource(name = "IntegralRechargeEntityDAO")
	private IGenericDAO<IntegralRechargeEntity> IntegralRechargeEntityDAO;
	@Override
	public boolean remove(Serializable id) {
		// TODO Auto-generated method stub
		try {
			IntegralRechargeEntityDAO.remove(id);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<IntegralRechargeEntity> query(String paramString, Map paramMap,
			int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return IntegralRechargeEntityDAO.query(paramString, paramMap, paramInt1, paramInt2);
	}

	@Override
	public boolean save(IntegralRechargeEntity integralRecharge) {
		// TODO Auto-generated method stub
		try {
			IntegralRechargeEntityDAO.save(integralRecharge);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(IntegralRechargeEntity integralRecharge) {
		// TODO Auto-generated method stub
		try {
			IntegralRechargeEntityDAO.update(integralRecharge);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

}
