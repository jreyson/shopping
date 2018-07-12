package com.shopping.api.domain.weChat;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.shopping.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_weChatAccountInfo")
public class WeChatAccountInfoEntity extends IdEntity {
	/**
	 * @author:akangah
	 * @description:微信的账户信息表,有些功能各个字段都有值，有些功能则没有值
	 */
	private static final long serialVersionUID = 1L;
	private String functionInfo;//微信功能信息,例如：公众号支付，微信登陆。。
	private String applyId;//应用id
	private String applySecretKey;//应用的密钥
	private Long merchantNum;//商户号
	private String grant_type;//微信授权类型
	private String serverSecret;//服务器端密钥
	public String getGrant_type() {
		return grant_type;
	}
	public String getServerSecret() {
		return serverSecret;
	}
	public void setServerSecret(String serverSecret) {
		this.serverSecret = serverSecret;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	public String getFunctionInfo() {
		return functionInfo;
	}
	public void setFunctionInfo(String functionInfo) {
		this.functionInfo = functionInfo;
	}
	public String getApplyId() {
		return applyId;
	}
	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}
	public String getApplySecretKey() {
		return applySecretKey;
	}
	public void setApplySecretKey(String applySecretKey) {
		this.applySecretKey = applySecretKey;
	}
	public Long getMerchantNum() {
		return merchantNum;
	}
	public void setMerchantNum(Long merchantNum) {
		this.merchantNum = merchantNum;
	}
}
