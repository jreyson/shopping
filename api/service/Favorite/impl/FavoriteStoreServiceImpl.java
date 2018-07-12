package com.shopping.api.service.Favorite.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.api.service.Favorite.IFavoriteStoreService;
import com.shopping.core.dao.IGenericDAO;
@Service
@Transactional
public class FavoriteStoreServiceImpl<T> implements IFavoriteStoreService<T> {

	@Resource(name = "favoriteStoreDao")
	private IGenericDAO<T> dao;

	@Override
	public T getObjById(Long Id) {
		return this.dao.get(Id);
	}

	@Override
	public boolean save(T paramsObj) {
		try {
			this.dao.save(paramsObj);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(T paramsObj) {
		try {
			this.dao.update(paramsObj);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean remove(Serializable Id) {
		try {
			this.dao.remove(Id);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> query(String paramString, Map<String, String> paramMap,
			int paramInt1, int paramInt2) {
		return (List<T>) this.dao.query(paramString, paramMap, paramInt1,
				paramInt2);
	}
}