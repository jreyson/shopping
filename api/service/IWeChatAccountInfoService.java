package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
public interface IWeChatAccountInfoService {
	
	public abstract WeChatAccountInfoEntity getObjById(Long Id);
	
	public abstract boolean save(WeChatAccountInfoEntity weChatAccountInfoEntity);
	
	public abstract boolean update(WeChatAccountInfoEntity weChatAccountInfoEntity);
	
	public abstract boolean remove(Serializable countOrderId);
	
	public abstract List<WeChatAccountInfoEntity> query(String paramString, Map<String, String> paramMap, int paramInt1, int paramInt2);
}
