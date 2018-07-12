package com.shopping.api.service.common;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
public abstract interface IUniversalService<T> {
	
	public abstract T getObjById(Long Id);
	
	public abstract boolean save(T paramsObj);
	
	public abstract boolean update(T paramsObj);
	
	public abstract boolean remove(Serializable paramsObj);
	
	public abstract List<T> query(String paramString, Map<String, String> paramMap, int paramInt1, int paramInt2);

}
