package com.shopping.api.service;

import com.shopping.api.domain.AuctionDetailsApi;

public abstract interface IAuctionDetailsService {
	
	public abstract AuctionDetailsApi getObjById(Long id);
	
	public abstract boolean save(AuctionDetailsApi paramGroupApi);

	public abstract boolean update(AuctionDetailsApi paramGroupApi);
	
}

