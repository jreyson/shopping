package com.shopping.api.domain.regionPartner;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.User;

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_areaPartner")
public class AreaPartnerEntity extends IdEntity{

	/**
	 * 区域合伙人用户
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private User user;//用户
	@ManyToOne
	private AreaGradeOfUser area;//区域站点
	private Date expireTime;//到期时间
	@ManyToOne
	private BuMen buMen;//部门
	@ManyToOne
	private AreaSiteRankConfig areaSiteRankConfig;//合伙人类型
	public AreaPartnerEntity() {
		super();
	}
	public AreaPartnerEntity(Date addTime) {
		super(addTime);
	}
	public AreaPartnerEntity(Date addTime, User user, AreaGradeOfUser area,
			Date expireTime, AreaSiteRankConfig areaSiteRankConfig) {
		super(addTime);
		this.user = user;
		this.area = area;
		this.expireTime = expireTime;
		this.areaSiteRankConfig = areaSiteRankConfig;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public AreaGradeOfUser getArea() {
		return area;
	}
	public void setArea(AreaGradeOfUser area) {
		this.area = area;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public BuMen getBuMen() {
		return buMen;
	}
	public void setBuMen(BuMen buMen) {
		this.buMen = buMen;
	}
	public AreaSiteRankConfig getAreaSiteRankConfig() {
		return areaSiteRankConfig;
	}
	public void setAreaSiteRankConfig(AreaSiteRankConfig areaSiteRankConfig) {
		this.areaSiteRankConfig = areaSiteRankConfig;
	}
}
