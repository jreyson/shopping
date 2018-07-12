package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralDeposit.IntegralDepositListEntity;
import com.shopping.api.domain.userFunction.RedPacket;
import com.shopping.api.domain.userFunction.RedPacketRecorder;
import com.shopping.api.domain.userFunction.RedPacketTheme;
import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.service.IRedPacketService;
import com.shopping.api.service.IWeChatAccountInfoService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositCashService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;

/***
 *@author:gaohao	
 *@description:app发送红包
 ***/
@Controller
public class RedPacketAction {
	@Autowired
	private IPredepositCashService predepositcashService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IRedPacketService redPacketService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IWeChatAccountInfoService weChatAccountInfoService;
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app获取红包主题
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetRedPacketThemes.htm", method = RequestMethod.POST)
	public void appGetRedPacketThemes(HttpServletRequest request,HttpServletResponse response){
		String redPacketThemeHql="select obj from RedPacketTheme as obj where obj.isUse = true order by obj.addTime DESC";
		List<RedPacketTheme> redPacketThemes = commonService.query(redPacketThemeHql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(RedPacketTheme.class, "id,themeName,showPhoto"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, redPacketThemes, "获取红包主题列表成功", 0,filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:singleMoney 单个红包金额,redPacketNum 红包个数,redPacketTheme 主题id,fontContent 红包文字,moneySum 金额总数
	 *@description:app发送红包,微信支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appWeixinPaymentRedPacket.htm", method = RequestMethod.POST)
	public void appWeixinPayRedPacket(HttpServletRequest request,HttpServletResponse response,String userId,
			String redPacketId){
		User user = this.userService.getObjById(CommUtil.null2Long(userId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		RedPacket redPacket = this.redPacketService.getObjById(CommUtil.null2Long(redPacketId));
		if (redPacket==null) {
			ApiUtils.json(response, "", "红包不存在", 1);
			return;
		}
		if (redPacket.getProvideUser().getId()!=user.getId()) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		Map<String, String> params = null;
		String alipayUrl=CommUtil.getURL(request)+"/app_weiXinRedPacketCallBack.htm";
		try {
			params=ApiUtils.get_weixin_sign_string(redPacket.getRunningWaterNum().toString(), alipayUrl,redPacket.getMoneySum()+"");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ApiUtils.json(response, params,"获取支付信息成功",0);		
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app发送红包,支付宝支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appAliPaymentRedPacket.htm", method = RequestMethod.POST)
	public void appAliPaymentRedPacket(HttpServletRequest request,HttpServletResponse response,String userId,
			String redPacketId){
		User user = this.userService.getObjById(CommUtil.null2Long(userId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		RedPacket redPacket = this.redPacketService.getObjById(CommUtil.null2Long(redPacketId));
		if (redPacket==null) {
			ApiUtils.json(response, "", "红包不存在", 1);
			return;
		}
		if (redPacket.getProvideUser().getId()!=user.getId()) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String alipayUrl=CommUtil.getURL(request)+"/app_alipayRedPacketCallBack.htm";
		String str=ApiUtils.getAlipayStr(redPacket.getRunningWaterNum().toString(), alipayUrl,redPacket.getMoneySum()+"");
		ApiUtils.json(response, str,"获取支付信息成功",0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app发送红包,积分支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBalancementRedPacket.htm", method = RequestMethod.POST)
	public void appBalancementRedPacket(HttpServletRequest request,HttpServletResponse response,String userId,
			String redPacketId,String password){
		User user = this.userService.getObjById(CommUtil.null2Long(userId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(CommUtil.null2String(password)).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		RedPacket redPacket = this.redPacketService.getObjById(CommUtil.null2Long(redPacketId));
		if (redPacket==null) {
			ApiUtils.json(response, "", "红包不存在", 1);
			return;
		}
		if (redPacket.getProvideUser().getId()!=user.getId()||redPacket.getOrderStatus()!=10) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String msg="";
		double amount=CommUtil.null2Int(redPacket.getRedPacketNum());
		double money=CommUtil.null2Double(redPacket.getSingleMoney());
		BigDecimal a1 = new BigDecimal(Double.toString((amount)));  
        BigDecimal b1 = new BigDecimal(Double.toString(money));  
        double sum = a1.multiply(b1).doubleValue();
		double userBalance=user.getAvailableBalance().doubleValue();
		if(user.getFreezeBlance().intValue()==1){
			if(userBalance-sum-1000<0){
				msg="余额不足，您有部分余额处于锁定状态";
				ApiUtils.json(response, "", msg , 1);
				return;
			}
		}else{
			if(userBalance-sum<0){
				msg="您的余额不足,谢谢惠顾";
				ApiUtils.json(response, "", msg , 1);
				return;
			}
		}	
		redPacket.setOrderStatus(20);
		redPacket.setPayTime(new Date());
		redPacket.setRechargeWay("jifen");
		boolean save = redPacketService.update(redPacket);
		if (save) {
			boolean is = ApiUtils.distributeWages(redPacket.getProvideUser(), -sum, -sum, "分享红包", userService, predepositLogService, "",commonService);
			if (is) {
				ApiUtils.json(response, redPacket.getWebUrl(),"支付成功",0);
				return;
			}			
		}
		redPacket.setOrderStatus(10);
		redPacketService.save(redPacket);
		ApiUtils.json(response, "","积分购买失败",1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app获取已经支付成功的红包
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetRedPacketNotes.htm", method = RequestMethod.POST)
	public void appGetRedPacketNotes(HttpServletRequest request,HttpServletResponse response,String userId,String password,String currentPage,String beginTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String date = sdf.format(new Date(System.currentTimeMillis()));	
		if (!"".equals(CommUtil.null2String(beginTime))) {
			try {
				Date parse = sdf.parse(beginTime);
				date = sdf.format(parse);	
			} catch (ParseException e) {
				e.printStackTrace();
				ApiUtils.json(response, "", "时间参数错误", 1);
				return;
			}	
		}
		StringBuffer begin = new StringBuffer().append(date).append("-1-1 00:00:00");
		StringBuffer end = new StringBuffer().append(date).append("-12-31 23:59:59");
		User user = ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "","密码错误",1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from RedPacket as obj where obj.orderStatus=20 and obj.provideUser.id = "+ user.getId() +" and obj.addTime >='" + begin.toString() + "' and obj.addTime <='" + end.toString() + "' order by obj.addTime DESC";
		List<RedPacket> redPackets = redPacketService.query(hql, null, current_page*pageSize, pageSize);
		int redPacketTotal=redPackets.size();
		double redPacketMoneySum=0;
		hql="select sum(obj.moneySum) from RedPacket as obj where obj.orderStatus=20 and obj.provideUser.id = "+ user.getId() +" and obj.addTime >='" + begin.toString() + "' and obj.addTime <='" + end.toString() + "'";
		List<?> money = commonService.query(hql, null, -1, -1);
		if (money.size()>0) {
			redPacketMoneySum=CommUtil.null2Double(money.get(0));
		}
		AppTransferData redPacketRecords=new AppTransferData();
		redPacketRecords.setFifthData(redPackets);
		redPacketRecords.setFirstData(redPacketTotal);
		redPacketRecords.setFourthData(CommUtil.formatDouble(redPacketMoneySum, 2));
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(RedPacket.class, "id,addTime,singleMoney,redPacketNum,surplusNum,webUrl,provideUser,fontContent,overdueState,moneySum,surplusMoney"));
		objs.add(new FilterObj(User.class, "id,userName"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,firstData,fourthData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, redPacketRecords, "获取红包列表成功", 0,filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:singleMoney 单个红包金额,redPacketNum 红包个数,redPacketTheme 主题id,fontContent 红包文字,moneySum 金额总数
	 *@description:app生成分享红包
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appProducedShareRedPacket.htm", method = RequestMethod.POST)
	public void appProducedShareRedPacket(HttpServletRequest request,HttpServletResponse response,String userId,
			String singleMoney,String redPacketNum,String redPacketThemeId,String fontContent,String moneySum){
		if (ApiUtils.is_null(userId,singleMoney,redPacketNum,redPacketThemeId,moneySum)) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		double amount=CommUtil.null2Int(redPacketNum);
		double money=CommUtil.null2Double(singleMoney);
		if (money<0.18) {
			ApiUtils.json(response, "", "红包金额不能小于0.18", 1);
			return;
		}
		money=CommUtil.formatDouble(money, 2);
		BigDecimal a1 = new BigDecimal(Double.toString((amount)));  
        BigDecimal b1 = new BigDecimal(Double.toString(money));  
        double sum = a1.multiply(b1).doubleValue();
		if (sum!=CommUtil.null2Double(moneySum)) {
			ApiUtils.json(response, "", "计算金额有误", 1);
			return;
		}
		RedPacket redPacket=new RedPacket();
		redPacket.setAddTime(new Date());
		redPacket.setDeleteStatus(false);
		if (!"".equals(CommUtil.null2String(fontContent))) {
			if (fontContent.length()>10) {
				fontContent=fontContent.substring(0, 10);
			}
			redPacket.setFontContent(fontContent);
		}
		redPacket.setOrderStatus(10);
		redPacket.setOverdueState(false);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 3);
		Date today = calendar.getTime();
		redPacket.setOverdueTime(today);
		redPacket.setProvideUser(user);
		redPacket.setRedPacketNum(CommUtil.null2Int(redPacketNum));
		Long redPacketTheme_id = CommUtil.null2Long(redPacketThemeId);
		RedPacketTheme redPacketTheme = (RedPacketTheme) commonService.getById("RedPacketTheme", redPacketTheme_id.toString());
		if (redPacketTheme==null) {
			ApiUtils.json(response, "", "红包主题不存在", 1);
			return;
		}
		redPacket.setRedPacketTheme(redPacketTheme);
		redPacket.setSign(user.getId().toString()+ApiUtils.getRandomString(3)+System.currentTimeMillis());
		redPacket.setSingleMoney(CommUtil.null2Double(singleMoney));
		redPacket.setSurplusNum(CommUtil.null2Int(redPacketNum));
		redPacket.setWebUrl(CommUtil.getURL(request) + "/redPacket_" + redPacket.getSign() + ".htm");
		redPacket.setMoneySum(sum);
		redPacket.setRunningWaterNum(ApiUtils.integralOrderNum(user.getId()));
		redPacket.setSurplusMoney(sum);
		boolean save = redPacketService.save(redPacket);
		if (save) {
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(RedPacket.class, "id,webUrl"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, redPacket,"生成分享红包成功",0,filter);
		}else {
			ApiUtils.json(response, "","生成分享红包失败",1);
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取红包领取详情
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetRedPacketReceiveInfo.htm", method = RequestMethod.POST)
	public void appGetRedPacketReceiveInfo(HttpServletRequest request,HttpServletResponse response,String userId,String redPacketId,String currentPage){
		boolean is_null = ApiUtils.is_null(userId,redPacketId);
		if (is_null) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		RedPacket redPacket = this.redPacketService.getObjById(CommUtil.null2Long(redPacketId));
		if (redPacket==null) {
			ApiUtils.json(response, "", "红包不存在", 1);
			return;
		}
		if (!redPacket.getProvideUser().getId().equals(CommUtil.null2Long(userId))) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from RedPacketRecorder as obj where obj.redPacket.id = " + redPacket.getId() + " order by obj.addTime DESC";
		List<RedPacketRecorder> redPacketRecorders = commonService.query(hql, null, current_page*pageSize, pageSize);
		AppTransferData redPacketRecords=new AppTransferData();
		redPacketRecords.setFifthData(redPacketRecorders);
		RedPacket redPacket2 = redPacketService.getObjById(CommUtil.null2Long(redPacketId));		
		if (redPacket2==null) {
			ApiUtils.json(response, "","红包不存在",1);
			return;
		}
		redPacketRecords.setFirstData(redPacket2);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(RedPacketRecorder.class, "addTime,receiveUser"));
		objs.add(new FilterObj(RedPacket.class, "id,singleMoney,redPacketNum,surplusNum,fontContent,overdueState,moneySum,provideUser,surplusMoney,surplusMoney"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,firstData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, redPacketRecords,"获取领取列表成功",0,filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:分享出去的红包H5界面,领取红包的入口界面
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/redPacketH5Entrance.htm")
	public ModelAndView redPacketH5Entrance(HttpServletRequest request,HttpServletResponse response,
			String redPacketSign){
		redPacketSign=CommUtil.null2String(redPacketSign);
//		String withdrawDepositTotalPersonNumSql="";
		String personTotalProfitSql="";
		String redPacketRecorderHql="";
		String withdrawDepositRecorderHql="";
		if(!"".equals(redPacketSign)){//分享到微信之后，点开的时候，手机端会在后面多附加字符串，需要截取，才可以使用
			int a=redPacketSign.indexOf(".");//http://www.d1sc.com/redPacket_149900WvX1521692366860.htm?from=singlemessage&isappinstalled=0
			if(a!=-1){//表示这段字符串中有?
				redPacketSign=redPacketSign.substring(0, a);
			}
		}
		ModelAndView mv=null;
		Map<String,Object> redPacketMap=this.judgeRedPackgetIsEffective(redPacketSign, request, response);
		RedPacket redPacket=(RedPacket)redPacketMap.get("redPacket");
		if(redPacket!=null){
			mv = new JModelAndView("redPacket/redPacketEntrance_newPage.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			User provideUser=redPacket.getProvideUser();
//			withdrawDepositTotalPersonNumSql = "SELECT "+
//													  "COUNT(res.cash_user_id) "+
//													"FROM (SELECT DISTINCT "+
//													        "spc.cash_user_id "+
//													      "FROM shopping_predeposit_cash AS spc "+
//													      "WHERE spc.cash_status = 1 "+
//													          "AND spc.cash_pay_status = 2) AS res";
			personTotalProfitSql = "SELECT "+
										  "ROUND(SUM(so.daogou_get_price),2) AS totalProfit "+
										"FROM shopping_user AS su "+
										  "JOIN shopping_orderform AS so "+
										    "ON so.user_id = su.id "+
										"WHERE so.order_status IN(20,30,40,50,60) "+
										    "AND su.id = "+provideUser.getId().toString();
			redPacketRecorderHql = "select obj from RedPacketRecorder as obj  order by obj.addTime desc";
			withdrawDepositRecorderHql="select obj from PredepositCash as obj where obj.cash_pay_status = 2 and obj.cash_status = 1 order by obj.addTime desc";
			//查询总共提现的人数有多少人
//			List<?> withdrawDepositTotalPersonNumList=this.commonService.executeNativeNamedQuery(withdrawDepositTotalPersonNumSql);
			//查询出个人盈利的总导购金是多少钱
			List<?> personTotalProfitList=this.commonService.executeNativeNamedQuery(personTotalProfitSql);
			//查询出领取过的红包记录
			List<?> redPacketRecorderList=this.commonService.query(redPacketRecorderHql, null, 0, 50);
			List<?> withdrawDepositRecorderList=this.predepositcashService.query(withdrawDepositRecorderHql, null, 0, 20);
			mv.addObject("redPacketRecorderList", redPacketRecorderList);
			mv.addObject("personTotalProfit", CommUtil.null2Double(personTotalProfitList.get(0)));
//			mv.addObject("withdrawDepositTotalPersonNum", CommUtil.null2String(withdrawDepositTotalPersonNumList.get(0)));
			mv.addObject("redPacketSign", redPacketSign);
			mv.addObject("redPacket", redPacket);
			mv.addObject("withdrawDepositRecorderList", withdrawDepositRecorderList);
			//System.out.println("红包入口界面redPacketH5Entrance.htm=====>"+redPacketSign);
		}else{
			mv=(ModelAndView)redPacketMap.get("mv");
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取提现记录列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value ="/withdrawDepositRecorderDetail.htm")
	public ModelAndView withdrawDepositRecorderDetail(HttpServletRequest request,
			HttpServletResponse response,String redPacketSign){
		ModelAndView mv=null;
		String withdrawDepositRecorderHql="select obj from PredepositCash as obj where obj.cash_pay_status = 2 and obj.cash_status = 1 order by obj.addTime desc";
		List<?> withdrawDepositRecorderList=this.predepositcashService.query(withdrawDepositRecorderHql, null, 0, 20);
		if(withdrawDepositRecorderList.size()>0){
			mv = new JModelAndView("redPacket/withdrawDepositRecorderDetail.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			mv.addObject("withdrawDepositRecorderList", withdrawDepositRecorderList);
		}else{
			mv = new JModelAndView("h5Register/wapRegisterError.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			mv.addObject("url", CommUtil.getURL(request)+"/redPacketH5Entrance.htm?redPacketSign="+redPacketSign);
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取提现总人数的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/requestWithdrawDepositTotalNum.htm")
	public void requestWithdrawDepositTotalNum(HttpServletRequest request,
			HttpServletResponse response){
		String withdrawDepositTotalPersonNumSql="SELECT "+
												  "COUNT(res.cash_user_id) "+
												"FROM (SELECT DISTINCT "+
												        "spc.cash_user_id "+
												      "FROM shopping_predeposit_cash AS spc "+
												      "WHERE spc.cash_status = 1 "+
												          "AND spc.cash_pay_status = 2) AS res";
		List<?> withdrawDepositTotalPersonNumList=this.commonService.executeNativeNamedQuery(withdrawDepositTotalPersonNumSql);
		if(withdrawDepositTotalPersonNumList.size()>0){
			ApiUtils.json(response,withdrawDepositTotalPersonNumList.get(0), "获取提现总人数成功", 0);
		}else{
			ApiUtils.json(response, "", "获取提现总人数失败", 1);
		}
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:红包领取的h5界面
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/redPacketH5Get.htm")
	public ModelAndView redPacketH5Get(HttpServletRequest request,HttpServletResponse response,
			String redPacketSign){
		ModelAndView mv=null;
		String redPacketRecorderHql="";
		String personTotalProfitSql="";
		String withdrawDepositRecorderHql="";
		String requestWebUrl="";
		Integer flag=0;
		Map<String,Object> redPacketMap=this.judgeRedPackgetIsEffective(redPacketSign, request, response);
		RedPacket redPacket=(RedPacket)redPacketMap.get("redPacket");
		if(redPacket!=null){
			mv = new JModelAndView("redPacket/redPacketGet_newPage.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			redPacketRecorderHql="select obj from RedPacketRecorder as obj  order by obj.addTime desc";
			personTotalProfitSql="SELECT "+
					  "ROUND(SUM(so.daogou_get_price),2) AS totalProfit "+
					"FROM shopping_user AS su "+
					  "JOIN shopping_orderform AS so "+
					    "ON so.user_id = su.id "+
					"WHERE so.order_status IN(20,30,40,50,60) "+
					    "AND su.id = "+redPacket.getProvideUser().getId().toString();
			withdrawDepositRecorderHql="select obj from PredepositCash as obj where obj.cash_pay_status = 2 and obj.cash_status = 1 order by obj.addTime desc";
			List<?> redPacketRecorderList=this.commonService.query(redPacketRecorderHql, null, 0, 50);
			List<?> personTotalProfitList=this.commonService.executeNativeNamedQuery(personTotalProfitSql);
			List<?> withdrawDepositRecorderList=this.predepositcashService.query(withdrawDepositRecorderHql, null, 0, 20);
			if(request.getHeader("user-agent").toLowerCase().indexOf("micromessenger")>-1){//判断是否是微信客户端
				requestWebUrl=CommUtil.getURL(request)+"/weChatTransitShipment.htm";
				flag=1;
				mv.addObject("weChatAccountInfoId", "2");//防止将微信公众号appId返回在前端页面
			}else{
				requestWebUrl=CommUtil.getURL(request)+"/register.htm";
			}
			mv.addObject("flag", flag);
			mv.addObject("requestWebUrl", requestWebUrl);
			mv.addObject("redPacket", redPacket);
			mv.addObject("redPacketRegisteWay", "redPacketRegisteWay");
			mv.addObject("redPacketSign", redPacketSign);
			mv.addObject("redPacketRecorderList", redPacketRecorderList);
			mv.addObject("personTotalProfit", CommUtil.null2String(personTotalProfitList.get(0)));
			mv.addObject("withdrawDepositRecorderList", withdrawDepositRecorderList);
		}else{
			mv=(ModelAndView)redPacketMap.get("mv");
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:微信中转重定向到code接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/weChatTransitShipment.htm")
	public String weChatTransitShipment(HttpServletRequest request,HttpServletResponse response,
			String weChatAccountInfoId,String redPacketSign,Integer flag){
		Long weChatAccountInfoIDLong=CommUtil.null2Long(weChatAccountInfoId);
		Map<String,String> params=new LinkedHashMap<String, String>();
		String weiXinStr="redirect:/index.htm";
		if(weChatAccountInfoIDLong!=-1){
			WeChatAccountInfoEntity weChatAccountInfo=this.weChatAccountInfoService.getObjById(weChatAccountInfoIDLong);
			if(weChatAccountInfo!=null){
				try {
					weiXinStr="https://open.weixin.qq.com/connect/oauth2/authorize?";
					params.put("appid", weChatAccountInfo.getApplyId());
					params.put("redirect_uri", URLEncoder.encode("http://www.d1sc.com/weChatPublicAccountsUserLogin.htm?redPacketSign="+redPacketSign+"&flag="+flag+"&weChatAccountInfoId="+weChatAccountInfoIDLong,"utf-8").replace("+", "%20").toString());
					params.put("response_type", "code");
					params.put("scope", "snsapi_userinfo");
					params.put("state", "STATE#wechat_redirect");
					weiXinStr="redirect:"+ApiUtils.conactRequestUrl(params, weiXinStr, 0);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					weiXinStr="redirect:/index.htm";
				}finally{
					params=null;
				}
			}
		}
		return weiXinStr;
	} 
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:纠正红包url加.htm的方法
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
//	@RequestMapping(value = "/correctRedPacketUrl.htm")
//	public void correctRedPacketUrl(HttpServletRequest request,
//			HttpServletResponse response){
//		String redPacketRecorderHql = "select obj from RedPacket as obj  order by obj.addTime desc";
//		String url="";
//		List<RedPacket> redPacketRecorderList=this.redPacketService.query(redPacketRecorderHql, null, -1, -1);
//		for(RedPacket obj : redPacketRecorderList){
//			url=obj.getWebUrl();
//			obj.setWebUrl(url+".htm");
//			this.redPacketService.update(obj);
//		}
//		ApiUtils.json(response, "", "修改url加.htm成功", 0);
//	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:判断红包是否过期的方法
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	private Map<String,Object> judgeRedPackgetIsEffective(String redPacketSign,
			HttpServletRequest request,HttpServletResponse response){
		Map<String,Object> returnStatusSign=new HashMap<String,Object>();
		ModelAndView mv=null;
		RedPacket redPacket=null;
		String hql="select obj from RedPacket as obj where obj.sign='"+redPacketSign+"' and obj.overdueState=false and obj.orderStatus=20";
		List<RedPacket> redPacketRecordList=this.redPacketService.query(hql, null, -1, -1);
		if(redPacketRecordList.size()>0){
			redPacket=redPacketRecordList.get(0);
			if(redPacket!=null){
				if(redPacket.getOverdueTime().before(new Date())){//红包过期==>过期界面
					mv = new JModelAndView("redPacket/beOverdue.html",
							this.configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 5, request, response);
					mv.addObject("op_title", "此红包失效了！");
					mv.addObject("promptWord", "温馨提示：红包的有效时间为72小时");
					this.refundToUser(redPacket);//进行对过期红包的退款处理,并且修改数据状态
				}else{
					if(redPacket.getSurplusNum()==0){//红包未失效,但是领取完毕了==>领取完毕界面
						mv = new JModelAndView("redPacket/getFinished.html",
								this.configService.getSysConfig(),
								this.userConfigService.getUserConfig(), 5, request, response);
						mv.addObject("op_title", "来晚了，领完啦！");
						this.refundToUser(redPacket);//修改红包的数据状态
					}else{
						returnStatusSign.put("redPacket", redPacket);
					}
				}
			}
		}else{//红包不存在==>过期界面
			mv = new JModelAndView("redPacket/beOverdue.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			mv.addObject("op_title", "此红包失效了！");
			mv.addObject("promptWord", "温馨提示：红包的有效时间为72小时");
		}
		returnStatusSign.put("mv", mv);
		return returnStatusSign;
	}
	private void refundToUser(RedPacket redPacket){
		if(redPacket.getSurplusNum()==0){
			redPacket.setOverdueState(true);
			this.redPacketService.update(redPacket);
		}else{
			redPacket.setOverdueState(true);
			boolean updateRet=false;
			try {
				updateRet=this.redPacketService.update(redPacket);
			} catch (Exception e) {
				// TODO: handle exception
				redPacket.setOverdueState(false);
				this.redPacketService.update(redPacket);
			}
			if(updateRet){
				User user=redPacket.getProvideUser();
				if(user!=null){
					Double reimbursementMoney=CommUtil.multiplyDouble(redPacket.getSingleMoney(),Double.valueOf(redPacket.getSurplusNum()+""), 2);
					Double userAvailableBalance=user.getAvailableBalance().doubleValue()+reimbursementMoney;
					user.setAvailableBalance(BigDecimal.valueOf(userAvailableBalance));
					boolean ret=this.userService.update(user);
					if(ret){
						PredepositLog positLog = new PredepositLog();
						positLog.setAddTime(new Date());
						positLog.setPd_log_user(user);
						positLog.setPd_op_type("增加");
						positLog.setPd_log_amount(BigDecimal.valueOf(reimbursementMoney));
						positLog.setPd_log_info("分享红包退款");
						positLog.setPd_type("可用预存款");
						positLog.setCurrent_price(userAvailableBalance);
						this.predepositLogService.save(positLog);
						ApiUtils.updateUserRenk(0,user, commonService, userService);//更新会员等级
					}
				}
			}
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:定时检验红包过期状态
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/inspectRedPackgetStatus.htm")
	public void inspectRedPackgetStatus(HttpServletRequest request,HttpServletResponse response){
		System.out.println("红包检测开始");
		Date newDate = new Date();
		int current_page=0;
		int pageSize=50;
		String count="select count(obj) from RedPacket as obj where obj.orderStatus = 20 and obj.overdueState = false and obj.overdueTime <=:overdueTime";
		Map<String, Date> paramMap=new HashMap<String, Date>();
		paramMap.put("overdueTime", newDate);
		List<?> counts = commonService.query(count, paramMap, -1, -1);
		int cycleNum=0;
		if (counts.size()>0) {
			int sum = CommUtil.null2Int(counts.get(0));
			cycleNum=sum%pageSize==0?sum/pageSize:sum/pageSize+1;
		}
		for (int i = 0; i < cycleNum; i++) {
			String hql="select obj from RedPacket as obj where obj.orderStatus = 20 and obj.overdueState = false and obj.overdueTime <= :overdueTime";		
			List<RedPacket> redPackets = redPacketService.query(hql, paramMap, pageSize*current_page, pageSize);
			for (RedPacket redPacket : redPackets) {
				if (redPacket!=null&&redPacket.isOverdueState()==false) {
					redPacket.setOverdueState(true);
					boolean is = redPacketService.update(redPacket);
					if (is&&redPacket.getOrderStatus()==20) {
						double amount=CommUtil.null2Int(redPacket.getSurplusNum());
						double money=CommUtil.null2Double(redPacket.getSingleMoney());
						BigDecimal a1 = new BigDecimal(Double.toString((amount)));  
				        BigDecimal b1 = new BigDecimal(Double.toString(money));  
				        double surplusSum = a1.multiply(b1).doubleValue();
				        surplusSum=CommUtil.formatDouble(surplusSum, 2);
						User user = redPacket.getProvideUser();
						boolean update = ApiUtils.distributeWages(user, surplusSum, surplusSum, redPacket.getRunningWaterNum()+"分享红包退款", userService, predepositLogService, "",commonService);
						if (!update) {
							redPacket.setOverdueState(false);
							redPacketService.update(redPacket);
						}						
					}
				}
			}
		}
	}
}
