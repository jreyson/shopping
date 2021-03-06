package com.shopping.api.service.evaluate.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopping.api.service.evaluate.IEvaluateFunctionService;
import com.shopping.core.dao.IGenericDAO;
@Service("vVPResource")
@Transactional
public class VVPResource<T> implements IEvaluateFunctionService<T> {
	@Resource(name = "vVPResourceDao")
	private IGenericDAO<T> dao;
	@Override
	public T getObjById(Long Id) {
		// TODO Auto-generated method stub
		return this.dao.get(Id);
	}
	@Override
	public boolean save(T paramsObj) {
		// TODO Auto-generated method stub
		try {
			this.dao.save(paramsObj);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean update(T paramsObj) {
		// TODO Auto-generated method stub
		try {
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean remove(Serializable Id) {
		// TODO Auto-generated method stub
		try {
			this.dao.remove(Id);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public List<T> query(String paramString,Map<String, String> paramMap, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return (List<T>)this.dao.query(paramString, paramMap, paramInt1, paramInt2);
	}
}
