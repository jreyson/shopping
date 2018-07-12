package com.shopping.api.action;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.shopping.api.domain.AccessoryApi;
import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.ZhiWeiRecoderEntity;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.domain.appDynamicImg.AppShareImg;
import com.shopping.api.domain.appDynamicImg.AppWelcomeImg;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageSwitchEntity;
import com.shopping.api.domain.power.AppSeeDataPower;
import com.shopping.api.domain.power.DepartmentPower;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.rank.UserRankName;
import com.shopping.api.domain.userAttribute.AppClickNum;
import com.shopping.api.domain.userBill.UserMonthlyBill;
import com.shopping.api.output.AppAdminData;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.api.tools.BigDecimalUtil;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Role;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.manage.admin.tools.OrderTools;

@Controller
public class AppAdminFunction {
	@Autowired
	private ISysConfigService configService;

	@Autowired
	private IUserConfigService userConfigService;

	@Autowired
	private IUserService userService;

	@Autowired
	private ICommonService commonService;

	@Autowired
	private IOrderFormService orderFormService;

	@Autowired
	private OrderTools orderTools;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsService goodsService;
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:数据统计：实时盈利动态,权限开放到执行总裁
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/realTimeProfit.htm", method = RequestMethod.POST)
	public void appGetIntegralDepositInfo(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage,String beginTime,
			String endTime){
		Map<String, Object> data = getToolsData(beginTime, endTime, currentPage);
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
//		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))||(user_id==20717&&userService.getObjById(user_id).getPassword().equals(password))) {
		if (user!=null) {
			String begin = (String) data.get("beginTime");
			String end = (String) data.get("endTime");
			Integer current_page=(Integer) data.get("current_page");
			Long days=(Long) data.get("days");
			if (days<=60) {
				int pageSize=20;
				String sql_str ="SELECT "+
						  "temp4.name2, "+
						 "temp4.payTimes, "+
						  "temp4.userName, "+
						  "temp4.id, "+
						  "round(temp4.daogou_get_price,2), "+
						  "round(temp4.totalPrice,2), "+
						 "temp4.orderId, "+
						  "temp4.name3 , "+
						  "temp4.zhixianName, "+
						  "sa.path, "+
						  "sa.name "+
						  "from shopping_accessory as sa right join "+
						  "(SELECT "+
						  "temp3.name2, "+
						  "temp3.payTimes, "+
						  "temp3.userName, "+
						  "temp3.id, "+
						  "temp3.daogou_get_price, "+
						  "temp3.totalPrice, "+
						  "temp3.orderId, "+
						  "temp3.name as name3, "+
						  "temp3.photo_id, "+
						  "ez.name as zhixianName from  shopping_zhixian as ez right join "+
						  "("+
						"SELECT "+
						  "temp2.name2, "+
						  "temp2.payTimes, "+
						  "temp2.userName, "+
						  "temp2.id, "+
						  "temp2.daogou_get_price, "+
						  "temp2.totalPrice, "+
						  "temp2.orderId, "+
						  "sagou.name ,"+
						  "temp2.photo_id, "+
			              "temp2.zhixian_id "+
						  "from  shopping_area_grade_of_user as sagou "+
						"right join ("+
						"SELECT "+
						  "eb.name as name2, "+
						  "temp.payTimes, "+
						  "temp.userName, "+
						  "temp.id, "+
						  "temp.daogou_get_price, "+
						  "temp.totalPrice, "+
						  "temp.orderId, "+
						  "temp.areaGradeOfUser_id ,"+
						  "temp.photo_id, "+
			              "temp.zhixian_id "+
						"FROM ecm_bumen AS eb "+
						  "RIGHT JOIN (SELECT "+
						               "DATE_FORMAT(so.payTimes,'%Y-%m-%d %H:%i:%s') AS payTimes, so.id as orderId, "+
						               "su.areaGradeOfUser_id, "+
						               "su.photo_id, "+
						               "su.zhixian_id, "+
						               "su.bumen_id, "+
						               "su.userName, "+
						               "su.id, "+
						               "so.daogou_get_price, "+
						               "so.totalPrice "+
						             "FROM shopping_user AS su "+
						               "LEFT JOIN shopping_orderform AS so "+
						                 "ON so.user_id = su.id "+
						             "WHERE so.order_status IN(20,30,40,50,60) "+
						                 "AND so.payTimes >='"+begin+"' "+
						                 "AND so.payTimes <='"+end+"' "+
						             "ORDER BY so.payTimes DESC "+
						             "LIMIT "+current_page*pageSize+","+pageSize+" ) AS temp "+
						    "ON temp.bumen_id = eb.id "+
						             ") as temp2 "+
						     "on temp2.areaGradeOfUser_id=sagou.id"
						             +") as temp3 "+
						     "on temp3.zhixian_id=ez.id"
						             +") as temp4 on temp4.photo_id=sa.id ORDER BY temp4.payTimes DESC ";
				List<?> xibao =this.commonService.executeNativeNamedQuery(sql_str);
				ApiUtils.json(response, xibao, "获取实时盈利动态成功", 0);
				return;			
			}else {
				ApiUtils.json(response, "", "查询时间不能大于60天", 1);
				return;
			}
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:search_value 模糊搜索的值;condition取值：userName查询用户，bumenName查询部门）
	 *@description:数据统计：个人盈利排行
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/personalEarningsRankings.htm", method = RequestMethod.POST )
	public void person_count(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String beginTime,
			String endTime, String condition, String search_value,String userId,String password) {
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
			Map<String, Object> data = getToolsData(beginTime, endTime, currentPage);
			String begin = (String) data.get("beginTime");
			String end = (String) data.get("endTime");
			Integer current_page=(Integer) data.get("current_page");
			int pageSize = 20;
			String demand = "";
			if (condition != null) {
				if ("userName".equals(condition) && !"".equals(CommUtil.null2String(search_value))) {
					demand = " and li.userName like '%" + search_value + "%' ";
				}
				if ("bumenName".equals(condition) && !"".equals(CommUtil.null2String(search_value))) {
					demand = " and b.name like '%" + search_value + "%' ";
				}
			}
			String sql = "select temp.bumenName,temp.userName,temp.daogou,temp.market,sz.name from  shopping_zhixian as sz right join ("+
					"SELECT " +
					"b.name as bumenName, " +
					"li.userName, " +
					"round(li.daogou_price,2) as daogou, " +
					"round(li.amount,2) as market, " +
					"li.zhixian_id " +
						"FROM "
					+ "(" +
					"SELECT " +
					"u.bumen_id," +
					"u.userName, " +
					"u.zhixian_id, " +
					"SUM(o.daogou_get_price) AS daogou_price," +
					"SUM(o.goods_amount) AS amount " +
						"FROM shopping_orderform o "
					+ "JOIN shopping_user u ON o.user_id=u.id WHERE o.payTimes>'"
					+ begin + "' AND  o.payTimes<'" + end + 
					"' and (" +
					"o.order_status =20 OR o.order_status=40 OR o.order_status=50 OR o.order_status=30 or o.order_status=60)" +
						" GROUP BY u.id) li "
					+ "JOIN ecm_bumen b ON li.bumen_id = b.id " + demand
					+ " ORDER BY li.daogou_price DESC limit "+current_page*pageSize+","+pageSize+" ) as temp on temp.zhixian_id = sz.id ORDER BY temp.daogou DESC";
			List<?> list = commonService.executeNativeNamedQuery(sql);
			double yingLi = 0.00;
			double xiaoShou = 0.00;
			sql = "SELECT " +
					"sum(round(daogou_price,2)), " +
					"sum(round(amount,2)) FROM "
					+ "(" +
					"SELECT u.bumen_id,u.userName, " +
					"SUM(o.daogou_get_price) AS daogou_price," +
					"SUM(o.goods_amount) AS amount " +
						"FROM shopping_orderform o "
					+ "JOIN shopping_user u ON o.user_id=u.id WHERE o.payTimes>'"
					+ begin + "' AND  o.payTimes<'" + end + 
					"' and (o.order_status =20 OR o.order_status=40 OR o.order_status=50 OR o.order_status=30 or o.order_status=60)" +
							" GROUP BY u.id) li "
					+ "JOIN ecm_bumen b ON li.bumen_id = b.id " + demand
					+ " ORDER BY li.daogou_price DESC";
			List<?> sum = commonService.executeNativeNamedQuery(sql);
			if (sum.size()>0) {
				Object[] object = (Object[]) sum.get(0);
				yingLi = CommUtil.null2Double(object[0]);
				xiaoShou = CommUtil.null2Double(object[1]);
			}
			AppAdminData adminData=new AppAdminData();
			adminData.setFirstData(xiaoShou);
			adminData.setSecondData(yingLi);
			adminData.setSortData(list);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AppAdminData.class, "firstData,secondData,sortData"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, adminData, "获取个人盈利排行成功", 0,filter);
			return;
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}		
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:数据统计：部门盈利排行，权限开放到执行总裁
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/divisionEarningsRankings.htm", method = RequestMethod.POST )
	public void divisionEarningsRankings(HttpServletRequest request,
			HttpServletResponse response, String beginTime,
			String endTime,String userId,String password) {
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
//		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))||(user_id==20717&&userService.getObjById(user_id).getPassword().equals(password))) {
		if (user!=null) {
			Map<String, Object> data = getToolsData(beginTime, endTime, "");
			String begin = (String) data.get("beginTime");
			String end = (String) data.get("endTime");
			Long days=(Long) data.get("days");
			if (days>60) {
				ApiUtils.json(response, "", "查询时间不能大于60天", 1);
				return;
			}
//			String sql="SELECT " +
//					"b.name, " +
//					"round(daogou_price,2), " +
//					"round(amount,2) " +
//					"FROM "+ 
//					"(" +
//						"SELECT " +
//						"u.bumen_id," +
//						"SUM(o.daogou_get_price) AS daogou_price," +
//						"SUM(o.totalPrice) AS amount " +
//					"FROM shopping_orderform o "+ 
//					"JOIN shopping_user u ON o.user_id=u.id WHERE o.payTimes>'"+begin+
//					 "' AND  o.payTimes<'"+ end+
//					 "' and (o.order_status =20 OR o.order_status=40 OR o.order_status=50 OR o.order_status=30 or o.order_status=60) GROUP BY u.bumen_id) li "+
//					"right JOIN ecm_bumen b ON li.bumen_id = b.id ORDER BY round(daogou_price,2) DESC";
			String sql="SELECT " +
					  "temp.name, " +
					  "( " +
					    "CASE " +
					      "WHEN temp.daogou_price IS NULL " + 
					      "THEN 0 " + 
					      "ELSE temp.daogou_price " + 
					    "END " +
					  "), " +
					  "( " +
					    "CASE " +
					      "WHEN temp.amount IS NULL " + 
					      "THEN 0 " +
					      "ELSE temp.amount " +
					    "END " +
					  ") " + 
					"FROM " +
					  "(SELECT " + 
					    "b.name, " +
					    "ROUND(daogou_price, 2) AS daogou_price, " +
					    "ROUND(amount, 2) AS amount " + 
					  "FROM " +
					    "(SELECT " + 
					      "su.bumen_id, " +
					      "ROUND(SUM(temp.daogou_get_price), 2) AS daogou_price, " +
					      "ROUND(SUM(temp.totalPrice), 2) AS amount " + 
					    "FROM " +
					      "shopping_user AS su " + 
					      "RIGHT JOIN " + 
					        "(SELECT " + 
					          "o.user_id, " +
					          "o.daogou_get_price, " +
					          "o.totalPrice " +
					        "FROM " +
					          "shopping_orderform AS o " + 
					        "WHERE o.order_status IN (20, 30, 40, 50, 60) " + 
					          "AND o.payTimes >= '"+begin+"' " + 
					          "AND o.payTimes <= '"+end+"') AS temp " + 
					        "ON su.id = temp.user_id " + 
					    "GROUP BY su.bumen_id " + 
					    "ORDER BY daogou_price DESC) li " + 
					    "RIGHT JOIN ecm_bumen b " +
					      "ON li.bumen_id = b.id " +
					  "WHERE b.id NOT IN (601,602,603,604,605,606,607,608,609,610) " + 
					  "ORDER BY ROUND(daogou_price, 2) DESC) AS temp";
			List<?> list = commonService.executeNativeNamedQuery(sql);
			String sumSql="SELECT " +
					"round(SUM(o.daogou_get_price),2) AS daogou_price," +
					"round(SUM(o.totalPrice),2) AS amount " +
						"FROM shopping_orderform o WHERE " +
					"o.payTimes>'"+begin+"' " +
					"AND o.payTimes<'"+end+"' " +
					"AND (o.order_status =20 OR o.order_status=40 OR o.order_status=50 OR o.order_status=30 OR o.order_status=60)";
			List<?> sumData = commonService.executeNativeNamedQuery(sumSql);
			double yingLi = 0.00;
			double xiaoShou = 0.00;
			if (sumData.size() > 0) {
				Object[] object = (Object[]) sumData.get(0);
				yingLi = CommUtil.null2Double(object[0]);
				xiaoShou = CommUtil.null2Double(object[1]);
			}
			AppAdminData adminData=new AppAdminData();
			adminData.setFirstData(xiaoShou);
			adminData.setSecondData(yingLi);
			adminData.setSortData(list);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AppAdminData.class, "firstData,secondData,sortData"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, adminData, "获取部门盈利排行成功", 0,filter);
			return;
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}		
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:数据统计：推荐人排行，权限开放到执行总裁
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/refereeRankings.htm", method = RequestMethod.POST )
	public void refereeRankings(HttpServletRequest request,
			HttpServletResponse response, String beginTime,
			String endTime,String userId,String password,String currentPage) {
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
//		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))||(user_id==20717&&userService.getObjById(user_id).getPassword().equals(password))) {
		if (user!=null) {
			Map<String, Object> data = getToolsData(beginTime, endTime, currentPage);
			String begin = (String) data.get("beginTime");
			String end = (String) data.get("endTime");
			Integer current_page=(Integer) data.get("current_page");
			Long days=(Long) data.get("days");
			if (days>60) {
				ApiUtils.json(response, "", "查询时间不能大于60天", 1);
				return;
			}
			int pageSize = 20;
			String sql="SELECT "+
						  "temp3.userName, "+
						  "temp3.danbao, "+
						  "temp3.bumenName, "+
						  "sz.name  "+
						"FROM "+
						  "shopping_zhixian as sz "+
						  "RIGHT JOIN  "+
						    "(SELECT  "+
						      "temp2.userName, "+
						      "temp2.danbao, "+
						      "temp2.zhixian_id, "+
						      "b.name AS bumenName  "+
						    "FROM "+
						      "ecm_bumen AS b  "+
						      "RIGHT JOIN  "+
						        "(SELECT  "+
						          "su.id, "+
						          "su.userName, "+
						          "temp.danbao, "+
						          "su.bumen_id, "+
						          "su.zhixian_id  "+
						        "FROM "+
						          "shopping_user AS su  "+
						          "RIGHT JOIN  "+
						            "(SELECT  "+
						              "u.dan_bao_ren, "+
						             " COUNT(u.dan_bao_ren) AS danbao  "+
						            "FROM "+
						              "shopping_user AS u  "+
						           " WHERE u.addTime > '"+ begin+"' "+
						              "AND u.addTime < '"+end+"' "+
						            "GROUP BY u.dan_bao_ren  "+
						            "ORDER BY danbao DESC "+ 
						            "LIMIT " + (current_page * pageSize) + "," + pageSize+") AS temp "+ 
						            "ON temp.dan_bao_ren = su.userName) AS temp2 "+ 
						        "ON temp2.bumen_id = b.id) AS temp3 "+ 
						    "ON temp3.zhixian_id = sz.id order by temp3.danbao desc";
			List<?> list = commonService.executeNativeNamedQuery(sql);
			ApiUtils.json(response, list, "获取推荐人排行成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:search_value 模糊搜索的值;condition取值：userName查询用户，bumenName查询部门）
	 *@description:数据统计：积分排行
	 *@function:**
	 *@exception:*******大宋贸易/20024   大宋贸易2/24264    大宋公司/21452   战略合作金/124384   职级金/112673   衔级金/112669  赠股金/112668  分红金/112670  税务金/112672  储赔金/112671  养老金/112674 经纬教育/121274
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/integralRankings.htm", method = RequestMethod.POST )
	public void integralRankings(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String begin,
			String end, String condition, String search_value,String userId,String password) {
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
			Map<String, Object> data = getToolsData(null, null, currentPage);
			double beginNum = 0;
			double endNum = CommUtil.null2Double(commonService
					.executeNativeNamedQuery(
							"select max(availableBalance) from shopping_user").get(
							0));;
			if (!"".equals(CommUtil.null2String(begin))) {
				beginNum = CommUtil.null2Double(begin);
			}
			if (!"".equals(CommUtil.null2String(end))) {
				endNum = CommUtil.null2Double(end);
			}
			Integer current_page=(Integer) data.get("current_page");
			int pageSize = 20;
			String where = "";
			if (condition != null) {
				if ("userName".equals(condition) && !"".equals(CommUtil.null2String(search_value))) {
					where = " AND userName like '%" + search_value + "%'";
				}
				if ("bumenName".equals(condition) && !"".equals(CommUtil.null2String(search_value))) {
					List query = commonService
							.executeNativeNamedQuery("select id from ecm_bumen where name like '%"
									+ search_value + "%'");
					int Bname = 0;
					if (query.size() > 0) {
						where = " AND bumen_id in(";
						for (int i = 0; i < query.size(); i++) {
							Bname = (Integer) query.get(i);
							where=where+Bname;
							if (query.size()>1&&i+1<query.size()) {
								where=where+",";
							}
						}
						where = where+" ) ";
					}					
				}
			}
			String sql ="SELECT "+
					  "temp2.userName, "+
					  "temp2.bumen_id, "+
					  "round(temp2.availableBalance,2), "+
					  "sz.name  "+
					"FROM "+
					  "shopping_zhixian as sz "+
					  "RIGHT JOIN  "+
					"( select  userName,bumen_id,availableBalance,zhixian_id from shopping_user  where id not in (20024,24264,21452,124384,112673,112669,112668,112670,112672,112671,112674,120258,121274) and availableBalance>="
					+ beginNum
					+ " AND  availableBalance<="
					+ endNum
					+ " "
					+ where
					+ "  ORDER BY availableBalance DESC limit "
					+ (current_page * pageSize) + "," + pageSize+") as temp2 on temp2.zhixian_id=sz.id  ORDER BY round(temp2.availableBalance,2) desc";
			List<?> list = commonService.executeNativeNamedQuery(sql);
			String yingLi = CommUtil.null2String(commonService
					.executeNativeNamedQuery(
							"select round(sum(availableBalance),2) from shopping_user ")
					.get(0));
			List<Object[]> sort=new ArrayList<Object[]>();
			for (int i = 0; i < list.size(); i++) {
				Object[] objec=(Object[])list.get(i);
				if (objec[1]!=null) {
					String hql="select obj.name from BuMen as obj where obj.id=" + objec[1];
					List<?> query = commonService.query(hql, null, -1, -1);
					if (query.size()>0) {
						objec[1]=query.get(0);
					}				
				}
				sort.add(objec);
			}
			AppAdminData adminData=new AppAdminData();
			adminData.setFirstData(yingLi);
			adminData.setSortData(list);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AppAdminData.class, "firstData,sortData"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, adminData, "获取积分排行成功", 0,filter);
			return;
		}else {
			ApiUtils.json(response, "", "权限不足或者密码错误", 1);
			return;
		}
	}
	private Map<String, Object> getToolsData(String beginTime,String endTime,String currentPage){
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
		if (beginTime!=null||endTime!=null) {
			if (beginTime==null) {
				String newTime=sft.format(new Date());
				long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
				if (days<0) {
					endTime=newTime;
				}
				try {
					beginTime=ApiUtils.getFirstday_Lastday(sft.parse(endTime), 0, 0);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else if (endTime==null) {
				try {
					endTime=ApiUtils.getFirstday_Lastday(sft.parse(beginTime), 1, 0);
					String newTime=sft.format(new Date());
					long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
					if (days<0) {
						endTime=newTime;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else {
				String newTime=sft.format(new Date());
				long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
				if (days<0) {
					endTime=newTime;
				}
			}
		}else {
			Date date = new Date();
			beginTime = ApiUtils.getFirstday_Lastday(date, 0, 0);
			endTime=sft.format(date);
		}
		int current_page=0;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin_Time=ApiUtils.weeHours(beginTime, 0);
		String end_Time=ApiUtils.weeHours(endTime, 1);
		Map<String, Object> map=new HashMap<String, Object>();
		long days = ApiUtils.acquisitionTimeSegment(beginTime, endTime);
		map.put("beginTime", begin_Time);
		map.put("endTime", end_Time);
		map.put("days", days);
		map.put("current_page", current_page);
		return map;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:招兵短信
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/InvitingUserSMS.htm", method = RequestMethod.POST )
	public void InvitingUserSMS(HttpServletRequest request,
			HttpServletResponse response,String userId,String password) {
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))) {
			String hql="select distinct obj.user_id from shopping_user_clickapps as obj where obj.addTime >'2017-11-3'";
			@SuppressWarnings("unchecked")
			List<BigInteger> query1 = commonService.executeNativeNamedQuery(hql);
			Map<BigInteger, Object> clickapps=new HashMap<BigInteger, Object>();
			for (BigInteger uid:query1) {
				if (uid!=null) {
					clickapps.put(uid, "1");			
				}
			}
			String count="SELECT count(1) FROM shopping_user WHERE loginCount > 10 AND mobile REGEXP '^[1][345678][0-9]{9}$'";
			@SuppressWarnings("unchecked")
			List<BigInteger> countNum = commonService.executeNativeNamedQuery(count);
			int sum=countNum.get(0).intValue();
			int frequency=sum%1000==0?sum/1000:sum/1000+1;
			int l=0;
			for (int i = 0; i < frequency; i++) {
				String sql="SELECT id,mobile,userName FROM shopping_user WHERE loginCount > 10 AND mobile REGEXP '^[1][345678][0-9]{9}$' order by mobile limit "+i*1000+",1000";
				@SuppressWarnings("unchecked")
				List<Object[]> query = commonService.executeNativeNamedQuery(sql);
				Map<Object, Object> users=new HashMap<Object, Object>();
				for (Object[] obj:query) {
					Object sign = clickapps.get(obj[0]);
					if (sign==null){
						users.put(obj[1], obj[2]);
					}
				}
				Set<Object> mobilekey = users.keySet();
				for (Object obj:mobilekey) {
//					ApiUtils.pushNoticeSMS(users.get(obj), obj);
					l++;
				}
			}
			System.out.println(l);
		}else {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:补发任职人员短信通知
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/supplyUserSMS.htm", method = RequestMethod.POST )
	public void supplyUserSMS(HttpServletRequest request,
			HttpServletResponse response,String userId,String password) {
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		if ((user_id==1&&userService.getObjById(user_id).getPassword().equals(password))) {
			String sql="SELECT DISTINCT user_id FROM `shopping_user_clickapps` WHERE user_id NOT IN(SELECT DISTINCT myselfUser_id FROM `shopping_zhiwei_recorder` AS szr WHERE szr.addTime>'2018-01-05 14:30:00')";
			@SuppressWarnings("unchecked")
			List<BigInteger> query = commonService.executeNativeNamedQuery(sql);
			for (BigInteger b : query) {
				if (b!=null) {
					long id = b.longValue();
					User user = userService.getObjById(id);
					if (user.getMobile()!=null&&id!=1) {
						String type = ApiUtils.judgmentType(user.getMobile().trim().toString());
						ZhiWei zhiwei = user.getZhiwei();
						if (type.equals("mobile")&&zhiwei!=null&&zhiwei.getId()!=0&&!zhiwei.equals("")) {
//							ApiUtils.pushNotice(user);
						}
					}
				}
			}
		}else {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:查看下载人数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appStatisticsNum.htm", method = RequestMethod.POST )
	public void appStatisticsNum(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String beginTime,
			String endTime,String currentPage) {
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Map<String, Object> data = getToolsData(beginTime, endTime, null);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		String sql="SELECT "+
					  "DISTINCT user_id "+ 
					"FROM "+
					  "shopping_user_clickapps AS suc "+ 
					"WHERE suc.addTime >= '" + begin + "' "+ 
					  "AND suc.addTime <= '" + end + "' "+
					  "AND suc.user_id NOT IN "+ 
					  "(SELECT DISTINCT "+ 
					    "user_id "+ 
					  "FROM "+
					    "shopping_user_clickapps AS suc "+ 
					  "WHERE suc.addTime < '" + begin + "' "+ 
					    "AND suc.addTime >= '2017-05-25 14:11:13')";
		String sql1="SELECT "+
				  "DISTINCT user_id "+ 
				"FROM "+
				  "shopping_user_clickapps AS suc "+ 
				"WHERE suc.addTime >= '" + begin + "' "+ 
				  "AND suc.addTime <= '" + end + "' "+
				  "AND suc.user_id NOT IN "+ 
				  "(SELECT DISTINCT "+ 
				    "user_id "+ 
				  "FROM "+
				    "shopping_user_clickapps AS suc "+ 
				  "WHERE suc.addTime < '" + begin + "' "+ 
				    "AND suc.addTime >= '2017-05-25 14:11:13') limit "+pageSize*current_page+","+pageSize;
		List<BigInteger> count = commonService.executeNativeNamedQuery(sql);
		List<BigInteger> userIds = commonService.executeNativeNamedQuery(sql1);
		String downloadSum_sql="SELECT count(DISTINCT user_id) FROM shopping_user_clickapps";
		List<BigInteger> downloadSum = commonService.executeNativeNamedQuery(downloadSum_sql);
		AppTransferData appStatisticsData=new AppTransferData();
		List<User> users=new ArrayList<User>();
		for (BigInteger user_id : userIds) {
			if (user_id!=null) {
				User newUser = userService.getObjById(user_id.longValue());
				users.add(newUser);
			}
		}
		int sum=0;
		if (downloadSum.size()>0) {
			sum=downloadSum.get(0).intValue();
		}	
		appStatisticsData.setFourthData(sum);
		appStatisticsData.setSecondData(count.size());
		appStatisticsData.setFifthData(CommUtil.formatShortDate(CommUtil.formatDate(begin, "yyyy-MM-dd")));
		appStatisticsData.setFirstData(CommUtil.formatShortDate(CommUtil.formatDate(end, "yyyy-MM-dd")));
		appStatisticsData.setThirdData(users);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,fourthData,fifthData,secondData,thirdData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appStatisticsData, "查询新增人数成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:部门招人排行榜，权限开放到执行总裁
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGetbumenRank.htm", method = RequestMethod.POST )
	public void appGetbumenRank(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String beginTime,
			String endTime) {
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
		if (user==null) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		Map<String, Object> data = getToolsData(beginTime, endTime, null);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Long days=(Long) data.get("days");
		if (days>60) {
			ApiUtils.json(response, "", "查询时间不能大于60天", 1);
			return;
		}
//		String sql="SELECT "+ 
//					  "eb.name, "+
//					  "temp.num "+ 
//					"FROM "+
//					  "ecm_bumen AS eb "+ 
//					  "left JOIN "+ 
//					    "(SELECT "+ 
//					      "COUNT(obj.bumen_id) AS num, "+
//					     "obj.bumen_id "+ 
//					   "FROM "+
//					      "shopping_user AS obj "+ 
//					    "WHERE obj.addTime >= '"+begin+"' "+ 
//					      "AND obj.addTime <= '"+end+"' "+ 
//					      "AND obj.`bumen_id` NOT IN (301,515,511,512,513,514) "+ 
//					    "GROUP BY obj.bumen_id) AS temp "+ 
//					    "ON eb.id = temp.bumen_id " +
//					  "order by temp.num desc";
		String sql="SELECT "+ 
				  "obj.name, "+
				  "( "+
				    "CASE "+
				      "WHEN obj.num IS NULL "+ 
				      "THEN 0 "+ 
				      "ELSE obj.num "+ 
				    "END "+
				  ") "+ 
				"FROM "+
				  "(SELECT "+ 
				    "eb.name, "+
				    "temp.num "+
				  "FROM "+
				    "ecm_bumen AS eb "+ 
				    "LEFT JOIN "+ 
				      "(SELECT "+ 
				        "COUNT(obj.bumen_id) AS num, "+
				        "obj.bumen_id "+ 
				      "FROM "+
				        "shopping_user AS obj "+
				      "WHERE obj.addTime >= '"+begin+"' "+
				        "AND obj.addTime <= '"+end+"' "+ 
				        "AND obj.bumen_id not in (601,602,603,604,605,606,607,608,609,610,515,301) "+ 
				      "GROUP BY obj.bumen_id) AS temp "+
				      "ON eb.id = temp.bumen_id "+
				  "WHERE eb.id not in (601,602,603,604,605,606,607,608,609,610,515,301) "+
				  "ORDER BY temp.num DESC) AS obj ";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		String sql2="SELECT "+ 
				  "eb.name, "+
				  "temp.num "+ 
				"FROM "+
				  "ecm_bumen AS eb "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "COUNT(obj.bumen_id) AS num, "+
				     "obj.bumen_id "+ 
				   "FROM "+
				      "shopping_user AS obj "+ 
				    "WHERE obj.addTime >= '"+begin+"' "+ 
				      "AND obj.addTime <= '"+end+"' "+ 
				      "AND obj.`bumen_id` IN (301,515) "+ 
				    "GROUP BY obj.bumen_id) AS temp "+ 
				    "ON eb.id = temp.bumen_id " +
				  "order by temp.num desc";
		List<?> query2 = commonService.executeNativeNamedQuery(sql2);
		AppAdminData outData=new AppAdminData();
		outData.setFirstData(query);
		outData.setSecondData(query2);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppAdminData.class, "firstData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, outData, "查询部门招人排行成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:gradeType:发送的级别类型
	 *@description:给各级发送群组消息:执行总裁  17;大区总裁  120;总裁    14;总经理  11;执行总监  8;运营总监  5;团队长  3;全体商家  seller;全体app用户  appUsers。
	 *@function:**                 20     150         619    2400    1324    299    94          6810          3148
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/adoptGradeSendMessage.htm", method = RequestMethod.POST )
	public void adoptGradeSendMessage(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String message,String gradeType){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		boolean is_null = ApiUtils.is_null(message,gradeType);
		if (is_null) {
			ApiUtils.json(response, "", "参数不能为空！", 1);
			return;
		}
		String[] zhiweis={"17","120","14","11","8","5","3"};
		String countSql="";
		String DataHql="";
		int current_page=0;
		int pageSize=100;
		boolean isZhiwei=false;
		for (String zhiwei : zhiweis) {
			if (zhiwei.equals(gradeType)) {
				isZhiwei=true;
				break;
			}	
		}
		if (gradeType.equals("seller")) {//#1
			countSql="SELECT COUNT(1) FROM shopping_user AS su LEFT JOIN shopping_store AS ss ON su.store_id=ss.id WHERE ss.store_status=2";
			DataHql="SELECT obj FROM User AS obj LEFT JOIN obj.store AS ss where ss.store_status = 2 ";
		}else if(gradeType.equals("appUsers")){//#2
			countSql="SELECT COUNT(1) FROM shopping_user_appclicknum";
			DataHql="select obj from AppClickNum as acn left join acn.user as obj";
		}else if(isZhiwei){//#3
			countSql="SELECT COUNT(1) FROM shopping_user WHERE zhiwei_id = " + gradeType;
			DataHql="SELECT obj FROM User as obj WHERE obj.zhiwei.id = " + gradeType;
		}else {
			ApiUtils.json(response, "", "参数错误！", 1);
			return;
		}
		List<BigInteger> count = commonService.executeNativeNamedQuery(countSql);
		int num=count.get(0).intValue()%100==0?count.get(0).intValue()/100:count.get(0).intValue()/100+1;
		int ii=0;
		for (int i = 0; i < num; i++) {
			System.out.println(i);
			List<User> users = userService.query(DataHql, null, current_page*pageSize, pageSize);
			current_page++;
			for (User user : users) {
				ApiUtils.sendMessageToSpecifiedUser(user, message, userService, userId);
				ii++;
			}
		}
		System.out.println(ii);
		ApiUtils.json(response, "", "发送成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员更改用户密码
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminChangeUserPassword.htm", method = RequestMethod.POST )
	public void appAdminChangeUserPassword(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String appointUserId,String appointPassword){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(appointUserId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "用户id错误！", 1);
			return;
		}
		if (CommUtil.null2String(appointPassword).equals("")) {
			ApiUtils.json(response, "", "请输入要更改的密码！", 1);
			return;
		}
		User user = userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "该用户不存在！", 1);
			return;
		}
		String lowerCase=Md5Encrypt.md5(appointPassword).toLowerCase();
		user.setPassword(lowerCase);
		boolean update = userService.update(user);
		if (update) {
			boolean haveHuanxin = ApiUtils.isHaveHuanxin(user.getId());
			if (haveHuanxin) {
				CommUtil.update_user_password(user.getPassword().toString(), user.getId().toString());//将用户的密码同步至环信服务器
			}
			ApiUtils.json(response, "", "修改密码成功。", 0);
			return;
		}else {
			ApiUtils.json(response, "", "操作失败！", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:数据实况：担保动态排名
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminGuaranteeRank.htm", method = RequestMethod.POST )
	public void appAdminGuaranteeRank(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		boolean is_admin = ApiUtils.isAdmin(userId, password, userService);
		if (!is_admin) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Date date = new Date();
		String time = ApiUtils.getFirstday_Lastday(date, 0, 7);
		time=ApiUtils.weeHours(time, 0);
		String sql="SELECT "+
					  "obj.id, "+
					  "obj.userName, "+
					  "obj.loginDate, "+
					  "temp2.num "+
					"FROM "+
					  "shopping_user AS obj "+ 
					  "RIGHT JOIN "+
					    "(SELECT "+ 
					      "su.dan_bao_ren, "+
					      "COUNT(1) AS num "+ 
					    "FROM "+
					      "shopping_user AS su "+ 
					      "RIGHT JOIN "+ 
					        "(SELECT "+ 
					          "sua.user_id "+
					        "FROM "+
					          "shopping_user_appclicknum AS sua "+ 
					        "WHERE sua.loginDate >= '"+time+"') AS temp "+
					        "ON su.id = temp.user_id "+ 
					        "where su.dan_bao_ren <> '' "+ 
					        "and su.dan_bao_ren is not null "+ 
					    "GROUP BY su.dan_bao_ren "+
					    "ORDER BY num DESC "+
					    "LIMIT "+current_page*pageSize+", "+pageSize+") AS temp2 "+
					    "ON temp2.dan_bao_ren = obj.userName "+
					"ORDER BY temp2.num DESC";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, query, "查询担保动态排名成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:数据实况：查看要发放的衔级金
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminCheckXianjiMoney.htm", method = RequestMethod.POST )
	public void appAdminCheckXianjiMoney(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		boolean is_admin = ApiUtils.isAdmin(userId, password, userService);
		if (!is_admin) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin =ApiUtils.weeHours(null, 0);
		String beginTime = ApiUtils.getFirstday_Lastday_Month(begin, 0);
		String endTime = ApiUtils.getFirstday_Lastday(CommUtil.formatDate(beginTime, "yyyy-MM-dd HH:mm:ss"), 0, 7);
		endTime=ApiUtils.weeHours(endTime, 0);
		String sql="SELECT "+
			  "obj.id, "+
			  "temp3.num "+ 
			"FROM "+
			  "shopping_user AS obj "+
			  "RIGHT JOIN "+ 
			    "(SELECT "+ 
			      "temp2.dan_bao_ren,temp2.num "+ 
			    "FROM "+
			      "(SELECT "+ 
			        "su.dan_bao_ren, "+
			        "COUNT(1) AS num "+
			      "FROM "+
			        "shopping_user AS su "+ 
			        "RIGHT JOIN "+ 
			          "(SELECT "+ 
			            "sua.user_id "+ 
			          "FROM "+
			            "shopping_user_appclicknum AS sua "+
			         "WHERE sua.loginDate <= '" + beginTime + "'"+
			            "AND sua.loginDate >= '" + endTime + "') AS temp "+
			         " ON su.id = temp.user_id "+
			      "WHERE su.dan_bao_ren <> '' "+ 
			        "AND su.dan_bao_ren IS NOT NULL "+ 
			     " GROUP BY su.dan_bao_ren "+ 
			      "ORDER BY num DESC) AS temp2 "+
			    "WHERE num >= 5 "+
			    "LIMIT "+current_page*pageSize+", "+pageSize+") AS temp3 "+ 
			    "ON temp3.dan_bao_ren = obj.userName "+ 
			"ORDER BY temp3.num DESC ";
		List<Object[]> query = commonService.executeNativeNamedQuery(sql);
		List<AppTransferData> users=geZhijiDatas(query, "");
		
		//统计总共需要发放多少钱
		int page=0;
		pageSize=100;
		sql="SELECT "+
				  "obj.id, "+
				  "temp3.num "+ 
				"FROM "+
				  "shopping_user AS obj "+
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "temp2.dan_bao_ren,temp2.num "+ 
				    "FROM "+
				      "(SELECT "+ 
				        "su.dan_bao_ren, "+
				        "COUNT(1) AS num "+
				      "FROM "+
				        "shopping_user AS su "+ 
				        "RIGHT JOIN "+ 
				          "(SELECT "+ 
				            "sua.user_id "+ 
				          "FROM "+
				            "shopping_user_appclicknum AS sua "+
				         "WHERE sua.loginDate <= '" + beginTime + "'"+
				            "AND sua.loginDate >= '" + endTime + "') AS temp "+
				         " ON su.id = temp.user_id "+
				      "WHERE su.dan_bao_ren <> '' "+ 
				        "AND su.dan_bao_ren IS NOT NULL "+ 
				     " GROUP BY su.dan_bao_ren "+ 
				      "ORDER BY num DESC) AS temp2 "+
				    "WHERE num >= 5 "+
				    "LIMIT "+page*pageSize+", "+pageSize+") AS temp3 "+ 
				    "ON temp3.dan_bao_ren = obj.userName "+ 
				"ORDER BY temp3.num DESC ";
		String countSql="SELECT "+ 
			      "count(1) "+ 
			    "FROM "+
			      "(SELECT "+ 
			        "su.dan_bao_ren, "+
			        "COUNT(1) AS num "+
			      "FROM "+
			        "shopping_user AS su "+ 
			        "RIGHT JOIN "+ 
			          "(SELECT "+ 
			            "sua.user_id "+ 
			          "FROM "+
			            "shopping_user_appclicknum AS sua "+
			         "WHERE sua.loginDate <= '" + beginTime + "'"+
			            "AND sua.loginDate >= '" + endTime + "') AS temp "+
			         " ON su.id = temp.user_id "+
			      "WHERE su.dan_bao_ren <> '' "+ 
			        "AND su.dan_bao_ren IS NOT NULL "+ 
			     " GROUP BY su.dan_bao_ren "+ 
			      "ORDER BY num DESC) AS temp2 "+
			    "WHERE num >= 5 ";
		List<?> count = commonService.executeNativeNamedQuery(countSql);
		int moneySum=0;
		int userNum=0;
		if (count.size()>0) {
			userNum = ((BigInteger)count.get(0)).intValue();
			int num=userNum%100==0?userNum/100:(userNum/100+1);
			for (int i = 0; i < num; i++) {
				List<Object[]> dynamics=commonService.executeNativeNamedQuery(sql);
				page++;
				for (Object[] obj : dynamics) {
					int dynamicNum=CommUtil.null2Int(obj[1]);
					if (dynamicNum>=5&&dynamicNum<10) {
						moneySum=moneySum+1;
					}else if (dynamicNum>=10&&dynamicNum<20) {
						moneySum=moneySum+5;
					}else if (dynamicNum>=20&&dynamicNum<30) {
						moneySum=moneySum+20;
					}else if (dynamicNum>=30&&dynamicNum<40) {
						moneySum=moneySum+50;
					}else if (dynamicNum>=40&&dynamicNum<50) {
						moneySum=moneySum+100;
					}else if (dynamicNum>=50) {
						moneySum=moneySum+200;
					}else {
						moneySum=moneySum+0;
					}
				}
			}
		}
		AppAdminData xianjiData=new AppAdminData();
		xianjiData.setSecondData(moneySum);
		xianjiData.setSortData(users);
		xianjiData.setFirstData(userNum);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppAdminData.class, "firstData,sortData,secondData"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,fourthData,fifthData,secondData"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, xianjiData, "查询成功", 0, filter);
	}
	
	@SuppressWarnings("all")
	private List<AppTransferData> geZhijiDatas(List<Object[]> query,String errand){
		String beginTime = CommUtil.getLastMonthFinalDay();
		String[] split = beginTime.split("-");
		String time = split[0] + split[1];
		List<AppTransferData> users=new ArrayList<AppTransferData>();
		String orderTime = ApiUtils.getFirstday_Lastday(CommUtil.formatDate(beginTime, "yyyy-MM-dd HH:mm:ss"), 0, 14);
		orderTime=ApiUtils.weeHours(orderTime, 0);
		int moneySum=0;
		int userNum=0;
		int payMoney=0;
		String user_sql="select so.id from shopping_orderform as so where so.addTime <= '"+beginTime+"' and so.addTime >= '" + orderTime +"'";
		for (Object[] obj : query) {
			AppTransferData userData=new AppTransferData();
			userData.setFirstData(obj[1]);
			User user = userService.getObjById(CommUtil.null2Long(obj[0]));
			userData.setSecondData(user);
			try {
				boolean is=false;
				long num = ApiUtils.acquisitionTimeSegment(CommUtil.formatShortDate(user.getLoginDate()),beginTime);
				if (num<=7) {
					is=true;
				}
				if (!is) {
					user_sql=user_sql+" and so.user_id = " +user.getId();
					List<?> orders = commonService.executeNativeNamedQuery(user_sql);
					if (orders.size()>0) {
						is=true;
					}
				}
				if (!is) {
					user_sql="select obj from AppClickNum as obj where obj.user.id = " + user.getId();
					List<AppClickNum> appClickNum = commonService.query(user_sql, null, -1, -1);
					if (appClickNum.size()>0) {
						Date loginDate = appClickNum.get(0).getLoginDate();
						if (loginDate!=null) {
							num = ApiUtils.acquisitionTimeSegment(CommUtil.formatShortDate(loginDate),beginTime);
							if (num<=7) {
								is=true;
							}
						}
					}
				}
				userData.setFourthData(is);
				int dynamicNum=((BigInteger)userData.getFirstData()).intValue();
				if (dynamicNum>=5&&dynamicNum<10) {
					userData.setFifthData("1");
				}else if (dynamicNum>=10&&dynamicNum<20) {
					userData.setFifthData("5");
				}else if (dynamicNum>=20&&dynamicNum<30) {
					userData.setFifthData("20");
				}else if (dynamicNum>=30&&dynamicNum<40) {
					userData.setFifthData("50");
				}else if (dynamicNum>=40&&dynamicNum<50) {
					userData.setFifthData("100");
				}else if (dynamicNum>=50) {
					userData.setFifthData("200");
				}else {
					userData.setFifthData("0");
				}
				if (!errand.equals("shot")) {
					users.add(userData);
				}		
				moneySum+=(CommUtil.null2Int(userData.getFifthData()));
				boolean distributionState = ApiUtils.distributionState(user, commonService, userService, "xianji");
				if ((!is&&errand.equals("shot"))||!distributionState) {
					//发送提示消息，不是动态会员，不发钱
					String msg=user.getUserName()+"战友，你好，因你最近处于非动态，衔级金停止发放，请尽快恢复动态。";
					CommUtil.send_messageToSpecifiedUser(user,msg,userService);
					PredepositLog predepositLog=new PredepositLog();
					predepositLog.setAddTime(new Date());
					predepositLog.setDeleteStatus(false);
					predepositLog.setPd_log_user(user);
					predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());
					predepositLog.setPd_log_amount(BigDecimal.valueOf(CommUtil.null2Double(userData.getFifthData())));
					predepositLog.setPd_op_type("增加");
					predepositLog.setPd_log_info(time+"衔级金,停止发放");
					if (!distributionState) {
						predepositLog.setPd_log_info(time+"衔级金,非领袖会员,停止发放");
					}
					predepositLog.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
				}
				if (is&&errand.equals("shot")) {
					//发衔级金
					userNum++;
					payMoney+=CommUtil.null2Int(userData.getFifthData());
					ApiUtils.distributeWages(user,CommUtil.null2Double(userData.getFifthData()), CommUtil.null2Double(userData.getFifthData()), time+"衔级金", userService, predepositLogService,"xianji",commonService);
				}
			} catch (Exception e) {
				e.printStackTrace();
				CommUtil.send_messageToSpecifiedUser(userService.getObjById(20717l),user.getId()+"衔级金发放异常",userService);
			}		
		}
		if (errand.equals("shot")) {
			AppTransferData zhijiMoney=new AppTransferData();
			zhijiMoney.setFifthData(moneySum);
			zhijiMoney.setFirstData(payMoney);
			zhijiMoney.setFourthData(userNum);
			users.add(zhijiMoney);
		}
		AppAdminData adminData=new AppAdminData();
		adminData.setSecondData(moneySum);
		adminData.setSortData(payMoney);
		return users;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**password  明文密码
	 *@description:数据实况：发放衔级金
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminPayXianjiMoney.htm", method = RequestMethod.POST )
	public void appAdminPayXianjiMoney(HttpServletRequest request,
			HttpServletResponse response,String userId,String password){
		boolean is_null = ApiUtils.is_null(userId,password);
		if (is_null) {
			ApiUtils.json(response, "", "参数不能为空！", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		if (user_id!=20717&&user_id!=1) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		int current_page=0;
		int pageSize=100;
		String beginTime = CommUtil.getLastMonthFinalDay();
		String[] split = beginTime.split("-");
		String time = split[0] + split[1];
		List<?> payStatus = commonService.query(" from OpenRecord as obj where openTime =" + time, null, -1, -1);
		if (payStatus.size()>0) {
			ApiUtils.json(response, "",beginTime+ "衔级金已经发放过一次！", 1);
			return;
		}
		String endTime = ApiUtils.getFirstday_Lastday(CommUtil.formatDate(beginTime, "yyyy-MM-dd HH:mm:ss"), 0, 6);
		endTime=ApiUtils.weeHours(endTime, 0);
		String sql="SELECT "+
			  "obj.id, "+
			  "temp3.num "+ 
			"FROM "+
			  "shopping_user AS obj "+
			  "RIGHT JOIN "+ 
			    "(SELECT "+ 
			      "temp2.dan_bao_ren,temp2.num "+ 
			    "FROM "+
			      "(SELECT "+ 
			        "su.dan_bao_ren, "+
			        "COUNT(1) AS num "+
			      "FROM "+
			        "shopping_user AS su "+ 
			        "RIGHT JOIN "+ 
			          "(SELECT "+ 
			            "sua.user_id "+ 
			          "FROM "+
			            "shopping_user_appclicknum AS sua "+
			         "WHERE sua.loginDate <= '" + beginTime + "'"+
			            "AND sua.loginDate >= '" + endTime + "') AS temp "+
			         " ON su.id = temp.user_id "+
			      "WHERE su.dan_bao_ren <> '' "+ 
			        "AND su.dan_bao_ren IS NOT NULL "+ 
			     " GROUP BY su.dan_bao_ren "+ 
			      "ORDER BY num DESC) AS temp2 "+
			    "WHERE num >= 5 "+
			    "LIMIT "+current_page*pageSize+", "+pageSize+") AS temp3 "+ 
			    "ON temp3.dan_bao_ren = obj.userName "+ 
			"ORDER BY temp3.num DESC ";
		
		String countSql="SELECT "+ 
			      "count(1) "+ 
			    "FROM "+
			      "(SELECT "+ 
			        "su.dan_bao_ren, "+
			        "COUNT(1) AS num "+
			      "FROM "+
			        "shopping_user AS su "+ 
			        "RIGHT JOIN "+ 
			          "(SELECT "+ 
			            "sua.user_id "+ 
			          "FROM "+
			            "shopping_user_appclicknum AS sua "+
			         "WHERE sua.loginDate <= '" + beginTime + "'"+
			            "AND sua.loginDate >= '" + endTime + "') AS temp "+
			         " ON su.id = temp.user_id "+
			      "WHERE su.dan_bao_ren <> '' "+ 
			        "AND su.dan_bao_ren IS NOT NULL "+ 
			     " GROUP BY su.dan_bao_ren "+ 
			      "ORDER BY num DESC) AS temp2 "+
			    "WHERE num >= 5 ";
		List<?> count = commonService.executeNativeNamedQuery(countSql);
		int userNum=0;
		int zhijiMoneySum=0;
		int payZhijiMoney=0;
		int payUserNum=0;
		if (count.size()>0) {
			userNum = ((BigInteger)count.get(0)).intValue();
			int num = userNum%100==0?userNum/100:(userNum/100+1);
			for (int i = 0; i < num; i++) {
				List<Object[]> dynamics=commonService.executeNativeNamedQuery(sql);
				List<AppTransferData> zhijiDatas = this.geZhijiDatas(dynamics, "shot");
				if (zhijiDatas.size()>0) {
					AppTransferData zhiji = zhijiDatas.get(0);
					zhiji.getFourthData();
					zhijiMoneySum+=CommUtil.null2Int(zhiji.getFifthData());
					payZhijiMoney+=CommUtil.null2Int(zhiji.getFirstData());
					payUserNum+=CommUtil.null2Int(zhiji.getFourthData());
				}
				current_page++;
			}
			commonService.executeNativeSQL("insert into ecm_openrecord (openTime,type) values('"
					+ time + "' ,'衔级金发放,总额："+payZhijiMoney+"')");
		}
		AppAdminData zhijiData=new AppAdminData();
		zhijiData.setFirstData(payUserNum);
		zhijiData.setSecondData(payZhijiMoney);
		zhijiData.setSortData(zhijiMoneySum);
		ApiUtils.json(response, zhijiData,split[0] +"年"+ split[1]+ "月衔级金成功发放！", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password  密文密码
	 *@description:管理员查看用户月盈利情况
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appAdminGetUserBillRank.htm", method = RequestMethod.POST)
	public void appAdminGetUserBillRank(HttpServletRequest request,HttpServletResponse response,String userId,String password,String beginTime,String currentPage) {		
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM");
		boolean is_null = ApiUtils.is_null(userId,password);
		if (is_null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		if ("".equals(CommUtil.null2String(beginTime))) {
			beginTime=CommUtil.getLastMonthFirstDay();
		}
		try {
			Date parse = df.parse(beginTime);
			beginTime = CommUtil.formatLongDate(parse);
		} catch (ParseException e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "时间参数错误", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		} 
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
	    Calendar lastDate = Calendar.getInstance();  
	    try {
			lastDate.setTime(sdf.parse(beginTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
        lastDate.add(Calendar.MONTH,1);//减一个月  
        lastDate.set(Calendar.DATE, 1);//把日期设置为当月第一天   
        beginTime=sdf.format(lastDate.getTime());  
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin =ApiUtils.getFirstday_Lastday_Month(beginTime, 0);
		String end = ApiUtils.getFirstday_Lastday_Month(beginTime, 1);
		String hql="select obj from UserMonthlyBill as obj where obj.addTime >= :begin and obj.addTime <= :end and obj.moneySum > 0 order by moneySum DESC";
		Map<String, Object> paramMap=new HashMap<String, Object>();
		paramMap.put("begin", CommUtil.formatDate(begin,"yyyy-MM-dd HH:mm:ss"));
		paramMap.put("end", CommUtil.formatDate(end,"yyyy-MM-dd HH:mm:ss"));
		List<UserMonthlyBill> bills = commonService.query(hql, paramMap, current_page*pageSize,pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserMonthlyBill.class, "chubeiMoney,daogouMoney,danbaoMoney,zhaoshangMoney,xianjiMoney,fenhongMoney,zengguMoney,moneySum,user"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response,bills,"获取用户月盈利排行成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员更改用户担保人
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminChangeUserReferee.htm", method = RequestMethod.POST )
	public void appAdminChangeUserReferee(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String appointUserId,String refereeId){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(appointUserId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "用户id错误！", 1);
			return;
		}
		Long referee_id = CommUtil.null2Long(refereeId);
		if (referee_id.longValue()==-1) {
			ApiUtils.json(response, "", "请输入要更改的担保人ID！", 1);
			return;
		}
		User user = userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "该用户不存在！", 1);
			return;
		}
		User refereeUser = userService.getObjById(referee_id);
		if (refereeUser==null) {
			ApiUtils.json(response, "", "担保用户不存在！", 1);
			return;
		}
		user.setDan_bao_ren(refereeUser.getUserName());
		boolean update = userService.update(user);
		if (update) {
			ApiUtils.json(response, "", "修改用户担保人成功。", 0);
			return;
		}else {
			ApiUtils.json(response, "", "操作失败！", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员查看任职记录
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminGetOfficeRecord.htm", method = RequestMethod.POST )
	public void appAdminGetOfficeRecord(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage,String seeUserId){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		} 
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Long see_UserId = CommUtil.null2Long(seeUserId);
		String add="";
		if (see_UserId!=-1) {
			add="where obj.myselfUser.id = " + seeUserId;
		}
		String hql="select obj from ZhiWeiRecoderEntity as obj "+ add +" order by obj.addTime desc";
		List<ZhiWeiRecoderEntity> zhiweiRecords = commonService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(ZhiWeiRecoderEntity.class, "msg,myselfUser,user,zhiwei"));
		objs.add(new FilterObj(User.class, "id,areaGradeOfUser,userName,zhiwei,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, zhiweiRecords, "获取职位操作记录成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员获取app首页欢迎图
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminGetAppWelcomeImgs.htm", method = RequestMethod.POST )
	public void appAdminGetAppWelcomeImgs(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		} 
		int current_page=0;
		int pageSize=6;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from AppWelcomeImg as obj order by obj.imgState DESC";
		List<AppWelcomeImg> appWelcomeImgs = commonService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppWelcomeImg.class, "id,imgState,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appWelcomeImgs, "获取APP欢迎图成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员设置app首页欢迎图
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminSetAppWelcomeImg.htm", method = RequestMethod.POST )
	public void appAdminSetAppWelcomeImg(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String imgId){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		Long id = CommUtil.null2Long(imgId);
		AppWelcomeImg appWelcomeImg = (AppWelcomeImg) commonService.getById("AppWelcomeImg", id.toString());
		String hql="select obj from AppWelcomeImg as obj where obj.imgState = 1";
		List<AppWelcomeImg> appWelcomeImgs = commonService.query(hql, null, -1, -1);
		for (AppWelcomeImg img : appWelcomeImgs) {
			img.setImgState("0");
			commonService.update(img);
		}
		if (appWelcomeImg!=null) {
			appWelcomeImg.setImgState("1");
			commonService.update(appWelcomeImg);
		}	
		ApiUtils.json(response, "", "更改APP欢迎图成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员获取app分享图
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminGetAppShareImgs.htm", method = RequestMethod.POST )
	public void appAdminGetAppShareImgs(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int current_page=0;
		int pageSize=6;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from AppShareImg as obj where obj.id <> 3 ORDER BY obj.imgState DESC,obj.imgOrder";
		List<AppShareImg> appShareImgs  = commonService.query(hql, null,current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppShareImg.class, "id,showPhoto,operatePhoto,imgState,imgOrder,headPortraitState,fontState,fontSize,fontColor,fontContent,fontUpRange,fontLeftRange,qRCodeUpRange,qRCodeLeftRange,qRCodeWidth,qRCodeHeight,headPortraitUpRange,headPortraitLeftRange,headPortraitWidth,headPortraitHeight"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appShareImgs, "获取APP分享图片成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员设置app分享图
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appAdminSetAppShareImgs.htm", method = RequestMethod.POST )
	public void appAdminSetAppShareImgs(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String imgIds){
		if (imgIds==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		String hql="select obj from AppShareImg as obj where obj.id <> 3 and obj.imgState = 1";
		List<AppShareImg> appShareImgs = commonService.query(hql, null, -1, -1);
		for (AppShareImg img : appShareImgs) {
			img.setImgState("0");
			img.setImgOrder(null);
			commonService.update(img);
		}
		String[] ids = imgIds.split(",");
		int i=1;
		for (String id : ids) {
			Long imgId = CommUtil.null2Long(id);
			if (imgId!=-1) {
				AppShareImg appShareImg= (AppShareImg) commonService.getById("AppShareImg", imgId.toString());
				if (appShareImg!=null) {
					appShareImg.setImgState("1");
					appShareImg.setImgOrder(i+"");
					commonService.update(appShareImg);
				}			
			}
			i++;
		}
		ApiUtils.json(response, "", "更改APP分享图成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员设置查看部门未任命人的权限
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminSetDepartmentPower.htm", method = RequestMethod.POST )
	public void appAdminSetDepartmentPower(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String appointUserId){
		Long tabUserId = CommUtil.null2Long(appointUserId);
		if (tabUserId==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		User user = userService.getObjById(tabUserId);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		DepartmentPower departmentPower = (DepartmentPower) commonService.getByWhere("DepartmentPower", "obj.user.id = " + appointUserId);
		if (departmentPower!=null) {
			ApiUtils.json(response, "", "用户已经拥有该权限", 1);
			return;
		}
		departmentPower=new DepartmentPower();
		departmentPower.setAddTime(new Date());
		departmentPower.setDeleteStatus(false);
		departmentPower.setUser(user);
		commonService.save(departmentPower);
		ApiUtils.json(response, "", "设置权限成功", 0);
		return;	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员获取拥有查看部门未任命人的权限人员列表
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminGetHaveDepartmentPowerList.htm", method = RequestMethod.POST )
	public void appAdminGetHaveDepartmentPowerList(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from DepartmentPower AS obj order by obj.addTime DESC";
		@SuppressWarnings("unchecked")
		List<DepartmentPower> departmentPowers = commonService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DepartmentPower.class, "id,addTime,user"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, departmentPowers, "获取列表成功", 0,filter);
		return;		
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员取消查看部门未任命人的权限
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminRemoveDepartmentPower.htm", method = RequestMethod.POST )
	public void appAdminRemoveDepartmentPower(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String appointUserId){
		Long tabUserId = CommUtil.null2Long(appointUserId);
		if (tabUserId==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		User user = userService.getObjById(tabUserId);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String sql="delete from shopping_departmentpower where user_id = " + user.getId().toString();
		commonService.executeNativeSQL(sql);
		ApiUtils.json(response, "", "取消权限成功", 0);
		return;	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员查看招商排行
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminGetRecommendSellerOrder.htm", method = RequestMethod.POST )
	public void appAdminGetRecommendSellerOrder(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage,String beginTime){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		if ("".equals(CommUtil.null2String(beginTime))) {
			beginTime=CommUtil.formatShortDate(new Date());
		}
		String begin =ApiUtils.weeHours(beginTime, 0);
		String end = ApiUtils.weeHours(beginTime, 1);
		String sql="SELECT "+
					  "temp5.id, "+
					  "temp5.userName, "+
					  "temp5.num, "+
					  "temp5.zhiweiName, "+
					  "temp5.bumenName, "+
					  "sz.name "+ 
					"FROM "+
					  "shopping_zhixian AS sz "+ 
					  "RIGHT JOIN "+ 
					    "(SELECT "+ 
					      "temp4.id, "+
					      "temp4.userName, "+
					      "temp4.num, "+
					      "temp4.zhiweiName, "+
					      "eb.name AS bumenName, "+
					      "temp4.zhixian_id "+ 
					    "FROM "+
					      "ecm_bumen AS eb "+ 
					      "RIGHT JOIN "+ 
					        "(SELECT "+ 
					          "temp3.id, "+
					          "temp3.userName, "+
					          "temp3.num, "+
					          "ez.name AS zhiweiName, "+
					          "temp3.bumen_id, "+
					          "temp3.zhixian_id "+ 
					        "FROM "+
					          "ecm_zhiwei AS ez "+ 
					          "RIGHT JOIN "+ 
					            "(SELECT "+ 
					              "obj.id, "+
					              "obj.userName, "+
					              "temp2.num, "+
					              "obj.zhiwei_id, "+
					              "obj.bumen_id, "+
					              "obj.zhixian_id "+ 
					            "FROM "+
					              "shopping_user AS obj "+ 
					              "RIGHT JOIN "+ 
					                "(SELECT "+ 
					                  "su.dan_bao_ren, "+
					                  "COUNT(1) AS num "+ 
					                "FROM "+
					                  "shopping_user AS su "+ 
					                  "RIGHT JOIN "+ 
					                    "(SELECT "+ 
					                      "ss.id "+ 
					                    "FROM "+
					                      "shopping_store AS ss "+ 
					                    "WHERE ss.addTime >= '" + begin + "' "+ 
					                      "AND ss.addTime <= '" + end + "') AS temp "+ 
					                    "ON temp.id = su.store_id "+
					                    "WHERE su.dan_bao_ren <> '' AND su.dan_bao_ren IS NOT NULL "+
					                "GROUP BY su.dan_bao_ren "+ 
					                "ORDER BY num DESC "+ 
					                "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp2 "+ 
					                "ON temp2.dan_bao_ren = obj.userName) AS temp3 "+ 
					            "ON ez.id = temp3.zhiwei_id) AS temp4 "+ 
					        "ON temp4.bumen_id = eb.id) AS temp5 "+ 
					    "ON temp5.zhixian_id = sz.id "+ 
					"ORDER BY temp5.num DESC";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, query, "获取招商排行成功", 0);
		return;	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取拓客系统网址
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appsetTokerWebsite.htm", method = RequestMethod.POST )
	public void appsetTokerWebsite(HttpServletRequest request,HttpServletResponse response){
		String tokerWebsite="";
		String sql="select ss.tokeUrl from shopping_sysconfig as ss where ss.id=1";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		if (query.size()>0) {
			tokerWebsite=(String) query.get(0);
		}
		ApiUtils.json(response, tokerWebsite, "获取拓客链接", 0);
		return;	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:部门招人排行
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGetBuMenInvitationRank.htm", method = RequestMethod.POST )
	public void appGetBuMenInvitationRank(HttpServletRequest request,HttpServletResponse response,String beginTime,
			String endTime,String userId,String password,String currentPage){
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
		if (user==null) {
			ApiUtils.json(response, "", "权限不足", 1);
			return;
		}
		Map<String, Object> data = getToolsData(beginTime, endTime, currentPage);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Integer current_page=(Integer) data.get("current_page");
		Long days=(Long) data.get("days");
		if (days>60) {
			ApiUtils.json(response, "", "查询时间不能大于60天", 1);
			return;
		}
		String bumenids=user.getBumen().getId()+"";
		String ids = ApiUtils.getSubclassBumenIds(user.getBumen().getId(), commonService);
		if (!ids.equals("")) {
			bumenids=bumenids+","+ids;
		}
		int pageSize = 20;
		String sql="SELECT "+
					  "temp3.userName, "+
					  "temp3.danbao, "+
					  "temp3.bumenName, "+
					  "sz.name  "+
					"FROM "+
					  "shopping_zhixian as sz "+
					  "RIGHT JOIN  "+
					    "(SELECT  "+
					      "temp2.userName, "+
					      "temp2.danbao, "+
					      "temp2.zhixian_id, "+
					      "b.name AS bumenName  "+
					    "FROM "+
					      "ecm_bumen AS b  "+
					      "RIGHT JOIN  "+
					        "(SELECT  "+
					          "su.id, "+
					          "su.userName, "+
					          "temp.danbao, "+
					          "su.bumen_id, "+
					          "su.zhixian_id  "+
					        "FROM "+
					          "shopping_user AS su  "+
					          "RIGHT JOIN  "+
					            "(SELECT  "+
					              "u.dan_bao_ren, "+
					             " COUNT(u.dan_bao_ren) AS danbao  "+
					            "FROM "+
					              "shopping_user AS u  "+
					           " WHERE u.addTime > '"+ begin+"' "+
					              "AND u.addTime < '"+end+"' "+
					            "GROUP BY u.dan_bao_ren  "+
					            ") AS temp "+ 
					            "ON temp.dan_bao_ren = su.userName where su.bumen_id in ("+ bumenids + ") ORDER BY danbao DESC " +
					            " LIMIT " + (current_page * pageSize) + "," + pageSize+") AS temp2 "+ 
					        "ON temp2.bumen_id = b.id) AS temp3 "+ 
					    "ON temp3.zhixian_id = sz.id order by temp3.danbao desc";
		List<?> list = commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, list, "success", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:测试用户余额是否正确
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appCheckUserInfo.htm" )
	public void appCheckUserInfo(HttpServletRequest request,HttpServletResponse response,String userId){
		if (CommUtil.null2Long(userId)==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = userService.getObjById(CommUtil.null2Long(userId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String hql="select obj from PredepositLog as obj where obj.pd_log_user.id="+user.getId()+" order by obj.id DESC";
		@SuppressWarnings("unchecked")
		List<PredepositLog> query = commonService.query(hql, null, 0, 3);
		if (query.size()>1) {
			double ava=0;
			double money=0;
			double sum=0;
			for (int i = 0; i < query.size(); i++) {
				if ((i+1)==query.size()) {				
					ava=query.get(i).getCurrent_price();
				}else {
					money=query.get(0).getCurrent_price();
					sum+=query.get(i).getPd_log_amount().doubleValue();
				}
			}
			if (CommUtil.formatDouble((ava+sum),2)!=CommUtil.formatDouble(money,2)) {
				CommUtil.send_messageToSpecifiedUser(userService.getObjById(149900l),CommUtil.formatLongDate(new Date())+"--"+user.getId()+"余额异常,操作前余额："+ava+",操作余额："+sum+",操作后余额："+money,userService);
				return;
			}
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:部门个人盈利排行
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGetBuMenPersonalProfitRank.htm", method = RequestMethod.POST )
	public void appGetBuMenPersonalProfitRank(HttpServletRequest request,HttpServletResponse response,String beginTime,
			String endTime,String userId,String password,String currentPage){
		if ("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "请传用户密码", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = ApiUtils.erifySeeDataPowerUser(userId, password, userService, 10,commonService);
		if (user==null) {
			ApiUtils.json(response, "", "权限不足", 1);
			return;
		}
		Map<String, Object> data = getToolsData(beginTime, endTime, currentPage);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Integer current_page=(Integer) data.get("current_page");
		Long days=(Long) data.get("days");
		if (days>60) {
			ApiUtils.json(response, "", "查询时间不能大于60天", 1);
			return;
		}
		int pageSize = 20;
		String bumenids=user.getBumen().getId()+"";
		String ids = ApiUtils.getSubclassBumenIds(user.getBumen().getId(), commonService);
		if (!ids.equals("")) {
			bumenids=bumenids+","+ids;
		}
		String sql="SELECT "+
					  "temp3.id, "+
					  "temp3.userName, "+
					  "temp3.bName, "+
					  "zw.name, "+
					  "temp3.daogou "+ 
					"FROM "+
					  "ecm_zhiwei AS zw  "+
					  "RIGHT JOIN "+
					    "(SELECT "+
					      "temp2.id, "+
					      "temp2.userName, "+
					      "bm.name AS bName, "+
					      "temp2.zhiwei_id, "+
					      "temp2.daogou "+
					    "FROM "+
					      "ecm_bumen AS bm "+ 
					      "RIGHT JOIN "+
					        "(SELECT "+
					          "u.id, "+
					          "u.userName, "+
					          "u.zhiwei_id, "+
					          "ROUND(SUM(temp.daogou), 2) AS daogou, "+
					          "u.bumen_id "+ 
					        "FROM "+
					          "shopping_user AS u "+ 
					          "RIGHT JOIN "+
					            "(SELECT "+
					              "o.daogou_get_price AS daogou, "+
					              "user_id "+
					            "FROM "+
					              "shopping_orderform AS o "+ 
					            "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
					              "AND o.payTimes >= '" + begin + "'"+ 
					              "AND o.payTimes <= '" + end + "' ) AS temp "+ 
					            "ON temp.user_id = u.id "+ 
					        "WHERE u.bumen_id IN (" + bumenids + ") "+
					        "GROUP BY u.id "+
					        "ORDER BY daogou DESC "+ 
					        "LIMIT " + pageSize*current_page + "," + pageSize+") AS temp2 "+ 
					        "ON bm.id = temp2.bumen_id) AS temp3 "+ 
					    "ON zw.id = temp3.zhiwei_id";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, query, "success", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:修改会员等级所需积分
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appModifyUserRankIntegral.htm", method = RequestMethod.POST )
	public void appModifyUserRankIntegral(HttpServletRequest request,HttpServletResponse response,
			String userId,String password,String integralNum,String userRankId){
		if (ApiUtils.is_null(userId,password,integralNum,userRankId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		UserRank userRank=(UserRank) commonService.getById("UserRank", userRankId);
		if (userRank==null) {
			ApiUtils.json(response, "", "该用户等级不存在", 1);
			return;
		}
		userRank.setIntegralNum(CommUtil.null2Int(integralNum));
		commonService.update(userRank);
		ApiUtils.json(response, "", "修改会员等级成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:转到上传分享图和欢迎图页面
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/uploadAppShareAndWelcome.htm")
	public ModelAndView testUserRank(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mv = new JModelAndView("upload_appImg.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:uploadType:share 分享图；welcome
	 *@description:管理员上传app分享图，首页欢迎图
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminUploadImgs.htm", method = RequestMethod.POST )
	public ModelAndView appAdminUploadImgs(HttpServletRequest request,
			HttpServletResponse response,String uploadType,AppShareImg appShareImg){
		boolean is_null = ApiUtils.is_null(uploadType);
		ModelAndView mv = new JModelAndView("error.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request,
				response);
		
		mv.addObject("url", CommUtil.getURL(request) + "/uploadAppShareAndWelcome.htm");
		if (is_null) {
			mv.addObject("op_title", "参数缺失");
			return mv;
		}
		Accessory showPhoto=null;
		Accessory operatePhoto=null;
        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        @SuppressWarnings("unchecked")
		Map<String, MultipartFile> fileMap = mRequest.getFileMap();
        Iterator<Map.Entry<String, MultipartFile>> it = fileMap.entrySet().iterator();
        while(it.hasNext()){
             Map.Entry<String, MultipartFile> entry = it.next();
             String key = entry.getKey();
             MultipartFile file = entry.getValue();
             String fileName=file.getOriginalFilename();//获取文件名加后缀
             if(fileName!=null&&fileName!=""){
                 String path = SystemResPath.imgUploadUrl; //文件存储位置
                 String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());//文件后缀
                 if (!".png".equals(fileF)&&!".jpg".equals(fileF)&&!".jpeg".equals(fileF)) {
                	mv.addObject("op_title", "文件格式错误");
					return mv;
				}
                fileName=new Date().getTime()+"_"+new Random().nextInt(1000)+"_"+key+fileF;//新的文件名
                this.uploadFile(file, fileName, path + "\\upload\\appImg");
                 
                Accessory photo=new Accessory();
 				photo.setAddTime(new Date());
 				photo.setName(fileName);
 				photo.setExt(fileF);
 				photo.setPath("upload/appImg");
 				this.accessoryService.save(photo);
 				if (key.equals("show")) {
 					showPhoto=photo;
				}else if (key.equals("operate")) {
					operatePhoto=photo;
				}
             }  
        }
		if (uploadType.equals("share")&&showPhoto!=null&&operatePhoto!=null) {
			appShareImg.setImgState("0");
			appShareImg.setAddTime(new Date());
			appShareImg.setDeleteStatus(false);
			if (ApiUtils.is_null(appShareImg.getqRCodeHeight()+"",appShareImg.getqRCodeLeftRange(),appShareImg.getqRCodeUpRange(),appShareImg.getqRCodeWidth()+"")) {
				mv.addObject("op_title", "二维码参数缺失");
				return mv;
			}
			appShareImg.setFontState("1");
			if (ApiUtils.is_null(appShareImg.getFontColor(),appShareImg.getFontContent(),appShareImg.getFontLeftRange(),appShareImg.getFontSize(),appShareImg.getFontUpRange())) {
				appShareImg.setFontState("0");
			}
			appShareImg.setHeadPortraitState("1");
			if (ApiUtils.is_null(appShareImg.getHeadPortraitHeight()+"",appShareImg.getHeadPortraitLeftRange(),appShareImg.getHeadPortraitUpRange(),appShareImg.getHeadPortraitWidth()+"")) {
				appShareImg.setHeadPortraitState("0");
			}
			appShareImg.setShowPhoto(showPhoto);
			appShareImg.setOperatePhoto(operatePhoto);
			commonService.save(appShareImg);
			mv = new JModelAndView("success.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
			mv.addObject("op_title", "保存APP分享图成功");
			return mv;
		}else if(uploadType.equals("welcome")&&showPhoto!=null){
			AppWelcomeImg awi=new AppWelcomeImg();
			awi.setAddTime(new Date());
			awi.setDeleteStatus(false);
			awi.setImgState("0");
			awi.setPhoto(showPhoto);
			commonService.save(awi);
			mv = new JModelAndView("success.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
			mv.addObject("op_title", "保存首页欢迎图成功");
			return mv;
		}
		mv.addObject("op_title", "上传文件不能为空");
		return mv;
	}
	private void uploadFile(MultipartFile file,String fileName,String path){
         //先判断文件是否存在
         File file1 =new File(path); 
         //如果文件夹不存在则创建    
         if(!file1 .exists()  && !file1 .isDirectory()){
             file1 .mkdir();  
         }
         File targetFile = new File(file1, fileName);
         try {
             file.transferTo(targetFile);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }  
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员设置App首页默认轮播图与顺序位图
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appAdminSetAppHomeData.htm", method = RequestMethod.POST)
	public void appAdminSetAppHomeData(HttpServletRequest request,HttpServletResponse response,String userId,String password,String id,String price,String defaultGoodsId,String type,String isSync){
		boolean is_null = ApiUtils.is_null(id,type);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		Goods goods = null;
		if (CommUtil.null2Long(defaultGoodsId)!=-1) {
			goods = this.goodsService.getObjById(CommUtil.null2Long(defaultGoodsId));
			if (goods==null) {
				ApiUtils.json(response, "", "商品不存在", 1);
				return;
			}
			if (goods.getGoods_status()!=0||goods.getGoods_store().getStore_status()!=2||goods.getGoods_inventory()<=0) {
				ApiUtils.json(response, "", "该商品已下架或库存不足，请上传其他商品", 1);
				return;
			}
		}
		double pc = CommUtil.null2Double(price);
		if (type.equals("banner")) {
			AppHomePageEntity ahpe = (AppHomePageEntity) this.commonService.getById("AppHomePageEntity", CommUtil.null2Long(id)+"");
			if (ahpe!=null) {
				if (goods!=null) {
					boolean isHave = AreaPartnerUtils.judgeIsRepetition(goods.getId().toString(), "AppHomePageCommonPosition", commonService);
					if(isHave){
						ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
						return;
					}
					ahpe.setDefaultGoods(goods);
				}
				if (pc!=0) {
					ahpe.setBanner_price(pc);
				}
				this.commonService.update(ahpe);
				ApiUtils.json(response, "", "设置轮播图成功", 0);
				if (CommUtil.null2String(isSync).equals("yes")) {
					ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appUpdateAreaData.htm?id=" + id +"&type=banner&price=" + price + "&defaultGoodsId="+defaultGoodsId, "GET");
				}
				return;
			}else {
				ApiUtils.json(response, "", "轮播图不存在", 1);
				return;
			}
		}else {
			AppHomePageCommonPosition ahpc = (AppHomePageCommonPosition) this.commonService.getById("AppHomePageCommonPosition", CommUtil.null2Long(id)+"");
			if (ahpc!=null) {
				if (goods!=null) {
					boolean isHave = AreaPartnerUtils.judgeIsRepetition(goods.getId().toString(), "AppHomePageCommonPosition", commonService);
					if(isHave){
						ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
						return;
					}
					ahpc.setDefaultGoods(goods);
				}
				if (pc!=0) {
					ahpc.setCommonPosition_price(pc);
				}
				if (CommUtil.null2String(isSync).equals("yes")) {
					ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appUpdateAreaData.htm?id=" + id +"&type=common&price=" + price + "&defaultGoodsId="+defaultGoodsId, "GET");
				}
				this.commonService.update(ahpc);
				ApiUtils.json(response, "", "设置顺序位成功", 0);
				return;
			}else {
				ApiUtils.json(response, "", "顺序位不存在", 1);
				return;
			}
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员设置App首页付费类型及最大支付时间
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appAdminSetAppHomeConfig.htm", method = RequestMethod.POST)
	public void appAdminSetAppHomeConfig(HttpServletRequest request,HttpServletResponse response,String userId,String password,
			String isSync,String payType,String maxPayNum,String isOpen){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		AppHomePageSwitchEntity ahpse = (AppHomePageSwitchEntity) this.commonService.getById("AppHomePageSwitchEntity", "1");
		int maxPay = CommUtil.null2Int(maxPayNum);
		StringBuffer url = new StringBuffer();
		if (maxPay!=0) {
			url.append("maxPayNum=").append(maxPay).append("&");
			ahpse.setMaxPayNum(maxPay);
		}
		if (!CommUtil.null2String(isOpen).equals("")) {
			url.append("isOpen=").append(isOpen).append("&");
			ahpse.setIs_turnOn(CommUtil.null2Boolean(isOpen));
		}
		if (!CommUtil.null2String(payType).equals("")) {
			url.append("payType=").append(payType).append("&");
			if (payType.equals("day")) {
				ahpse.setPayType("元/天");
			}else if(payType.equals("hour")){
				ahpse.setPayType("元/小时");
			}
		}
		this.commonService.update(ahpse);
		if (CommUtil.null2String(isSync).equals("yes")) {
			ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appSyncAreaPartnerSetConfig.htm?" + url.toString(), "GET");
		}
		ApiUtils.json(response, "", "修改配置成功", 0);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:上传区域合伙人PPT
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title="上传燎原计划PPT", value="/admin/appUploadAreaPartnerPPT.htm*", rtype="admin", rname="上传设置", rcode="admin_set_image", rgroup="设置")
	@RequestMapping(value = "/admin/appUploadAreaPartnerPPT.htm", method = RequestMethod.POST)
	public void appUploadAreaPartnerPPT(HttpServletRequest request,
			HttpServletResponse response) {
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        @SuppressWarnings("unchecked")
		Map<String, MultipartFile> fileMap = mRequest.getFileMap();
        Iterator<Map.Entry<String, MultipartFile>> it = fileMap.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<String, MultipartFile> entry = it.next();
            MultipartFile file = entry.getValue();
            String fileName=file.getOriginalFilename();//获取文件名加后缀
            if(fileName!=null&&fileName!=""){
                String path = SystemResPath.imgUploadUrl; //文件存储位置
                String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());//文件后缀
                if (!fileF.equals(".ppt")&&!fileF.equals(".PPT")) {
            		ApiUtils.json(response, "", "请选择PPT文件上传", 1);
             		return;
				}
               fileName="第一商城燎原计划"+fileF;//新的文件名
               this.uploadFile(file, fileName, path + "\\upload\\ppt");
            }
            String hql = "select obj from AccessoryApi as obj where obj.name like '%燎原%'";
    		@SuppressWarnings("unchecked")
    		List<AccessoryApi> accessoryApis = this.commonService.query(hql, null, -1, -1);
    		if (accessoryApis.size()<=0) {
    			Accessory accessory =new Accessory();
    			accessory.setAddTime(new Date());
    			accessory.setDeleteStatus(false);
    			accessory.setExt(".ppt");
    			accessory.setName(fileName);
    			accessory.setPath("upload/ppt");
    			boolean save = this.accessoryService.save(accessory);
    			if (!save) {
		            ApiUtils.json(response, "", "上传失败，请重新上传", 1);
		    		return;
				}
			}
    		ApiUtils.json(response, "", "上传成功", 0);
    		return;
		}
        ApiUtils.json(response, "", "请选择上传文件", 1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:转到上传PPT页面
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title="燎原计划", value="/admin/appUploadPPT.htm*", rtype="admin", rname="上传设置", rcode="admin_set_image", rgroup="设置")
	@RequestMapping(value="/admin/appUploadPPT.htm")
	public ModelAndView appUploadPPT(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mv = new JModelAndView("admin/upload_ppt.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:type:jituan 集团总指挥;  bumen 部门执行总裁
	 *@description:获取战区总指挥，部门执行总裁
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGetHighPositionUser.htm",method = RequestMethod.POST)
	public void appGetHighPositionUser(HttpServletRequest request,HttpServletResponse response,String userId,String password,String type){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		String userType = CommUtil.null2String(type);
		String hql = "select obj from User as obj";
		if ("jituan".equals(userType)) {
			hql = hql + " where obj.zhiwei.id = 300 order by obj.bumen.id";
		}else if ("bumen".equals(userType)) {
			hql = hql + " where obj.zhiwei.id = 17";
		}else {
			ApiUtils.json(response, "", "请选择会员类型", 1);
			return;
		}
		List<User> users = this.userService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance,userRank"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(UserRank.class, "userRankName"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, users, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password:明文密码
	 *@description:给战区总指挥，部门执行总裁发放积分
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGrantAwardUser.htm",method = RequestMethod.POST)
	public void appGrantAwardUser(HttpServletRequest request,HttpServletResponse response,String userIds,String type,String userId,String password,String explain,String money){
		long user_id = CommUtil.null2Long(userId).longValue();
		if (user_id!=20717&&user_id!=1) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		double m = CommUtil.null2Double(money);
		if (m<=0) {
			ApiUtils.json(response, "", "发放奖励金额不能小于零元", 1);
			return;
		}
		List<User> users = new ArrayList<>();
		String userType = CommUtil.null2String(type);
		long zhiweiId = 0l;
		if ("jituan".equals(userType)) {
			zhiweiId = 300l;
		}else if ("bumen".equals(userType)) {
			zhiweiId = 17l;
		}else {
			ApiUtils.json(response, "", "请选择会员类型", 1);
			return;
		}
		if (!CommUtil.null2String(userIds).equals("")) {//不为空则只给传过来的id发放奖励，否则给所有该等级的人发放
			String[] ids = userIds.split(",");
			for (String id : ids) {
				User u = this.userService.getObjById(CommUtil.null2Long(id));
				if (u!=null&&u.getZhiwei().getId()==zhiweiId) {
					users.add(u);
				}
			}
		}else {
			String hql = "select obj from User as obj where obj.zhiwei.id = " + zhiweiId;
			users = this.userService.query(hql, null, -1, -1);
		}
		if (users.size()<=0) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		double consumption = BigDecimalUtil.mul(m, users.size()).doubleValue();
		AllocateWagesUtils.allocateMoneyToUser(
				user.getId() + "", -consumption, CommUtil.formatLongDate(new Date()) + " 奖励," + explain, "",
				predepositLogService, userService, commonService, 0);
		if ("".equals(CommUtil.null2String(explain))) {
			explain = "收到" + user.getUserName() + "积分转赠";
		}
		for (User u : users) {
			AllocateWagesUtils.allocateMoneyToUser(
					u.getId() + "", m, explain, "",
					predepositLogService, userService, commonService, 1);
		}
		ApiUtils.json(response, "", "奖励发放完毕", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password:明文密码
	 *@description:给战区总指挥，部门执行总裁发放积分
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGrantAwardLogin.htm")
	public ModelAndView appGrantAwardLogin(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mv = new JModelAndView("/user/login/grantLogin.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:uId 操作用户的id
	 *@description:添加APP可以查看数据实况的用户
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/addAppSeeDataUser.htm",method=RequestMethod.POST)
	public void addAppSeeDataUser(HttpServletRequest request,HttpServletResponse response,String userId,String password,String uId){
		Long user_id = CommUtil.null2Long(uId);
		if (user_id==-1) {
			ApiUtils.json(response, "", "操作用户不存在", 1);
			return;
		}
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String hql = "select obj from AppSeeDataPower as obj where obj.deleteStatus = false and obj.user.id = " + user.getId();
		List<?> users = commonService.query(hql, null, -1, -1);
		if (users.size()>0) {
			ApiUtils.json(response, "", "用户已经拥有该权限", 1);
			return;
		}
		AppSeeDataPower asdp = new AppSeeDataPower();
		asdp.setAddTime(new Date());
		asdp.setDeleteStatus(false);
		asdp.setUser(user);
		this.commonService.save(asdp);
		List<User> list = this.getAppSeeDataPowers(0, 20);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DepartmentPower.class, "id,addTime,user"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "添加成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取可以查看APP数据实况的用户
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/getAppSeeDataUser.htm",method=RequestMethod.POST)
	public void getAppSeeDataUser(HttpServletRequest request,HttpServletResponse response,String userId,String password,String currentPage){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<User> list = this.getAppSeeDataPowers(current_page, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DepartmentPower.class, "id,addTime,user"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "success", 0,filter);
		return;		
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:删除APP查看数据实况的用户
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/deleteAppSeeDataUser.htm",method=RequestMethod.POST)
	public void deleteAppSeeDataUser(HttpServletRequest request,HttpServletResponse response,String userId,String password,String uId){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		User user = this.userService.getObjById(CommUtil.null2Long(uId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String hql = "select obj from AppSeeDataPower as obj where obj.deleteStatus = false and obj.user.id = " + user.getId();
		List<?> users = commonService.query(hql, null, -1, -1);
		if (users.size()<=0) {
			ApiUtils.json(response, "", "用户没有该权限", 1);
			return;
		}
		String sql="delete from shopping_app_seedatapower where user_id = " + user.getId().toString();
		commonService.executeNativeSQL(sql);
		List<User> list = this.getAppSeeDataPowers(0, 20);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DepartmentPower.class, "id,addTime,user"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "取消权限成功", 0,filter);
		return;
	}
	private List<User> getAppSeeDataPowers(int current_page,int pageSize){
		String hql = "select obj from AppSeeDataPower as asdp left join asdp.user as obj order by asdp.addTime desc";
		@SuppressWarnings("unchecked")
		List<User> list = this.commonService.query(hql, null, current_page*pageSize, pageSize);
		return list;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:管理员更改用户会员等级
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAdminUpdateUserRank.htm",method=RequestMethod.POST)
	public void appAdminUpdateUserRank(HttpServletRequest request,HttpServletResponse response,String userId,String password,String uId,String userRankId){
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限！", 1);
			return;
		}
		User user = this.userService.getObjById(CommUtil.null2Long(uId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		UserRank userRank=(UserRank) commonService.getById("UserRank", userRankId);
		if (userRank==null) {
			ApiUtils.json(response, "", "该用户等级不存在", 1);
			return;
		}
		user.setUserRank(userRank);
		boolean update = this.userService.update(user);
		if (update) {
			ApiUtils.json(response, "", "修改成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "修改失败，请稍后重试", 1);
			return;
		}
	}
}