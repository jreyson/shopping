package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.ApiIdEntity;
import com.shopping.foundation.domain.Accessory;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_group_message")
public class GroupApi extends ApiIdEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GroupApi(){
		
	}
	private Long group_id;
	public Long getGroup_id() {
		return group_id;
	}
	public void setGroup_id(Long group_id) {
		this.group_id = group_id;
	}
	@OneToOne
	private Accessory photo;
	private String groupName;

	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Accessory getPhoto() {
		return photo;
	}
	public void setPhoto(Accessory photo) {
		this.photo = photo;
	}
}
