package com.shopping.api.output;

import java.io.Serializable;
import java.util.List;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.User;
/**
 * @author:akangah
 * @description:输出app端的首页数据
 * @classType:中转类
 */
public class HomePageData implements Serializable{
	private static final long serialVersionUID = 1L;
	public HomePageData(){
		super();
	}
	private List<AppHomePageEntity> homePageCarousel;//app首页banner数据
	private List<AppHomePageCommonPosition> homePageCommon;//app首页普通位数据
	private List<GoodsClass> goodsClass;//app首页商品类别数据
	private List<Goods> goodsSalum;//app首页商品销量最好的商品
	private List<?> xibao;//app首页喜报的数据
	private String year;//app首页获取到的年份
	private String sharedGoodsNum;//app首页共享商品数量
	private List<AppTransferData> modularDate;//首页模块数据
	private User user;//用户格言与头像
	private AppTransferData salesRank;//销量排行榜
	private AppTransferData dailyRecommend;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public List<AppTransferData> getModularDate() {
		return modularDate;
	}
	public void setModularDate(List<AppTransferData> modularDate) {
		this.modularDate = modularDate;
	}
	public List<AppHomePageCommonPosition> getHomePageCommon() {
		return homePageCommon;
	}
	public void setHomePageCommon(List<AppHomePageCommonPosition> homePageCommon) {
		this.homePageCommon = homePageCommon;
	}
	public List<AppHomePageEntity> getHomePageCarousel() {
		return homePageCarousel;
	}
	public void setHomePageCarousel(List<AppHomePageEntity> homePageCarousel) {
		this.homePageCarousel = homePageCarousel;
	}
	public List<GoodsClass> getGoodsClass() {
		return goodsClass;
	}
	public void setGoodsClass(List<GoodsClass> goodsClass) {
		this.goodsClass = goodsClass;
	}
	public List<Goods> getGoodsSalum() {
		return goodsSalum;
	}
	public void setGoodsSalum(List<Goods> goodsSalum) {
		this.goodsSalum = goodsSalum;
	}
	public List<?> getXibao() {
		return xibao;
	}
	public void setXibao(List<?> xibao) {
		this.xibao = xibao;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getSharedGoodsNum() {
		return sharedGoodsNum;
	}
	public void setSharedGoodsNum(String sharedGoodsNum) {
		this.sharedGoodsNum = sharedGoodsNum;
	}
	public AppTransferData getSalesRank() {
		return salesRank;
	}
	public void setSalesRank(AppTransferData salesRank) {
		this.salesRank = salesRank;
	}
	public AppTransferData getDailyRecommend() {
		return dailyRecommend;
	}
	public void setDailyRecommend(AppTransferData dailyRecommend) {
		this.dailyRecommend = dailyRecommend;
	}
}
