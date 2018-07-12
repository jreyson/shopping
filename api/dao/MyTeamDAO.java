package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.MyTeamEntity;
import com.shopping.core.base.GenericDAO;
@SuppressWarnings("unchecked")
@Repository("myTeamEntity")
public class MyTeamDAO extends GenericDAO<MyTeamEntity> {
	public MyTeamDAO(){
		super();
	}
}
