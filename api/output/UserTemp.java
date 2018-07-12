package com.shopping.api.output;

import java.io.Serializable;

import com.shopping.foundation.domain.User;

public class UserTemp implements Serializable{
	/**
	 * @author:akangah
	 * @description:输出用户的中转类,输出其活跃度(sql统计出来的一个总数)
	 * @classType:中转类
	 */
	private static final long serialVersionUID = 1L;
	public UserTemp(){
		super();
	}
	private User user;//当前登陆的用户
	private String liveness;//app上用户的活跃度(sql统计出来的一个总数)
	private AppTransferData guaranteeUser;//该用户的担保人信息
	private Integer fenhongNum;//分红股数量
	private Integer influences;//影响力
	private Integer leader;//领袖力
	private Integer affinitys;//亲和力
	private String userActiveState;//会员状态  1.不活跃;2.7天内登陆过万手；3.15天内下过单；
	private boolean appSeeDataPower;//是否拥有app查看数据实况的权限
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getLiveness() {
		return liveness;
	}
	public void setLiveness(String liveness) {
		this.liveness = liveness;
	}
	public AppTransferData getGuaranteeUser() {
		return guaranteeUser;
	}
	public void setGuaranteeUser(AppTransferData guaranteeUser) {
		this.guaranteeUser = guaranteeUser;
	}
	public Integer getFenhongNum() {
		return fenhongNum;
	}
	public void setFenhongNum(Integer fenhongNum) {
		this.fenhongNum = fenhongNum;
	}
	public Integer getInfluences() {
		return influences;
	}
	public void setInfluences(Integer influences) {
		this.influences = influences;
	}
	public Integer getLeader() {
		return leader;
	}
	public void setLeader(Integer leader) {
		this.leader = leader;
	}
	public Integer getAffinitys() {
		return affinitys;
	}
	public void setAffinitys(Integer affinitys) {
		this.affinitys = affinitys;
	}
	public String getUserActiveState() {
		return userActiveState;
	}
	public void setUserActiveState(String userActiveState) {
		this.userActiveState = userActiveState;
	}
	public boolean isAppSeeDataPower() {
		return appSeeDataPower;
	}
	public void setAppSeeDataPower(boolean appSeeDataPower) {
		this.appSeeDataPower = appSeeDataPower;
	}	
}
