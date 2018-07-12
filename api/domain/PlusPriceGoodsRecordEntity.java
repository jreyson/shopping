package com.shopping.api.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_plusPriceGoods_record")
public class PlusPriceGoodsRecordEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:记录用户加价的实体类
	 */
	private static final long serialVersionUID = 1L;
	public PlusPriceGoodsRecordEntity(){
		
	}
	@ManyToOne
	private User user;//加价的用户
	@OneToOne
	private Accessory photos;//加价商品照片的路径
	@ManyToOne
	private Goods pricegoods;//加价的商品
	private double plusAfterPrice;//加价的价格增量
	private int priceGoodsCount;//加价购买的数量
	public int getPriceGoodsCount() {
		return priceGoodsCount;
	}
	public void setPriceGoodsCount(int priceGoodsCount) {
		this.priceGoodsCount = priceGoodsCount;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Accessory getPhotos() {
		return photos;
	}
	public double getPlusAfterPrice() {
		return plusAfterPrice;
	}
	public void setPlusAfterPrice(double plusAfterPrice) {
		this.plusAfterPrice = plusAfterPrice;
	}
	public void setPhotos(Accessory photos) {
		this.photos = photos;
	}
	public Goods getPricegoods() {
		return pricegoods;
	}
	public void setPricegoods(Goods pricegoods) {
		this.pricegoods = pricegoods;
	}
}
