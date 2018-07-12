package com.shopping.api.domain;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name="shopping_payment_variety")
public class PaymentWayVariety extends IdEntity implements Serializable{
	/**
	 * @author:akangah
	 * @description:支付方式的实体类
	 */
	private static final long serialVersionUID = 1L;
	public PaymentWayVariety(){
		super();
	}
	private String iconUrl;
	private String paymentName;
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getPaymentName() {
		return paymentName;
	}
	public void setPaymentName(String paymentName) {
		this.paymentName = paymentName;
	}
}
