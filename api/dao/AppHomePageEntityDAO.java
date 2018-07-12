package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.core.base.GenericDAO;
@SuppressWarnings("unchecked")
@Repository("appHomePageEntityDAO")
public class AppHomePageEntityDAO extends GenericDAO<AppHomePageEntity> {
	public AppHomePageEntityDAO(){
		super();
	}
}
