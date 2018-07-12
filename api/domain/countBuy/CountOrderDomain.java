package com.shopping.api.domain.countBuy;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_count_order")
public class CountOrderDomain extends IdEntity implements Serializable {
	/**
	 * @author:akangah
	 * @description:app购买点数之后的订单实体类 
	 */
	private static final long serialVersionUID = 1L;
	public CountOrderDomain(){
		
	}
	private double total_price;//订单总价
	private double order_status;//10表示未付款订单状态20表示已经付款成功  30表示管理员已经完成对点数发放
	private String order_remark;//订单备注
	private Long order_num;//订单编号
	private Date payTime;//支付时间
	private String pay_way;//支付途径
	@ManyToOne(fetch = FetchType.EAGER)
	private CountPriceDomain countPrice;//保存相应的条数记录
	@ManyToOne(fetch = FetchType.EAGER)
	private User neededUser;
	public CountPriceDomain getCountPrice() {
		return countPrice;
	}
	public void setCountPrice(CountPriceDomain countPrice){
		this.countPrice = countPrice;
	}
	public double getTotal_price() {
		return total_price;
	}
	public void setTotal_price(double total_price) {
		this.total_price = total_price;
	}
	public double getOrder_status() {
		return order_status;
	}
	public void setOrder_status(double order_status) {
		this.order_status = order_status;
	}
	public String getOrder_remark() {
		return order_remark;
	}
	public void setOrder_remark(String order_remark) {
		this.order_remark = order_remark;
	}
	public Long getOrder_num() {
		return order_num;
	}
	public void setOrder_num(Long order_num) {
		this.order_num = order_num;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public String getPay_way() {
		return pay_way;
	}
	public void setPay_way(String pay_way) {
		this.pay_way = pay_way;
	}
	public User getNeededUser() {
		return neededUser;
	}
	public void setNeededUser(User neededUser) {
		this.neededUser = neededUser;
	}
}
