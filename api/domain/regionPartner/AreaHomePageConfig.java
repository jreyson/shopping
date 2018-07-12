package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_areaHomePageConfig")
public class AreaHomePageConfig extends IdEntity{

	/**
	 * 区域首页配置
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(20) COMMENT '付费类型:元/小时;元/天'")
	private String payType;
	@Column(columnDefinition = "INT(8) COMMENT '最大支付的时间'")
	private Integer maxPayNum;
	@Column(columnDefinition="bit COMMENT '是否开启首页付费抢购'")
	private boolean isOpen;//是否开启首页付费抢购
	@ManyToOne(fetch=FetchType.LAZY)
	private AreaPartnerEntity regionPartner;//区域站点实体
	public AreaHomePageConfig(Date addTime,String payType, Integer maxPayNum,
			boolean isOpen, AreaPartnerEntity regionPartner) {
		super(addTime);
		this.payType = payType;
		this.maxPayNum = maxPayNum;
		this.isOpen = isOpen;
		this.regionPartner = regionPartner;
	}
	public String getPayType() {
		return payType;
	}
	public AreaHomePageConfig() {
		super();
	}
	public AreaHomePageConfig(Date addTime) {
		super(addTime);
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public Integer getMaxPayNum() {
		return maxPayNum;
	}
	public void setMaxPayNum(Integer maxPayNum) {
		this.maxPayNum = maxPayNum;
	}
	public AreaPartnerEntity getRegionPartner() {
		return regionPartner;
	}
	public void setRegionPartner(AreaPartnerEntity regionPartner) {
		this.regionPartner = regionPartner;
	}
	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
}
