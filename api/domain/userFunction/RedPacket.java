package com.shopping.api.domain.userFunction;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_redPacket")
public class RedPacket extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>用户发放红包
	 */
	private static final long serialVersionUID = 1L;
	private Double singleMoney;//单个红包金额
	private Integer redPacketNum;//红包数量
	private Integer surplusNum;//剩余数量
	private Date overdueTime;//过期时间
	private String webUrl;//h5连接
	private String sign;//标记
	@ManyToOne(fetch = FetchType.LAZY)
	private RedPacketTheme redPacketTheme;//主题实体
	@ManyToOne
	private User provideUser;//发送红包用户
	private String fontContent;//文字内容
	private boolean overdueState;//是否过期
	private String rechargeWay;//充值方式:微信充值还是支付宝充值,weixin/alipay
	private Integer orderStatus;//订单状态
	@Temporal(TemporalType.DATE)
	private Date payTime;//支付时间
	private Double moneySum;//红包总额
	private Long runningWaterNum;//流水号
	private Double surplusMoney;//剩余红包金额
	public RedPacket() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RedPacket(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public Double getSingleMoney() {
		return singleMoney;
	}
	public void setSingleMoney(Double singleMoney) {
		this.singleMoney = singleMoney;
	}
	public Integer getRedPacketNum() {
		return redPacketNum;
	}
	public void setRedPacketNum(Integer redPacketNum) {
		this.redPacketNum = redPacketNum;
	}
	public Integer getSurplusNum() {
		return surplusNum;
	}
	public void setSurplusNum(Integer surplusNum) {
		this.surplusNum = surplusNum;
	}
	public Date getOverdueTime() {
		return overdueTime;
	}
	public void setOverdueTime(Date overdueTime) {
		this.overdueTime = overdueTime;
	}
	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public RedPacketTheme getRedPacketTheme() {
		return redPacketTheme;
	}
	public void setRedPacketTheme(RedPacketTheme redPacketTheme) {
		this.redPacketTheme = redPacketTheme;
	}
	public User getProvideUser() {
		return provideUser;
	}
	public void setProvideUser(User provideUser) {
		this.provideUser = provideUser;
	}
	public String getFontContent() {
		return fontContent;
	}
	public void setFontContent(String fontContent) {
		this.fontContent = fontContent;
	}
	public boolean isOverdueState() {
		return overdueState;
	}
	public void setOverdueState(boolean overdueState) {
		this.overdueState = overdueState;
	}
	public String getRechargeWay() {
		return rechargeWay;
	}
	public void setRechargeWay(String rechargeWay) {
		this.rechargeWay = rechargeWay;
	}
	public Integer getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public Double getMoneySum() {
		return moneySum;
	}
	public void setMoneySum(Double moneySum) {
		this.moneySum = moneySum;
	}
	public Long getRunningWaterNum() {
		return runningWaterNum;
	}
	public void setRunningWaterNum(Long runningWaterNum) {
		this.runningWaterNum = runningWaterNum;
	}
	public Double getSurplusMoney() {
		return surplusMoney;
	}
	public void setSurplusMoney(Double surplusMoney) {
		this.surplusMoney = surplusMoney;
	}
}
