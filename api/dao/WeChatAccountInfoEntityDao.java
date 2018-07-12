package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.core.base.GenericDAO;
@SuppressWarnings("unchecked")
@Repository("weChatAccountInfoEntityDao")
public class WeChatAccountInfoEntityDao extends GenericDAO<WeChatAccountInfoEntity> {
	public WeChatAccountInfoEntityDao(){
		super();
	}
}
