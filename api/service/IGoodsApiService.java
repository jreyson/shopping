package com.shopping.api.service;

import java.util.List;
import java.util.Map;

import com.shopping.api.domain.GoodsApi;

public abstract interface IGoodsApiService
{

  public abstract List<GoodsApi> query(String paramString, Map paramMap, int paramInt1, int paramInt2);

  public abstract List<GoodsApi> getGoodsList();
  
  public abstract GoodsApi getObjById(Long id);
}



 
 