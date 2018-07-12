package com.shopping.api.output;

import com.shopping.foundation.domain.User;

public class UserTempData extends User{

	/**
	 * @author:gaohao
	 * @description:输出app端用户实体类的数据
	 * @classType:中转类 
	 */
	private static final long serialVersionUID = 1L;
	private Integer appClickapps;//app点击数
	private Integer fenhongNum;//分红股数量
	private Integer influences;//影响力
	private Integer leader;//领袖力
	private Integer affinitys;//亲和力
	private String userActiveState;//会员状态  1.不活跃;2.7天内登陆过万手；3.15天内下过单；
	
	public Integer getAppClickapps() {
		return appClickapps;
	}
	public void setAppClickapps(Integer appClickapps) {
		this.appClickapps = appClickapps;
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
}
