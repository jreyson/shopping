package com.shopping.api.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_area_grade_of_user")
public class AreaGradeOfUser implements Serializable {
	/**
	 * @author:akangah
	 * @description:有职位的用户所属区域的实体类
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private long id;
	private String name;
	private long pid;
	private int sort;
	private int level;
	private String code;
	private String longcode;
	@Transient
	private AreaGradeOfUser area;//临时存放父级
	public AreaGradeOfUser(){
		super();
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
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLongcode() {
		return longcode;
	}
	public void setLongcode(String longcode) {
		this.longcode = longcode;
	}
	public AreaGradeOfUser getArea() {
		return area;
	}
	public void setArea(AreaGradeOfUser area) {
		this.area = area;
	}
}
