package com.shopping.api.service;
import java.io.Serializable;
import com.shopping.api.domain.PlusPriceGoodsRecordEntity;

public interface IPlusPriceGoodsRecordService {
	
	public abstract PlusPriceGoodsRecordEntity getObjById(Long plusPriceGoodsRecordId);
	
	public abstract boolean save(PlusPriceGoodsRecordEntity plusPriceGoodsRecordObj);

	public abstract boolean update(PlusPriceGoodsRecordEntity plusPriceGoodsRecordObj);
	
	public abstract void remove(Serializable plusPriceGoodsRecordId);
}
