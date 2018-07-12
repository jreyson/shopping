package com.shopping.api.domain.rank;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_userRankName",catalog = "shopping")
public class UserRankName extends IdEntity{
	/**
	 * @author:gaohao
	 * @description:会员等级名称
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(100) COMMENT '会员等级名称'")
	private String rankName;
	@Column(columnDefinition="varchar(100) COMMENT '会员等级说明'")
	private String rankExplain;
	public UserRankName() {
		super();
	}
	public UserRankName(Date addTime) {
		super(addTime);
	}
	public String getRankName() {
		return rankName;
	}
	public void setRankName(String rankName) {
		this.rankName = rankName;
	}
	public String getRankExplain() {
		return rankExplain;
	}
	public void setRankExplain(String rankExplain) {
		this.rankExplain = rankExplain;
	}
}
