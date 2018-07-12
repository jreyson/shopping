package com.shopping.api.domain.materialCircle;

import java.util.Date;

import javax.persistence.Column;
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
@Table(name = "shopping_mc_materialCircleMember")
public class MaterialCircleMemberEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:app==>素材圈管理员成员
	 */
	public MaterialCircleMemberEntity(){
		super();
	}
	public MaterialCircleMemberEntity(Date addTime,boolean deleteStatus,User user,Long transmitTotalNum,
			Short checkStatus,MCAuthorityEntity mcAuthority){
		super.setAddTime(addTime);
		super.setDeleteStatus(deleteStatus);
		this.member=user;
		this.transmitTotalNum=transmitTotalNum;
		this.checkStatus=checkStatus;
		this.mcAuthority=mcAuthority;
	}
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private User member;//素材圈的成员
	@Column(columnDefinition = "BIGINT(19) unsigned  default 0  COMMENT '用户转发的总次数'")
	private Long transmitTotalNum;//用户的转发总次数
	@Column(columnDefinition = "TINYINT(1) unsigned zerofill default 0 COMMENT '用户申请上传素材圈的状态'")
	private Short checkStatus;//审核状态0表示未审核1表示已经通过审核
	@ManyToOne(fetch = FetchType.LAZY)
	private MCAuthorityEntity mcAuthority;//素材圈的成员权限
	public User getMember() {
		return member;
	}
	public void setMember(User member) {
		this.member = member;
	}
	public Long getTransmitTotalNum() {
		return transmitTotalNum;
	}
	public void setTransmitTotalNum(Long transmitTotalNum) {
		this.transmitTotalNum = transmitTotalNum;
	}
	public Short getCheckStatus() {
		return checkStatus;
	}
	public void setCheckStatus(Short checkStatus) {
		this.checkStatus = checkStatus;
	}
	public MCAuthorityEntity getMcAuthority() {
		return mcAuthority;
	}
	public void setMcAuthority(MCAuthorityEntity mcAuthority) {
		this.mcAuthority = mcAuthority;
	}
}
