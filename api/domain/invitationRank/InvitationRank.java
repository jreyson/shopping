package com.shopping.api.domain.invitationRank;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Accessory;

/**
 * @author:gaohao
 * @description:app==>邀请人对应的军衔，职级
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_invitationRank")
public class InvitationRank extends IdEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	private String invitationRankName;//邀请等级名称
	private Integer dynamicNum;//动态战友数
	private Integer rankType;//0：军衔；1：职级	
	@OneToOne
	private Accessory photo;//图片
	public InvitationRank() {
		super();
	}
	public String getInvitationRankName() {
		return invitationRankName;
	}
	public void setInvitationRankName(String invitationRankName) {
		this.invitationRankName = invitationRankName;
	}
	public Integer getDynamicNum() {
		return dynamicNum;
	}
	public void setDynamicNum(Integer dynamicNum) {
		this.dynamicNum = dynamicNum;
	}
	public Integer getRankType() {
		return rankType;
	}
	public void setRankType(Integer rankType) {
		this.rankType = rankType;
	}
	public Accessory getPhoto() {
		return photo;
	}
	public void setPhoto(Accessory photo) {
		this.photo = photo;
	}
}
