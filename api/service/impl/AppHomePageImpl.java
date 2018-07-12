package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.dao.AppHomePageEntityDAO;
import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.MyTeamEntity;
import com.shopping.api.service.IAppHomePageService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class AppHomePageImpl implements IAppHomePageService {
	@Resource(name = "appHomePageEntityDAO")
	private IGenericDAO<AppHomePageEntity> appHomePageEntityDAO;
	@Override
	public boolean remove(Serializable id) {
	// TODO Auto-generated method stub
		try {
			appHomePageEntityDAO.remove(id);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public List<AppHomePageEntity> query(String paramString, Map paramMap,
			int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return appHomePageEntityDAO.query(paramString, paramMap, paramInt1, paramInt2);
	}
	@Override
	public boolean save(AppHomePageEntity appHome) {
		// TODO Auto-generated method stub
		try {
			appHomePageEntityDAO.save(appHome);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean update(AppHomePageEntity appHome) {
		// TODO Auto-generated method stub
		try {
			appHomePageEntityDAO.update(appHome);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
}
