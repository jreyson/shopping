package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.PlusPriceGoodsRecordEntity;
import com.shopping.core.base.GenericDAO;
@Repository("plusPriceGoodsRecordDao")
public class PlusPriceGoodsRecordDao extends GenericDAO<PlusPriceGoodsRecordEntity>{
	public PlusPriceGoodsRecordDao(){
		super();
	}
}
