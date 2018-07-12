package com.shopping.api.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.AuctionDetailsApi;
import com.shopping.api.domain.AuctionRecordApi;
import com.shopping.api.service.IAuctionDetailsService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class AuctionDetailsImpl implements IAuctionDetailsService {
	@Resource(name = "auctionDetailsApi")
	private IGenericDAO<AuctionDetailsApi> auctionDetailsApi;
	@Override
	public AuctionDetailsApi getObjById(Long id) {
		// TODO Auto-generated method stub
		return auctionDetailsApi.get(id);
	}
	
	@Override
	public boolean save(AuctionDetailsApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			auctionDetailsApi.save(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(AuctionDetailsApi paramGroupApi) {
		// TODO Auto-generated method stub
		try {
			auctionDetailsApi.update(paramGroupApi);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

}
