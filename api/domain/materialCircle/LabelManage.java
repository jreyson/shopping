package com.shopping.api.domain.materialCircle;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_mcLabelManage")
public class LabelManage extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈标签管理
	 */
	private static final long serialVersionUID = 1L;
	private String lableName;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getLableName() {
		return lableName;
	}
	public void setLableName(String lableName) {
		this.lableName = lableName;
	}
}
