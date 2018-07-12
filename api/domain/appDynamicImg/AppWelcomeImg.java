package com.shopping.api.domain.appDynamicImg;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Accessory;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appWelcomeImg")
public class AppWelcomeImg extends IdEntity implements Serializable{
	
	/**
	 * @author:gaohao
	 * @description:app==>app闪屏页
	 */
	private static final long serialVersionUID = 1L;
	private String imgState;//图片状态，1：展示；0：不展示
	@OneToOne
	private Accessory photo;
	
	public AppWelcomeImg() {
		super();
	}
	public AppWelcomeImg(Date addTime) {
		super(addTime);
	}
	public String getImgState() {
		return imgState;
	}
	public void setImgState(String imgState) {
		this.imgState = imgState;
	}
	public Accessory getPhoto() {
		return photo;
	}
	public void setPhoto(Accessory photo) {
		this.photo = photo;
	}
}
