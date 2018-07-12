package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.ZhiWei;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_areaSiteRankConfig")
public class AreaSiteRankConfig extends IdEntity{

	/**
	 * 配置区域合伙人
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(20) COMMENT '区域等级名称'")
	private String areaRankName;//区域等级名称
	@Lob
    @Column(columnDefinition="LongText")
    private String areaExplain; //区域等级说明
	@Column(columnDefinition = "INT(8) COMMENT '区域等级'")
	private Integer areaRank;//区域等级
	private Double openRequiredMoney;//开通所需钱数
	private Integer lengthOfTime;//开通时长
	@ManyToOne(fetch = FetchType.LAZY)
	private ZhiWei zhiwei;
	public AreaSiteRankConfig() {
		super();
	}
	public AreaSiteRankConfig(Date addTime) {
		super(addTime);
	}
	public String getAreaRankName() {
		return areaRankName;
	}
	public void setAreaRankName(String areaRankName) {
		this.areaRankName = areaRankName;
	}
	public String getAreaExplain() {
		return areaExplain;
	}
	public void setAreaExplain(String areaExplain) {
		this.areaExplain = areaExplain;
	}
	public Integer getAreaRank() {
		return areaRank;
	}
	public void setAreaRank(Integer areaRank) {
		this.areaRank = areaRank;
	}
	public Double getOpenRequiredMoney() {
		return openRequiredMoney;
	}
	public void setOpenRequiredMoney(Double openRequiredMoney) {
		this.openRequiredMoney = openRequiredMoney;
	}
	public Integer getLengthOfTime() {
		return lengthOfTime;
	}
	public void setLengthOfTime(Integer lengthOfTime) {
		this.lengthOfTime = lengthOfTime;
	}
	public ZhiWei getZhiwei() {
		return zhiwei;
	}
	public void setZhiwei(ZhiWei zhiwei) {
		this.zhiwei = zhiwei;
	}
}
