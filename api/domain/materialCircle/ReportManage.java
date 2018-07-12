package com.shopping.api.domain.materialCircle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_reportManage")
public class ReportManage extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈举报管理
	 */
	private static final long serialVersionUID = 1L;
	@Lob//在Hibernate中使用@lob修饰大数据类型的属性
	@Column(columnDefinition = "LongText")
	private String reportReason;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private MaterialItems materialItems;
	public String getReportReason() {
		return reportReason;
	}
	public MaterialItems getMaterialItems() {
		return materialItems;
	}
	public void setMaterialItems(MaterialItems materialItems) {
		this.materialItems = materialItems;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setReportReason(String reportReason) {
		this.reportReason = reportReason;
	}
}
