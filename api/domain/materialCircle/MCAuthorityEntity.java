package com.shopping.api.domain.materialCircle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.annotation.Lock;
import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_mcAuthority")
public class MCAuthorityEntity extends IdEntity{
	/**
	 * @author:akangah
	 * @description:app==>素材圈管理员权限
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="rankName",nullable=false,unique=true,columnDefinition="varchar(180) default 'adminName' COMMENT '管理员级别名字'")  
	private String rankName;//成员级别名字
	@Column(columnDefinition="varchar(180)")
	private String rankNameExplain;//成员级别名字名字解释
	@Column(columnDefinition="char(120) COMMENT'成员拥有对素材操作的权限范围' ")
	private String materialAuthRange;//成员拥有对素材操作的权限范围
	@Column(columnDefinition="char(120)")
	private String authIllustrate;//成员权限说明
	@Lock
	private boolean isAdmin;//标识成员是否是管理员
	@Column(columnDefinition = "INT(2) unsigned zerofill default 0 COMMENT '用户的管理员级别'")
	private Integer memberLevel;//成员级别,越小级别越大
	public String getRankName() {
		return rankName;
	}
	public Integer getMemberLevel() {
		return memberLevel;
	}
	public void setMemberLevel(Integer memberLevel) {
		this.memberLevel = memberLevel;
	}
	public void setRankName(String rankName) {
		this.rankName = rankName;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public String getRankNameExplain() {
		return rankNameExplain;
	}
	public void setRankNameExplain(String rankNameExplain) {
		this.rankNameExplain = rankNameExplain;
	}
	public String getAuthIllustrate() {
		return authIllustrate;
	}
	public void setAuthIllustrate(String authIllustrate) {
		this.authIllustrate = authIllustrate;
	}
	public String getMaterialAuthRange() {
		return materialAuthRange;
	}
	public void setMaterialAuthRange(String materialAuthRange) {
		this.materialAuthRange = materialAuthRange;
	}
}
