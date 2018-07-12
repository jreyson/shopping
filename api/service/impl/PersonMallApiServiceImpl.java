package com.shopping.api.service.impl;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.AuctionDetailsApi;
import com.shopping.api.domain.PersonMallApi;
import com.shopping.api.service.IPersonMallApiService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class PersonMallApiServiceImpl implements IPersonMallApiService {
	@Resource(name = "personMallApi")
	private IGenericDAO<PersonMallApi> personMallApi;
	@Override
	public PersonMallApi getObjById(Long id) {
		// TODO Auto-generated method stub
		return personMallApi.get(id);
	}
	
	@Override
	public boolean save(PersonMallApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			personMallApi.save(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	@Override
	public boolean update(PersonMallApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			personMallApi.update(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public void remove(Serializable id) {
		// TODO Auto-generated method stub
		personMallApi.remove(id);
	}

}
