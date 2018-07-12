package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.shopping.api.domain.countBuy.CountPriceDomain;
import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralDeposit.IntegralDepositListEntity;
import com.shopping.api.output.AppBillsDataTemp;
import com.shopping.api.service.IIntegralDepositListService;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.api.service.impl.IntegralDepositListServiceImpl;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
import com.sun.org.apache.bcel.internal.generic.NEW;

@Controller
public class IntegralApiAction {
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IIntegralDepositListService integralDepositListService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IIntegralDepositService integralDepositService;
	@Autowired
	private IPredepositLogService predepositLogService;
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:积分理财，获取积分理财描述，图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appIntegralDepositInfo.htm", method = RequestMethod.POST)
	public void appIntegralDepositInfo(HttpServletRequest request,
			HttpServletResponse response){
		String hql="select obj from IntegralDepositInfo as obj order by obj.depositInfoorder";
		List<?> list = commonService.query(hql, null, -1, -1);
		if (list.size()>0) {
			ApiUtils.json(response, list, "获取理财描述列表成功", 0);
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:积分理财，获取积分理财利率信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appIntegralDepositList.htm", method = RequestMethod.POST)
	public void appIntegralsList(HttpServletRequest request,
			HttpServletResponse response,Long depositListId){
		if (depositListId==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String hql="select obj from IntegralDepositListEntity as obj where obj.integralDepositInfo.id="+depositListId+" order by obj.days";
		List<IntegralDepositListEntity> list = integralDepositListService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(IntegralDepositListEntity.class, "id,deleteStatus,annualRate,dailyRate,days,title,purchaseThreshold,riskInfo,annualInfo"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		if (list.size()>0) {
			ApiUtils.json(response, list, "获取理财列表成功", 0,filter);
		}
	}
	/***
	 *@author:gaohao
	 * @param integralEntity 
	 *@return:void
	 *@param:**
	 *@description:积分理财，支付宝购买
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appAliPaymentIntegralDeposit.htm", method = RequestMethod.POST)
	public void appAliPaymentIntegralDeposit(HttpServletRequest request,
			HttpServletResponse response,Long userId,Integer count,Double depositAll,Long DepositId){
		System.out.println(userId+"--"+count+"=="+depositAll+"==="+DepositId);
		if (userId==null||count==null||depositAll==null||DepositId==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = userService.getObjById(userId);
		IntegralDepositListEntity integralDepositListEntity = integralDepositListService.getObjById(DepositId);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		if (integralDepositListEntity==null) {
			ApiUtils.json(response, "", "积分投资项目不存在", 1);
			return;
		}
		if (count<100) {
			ApiUtils.json(response, "", "积分投资不能小于100", 1);
			return;
		}
		Double dailyRate = integralDepositListEntity.getDailyRate();
		Integer days = integralDepositListEntity.getDays();
		Double depositInterest=dailyRate*count*days;
		depositInterest=CommUtil.formatDouble(depositInterest, 2);
		Double depositAlls=depositInterest+count;
		if (!depositAlls.equals(depositAll)) {
			ApiUtils.json(response, "", "利息计算有误", 1);
			return;
		}
		String end = ApiUtils.getFirstday_Lastday(new Date(), 1, days);
		String endTime = ApiUtils.weeHours(end, 1);
		IntegralDepositEntity integralDepositEntity=new IntegralDepositEntity();
		integralDepositEntity.setAddTime(new Date());
		integralDepositEntity.setDeleteStatus(false);
		integralDepositEntity.setDepositAll(depositAlls);
		integralDepositEntity.setDepositInterest(depositInterest);
		integralDepositEntity.setDepositOrderNum(ApiUtils.integralOrderNum(userId));
		integralDepositEntity.setDepositQuantity(count);
		integralDepositEntity.setDepositStatus(0);
		integralDepositEntity.setEndTime(CommUtil.formatDate(endTime, "yyyy-MM-dd HH:mm:ss"));
		integralDepositEntity.setIntegralDepositListEntity(integralDepositListEntity);
		integralDepositEntity.setOrderStatus(10);
		integralDepositEntity.setRechargeWay("alipay");
		integralDepositEntity.setUser(user);
		boolean is = integralDepositService.save(integralDepositEntity);
		if (is) {
			String alipayUrl=CommUtil.getURL(request)+"/app_alipayIntegralDepositCallBack.htm";
			String str=ApiUtils.getAlipayStr(integralDepositEntity.getDepositOrderNum().toString(), alipayUrl, count.toString());
			ApiUtils.json(response, str,"获取支付信息成功",0);
		}
	}
	/***
	 *@author:gaohao
	 * @param integralEntity 
	 *@return:void
	 *@param:**
	 *@description:积分理财，微信购买
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appWeixinPaymentIntegralDeposit.htm", method = RequestMethod.POST)
	public void appWeixinPaymentIntegralDeposit(HttpServletRequest request,
			HttpServletResponse response,Long userId,Integer count,Double depositAll,Long DepositId){
		System.out.println(userId+"--"+count+"=="+depositAll+"==="+DepositId);
		if (userId==null||count==null||depositAll==null||DepositId==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = userService.getObjById(userId);
		IntegralDepositListEntity integralDepositListEntity = integralDepositListService.getObjById(DepositId);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		if (integralDepositListEntity==null) {
			ApiUtils.json(response, "", "积分投资项目不存在", 1);
			return;
		}
		if (count<100) {
			ApiUtils.json(response, "", "积分投资不能小于100", 1);
			return;
		}
		Double dailyRate = integralDepositListEntity.getDailyRate();
		Integer days = integralDepositListEntity.getDays();
		Double depositInterest=dailyRate*count*days;
		depositInterest=CommUtil.formatDouble(depositInterest, 2);
		Double depositAlls=depositInterest+count;
		if (!depositAlls.equals(depositAll)) {
			ApiUtils.json(response, "", "利息计算有误", 1);
			return;
		}
		String end = ApiUtils.getFirstday_Lastday(new Date(), 1, days);
		String endTime = ApiUtils.weeHours(end, 1);
		IntegralDepositEntity integralDepositEntity=new IntegralDepositEntity();
		integralDepositEntity.setAddTime(new Date());
		integralDepositEntity.setDeleteStatus(false);
		integralDepositEntity.setDepositAll(depositAlls);
		integralDepositEntity.setDepositInterest(depositInterest);
		integralDepositEntity.setDepositOrderNum(ApiUtils.integralOrderNum(userId));
		integralDepositEntity.setDepositQuantity(count);
		integralDepositEntity.setDepositStatus(0);
		integralDepositEntity.setEndTime(CommUtil.formatDate(endTime, "yyyy-MM-dd HH:mm:ss"));
		integralDepositEntity.setIntegralDepositListEntity(integralDepositListEntity);
		integralDepositEntity.setOrderStatus(10);
		integralDepositEntity.setRechargeWay("weixin");
		integralDepositEntity.setUser(user);
		boolean is = integralDepositService.save(integralDepositEntity);
		if (is) {
			String weixinpayUrl=CommUtil.getURL(request)+"/app_weiXinIntegralDepositCallBack.htm";
			Map<String, String> params = null;
			try {
				params=ApiUtils.get_weixin_sign_string(integralDepositEntity.getDepositOrderNum().toString(),weixinpayUrl,count.toString());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ApiUtils.json(response, params,"获取支付信息成功",0);
		}
	}
	/***
	 *@author:gaohao
	 * @param integralEntity 
	 *@return:void
	 *@param:**
	 *@description:积分理财，积分购买
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBalancePaymentIntegralDeposit.htm", method = RequestMethod.POST)
	public void appBalancePaymentIntegralDeposit(HttpServletRequest request,
			HttpServletResponse response,Long userId,Integer count,Double depositAll,Long DepositId){
		if (userId==null||count==null||depositAll==null||DepositId==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = userService.getObjById(userId);
		IntegralDepositListEntity integralDepositListEntity = integralDepositListService.getObjById(DepositId);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		if (integralDepositListEntity==null) {
			ApiUtils.json(response, "", "积分投资项目不存在", 1);
			return;
		}
		if (count<100) {
			ApiUtils.json(response, "", "积分投资不能小于100", 1);
			return;
		}
		String msg="";
		double userBalance=user.getAvailableBalance().doubleValue();
		if(user.getFreezeBlance().intValue()==1){
			if(userBalance-count-1000<0){
				msg="您的余额有一千块诚信保证金被锁定,要完成此次支付,余额大于1000";
				ApiUtils.json(response, "", msg , 1);
				return;
			}
		}else{
			if(userBalance-count<0){
				msg="您的余额不足,谢谢惠顾";
				ApiUtils.json(response, "", msg , 1);
				return;
			}
		}
		Double dailyRate = integralDepositListEntity.getDailyRate();
		Integer days = integralDepositListEntity.getDays();
		Double depositInterest=dailyRate*count*days;
		depositInterest=CommUtil.formatDouble(depositInterest, 2);
		Double depositAlls=depositInterest+count;
		if (!depositAlls.equals(depositAll)) {
			ApiUtils.json(response, "", "利息计算有误", 1);
			return;
		}
		String end = ApiUtils.getFirstday_Lastday(new Date(), 1, days);
		String endTime = ApiUtils.weeHours(end, 1);
		IntegralDepositEntity integralDepositEntity=new IntegralDepositEntity();
		integralDepositEntity.setAddTime(new Date());
		integralDepositEntity.setDeleteStatus(false);
		integralDepositEntity.setDepositAll(depositAlls);
		integralDepositEntity.setDepositInterest(depositInterest);
		integralDepositEntity.setDepositOrderNum(ApiUtils.integralOrderNum(userId));
		integralDepositEntity.setDepositQuantity(count);
		integralDepositEntity.setDepositStatus(0);
		integralDepositEntity.setEndTime(CommUtil.formatDate(endTime, "yyyy-MM-dd HH:mm:ss"));
		integralDepositEntity.setIntegralDepositListEntity(integralDepositListEntity);
		integralDepositEntity.setOrderStatus(20);
		integralDepositEntity.setRechargeWay("jifen");
		integralDepositEntity.setUser(user);
		boolean is = integralDepositService.save(integralDepositEntity);
		if (is) {
			ApiUtils.updateUserAndDeposit(integralDepositEntity, userService, predepositLogService, integralDepositService, 3,commonService);
		}
		msg=user.getUserName()+"战友，你好，你已成功投资理财项目，请在积分理财里查看";
		CommUtil.send_messageToSpecifiedUser(user, msg,userService);
		msg="购买积分理财成功";
		ApiUtils.json(response, "", msg , 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:userId 用户id
	 *@description:通过用户id,获取用户积分
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_integralBalances.htm", method = RequestMethod.POST)
	public void app_integralBalance(HttpServletRequest request,
			HttpServletResponse response, Long userId) {
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		ApiUtils.updateDepositStatus(userId, integralDepositService, predepositLogService, userService,commonService);
		String weeHours = ApiUtils.weeHours(ApiUtils.getFirstday_Lastday(new Date(), 0, 1),1);
		Date formatDate = CommUtil.formatDate(weeHours, "yyyy-MM-dd HH:mm:ss");
		String beginTime=ApiUtils.weeHours(CommUtil.formatLongDate(new Date()),0);
		Date addTime = CommUtil.formatDate(beginTime, "yyyy-MM-dd HH:mm:ss");
		String hql="from IntegralDepositEntity as obj where obj.user.id=" + userId + " and obj.orderStatus=20 and obj.endTime >= :endTime and obj.addTime < :addTime";
		Map<String, Object> parameter=new HashMap<String, Object>();
		parameter.put("endTime", formatDate);
		parameter.put("addTime", addTime);
		List<IntegralDepositEntity> list = integralDepositService.query(hql, parameter, -1, -1);
		double profit=0;
		double depositAll=0;
		if (list.size()>0) {
			for (IntegralDepositEntity i:list) {
				Integer days = i.getIntegralDepositListEntity().getDays();
				profit=profit+i.getDepositInterest()/days;
			}
		}
		hql="from IntegralDepositEntity as obj where obj.user.id=" + userId + " and obj.orderStatus=20 and obj.depositStatus = 0";
		List<IntegralDepositEntity> lists = integralDepositService.query(hql, null, -1, -1);
		if (lists.size()>0) {
			for (IntegralDepositEntity i:lists) {
				depositAll=depositAll+i.getDepositAll();
			}
		}
		profit=CommUtil.formatDouble(profit, 2);
		depositAll=CommUtil.formatDouble(depositAll, 2);		
		hql="select obj.availableBalance from User as obj where obj.id = "+userId;
		List<?> query = this.commonService.query(hql, null, -1, -1);
		BigDecimal integral=new BigDecimal(0);
		if (query.size()>0) {
			integral=(BigDecimal) query.get(0);
		}
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("availableBalance", integral);
		map.put("profit", profit);
		map.put("depositAll", depositAll);
		ApiUtils.json(response, map, "积分模块查询成功",0);
	}
	/***
	 *@author:gaohao
	 * @param ** 
	 *@return:void
	 *@param:**
	 *@description:积分理财，管理员获取积分理财记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetIntegralDepositInfo.htm", method = RequestMethod.POST)
	public void appGetIntegralDepositInfo(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String depositStatus,String currentPage){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))||(user_id==20717&&userService.getObjById(user_id).getPassword().equals(password))) {
			String hql="from IntegralDepositEntity as obj where obj.orderStatus=20 ";
			if (depositStatus!=null) {
				if (depositStatus.equals("1")) {
					hql=hql+" and obj.depositStatus=1";
				}else if(depositStatus.equals("0")){
					hql=hql+" and obj.depositStatus=0";
				}			
			}
			List<IntegralDepositEntity> list = integralDepositService.query(hql, null, current_page*pageSize, pageSize);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(IntegralDepositEntity.class, "id,user,integralDepositListEntity,depositQuantity,endTime,depositStatus,depositInterest,depositAll,rechargeWay"));
			objs.add(new FilterObj(IntegralDepositListEntity.class, "id,days,annualRate"));
			objs.add(new FilterObj(User.class, "id,userName,zhiwei,bumen"));
			objs.add(new FilterObj(ZhiWei.class, "id,name"));
			objs.add(new FilterObj(BuMen.class, "id,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, list, "获取投资理财记录成功", 0, filter);
			return;			
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 * @param ** 
	 *@return:void 
	 *@param:**
	 *@description:积分理财，用户获取自己的积分理财记录;depositStatus:0,1（0：未到期；1:已到期）
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserDepositInfo.htm", method = RequestMethod.POST)
	public void appGetUserDepositInfo(HttpServletRequest request,
			HttpServletResponse response,String depositStatus,String currentPage,String userId){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String hql="from IntegralDepositEntity as obj where obj.orderStatus=20 and obj.user.id = " + user_id;
		if (depositStatus!=null) {
			if (depositStatus.equals("1")) {
				hql=hql+" and obj.depositStatus=1";
			}else if(depositStatus.equals("0")){
				hql=hql+" and obj.depositStatus=0";
			}			
		}
		List<IntegralDepositEntity> list = integralDepositService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(IntegralDepositEntity.class, "id,integralDepositListEntity,depositQuantity,endTime,depositStatus,depositInterest,depositAll,rechargeWay"));
		objs.add(new FilterObj(IntegralDepositListEntity.class, "id,title,days,annualRate"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "获取用户投资理财记录成功", 0, filter);
		return;			
		
	}
}
