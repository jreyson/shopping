package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.weChat.WeChatUserMessageEntity;
import com.shopping.core.base.GenericDAO;
@SuppressWarnings("unchecked")
@Repository("weChatUserMessageEntityDao")
public class WeChatUserMessageEntityDao extends GenericDAO<WeChatUserMessageEntity> {
	public WeChatUserMessageEntityDao(){
		super();
	}
}
