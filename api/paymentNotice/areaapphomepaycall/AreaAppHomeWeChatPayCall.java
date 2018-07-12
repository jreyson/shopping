package com.shopping.api.paymentNotice.areaapphomepaycall;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.InputSource;

import com.shopping.api.domain.regionPartner.AreaAppHomePayTemporary;
import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
import com.shopping.pay.weixin.WxPayResult;

@Controller
public class AreaAppHomeWeChatPayCall {
	@Autowired
	@Qualifier("areaCommonpositionServiceImpl")
	private IPartnerFunctionService<AreaCommonposition> areaCommonpositionService;
	@Autowired
	@Qualifier("areaBannerpositionServiceImpl")
	private IPartnerFunctionService<AreaBannerposition> areaBannerpositionService;
	@Autowired
	@Qualifier("areaAppHomePayTemporaryServiceImpl")
	private IPartnerFunctionService<AreaAppHomePayTemporary> areaAppHomePayTemporaryService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@RequestMapping({ "/appWeChatPayAreaAppHomeCallBack.htm"})
	public void app_HomePageWeixinPay_Callback(HttpServletRequest request,
			HttpServletResponse response){
		System.out.print("app端区域站点首页空位付费微信支付回调数据开始");
		String inputLine;
		String notityXml = "";
		String resXml = "";
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("app购买区域合伙人收到的报文：" + notityXml);
		Map<String, String> m = this.parseXmlToList2(notityXml);
		WxPayResult wpr = new WxPayResult();
		wpr.setAppid(m.get("appid").toString());
		wpr.setBankType(m.get("bank_type").toString());
		wpr.setCashFee(m.get("cash_fee").toString());
		wpr.setFeeType(m.get("fee_type").toString());
		wpr.setIsSubscribe(m.get("is_subscribe").toString());
		wpr.setMchId(m.get("mch_id").toString());
		wpr.setNonceStr(m.get("nonce_str").toString());
		wpr.setOpenid(m.get("openid").toString());
		wpr.setOutTradeNo(m.get("out_trade_no").toString());
		wpr.setResultCode(m.get("result_code").toString());
		wpr.setReturnCode(m.get("return_code").toString());
		wpr.setSign(m.get("sign").toString());
		wpr.setTimeEnd(m.get("time_end").toString());
		wpr.setTotalFee(m.get("total_fee").toString());
		wpr.setTradeType(m.get("trade_type").toString());
		wpr.setTransactionId(m.get("transaction_id").toString());
		if ("SUCCESS".equals(wpr.getResultCode())) {
			// 支付成功
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
					+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			String hql="select obj from AreaAppHomePayTemporary as obj where obj.orderNum = " + m.get("out_trade_no");
			List<AreaAppHomePayTemporary> aahpts = this.areaAppHomePayTemporaryService.query(hql, null, -1, -1);
			if (aahpts.size()>0) {
				AreaAppHomePayTemporary aahpt = aahpts.get(0);
				if(aahpt.getOrderStatus()==10){
					boolean appPaymentAreaPlace = AreaPartnerUtils.appPaymentAreaPlace("AliPay",aahpt,areaCommonpositionService,areaBannerpositionService,userService,commonService,areaAppHomePayTemporaryService,predepositLogService);
				}
			}
		} else {
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
					+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";

		}
		BufferedOutputStream out=null;
		try {
			out = new BufferedOutputStream(
					response.getOutputStream());
			out.write(resXml.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private Map<String,String> parseXmlToList2(String xml) {
		Map<String,String> retMap = new HashMap<String,String>();
		try {
			StringReader read = new StringReader(xml);
			// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
			InputSource source = new InputSource(read);
			// 创建一个新的SAXBuilder
			SAXBuilder sb = new SAXBuilder();
			// 通过输入源构造一个Document
			Document doc = (Document) sb.build(source);
			Element root = doc.getRootElement();// 指向根节点
			@SuppressWarnings("unchecked")
			List<Element> es = root.getChildren();
			if (es != null && es.size() != 0) {
				for (Element element : es) {
					retMap.put(element.getName(), element.getValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retMap;
	}
}
