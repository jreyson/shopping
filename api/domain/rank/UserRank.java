package com.shopping.api.domain.rank;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Accessory;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_userRank",catalog = "shopping")
public class UserRank extends IdEntity{
	/**
	 * @author:gaohao
	 * @description:会员等级
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="int COMMENT '会员等级'")
	private Integer rankNum;
	@Column(columnDefinition="int COMMENT '对应需要的积分数'")
	private Integer integralNum;
	@ManyToOne(fetch = FetchType.LAZY)
	private UserRankName userRankName;//对应的会员等级名称
	@Column(columnDefinition="varchar(100) COMMENT '会员等级说明'")
	private String rankExplain;
	@Column(columnDefinition="bit COMMENT '是否有担保金'")
	private Boolean isHaveDanbaoPrice;
	@Column(columnDefinition="bit COMMENT '是否有储备金'")
	private Boolean isHaveChubeiPrice;
	@Column(columnDefinition="bit COMMENT '是否有导购金'")
	private Boolean isHaveDaogouPrice;
	@Column(columnDefinition="bit COMMENT '是否有招商金'")
	private Boolean isHaveZhaoshangPrice;
	@Column(columnDefinition="bit COMMENT '是否有衔级金'")
	private Boolean isHaveXianjiPrice;
	@Column(columnDefinition="bit COMMENT '是否有赠股金'")
	private Boolean isHaveZengguPrice;
	@Column(columnDefinition="bit COMMENT '是否有分红金'")
	private Boolean isHaveFenhongPrice;
	@Column(columnDefinition="int COMMENT '职位权限大小'")
	private Integer positionOrder;//职位权限大小
	@Column(columnDefinition="bit COMMENT '是否有任职权限'")
	private Boolean isHaveZhiweiRight;
	@OneToOne
	private Accessory gradeSmallIcon;//会员等级小图标
	@OneToOne
	private Accessory gradeBigIcon;//会员等级大图标
	@OneToOne
	private Accessory gradeTrophy;//会员等级奖杯图片
	public UserRank() {
		super();
	}
	public UserRank(Date addTime) {
		super(addTime);
	}
	public Boolean getIsHaveZhiweiRight() {
		return isHaveZhiweiRight;
	}
	public void setIsHaveZhiweiRight(Boolean isHaveZhiweiRight) {
		this.isHaveZhiweiRight = isHaveZhiweiRight;
	}
	public Integer getPositionOrder() {
		return positionOrder;
	}
	public void setPositionOrder(Integer positionOrder) {
		this.positionOrder = positionOrder;
	}
	public Integer getRankNum() {
		return rankNum;
	}
	public void setRankNum(Integer rankNum) {
		this.rankNum = rankNum;
	}
	public Integer getIntegralNum() {
		return integralNum;
	}
	public void setIntegralNum(Integer integralNum) {
		this.integralNum = integralNum;
	}
	public UserRankName getUserRankName() {
		return userRankName;
	}
	public void setUserRankName(UserRankName userRankName) {
		this.userRankName = userRankName;
	}
	public String getRankExplain() {
		return rankExplain;
	}
	public void setRankExplain(String rankExplain) {
		this.rankExplain = rankExplain;
	}
	public Boolean getIsHaveDanbaoPrice() {
		return isHaveDanbaoPrice;
	}
	public void setIsHaveDanbaoPrice(Boolean isHaveDanbaoPrice) {
		this.isHaveDanbaoPrice = isHaveDanbaoPrice;
	}
	public Boolean getIsHaveChubeiPrice() {
		return isHaveChubeiPrice;
	}
	public void setIsHaveChubeiPrice(Boolean isHaveChubeiPrice) {
		this.isHaveChubeiPrice = isHaveChubeiPrice;
	}
	public Boolean getIsHaveDaogouPrice() {
		return isHaveDaogouPrice;
	}
	public void setIsHaveDaogouPrice(Boolean isHaveDaogouPrice) {
		this.isHaveDaogouPrice = isHaveDaogouPrice;
	}
	public Boolean getIsHaveZhaoshangPrice() {
		return isHaveZhaoshangPrice;
	}
	public void setIsHaveZhaoshangPrice(Boolean isHaveZhaoshangPrice) {
		this.isHaveZhaoshangPrice = isHaveZhaoshangPrice;
	}
	public Boolean getIsHaveXianjiPrice() {
		return isHaveXianjiPrice;
	}
	public void setIsHaveXianjiPrice(Boolean isHaveXianjiPrice) {
		this.isHaveXianjiPrice = isHaveXianjiPrice;
	}
	public Boolean getIsHaveZengguPrice() {
		return isHaveZengguPrice;
	}
	public void setIsHaveZengguPrice(Boolean isHaveZengguPrice) {
		this.isHaveZengguPrice = isHaveZengguPrice;
	}
	public Boolean getIsHaveFenhongPrice() {
		return isHaveFenhongPrice;
	}
	public void setIsHaveFenhongPrice(Boolean isHaveFenhongPrice) {
		this.isHaveFenhongPrice = isHaveFenhongPrice;
	}
	public Accessory getGradeSmallIcon() {
		return gradeSmallIcon;
	}
	public void setGradeSmallIcon(Accessory gradeSmallIcon) {
		this.gradeSmallIcon = gradeSmallIcon;
	}
	public Accessory getGradeBigIcon() {
		return gradeBigIcon;
	}
	public void setGradeBigIcon(Accessory gradeBigIcon) {
		this.gradeBigIcon = gradeBigIcon;
	}
	public Accessory getGradeTrophy() {
		return gradeTrophy;
	}
	public void setGradeTrophy(Accessory gradeTrophy) {
		this.gradeTrophy = gradeTrophy;
	}
}
