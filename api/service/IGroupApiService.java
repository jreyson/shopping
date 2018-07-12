package com.shopping.api.service;

import com.shopping.api.domain.GroupApi;

public abstract interface IGroupApiService {
	
	public abstract GroupApi getObjById(Long id);
	
	public abstract boolean save(GroupApi paramGroupApi);

	public abstract boolean update(GroupApi paramGroupApi);
}
