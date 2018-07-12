package com.shopping.api.domain.zhiwei;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.ZhiWei;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_zhiwei_fuzhi")
public class DeputyPosition extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:职位的副职表
	 **/
	public DeputyPosition(){
		super();
	}
	/**
	 * hibrenate @ManyToOne(fetch = FetchType.EAGER) 和 lazy 区别
     * 1,如果是EAGER，那么表示取出这条数据时，它关联的数据也同时取出放入内存中 
	 * 2,如果是LAZY，那么取出这条数据时，它关联的数据并不取出来，在session中，什么时候要用，就什么时候取(再次访问数据库)。 
	 **/
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch=FetchType.EAGER)
	private ZhiWei samePrincipalZhiWei;//对应的同级正职位
	private String name;//副职名字
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
