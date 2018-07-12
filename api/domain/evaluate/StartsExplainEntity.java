package com.shopping.api.domain.evaluate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_pj_StartsExplain",catalog = "shopping")
public class StartsExplainEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:星星的数量功能说明表
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition = "TINYINT(2) unsigned zerofill default 0 COMMENT '星星数量'")
	private Integer startsNum;//星星数量
	@Column(columnDefinition="char(100) COMMENT '星星说明'")
	private String	startExplain;//星星说明
	@Column(columnDefinition = "BIGINT(1) unsigned zerofill default 0  COMMENT '星星用途'")
	private Long	startPurpose;//星星用途，用户区分是那种业务1物流相关2:店铺的服务态度0是描述相关
	public Integer getStartsNum() {
		return startsNum;
	}
	public Long getStartPurpose() {
		return startPurpose;
	}
	public void setStartPurpose(Long startPurpose) {
		this.startPurpose = startPurpose;
	}
	public void setStartsNum(Integer startsNum) {
		this.startsNum = startsNum;
	}
	public String getStartExplain() {
		return startExplain;
	}
	public void setStartExplain(String startExplain) {
		this.startExplain = startExplain;
	}
}
