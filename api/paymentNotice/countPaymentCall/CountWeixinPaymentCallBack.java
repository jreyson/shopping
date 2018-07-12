package com.shopping.api.paymentNotice.countPaymentCall;

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

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.api.service.ICountOrderService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
import com.shopping.pay.weixin.WxPayResult;
@Controller
public class CountWeixinPaymentCallBack {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICountOrderService countOrderService;
	@RequestMapping({"/app_countWeixinPayment_CallBack.htm"})
	public void app_countWeixinPayment_CallBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.print("微信支付回调数据开始");
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
		
		CountOrderDomain countOrder=this.countOrderService.getObjById(CommUtil.null2Long(m.get("out_trade_no")));
		if(countOrder!=null){
			if(countOrder.getOrder_status()==10){
				countOrder.setOrder_status(20);
				countOrder.setPayTime(new Date());
				boolean ret=this.countOrderService.update(countOrder);
				if(ret){
					User countUser=this.userService.getObjById(1L);
					double toatal=countOrder.getTotal_price();
					countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
							countUser.getAvailableBalance().doubleValue(), toatal)));
					boolean up_ret=this.userService.update(countUser);
					if(up_ret){
						PredepositLog countUser_log = new PredepositLog();
						countUser_log.setAddTime(new Date());
						countUser_log.setPd_log_user(countUser);
						countUser_log.setPd_op_type("增加");
						countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
						countUser_log.setPd_log_info("订单"+countOrder.getId()+"微信购买拓客点数增加预存款");
						countUser_log.setOrder_id(ApiUtils.integralOrderNum(1L));
						countUser_log.setPd_type("可用预存款");
						countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
						boolean save_ret=this.predepositLogService.save(countUser_log);
						if(save_ret){
							String sendMsg="你已成功购买,等待管理员充值";
							ApiUtils.json(response, "", sendMsg, 0);
							this.send_message(countOrder.getNeededUser(), sendMsg);
							User adminUser=this.userService.getObjById(new Long(20717));
							if(adminUser!=null){
								this.send_message(adminUser, countOrder.getOrder_remark()+"战友已经购买点数,请及时充值");
								return;
							}
						}
					}
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
	private void send_message(User user,String msg){
		if(user.getIs_huanxin()==0){//如果用户没有注册环信
			CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUserName());
			user.setIs_huanxin(1);
			this.userService.update(user);
		}
		String[] users={user.getId().toString()};
		JSONObject messages=new JSONObject();
		messages.put("type", "txt");
		messages.put("msg", msg);
		String sender="150381";
		CommUtil.send_message_to_user(users, messages, sender);
	}
}
