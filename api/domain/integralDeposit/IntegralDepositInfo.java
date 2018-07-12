package com.shopping.api.domain.integralDeposit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_appMySelf_integralDepositInfo")
public class IntegralDepositInfo extends IdEntity implements Serializable{
	/**
	 * @author:gaohao
	 * @description:app==>我的模块里面用于获取理财类型(日月年)以及图片
	 */
	private static final long serialVersionUID = 1L;
	private String depositInfo;//日月年描述
	private String path;//理财图片路径
	@Column(nullable=false)
	private Integer depositInfoorder;//存款信息顺序
	
	public IntegralDepositInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IntegralDepositInfo(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public Integer getDepositInfoorder() {
		return depositInfoorder;
	}
	public void setDepositInfoorder(Integer depositInfoorder) {
		this.depositInfoorder = depositInfoorder;
	}
	public String getDepositInfo() {
		return depositInfo;
	}
	public void setDepositInfo(String depositInfo) {
		this.depositInfo = depositInfo;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
}
