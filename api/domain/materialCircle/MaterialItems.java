package com.shopping.api.domain.materialCircle;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.annotation.Lock;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_materialItems")
public class MaterialItems extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈普通用户看到的条目
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="mcTheme",nullable=false,unique=false,columnDefinition="varchar(150) default 'mcTheme' COMMENT '素材圈主题名字'")
	private String mcTheme;
	@Column(columnDefinition = "BIGINT(19) unsigned  default 0 COMMENT '该素材转发的总次数'")
	private Long transmitTotalNum;
	private Date lastTransmitTime;//最后的转发时间
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;//来自于那个用户
	@Column(name="materialTpye",nullable=false,unique=false,columnDefinition="char(20) default 'materialTpye' COMMENT '素材类别'")
	private String materialTpye;//视频类型==>video 图文类型==>imgText
	private Date topTime;//置顶时间
	@ManyToOne(fetch = FetchType.LAZY)
	private GeneralizeGoods generalizeGoods;
	@ManyToOne(fetch = FetchType.LAZY)
	private LabelManage labelManage;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "materialItems",cascade = { javax.persistence.CascadeType.REMOVE })
	private List<ReportManage> reportItems;//素材圈举报管理实体
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "materialItems",cascade = { javax.persistence.CascadeType.REMOVE })
	private List<VPResource> vPResourceItems;//素材圈资源实体
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "materialItems",cascade = { javax.persistence.CascadeType.REMOVE })
	private List<TranspondRecord> transpondRecordList;//转发记录
	public List<TranspondRecord> getTranspondRecordList() {
		return transpondRecordList;
	}
	public void setTranspondRecordList(List<TranspondRecord> transpondRecordList) {
		this.transpondRecordList = transpondRecordList;
	}
	public String getMcTheme() {
		return mcTheme;
	}
	public List<VPResource> getvPResourceItems() {
		return vPResourceItems;
	}
	public void setvPResourceItems(List<VPResource> vPResourceItems) {
		this.vPResourceItems = vPResourceItems;
	}
	public void setMcTheme(String mcTheme) {
		this.mcTheme = mcTheme;
	}
	public Long getTransmitTotalNum() {
		return transmitTotalNum;
	}
	public void setTransmitTotalNum(Long transmitTotalNum) {
		this.transmitTotalNum = transmitTotalNum;
	}
	public Date getLastTransmitTime() {
		return lastTransmitTime;
	}
	public void setLastTransmitTime(Date lastTransmitTime) {
		this.lastTransmitTime = lastTransmitTime;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getMaterialTpye() {
		return materialTpye;
	}
	public void setMaterialTpye(String materialTpye) {
		this.materialTpye = materialTpye;
	}
	public Date getTopTime() {
		return topTime;
	}
	public void setTopTime(Date topTime) {
		this.topTime = topTime;
	}
	public GeneralizeGoods getGeneralizeGoods() {
		return generalizeGoods;
	}
	public void setGeneralizeGoods(GeneralizeGoods generalizeGoods) {
		this.generalizeGoods = generalizeGoods;
	}
	public LabelManage getLabelManage() {
		return labelManage;
	}
	public void setLabelManage(LabelManage labelManage) {
		this.labelManage = labelManage;
	}
	public List<ReportManage> getReportItems() {
		return reportItems;
	}
	public void setReportItems(List<ReportManage> reportItems) {
		this.reportItems = reportItems;
	}
}
