package com.shopping.api.domain.evaluate;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_pj_AssessingDiscourse",catalog = "shopping")
public class AssessingDiscourseEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:评价的谈论表
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="TEXT(255) COMMENT '描述相关评价说明'")
	private String assessingCharacter;//描述相关评价说明
	@Column(columnDefinition="TEXT(255) COMMENT '店家回复相关说明'")
	private String replyCharacter;//店家回复相关说明
	@Column(columnDefinition="TEXT(255) COMMENT '追评说明'")
	private String appendWord;//追评说明
	@Column(name="assessingTime",columnDefinition="DATETIME COMMENT '生成时间'")
	private Date assessingTime;//评价时间
	@Column(name="replyTime",columnDefinition="DATETIME COMMENT '掌柜回复时间'")
	private Date replyTime;//掌柜回复时间
	@Column(name="appendTime",columnDefinition="DATETIME COMMENT '追评时间'")
	private Date appendTime;//追评时间
	@OneToOne(mappedBy = "assessingDiscourse",fetch = FetchType.LAZY)
	private AppraiseMessageEntity appraiseMessage;//评价相关
	public String getAssessingCharacter() {
		return assessingCharacter;
	}
	public void setAssessingCharacter(String assessingCharacter) {
		this.assessingCharacter = assessingCharacter;
	}
	public String getReplyCharacter() {
		return replyCharacter;
	}
	public void setReplyCharacter(String replyCharacter) {
		this.replyCharacter = replyCharacter;
	}
	public String getAppendWord() {
		return appendWord;
	}
	public void setAppendWord(String appendWord) {
		this.appendWord = appendWord;
	}
	public Date getAssessingTime() {
		return assessingTime;
	}
	public void setAssessingTime(Date assessingTime) {
		this.assessingTime = assessingTime;
	}
	public Date getReplyTime() {
		return replyTime;
	}
	public void setReplyTime(Date replyTime) {
		this.replyTime = replyTime;
	}
	public Date getAppendTime() {
		return appendTime;
	}
	public void setAppendTime(Date appendTime) {
		this.appendTime = appendTime;
	}
	public AppraiseMessageEntity getAppraiseMessage() {
		return appraiseMessage;
	}
	public void setAppraiseMessage(AppraiseMessageEntity appraiseMessage) {
		this.appraiseMessage = appraiseMessage;
	}
}
