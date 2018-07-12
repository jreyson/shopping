package com.shopping.api.domain.userFunction;

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
@Table(name = "shopping_redPacketTheme")
public class RedPacketTheme extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>红包主题
	 */
	private static final long serialVersionUID = 1L;
	private String themeName;//主题名称
	@OneToOne
	private Accessory showPhoto;//展示图
	@OneToOne
	private Accessory operatePhoto;//操作图，png
	private String themeType;//主题类型
	private String themeTypeName;//主题类型名称
	private boolean isUse;//是否使用
	public RedPacketTheme() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RedPacketTheme(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public String getThemeName() {
		return themeName;
	}
	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
	public Accessory getShowPhoto() {
		return showPhoto;
	}
	public void setShowPhoto(Accessory showPhoto) {
		this.showPhoto = showPhoto;
	}
	public Accessory getOperatePhoto() {
		return operatePhoto;
	}
	public void setOperatePhoto(Accessory operatePhoto) {
		this.operatePhoto = operatePhoto;
	}
	public String getThemeType() {
		return themeType;
	}
	public void setThemeType(String themeType) {
		this.themeType = themeType;
	}
	public String getThemeTypeName() {
		return themeTypeName;
	}
	public void setThemeTypeName(String themeTypeName) {
		this.themeTypeName = themeTypeName;
	}
	public boolean isUse() {
		return isUse;
	}
	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}
}
