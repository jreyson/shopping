package com.shopping.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.shopping.api.domain.AppHomePageEntity;
public interface IAppHomePageService {
	public boolean remove(Serializable id);
	
	public abstract List<AppHomePageEntity> query(String paramString, Map paramMap, int paramInt1, int paramInt2);
	
	public abstract boolean save(AppHomePageEntity appHome);
	
	public abstract boolean update(AppHomePageEntity appHome);
}
