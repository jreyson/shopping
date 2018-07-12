package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_person_mall")
public class PersonMallApi extends IdEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:个人商城的实体类(可以没有这张表,但为了后续功能扩展,新建此类)
	 **/
	public PersonMallApi(){
		
	}
	private static final long serialVersionUID = 1L;
	private String self_photo_introduce;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;//这条记录对应的用户
	@OneToOne(fetch = FetchType.LAZY)
	private Goods person_goods;//此次新建保存的商品
	@Column(columnDefinition = "INT(3) default 0")
	private int delete_status;//这条记录的删除状态
	public String getSelf_photo_introduce() {
		return self_photo_introduce;
	}
	public void setSelf_photo_introduce(String self_photo_introduce) {
		this.self_photo_introduce = self_photo_introduce;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Goods getPerson_goods() {
		return person_goods;
	}
	public void setPerson_goods(Goods person_goods) {
		this.person_goods = person_goods;
	}
	public int getDelete_status() {
		return delete_status;
	}
	public void setDelete_status(int delete_status) {
		this.delete_status = delete_status;
	}
	
	
}
