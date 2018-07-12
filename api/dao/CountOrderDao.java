package com.shopping.api.dao;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.core.base.GenericDAO;
@SuppressWarnings("unchecked")
@Repository("countOrderDao")
public class CountOrderDao extends GenericDAO<CountOrderDomain>{
	public CountOrderDao(){
		super();
	}
}
