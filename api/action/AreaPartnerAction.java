package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mysql.fabric.xmlrpc.base.Data;
import com.shopping.api.domain.AccessoryApi;
import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.rank.UserRankName;
import com.shopping.api.domain.regionPartner.AreaAppHomePayTemporary;
import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.domain.regionPartner.AreaHomePageConfig;
import com.shopping.api.domain.regionPartner.AreaPartnerEntity;
import com.shopping.api.domain.regionPartner.AreaPartnerPayRecord;
import com.shopping.api.domain.regionPartner.AreaSiteRankConfig;
import com.shopping.api.domain.regionPartner.InvitePartnerRewardRatio;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.UserTemp;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.domain.IdEntity;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AreaPartnerAction {
	@Autowired
	@Qualifier("areaSiteRankConfigServiceImpl")
	private IPartnerFunctionService<AreaSiteRankConfig> areaSiteRankConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	@Qualifier("areaPartnerPayRecordServiceImpl")
	private IPartnerFunctionService<AreaPartnerPayRecord> areaPartnerPayRecordService;
	@Autowired
	@Qualifier("areaPartnerEntityServiceImpl")
	private IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	@Qualifier("areaHomePageConfigServiceImpl")
	private IPartnerFunctionService<AreaHomePageConfig> areaHomePageConfigService;
	@Autowired
	@Qualifier("areaCommonpositionServiceImpl")
	private IPartnerFunctionService<AreaCommonposition> areaCommonpositionService;
	@Autowired
	@Qualifier("areaBannerpositionServiceImpl")
	private IPartnerFunctionService<AreaBannerposition> areaBannerpositionService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	@Qualifier("areaAppHomePayTemporaryServiceImpl")
	private IPartnerFunctionService<AreaAppHomePayTemporary> areaAppHomePayTemporaryService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:查询区域合伙人权益以及价格
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaPartnerRank.htm", method = RequestMethod.POST)
	public void appGetAreaPartnerRank(HttpServletRequest request,
			HttpServletResponse response, String userId, String password) {
		String hql = "select obj from AreaSiteRankConfig as obj where obj.deleteStatus = false order by obj.id ASC";
		if (!ApiUtils.is_null(userId, password)) {
			boolean admin = ApiUtils
					.isAdmin(userId, password, this.userService);
			if (admin) {
				hql = "select obj from AreaSiteRankConfig as obj order by obj.id ASC";
			}
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				AreaSiteRankConfig.class,
				"id,deleteStatus,areaRankName,areaExplain,areaRank,openRequiredMoney,lengthOfTime"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, asrcs, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:管理员修改区域合伙人权益表
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAdminUpdateAreaPartnerRank.htm", method = RequestMethod.POST)
	public void appAdminUpdateAreaPartnerRank(HttpServletRequest request,
			HttpServletResponse response, AreaSiteRankConfig asrc,
			String userId, String password, String openStatus) {
		boolean admin = ApiUtils.isAdmin(userId, password, this.userService);
		if (!admin) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		AreaSiteRankConfig areaSiteRankConfig = this.areaSiteRankConfigService
				.getObjById(CommUtil.null2Long(asrc.getId()));
		if (areaSiteRankConfig == null) {
			ApiUtils.json(response, "", "该配置不存在", 1);
			return;
		}
		if (!CommUtil.null2String(openStatus).equals("")) {
			areaSiteRankConfig.setDeleteStatus(CommUtil
					.null2Boolean(openStatus));
		}
		String areaExplain = CommUtil.null2String(asrc.getAreaExplain());
		if (!areaExplain.equals("")) {
			areaSiteRankConfig.setAreaExplain(areaExplain);
		}
		String areaRankName = CommUtil.null2String(asrc.getAreaRankName());
		if (!areaRankName.equals("")) {
			areaSiteRankConfig.setAreaRankName(areaRankName);
		}
		double openRequiredMoney = CommUtil.null2Double(asrc
				.getOpenRequiredMoney());
		if (openRequiredMoney != 0) {
			areaSiteRankConfig.setOpenRequiredMoney(openRequiredMoney);
		}
		Integer lengthOfTime = CommUtil.null2Int(asrc.getLengthOfTime());
		if (lengthOfTime != 0) {
			areaSiteRankConfig.setLengthOfTime(lengthOfTime);
		}
		boolean update = this.areaSiteRankConfigService
				.update(areaSiteRankConfig);
		if (update) {
			ApiUtils.json(response, "", "修改成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "修改失败", 1);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:开通区域合伙人，查询用户选择区域是否合法，若不合法则提醒用户，否则创建对应订单
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appCreateAreaPartnerOrder.htm", method = RequestMethod.POST)
	public void appCreateAreaPartnerOrder(HttpServletRequest request,
			HttpServletResponse response, String partnerId, String areaId,
			String userId, String password, String bumenId) {
		boolean is_null = ApiUtils.is_null(partnerId, areaId, userId, password,
				bumenId);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		Long pid = CommUtil.null2Long(partnerId);
		if (pid == -1) {
			ApiUtils.json(response, "", "合伙人参数错误", 1);
			return;
		}
		BuMen bumen = (BuMen) this.commonService.getById("BuMen", CommUtil
				.null2Long(bumenId).toString());
		if (bumen == null) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		AreaSiteRankConfig asrc = this.areaSiteRankConfigService
				.getObjById(pid);
		if (asrc == null || asrc.isDeleteStatus() == true) {
			ApiUtils.json(response, "", "合伙人类型不存在", 1);
			return;
		}
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		AreaGradeOfUser area = (AreaGradeOfUser) this.commonService.getById(
				"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
		if (area == null) {
			ApiUtils.json(response, "", "选择的地区不存在", 1);
			return;
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		if (area.getLevel() != asrc.getAreaRank()) {
			ApiUtils.json(response, "", "选择地区级别错误", 1);
			return;
		}
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select obj from AreaPartnerEntity as obj where obj.buMen.id = "
				+ bumenId
				+ " and obj.deleteStatus = false and obj.expireTime > '"
				+ time
				+ "' and obj.area.id = " + area.getId();
		List<AreaPartnerEntity> aPartnerEntities = this.areaPartnerEntityService
				.query(hql, null, -1, -1);
		if (aPartnerEntities.size() > 0) {
			ApiUtils.json(response, "", "该区域已被抢购，赶快抢购其他区域站点吧", 1);
			return;
		}
		AreaPartnerPayRecord areaPartnerPayRecord = new AreaPartnerPayRecord(
				new Date(), ApiUtils.integralOrderNum(user.getId()) + "", 10,
				asrc.getOpenRequiredMoney(), user, area, bumen, asrc);
		areaPartnerPayRecord.setDeleteStatus(false);
		boolean save = this.areaPartnerPayRecordService
				.save(areaPartnerPayRecord);
		if (save) {
			ApiUtils.json(response, areaPartnerPayRecord.getOrderNum(),
					"success", 0);
			return;
		} else {
			ApiUtils.json(response, "", "提交失败，请重试", 1);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:检测区域站点是否被购买
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appInspectAreaBuyStatus.htm", method = RequestMethod.POST)
	public void appCreateAreaPartnerOrder(HttpServletRequest request,
			HttpServletResponse response, String areaId, String bumenId) {
		AreaGradeOfUser area = (AreaGradeOfUser) this.commonService.getById(
				"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
		if (area == null) {
			ApiUtils.json(response, "", "选择的地区不存在", 1);
			return;
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		BuMen bumen = (BuMen) this.commonService.getById("BuMen", CommUtil
				.null2Long(bumenId).toString());
		if (bumen == null) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		AreaPartnerUtils.saveAreaAppHome(null, null, area, bumen,
				commonService, areaPartnerEntityService,
				areaHomePageConfigService, areaCommonpositionService,
				areaBannerpositionService, areaSiteRankConfigService);
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select obj from AreaPartnerEntity as obj where obj.user is not null and obj.deleteStatus = false and obj.expireTime > '"
				+ time
				+ "' and obj.area.id = "
				+ area.getId()
				+ " and obj.buMen.id = " + bumen.getId();
		List<AreaPartnerEntity> aPartnerEntities = this.areaPartnerEntityService
				.query(hql, null, -1, -1);
		if (aPartnerEntities.size() > 0) {
			ApiUtils.json(response, "", "该区域已被抢购，赶快抢购其他区域站点吧", 1);
			return;
		} else {
			ApiUtils.json(response, "", "该区域可以购买,赶快抢购吧", 0);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:payType:balance 积分支付； alipay 支付宝支付；weixin 微信支付
	 * @description:购买区域合伙人，积分支付,微信支付,支付宝支付,如果为积分支付 需要password明文密码
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appPaymentAreaPartner.htm", method = RequestMethod.POST)
	public void appPaymentAreaPartner(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String orderNum, String payType) {
		boolean is_null = ApiUtils.is_null(userId, orderNum, payType);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue() == -1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user == null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String hql = "select obj from AreaPartnerPayRecord as obj where obj.orderNum = "
				+ orderNum;
		List<AreaPartnerPayRecord> apprs = this.areaPartnerPayRecordService
				.query(hql, null, -1, -1);
		if (apprs.size() <= 0) {
			ApiUtils.json(response, "", "订单不存在，请重新购买", 1);
			return;
		}
		AreaPartnerPayRecord areaPartnerPayRecord = apprs.get(0);
		if (areaPartnerPayRecord.getPayStatus() == 20) {
			ApiUtils.json(response, "", "该订单已被支付", 1);
			return;
		}
		hql = "select obj from AreaPartnerEntity as obj where obj.user is not null and obj.deleteStatus = false "
				+ " and obj.area.id = "
				+ areaPartnerPayRecord.getAreaGradeOfUser().getId()
				+ " and obj.buMen.id = "
				+ areaPartnerPayRecord.getBuMen().getId();
		List<AreaPartnerEntity> aPartnerEntities = this.areaPartnerEntityService
				.query(hql, null, -1, -1);
		if (aPartnerEntities.size() > 0
				&& aPartnerEntities.get(0).getUser() != null) {
			ApiUtils.json(response, "", "该商区已被抢购，请选择其他商区", 1);
			return;
		}
		if (payType.equals("balance")) {
			String psw = user.getPassword();
			String lowerCase = Md5Encrypt.md5(password).toLowerCase();
			if (!psw.equals(lowerCase)) {
				ApiUtils.json(response, "", "密码错误！", 1);
				return;
			}
			double userBalance = user.getAvailableBalance().doubleValue();
			Double openRequiredMoney = areaPartnerPayRecord
					.getAreaSiteRankConfig().getOpenRequiredMoney();
			if (user.getFreezeBlance().intValue() == 1) {
				if (userBalance - openRequiredMoney - 1000 < 0) {
					ApiUtils.json(response, "", "余额不足，您有部分余额处于锁定状态", 1);
					return;
				}
			} else {
				if (userBalance - openRequiredMoney < 0) {
					ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 1);
					return;
				}
			}
			boolean status = AllocateWagesUtils.allocateMoneyToUser(
					user.getId() + "", -openRequiredMoney, "购买"
							+ areaPartnerPayRecord.getAreaGradeOfUser()
									.getName() + "区域站点支出", "",
					predepositLogService, userService, commonService, 0);
			if (status) {
				AreaPartnerUtils.distributionOrderAmount(areaPartnerPayRecord, commonService, predepositLogService, userService);
				areaPartnerPayRecord.setPayStatus(20);
				areaPartnerPayRecord.setRewardStatus(false);
				areaPartnerPayRecord.setPayTime(new Date());
				areaPartnerPayRecord.setPayType(payType);
				boolean update = this.areaPartnerPayRecordService
						.update(areaPartnerPayRecord);
				if (update) {
					AreaPartnerUtils.saveAreaAppHome(user,
							areaPartnerPayRecord.getAreaSiteRankConfig(),
							areaPartnerPayRecord.getAreaGradeOfUser(),
							areaPartnerPayRecord.getBuMen(),
							this.commonService, this.areaPartnerEntityService,
							this.areaHomePageConfigService,
							this.areaCommonpositionService,
							this.areaBannerpositionService,
							this.areaSiteRankConfigService);
					ApiUtils.json(response, "", "购买区域站点成功", 0);
					return;
				}
			} else {
				ApiUtils.json(response, "", "购买区域站点失败，请重新购买", 1);
				return;
			}
		} else if (payType.equals("alipay")) {
			String alipayUrl = CommUtil.getURL(request)
					+ "/appAlipayAreaPartnerCallBack.htm";
			 String str=ApiUtils.getAlipayStr(areaPartnerPayRecord.getOrderNum(),
			 alipayUrl,areaPartnerPayRecord.getAreaSiteRankConfig().getOpenRequiredMoney()+"");
			ApiUtils.json(response, str, "获取支付信息成功", 0);
			return;
		} else if (payType.equals("weixin")) {
			Map<String, String> params = null;
			String weixinUrl = CommUtil.getURL(request)
					+ "/appWeiXinAreaPartnerCallBack.htm";
			try {
				 params=ApiUtils.get_weixin_sign_string(areaPartnerPayRecord.getOrderNum(),
				 weixinUrl,areaPartnerPayRecord.getAreaSiteRankConfig().getOpenRequiredMoney()+"");
//				params = ApiUtils.get_weixin_sign_string(
//						areaPartnerPayRecord.getOrderNum(), weixinUrl, "0.01");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			ApiUtils.json(response, params, "获取支付信息成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "请选择支付方式", 1);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:根据地区名称返回数据库对应的地区
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaByName.htm", method = RequestMethod.POST)
	public void appGetAreaByName(HttpServletRequest request,
			HttpServletResponse response, String areaName, String currentPage,String partnerRank) {
		int current_page = 0;
		int pageSize = 20;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		if (CommUtil.null2String(areaName).equals("")) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		String sql = "";
		List<AreaGradeOfUser> areas = new ArrayList<AreaGradeOfUser>();
		String levels = CommUtil.null2Int(partnerRank)+"";
		if (levels.equals("0")) {
			sql = "SELECT obj.areaRank FROM shopping_areaSiteRankConfig AS obj WHERE obj.deleteStatus = FALSE";
			List<?> level = this.commonService.executeNativeNamedQuery(sql);
			if (level.size() > 0) {
				levels = level.toString().substring(1,
						level.toString().length() - 1);
			}
		}
		sql = "SELECT " + "obj.id, " + "obj.pid " + "FROM "
				+ "shopping_area_grade_of_user AS obj " + "WHERE NAME LIKE '%"
				+ areaName + "%' " + "AND obj.level IN (" + levels + ") "
				+ "ORDER BY obj.level " + "LIMIT " + current_page * pageSize
				+ ", " + pageSize + " ";
		@SuppressWarnings("unchecked")
		List<Object[]> query = this.commonService.executeNativeNamedQuery(sql);
		;
		if (query.size() > 0) {
			for (Object[] obj : query) {
				AreaGradeOfUser a = (AreaGradeOfUser) commonService.getById(
						"AreaGradeOfUser", CommUtil.null2String(obj[0]));
				AreaGradeOfUser p = (AreaGradeOfUser) commonService.getById(
						"AreaGradeOfUser", CommUtil.null2String(obj[1]));
				a.setArea(p);
				areas.add(a);
			}
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,area,level"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, areas, "success", 0, filter);
			return;
		} else {
			ApiUtils.json(response, "", "您所选地区还未开放购买,请联系客服开放", 1);
			return;
		}

	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:根据省级id返回市区和区县数据
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetAreaByProvId.htm", method = RequestMethod.POST)
	public void appGetAreaByProvId(HttpServletRequest request,
			HttpServletResponse response, String areaId) {
		if (CommUtil.null2Long(areaId) == -1) {
			ApiUtils.json(response, "", "参数格式错误", 1);
			return;
		}
		String hql = "select obj from AreaGradeOfUser as obj where obj.pid = "
				+ areaId;
		List<AreaGradeOfUser> citys = this.commonService.query(hql, null, -1,
				-1);
		hql = "select obj from AreaGradeOfUser as obj where obj.pid in (select obj.id from AreaGradeOfUser as obj where obj.pid = "
				+ areaId + ")";
		List<AreaGradeOfUser> areas = this.commonService.query(hql, null, -1,
				-1);
		areas.addAll(citys);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,level"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, areas, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:触发创建对应区域的首页付费图片
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appTriggerAreaHomePageData.htm")
	public void appTriggerAreaHomePageData(HttpServletRequest request,
			HttpServletResponse response, String areaId, String bumenId) {
		AreaGradeOfUser area = (AreaGradeOfUser) this.commonService.getById(
				"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
		if (area == null) {
			ApiUtils.json(response, "", "选择的地区不存在", 1);
			return;
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		BuMen bumen = (BuMen) this.commonService.getById("BuMen", CommUtil
				.null2Long(bumenId).toString());
		if (bumen == null) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		AreaPartnerUtils.saveAreaAppHome(null, null, area, bumen,
				commonService, areaPartnerEntityService,
				areaHomePageConfigService, areaCommonpositionService,
				areaBannerpositionService, areaSiteRankConfigService);
		ApiUtils.json(response, "", "success", 0);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:areaType:区域等级(村委:1; 乡镇:2; 区县:3; 市区:4; 省级:5; 大区:6;);bumenId :部门
	 * @description:管理员获取所有的区域合伙人
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAdminGetAreaPartners.htm", method = RequestMethod.POST)
	public void appAdminGetAreaPartners(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String areaType, String currentPage, String bumenId) {
		boolean admin = ApiUtils.isAdmin(userId, password, this.userService);
		if (!admin) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int current_page = 0;
		int pageSize = 20;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		StringBuffer where = new StringBuffer();
		String num = CommUtil.null2String(areaType);
		if (!num.equals("")) {
			where = where.append(" and obj.areaSiteRankConfig.id = ").append(
					num);
		}
		if (!CommUtil.null2String(bumenId).equals("")) {
			where = where.append(" and obj.buMen.id = ").append(bumenId);
		}
		String hql = "select obj from AreaPartnerEntity as obj where obj.user.id is not null and obj.deleteStatus = false "
				+ where.toString() + " order by obj.id DESC";
		List<AreaPartnerEntity> apes = this.areaPartnerEntityService.query(hql,
				null, current_page * pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,area,level"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(AreaPartnerEntity.class,
				"id,user,area,expireTime,areaSiteRankConfig,buMen"));
		objs.add(new FilterObj(AreaSiteRankConfig.class, "id,areaRankName"));
		objs.add(new FilterObj(BuMen.class, "id,name"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, apes, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:管理员修改区域合伙人所属地区
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAdminUpdateAreaPartnerInfo.htm", method = RequestMethod.POST)
	public void appAdminUpdateAreaPartnerInfo(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String areaPartnerId, String areaId, String areaRankId,
			String bumenId) {
		boolean admin = ApiUtils.isAdmin(userId, password, this.userService);
		if (!admin) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		AreaPartnerEntity areaPartner = this.areaPartnerEntityService
				.getObjById(CommUtil.null2Long(areaPartnerId));
		if (areaPartner == null) {
			ApiUtils.json(response, "", "该区域合伙人不存在", 1);
			return;
		}
		User user = areaPartner.getUser();
		if (user == null) {
			ApiUtils.json(response, "", "该区域暂无合伙人", 1);
			return;
		}
		AreaGradeOfUser area = areaPartner.getArea();
		if (CommUtil.null2Long(areaId) != -1) {
			area = (AreaGradeOfUser) this.commonService.getById(
					"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
			if (area == null) {
				ApiUtils.json(response, "", "该区域不存在", 1);
				return;
			}
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		BuMen bumen = areaPartner.getBuMen();
		Long bId = CommUtil.null2Long(bumenId);
		if (bId != -1) {
			bumen = (BuMen) this.commonService.getById("BuMen", bId.toString());
			if (bumen == null) {
				ApiUtils.json(response, "", "部门不存在", 1);
				return;
			}
		}
		Long id = CommUtil.null2Long(areaRankId);
		AreaSiteRankConfig asrc = areaPartner.getAreaSiteRankConfig();
		if (id != -1) {
			asrc = this.areaSiteRankConfigService.getObjById(id);
			if (asrc == null || asrc.isDeleteStatus()) {
				ApiUtils.json(response, "", "该区域合伙人等级不存在", 1);
				return;
			}
		}
		if (area.getLevel() != asrc.getAreaRank()) {
			ApiUtils.json(response, "", "选择地区级别错误", 1);
			return;
		}
		if (area.getId() == areaPartner.getArea().getId()
				&& bumen.getId() == areaPartner.getBuMen().getId()) {
			ApiUtils.json(response, "", "所要修改的区域不能与原区域一致", 1);
			return;
		}
		// 检测区域站点是否被其他人购买
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select obj from AreaPartnerEntity as obj where obj.user is not null and obj.deleteStatus = false and obj.expireTime > '"
				+ time
				+ "' and obj.area.id = "
				+ area.getId()
				+ " and obj.buMen.id = " + bumen.getId();
		List<AreaPartnerEntity> aPartnerEntities = areaPartnerEntityService
				.query(hql, null, -1, -1);
		if (aPartnerEntities.size() > 0) {
			ApiUtils.json(response, "", "该区域已被抢购，请选择其他区域", 1);
			return;
		}
		AreaPartnerUtils.saveAreaAppHome(user, asrc, area, bumen,
				this.commonService, this.areaPartnerEntityService,
				this.areaHomePageConfigService, this.areaCommonpositionService,
				this.areaBannerpositionService, this.areaSiteRankConfigService);
		AreaPartnerPayRecord areaPartnerPayRecord = new AreaPartnerPayRecord(
				new Date(), ApiUtils.integralOrderNum(user.getId()) + "", 30,
				asrc.getOpenRequiredMoney(), user, area, bumen, asrc);
		areaPartnerPayRecord.setDeleteStatus(false);
		areaPartnerPayRecord.setPayTime(new Date());
		areaPartnerPayRecord.setPayType("admin");
		this.areaPartnerPayRecordService.save(areaPartnerPayRecord);

		AreaPartnerUtils.backupsConfigToNewArea(areaPartner.getArea(),
				areaPartner.getBuMen(), area, bumen, commonService,
				areaPartnerEntityService, areaHomePageConfigService,
				areaCommonpositionService, areaBannerpositionService);
		// AreaPartnerUtils.initiaAreaCommonAndBanner(areaPartner.getArea(),areaPartner.getBuMen(),
		// commonService, areaCommonpositionService,
		// areaBannerpositionService);
		// AreaPartnerUtils.initiaArea(areaPartner.getArea(),areaPartner.getBuMen(),
		// commonService,
		// areaHomePageConfigService);
		areaPartner.setUser(null);
		boolean update = this.areaPartnerEntityService.update(areaPartner);
		if (update) {
			ApiUtils.json(response, "", "修改成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "修改失败，请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password:明文密码
	 *@description:管理员设置区域合伙人登陆页面
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appAreaActionLogin.htm")
	public ModelAndView appAreaActionLogin(HttpServletRequest request,HttpServletResponse response){
		ModelAndView mv = new JModelAndView("/user/login/areaPartnerLogin.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		String hql = "select obj from BuMen as obj where obj.superiorBumen.id is not null";
		@SuppressWarnings("unchecked")
		List<BuMen> bumens = this.commonService.query(hql, null, -1, -1);
		String as = "select obj from AreaSiteRankConfig as obj where obj.deleteStatus = false order by obj.id ASC";
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				as, null, -1, -1);
		String agHql = "select obj from AreaGradeOfUser as obj where obj.level = 2 order by id ";
		@SuppressWarnings("unchecked")
		List<AreaGradeOfUser> list = this.commonService.query(agHql, null, -1, -1);
		mv.addObject("bumens", bumens);
		mv.addObject("asrcs", asrcs);
		mv.addObject("areas", list);
		return mv;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:管理员设置某用户为区域合伙人
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAdminSetUserToAreaPartner.htm", method = RequestMethod.POST)
	public void appAdminSetUserToAreaPartner(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String acceptUserId, String areaId, String areaRankId,
			String bumenId) {
		boolean is_null = ApiUtils.is_null(userId, password, acceptUserId,
				areaId, areaRankId, bumenId);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		BuMen bumen = (BuMen) this.commonService.getById("BuMen", CommUtil
				.null2Long(bumenId).toString());
		if (bumen == null) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		User acceptUser = this.userService.getObjById(CommUtil
				.null2Long(acceptUserId));
		if (acceptUser == null) {
			ApiUtils.json(response, "", "所要操作的用户不存在", 1);
			return;
		}
		boolean admin = ApiUtils.isAdmin(userId, password, this.userService);
		if (!admin) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		AreaGradeOfUser area = (AreaGradeOfUser) this.commonService.getById(
				"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
		if (area == null) {
			ApiUtils.json(response, "", "该区域不存在", 1);
			return;
		}
		Long id = CommUtil.null2Long(areaRankId);
		if (id == -1) {
			ApiUtils.json(response, "", "区域合伙人参数格式错误", 1);
			return;
		}
		AreaSiteRankConfig asrc = this.areaSiteRankConfigService.getObjById(id);
		if (asrc == null || asrc.isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域合伙人等级不存在", 1);
			return;
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		if (area.getLevel() != asrc.getAreaRank()) {
			ApiUtils.json(response, "", "选择地区级别错误", 1);
			return;
		}
		// 检测区域站点是否被其他人购买
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select obj from AreaPartnerEntity as obj where obj.user is not null and obj.deleteStatus = false and obj.expireTime > '"
				+ time
				+ "' and obj.area.id = "
				+ area.getId()
				+ " and obj.buMen.id = " + bumen.getId();
		List<AreaPartnerEntity> aPartnerEntities = areaPartnerEntityService
				.query(hql, null, -1, -1);
		if (aPartnerEntities.size() > 0) {
			ApiUtils.json(response, "", "该区域已被抢购，请选择其他区域", 1);
			return;
		}
		AreaPartnerUtils.saveAreaAppHome(acceptUser, asrc, area, bumen,
				this.commonService, this.areaPartnerEntityService,
				this.areaHomePageConfigService, this.areaCommonpositionService,
				this.areaBannerpositionService, this.areaSiteRankConfigService);
		AreaPartnerPayRecord areaPartnerPayRecord = new AreaPartnerPayRecord(
				new Date(), ApiUtils.integralOrderNum(acceptUser.getId()) + "",
				30, asrc.getOpenRequiredMoney(), acceptUser, area, bumen, asrc);
		areaPartnerPayRecord.setDeleteStatus(false);
		areaPartnerPayRecord.setPayTime(new Date());
		areaPartnerPayRecord.setPayType("admin");
		boolean save = this.areaPartnerPayRecordService
				.save(areaPartnerPayRecord);
		if (save) {
			ApiUtils.json(response, "", "开通区域合伙人成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "开通失败，请重试", 1);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域合伙人设置自己所属区域的配置
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAreaPartnerSetConfig.htm", method = RequestMethod.POST)
	public void appAreaPartnerSetConfig(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String areaPartnerId, String payType, String maxPayNum,
			String isOpen) {
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		boolean admin = ApiUtils.isAdmin(userId, password, this.userService);
		AreaPartnerEntity areaPartnerEntity = this.areaPartnerEntityService
				.getObjById(CommUtil.null2Long(areaPartnerId));
		if (areaPartnerEntity == null) {
			ApiUtils.json(response, "", "该区域合伙人不存在", 1);
			return;
		}
		if (areaPartnerEntity.getUser() != null
				&& areaPartnerEntity.getUser().getId() != user.getId()
				&& !admin) {
			ApiUtils.json(response, "", "没有权限，该区域已被其他人抢购", 1);
			return;
		}
		if (areaPartnerEntity.getUser() == null && !admin) {
			ApiUtils.json(response, "", "没有权限,您未购买该区域", 1);
			return;
		}
		if (areaPartnerEntity.getUser() != null
				&& areaPartnerEntity.getExpireTime().before(new Date())) {
			// 触发过期
			ApiUtils.json(response, "", "区域合伙人权益已过期，请重新购买", 1);
			return;
		}
		String hql = "select obj from AreaHomePageConfig as obj where obj.regionPartner.id = "
				+ areaPartnerEntity.getId();
		List<AreaHomePageConfig> ahpcs = areaHomePageConfigService.query(hql,
				null, -1, -1);
		if (ahpcs.size() <= 0) {
			ApiUtils.json(response, "", "系统繁忙，请联系管理员处理", 1);
			return;
		}
		AreaHomePageConfig areaHomePageConfig = ahpcs.get(0);
		int maxPay = CommUtil.null2Int(maxPayNum);
		if (maxPay != 0) {
			areaHomePageConfig.setMaxPayNum(maxPay);
		}
		if (!CommUtil.null2String(isOpen).equals("")) {
			areaHomePageConfig.setOpen(CommUtil.null2Boolean(isOpen));
		}
		if (!CommUtil.null2String(payType).equals("")) {
			if (payType.equals("day")) {
				areaHomePageConfig.setPayType("元/天");
			} else if (payType.equals("hour")) {
				areaHomePageConfig.setPayType("元/小时");
			}
		}
		boolean update = this.areaHomePageConfigService
				.update(areaHomePageConfig);
		if (update) {
			ApiUtils.json(response, "", "修改配置成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "修改配置失败，请重试", 0);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域合伙人设置自己所属区域的轮播图价钱与默认商品
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAreaPartnerSetAppHomeData.htm", method = RequestMethod.POST)
	public void appAreaPartnerSetAppHomeData(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String id, String price, String defaultGoodsId, String type) {
		boolean is_null = ApiUtils.is_null(type, id);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		String hql = "";
		AreaBannerposition areaBannerposition = null;
		AreaCommonposition areaCommonposition = null;
		AreaHomePageConfig areaHomePageConfig = null;
		if (type.equals("banner")) {
			hql = "select obj from AreaBannerposition as obj where obj.id = "
					+ CommUtil.null2Long(id);
			List<AreaBannerposition> query = this.areaBannerpositionService
					.query(hql, null, -1, -1);
			if (query.size() > 0) {
				areaBannerposition = query.get(0);
				areaHomePageConfig = areaBannerposition.getAreaHomePageConfig();
			}
		} else {
			hql = "select obj from AreaCommonposition as obj where obj.id = "
					+ CommUtil.null2Long(id);
			List<AreaCommonposition> query = this.areaCommonpositionService
					.query(hql, null, -1, -1);
			if (query.size() > 0) {
				areaCommonposition = query.get(0);
				areaHomePageConfig = areaCommonposition.getAreaHomePageConfig();
			}
		}
		if (areaBannerposition == null && areaCommonposition == null) {
			ApiUtils.json(response, "", "该付费位不存在", 1);
			return;
		}
		AreaPartnerEntity areaPartnerEntity = areaHomePageConfig
				.getRegionPartner();
		if (areaPartnerEntity.getUser() == null) {
			ApiUtils.json(response, "", "没有权限，您未购买该区域", 1);
			return;
		}
		if (areaPartnerEntity.getUser() != null
				&& areaPartnerEntity.getUser().getId() != user.getId()) {
			ApiUtils.json(response, "", "没有权限，该区域已被其他人抢购", 1);
			return;
		}
		if (areaPartnerEntity.getExpireTime().before(new Date())) {
			// 触发过期
			ApiUtils.json(response, "", "区域合伙人权益已过期，请重新购买", 1);
			return;
		}
		Goods goods = null;
		if (CommUtil.null2Long(defaultGoodsId) != -1) {
			goods = this.goodsService.getObjById(CommUtil
					.null2Long(defaultGoodsId));
			if (goods == null) {
				ApiUtils.json(response, "", "商品不存在", 1);
				return;
			}
			if (goods.getGoods_status() != 0
					|| goods.getGoods_store().getStore_status() != 2
					|| goods.getGoods_inventory() <= 0) {
				ApiUtils.json(response, "", "该商品已下架或库存不足，请上传其他商品", 1);
				return;
			}
		}
		double pc = CommUtil.null2Double(price);
		boolean is = false;
		if (type.equals("banner")) {
			if (goods != null) {
				boolean is_have = this.judgeIsRepetition(goods.getId() + "",
						"AreaBannerposition", areaHomePageConfig.getId(),"defaultGoods");
				if (is_have) {
					ApiUtils.json(response, "", "为了让首页多元化,请不要设置同一件商品", 3);
					return;
				}
				areaBannerposition.setDefaultGoods(goods);
			}
			if (pc != 0) {
				areaBannerposition.setBanner_price(pc);
			}
			is = this.areaBannerpositionService.update(areaBannerposition);
		} else {
			if (goods != null) {
				boolean is_have = this.judgeIsRepetition(goods.getId() + "",
						"AreaCommonposition", areaHomePageConfig.getId(),"defaultGoods");
				if (is_have) {
					ApiUtils.json(response, "", "为了让首页多元化,请不要设置同一件商品", 3);
					return;
				}
				areaCommonposition.setDefaultGoods(goods);
			}
			if (pc != 0) {
				areaCommonposition.setCommonPosition_price(pc);
			}
			is = this.areaCommonpositionService.update(areaCommonposition);
		}
		if (is) {
			ApiUtils.json(response, "", "修改首页付费图成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "修改首页付费图失败，请重试", 1);
			return;
		}
	}

	private boolean judgeIsRepetition(String goodsId, String classType,
			Long ahpcId,String name) {
		boolean ret = false;
		String judgeHql = "select obj from " + classType
				+ " as obj where obj." + name + ".id=" + goodsId
				+ " and obj.areaHomePageConfig.id = " + ahpcId;
		List<?> appJudgeList = this.commonService.query(judgeHql, null, -1, -1);
		if (appJudgeList.size() > 0) {
			ret = true;
		}
		return ret;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:三级联动，获取区域合伙人地址
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appAreaLinkage.htm", method = RequestMethod.POST)
	public void appAreaLinkage(HttpServletRequest request,
			HttpServletResponse response, String addressId) {
		String where = "";
		if ("".equals(CommUtil.null2String(addressId))) {
			where = "obj.pid in (select obj.id from AreaGradeOfUser as obj where obj.pid = 1)";
		} else {
			where = "obj.pid = " + CommUtil.null2Long(addressId);
		}
		String hql = "select obj from AreaGradeOfUser as obj where " + where
				+ " order by id ";
		List<Area> query = commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,level"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query, "获取成功", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:通过区域id和部门获取区域付费位
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaBannerAndCommon.htm", method = RequestMethod.POST)
	public void appGetAreaBannerAndCommon(HttpServletRequest request,
			HttpServletResponse response, String areaId, String bumenId) {
		AreaGradeOfUser area = (AreaGradeOfUser) this.commonService.getById(
				"AreaGradeOfUser", CommUtil.null2Long(areaId) + "");
		if (area == null) {
			ApiUtils.json(response, "", "该区域不存在", 1);
			return;
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ area.getLevel(), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		BuMen bumen = (BuMen) this.commonService.getById("BuMen", CommUtil
				.null2Long(bumenId).toString());
		if (bumen == null) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		String hql = "select obj from AreaHomePageConfig as obj where obj.regionPartner.area.id = "
				+ area.getId()
				+ " and obj.regionPartner.buMen.id = "
				+ bumen.getId();
		List<AreaHomePageConfig> ahpcs = areaHomePageConfigService.query(hql,
				null, -1, -1);
		AreaHomePageConfig ahpc = null;
		if (ahpcs.size() <= 0) {
			AreaPartnerUtils.saveAreaAppHome(null, null, area, bumen,
					commonService, areaPartnerEntityService,
					areaHomePageConfigService, areaCommonpositionService,
					areaBannerpositionService, areaSiteRankConfigService);
			ahpcs = areaHomePageConfigService.query(hql, null, -1, -1);
		}
		ahpc = ahpcs.get(0);// 报错验证
		if (ahpc.getRegionPartner().getUser() == null) {
			if (!ahpc.isOpen()) {
				ahpc.setOpen(true);
				areaHomePageConfigService.update(ahpc);
			}
		}
		hql = "select obj from AreaBannerposition as obj where obj.areaHomePageConfig.id = "
				+ ahpc.getId() + " order by obj.id";
		List<AreaBannerposition> abps = areaBannerpositionService.query(hql,
				null, -1, -1);
		hql = "select obj from AreaCommonposition as obj where obj.areaHomePageConfig.id = "
				+ ahpc.getId() + " order by obj.id";
		List<AreaCommonposition> acps = areaCommonpositionService.query(hql,
				null, -1, -1);
		AppTransferData appTransferData = new AppTransferData();
		appTransferData.setFirstData(abps);
		appTransferData.setSecondData(acps);
		appTransferData.setFifthData(ahpc);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaBannerposition.class,
				"id,is_can_buy,banner_price,sequence,position_name,defaultGoods"));
		objs.add(new FilterObj(AreaCommonposition.class,
				"id,is_can_buy,commonPosition_price,sequence,position_name,defaultGoods"));
		objs.add(new FilterObj(AppTransferData.class,
				"firstData,secondData,fifthData"));
		objs.add(new FilterObj(AreaHomePageConfig.class,
				"id,maxPayNum,open,payType"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_main_photo,goods_price,store_price,goods_salenum,goods_name"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appTransferData, "查询成功", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:创建区域付费位订单
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appCreateAreaPayPlaceOrder.htm", method = RequestMethod.POST)
	public void appCreateAreaPayPlaceOrder(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String placeId, String placeType, String goodsId, String purchaseNum) {
		boolean is_null = ApiUtils.is_null(userId, password, placeId,
				placeType, goodsId, purchaseNum);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if (goods == null) {
			ApiUtils.json(response, "", "商品不存在", 1);
			return;
		}
		if (goods.getGoods_status() != 0
				|| goods.getGoods_store().getStore_status() != 2
				|| goods.getGoods_inventory() <= 0) {
			ApiUtils.json(response, "", "商品已下架或库存不足，请选择上架商品", 1);
			return;
		}
		if (user.getStore().getId() != goods.getGoods_store().getId()) {
			ApiUtils.json(response, "", "请选择自己店铺中的商品", 1);
			return;
		}
		int num = CommUtil.null2Int(purchaseNum);
		if (num == 0) {
			ApiUtils.json(response, "", "购买时长错误", 1);
			return;
		}
		Date time = new Date();
		AreaAppHomePayTemporary aahpt = new AreaAppHomePayTemporary(goods, num,
				time, null, "", 0, 10, "", time, user);
		AreaHomePageConfig areaHomePageConfig = null;
		if (placeType.equals("banner")) {
			AreaBannerposition abp = this.areaBannerpositionService
					.getObjById(CommUtil.null2Long(placeId));
			if (abp == null) {
				ApiUtils.json(response, "", "该轮播位不存在", 1);
				return;
			}
			areaHomePageConfig = abp.getAreaHomePageConfig();
			aahpt.setAreaBannerposition(abp);
			aahpt.setVacantPositionType("banner");
			aahpt.setTotal(num * abp.getBanner_price());
		} else if (placeType.equals("common")) {
			AreaCommonposition acp = this.areaCommonpositionService
					.getObjById(CommUtil.null2Long(placeId));
			if (acp == null) {
				ApiUtils.json(response, "", "该普通位不存在", 1);
				return;
			}
			areaHomePageConfig = acp.getAreaHomePageConfig();
			aahpt.setAreaCommonposition(acp);
			aahpt.setVacantPositionType("common");
			aahpt.setTotal(num * acp.getCommonPosition_price());
		} else {
			ApiUtils.json(response, "", "购买类型错误", 1);
			return;
		}
		boolean is_have = this.judgeIsRepetition(goods.getId() + "",
				"AreaBannerposition", areaHomePageConfig.getId(),"goods");
		if (is_have) {
			ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
			return;
		}
		int hour = this.getAppHomePayHour(num, areaHomePageConfig);
		if (hour == 0) {
			ApiUtils.json(response, "", "购买时间超出系统允许的范围", 1);
			return;
		}
		aahpt.setFlush_time(this.addDateMinut(time, hour));
		aahpt.setDeleteStatus(false);
		aahpt.setOrderNum(ApiUtils.integralOrderNum(user.getId()).toString());
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.MINUTE, 15);
		aahpt.setCloseTime(cal.getTime());
		boolean save = this.areaAppHomePayTemporaryService.save(aahpt);
		if (save) {
			ApiUtils.json(response, aahpt.getOrderNum(), "success", 0);
			return;
		} else {
			ApiUtils.json(response, "", "生成失败，请重试", 1);
			return;
		}
	}

	/**
	 * 给时间加上几个小时
	 * 
	 * @param date
	 *            当前时间
	 * @param hour
	 *            需要加的时间
	 * @return
	 */
	private Date addDateMinut(Date date, int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, hour);// 24小时制
		date = cal.getTime();
		cal = null;
		return date;
	}

	private int getAppHomePayHour(int purchase_timeDuan,
			AreaHomePageConfig areaHomePageConfig) {
		int hour = purchase_timeDuan;
		if (!(areaHomePageConfig.getMaxPayNum() >= purchase_timeDuan && purchase_timeDuan > 0)) {
			return 0;
		}
		if ("元/天".equals(areaHomePageConfig.getPayType())) {
			hour = 24 * purchase_timeDuan;
		}
		return hour;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:payType:balance 积分支付； alipay 支付宝支付；weixin 微信支付
	 * @description:购买区域首页付费位，积分支付,微信支付,支付宝支付,如果为积分支付 需要password明文密码
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appPaymentAreaPlace.htm", method = RequestMethod.POST)
	public void appPaymentAreaPlace(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String orderNum, String payType) {
		boolean is_null = ApiUtils.is_null(userId, orderNum, payType);
		if (is_null) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue() == -1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user == null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String hql = "select obj from AreaAppHomePayTemporary as obj where obj.orderNum = "
				+ orderNum;
		List<AreaAppHomePayTemporary> aahpts = this.areaAppHomePayTemporaryService
				.query(hql, null, -1, -1);
		if (aahpts.size() <= 0) {
			ApiUtils.json(response, "", "订单不存在，请重新购买", 1);
			return;
		}
		AreaAppHomePayTemporary aahpt = aahpts.get(0);
		if (aahpt.getUser().getId() != user.getId()) {
			ApiUtils.json(response, "", "请选择自己的订单支付", 1);
			return;
		}
		if (aahpt.getOrderStatus() == 20) {
			ApiUtils.json(response, "", "该订单已被支付", 1);
			return;
		}
		if (payType.equals("balance")) {
			if ("".equals(CommUtil.null2String(password))) {
				ApiUtils.json(response, "", "密码不能为空", 1);
				return;
			}
			String psw = user.getPassword();
			String lowerCase = Md5Encrypt.md5(password).toLowerCase();
			if (!psw.equals(lowerCase)) {
				ApiUtils.json(response, "", "密码错误！", 1);
				return;
			}
			double userBalance = user.getAvailableBalance().doubleValue();
			Double openRequiredMoney = aahpt.getTotal();
			if (user.getFreezeBlance().intValue() == 1) {
				if (userBalance - openRequiredMoney - 1000 < 0) {
					ApiUtils.json(response, "", "余额不足，您有部分余额处于锁定状态", 1);
					return;
				}
			} else {
				if (userBalance - openRequiredMoney < 0) {
					ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 1);
					return;
				}
			}
			String explain = aahpt.getAreaBannerposition() == null ? aahpt
					.getAreaCommonposition().getPosition_name() : aahpt
					.getAreaBannerposition().getPosition_name();
			boolean status = AllocateWagesUtils.allocateMoneyToUser(
					user.getId() + "", -openRequiredMoney, "购买" + explain
							+ "支出", "", predepositLogService, userService,
					commonService, 0);
			if (status) {
				boolean appPaymentAreaPlace = AreaPartnerUtils
						.appPaymentAreaPlace("balance", aahpt,
								areaCommonpositionService,
								areaBannerpositionService, userService,
								commonService, areaAppHomePayTemporaryService,
								predepositLogService);
				if (!appPaymentAreaPlace) {
					// 退款
					AllocateWagesUtils.allocateMoneyToUser(user.getId() + "",
							openRequiredMoney, "购买" + explain + "退款", "",
							predepositLogService, userService, commonService,
							CommUtil.null2Int(openRequiredMoney));
					ApiUtils.json(response, "", "购买失败，所选付费位已被其他人购买", 1);
					return;
				}
				aahpt.setOrderStatus(20);
				aahpt.setPayTime(new Date());
				aahpt.setPayType(payType);
				boolean update = this.areaAppHomePayTemporaryService
						.update(aahpt);
				if (update) {
					ApiUtils.json(response, "", "购买区域付费位成功", 0);
					return;
				}
			} else {
				ApiUtils.json(response, "", "购买区域付费位失败，请重新购买", 1);
				return;
			}
		} else if (payType.equals("alipay")) {
			String alipayUrl = CommUtil.getURL(request)
					+ "/appAliPayAreaAppHomeCallBack.htm";
			String str = ApiUtils.getAlipayStr(aahpt.getOrderNum(), alipayUrl,
					aahpt.getTotal() + "");
			ApiUtils.json(response, str, "获取支付信息成功", 0);
			return;
		} else if (payType.equals("weixin")) {
			Map<String, String> params = null;
			String weixinUrl = CommUtil.getURL(request)
					+ "/appWeChatPayAreaAppHomeCallBack.htm";
			try {
				params = ApiUtils.get_weixin_sign_string(aahpt.getOrderNum(),
						weixinUrl, aahpt.getTotal() + "");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			ApiUtils.json(response, params, "获取支付信息成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "请选择支付方式", 1);
			return;
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:获取用户购买的所有区域站点
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaPartnerAllArea.htm", method = RequestMethod.POST)
	public void appGetAreaPartnerAllArea(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String currentPage, String adminUserId, String adminPassword) {
		int current_page = 0;
		int pageSize = 20;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			if (!ApiUtils.isAdmin(adminUserId, adminPassword, userService)) {
				ApiUtils.json(response, "", "用户名或密码错误", 1);
				return;
			}
		}
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select ahpc from AreaHomePageConfig as ahpc left join ahpc.regionPartner as obj where obj.expireTime >= '"
				+ time
				+ "' and obj.user.id = "
				+ userId
				+ " order by obj.area.level";
		List<AreaPartnerEntity> query = this.areaPartnerEntityService.query(
				hql, null, current_page * pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaPartnerEntity.class,
				"id,area,expireTime,areaSiteRankConfig,buMen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,level"));
		objs.add(new FilterObj(AreaHomePageConfig.class, "payType,maxPayNum,open,regionPartner"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(BuMen.class, "id,name"));
		objs.add(new FilterObj(AreaSiteRankConfig.class,
				"id,areaRankName,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域合伙人查看自己的付费位收入情况
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appPartnerGetAreaPlaceIncome.htm", method = RequestMethod.POST)
	public void appPartnerGetAreaPlaceIncome(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String beginTime, String endTime, String adminUserId,
			String adminPassword) {
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user == null) {
			if (!ApiUtils.isAdmin(adminUserId, adminPassword, userService)) {
				ApiUtils.json(response, "", "用户名或密码错误", 1);
				return;
			}
		}
		Map<String, Object> data = AreaPartnerUtils.getToolsData(beginTime,
				endTime, null);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Long days = (Long) data.get("days");
		if (days > 90) {
			ApiUtils.json(response, "", "查询时间不能大于90天", 1);
			return;
		}
		String sql = "SELECT " + "temp4.id, " + "temp4.aName, "
				+ "temp4.sapId," + "temp4.total, " + "eb.name " + "FROM "
				+ "ecm_bumen AS eb " + "RIGHT JOIN " + "(SELECT " + "sag.id, "
				+ "temp3.sapId," + "sag.name as aName, " + "temp3.total, "
				+ "temp3.buMen_id " + "FROM "
				+ "shopping_area_grade_of_user AS sag " + "RIGHT JOIN "
				+ "(SELECT " + "sap.id as sapId,sap.area_id, "
				+ "sap.buMen_id, " + "temp2.total " + "FROM "
				+ "shopping_areapartner AS sap " + "RIGHT JOIN " + "(SELECT "
				+ "sac.id, " + "sac.regionPartner_id, " + "temp.total "
				+ "FROM " + "shopping_areahomepageconfig AS sac "
				+ "RIGHT JOIN " + "(SELECT " + "SUM(sa.total) AS total, "
				+ "sa.areaHomepageConfig_id " + "FROM "
				+ "shopping_areaapphomepaytemporary AS sa "
				+ "WHERE sa.addTime >= '" + begin + "' "
				+ "AND sa.addTime <= '" + end + "' "
				+ "AND sa.orderStatus = 20 " + "AND partnerUser_id = " + userId
				+ " GROUP BY sa.areaHomepageConfig_id) AS temp "
				+ "ON sac.id = temp.areaHomepageConfig_id) AS temp2 "
				+ "ON sap.id = temp2.regionPartner_id) AS temp3 "
				+ "ON sag.id = temp3.area_id) AS temp4 "
				+ "ON temp4.buMen_id = eb.id ";
		List<?> query = this.commonService.executeNativeNamedQuery(sql);
		double sum = 0;
		for (Object obj : query) {
			sum += (double) ((Object[]) obj)[3];
		}
		AppTransferData d = new AppTransferData();
		d.setFirstData(query);
		d.setSecondData(sum);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, d, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:首页轮播图与顺序图更改以后同步更改没有区域合伙人的区域数据，与首页数据同步
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appUpdateAreaData.htm")
	public void appUpdateAreaData(HttpServletRequest request,
			HttpServletResponse response, String id, String type, String price,
			String defaultGoodsId) {
		String where = "";
		if (!"".equals(defaultGoodsId)) {
			where = "sa.defaultGoods_id =" + defaultGoodsId;
		}
		String sql = "";
		if (CommUtil.null2String(type).equals("banner")) {
			if (!"".equals(price)) {
				if (!"".equals(where)) {
					where += ",";
				}
				where += "sa.banner_price = " + price;
			}
			AppHomePageEntity ahpe = (AppHomePageEntity) this.commonService
					.getById("AppHomePageEntity", CommUtil.null2Long(id) + "");
			sql = "UPDATE " + "shopping_areabannerposition AS sa, "
					+ "(SELECT " + "* " + "FROM " + "(SELECT " + "temp2.id, "
					+ "temp2.banner_price, " + "temp2.defaultGoods_id "
					+ "FROM " + "shopping_areapartner AS sap " + "RIGHT JOIN "
					+ "(SELECT " + "temp.id, " + "temp.banner_price, "
					+ "temp.defaultGoods_id, " + "sa.regionPartner_id "
					+ "FROM " + "shopping_areahomepageconfig AS sa "
					+ "RIGHT JOIN " + "(SELECT " + "id, "
					+ "banner_price, " + "defaultGoods_id, "
					+ "areaHomePageConfig_id " + "FROM "
					+ "shopping_areabannerposition AS obj "
					+ "WHERE obj.sequence = " + ahpe.getSequence()
					+ ") AS temp "
					+ "ON sa.id = temp.areaHomePageConfig_id) AS temp2 "
					+ "ON sap.id = temp2.regionPartner_id "
					+ "WHERE sap.user_id IS NULL) AS temp3) AS temp4 " + "SET "
					+ where + " WHERE temp4.id = sa.id ";
		} else {
			AppHomePageCommonPosition ahpc = (AppHomePageCommonPosition) this.commonService
					.getById("AppHomePageCommonPosition",
							CommUtil.null2Long(id) + "");
			if (!"".equals(price)) {
				if (!"".equals(where)) {
					where += ",";
				}
				where += "sa.commonPosition_price = " + price;
			}
			sql = "UPDATE " + "shopping_areacommonposition AS sa, "
					+ "(SELECT " + "* " + "FROM " + "(SELECT " + "temp2.id, "
					+ "temp2.commonPosition_price, " + "temp2.defaultGoods_id "
					+ "FROM " + "shopping_areapartner AS sap " + "RIGHT JOIN "
					+ "(SELECT " + "temp.id, " + "temp.commonPosition_price, "
					+ "temp.defaultGoods_id, " + "sa.regionPartner_id "
					+ "FROM " + "shopping_areahomepageconfig AS sa "
					+ "RIGHT JOIN  " + "(SELECT  " + "id,  "
					+ "commonPosition_price,  " + "defaultGoods_id,  "
					+ "areaHomePageConfig_id  " + "FROM  "
					+ "shopping_areacommonposition AS obj  "
					+ "WHERE obj.sequence = " + ahpc.getSequence()
					+ ") AS temp  "
					+ "ON sa.id = temp.areaHomePageConfig_id) AS temp2  "
					+ "ON sap.id = temp2.regionPartner_id  "
					+ "WHERE sap.user_id IS NULL) AS temp3) AS temp4  "
					+ "SET  " + where + " WHERE temp4.id = sa.id";
		}
		int num = this.commonService.executeNativeSQL(sql);
		ApiUtils.json(response, num, "success", 0);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域的配置与首页数据同步
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appSyncAreaPartnerSetConfig.htm")
	public void appSyncAreaPartnerSetConfig(HttpServletRequest request,
			HttpServletResponse response, String payType, String maxPayNum,
			String isOpen) {
		StringBuffer where = new StringBuffer();
		int maxPay = CommUtil.null2Int(maxPayNum);
		if (maxPay != 0) {
			where.append("obj.maxPayNum = ").append(maxPay);
		}
		if (!CommUtil.null2String(isOpen).equals("")) {
			if (!where.toString().equals("")) {
				where.append(",");
			}
			where.append("obj.isOpen = ").append(CommUtil.null2Boolean(isOpen));
		}
		if (!CommUtil.null2String(payType).equals("")) {
			if (!where.toString().equals("")) {
				where.append(",");
			}
			if (payType.equals("day")) {
				where.append("obj.payType ='元/天'");
			} else if (payType.equals("hour")) {
				where.append("obj.payType ='元/小时'");
			}
		}
		String sql = "UPDATE " + "shopping_areahomepageconfig AS obj, "
				+ "(SELECT " + "obj.id " + "FROM "
				+ "shopping_areahomepageconfig AS obj "
				+ "LEFT JOIN shopping_areapartner AS sa "
				+ "ON sa.id = obj.regionPartner_id "
				+ "WHERE sa.user_id IS NULL) AS temp " + "SET "
				+ where.toString() + " WHERE obj.id = temp.id ";
		this.commonService.executeNativeSQL(sql);
		ApiUtils.json(response, "", "success", 0);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域合伙人查询具体区域付费位购买详情
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAreaPartnerQueryProfit.htm", method = RequestMethod.POST)
	public void appAreaPartnerQueryProfit(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String beginTime, String areaPartnerId, String endTime,
			String adminUserId, String adminPassword,String currentPage) {
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		boolean admin = ApiUtils.isAdmin(adminUserId, adminPassword,
				userService);
		if (user == null) {
			if (!admin) {
				ApiUtils.json(response, "", "用户名或密码错误", 1);
				return;
			}
		}
		AreaPartnerEntity areaPartner = this.areaPartnerEntityService
				.getObjById(CommUtil.null2Long(areaPartnerId));
		if (areaPartner == null) {
			ApiUtils.json(response, "", "区域合伙人不存在", 1);
			return;
		}
		Map<String, Object> data = AreaPartnerUtils.getToolsData(beginTime,
				endTime, currentPage);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Long days = (Long) data.get("days");
		int current_page = CommUtil.null2Int(data.get("current_page"));
		int pageSize = 20;
		if (days > 90) {
			ApiUtils.json(response, "", "查询时间不能大于90天", 1);
			return;
		}
		String hql = "select obj from AreaAppHomePayTemporary as obj where obj.orderStatus=20 and obj.addTime >= '"
				+ begin
				+ "' and obj.addTime <= '"
				+ end
				+ "' and obj.partnerUser.id = "
				+ userId
				+ " and obj.areaHomePageConfig.regionPartner.area.id = "
				+ areaPartner.getArea().getId()
				+ " and obj.areaHomePageConfig.regionPartner.buMen.id ="
				+ areaPartner.getBuMen().getId();
		List<AreaAppHomePayTemporary> query = this.areaAppHomePayTemporaryService
				.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				AreaAppHomePayTemporary.class,
				"id,total,vacantPositionType,payTime,areaBannerposition,areaCommonposition,areaCommonposition,goods"));
		objs.add(new FilterObj(AreaBannerposition.class, "id,position_name"));
		objs.add(new FilterObj(AreaCommonposition.class, "id,position_name"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_main_photo,goods_price,store_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:检测区域合伙人是否到期
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appInspectAreaPartnerStatus.htm", method = RequestMethod.POST)
	public void appInspectAreaPartnerStatus(HttpServletRequest request,
			HttpServletResponse response) {
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select count(obj) from AreaPartnerEntity as obj where obj.user.id is not null and obj.expireTime <= '"
				+ time + "'";
		List<?> sum = this.commonService.query(hql, null, -1, -1);
		int num = 0;
		int current_page = 0;
		int pageSize = 100;
		if (sum.size() > 0) {
			int count = CommUtil.null2Int(sum.get(0));
			num = count % pageSize == 0 ? count / pageSize : count / pageSize
					+ 1;
		}
		hql = "select obj from AreaPartnerEntity as obj where obj.user.id is not null and obj.expireTime <= '"
				+ time + "'";
		for (int i = 0; i < num; i++) {
			List<AreaPartnerEntity> apes = this.areaPartnerEntityService.query(
					hql, null, current_page * pageSize, pageSize);
			current_page++;
			for (AreaPartnerEntity ape : apes) {
				AreaGradeOfUser area = ape.getArea();
				ape.setUser(null);
				boolean update = this.areaPartnerEntityService.update(ape);
				if (update) {
					AreaPartnerUtils.initiaAreaCommonAndBanner(area,
							ape.getBuMen(), commonService,
							areaCommonpositionService,
							areaBannerpositionService);
					AreaPartnerUtils.initiaArea(area, ape.getBuMen(),
							commonService, areaHomePageConfigService);
				}
			}
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:检查区域首页付费位过期状态
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/AppInspectAreaDataStatus.htm", method = RequestMethod.POST)
	public void AppInspectAreaDataStatus(HttpServletRequest request,
			HttpServletResponse response) {
		String time = CommUtil.formatLongDate(new Date());
		String hql = "select count(obj) from AreaBannerposition as obj where obj.goods.id is not null and obj.flush_time <= '"
				+ time + "'";
		List<?> sum = this.commonService.query(hql, null, -1, -1);
		int num = 0;
		int current_page = 0;
		int pageSize = 100;
		try {
			if (sum.size() > 0) {
				int count = CommUtil.null2Int(sum.get(0));
				num = count % pageSize == 0 ? count / pageSize : count
						/ pageSize + 1;
			}
			hql = "select obj from AreaBannerposition as obj where obj.goods.id is not null and obj.flush_time <= '"
					+ time + "'";
			for (int i = 0; i < num; i++) {
				List<AreaBannerposition> abps = this.areaBannerpositionService
						.query(hql, null, current_page * pageSize, pageSize);
				for (AreaBannerposition abp : abps) {
					abp.setGoods(null);
					abp.setIs_can_buy(true);
					abp.setFlush_time(null);
					this.areaBannerpositionService.update(abp);
				}
				current_page++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		current_page = 0;
		hql = "select count(obj) from AreaCommonposition as obj where obj.goods.id is not null and obj.flush_time <= '"
				+ time + "'";
		sum = this.commonService.query(hql, null, -1, -1);
		try {
			if (sum.size() > 0) {
				int count = CommUtil.null2Int(sum.get(0));
				num = count % pageSize == 0 ? count / pageSize : count
						/ pageSize + 1;
			}
			hql = "select obj from AreaCommonposition as obj where obj.goods.id is not null and obj.flush_time <= '"
					+ time + "'";
			for (int i = 0; i < num; i++) {
				List<AreaCommonposition> acps = this.areaCommonpositionService
						.query(hql, null, current_page * pageSize, pageSize);
				for (AreaCommonposition acp : acps) {
					acp.setGoods(null);
					acp.setIs_can_buy(true);
					acp.setFlush_time(null);
					this.areaCommonpositionService.update(acp);
				}
				current_page++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:检查区域首页付费位商品是否下架
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appChangeAreaHomePageGoods.htm", method = RequestMethod.POST)
	public void appChangeAreaHomePageGoods(HttpServletRequest request,
			HttpServletResponse response) {
		String hql = "";
		String sql = "";
		for (int i = 0; i < 5; i++) {
			hql = "select obj.id from AreaBannerposition as obj where obj.defaultGoods.goods_status <> 0 or obj.defaultGoods.goods_store.store_status <> 2 and obj.sequence = "
					+ (i + 1);
			List<?> sum = this.commonService.query(hql, null, -1, -1);
			if (sum.size() > 0) {
				hql = "select obj from AppHomePageEntity as obj where obj.sequence = "
						+ (i + 1);
				List<AppHomePageEntity> ahpes = this.commonService.query(hql,
						null, -1, -1);
				String ids = sum.toString().substring(1,
						sum.toString().length() - 1);
				sql = "UPDATE shopping_areabannerposition AS obj SET obj.defaultGoods_id = "
						+ ahpes.get(0).getDefaultGoods().getId()
						+ " WHERE obj.id in (" + ids + ")";
				this.commonService.executeNativeSQL(sql);
			}
		}
		for (int i = 0; i < 12; i++) {
			hql = "select obj.id from AreaCommonposition as obj where obj.defaultGoods.goods_status <> 0 or obj.defaultGoods.goods_store.store_status <> 2 and obj.sequence = "
					+ (i + 1);
			List<?> sum = this.commonService.query(hql, null, -1, -1);
			if (sum.size() > 0) {
				hql = "select obj from AppHomePageCommonPosition as obj where obj.sequence = "
						+ (i + 1);
				List<AppHomePageCommonPosition> ahpes = this.commonService
						.query(hql, null, -1, -1);
				String ids = sum.toString().substring(1,
						sum.toString().length() - 1);
				sql = "UPDATE shopping_areaCommonposition AS obj SET obj.defaultGoods_id = "
						+ ahpes.get(0).getDefaultGoods().getId()
						+ " WHERE obj.id in (" + ids + ")";
				this.commonService.executeNativeSQL(sql);
			}
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:获取20个部门
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetBumenInfo.htm", method = RequestMethod.POST)
	public void appGetBumenInfo(HttpServletRequest request,
			HttpServletResponse response) {
		String hql = "select obj from BuMen as obj where obj.superiorBumen.id is not null";
		@SuppressWarnings("unchecked")
		List<BuMen> bumens = this.commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(BuMen.class, "id,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, bumens, "success", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:根据区域合伙人等级获取对应等级的区域
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaByPartnerRank.htm", method = RequestMethod.POST)
	public void appGetAreaByPartnerRank(HttpServletRequest request,
			HttpServletResponse response, String partnerRank, String currentPage) {
		int current_page = 0;
		int pageSize = 20;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		List<AreaSiteRankConfig> asrcs = this.areaSiteRankConfigService.query(
				"select obj from AreaSiteRankConfig as obj where obj.areaRank = "
						+ CommUtil.null2Int(partnerRank), null, -1, -1);
		if (asrcs.size() <= 0 || asrcs.size() > 0
				&& asrcs.get(0).isDeleteStatus()) {
			ApiUtils.json(response, "", "该区域暂未开放购买", 1);
			return;
		}
		String hql = "select obj from AreaGradeOfUser as obj where obj.level = "
				+ CommUtil.null2Int(partnerRank);
		@SuppressWarnings("unchecked")
		List<AreaGradeOfUser> citys = this.commonService.query(hql, null,
				current_page * pageSize, pageSize);
		for (AreaGradeOfUser obj : citys) {
			AreaGradeOfUser p = (AreaGradeOfUser) commonService.getById(
					"AreaGradeOfUser", CommUtil.null2String(obj.getPid()));
			obj.setArea(p);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,level,area"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, citys, "success", 0, filter);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:获取区域合伙人PPT下载链接
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetAreaPartnerPPT.htm", method = RequestMethod.POST)
	public void appGetAreaPartnerPPT(HttpServletRequest request,
			HttpServletResponse response, String partnerRank, String currentPage) {
		String hql = "select obj from AccessoryApi as obj where obj.name like '%燎原%'";
		@SuppressWarnings("unchecked")
		List<AccessoryApi> accessoryApis = this.commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AccessoryApi.class, "id,name,path"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, accessoryApis, "success", 0, filter);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:管理员查看区域合伙人清单记录
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appAreaPartnerRecord.htm", method = RequestMethod.POST)
	public void appAreaPartnerRecord(HttpServletRequest request,
			HttpServletResponse response, String payStatus, String currentPage,String userId,String password,
			String rewardStatus, String beginTime, String endTime) {
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		int pageSize = 20;
		Map<String, Object> data = AreaPartnerUtils.getToolsData(beginTime, endTime, currentPage);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		Integer current_page=(Integer) data.get("current_page");
		StringBuffer where = new StringBuffer();
		int pay = CommUtil.null2Int(payStatus);
		if (pay!=0) {
			where.append(" and obj.payStatus = ").append(pay);
		}else {
			where.append(" and obj.payStatus = 20");
		}
		if (!CommUtil.null2String(rewardStatus).equals("")) {
			where.append(" and obj.rewardStatus = ").append(CommUtil.null2Boolean(rewardStatus));
		}
		String hql = "select obj from AreaPartnerPayRecord as obj where obj.deleteStatus = false and obj.addTime >= '" + begin + "'  and obj.addTime <= '"  + end + "'" + where.toString() + " order by obj.id desc";
		List<AreaPartnerPayRecord> list = this.areaPartnerPayRecordService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AreaPartnerPayRecord.class, "id,payType,payStatus,payMoney,user,areaGradeOfUser,areaSiteRankConfig,buMen,rewardStatus,funds"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,level"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(AreaSiteRankConfig.class, "id,areaRankName"));
		objs.add(new FilterObj(BuMen.class, "id,name"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "success", 0, filter);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:获取邀请合伙人奖励分配比例
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGetInvitePartnerRewardRatio.htm", method = RequestMethod.POST)
	public void appGetInvitePartnerRewardRatio(HttpServletRequest request,
			HttpServletResponse response) {
		String hql = "select obj from InvitePartnerRewardRatio as obj";
		@SuppressWarnings("unchecked")
		List<InvitePartnerRewardRatio> list = this.commonService.query(hql, null, -1, -1);
		ApiUtils.json(response, list, "success", 0);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:管理员修改邀请合伙人奖励分配比例
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appUpdateInvitePartnerRewardRatio.htm", method = RequestMethod.POST)
	public void appUpdateInvitePartnerRewardRatio(HttpServletRequest request,
			HttpServletResponse response,String id,String userId,String password,String percentage) {
		Double ratio = CommUtil.null2Double(percentage);
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		InvitePartnerRewardRatio iprr = (InvitePartnerRewardRatio) this.commonService.getById("InvitePartnerRewardRatio", id);
		if (iprr==null) {
			ApiUtils.json(response, "", "该分配比例不存在", 1);
			return;
		} 
		iprr.setPercentage(percentage + "%");
		iprr.setScale(ratio/100);
		this.commonService.update(iprr);
		ApiUtils.json(response, "", "success", 0);
		return;
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:管理员查看发放邀请合伙人奖励
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appGrantInvitePartnerReward.htm", method = RequestMethod.POST)
	public void appGrantInvitePartnerReward(HttpServletRequest request,
			HttpServletResponse response,String id,String userId,String password,String grant) {
		boolean is = ApiUtils.isAdmin(userId, password, userService);
		if (!is) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		AreaPartnerPayRecord appr = this.areaPartnerPayRecordService.getObjById(CommUtil.null2Long(id));
		if (appr==null) {
			ApiUtils.json(response, "", "该订单不存在", 1);
			return;
		}
		if (appr.getPayStatus()==10) {
			ApiUtils.json(response, "", "该订单未支付,不能分款", 1);
			return;
		}
		if (appr.isRewardStatus()) {
			ApiUtils.json(response, "", "该订单拓展经费已经发放过了", 1);
			return;
		}
		if (CommUtil.null2String(grant).equals("false")) {
			appr.setRewardStatus(true);
			appr.setFunds(0d);
			boolean update = this.areaPartnerPayRecordService.update(appr);
			if (update) {
				ApiUtils.json(response, "", "发放拓展经费成功", 0);
				return;
			}else {
				ApiUtils.json(response, "", "系统异常，发放拓展经费失败，请稍后重试", 0);
				return;
			}
		}
		User user = appr.getUser();//购买的用户
		String hql = "select obj from User as obj where obj.userName = '" + CommUtil.null2String(user.getDan_bao_ren()) + "'";
		List<User> users = this.userService.query(hql, null, -1, -1);
		User danbaoUser = null;
		if (users.size()<=0) {
			danbaoUser = this.userService.getObjById(1l);
		}else {
			danbaoUser = users.get(0);
		}
		Long ratioId = 0l;
		String time = CommUtil.formatLongDate(new Date());
		hql = "select obj.id from AreaPartnerEntity as obj where obj.expireTime >= '"
				+ time
				+ "' and obj.user.id = "
				+ danbaoUser.getId();
		List<AreaPartnerEntity> apes = this.areaPartnerEntityService.query(hql, null, 0, 1);
		if (apes.size()>0) {
			ratioId = 4l;
		}
		if (ratioId!=4&&danbaoUser.getUserRank().getUserRankName().getId()==4) {
			ratioId = 3l;
		}
		if (ratioId!=4&&ratioId!=3) {
			Date date = CommUtil.formatDate(ApiUtils.getFirstday_Lastday(new Date(), 0, 30));
			if (danbaoUser.getLastLoginDate().after(date)||danbaoUser.getLoginDate().after(date)) {
				ratioId = 2l;
			}else {
				ratioId = 1l;
			}
		}
		InvitePartnerRewardRatio iprr = (InvitePartnerRewardRatio) this.commonService.getById("InvitePartnerRewardRatio", ratioId.toString());
		double money = appr.getPayMoney()*iprr.getScale();
		if (CommUtil.null2String(grant).equals("true")) {
			appr.setRewardStatus(true);
			appr.setFunds(money);
			boolean update = this.areaPartnerPayRecordService.update(appr);
			if (update) {
				User adminUser = this.userService.getObjById(1l);
				AllocateWagesUtils.allocateMoneyToUser(danbaoUser.getId().toString(), money, user.getUserName() + "购买" + appr.getBuMen().getName() + appr.getAreaGradeOfUser().getName() + "区域合伙人拓展经费", "",
						predepositLogService, userService, commonService, 1);
				AllocateWagesUtils.allocateMoneyToUser(adminUser.getId().toString(), -money, user.getUserName() + "购买" + appr.getBuMen().getName() + appr.getAreaGradeOfUser().getName() + "区域合伙人拓展经费,分成" + iprr.getPercentage(), "",
						predepositLogService, userService, commonService, 0);
				ApiUtils.json(response, "", "发放拓展经费成功", 0);
				return;
			}else {
				ApiUtils.json(response, "", "系统异常，发放拓展经费失败，请稍后重试", 0);
				return;
			}
		}else {
			AppTransferData appTransferData = new AppTransferData();
			appTransferData.setFirstData(danbaoUser);
			appTransferData.setSecondData(iprr);
			appTransferData.setThirdData(money);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class, "id,userName,photo"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			objs.add(new FilterObj(AppTransferData.class, "firstData,secondData,thirdData"));
			objs.add(new FilterObj(InvitePartnerRewardRatio.class, "id,typeName,scale,percentage"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, appTransferData, "success", 0, filter);
			return;
		}
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:区域合伙人查看对应区域职位的用户
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/appPartnerGetJobUser.htm", method = RequestMethod.POST)
	public void appPartnerGetJobUser(HttpServletRequest request,
			HttpServletResponse response,String partnerId) {
		if(ApiUtils.is_null(partnerId)){
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		AreaPartnerEntity areaPartner = this.areaPartnerEntityService
				.getObjById(CommUtil.null2Long(partnerId));
		if (areaPartner == null) {
			ApiUtils.json(response, "", "区域合伙人不存在", 1);
			return;
		}
		String hql = "select obj from User as obj where obj.bumen.id = " + areaPartner.getBuMen().getId() + " and obj.areaGradeOfUser.id = " + areaPartner.getArea().getId() +" and obj.zhiwei.id = " + areaPartner.getAreaSiteRankConfig().getZhiwei().getId();
		List<User> users = this.userService.query(hql, null, -1, -1);
		if (users.size()<=0) {
			ApiUtils.json(response, "", "该地区暂无"+areaPartner.getAreaSiteRankConfig().getZhiwei().getName(), 1);
			return;
		}
		User areaUser = users.get(0);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance,userRank"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(UserRank.class, "userRankName"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, areaUser, "查询用户成功", 0, filter);
	}
}
