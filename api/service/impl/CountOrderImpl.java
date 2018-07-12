package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.api.service.ICountOrderService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class CountOrderImpl implements ICountOrderService {
	@Resource(name = "countOrderDao")
	private IGenericDAO<CountOrderDomain> countOrderDao;
	@Override
	public CountOrderDomain getObjById(Long Id) {
		// TODO Auto-generated method stub
		return countOrderDao.get(Id);
	}
	
	@Override
	public boolean save(CountOrderDomain countOrderDomain) {
		// TODO Auto-generated method stub
		try {
			countOrderDao.save(countOrderDomain);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(CountOrderDomain countOrderDomain) {
		// TODO Auto-generated method stub
		try {
			countOrderDao.update(countOrderDomain);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean remove(Serializable countOrderId) {
		// TODO Auto-generated method stub
		try {
			countOrderDao.remove(countOrderId);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public List<CountOrderDomain> query(String paramString,
			Map<String, String> paramMap, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return countOrderDao.query(paramString, paramMap, paramInt1, paramInt2);
	}

}
