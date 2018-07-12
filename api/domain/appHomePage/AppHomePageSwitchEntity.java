package com.shopping.api.domain.appHomePage;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appHome_switch")
public class AppHomePageSwitchEntity extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:app首页付费购买轮播图的开关实体类
	 */
	private static final long serialVersionUID = 1L;
	public AppHomePageSwitchEntity(){
		
	}
	private boolean is_turnOn;//是否开启付费上首页功能
	private boolean is_sendMessage;//如果开启的话,给所有的用户推送消息
	@Column(columnDefinition="varchar(20) COMMENT '付费类型:元/小时;元/天'")
	private String payType;
	@Column(columnDefinition = "INT(8) COMMENT '最大支付的时间'")
	private Integer maxPayNum;
	public boolean getIs_turnOn() {
		return is_turnOn;
	}
	public void setIs_turnOn(boolean is_turnOn) {
		this.is_turnOn = is_turnOn;
	}
	public boolean getIs_sendMessage() {
		return is_sendMessage;
	}
	public void setIs_sendMessage(boolean is_sendMessage) {
		this.is_sendMessage = is_sendMessage;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public Integer getMaxPayNum() {
		return maxPayNum;
	}
	public void setMaxPayNum(Integer maxPayNum) {
		this.maxPayNum = maxPayNum;
	}
}
