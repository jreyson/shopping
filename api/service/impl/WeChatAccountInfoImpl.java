package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.api.service.IWeChatAccountInfoService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class WeChatAccountInfoImpl implements IWeChatAccountInfoService {
	@Resource(name = "weChatAccountInfoEntityDao")
	private IGenericDAO<WeChatAccountInfoEntity> weChatAccountInfoEntityDao;
	@Override
	public WeChatAccountInfoEntity getObjById(Long Id) {
		// TODO Auto-generated method stub
		return  weChatAccountInfoEntityDao.get(Id);
	}

	@Override
	public boolean save(WeChatAccountInfoEntity weChatAccountInfoEntity) {
		// TODO Auto-generated method stub
		try {
			weChatAccountInfoEntityDao.save(weChatAccountInfoEntity);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(WeChatAccountInfoEntity weChatAccountInfoEntity) {
		// TODO Auto-generated method stub
		try {
			weChatAccountInfoEntityDao.update(weChatAccountInfoEntity);
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
			weChatAccountInfoEntityDao.remove(countOrderId);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public List<WeChatAccountInfoEntity> query(String paramString,
			Map<String, String> paramMap, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return weChatAccountInfoEntityDao.query(paramString, paramMap, paramInt1, paramInt2);
	}

}
