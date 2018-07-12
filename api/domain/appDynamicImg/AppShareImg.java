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
@Table(name = "shopping_appShareImg")
public class AppShareImg extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>分享图
	 */
	private static final long serialVersionUID = 1L;
	@OneToOne
	private Accessory showPhoto;//展示图
	@OneToOne
	private Accessory operatePhoto;//操作图，png
	private String imgState;//图片状态,1：展示；0：不展示
	private String imgOrder;//图片顺序
	private String headPortraitState;//头像状态
	private String fontState;//文字状态
	private String fontSize;//字体大小
	private String fontColor;//字体颜色
	private String fontContent;//文字内容
	private String fontUpRange;//字体距上距离
	private String fontLeftRange;//字体距下距离
	private String qRCodeUpRange;//二维码距上距离
	private String qRCodeLeftRange;//二维码距下距离
	private Double qRCodeWidth;//二维码的宽
	private Double qRCodeHeight;//二维码的宽
	private String headPortraitUpRange;//头像距上距离
	private String headPortraitLeftRange;//头像距下距离
	private Double headPortraitWidth;//头像的宽
	private Double headPortraitHeight;//头像的宽
	public AppShareImg() {
		super();
	}
	public AppShareImg(Date addTime) {
		super(addTime);
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
	public String getImgState() {
		return imgState;
	}
	public void setImgState(String imgState) {
		this.imgState = imgState;
	}
	public String getFontSize() {
		return fontSize;
	}
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	public String getFontUpRange() {
		return fontUpRange;
	}
	public void setFontUpRange(String fontUpRange) {
		this.fontUpRange = fontUpRange;
	}
	public String getFontLeftRange() {
		return fontLeftRange;
	}
	public void setFontLeftRange(String fontLeftRange) {
		this.fontLeftRange = fontLeftRange;
	}
	public String getqRCodeUpRange() {
		return qRCodeUpRange;
	}
	public void setqRCodeUpRange(String qRCodeUpRange) {
		this.qRCodeUpRange = qRCodeUpRange;
	}
	public String getqRCodeLeftRange() {
		return qRCodeLeftRange;
	}
	public void setqRCodeLeftRange(String qRCodeLeftRange) {
		this.qRCodeLeftRange = qRCodeLeftRange;
	}
	public String getHeadPortraitUpRange() {
		return headPortraitUpRange;
	}
	public void setHeadPortraitUpRange(String headPortraitUpRange) {
		this.headPortraitUpRange = headPortraitUpRange;
	}
	public String getHeadPortraitLeftRange() {
		return headPortraitLeftRange;
	}
	public void setHeadPortraitLeftRange(String headPortraitLeftRange) {
		this.headPortraitLeftRange = headPortraitLeftRange;
	}
	public String getImgOrder() {
		return imgOrder;
	}
	public void setImgOrder(String imgOrder) {
		this.imgOrder = imgOrder;
	}
	public String getHeadPortraitState() {
		return headPortraitState;
	}
	public void setHeadPortraitState(String headPortraitState) {
		this.headPortraitState = headPortraitState;
	}
	public String getFontState() {
		return fontState;
	}
	public void setFontState(String fontState) {
		this.fontState = fontState;
	}
	public Double getqRCodeWidth() {
		return qRCodeWidth;
	}
	public void setqRCodeWidth(Double qRCodeWidth) {
		this.qRCodeWidth = qRCodeWidth;
	}
	public Double getqRCodeHeight() {
		return qRCodeHeight;
	}
	public void setqRCodeHeight(Double qRCodeHeight) {
		this.qRCodeHeight = qRCodeHeight;
	}
	public Double getHeadPortraitWidth() {
		return headPortraitWidth;
	}
	public void setHeadPortraitWidth(Double headPortraitWidth) {
		this.headPortraitWidth = headPortraitWidth;
	}
	public Double getHeadPortraitHeight() {
		return headPortraitHeight;
	}
	public void setHeadPortraitHeight(Double headPortraitHeight) {
		this.headPortraitHeight = headPortraitHeight;
	}
	public String getFontContent() {
		return fontContent;
	}
	public void setFontContent(String fontContent) {
		this.fontContent = fontContent;
	}
}
