package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.shopping.api.domain.weChat.WeChatUserMessageEntity;
public interface IWeChatUserMessageService {
	public abstract WeChatUserMessageEntity getObjById(Long Id);
	
	public abstract boolean save(WeChatUserMessageEntity weChatUserMessageEntity);
	
	public abstract boolean update(WeChatUserMessageEntity weChatUserMessageEntity);
	
	public abstract boolean remove(Serializable weChatUserMessageEntityId);
	
	public abstract List<WeChatUserMessageEntity> query(String paramString, Map<String, String> paramMap, int paramInt1, int paramInt2);
}
