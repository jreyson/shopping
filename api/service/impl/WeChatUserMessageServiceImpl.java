package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.api.domain.weChat.WeChatUserMessageEntity;
import com.shopping.api.service.IWeChatUserMessageService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class WeChatUserMessageServiceImpl implements IWeChatUserMessageService {
	@Resource(name = "weChatUserMessageEntityDao")
	private IGenericDAO<WeChatUserMessageEntity> weChatUserMessageEntityDao;
	@Override
	public WeChatUserMessageEntity getObjById(Long Id) {
		// TODO Auto-generated method stub
		return  weChatUserMessageEntityDao.get(Id);
	}

	@Override
	public boolean save(WeChatUserMessageEntity weChatAccountInfoEntity) {
		// TODO Auto-generated method stub
		try {
			weChatUserMessageEntityDao.save(weChatAccountInfoEntity);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(WeChatUserMessageEntity weChatAccountInfoEntity) {
		// TODO Auto-generated method stub
		try {
			weChatUserMessageEntityDao.update(weChatAccountInfoEntity);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean remove(Serializable countOrderId) {
		// TODO Auto-generated method stub
		try {
			weChatUserMessageEntityDao.remove(countOrderId);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public List<WeChatUserMessageEntity> query(String paramString,
			Map<String, String> paramMap, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return weChatUserMessageEntityDao.query(paramString, paramMap, paramInt1, paramInt2);
	}

}
