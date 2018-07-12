package com.shopping.api.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

public class AllocateWagesUtils {
	//将原来的职级金也就是现在的储备金分发掉
	public static final boolean allocationChuBei(OrderForm obj,User buyer,IUserService userService,
			ICommonService commonService,IOrderFormService orderFormService,IPredepositLogService predepositLogService){
		boolean ret=false;
		try {
			User adminUser=userService.getObjById(1l);
			ZhiWei buyerZhiwei = buyer.getZhiwei();
			AreaGradeOfUser buyerArea = buyer.getAreaGradeOfUser();
			BuMen buyerBumen = buyer.getBumen();
			double zhijiPrice=obj.getZhi_ji_price();
			double Average=CommUtil.formatDouble(zhijiPrice/8,2);
			boolean is;
			Double scale = 1d;
			if (zhijiPrice>0) {
				if (buyerBumen==null) {//部门为空
					ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金" ,userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
				}else {//部门不为空
					Map<String, Object> info=new HashMap<>();
					if (buyerZhiwei!=null&&buyerZhiwei.getId()!=0) {//有职位
						if (buyerZhiwei.getId()==109) {
							ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
						}else if (buyerZhiwei.getId()!=109&&buyerZhiwei.getPositionOrder()<=15) {//执行总裁以上的		
							info = ApiUtils.setChubeiMoney(buyer,Average, Average, "订单"+obj.getOrder_id()+"储备金", userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
							is=(boolean) info.get("is");
							scale=(Double) info.get("scale");
							if (is) {
								zhijiPrice=zhijiPrice-Average*scale;
							}
							User seniorUser=buyer;
							int i=1;
							do {
								seniorUser = ApiUtils.getSeniorUser(seniorUser,userService);
								info=ApiUtils.setChubeiMoney(seniorUser,Average, Average, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
								is=(boolean) info.get("is");
								scale=(Double) info.get("scale");
								if (is) {
									zhijiPrice=zhijiPrice-Average*scale;
									i++;
								}
								if (i>8) {
									CommUtil.send_messageToSpecifiedUser(userService.getObjById(20717l),obj.getOrder_id()+"储备金发放异常",userService);
									break;
								}
							} while (seniorUser!=null);
							if (zhijiPrice>0) {
								ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
							}
						}else if(buyerZhiwei.getId()!=109&&buyerZhiwei.getPositionOrder()>15){
							if (buyerArea==null) {//无地区
								ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
							}else {//有地区
								info = ApiUtils.setChubeiMoney(buyer,Average, Average, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
								is=(boolean) info.get("is");
								scale=(Double) info.get("scale");
								if (is) {
									zhijiPrice=zhijiPrice-Average*scale;
								}
								User seniorUser=buyer;
								int i=1;
								do {
									seniorUser = ApiUtils.getSeniorUser(seniorUser,userService);
									info=ApiUtils.setChubeiMoney(seniorUser, Average, Average, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
									is=(boolean) info.get("is");
									scale=(Double) info.get("scale");
									if (is) {
										zhijiPrice=zhijiPrice-Average*scale;
										i++;
									}
									if (i>8) {
										CommUtil.send_messageToSpecifiedUser(userService.getObjById(20717l),obj.getOrder_id()+"储备金发放异常",userService);
										break;
									}
								} while (seniorUser!=null);
								if (zhijiPrice>0) {
									ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
								}						
							}
						}		
					}else {//无职位
						if (buyerArea==null) {//无地区
							ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
						}else {//有地区
							String hql="";
							User seniorUser;
							int i=1;
							hql="select obj from User as obj where obj.areaGradeOfUser.id = " + buyerArea.getId() +" and obj.bumen.id = " + buyerBumen.getId() +" and obj.zhiwei.name not like '%副%' order by obj.zhiwei.positionOrder";
							List<User> users = userService.query(hql, null, -1, -1);
							if (users.size()>0) {
								seniorUser=users.get(0);
								info=ApiUtils.setChubeiMoney(seniorUser,Average, Average, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
								is=(boolean) info.get("is");
								scale=(Double) info.get("scale");
								if (is) {
									zhijiPrice=zhijiPrice-Average*scale;
								}
								do {
										seniorUser = ApiUtils.getSeniorUser(seniorUser,userService);
										info=ApiUtils.setChubeiMoney(seniorUser,Average, Average,"订单"+obj.getOrder_id()+ "储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
										is=(boolean) info.get("is");
										scale=(Double) info.get("scale");
										if (is) {
											zhijiPrice=zhijiPrice-Average*scale;
											i++;
										}
										if (i>8) {
											CommUtil.send_messageToSpecifiedUser(userService.getObjById(20717l),obj.getOrder_id()+"储备金发放异常",userService);
											break;
										}
								} while (seniorUser!=null);
								if (zhijiPrice>0) {
									ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
								}
							}else {
								ApiUtils.setChubeiMoney(adminUser,zhijiPrice, zhijiPrice, "订单"+obj.getOrder_id()+"储备金",userService,predepositLogService,"chubei",commonService,obj.getChu_pei_price());
							}
						}
					}
				}
			}
			ret=true;
		} catch (Exception e) {
			CommUtil.send_messageToSpecifiedUser(userService.getObjById(20717l),obj.getOrder_id()+"储备金发放异常",userService);
			return false;
		}
		return ret;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:生成相应的明细记录和改变用户余额
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/	
	public static final boolean allocateMoneyToUser(String userId,Double wagesRange,String explain,
			String moneyType,IPredepositLogService predepositLogService,IUserService userService,
			ICommonService commonService,int flag){
		synchronized (AllocateWagesUtils.class) {
			boolean ret=false;
			if(ApiUtils.is_null(userId,CommUtil.null2String(wagesRange),explain)){
				return ret;
			}
			User user=userService.getObjById(CommUtil.null2Long(userId));
			//如果对应的金，能找到的话，判断是否有资格领取，如果没有资格领取返回false，如果找不到的话，返回true,直接发掉
			if(!ApiUtils.distributionState(user, commonService, userService, moneyType)){
				return ret;
			}
			Double userMoeny=0D;//==>用户当前的余额
			String sql="select su.availableBalance from shopping_user as su where su.id="+user.getId();
			List<?> userMoenyList=commonService.executeNativeNamedQuery(sql);
			userMoeny=CommUtil.null2Double(userMoenyList.get(0));
			user.setAvailableBalance(BigDecimalUtil.add(userMoeny, wagesRange));
			ret=userService.update(user);
			if(ret){
				PredepositLog predepositLog=new PredepositLog();
				predepositLog.setAddTime(new Date());
				predepositLog.setDeleteStatus(false);
				predepositLog.setPd_log_user(user);
				predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());//记录用户当前余额
				predepositLog.setPd_log_amount(BigDecimal.valueOf(wagesRange));//当前增加了多少钱
				if(wagesRange>0){
					predepositLog.setPd_op_type("增加");
				}else{
					predepositLog.setPd_op_type("减少");
				}
				predepositLog.setPd_log_info(explain);//日志说明
				if(moneyType.equals("chubei")){
					predepositLog.setZhi_ji_price(wagesRange);
				}else if (moneyType.equals("daogou")) {
					predepositLog.setDaogou_get_price(wagesRange);
				}else if (moneyType.equals("danbao")) {
					predepositLog.setDaogou_tuijian_get_price(wagesRange);
				}else if (moneyType.equals("zhaoshang")) {
					predepositLog.setMaijia_tuijian_get_price(wagesRange);
				}else if (moneyType.equals("xianji")) {
					predepositLog.setXian_ji_price(wagesRange);
				}else if (moneyType.equals("fenhong")) {
					predepositLog.setFen_hong_price(wagesRange);
				}else if (moneyType.equals("zenggu")) {
					predepositLog.setZeng_gu_price(wagesRange);
				}else if (moneyType.equals("huokuan")) {
					predepositLog.setMaijia_get_price(wagesRange);
				}else if (moneyType.equals("yanglao")) {
					predepositLog.setYang_lao_price(wagesRange);
				}else if (moneyType.equals("shuiwu")) {
					predepositLog.setShui_wu_price(wagesRange);
				}else if (moneyType.equals("chupei")) {
					predepositLog.setChu_pei_price(wagesRange);
				}else if(moneyType.equals("yanglao")){
					predepositLog.setYang_lao_price(wagesRange);
				}
				ret=predepositLogService.save(predepositLog);
				if(ret){
					ApiUtils.asynchronousUrl("http://www.d1sc.com/autoConferZhiXian.htm?userId="+user.getId(), "GET");
					ApiUtils.asynchronousUrl("http://www.d1sc.com/appCheckUserInfo.htm?userId="+user.getId(), "GET");
					ApiUtils.updateUserRenk(0,user, commonService, userService);//更新会员等级
					ret=true;
				}
			}
			return ret;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:给对应的用户发钱,用户确认收货的入口方法
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/	
	public static final boolean distributeIncomeForUser(List<Map<String,String>> retList,String Id,IPredepositLogService predepositLogService,
			IUserService userService,ICommonService commonService,IOrderFormService orderFormService){
		synchronized(AllocateWagesUtils.class){//对同一订单进行同步，告诉其他线程已经在处这条订单中了
			boolean ret=false;
			Integer totalNum=0;//总次数
			Integer successNum=0;//成功次数
			String wagesRange="";//需要发放的金额
			String explain="";//发放金额的备注
			String moneyType="";//发放金额的类型
			String userId="";//接受钱的用户
			String sql="select so.order_status from shopping_orderform as so where so.id="+Id;
			List<?> orderStatusLit=commonService.executeNativeNamedQuery(sql);
			OrderForm of=orderFormService.getObjById(CommUtil.null2Long(Id));
			if(CommUtil.null2Int(orderStatusLit.get(0))==30){//只有已发货的单子，才可以进行确认收货的
				of.setOrder_status(40);
				ret=orderFormService.update(of);
				if(ret){
					for(Map<String,String> retMap:retList){
						userId=retMap.get("userId");
						wagesRange=retMap.get("wagesRange");
						explain=retMap.get("explain");
						moneyType=retMap.get("moneyType");
						totalNum++;
						if("chubei".equals(moneyType)){//如果是储备的话，走不同的发放渠道
							if(CommUtil.null2Double(wagesRange)>0){
								ret=AllocateWagesUtils.allocationChuBei(of, of.getUser(),userService, commonService,
										orderFormService,predepositLogService);
							}
						}else if(CommUtil.null2Double(wagesRange)>0){//保证方法的金额大于0
							ret=AllocateWagesUtils.allocateMoneyToUser(userId, CommUtil.null2Double(wagesRange), explain, moneyType,
									predepositLogService,userService,commonService,1);
						}
						if(ret){
							successNum++;
						}
					}
					if(successNum!=totalNum){
						CommUtil.send_messageToSpecifiedUser(userService.getObjById(137054L),
								CommUtil.formatLongDate(new Date()).toString()+"订单"+of.getOrder_id()+"确认收货生成的条目不合适,需要急需校验",userService);
					}
					ret=true;//为了更好的用户体验，如果部分条目生成不合适，也返回true，这条有问题的订单，管理员会去校验，核算
				}else{
					ret=false;
				}
			}	
			return ret;
		}
	}
	//创建发钱时需要的参数，一齐打包在list中，然后循环分发
	public static boolean createDistributionParams(OrderForm obj,User user,IPredepositLogService predepositLogService,
			ICommonService commonService,IOrderFormService orderFormService,IUserService userService,
			IOrderFormLogService orderFormLogService){
		User seller = userService.getObjById(obj.getStore().getUser().getId());//商家==>结算价
		User buyer = obj.getUser();//买家==>导购金
		User daogou = buyer;//导购默认是买家
		User daogou_tuijian=null;//导购担保人
		User maijia_tuijian = userService.getObjByProperty("userName",seller.getDan_bao_ren());//卖家担保人
		User zeng_gu = userService.getObjById((long) 112668);//增股
		User chu_pei = userService.getObjById((long) 112671);//储赔
		User shui_wu = userService.getObjById((long) 112672);//税务
		User xian_ji = userService.getObjById((long) 112669);//衔级
		User yang_lao = userService.getObjById((long) 112674);//养老
		User fen_hong = userService.getObjById((long) 112670);//分红
		User chang_tui = userService.getObjById((long) 124384);//厂推
		User admin = userService.getObjByProperty("userName","夏天先生");//后台管理员
		boolean ret=false;
		String orderId=CommUtil.null2String(obj.getOrder_id());
		List<Map<String,String>> retList=new ArrayList<Map<String,String>>();
		if(!"".equals(CommUtil.null2String(obj.getTuijian_id()))) {//链接优先,如果订单上面带着推荐人id的话，则以这个为主
			daogou = userService.getObjById(Long.parseLong(CommUtil.null2String(obj.getTuijian_id())));
		}
		if(!"".equals(daogou.getDan_bao_ren())){
			daogou_tuijian = userService.getObjByProperty("userName",daogou.getDan_bao_ren());
		}
		if(daogou_tuijian==null){//导购推荐人为空
			daogou_tuijian=admin;
		}
		if(maijia_tuijian==null){//卖家担保人为空
			maijia_tuijian=admin;
		}
		//职级金==>变成了现在的储备金，这里其实发的是职级金，只不过名称改变了
		Map<String,String> zhijiMap=new HashMap<String, String>();
		zhijiMap.put("userId", buyer.getId().toString());
		zhijiMap.put("wagesRange", obj.getZhi_ji_price()+"");
		zhijiMap.put("explain", "订单"+orderId+"储备金");
		zhijiMap.put("moneyType", "chubei");
		retList.add(zhijiMap);//===>添加到对应集合
		//储赔金==>是给后台管理员的，发给管理员，依据比列发放
		Map<String,String> chuBeiMap=new HashMap<String, String>();
		chuBeiMap.put("userId", chu_pei.getId().toString());
		chuBeiMap.put("wagesRange", obj.getChu_pei_price()+"");
		chuBeiMap.put("explain", "订单"+orderId+"储赔金");
		chuBeiMap.put("moneyType", "chupei");
		retList.add(chuBeiMap);//===>添加到对应集合
		//加价金
		Map<String,String> plusRangeMap=new HashMap<String, String>();
		plusRangeMap.put("userId", daogou.getId().toString());
		plusRangeMap.put("wagesRange", obj.getPlusRange_price()+"");
		plusRangeMap.put("explain", "订单"+orderId+"加价金");
		plusRangeMap.put("moneyType", "");
		retList.add(plusRangeMap);//===>添加到对应集合
		//货款金
		Map<String,String> sellerMap=new HashMap<String, String>();
		sellerMap.put("userId", seller.getId().toString());
		sellerMap.put("wagesRange", CommUtil.add(obj.getMaijia_get_price(),CommUtil.null2Double(obj.getShip_price()))+"");
		sellerMap.put("explain", "订单"+orderId+"货款金");
		sellerMap.put("moneyType", "huokuan");
		retList.add(sellerMap);//===>添加到对应集合
		//招商金
		Map<String,String> maijiaTJMap=new HashMap<String, String>();
		maijiaTJMap.put("userId", maijia_tuijian.getId().toString());
		maijiaTJMap.put("wagesRange", obj.getMaijia_tuijian_get_price()+"");
		maijiaTJMap.put("explain", "订单"+orderId+"招商金");
		maijiaTJMap.put("moneyType", "zhaoshang");
		retList.add(maijiaTJMap);//===>添加到对应集合
		//导购金
		Map<String,String> daogouMap=new HashMap<String, String>();
		daogouMap.put("userId", daogou.getId().toString());
		daogouMap.put("wagesRange", obj.getDaogou_get_price()+"");
		daogouMap.put("explain", "订单"+orderId+"导购金");
		daogouMap.put("moneyType", "daogou");
		retList.add(daogouMap);//===>添加到对应集合
		//担保金
		Map<String,String> daogouTJMap=new HashMap<String, String>();
		daogouTJMap.put("userId", daogou_tuijian.getId().toString());
		daogouTJMap.put("wagesRange", obj.getDaogou_tuijian_get_price()+"");
		daogouTJMap.put("explain", "订单"+orderId+"担保金");
		daogouTJMap.put("moneyType", "danbao");
		retList.add(daogouTJMap);//===>添加到对应集合
		//增股金
		Map<String,String> zengGuMap=new HashMap<String, String>();
		zengGuMap.put("userId", zeng_gu.getId().toString());
		zengGuMap.put("wagesRange", obj.getZeng_gu_price()+"");
		zengGuMap.put("explain", "订单"+orderId+"增股金");
		zengGuMap.put("moneyType", "zenggu");
		retList.add(zengGuMap);//===>添加到对应集合
		//税务金
		Map<String,String> shuiWuMap=new HashMap<String, String>();
		shuiWuMap.put("userId", shui_wu.getId().toString());
		shuiWuMap.put("wagesRange", obj.getShui_wu_price()+"");
		shuiWuMap.put("explain", "订单"+orderId+"税务金");
		shuiWuMap.put("moneyType", "shuiwu");
		retList.add(shuiWuMap);//===>添加到对应集合
		//衔级金
		Map<String,String> xianJiMap=new HashMap<String, String>();
		xianJiMap.put("userId", xian_ji.getId().toString());
		xianJiMap.put("wagesRange", obj.getXian_ji_price()+"");
		xianJiMap.put("explain", "订单"+orderId+"衔级金");
		xianJiMap.put("moneyType", "xianji");
		retList.add(xianJiMap);//===>添加到对应集合
		//养老金
		Map<String,String> yangLaoMap=new HashMap<String, String>();
		yangLaoMap.put("userId", yang_lao.getId().toString());
		yangLaoMap.put("wagesRange", obj.getYang_lao_price()+"");
		yangLaoMap.put("explain", "订单"+orderId+"养老金");
		yangLaoMap.put("moneyType", "yanglao");
		retList.add(yangLaoMap);//===>添加到对应集合
		//分红金
		Map<String,String> fenHongMap=new HashMap<String, String>();
		fenHongMap.put("userId", fen_hong.getId().toString());
		fenHongMap.put("wagesRange", obj.getFen_hong_price()+"");
		fenHongMap.put("explain", "订单"+orderId+"分红金");
		fenHongMap.put("moneyType", "fenhong");
		retList.add(fenHongMap);//===>添加到对应集合
		//厂推金
		Map<String,String> changTuiMap=new HashMap<String, String>();
		changTuiMap.put("userId", chang_tui.getId().toString());
		changTuiMap.put("wagesRange", obj.getCtj()+"");
		changTuiMap.put("explain", "订单"+orderId+"厂推金");
		changTuiMap.put("moneyType", "");
		retList.add(changTuiMap);//===>添加到对应集合
		//==>对订单ID进行排队
		System.out.println(user.getId()+"===="+user.getUsername());
		synchronized (obj) {
			ret=AllocateWagesUtils.distributeIncomeForUser(retList, obj.getId()+"",predepositLogService,
					userService,commonService,orderFormService);
		}
		return ret;
	}
	/***
	 *@author:gaohao
	 *@return:double
	 *@param:**
	 *@description:获取用户最大可提现金额
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static double getCashAmount(User user){
		Store store = user.getStore();
		store = store == null ? new Store() : store;
		double cash_amount=0;//最多提现金额
		double availableBalance = CommUtil.null2Double(user.getAvailableBalance());
		if (CommUtil.null2Double(user.getFreezeBlance()) == 1 && store.getStore_status() != 3) {
			cash_amount = BigDecimalUtil.sub(availableBalance, 1000d).doubleValue();
			if (cash_amount<0) {
				cash_amount = 0;
			}
		}else {
			cash_amount = availableBalance;
		}
		BigDecimal bd = new BigDecimal(cash_amount+"");
		BigDecimal setScale = bd.setScale(2, BigDecimal.ROUND_DOWN);
		cash_amount=setScale.doubleValue();
		return cash_amount;
	}
}
