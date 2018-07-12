package com.shopping.api.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.domain.userFunction.RedPacket;
import com.shopping.api.service.IRedPacketService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class RedPacketServiceImpl implements IRedPacketService {
	@Resource(name = "redPacketDao")
	private IGenericDAO<RedPacket> redPacketDao;
	@Override
	public boolean remove(Serializable id) {
		try {
			redPacketDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<RedPacket> query(String paramString, Map paramMap,
			int paramInt1, int paramInt2) {
		return redPacketDao.query(paramString, paramMap, paramInt1, paramInt2);
	}

	@Override
	public boolean save(RedPacket redPacket) {
		try {
			redPacketDao.save(redPacket);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(RedPacket redPacket) {
		try {
			redPacketDao.update(redPacket);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public RedPacket getObjById(Long id) {
		
		return redPacketDao.get(id);
	}

}
