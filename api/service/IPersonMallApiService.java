package com.shopping.api.service;

import java.io.Serializable;
import com.shopping.api.domain.PersonMallApi;

public interface IPersonMallApiService {
	
	public abstract PersonMallApi getObjById(Long id);
	
	public abstract boolean save(PersonMallApi paramGroupApi);

	public abstract boolean update(PersonMallApi paramGroupApi);
	
	public abstract void remove(Serializable id);
}
