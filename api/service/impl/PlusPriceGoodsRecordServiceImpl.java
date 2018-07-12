package com.shopping.api.service.impl;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.PlusPriceGoodsRecordEntity;
import com.shopping.api.service.IPlusPriceGoodsRecordService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class PlusPriceGoodsRecordServiceImpl implements IPlusPriceGoodsRecordService{
	@Resource(name = "plusPriceGoodsRecordDao")
	private IGenericDAO<PlusPriceGoodsRecordEntity> plusPriceGoodsRecordDao;
	@Override
	public PlusPriceGoodsRecordEntity getObjById(Long plusPriceGoodsRecordId) {
		// TODO Auto-generated method stub
		return plusPriceGoodsRecordDao.get(plusPriceGoodsRecordId);
	}
	@Override
	public boolean save(PlusPriceGoodsRecordEntity plusPriceGoodsRecordObj) {
		// TODO Auto-generated method stub
		try {
			plusPriceGoodsRecordDao.save(plusPriceGoodsRecordObj);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	@Override
	public boolean update(PlusPriceGoodsRecordEntity plusPriceGoodsRecordObj) {
		// TODO Auto-generated method stub
		try {
			plusPriceGoodsRecordDao.update(plusPriceGoodsRecordObj);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	@Override
	public void remove(Serializable plusPriceGoodsRecordId) {
		// TODO Auto-generated method stub
		plusPriceGoodsRecordDao.remove(plusPriceGoodsRecordId);
	}
}
