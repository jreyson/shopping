package com.shopping.api.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.GoodsApi;
import com.shopping.api.service.IGoodsApiService;
import com.shopping.core.dao.IGenericDAO;
import com.shopping.foundation.domain.Goods;

@Service
@Transactional
public class GoodsApiServiceImpl implements IGoodsApiService {

	@Resource(name = "goodsApiDAO")
	private IGenericDAO<GoodsApi> goodsApiDAO;

	public List<GoodsApi> query(String paramString, Map paramMap,
			int paramInt1, int paramInt2) {
		return goodsApiDAO.query(paramString, paramMap, paramInt1, paramInt2);
	}

	public List<GoodsApi> getGoodsList() {
		// TODO Auto-generated method stub
		return null;
	}

	public GoodsApi getObjById(Long id) {
		return goodsApiDAO.get(id);
	}

}
