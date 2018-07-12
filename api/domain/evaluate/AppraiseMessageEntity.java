package com.shopping.api.domain.evaluate;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_pj_AppraiseMessage",catalog = "shopping")
public class AppraiseMessageEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:商品评价表
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private StartsExplainEntity describeStarts;//商品描述相关星星
	@OneToMany(mappedBy = "appraiseMessage",fetch = FetchType.LAZY)
	private List<VVPResourceEntity> vvrResource;//评价的视频资源
	@OneToOne(fetch = FetchType.LAZY)
	private AssessingDiscourseEntity assessingDiscourse;//评价回复实体
	@Column(columnDefinition="BOOLEAN COMMENT '是否匿名评价'")
	private Boolean isAnonymity;//是否匿名评价
	@ManyToOne(fetch = FetchType.LAZY)
	private Goods goods;//针对哪个商品进行评价
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private OrderForm order;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public OrderForm getOrder() {
		return order;
	}
	public void setOrder(OrderForm order) {
		this.order = order;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public StartsExplainEntity getDescribeStarts() {
		return describeStarts;
	}
	public void setDescribeStarts(StartsExplainEntity describeStarts) {
		this.describeStarts = describeStarts;
	}
	public List<VVPResourceEntity> getVvrResource() {
		return vvrResource;
	}
	public void setVvrResource(List<VVPResourceEntity> vvrResource) {
		this.vvrResource = vvrResource;
	}
	public AssessingDiscourseEntity getAssessingDiscourse() {
		return assessingDiscourse;
	}
	public void setAssessingDiscourse(AssessingDiscourseEntity assessingDiscourse) {
		this.assessingDiscourse = assessingDiscourse;
	}
	public Boolean getIsAnonymity() {
		return isAnonymity;
	}
	public void setIsAnonymity(Boolean isAnonymity) {
		this.isAnonymity = isAnonymity;
	}
}	
