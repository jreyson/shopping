package com.shopping.api.domain.materialCircle;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_transpondRecord")
public class TranspondRecord extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈已转发功能
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@OneToOne(fetch = FetchType.LAZY)
	private MaterialItems materialItems;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public MaterialItems getMaterialItems() {
		return materialItems;
	}
	public void setMaterialItems(MaterialItems materialItems) {
		this.materialItems = materialItems;
	}
}
