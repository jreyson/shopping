package com.shopping.api.domain.materialCircle;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_mc_vPResource")
public class VPResource extends IdEntity{
	/**
	 * @author:akangah
	 * @description:app==>素材圈视频和图片资源实体
	 */
	private static final long serialVersionUID = 1L;
	public VPResource(){
		
	}
	public VPResource(String path,String name,VPResource coverPhoto,Long size,User user,
			String fileType,MaterialItems materialItems){
		this.path=path;
		this.name=name;
		this.coverPhoto=coverPhoto;
		this.size=size;
		this.user=user;
		this.fileType=fileType;
		this.materialItems=materialItems;
		super.setDeleteStatus(true);
		super.setAddTime(new Date());
		this.sizeUnit="b";
	}
	@Column(columnDefinition = "BIGINT(19) unsigned  default 0 COMMENT '文件大小'")
	private Long size;//文件大小
	@Column(columnDefinition="char(10) default 'b' COMMENT '文件大小单位'")
	private String sizeUnit;//文件大小单位
	@Column(columnDefinition="varchar(100) default ' ' COMMENT '文件路径'")
	private String path;//文件路径
	@Column(columnDefinition="varchar(200) default ' ' COMMENT '文件名字'")
	private String name;//文件名字
	@OneToOne(fetch = FetchType.LAZY,cascade = { javax.persistence.CascadeType.REMOVE })
	private VPResource coverPhoto;
	@ManyToOne(fetch = FetchType.LAZY)
	private MaterialItems materialItems;
	@Column(name="fileType",nullable=false,unique=false,columnDefinition="char(20) default 'fileType' COMMENT '文件类型'")
	private String fileType;//文件类型
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public String getSizeUnit() {
		return sizeUnit;
	}
	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public VPResource getCoverPhoto() {
		return coverPhoto;
	}
	public void setCoverPhoto(VPResource coverPhoto) {
		this.coverPhoto = coverPhoto;
	}
	public MaterialItems getMaterialItems() {
		return materialItems;
	}
	public void setMaterialItems(MaterialItems materialItems) {
		this.materialItems = materialItems;
	}
}
