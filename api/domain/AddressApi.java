 package com.shopping.api.domain;
 

import java.io.Serializable;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.Area;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 @Entity
 @Table(name="shopping_address")
 public class AddressApi extends IdEntity implements Serializable 
 {
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String trueName;
   private String area_info;
   private String zip;
   private String telephone;
   private String mobile;
   @ManyToOne
   private UserApi user;
   @ManyToOne
   private AreaApi area;
 
  
 
   public UserApi getUser() {
	return user;
}

public void setUser(UserApi user) {
	this.user = user;
}

public String getTrueName() {
     return this.trueName;
   }
 
   public void setTrueName(String trueName) {
     this.trueName = trueName;
   }
 
  
 
   public AreaApi getArea() {
	return area;
}

public void setArea(AreaApi area) {
	this.area = area;
}

public String getArea_info() {
     return this.area_info;
   }
 
   public void setArea_info(String area_info) {
     this.area_info = area_info;
   }
 
   public String getZip() {
     return this.zip;
   }
 
   public void setZip(String zip) {
     this.zip = zip;
   }
 
   public String getTelephone() {
     return this.telephone;
   }
 
   public void setTelephone(String telephone) {
     this.telephone = telephone;
   }
 
   public String getMobile() {
     return this.mobile;
   }
 
   public void setMobile(String mobile) {
     this.mobile = mobile;
   }
 }



 
 