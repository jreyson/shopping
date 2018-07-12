package com.shopping.api.service.impl;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.MyTeamEntity;
import com.shopping.api.service.IMyTeamService;
import com.shopping.core.dao.IGenericDAO;

@Service
@Transactional
public class MyTeamServiceImpl implements IMyTeamService {
	@Resource(name = "myTeamEntity")
	private IGenericDAO<MyTeamEntity> myTeamEntity;
	@Override
	public boolean remove(Serializable id) {
		// TODO Auto-generated method stub
		try {
			myTeamEntity.remove(id);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
}
