package com.shopping.api.paymentNotice;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.InputSource;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageTemporaryData;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
import com.shopping.pay.weixin.WxPayResult;
@Controller
public class AppHomeWeixinPayCallback {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ICommonService commonService;
	@RequestMapping({ "/app_HomePageWeixinPay_Callback.htm"})
	public void app_HomePageWeixinPay_Callback(HttpServletRequest request,
			HttpServletResponse response){
		System.out.print("微信支付回调数据开始");
		/*	示例报文
		 *  String xml=
		 *  <xml>
				<appid><![CDATA[wxb4dc385f953b356e]]></appid>
				<bank_type><![CDATA[CCB_CREDIT]]></bank_type>
				<cash_fee><![CDATA[1]]></cash_fee>
				<fee_type><![CDATA[CNY]]></fee_type>
				<is_subscribe><![CDATA[Y]]></is_subscribe>
				<mch_id><![CDATA[1228442802]]></mch_id>
				<nonce_str><![CDATA[1002477130]]></nonce_str>
				<openid><![CDATA[o-HREuJzRr3moMvv990VdfnQ8x4k]]></openid>
				<out_trade_no><![CDATA[1000000000051249]]></out_trade_no>
				<result_code><![CDATA[SUCCESS]]></result_code>
				<return_code><![CDATA[SUCCESS]]></return_code>
				<sign><![CDATA[1269E03E43F2B8C388A414EDAE185CEE]]></sign>
				<time_end><![CDATA[20150324100405]]></time_end>
				<total_fee>1</total_fee>
				<trade_type><![CDATA[JSAPI]]></trade_type>
				<transaction_id><![CDATA[1009530574201503240036299496]]></transaction_id>
			</xml>*/
		String inputLine="";
		String notityXml = "";
		String resXml = "";
		BufferedReader reader=null;
		try{
			reader=request.getReader();
			while((inputLine = reader.readLine()) != null) {
				notityXml += inputLine;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			try{
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("接收到的报文："+notityXml);
		Map<String,String> m =this.parseXmlToList2(notityXml);
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
		String hql="select obj from AppHomePageTemporaryData as obj where obj.orderNum="+m.get("out_trade_no").toString();
		List<?> appHomeList=this.commonService.query(hql, null, -1, -1);
		if(appHomeList.size()>0){
			AppHomePageTemporaryData appHome=(AppHomePageTemporaryData) appHomeList.get(0);
			if(appHome!=null){
				String judgeHql="";
				Goods goods=this.goodsService.getObjById(appHome.getGoodsId());
				if("banner".equals(appHome.getVacantPositionType())){
					judgeHql="select obj from AppHomePageEntity as obj where obj.id="+appHome.getVacantPositionId();
					List<?> appHomeBannerList=this.commonService.query(judgeHql, null, -1, -1);
					if(appHomeBannerList.size()>0){
						AppHomePageEntity appHomeBanner=(AppHomePageEntity) appHomeBannerList.get(0);
						appHomeBanner.setFlush_time(appHome.getFlush_time());
						appHomeBanner.setGoods(goods);
						appHomeBanner.setIs_can_buy(false);
						appHomeBanner.setPurchase_timeDuan(appHome.getPurchase_timeDuan());
						appHomeBanner.setStart_time(appHome.getStart_time());
						this.commonService.update(appHomeBanner);
					}
				}else if("common".equals(appHome.getVacantPositionType())){
					judgeHql="select obj from AppHomePageCommonPosition as obj where obj.id="+appHome.getVacantPositionId();
					List<?> appHomeCommonList=this.commonService.query(judgeHql, null, -1, -1);
					if(appHomeCommonList.size()>0){
						AppHomePageCommonPosition appHomeCommon=(AppHomePageCommonPosition) appHomeCommonList.get(0);
						appHomeCommon.setFlush_time(appHome.getFlush_time());
						appHomeCommon.setGoods(goods);
						appHomeCommon.setIs_can_buy(false);
						appHomeCommon.setPurchase_timeDuan(appHome.getPurchase_timeDuan());
						appHomeCommon.setStart_time(appHome.getStart_time());
						this.commonService.update(appHomeCommon);
					}
				}
				User countUser=this.userService.getObjById(1L);
				double toatal=appHome.getTotal();
				countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
						countUser.getAvailableBalance().doubleValue(), toatal)));
				boolean ret=this.userService.update(countUser);
				if(ret){
					PredepositLog countUser_log = new PredepositLog();
					countUser_log.setAddTime(new Date());
					countUser_log.setPd_log_user(countUser);
					countUser_log.setPd_op_type("增加");
					countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
					countUser_log.setPd_log_info("首页空位收入");
					countUser_log.setPd_type("可用预存款");
					countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
					this.predepositLogService.save(countUser_log);
				}
			}
		}
		if ("SUCCESS".equals(wpr.getResultCode())) {
			// 支付成功
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
					+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
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
		Map<String,String> retMap = new HashMap<String, String>();
		try {
			StringReader read = new StringReader(xml);
			// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
			InputSource source = new InputSource(read);
			// 创建一个新的SAXBuilder
			SAXBuilder sb = new SAXBuilder();
			// 通过输入源构造一个Document
			Document doc = (Document) sb.build(source);
			Element root = doc.getRootElement();// 指向根节点
			List<?> es = root.getChildren();//List<Element> es = root.getChildren();
			if (es != null && es.size() != 0) {
				for (Object element : es) {
					retMap.put(((Element) element).getName(), ((Element) element).getValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retMap;
	}
}
