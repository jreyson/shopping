package com.shopping.api.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.mysql.fabric.xmlrpc.base.Data;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.RespApi;
import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.regionPartner.AreaPartnerEntity;
import com.shopping.api.domain.reserve.ReserveScale;
import com.shopping.api.domain.userAttribute.AppClickNum;
import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.UserTemp;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Express;
import com.shopping.foundation.domain.FenPei;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IPredepositService;
import com.shopping.foundation.service.IUserService;
import com.shopping.messageNotice.alidayu.MessageNotice;
import com.shopping.pay.weixin.WxIndex;
import com.shopping.pay.weixin.utils.GetWxOrderno;
import com.shopping.pay.weixin.utils.RequestHandler;
import com.shopping.pay.weixin.utils.Sha1Util;
import com.shopping.pay.weixin.utils.TenpayUtil;

public class ApiUtils { 
	
	private static String encoding = "UTF-8";

	public static void json(HttpServletResponse response, Object data) {
		json(response, data, null);
	}

	public static void update_goods_imgs(List<Goods> list) {
		for (Goods goods : list) {
			update_goods_img(goods);
		}
	}

	public static void update_goods_img(Goods goods) {
		Accessory goods_main_photo = goods.getGoods_main_photo();
		update_goods_img_ac(goods_main_photo);
		for (Accessory ac : goods.getGoods_photos()) {
			update_goods_img_ac(ac);
		}
	}

	public static void update_goods_img_ac(Accessory ac) {
		String path = ac.getPath();
		if (CommUtil.null2String(path).contains("data/files/store")) {
			path = "gold/" + path;
			ac.setPath(path);
			// ac.setImg_path(path);
		}
	}

	public static void json(HttpServletResponse response, Object result,
			String msg, int status) {
		RespApi resp = new RespApi();
		resp.setMsg(msg);
		resp.setStatus(status);
		resp.setResult(result);
		json(response, resp, null);
	}

	public static void json(HttpServletResponse response, Object result,
			String msg, int status, CustomerFilter filter) {
		RespApi resp = new RespApi();
		resp.setMsg(msg);
		resp.setStatus(status);
		resp.setResult(result);
		json(response, resp, filter);
	}

	public static void json(HttpServletResponse response, Object data,
			CustomerFilter filter) {
		response.setContentType("text/plain;charset=" + encoding);
		response.setCharacterEncoding(encoding);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			if (filter == null) {
				out.write(toJSONString(data));
			} else {
				out.write(toJSONString(data, filter));
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * FastJSON的序列化设置
	 */
	private static SerializerFeature[] features = new SerializerFeature[] {
			// 输出Map中为Null的值
			SerializerFeature.WriteMapNullValue,

			// 如果Boolean对象为Null，则输出为false
			SerializerFeature.WriteNullBooleanAsFalse,

			// 如果List为Null，则输出为[]
			SerializerFeature.WriteNullListAsEmpty,

			// 如果Number为Null，则输出为0
			SerializerFeature.WriteNullNumberAsZero,

			// 输出Null字符串
			SerializerFeature.WriteNullStringAsEmpty,

			// 格式化输出日期
			SerializerFeature.WriteDateUseDateFormat };

	/**
	 * 把Java对象JSON序列化
	 * 
	 * @param obj
	 *            需要JSON序列化的Java对象
	 * @return JSON字符串
	 */
	private static String toJSONString(Object obj) {
		return JSON.toJSONString(obj, features);
	}

	private static String toJSONString(Object obj, CustomerFilter filter) {
		return JSON.toJSONString(obj, filter, features);
	}

	/*
	 * 获取的是Ipv4地址  也就是内网的ip地址，不是终端的ip地址
	 */
	public static String getWebIp() {
		String localip = null;
		String netip = null;
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) netInterfaces
						.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();
				while (address.hasMoreElements()) {
					InetAddress ip = (InetAddress) address.nextElement();
					if (ip.isLoopbackAddress()
							|| ip.getHostAddress().indexOf(":") != -1) {
						continue;
					}
					if (ip.isSiteLocalAddress()) {
						localip = ip.getHostAddress();
					} else {
						netip = ip.getHostAddress();
						break;
					}
				}
				if (netip != null) {
					break;
				}
			}
		} catch (SocketException e) {
		}
		if (netip != null)
			return netip;
		else if (localip != null)
			return localip;
		else
			return "127.0.0.1";
	}

	public static CustomerFilter addIncludes(final List<FilterObj> objs) {
		objs.add(new FilterObj(RespApi.class, "msg,status,result"));
		CustomerFilter filter = new CustomerFilter();

		filter.setIncludes(new HashMap<Class<?>, String[]>(){
			private static final long serialVersionUID = -8411128674046835592L;
			{
				for (FilterObj filterObj : objs) {
					put(filterObj.getClazz(), filterObj.getStr().split(","));
				}
			}
		});
		return filter;
	}

	public static boolean is_null(String... strings) {
		for (int i = 0; i < strings.length; i++) {
			if (CommUtil.null2String(strings[i]).equals("")) {
				return true;
			}
		}
		return false;
	}

	public static void IncludesStoreCart(List<FilterObj> objs) {
		objs.add(new FilterObj(StoreCart.class, "id,store,gcs,total_price"));
		objs.add(new FilterObj(GoodsCart.class, "id,gcs,count,goods,spec_info_key,price"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_name,goods_price,store_price,goods_main_photo"));
		objs.add(new FilterObj(Store.class, "store_name,id"));
		objs.add(new FilterObj(Accessory.class, "name,path"));
	}

	public static void IncludesAddress(List<FilterObj> objs) {
		objs.add(new FilterObj(Address.class,
				"trueName,area_info,telephone,mobile,id"));
	}

	public static void IncludesExpress(List<FilterObj> objs) {
		objs.add(new FilterObj(Express.class,
				"id,expressPrice,cityName,expressAddPrice,expressAddPiece,transName"));
	}

	
	public static String getSignContent(Map<String, String> sortedParams) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		int index = 0;
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = sortedParams.get(key);
			if (StringUtils.areNotEmpty(key, value)) {
				String encode_value = "";
				try {
					encode_value = URLEncoder.encode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				content.append((index == 0 ? "" : "&") + key + "="
						+ encode_value);
				index++;
			}
		}
		return content.toString();
	}
	@Test
	public void test() {
		 System.out.println(getAlipayStr("12345670001","http://localhost","1"));
	}
	//支付宝app支付
	public static String getAlipayStr(String order_id, String url,
			String total_amount) {
		String str = "";
		//创建一个指定格式的dateFormat对象
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String date = dateFormat.format(now);
		//多多猫
		//String pri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKhu01qdYDr/yafI2TUkhBUcJK+2QEbeXIp1gXRwCLNP3jdDpR9hw+Ngm5NaOvJULubBfb8irCs7JZEkn5sLtElmGJazIddO0uKnHBqXddV3QpXfZgo4mtFpG6kanV9h6w9+DHOxGYznHAl5RuLIXBYCey2vlNnl3M6UOlNBsn6xAgMBAAECgYBrdhHozWG5IrsxDmbujfarVUJezQOjc3lNaX0HofcbGEjpr4HpTMHjDx8TW00ikO0/kpG84c1A48KrINen30bNSF+2H8bkfX+ALqJ8e2cgwl5saseKbSNjPO9mpAu4dIkDlpYR3EMU0yI4lFj1qBLlqAhWfrWyBWrsG/oi0vPThQJBANiI0w53CN8MhzqzgNDmoku153EbFqUvfHGbJV+XlGXJGdoYhkX+eFfCP/6nW6CQiTeClWNbzUuE7/GqID5U4hsCQQDHIad4eyWxK1ZiQnA9xEyf2fRo5JtF5bfkY8jHAIyKO6Lz0qrSastxm3ux/3L4KKA2vtUAhFn6dF3eBHE2CA8jAkEArKaUGoGA+lAD9yMvP+HVYCa/Tmj56mXthKve5dR3x5zMVyCc12xqShchbYvFvEXikvc05A9Lpr5tjzRGF00ZJwJAbGQUPY+Ct8poLfoOEID+WHCSClqNbmGZVFdAXZod5cyKaX+9feWlscQ5c20hzpSGiOYdGTfxplObGJOAcDG40wJBALbTtktQJh4gvzsK68kD3CJWu2HeLd96L2yeJu1cfFCND81WX2ayshzDM3B/viF+miQoneNCpqn781FP0W++BRM=";
		//第一商城
		String pri ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJv7nEOQqT/lNeeV5WCWQ+Ov7DKMR2583wbB2juGn6UaMQEuVydGGbl2WPBeoKwQU/SypQHniM/O5As9XsqKKUKBGKaANl834T6H8uWaFoZrD1UBj0uiLA8MPUSCyYSeY87c4Qg64SZw/xgYlsvoZCMYMGCrEMtOLpx49fIQZqRzAgMBAAECgYA4LKsgIg7x9egt2OvclwEoFIkpwx9OiSYA1vtzCI6h9QbE+B1nBXXqNtbc6hdPICo8K8IFJ48717SE37stkgg9XTSgXsEPB99Ajth6ZMVvOasTTuY3Ig0AwobfJ5ZTzCeAxtryczcsNQqxU+P2vVPERhiUNI/KAiUbcS4EoT/QwQJBAMmjZegSdsRpQ1gEsAj3vvOIZuTsErwAOjuVd5uRJofjTAdXPsqgYr8hU0UZFGPAFA025uvFmeU9Rnmnw/pNSrECQQDGCS6Yg+b3QETKcGXMeW9AOmh2Cjh76sPVoI7xW0G1R/2R1k5Oi8kM96qXxxkDtqHigfcT1OqKE+AzZXVxW2JjAkBNHiI2jQFBg2TPQBO3ilFMwQlhWmyb3JZo/XReWjhbVnJYiGSqiIbbxLodPkdPE0JX/3x4Bf4i77mJg8FvdKoxAkByzhXmMultwyNeHKTXUjrNiE7Qx4IRTHs1WDwl7Zp9Jnv41L1WybDHRLjv/msg/PqCXofHRnKlk3oxMYtRKQIzAkA2rJ2YZ78mjFYHzJpaBS4Xe+at8+n9jB+yTqMOSmwnqqiRcNIz23FTInJiugkr5asa2WkBc1zy3jG52z4TFh2l";
		JSONObject biz_content = new JSONObject();
		biz_content.put("timeout_express", "30m");
		biz_content.put("seller_id", "");
		biz_content.put("product_code", "QUICK_MSECURITY_PAY");
		biz_content.put("total_amount", total_amount);
		biz_content.put("subject", "第一商城订单");
		biz_content.put("body", "goods");
		biz_content.put("out_trade_no", order_id);

		Map<String, String> params = new HashMap<String, String>();
		params.put("app_id", "2016121504303051");
		params.put("biz_content", biz_content.toJSONString());
		params.put("charset", "utf-8");
		params.put("format", "json");
		params.put("method", "alipay.trade.app.pay");
		params.put("notify_url", url);
		params.put("sign_type", "RSA");
		params.put("timestamp", date);
		params.put("version", "1.0");
		try{
			String sign = AlipaySignature.rsaSign(params, pri, "utf-8");
			params.put("sign", sign);
			str = getSignContent(params);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return str;
	}
	/***
	 *@author:aknagah
	 *@return:Map(返给前端调用app支付所需要的参数)
	 *@param:order_id:订单id,url:微信通知回掉的url,total_amount:订单总额
	 *@throws UnsupportedEncodingException 
	 *@description:主要是返给前端调用app支付所需要的参数
	 *@function:将xml数据提交给微信的服务器,得到微信返回的数据,移除sign字段,重新生成签名加密(然后返给前端)
	 *@exception:*******
	 *@method_detail:#1:获取prepay_id后，拼接最后请求支付所需要的package
	 *				 #2:主要是进行二次加密签名,二次参与签名的partnerId是商户id,
	 *					密钥也要参与签名
	 *@variable:attach:附加数据 原样返回,totalFee:总金额以分为单位，不带小数点
	 *			spbill_create_ip:订单生成的机器 IP
	 *			notify_url:这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
	 *			mch_id:商户号
	 *			nonce_str:随机字符串(主要保证签名的不可预测性)
	 *			body:商品描述根据情况修改
	 *			out_trade_no:商户订单号
	 ***/
	//========================微信的app支付===============================
	/**
	 * 微信app支付指的是原生app调起微信支付
	 * **/
	//1，在app端进行的支付就叫做是微信的app支付
	//2，当前商户KEY是否正确
    //3，加入签名的参数是否和接口提交的参数个数一致，字段名是否和接口文档一致
	public static Map<String,String> get_weixin_sign_string(String order_id,String url,
			String total_amount) throws UnsupportedEncodingException{
		String mch_id = "1463602702";
		String appid="wxae8cd6dadd2852b7";
		String attach="goods";
		String body="第一商城订单";
		String spbill_create_ip="127.0.0.1";
		String trade_type = "APP";
		String nonce_str=WxIndex.getNonceStr();
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", order_id);
		packageParams.put("total_fee", WxIndex.getMoney(total_amount));
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", url);
		packageParams.put("trade_type", trade_type);
		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.setKey("jrkBv2XBhBofyouKIrNEaEVopHkOHClz");
		String sign = reqHandler.createSign(packageParams);
		String request_url="https://api.mch.weixin.qq.com/pay/unifiedorder";
		String xmlParam = 
				"<xml>" + 
						"<appid>" + appid + "</appid>" + 
						"<mch_id>"+ mch_id + "</mch_id>" +
						"<nonce_str>" + nonce_str+ "</nonce_str>" + 
						"<sign>" + sign + "</sign>"+
						"<body><![CDATA[" + body + "]]></body>" + 
						"<out_trade_no>" + order_id+ "</out_trade_no>" +
						"<attach>" + attach + "</attach>"+
						"<total_fee>" + WxIndex.getMoney(total_amount) + "</total_fee>"+ 
						"<spbill_create_ip>" + spbill_create_ip+ "</spbill_create_ip>" + 
						"<notify_url>" + url+ "</notify_url>" + 
						"<trade_type>" + trade_type+ "</trade_type>" + 
				"</xml>";
		String prepay_id="";
		Map<String,String> prepayMap=GetWxOrderno.getPayNo(request_url, xmlParam);//#1
		if(prepayMap!=null){
			prepay_id=prepayMap.get("prepay_id");
		}
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String timestamp = Sha1Util.getTimeStamp();
		finalpackage.put("appid", appid);
		finalpackage.put("partnerid", mch_id);
		finalpackage.put("prepayid",prepay_id);
		finalpackage.put("noncestr", nonce_str); 
		finalpackage.put("timestamp", timestamp);  
		finalpackage.put("package", "Sign=WXPay");  
		String secondSign=reqHandler.createSign(finalpackage);//#2
		Map<String, String> map=new HashMap<String,String>();
		map.put("appid", appid);
		map.put("partnerid", mch_id);
		map.put("prepayid", prepay_id);
		map.put("package", "Sign=WXPay");
		map.put("noncestr", nonce_str);
		map.put("timestamp", timestamp);
		map.put("sign",secondSign);
		return map;
	}
	/***
	 *@author:aknagah
	 *@return:String(一段随机的字符串)
	 *@param:***
	 *@description:获取需要h5微信支付的url地址
	 *@function:获取到微信h5支付的url地址，然后直接转跳到h5支付界面,这里不需要进行二次加密
	 *@exception:*******
	 *@method_detail:**
	 *@variable:appid：微信分配的公众账号ID（企业号corpid即为此appId）
	 *				这个参数需要到微信开放平台获取
	 *			mch_id：微信支付分配的商户号
	 *			device_info：终端设备号(门店号或收银设备ID)，
	 *						注意：PC网页或公众号内支付请传"WEB"
	 *			nonce_str：随机字符串，不长于32位。推荐随机数生成算法
	 *			sign：微信所需要的签名，默认用md5算法进行加密
	 *			sign_type：签名类型
	 *			body：商品简单描述，该字段须严格按照规范传递
	 *			detail：商品的详情描述
	 *			attach：附加数据，在查询API和支付通知中原样返回，
	 *					该字段主要用于商户携带订单的自定义数据
	 *			out_trade_no：商户系统内部的订单号,32个字符内、
	 *						可包含字母, 其他说明见商户订单号
	 *			fee_type：符合ISO 4217标准的三位字母代码，
	 *					  默认人民币：CNY，其他值列表详见货币类型
	 *			total_fee：订单总金额，单位为分，详见支付金额
	 *			spbill_create_ip：必须传正确的用户端IP,详见获取用户ip指引
	 *			time_start：订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日
	 *						9点10分10秒表示为20091225091010。
	 *			time_expire：订单失效时间，格式为yyyyMMddHHmmss，
	 *						如2009年12月27日9点10分10秒表示为20091227091010。
	 *						其他详见时间规则  注意：最短失效时间间隔必须大于5分钟
	 *			goods_tag：商品标记，代金券或立减优惠功能的参数，
	 *						说明详见代金券或立减优惠
	 *			notify_url：接收微信支付异步通知回调地址，
	 *						通知url必须为直接可访问的url，不能携带参数
	 *			trade_type：H5支付的交易类型为MWEB
	 *			product_id；trade_type=NATIVE，此参数必传。
	 *						此id为二维码中包含的商品ID，商户自行定义。
	 *			limit_pay：no_credit--指定不能使用信用卡支付
	 *			openid:trade_type=JSAPI，此参数必传，用户在商户appid下
	 *					的唯一标识。openid如何获取，
	 *					可参考【获取openid】。企业号请使用【企业号OAuth2.0接口】
	 *					获取企业号内成员userid，再调用【企业号userid转openid接口】
	 *					进行转换
	 *			scene_info:该字段用于上报支付的场景信息,
	 *				针对H5支付有以下三种场景,请根据对应场景上报,
	 *				H5支付不建议在APP端使用，针对场景1，2请接入APP支付，
	 *				不然可能会出现兼容性问题
	 *			
	 ***/
	//=========================微信的h5支付============================
	/**
	 *在外部浏览器中进行的支付叫做h5支付，在微信浏览器中进行的h支付叫做公众号支付
	 * */
	//1，当前商户KEY是否正确
    //2，加入签名的参数是否和接口提交的参数个数一致，字段名是否和接口文档一致
	//3，appid是在微信开放平台中获取，表示哪个应用对哪个商户进行支付
	//4，spbill_create_ip指的是用户提交数据时终端的IP  getIpAddr获取到终端IP地址
	public static String app_h5WeiXin_payment(String order_id,String notify_url,
			String total_amount,HttpServletRequest request){
		String appid="wxae8cd6dadd2852b7";
		String mch_id="1463602702";
		String nonce_str=WxIndex.getNonceStr();
		String body="第一商城订单";
		String total_fee=WxIndex.getMoney(total_amount);
		String spbill_create_ip=getIpAddr(request);
		String trade_type="MWEB";
		Map<String,JSONObject>  scene_infoMap=new HashMap<String, JSONObject>();
		JSONObject obj=new JSONObject();
		obj.put("type", "Wap");
		obj.put("wap_name", "第一商城海报支付");
		obj.put("wap_url", "www.d1sc.com");
		scene_infoMap.put("h5_info", obj);
		String scene_info=JSON.toJSONString(scene_infoMap).toString();
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("body", body);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", order_id);
		packageParams.put("total_fee", WxIndex.getMoney(total_amount));
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url",notify_url);
		packageParams.put("trade_type", trade_type);
		packageParams.put("scene_info",scene_info);
		RequestHandler reqHandler = new RequestHandler(null, null);
		//赋值Secret
		reqHandler.setKey("jrkBv2XBhBofyouKIrNEaEVopHkOHClz");
		//这里进行签名
		String sign = reqHandler.createSign(packageParams);
		String request_url="https://api.mch.weixin.qq.com/pay/unifiedorder";
		String xmlParam =
				"<xml>" + 
						"<appid>" + appid + "</appid>" + 
						"<mch_id>"+ mch_id + "</mch_id>" + 
						"<nonce_str>"+ nonce_str+"</nonce_str>"+ 
						"<sign>"+sign+"</sign>"+
						"<body><![CDATA["+body+"]]></body>"+ 
						"<out_trade_no>"+order_id+"</out_trade_no>" + 
						"<total_fee>"+total_fee+"</total_fee>"+
						"<spbill_create_ip>"+spbill_create_ip+"</spbill_create_ip>" + 
						"<notify_url>"+notify_url+"</notify_url>"+ 
						"<trade_type>"+trade_type+"</trade_type>"+
						"<scene_info>"+scene_info+"</scene_info>"+
				"</xml>";
		//这里向微信服务器发送请求
		Map<String,String> resultMap=GetWxOrderno.getH5PaymentUrl(request_url, xmlParam);
		String mweb_url="";//h5的跳转链接
		if(resultMap!=null){
			mweb_url=resultMap.get("mweb_url");
		}
		return mweb_url;
	}
	 /** 
     * 获取用户真实IP地址，不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址, 
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值 
     * @return ip
     */
    public static final String getIpAddr(HttpServletRequest request) {
    	String ip = request.getHeader("x-forwarded-for"); 
        System.out.println("x-forwarded-for ip: " + ip);
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {  
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if( ip.indexOf(",")!=-1 ){
                ip = ip.split(",")[0];
            }
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
            System.out.println("Proxy-Client-IP ip: " + ip);
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
            System.out.println("WL-Proxy-Client-IP ip: " + ip);
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
            System.out.println("HTTP_CLIENT_IP ip: " + ip);
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
            System.out.println("HTTP_X_FORWARDED_FOR ip: " + ip);
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("X-Real-IP");  
            System.out.println("X-Real-IP ip: " + ip);
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
            System.out.println("getRemoteAddr ip: " + ip);
        } 
        System.out.println("获取客户端ip: " + ip);
        return ip;  
    }
	public static boolean order_verify(OrderForm of){
		//得到订单的商品总额
		double amount = of.getMaijia_get_price()
				+ of.getMaijia_tuijian_get_price()
				+ of.getDaogou_get_price()
				+ of.getDaogou_tuijian_get_price()
				+ of.getShui_wu_price() + of.getChu_pei_price()
				+ of.getZhi_ji_price() + of.getYang_lao_price()
				+ of.getXian_ji_price() + of.getFen_hong_price()
				+ of.getZeng_gu_price() + of.getCtj()
				+ of.getZhanlue_price() +of.getGet_by_auction_gold();
		//得到订单的商品总额
		double goods_amount = of.getGoods_amount().doubleValue();
		//得到订单总额
		double total_price = amount + CommUtil.null2Double(of.getShip_price());
		//得到商品总额之间的差价
		double def_price = Math.abs(amount-goods_amount);
		//得到订单总额之间的差价    of.getTotalPrice()得到订单总额
		double def_total_price = Math.abs(total_price-CommUtil.null2Double(of.getTotalPrice()));
		//计算导购金的时候回有一定误差,控制在0.5元
		if (def_price > 0.5 || of.getDaogou_get_price()<0 || def_total_price>0.5) {
			return false;
		}else{
			return true;
		}
	}
	/***
	 *@author:gaohao
	 *@return:Long 积分订单号
	 *@param:userId 用户id
	 *@description:通过用户的Id以及当前时间戳拼接一个最大19位的随机订单号
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static Long integralOrderNum(Long userId){
		int length = userId.toString().length();
		Long time=System.currentTimeMillis();
		String date=time.toString();
		if (length<6) {
			Integer num = TenpayUtil.buildRandom(6-length);
			date+=num.toString();
		}
		String string = date+userId.toString();
		return Long.valueOf(string.trim());
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:积分充值，更新用户余额以及保存信息到用户积分表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static void updateUserAvailableBalance(IntegralRechargeEntity integralRechargeEntity,IUserService userService,IPredepositLogService predepositLogService,ICommonService commonService){
		Double count=integralRechargeEntity.getRechargeQuantity();
		User user = integralRechargeEntity.getUser();
		Double availableBalance=user.getAvailableBalance().doubleValue()+count;
		user.setAvailableBalance(BigDecimal.valueOf(availableBalance));
		boolean ret=userService.update(user);
		if(ret){
			PredepositLog predepositLog=new PredepositLog();
			predepositLog.setAddTime(new Date());
			predepositLog.setDeleteStatus(false);
			predepositLog.setPd_log_user(user);
			predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());
			predepositLog.setPd_log_amount(BigDecimal.valueOf(count));
			predepositLog.setPd_op_type("增加");
			predepositLog.setPd_log_info("积分充值");
			predepositLog.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
			predepositLogService.save(predepositLog);
			ApiUtils.updateUserRenk(0, user, commonService, userService);//更新会员等级
		}
	}
	/***
	 *@author:gaohao
	 *@return:String 格式化的时间
	 *@param:dateTime 时间,flag 取值0,1
	 *@description:0 返回yyyy-MM-dd 00:00:00日期
	 *       	   1 返回yyyy-MM-dd 23:59:59日期
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static String weeHours(String dateTime, int flag) {
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sftTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=new Date();
		try {
			if (dateTime!=null) {
				date = sft.parse(dateTime);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		// 时分秒（毫秒数）
		long millisecond = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second
				* 1000;
		// 凌晨00:00:00
		cal.setTimeInMillis(cal.getTimeInMillis() - millisecond);
		if (flag == 0) {			
			return sftTime.format(cal.getTime());
		} else if (flag == 1) {
			// 凌晨23:59:59
			cal.setTimeInMillis(cal.getTimeInMillis() + 23 * 60 * 60 * 1000
					+ 59 * 60 * 1000 + 59 * 1000);
		}
		return sftTime.format(cal.getTime());
	}
	/***
	 *@author:gaohao
	 *@return:long 两个时间的差
	 *@param:beginTime 开始时间, endTime 结束时间
	 *@description:判断两个日期之间的天数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static long acquisitionTimeSegment(String beginTime,String endTime) {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
        Date begin;
        Date end;
        long days=0;
        try {
			begin = df.parse(beginTime);			
        	end = df.parse(endTime);  
            long time1 = begin.getTime();  
            long time2 = end.getTime();  
            long diff ;  
            diff = time2 - time1; 
            days = diff / (1000 * 60 * 60 * 24); 
        } catch (ParseException e) {
			e.printStackTrace();
		}
		return days;
	}
	/***
	 *@author:gaohao
	 *@return:String 返回格式化的时间
	 *@param:dateTime 时间,flag 0表示每月第一天，1表示每月最后一天
	 *@description:返回每月的第一天或者最后一天
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	 public static String getFirstday_Lastday_Month(String dateTime,int flag) {
	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	        Date date=new Date();
			try {
				date = df.parse(dateTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);
	        Date theDate = calendar.getTime();
	        String day_first;
	        if (flag==0) {
	        	//每月第一天
		        GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
		        gcLast.setTime(theDate);
		        gcLast.set(Calendar.DAY_OF_MONTH, 1);
		        day_first = df.format(gcLast.getTime());
		        StringBuffer str = new StringBuffer().append(day_first).append(" 00:00:00");
		        day_first = str.toString();
		        return day_first;
			}	        
	        if (flag==1) {
	        	//每月最后一天
		        calendar.add(Calendar.MONTH, 1);    //加一个月
		        calendar.set(Calendar.DATE, 1);        //设置为该月第一天
		        calendar.add(Calendar.DATE, -1);    //再减一天即为上个月最后一天
		        String day_last = df.format(calendar.getTime());
		        StringBuffer endStr = new StringBuffer().append(day_last).append(" 23:59:59");
		        day_last = endStr.toString();
		        return day_last;
			}
	        return "";
	    }
		/***
		 *@author:gaohao
		 *@return:String 格式化的时间
		 *@param:date 时间, sign 0,1(0表示取时间前面的天数，1表示取时间后面的天数) dayNum 天数
		 *@description:传入一个时间，取时间的前后多少天的格式化时间
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
	 public static String getFirstday_Lastday(Date date,int sign,int dayNum){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (sign==0) {
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - dayNum);
		}
		if (sign==1){
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + dayNum);
		} 
		Date today = calendar.getTime();  
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		String result = format.format(today); 
		return result;
	 }
		/***
		 *@author:gaohao
		 *@return:void
		 *@param:**choice 1.支付宝充值；2.微信充值；3.积分理财
		 *@description:积分理财，更新用户余额或者保存信息到用户积分表，当积分理财的时候，同时更新用户的余额
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static void updateUserAndDeposit(IntegralDepositEntity integralDepositEntity,IUserService userService,IPredepositLogService predepositLogService,IIntegralDepositService integralDepositService,int choice,ICommonService commonService){			
			Integer count = integralDepositEntity.getDepositQuantity();
			User user = integralDepositEntity.getUser();
			PredepositLog predepositLog=new PredepositLog();
			predepositLog.setPd_op_type("增加");
			predepositLog.setPd_log_amount(BigDecimal.valueOf(count));
			if (choice==1) {
				predepositLog.setPd_log_info("订单"+integralDepositEntity.getDepositOrderNum()+"支付宝购买积分理财");
			}else if (choice==2){
				predepositLog.setPd_log_info("订单"+integralDepositEntity.getDepositOrderNum()+"微信购买积分理财");
			}else if (choice==3) {
				predepositLog.setPd_op_type("减少");
				predepositLog.setPd_log_amount(BigDecimal.valueOf(-count));
				predepositLog.setPd_log_info("订单"+integralDepositEntity.getDepositOrderNum()+"积分购买积分理财");
				Double availableBalance=user.getAvailableBalance().doubleValue()-count;
				user.setAvailableBalance(BigDecimal.valueOf(availableBalance));
				boolean ret=userService.update(user);
				if (!ret) {//如果没有扣除用户积分，则将理财数据状态改为未付款
					System.out.println(user.getId()+"积分购买理财异常");
					integralDepositEntity.setOrderStatus(10);
					integralDepositService.update(integralDepositEntity);
				}
			}
			predepositLog.setAddTime(new Date());
			predepositLog.setDeleteStatus(false);
			predepositLog.setPd_log_user(user);
			predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());
			predepositLog.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
			boolean save = predepositLogService.save(predepositLog);
			ApiUtils.updateUserRenk(integralDepositEntity.getDepositQuantity(),user, commonService, userService);//更新会员等级
		}
		/***
		 *@author:gaohao
		 *@return:void
		 *@param:**userId 用户id
		 *@description:积分理财，更新用户理财项目的到期状态
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static void updateDepositStatus(Long userId,IIntegralDepositService integralDepositService,IPredepositLogService predepositLogService,IUserService userService,ICommonService commonService){
			String hql="from IntegralDepositEntity as obj where obj.user.id=" + userId + " and obj.orderStatus=20 and obj.depositStatus=0";
			List<IntegralDepositEntity> list = integralDepositService.query(hql, null, -1, -1);
			if (list.size()>0) {
				for (IntegralDepositEntity i:list) {
					long day = ApiUtils.acquisitionTimeSegment(CommUtil.formatShortDate(new Date()),CommUtil.formatShortDate(i.getEndTime()));
					if (day<0) {
						i.setDepositStatus(1);
						boolean is = integralDepositService.update(i);
						if(!is){
							System.out.println(userId+"理财状态更新异常。");
						}else {
							User user = userService.getObjById(userId);;
							AllocateWagesUtils.allocateMoneyToUser(
									user.getId() + "", i.getDepositAll(), "订单号"+i.getDepositOrderNum()+"积分理财到期", "",
									predepositLogService, userService, commonService, 1);
						}
					}			
				}
			}
		}
		/***
		 *@author:gaohao
		 *@return:boolean
		 *@param:**userName 用户昵称  mobile 手机号码
		 *@description:推送通知短信
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static boolean pushNoticeSMS(Object userName,Object mobile){
			String templateId="SMS_118790166";
			JSONObject obj=new JSONObject();
			obj.put("name",userName);
			boolean ret=CommUtil.sendNote((String)userName, obj, (String)mobile,templateId);
			return ret;
		}
		/***
		 *@author:gaohao
		 *@return:String
		 *@param:**
		 *@description:正则表达式判断类型(用户名，手机号，万手号)
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static String judgmentType(String message){
			final String REGEX_MOBILE="^[1][3456789][0-9]{9}$";
			final String REGEX_ID="^(?!0)\\d{1,7}$";
			final String REGEX_NAME="^[0-9]*$";
			boolean is=false;
			is = Pattern.matches(REGEX_MOBILE, message);
			if (is) {
				return "mobile";
			}
			is=Pattern.matches(REGEX_ID, message);
			if (is) {
				return "id";
			}
			is=Pattern.matches(REGEX_NAME, message);
			if (!is) {
				return "userName";
			}
			return "";
		}
		/***
		 *@author:gaohao
		 *@return:boolean
		 *@param:**
		 *@description:app验证管理员权限
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static boolean isAdmin(String userId,String password,IUserService userService){
			if ("".equals(CommUtil.null2String(password))){
				return false;
			}
			Long user_id = CommUtil.null2Long(userId);
			if (user_id.longValue()==-1) {
				return false;
			}
			if (user_id!=1&&user_id!=20717) {
				return false;
			}
			String pw = userService.getObjById(user_id).getPassword();
			if ((user_id==1&&pw.equals(password))||(user_id==20717&&pw.equals(password))) {
				return true;
			}
			return false;
		}
		/***
		 *@author:gaohao
		 *@return:User
		 *@param:**
		 *@description:app用户验证帐号密码
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static User erifyUser(String userId,String password,IUserService userService){
			if ("".equals(CommUtil.null2String(password))){
				return null;
			}
			Long user_id = CommUtil.null2Long(userId);
			if (user_id.longValue()==-1) {
				return null;
			}
			User user = userService.getObjById(user_id);
			if (user!=null) {
				if (user.getPassword().equals(password)) {
					return user;
				}
			}
			return null;
		}
		/***
		 *@author:gaohao
		 *@return:boolean
		 *@param:**
		 *@description:推送职位任命通知短信
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static boolean pushNotice(User user){
			if (user.getMobile()==null) {
				return false;
			}
			String type = judgmentType(user.getMobile().trim().toString());
			if (!type.equals("mobile")) {
				return false;
			}
			String templateId="SMS_118640173";
			AreaGradeOfUser areaGradeOfUser = user.getAreaGradeOfUser();
			String area="";
			String detail="";
			BuMen bumen = user.getBumen();
			if (bumen!=null&&!bumen.equals("")) {
				detail=detail+bumen.getName();
			}
			if (areaGradeOfUser!=null&&!areaGradeOfUser.equals("")) {
				area=areaGradeOfUser.getName();
				detail=detail+area;
			}
			ZhiWei zhiwei = user.getZhiwei();
			if (zhiwei!=null&&!zhiwei.equals("")) {
				detail=detail+zhiwei.getName();
			}
			JSONObject obj=new JSONObject();
			obj.put("name",user.getUserName());
			obj.put("detail",detail);
			obj.put("area",area);
			boolean ret=false;
			MessageNotice ms=new MessageNotice();
			JSONObject jsonobj=ms.appSendMessage(user.getUserName(), obj, user.getMobile(), templateId);
			if(jsonobj!=null){
				if(jsonobj.getJSONObject("result")!=null){
					ret=jsonobj.getJSONObject("result").getBoolean("success");
				}
			}
			return ret;
		}
		/***
		 *@author:gaohao
		 *@return:List<UserTemp>
		 *@param:**guaranteeUser 担保人信息，传null，方法中会获取
		 *@description:格式化职位管理中的信息
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static List<UserTemp> getUserTemps(List<User> users,AppTransferData guaranteeUser,ICommonService commonService,IUserService userService){
			List<UserTemp> out_put=new ArrayList<UserTemp>();
			for (User user:users) {
				String sql="select count(1) from shopping_user_clickapps as obj where user_id ="+user.getId();
				List<?> count = commonService.executeNativeNamedQuery(sql);
				UserTemp usertemp=new UserTemp();
				usertemp.setUser(user);
				AppTransferData gt=new AppTransferData();
				if (guaranteeUser==null&&user.getDan_bao_ren()!=null&&(!user.getDan_bao_ren().equals(""))) {
					String hql="select obj from User AS obj where obj.userName = '"+user.getDan_bao_ren()+"'";
					List<User> danbao = userService.query(hql, null, 0, 1);
					if (danbao.size()>0) {
						gt.setFifthData(danbao.get(0).getId());
						gt.setSecondData(danbao.get(0).getUsername());
					}			
				}
				usertemp.setGuaranteeUser(gt);
				if (count.size()>0) {
					usertemp.setLiveness(count.get(0).toString());			
				}else {
					usertemp.setLiveness("0");
				}
				out_put.add(usertemp);
			}	
			return out_put;
		}
		/***
		 *@author:gaohao
		 *@return:User
		 *@param:**
		 *@description:app验证用户职位级别的权限
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static User erifyPowerUser(String userId,String password,IUserService userService,Integer power){
			if ("".equals(CommUtil.null2String(password))){
				return null;
			}
			Long user_id = CommUtil.null2Long(userId);
			if (user_id.longValue()==-1) {
				return null;
			}
			User user = userService.getObjById(user_id);
			if (user!=null) {
				if (user.getPassword().equals(password)&&user.getZhiwei().getPositionOrder()<=power) {
					return user;
				}
			}
			return null;
		}
		/***
		 *@author:gaohao
		 *@return:List<UserTemp>
		 *@param:**guaranteeUser 担保人信息，传null，方法中会获取
		 *@description:格式化职位管理中的信息
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static List<UserTemp> getAppUserTemps(List<User> users,AppTransferData guaranteeUser,ICommonService commonService,IUserService userService){
			List<UserTemp> out_put=new ArrayList<UserTemp>();
			for (User user:users) {
				UserTemp usertemp=new UserTemp();
				String hql="select obj from AppClickNum as obj where obj.user.id = " + user.getId();
				@SuppressWarnings("unchecked")
				List<AppClickNum> query = commonService.query(hql, null, -1, -1);		
				usertemp.setUser(user);
				AppTransferData gt=new AppTransferData();
				if (guaranteeUser==null&&user.getDan_bao_ren()!=null&&(!user.getDan_bao_ren().equals(""))) {
					String hql1="select obj from User AS obj where obj.userName = '"+user.getDan_bao_ren()+"'";
					List<User> danbao = userService.query(hql1, null, 0, 1);
					if (danbao.size()>0) {
						gt.setFifthData(danbao.get(0).getId());
						gt.setSecondData(danbao.get(0).getUsername());
					}			
				}
				if (guaranteeUser==null) {
					usertemp.setGuaranteeUser(gt);
				}else {
					usertemp.setGuaranteeUser(guaranteeUser);
				}
				if (query.size()>0) {
					usertemp.setLiveness(query.get(0).getClickNum().toString());		
				}else {
					usertemp.setLiveness("0");
				}
//				try {
//					String userActiveState = ApiUtils.getUserActiveState(user, commonService);
//					Integer fenhongNum = ApiUtils.getFenhongNum(user, commonService);
//					Map<String, Integer> userAttribute = ApiUtils.getUserAttribute(user, commonService, userService);
//					usertemp.setAffinitys(userAttribute.get("affinitys"));
//					usertemp.setLeader(userAttribute.get("leader"));
//					usertemp.setInfluences(userAttribute.get("influences"));
//					usertemp.setFenhongNum(fenhongNum);
//					usertemp.setUserActiveState(userActiveState);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}	
				out_put.add(usertemp);
			}
			return out_put;
		}
		/***
		 *@author:gaohao
		 *@return:User
		 *@param:**
		 *@description:确认收货中获取用户上级，用于发放职级金，如果该用户是副职，则上级为比他等级高一级的正级。如副总裁，上级为大区总裁。
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static User getSeniorUser(User user,IUserService userService){
			AreaGradeOfUser area = user.getAreaGradeOfUser();
			ZhiWei zhiwei = user.getZhiwei();
			BuMen bumen = user.getBumen();
			String hql="";
			if (zhiwei==null||bumen==null) {
				return null;
			}
			if (zhiwei.getPositionOrder()<=10) {
				if (bumen.getSuperiorBumen()!=null&&zhiwei.getPositionOrder()==10) {
					if (bumen.getSuperiorBumen()!=null) {
						hql="select obj from User as obj where obj.zhiwei.positionOrder = 5  and obj.zhiwei.name not like '%副%' and obj.bumen.id = " + bumen.getSuperiorBumen().getId();
						List<User> seniorUser = userService.query(hql, null, -1, -1);
						if (seniorUser.size()>0) {
							return seniorUser.get(0);
						}else {
							return null;
						}					
					}else {
						return null;
					}
				}else {
					return null;
				}
			}else {
				if (area!=null&&area.getPid()!=0) {
					if (area.getPid()==1) {
						hql="select obj from User as obj where obj.zhiwei.positionOrder = 10 and obj.bumen.id = " + bumen.getId() +" and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
					}else{
						hql="select obj from User as obj where obj.areaGradeOfUser.id = " + area.getPid() +" and obj.bumen.id = " + bumen.getId() +"  and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
					}
					List<User> seniorUser = userService.query(hql, null, -1, -1);
					if (seniorUser.size()>0) {
						return seniorUser.get(0);
					}else {
						return null;
					}	
				}else {
					if (zhiwei.getPositionOrder()==15) {
						hql="select obj from User as obj where obj.bumen.id = " + bumen.getSuperiorBumen().getId() +" and obj.zhiwei.positionOrder = 5 and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
						List<User> seniorUser = userService.query(hql, null, -1, -1);
						if (seniorUser.size()>0) {
							return seniorUser.get(0);
						}	
					}
					return null;
				}
			}
		}
		/***
		 *@author:gaohao
		 *@return:boolean
		 *@param:**user 要增加余额的用户；wages：增加的余额数；explain：收支说明。
		 *@description:发放各种金
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static boolean distributeWages(User user,double addWages,double wages,String explain,IUserService userService,IPredepositLogService predepositLogService,String payType,ICommonService commonService){
			synchronized (payType) {
				if (user==null) {
					return false;
				}
				if (!"".equals(payType)&&!ApiUtils.distributionState(user, commonService, userService, payType)) {
					return false;
				}
				user=userService.getObjById(user.getId());
				BigDecimal availableBalance = BigDecimal.valueOf(CommUtil.add(
						user.getAvailableBalance(),Double.valueOf(addWages)));
				user.setAvailableBalance(availableBalance);
//				boolean is= userService.update(user);
				String sql="update shopping_user set availableBalance="+ availableBalance.toString()
								+ " where id="+user.getId();
				int num = commonService.executeNativeSQL(sql);
				if (num>0) {
					PredepositLog predepositLog=new PredepositLog();
					predepositLog.setAddTime(new Date());
					predepositLog.setDeleteStatus(false);
					predepositLog.setPd_log_user(user);
					predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());
					predepositLog.setPd_log_amount(BigDecimal.valueOf(wages));
					if (addWages<0||wages<0) {
						predepositLog.setPd_op_type("减少");
					}else {
						predepositLog.setPd_op_type("增加");
					}			
					predepositLog.setPd_log_info(explain);
					predepositLog.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
					if (payType.equals("chubei")) {
						predepositLog.setZhi_ji_price(wages);
					}else if (payType.equals("daogou")) {
						predepositLog.setDaogou_get_price(wages);
					}else if (payType.equals("danbao")) {
						predepositLog.setDaogou_tuijian_get_price(wages);
					}else if (payType.equals("zhaoshang")) {
						predepositLog.setMaijia_tuijian_get_price(wages);
					}else if (payType.equals("xianji")) {
						predepositLog.setXian_ji_price(wages);
					}else if (payType.equals("fenhong")) {
						predepositLog.setFen_hong_price(wages);
					}else if (payType.equals("zenggu")) {
						predepositLog.setZeng_gu_price(wages);
					}else if (payType.equals("huokuan")) {
						predepositLog.setMaijia_get_price(wages);
					}else if (payType.equals("yanglao")) {
						predepositLog.setYang_lao_price(wages);
					}else if (payType.equals("shuiwu")) {
						predepositLog.setShui_wu_price(wages);
					}else if (payType.equals("chupei")) {
						predepositLog.setChu_pei_price(wages);
					}
					boolean save = predepositLogService.save(predepositLog);
					if (save) {
						ApiUtils.updateUserRenk(0,user, commonService, userService);//更新会员等级
						ApiUtils.asynchronousUrl("http://www.d1sc.com/appCheckUserInfo.htm?userId="+user.getId(), "GET");
						return save;
					}			
				}
				return false;
			}
		}
		/***
		 *@author:gaohao
		 *@return:void
		 *@param:**
		 *@description:给指定用户发送消息
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static final void sendMessageToSpecifiedUser(User user,String msg,IUserService userService,String sender){
			if(user.getIs_huanxin()==0){//如果用户没有注册环信
				CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUserName());
				user.setIs_huanxin(1);
				userService.update(user);
			}
			String[] users={user.getId().toString()};
			JSONObject messages=new JSONObject();
			messages.put("type", "txt");
			messages.put("msg", msg);
			CommUtil.send_message_to_user(users, messages, sender);
		}
		/***
		 *@author:gaohao
		 *@return:user
		 *@param:**
		 *@description:获取用户上级，如该用户为副职，则上级为正职,用于职位管理中获取上级
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static User getUserSenior(User user,IUserService userService){
			AreaGradeOfUser area = user.getAreaGradeOfUser();
			ZhiWei zhiwei = user.getZhiwei();
			zhiwei=zhiwei==null?new ZhiWei():zhiwei;
			BuMen bumen = user.getBumen();
			String hql="";
			if (zhiwei.getId()==0||bumen==null) {
				return null;
			}
			if (zhiwei.getPositionOrder()<=10||(zhiwei.getId()>=152&&zhiwei.getId()<=156)) {//无地区的职位
				if (zhiwei.getName().indexOf("副")==-1) {//正职
					if (zhiwei.getPositionOrder()==5) {
						return null;
					}
					hql="select obj from User as obj where obj.zhiwei.positionOrder < "+zhiwei.getPositionOrder()+"  and obj.zhiwei.name not like '%副%' and obj.bumen.id = " + bumen.getSuperiorBumen().getId();
					List<User> seniorUser = userService.query(hql, null, -1, -1);
					if (seniorUser.size()>0) {
						return seniorUser.get(0);
					}
				}else {//副职
					hql="select obj from User as obj where obj.zhiwei.positionOrder = "+(zhiwei.getPositionOrder()-5)+"  and obj.zhiwei.name not like '%副%' and obj.bumen.id = " + bumen.getId();
					List<User> seniorUser = userService.query(hql, null, -1, -1);
					if (seniorUser.size()>0) {
						return seniorUser.get(0);
					}
				}				
			}else {
				if (area!=null) {
					if (zhiwei.getName().indexOf("副")==-1) {
						if (area.getPid()==1) {
							hql="select obj from User as obj where obj.zhiwei.positionOrder = 10 and obj.bumen.id = " + bumen.getId() +" and obj.zhiwei.name not like '%副%' and obj.zhiwei.id <> 0 order by obj.zhiwei.positionOrder";
						}else{
							hql="select obj from User as obj where obj.areaGradeOfUser.id = " + area.getPid() +" and obj.bumen.id = " + bumen.getId() +" and obj.zhiwei.id <> 0  and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
						}
						List<User> seniorUser = userService.query(hql, null, -1, -1);
						if (seniorUser.size()>0) {
							return seniorUser.get(0);
						}else {
							return null;
						}
					}else {
						if (area.getPid()==1) {
							hql="select obj from User as obj where obj.areaGradeOfUser.id = " + area.getId() +" and obj.bumen.id = " + bumen.getId() +" and obj.zhiwei.id <> 0 and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
						}else{
							hql="select obj from User as obj where obj.areaGradeOfUser.id = " + area.getId() +" and obj.bumen.id = " + bumen.getId() +" and obj.zhiwei.id <> 0  and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
						}
						List<User> seniorUser = userService.query(hql, null, -1, -1);
						if (seniorUser.size()>0) {
							return seniorUser.get(0);
						}else {
							return null;
						}
					}
				}
			}
			return null;
		}
		/***
		 *@author:akangah
		 *@return:Id:id goodsCartService购物车service goodsService 商品service
		 *@param:**
		 *@description:跟新每个商品的销量
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static void statSaleNum(Long Id,IGoodsCartService goodsCartService,IGoodsService goodsService){
			String queryGoodsCart="select obj from GoodsCart as obj where obj.of.id="+Id.toString();
			List<GoodsCart> goodsCartList=goodsCartService.query(queryGoodsCart, null, -1, -1);
			Goods goods=null;
			for(GoodsCart goodsCart:goodsCartList){
				goods=goodsCart.getGoods();
				goods.setGoods_salenum(goods.getGoods_salenum()+goodsCart.getCount());
				goodsService.update(goods);
				goods=null;
			}
		} 
		/***
		 *@author:gaohao
		 *@return:**
		 *@param:**
		 *@description:获取用户分红股的数量(id:28911 70369 77089)
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static Integer getFenhongNum(User user,ICommonService commonService){
			if (user==null) {
				return 0;
			}
			int fenhongNum=0;
			//新分红，单价10.5
			String newGoods="SELECT "+ 
							  "round(SUM(so.totalPrice) / 10.5,0) "+ 
							"FROM "+
							  "shopping_orderform AS so "+ 
							  "RIGHT JOIN "+
							    "(SELECT "+ 
							      "sg.COUNT, "+
							      "sg.goods_id, "+
							      "sg.of_id "+ 
							    "FROM "+
							      "shopping_goodscart as sg "+
							    "WHERE sg.goods_id IN (77089, 70369)) AS temp "+
							    "ON temp.of_id = so.id "+ 
							"WHERE so.order_status IN (20, 30, 40, 50, 60) "+
							"and so.user_id = " +user.getId().toString();
			//旧分红股，单价有10，有10.5
			String usedGoods="SELECT "+
							  "round(SUM(temp.count),0) "+
							"FROM "+
							  "shopping_orderform AS so "+
							  "RIGHT JOIN "+
							    "(SELECT "+
							     "sg.COUNT, "+
							      "sg.goods_id, "+
							      "sg.of_id "+ 
							    "FROM "+
							      "shopping_goodscart as sg "+ 
							    "WHERE sg.goods_id =28911) AS temp "+
							    "ON temp.of_id = so.id "+ 
							"WHERE so.order_status IN (20, 30, 40, 50, 60) "+ 
							"and so.user_id =" +user.getId().toString();
			List<?> newGoodNum = commonService.executeNativeNamedQuery(newGoods);
			List<?> usedGoodsNum = commonService.executeNativeNamedQuery(usedGoods);
			if (newGoodNum.size()>0) {
				fenhongNum+=CommUtil.null2Int(newGoodNum.get(0));
			}
			if (usedGoodsNum.size()>0) {
				fenhongNum+=CommUtil.null2Int(usedGoodsNum.get(0));
			}
			return fenhongNum;
		}
		/***
		 *@author:gaohao
		 *@return:**
		 *@param:**
		 *@description:判断用户是否属于动态会员1：30天以上无登录；2：30天内有登录；3：30天内有销售或者招人；4：10天内有销售或者招人；5：3天内有销售或者招人。
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static String getUserActiveState(User user,ICommonService commonService){
			if (user==null) {
				return "1";
			}
			return ApiUtils.getUserState(commonService, user);
		}
		/***
		 *@author:gaohao
		 *@return:**
		 *@param:**
		 *@description:获取用户的亲和力：担保用户中动态人数，领袖力：全部下属人数，统计到省一级，影响力：担保用户数量。
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
		public static Map<String, Integer> getUserAttribute(User user,ICommonService commonService,IUserService userService){
			Map<String, Integer> types=new HashMap<String, Integer>();
			//影响力
			String sql="SELECT COUNT(1) FROM shopping_user AS su WHERE su.dan_bao_ren = '"+user.getUserName()+"'";
			List<?> influences = commonService.executeNativeNamedQuery(sql);
			if (influences.size()>0) {
				types.put("influences", CommUtil.null2Int(influences.get(0)));
			}
			BuMen bumen = user.getBumen();
			ZhiWei zhiwei = user.getZhiwei();
			if (zhiwei!=null&&bumen!=null) {
				long zhiweiId = zhiwei.getId();
				String ids="";
				if ((zhiweiId>=300&&zhiweiId<=305)||zhiweiId==124||(zhiweiId>=187&&zhiweiId<=191)) {
					String bumenIdSql="select id from ecm_bumen where superiorBumen_id =" + bumen.getId();
					List<?> bumenIds = commonService.executeNativeNamedQuery(bumenIdSql);		
					for (Object obj : bumenIds) {
						ids+=CommUtil.null2String(obj)+",";
					}
				}
				
				
				if (zhiwei.getName().indexOf("副")==-1) {//正职
					sql="SELECT "+
							  "COUNT(1) "+
							"FROM "+
							  "ecm_zhiwei AS ez "+ 
							  "RIGHT JOIN "+
							    "(SELECT "+ 
							      "su.zhiwei_id "+ 
							    "FROM "+
							      "shopping_user AS su "+ 
							    "WHERE su.zhiwei_id <> 0 "+ 
							      "AND su.bumen_id in ("+ ids + user.getBumen().getId()+")) AS temp "+ 
							    "ON temp.zhiwei_id = ez.id "+ 
							"WHERE ez.positionOrder > " + zhiwei.getPositionOrder();
					List<?> leaders = commonService.executeNativeNamedQuery(sql);//领袖力
					if (leaders.size()>0) {
						types.put("leader", CommUtil.null2Int(leaders.get(0)));
					}
					//亲和力
					sql="SELECT ez.positionOrder FROM ecm_zhiwei AS ez WHERE ez.positionOrder > "+zhiwei.getPositionOrder()+" ORDER BY ez.positionOrder LIMIT 0,1";
					List<?> zhiweiPositionOrder=commonService.executeNativeNamedQuery(sql);
					if (zhiweiPositionOrder.size()>0) {
						sql="SELECT "+
							  "temp.id "+ 
							"FROM "+
							  "ecm_zhiwei AS ez "+ 
							  "RIGHT JOIN "+ 
							    "(SELECT "+ 
							      "su.id, "+
							      "su.zhiwei_id "+ 
							    "FROM "+
							      "shopping_user AS su "+ 
							    "WHERE bumen_id IN ("+ ids + bumen.getId() +") "+ 
							      "AND zhiwei_id <> 0) AS temp "+ 
							    "ON ez.id = temp.zhiwei_id "+
							"WHERE ez.positionOrder = " + CommUtil.null2Int(zhiweiPositionOrder.get(0));
						List<?> userIds = commonService.executeNativeNamedQuery(sql);
						int activeNum=0;
						for (Object obj : userIds) {
							User u = userService.getObjById(CommUtil.null2Long(obj));
							String status = getUserActiveState(u, commonService);
							if (!status.equals("1")) {
								activeNum++;
							}
						}
						types.put("affinitys", activeNum);
					}
				}else {//副职
					sql="SELECT "+
							  "COUNT(1) "+
							"FROM "+
							  "ecm_zhiwei AS ez "+ 
							  "RIGHT JOIN "+
							    "(SELECT "+ 
							      "su.zhiwei_id "+ 
							    "FROM "+
							      "shopping_user AS su "+ 
							    "WHERE su.zhiwei_id <> 0 "+
							      "AND su.bumen_id in ("+ ids + user.getBumen().getId()+")) AS temp "+ 
							    "ON temp.zhiwei_id = ez.id "+ 
							"WHERE ez.positionOrder > " + zhiwei.getPositionOrder();
					List<?> leaders = commonService.executeNativeNamedQuery(sql);//领袖力
					if (leaders.size()>0&&CommUtil.null2Int(leaders.get(0))!=0) {
						types.put("leader", CommUtil.null2Int(leaders.get(0))+1);
					}
//					亲和力
					sql="SELECT "+
							  "temp.id "+ 
							"FROM "+
							  "ecm_zhiwei AS ez "+ 
							  "RIGHT JOIN "+ 
							    "(SELECT "+ 
							      "su.id, "+
							      "su.zhiwei_id "+ 
							    "FROM "+
							      "shopping_user AS su "+ 
							    "WHERE bumen_id IN ("+ ids + bumen.getId() +") "+ 
							      "AND zhiwei_id <> 0) AS temp "+ 
							    "ON ez.id = temp.zhiwei_id "+
							"WHERE ez.name not like '%副%' and  ez.positionOrder = " + zhiwei.getPositionOrder();
					List<?> userIds = commonService.executeNativeNamedQuery(sql);
					int activeNum=0;
					for (Object obj : userIds) {
						User u = userService.getObjById(CommUtil.null2Long(obj));
						String status = getUserActiveState(u, commonService);
						if (!status.equals("1")) {
							activeNum++;
						}
					}
					types.put("affinitys", activeNum);
				}
			}
			if (types.get("influences")==null) {
				types.put("influences", 0);
			}
			if (types.get("leader")==null) {
				types.put("leader", 0);
			}
			if (types.get("affinitys")==null) {
				types.put("affinitys", 0);
			}
			return types;
		}
		/***
		 *@author:gaohao
		 *@return:**
		 *@param:**
		 *@description:查询单个会员是否注册环信
		 *@function:**
		 *@exception:*******
		 *@method_detail:***
		 *@variable:*******
		 ***/
	public static boolean isHaveHuanxin(Long userId){
		boolean isHaveHuanxin=true;	
		try {
			HttpGet get=new HttpGet("https://a1.easemob.com/a1241328428/firstlives/users/" + userId);
			get.setHeader("Authorization","Bearer "+CommUtil.huanxin_token());
			HttpClient client = new DefaultHttpClient();		
			HttpResponse execute = client.execute(get);
			String resp = EntityUtils.toString(execute.getEntity());
			JSONObject jsonobj = JSONObject.parseObject(resp);
			if (jsonobj!=null) {
				String error=jsonobj.getString("error");
				if ("service_resource_not_found".equals(error)) {
					isHaveHuanxin=false;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isHaveHuanxin;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:查询单个会员指定时间内有无下单或者招人
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static boolean getUserState(Integer days,ICommonService commonService,User user){
		String orderTime = ApiUtils.getFirstday_Lastday(new Date(), 0, days);
		orderTime=ApiUtils.weeHours(orderTime, 0);
		String user_sql="select so.id from shopping_orderform as so where so.addTime >= '" + orderTime +"' and so.user_id = " +user.getId();
		List<?> orders = commonService.executeNativeNamedQuery(user_sql);
		if (orders.size() > 0) {
			return true;
		}
		user_sql="SELECT obj.id FROM shopping_user AS obj WHERE obj.addTime >= '" + orderTime + "' AND obj.dan_bao_ren = '" + user.getUserName() + "'";
		List<?> users = commonService.executeNativeNamedQuery(user_sql);
		if (users.size() > 0) {
			return true;
		}
		if (days == 15) {
			String loginDate = CommUtil.formatLongDate(user.getLoginDate());
			if (!"".equals(loginDate)) {
				long day = ApiUtils.acquisitionTimeSegment(loginDate,orderTime);
				if (day <= 0) {
					return true;
				}
			}				
			user_sql="select obj.id from shopping_user_appClickNum as obj where obj.loginDate >= '" + orderTime + "' and obj.user_id = " + user.getId();
			List<?> clicks = commonService.executeNativeNamedQuery(user_sql);
			if (clicks.size() > 0) {
				return true;
			}
		}
		return false;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:获取用户的亲和力：担保用户中动态人数，领袖力：全部下属人数，统计到省一级，影响力：担保用户数量。
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static Map<String, Integer> getUserAttributes(User user,ICommonService commonService,IUserService userService){
		Map<String, Integer> types=new HashMap<String, Integer>();
		//影响力
		String sql="SELECT count(1) FROM shopping_user AS su WHERE su.dan_bao_ren = '"+user.getUserName()+"'";
		List<?> influences = commonService.executeNativeNamedQuery(sql);
//		int current_page=0;
//		int pageSize=100;
		int activeNum=0;//亲和力
		if (influences.size()>0) {
			int danbaoorenNum = CommUtil.null2Int(influences.get(0));
			types.put("influences",danbaoorenNum);
			
//			int cycleNum=danbaoorenNum%pageSize==0?danbaoorenNum/pageSize:danbaoorenNum/pageSize+1;
			String orderTime = ApiUtils.getFirstday_Lastday(new Date(), 0, 30);
			orderTime=ApiUtils.weeHours(orderTime, 0);
//			for (int i = 0; i < cycleNum; i++) {
				sql="SELECT su.id FROM shopping_user AS su WHERE su.loginDate >='" + orderTime + "' and su.dan_bao_ren = '"+user.getUserName()+"'";
				List<?> affinitys = commonService.executeNativeNamedQuery(sql);
//				current_page++;
				if (affinitys.size()>0) {
					for (Object obj : affinitys) {
						User u = userService.getObjById(CommUtil.null2Long(obj));
						boolean userState = getUserState(15,commonService,u);
						if (userState) {
							activeNum++;
						}
					}	
				}
//			}
		}
		types.put("affinitys", activeNum);
		sql="";
		//领袖力		
		BuMen bumen = user.getBumen();
		ZhiWei zhiwei = user.getZhiwei();
		if (zhiwei!=null&&bumen!=null) {
			long zhiweiId = zhiwei.getId();
			String ids="";
			if ((zhiweiId>=300&&zhiweiId<=305)||zhiweiId==116||zhiweiId==124||(zhiweiId>=187&&zhiweiId<=191)) {
				String bumenIdSql="select id from ecm_bumen where superiorBumen_id =" + bumen.getId();
				List<?> bumenIds = commonService.executeNativeNamedQuery(bumenIdSql);		
				for (Object obj : bumenIds) {
					ids+=CommUtil.null2String(obj)+",";
				}
			}
			if (zhiweiId==109) {//创始人
				sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND su.zhiwei_id = ez.id";				
			}else if (zhiwei.getPositionOrder()==5) {//总指挥，院长
				sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND ez.positionOrder > 5 AND su.zhiwei_id = ez.id AND su.bumen_id IN (" + ids + bumen.getId() + ")";				
			}else if (zhiwei.getName().indexOf("副")!=-1 && zhiwei.getPositionOrder()==10) {//副总指挥，副院长
				if (!"".equals(ids)&&ids.substring(ids.length()-1, ids.length()).equals(",")) {
					ids= ids.substring(0,ids.length() - 1);
				}
				if (!ids.equals("")) {
					sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND ez.positionOrder >= 10 AND su.zhiwei_id = ez.id AND su.bumen_id IN (" + ids + ")";
				}
			}else if (zhiwei.getName().indexOf("副")==-1 && zhiwei.getPositionOrder()==10) {//执行总裁,
				sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND ez.positionOrder > 10 AND su.zhiwei_id = ez.id AND su.bumen_id = " + bumen.getId();
			}else if (zhiwei.getPositionOrder()==15) {
				if (zhiwei.getName().indexOf("副执行总裁")!=-1) {//副执行总裁
					sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND ez.positionOrder >= 15 And ez.name not like '%副执行总裁%' AND su.zhiwei_id = ez.id AND  AND su.bumen_id = " + bumen.getId();
				}else {//大区总裁
//					sql="SELECT COUNT(1) FROM shopping_user AS su,ecm_zhiwei AS ez WHERE ez.positionOrder <= 25 AND ez.positionOrder > 15 AND su.zhiwei_id = ez.id AND su.bumen_id = " + bumen.getId();
					sql="SELECT "+ 
							  "COUNT(1) "+ 
							"FROM "+
							  "shopping_area_grade_of_user AS sagou "+ 
							  "RIGHT JOIN "+ 
							    "(SELECT "+ 
							      "temp.id, "+
							      "temp.areaGradeOfUser_id "+
							    "FROM "+
							     " ecm_zhiwei AS ez "+
							     " RIGHT JOIN "+ 
							        "(SELECT "+ 
							          "su.id, "+
							          "su.zhiwei_id, "+
							          "su.areaGradeOfUser_id "+ 
							        "FROM "+
							          "shopping_user AS su "+ 
							        "WHERE su.bumen_id = " + bumen.getId() + ") AS temp "+ 
							        "ON temp.zhiwei_id = ez.id "+
							    "WHERE ez.positionOrder <= 20 "+ 
							      "AND ez.positionOrder > 15) AS temp2 "+
							   " ON sagou.id = temp2.areaGradeOfUser_id "+ 
							"WHERE sagou.pid = " + user.getAreaGradeOfUser().getId();
				}
			}else if(zhiwei.getPositionOrder()==20){
				if (zhiwei.getName().indexOf("副大区总裁")!=-1) {//副大区总裁
					sql="SELECT "+ 
							  "COUNT(1) "+ 
							"FROM "+
							  "shopping_area_grade_of_user AS sagou "+ 
							  "RIGHT JOIN "+ 
							    "(SELECT "+ 
							      "temp.id, "+
							      "temp.areaGradeOfUser_id "+
							    "FROM "+
							     " ecm_zhiwei AS ez "+
							     " RIGHT JOIN "+ 
							        "(SELECT "+ 
							          "su.id, "+
							          "su.zhiwei_id, "+
							          "su.areaGradeOfUser_id "+ 
							        "FROM "+
							          "shopping_user AS su "+ 
							        "WHERE su.bumen_id = " + bumen.getId() + ") AS temp "+ 
							        "ON temp.zhiwei_id = ez.id "+
							    "WHERE ez.positionOrder <= 20 "+ 
							      "AND ez.positionOrder > 15" +
							      "And ez.name not like '%副大区总裁%') AS temp2 "+
							   " ON sagou.id = temp2.areaGradeOfUser_id "+ 
							"WHERE sagou.pid = " + user.getAreaGradeOfUser().getId();
				}else {//总裁
					sql="SELECT "+ 
							  "COUNT(1) "+ 
							"FROM "+
							  "shopping_area_grade_of_user AS sagou "+ 
							  "RIGHT JOIN "+ 
							    "(SELECT "+ 
							      "temp.id, "+
							      "temp.areaGradeOfUser_id "+
							    "FROM "+
							     " ecm_zhiwei AS ez "+
							     " RIGHT JOIN "+ 
							        "(SELECT "+ 
							          "su.id, "+
							          "su.zhiwei_id, "+
							          "su.areaGradeOfUser_id "+ 
							        "FROM "+
							          "shopping_user AS su "+ 
							        "WHERE su.bumen_id = " + bumen.getId() + ") AS temp "+ 
							        "ON temp.zhiwei_id = ez.id "+
							    "WHERE ez.positionOrder <= 25 "+ 
							      "AND ez.positionOrder > 20) AS temp2 "+
							   " ON sagou.id = temp2.areaGradeOfUser_id "+ 
							"WHERE sagou.pid = " + user.getAreaGradeOfUser().getId();
				}
			}else if (zhiwei.getPositionOrder()==25&&zhiwei.getName().indexOf("副总裁")!=-1) {
				sql="SELECT "+ 
						  "COUNT(1) "+ 
						"FROM "+
						  "shopping_area_grade_of_user AS sagou "+ 
						  "RIGHT JOIN "+ 
						    "(SELECT "+ 
						      "temp.id, "+
						      "temp.areaGradeOfUser_id "+
						    "FROM "+
						     " ecm_zhiwei AS ez "+
						     " RIGHT JOIN "+ 
						        "(SELECT "+ 
						          "su.id, "+
						          "su.zhiwei_id, "+
						          "su.areaGradeOfUser_id "+ 
						        "FROM "+
						          "shopping_user AS su "+ 
						        "WHERE su.bumen_id = " + bumen.getId() + ") AS temp "+ 
						        "ON temp.zhiwei_id = ez.id "+
						    "WHERE ez.positionOrder <= 25 "+ 
						      "AND ez.positionOrder > 20 " +
						      "And ez.name not like '%副总裁%') AS temp2 "+
						   " ON sagou.id = temp2.areaGradeOfUser_id "+ 
						"WHERE sagou.pid = " + user.getAreaGradeOfUser().getId();
			}
			if (!"".equals(sql)) {
				List<?> leaders = commonService.executeNativeNamedQuery(sql);
				if (leaders.size()>0) {
					types.put("leader", CommUtil.null2Int(leaders.get(0)));
				}
			}		
		}
		if (types.get("influences")==null) {
			types.put("influences", 0);
		}
		if (types.get("leader")==null) {
			types.put("leader", 0);
		}
		if (types.get("affinitys")==null) {
			types.put("affinitys", 0);
		}
		return types;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:生成可变长度的随机字符串
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static String getRandomString(int length){
	    //定义一个字符串（A-Z，a-z，0-9）即62位；
	    String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
	    //由Random生成随机数
	        Random random=new Random();  
	        StringBuffer sb=new StringBuffer();
	        //长度为几就循环几次
	        for(int i=0; i<length; ++i){
	          //产生0-61的数字
	          int number=random.nextInt(62);
	          //将产生的数字通过length次承载到sb中
	          sb.append(str.charAt(number));
	        }
	        //将承载的字符转换成字符串
	        return sb.toString();
	  }
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:查询是否有该手机用户
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static boolean verifyPhoneExistenceState(String phone,IUserService userService){
		String judgmentType = judgmentType(phone);
		if (judgmentType=="mobile") {
			String hql="select obj from User AS obj where obj.mobile = " + phone;
			List<User> users = userService.query(hql, null, 0, 1);
			if (users.size()==0) {
				return false;
			}
		}
		return true;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:查询单个会员的活跃状态；1：30天以上无登录；2：30天内有登录；3：30天内有销售或者招人；4：10天内有销售或者招人；5：3天内有销售或者招人。
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static String getUserState(ICommonService commonService,User user){
		Long day = 60l;
		String orderTime = ApiUtils.getFirstday_Lastday(new Date(), 0, 30);
		orderTime=ApiUtils.weeHours(orderTime, 0);
		String user_sql="select so.addTime from shopping_orderform as so where so.order_status in (20,30,40,50,60) and so.addTime >= '" + orderTime +"' and so.user_id = " +user.getId() + " order by so.addTime DESC limit 0,1";
		List<?> orders = commonService.executeNativeNamedQuery(user_sql);
		if (orders.size()>0) {
			day=ApiUtils.acquisitionTimeSegment(CommUtil.null2String(orders.get(0)), CommUtil.formatLongDate(new Date()));
		}

		user_sql="SELECT obj.addTime FROM shopping_user AS obj WHERE obj.addTime >= '" + orderTime + "' AND obj.dan_bao_ren = '" + user.getUserName() + "' order by obj.addTime DESC limit 0,1";
		List<?> users = commonService.executeNativeNamedQuery(user_sql);
		if (users.size()>0) {
			Long num = ApiUtils.acquisitionTimeSegment(CommUtil.null2String(users.get(0)), CommUtil.formatLongDate(new Date()));
			day=num<day?num:day;
		}	
		if (day > 30) {
			String loginDate = CommUtil.formatLongDate(user.getLoginDate());
			if (!"".equals(loginDate)) {
				long days = ApiUtils.acquisitionTimeSegment(loginDate,orderTime);
				if (days <= 0) {
					return "2";
				}
			}				
			user_sql="select obj.id from shopping_user_appClickNum as obj where obj.loginDate >= '" + orderTime + "' and obj.user_id = " + user.getId();
			List<?> clicks = commonService.executeNativeNamedQuery(user_sql);
			if (clicks.size() > 0) {
				return "2";
			}
		}
		if (day<=3) {
			return "5";
		}else if (day<=10) {
			return "4";
		}else if (day<=30) {
			return "3";
		}
		return "1";
	}
	public static String conactRequestUrl(Map<String, String> packageParams,
			String domainAddress,int flag){
		StringBuffer contactStr=new StringBuffer();
		Set<Map.Entry<String,String>> paramsSet=null;
		String key="";
		String value="";
		String ret="";
		if("".equals(domainAddress)||packageParams.size()<=0){
			return ret;
		}
		paramsSet=packageParams.entrySet();
		Iterator<Map.Entry<String, String>> iterator=paramsSet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry=iterator.next();
			key=entry.getKey();
			value=entry.getValue();
			if(!"".equals(CommUtil.null2String(key))&&!"".equals(CommUtil.null2String(value))){
				contactStr.append(key+"="+value+"&");
			}
		}
		if(flag==0){
			ret=domainAddress+contactStr.toString().substring(0, contactStr.length()-1);
		}else{
			String redirectUriQuondamStr=contactStr.toString().substring(0, contactStr.length()-1);
			try {
				ret=domainAddress+URLEncoder.encode(redirectUriQuondamStr,"utf-8").replace("+", "%20").toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
//	public static void main(String[] args) {
//		Map<String,String> params=new LinkedHashMap<String, String>();
//		String weiXinStr="https://open.weixin.qq.com/connect/oauth2/authorize?";
//		params.put("appid", "wxaec3c9b0a6071875");
//		params.put("redirect_uri", "http://www.d1sc.com");
//		params.put("response_type", "code");
//		params.put("scope", "snsapi_userinfo");
//		params.put("state", "STATE#wechat_redirect");
//		String a=ApiUtils.conactRequestUrl(params, weiXinStr, 0);
//		System.out.println(a);
//	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:requestURL:访问url  requestMethod:请求方式（post；get）
	 *@description:异步请求url，不等待返回结果
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static void asynchronousUrl(String requestURL,String requestMethod){
//        StringBuffer httpUrl = new StringBuffer("http://localhost:8080/shopping/app_acquire_zhiwei.htm");
        StringBuffer httpUrl = new StringBuffer(requestURL);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpUrl.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // 请求方式设置 POST
            connection.setRequestMethod(requestMethod);
            // 设置维持长连接
            connection.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            connection.setRequestProperty("Charset", "UTF-8");
            //根据需求设置读超时的时间  ReadTimeout , 
            //java是这样解释的。 意思是已经建立连接，并开始读取服务端资源。如果到了指定的时间，没有可能的数据被客户端读取，则报异常。客户端不再等待
            connection.setReadTimeout(1);
            // 开始连接请求
            connection.connect();
            connection.getResponseCode();
        } catch (Exception e) {
        	//e.printStackTrace();
        }
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:phone:手机号
	 *@description:查询手机号码的归属地
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static Map<String, Object> getPhoneNumBelongingPlace(String phone){
		String url="http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=" + phone;
		JSONObject jsonobj=CommUtil.acquireWeChatUserData(url);
		Map<String, Object> info=new HashMap<>();
		String city="未知";
		String province="未知";
		String operator="未知";
		JSONObject obj=(JSONObject) jsonobj.get("response");
		if (obj.get(phone)!=null) {
			JSONObject p=(JSONObject) ((JSONObject) obj.get(phone)).get("detail");
			city=(String) ((JSONObject) ((JSONArray)p.get("area")).get(0)).get("city");
			province=(String) p.get("province");
			operator=(String) p.get("operator");
		}
		info.put("city", city);
		info.put("province", province);
		info.put("operator", operator);
		return info;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:max：最大；  min：最小
	 *@description:随机数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static Integer randoms(Integer max,Integer min){
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**userName 用户昵称  mobile 手机号码
	 *@description:推送提醒卖家发货通知短信
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static boolean pushRemindDeliverySMS(String sellerName,String mobile,String buyerName){
		String templateId="SMS_137668388";//创建模版
		JSONObject obj=new JSONObject();
		obj.put("sellerName",sellerName);
		obj.put("buyerName",buyerName);
		boolean ret=CommUtil.sendNote((String)sellerName, obj, mobile,templateId);
		return ret;
	}
	/***
	 *@author:gaohao
	 *@return:String
	 *@param:**
	 *@description:根据部门id取下级部门id
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static String getSubclassBumenIds(Long bumen_id,ICommonService commonService){
		String ids="";
		String sql="SELECT id FROM ecm_bumen WHERE superiorBumen_id = " + bumen_id;
		List<?> bumenIds = commonService.executeNativeNamedQuery(sql);
		if (bumenIds.size()>0) {
			ids = bumenIds.toString();
			ids=ids.substring(1, ids.length()-1);
		}
		return ids;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**
	 *@description:更新会员等级
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	public static boolean updateUserRenk(Integer money,User user,ICommonService commonService,IUserService userService){
		if (user.getUserRank()==null) {
			user.setUserRank((UserRank) commonService.getById("UserRank", "1"));
			userService.update(user);
		}
		boolean update=false;
		int ava = user.getAvailableBalance().intValue();
		if (CommUtil.null2Int(money)!=0) {
			ava+=money;
		}
		String hql="select obj from UserRank as obj where obj.integralNum <= " + ava +" order by obj.integralNum DESC";
		List<UserRank> userRanks = commonService.query(hql, null, 0, 1);
		if (userRanks.size()>0) {
			UserRank userRank = userRanks.get(0);
			if (userRank.getRankNum()>user.getUserRank().getRankNum()) {
				if (user.getZhiwei()==null) {
					user.setZhiwei((ZhiWei)commonService.getById("ZhiWei", "0"));
				}
				user.setUserRank(userRank);
				update = userService.update(user);
			}
		}
		if (user.getUserRank().getIsHaveZhiweiRight()&&user.getZhiwei().getId()==0) {
			ApiUtils.asynchronousUrl("http://www.d1sc.com/automaticAppointmentByUserRank.htm?userId="+user.getId(), "GET");
		}
		return update;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**
	 *@description:检测会员是否发放对应的津贴的权限
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static boolean distributionState(User user,ICommonService commonService,IUserService userService,String payType){
		boolean is=true;
		if (user.getUserRank()==null) {
			user.setUserRank((UserRank) commonService.getById("UserRank", "1"));
			userService.update(user);
		}
		UserRank userRank = user.getUserRank();
		if (payType.equals("daogou")) {
			return userRank.getIsHaveDaogouPrice();
		}else if (payType.equals("danbao")) {
			return userRank.getIsHaveDanbaoPrice();
		}else if (payType.equals("zhaoshang")) {
			return userRank.getIsHaveZhaoshangPrice();
		}else if (payType.equals("xianji")) {
			return userRank.getIsHaveXianjiPrice();
		}else if (payType.equals("fenhong")) {
			return userRank.getIsHaveFenhongPrice();
		}else if (payType.equals("zenggu")) {
			return userRank.getIsHaveZengguPrice();
		}else if (payType.equals("chubei")) {
			return userRank.getIsHaveChubeiPrice();
		}
		return is;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**
	 *@description:检测动态会员储备金分成比例
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static ReserveScale getUserReserveScale(User user,ICommonService commonService){
		String userState = ApiUtils.getUserState(commonService, user);
		String hql="select obj from ReserveScale as obj where obj.activeRank = " + userState;
		@SuppressWarnings("unchecked")
		List<ReserveScale> rs = commonService.query(hql, null, -1, -1);
		if (rs.size()>0) {
			return rs.get(0);
		}
		return null;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**
	 *@description:按比例发放储备金
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static Map<String, Object> setChubeiMoney(User user,double addWages,double wages,String explain,IUserService userService,IPredepositLogService predepositLogService,String payType,ICommonService commonService,double profit){
		Map<String, Object> info=new HashMap<>();
		boolean update = false;
		Double scale = 1d;
		if (user!=null) {
			ReserveScale reserveScale = ApiUtils.getUserReserveScale(user, commonService);
			Integer activeRank = 5;
			String percentage = "";
			if (reserveScale!=null&&reserveScale.getScale()!=1&&user.getId()!=1) {
				scale = reserveScale.getScale();
				activeRank = reserveScale.getActiveRank();
				percentage = reserveScale.getPercentage();
				explain+=",您为"+activeRank+"级动态会员,发放"+percentage+"的储备金";
			}
//			update = ApiUtils.distributeWages(user, CommUtil.formatDouble(addWages*scale,2), CommUtil.formatDouble(wages*scale,2), explain, userService, predepositLogService, payType, commonService);
			update=AllocateWagesUtils.allocateMoneyToUser(user.getId()+"",CommUtil.formatDouble(addWages*scale,2),
											explain,"chubei",predepositLogService,userService,commonService,1);
			//商区收益
			try {
				ApiUtils.grantAreaPartnerMoney(user, profit, commonService, userService, predepositLogService);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		info.put("is", update);
		info.put("scale", scale);
		return info;
	}
	/***
	 *@author:gaohao
	 *@return:Date
	 *@param:**
	 *@description:返回当前时间的前后N年
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
    public static Date getYear(int year){
        Calendar cal = Calendar.getInstance();
        cal.add(1, year);
        return cal.getTime();
    }
    /***
	 *@author:gaohao
	 *@return:User
	 *@param:**
	 *@description:app验证用户是否可以查看数据实况
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static User erifySeeDataPowerUser(String userId,String password,IUserService userService,Integer power,ICommonService commonService){
		if ("".equals(CommUtil.null2String(password))){
			return null;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			return null;
		}
		User user = userService.getObjById(user_id);
		if (user!=null) {
			if (user.getPassword().equals(password)&&user.getZhiwei().getPositionOrder()<=power) {
				return user;
			}
			String hql = "select obj from AppSeeDataPower as obj where obj.deleteStatus = false and obj.user.id = " + user.getId();
			List<?> users = commonService.query(hql, null, -1, -1);
			if (users.size()>0) {
				return user;
			}
		}
		return null;
	}

	/***
	 * @author:gaohao
	 * @return:User
	 * @param:**
	 * @description:发放区域合伙人商区收益，毛利的1%
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static boolean grantAreaPartnerMoney(User user, double profit,
			ICommonService commonService, IUserService userService,
			IPredepositLogService predepositLogService) {
		if (user.getBumen() == null || user.getAreaGradeOfUser() == null) {
			return false;
		}
		String hql = "select obj from AreaPartnerEntity as obj where obj.buMen.id = "
				+ user.getBumen().getId()
				+ " and obj.area.id = "
				+ user.getAreaGradeOfUser().getId()
				+ " and obj.user.id <> null";
		List<AreaPartnerEntity> apes = commonService.query(hql, null, -1, -1);
		if (apes.size() <= 0) {
			return false;
		}
		AreaPartnerEntity areaPartnerEntity = apes.get(0);
		boolean is = AllocateWagesUtils.allocateMoneyToUser(areaPartnerEntity
				.getUser().getId().toString(), profit, areaPartnerEntity
				.getBuMen().getName()
				+ areaPartnerEntity.getArea().getName()
				+ "商区收益", "", predepositLogService, userService, commonService,
				1);
		return is;
	}
	/***
	 *@author:gaohao
	 *@return:boolean
	 *@param:**userName 用户昵称  mobile 手机号码
	 *@description:推送职衔通知短信
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static boolean pushRemindTitleSMS(String userName,String mobile){
		String type = judgmentType(mobile);
		if (!type.equals("mobile")) {
			return false;
		}
		String templateId="SMS_138062775";
		JSONObject obj=new JSONObject();
		obj.put("userName",userName);
		boolean ret=CommUtil.sendNote((String)userName, obj, mobile,templateId);
		return ret;
	}
}