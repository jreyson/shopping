package com.shopping.api.service.impl;


import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopping.api.domain.GroupApi;
import com.shopping.api.service.IGroupApiService;
import com.shopping.core.dao.IGenericDAO;


@Service
@Transactional
public class GroupApiServiceImpl implements IGroupApiService {
	@Resource(name = "groupApiDAO")
	private IGenericDAO<GroupApi> groupApiDAO;
	public GroupApi getObjById(Long id) {
		// TODO Auto-generated method stub
		return groupApiDAO.get(id);
	}

	public boolean save(GroupApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			groupApiDAO.save(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	public boolean update(GroupApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			groupApiDAO.update(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	

}
