package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_zhixian")
public class ZhiXianEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:职衔的实体类
	 **/
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private long id;
	private String name;
	private String img_url;
	private Integer rankOrder;//职衔顺序
	@Column(columnDefinition="int COMMENT '升级所需订单数'")
	private Integer upgradeOrderNum;
	@Column(columnDefinition="int COMMENT '升级所需钱数'")
	private Integer upgradeMoney;
	public ZhiXianEntity(){
		super();
	}
	public ZhiXianEntity(long id){
		this.id=id;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImg_url() {
		return img_url;
	}
	public void setImg_url(String img_url) {
		this.img_url = img_url;
	}
	public Integer getRankOrder() {
		return rankOrder;
	}
	public void setRankOrder(Integer rankOrder) {
		this.rankOrder = rankOrder;
	}
	public Integer getUpgradeOrderNum() {
		return upgradeOrderNum;
	}
	public void setUpgradeOrderNum(Integer upgradeOrderNum) {
		this.upgradeOrderNum = upgradeOrderNum;
	}
	public Integer getUpgradeMoney() {
		return upgradeMoney;
	}
	public void setUpgradeMoney(Integer upgradeMoney) {
		this.upgradeMoney = upgradeMoney;
	}
}
