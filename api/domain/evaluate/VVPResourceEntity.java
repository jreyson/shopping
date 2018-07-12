package com.shopping.api.domain.evaluate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_pj_VvpResource",catalog = "shopping")
public class VVPResourceEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:视频和音频和图片的资源表
	 */
	public VVPResourceEntity(){
		
	}
	public VVPResourceEntity(String resName, String resPath, Float resSize,
			String ext, String info, String resType,AppraiseMessageEntity appraiseMessage, VVPResourceEntity surfacePlot,
			String originalFilename) {
		super();
		this.resName = resName;
		this.resPath = resPath;
		this.resSize = resSize;
		this.ext = ext;
		this.info = info;
		this.resType = resType;
		this.appraiseMessage = appraiseMessage;
		this.surfacePlot = surfacePlot;
		this.originalFilename=originalFilename;
	}
	private static final long serialVersionUID = 1L;
	@Column(columnDefinition="varchar(100) COMMENT '资源名字'")
	private String resName;//资源名字
	@Column(columnDefinition="varchar(100) COMMENT '资源路径'")
	private String resPath;//资源路径
	@Column(columnDefinition="Float COMMENT '资源大小'")
	private Float resSize;//资源大小
	@Column(columnDefinition="char(10) COMMENT '资源后缀名'")
	private String ext;//资源后缀名
	@Column(columnDefinition="varchar(50) COMMENT '资源路径'")
	private String info;//资源信息
	@Column(columnDefinition="char(10) COMMENT '资源类型'")
	private String resType;//资源类型
	@ManyToOne(fetch=FetchType.LAZY)
	private AppraiseMessageEntity appraiseMessage;//评价相关联的记录
	@OneToOne( cascade = { javax.persistence.CascadeType.REMOVE })
	private VVPResourceEntity surfacePlot;//封面图
	@Column(columnDefinition="varchar(100) COMMENT '原始文件名字'")
	private String originalFilename;
	@Column(columnDefinition="char(2) default 'kb' COMMENT '文件大小单位'")
	private String sizeUnit="kb";
	@Column(columnDefinition="char(5) default 'yes' COMMENT '文件是否要删除'")
	private String fileIsDelete="yes";//是要删除:yes不是删除要删除:no
	public String getFileIsDelete() {
		return fileIsDelete;
	}
	public void setFileIsDelete(String fileIsDelete) {
		this.fileIsDelete = fileIsDelete;
	}
	public String getSizeUnit() {
		return sizeUnit;
	}
	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
	public String getOriginalFilename() {
		return originalFilename;
	}
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}
	public VVPResourceEntity getSurfacePlot() {
		return surfacePlot;
	}
	public void setSurfacePlot(VVPResourceEntity surfacePlot) {
		this.surfacePlot = surfacePlot;
	}
	public String getResName() {
		return resName;
	}
	public void setResName(String resName) {
		this.resName = resName;
	}
	public String getResPath() {
		return resPath;
	}
	public void setResPath(String resPath) {
		this.resPath = resPath;
	}
	public Float getResSize() {
		return resSize;
	}
	public void setResSize(Float resSize) {
		this.resSize = resSize;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getResType() {
		return resType;
	}
	public void setResType(String resType) {
		this.resType = resType;
	}
	public AppraiseMessageEntity getAppraiseMessage() {
		return appraiseMessage;
	}
	public void setAppraiseMessage(AppraiseMessageEntity appraiseMessage) {
		this.appraiseMessage = appraiseMessage;
	}
	
}
