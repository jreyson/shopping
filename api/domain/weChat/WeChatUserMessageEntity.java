package com.shopping.api.domain.weChat;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_weChatUserMessage",catalog = "shopping")
public class WeChatUserMessageEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:微信用户的信息表
	 */
	private static final long serialVersionUID = 1L;
	@OneToOne
	private User owerSystemUser;//对应的我们系统用户
	@Column(columnDefinition="varchar(200)")
	private String nickname;//微信昵称
	@Column(name="openId",nullable=false,unique=true,columnDefinition="char(100) default 'openId' COMMENT '微信opendId'")  
	private String openId;//微信opendId
	@Column(columnDefinition = "TINYINT(2) unsigned zerofill default 0")//unsigned表示不能存储负数zerofill表示用0填充
	private int sex;//微信用户的性别			
	@Column(columnDefinition="varchar(10)")
	private String language;//微信用户使用的语言
	@Column(columnDefinition="varchar(30)")
	private String city;//城市
	@Column(columnDefinition="varchar(30)")
	private String province;//省份
	@Column(columnDefinition="varchar(30)")
	private String country;//国家
	@Column(columnDefinition="varchar(200)")
	private String headimgurl;//头像url
	@Column(name="unionid",nullable=false,unique=true,columnDefinition="varchar(180) default 'unionid' COMMENT '微信unionid'")  
	private String unionid;//联合Id，在多个应用下实现数据资源共享
	public User getOwerSystemUser() {
		return owerSystemUser;
	}
	public void setOwerSystemUser(User owerSystemUser) {
		this.owerSystemUser = owerSystemUser;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
}
