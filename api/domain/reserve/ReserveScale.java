package com.shopping.api.domain.reserve;

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
@Table(name="shopping_reserve_scale",catalog = "shopping")
public class ReserveScale implements Serializable{
	/**
	 * @author:gaohao
	 * @description:储备金分配比例
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;
	@Column(columnDefinition="int COMMENT '会员活跃等级'")
	private Integer activeRank;
	@Column(columnDefinition="double COMMENT '分配比例'")
	private Double scale;
	@Column(columnDefinition="varchar(100) COMMENT '会员分配比例说明'")
	private String activeScaleExplain;
	@Column(columnDefinition="varchar(20) COMMENT '百分比'")
	private String percentage;
	public ReserveScale() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getActiveRank() {
		return activeRank;
	}
	public void setActiveRank(Integer activeRank) {
		this.activeRank = activeRank;
	}
	public Double getScale() {
		return scale;
	}
	public void setScale(Double scale) {
		this.scale = scale;
	}
	public String getActiveScaleExplain() {
		return activeScaleExplain;
	}
	public void setActiveScaleExplain(String activeScaleExplain) {
		this.activeScaleExplain = activeScaleExplain;
	}
	public String getPercentage() {
		return percentage;
	}
	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}
}
