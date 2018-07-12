package com.shopping.api.domain.headlines;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_newstest")
public class ArticleContextEntity extends IdEntity{

	/**
	 * 测试
	 */
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(50) COMMENT '标题'")
	private String title;
	@Column(columnDefinition="varchar(2000) COMMENT '内容'")
	private String context;
	@Column(columnDefinition="varchar(20) COMMENT '作者'")
	private String author;
	@Column(columnDefinition="DATETIME COMMENT '日期'")
	private String date;
	@Column(columnDefinition="int(11) COMMENT '浏览次数'")
	private String viewtimes;
	@Column(columnDefinition="varchar(20) COMMENT '分类'")
	private String category;
	@Column(columnDefinition="varchar(100) COMMENT '图片'")
	private String pic;
	@Column(columnDefinition="varchar(100) COMMENT '视频'")
	private String video;
	@Column(columnDefinition="varchar(20) COMMENT '商品'")
	private String goods;
	
	
	public ArticleContextEntity() {
		super();
	}
	public ArticleContextEntity(Date addTime) {
		super(addTime);
	}
	
}
