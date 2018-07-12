package com.shopping.api.dao.materialCircle;

import org.springframework.stereotype.Repository;

import com.shopping.api.domain.materialCircle.MaterialItems;
import com.shopping.core.base.GenericDAO;
@Repository("materialItemsDao")
public class MaterialItemsDao<T> extends GenericDAO<MaterialItems> {
	
}
