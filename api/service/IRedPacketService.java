package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.userFunction.RedPacket;

public interface IRedPacketService {
	public boolean remove(Serializable id);
	
	public abstract List<RedPacket> query(String paramString, Map paramMap, int paramInt1, int paramInt2);
	
	public abstract boolean save(RedPacket params);
	
	public abstract boolean update(RedPacket params);
	
	public abstract RedPacket getObjById(Long id);
}
