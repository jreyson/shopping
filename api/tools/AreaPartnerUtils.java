package com.shopping.api.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageSwitchEntity;
import com.shopping.api.domain.regionPartner.AreaAppHomePayTemporary;
import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.domain.regionPartner.AreaHomePageConfig;
import com.shopping.api.domain.regionPartner.AreaPartnerEntity;
import com.shopping.api.domain.regionPartner.AreaPartnerPayRecord;
import com.shopping.api.domain.regionPartner.AreaSiteRankConfig;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

public class AreaPartnerUtils {
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:创建区域站点首页付费实体
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static void saveAreaAppHome(
			User user,
			AreaSiteRankConfig areaSiteRankConfig,
			AreaGradeOfUser area,
			BuMen bumen,
			ICommonService commonService,
			IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService,
			IPartnerFunctionService<AreaHomePageConfig> areaHomePageConfigService,
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService,
			IPartnerFunctionService<AreaSiteRankConfig> areaSiteRankConfigService) {
		String hql = "select obj from AreaPartnerEntity as obj where obj.area.id = "
				+ area.getId() + " and obj.buMen.id = " + bumen.getId();
		List<AreaPartnerEntity> list = areaPartnerEntityService.query(hql,
				null, -1, -1);
		AreaPartnerEntity areaPartnerEntity = null;
		if (list.size() > 0) {
			areaPartnerEntity = list.get(0);
		} else {
			hql = "select obj from AreaSiteRankConfig as obj where obj.areaRank = "
					+ area.getLevel();
			List<AreaSiteRankConfig> asrcs = areaSiteRankConfigService.query(
					hql, null, -1, -1);
			areaPartnerEntity = new AreaPartnerEntity(new Date(), null, area,
					null, asrcs.get(0));
			areaPartnerEntity.setBuMen(bumen);
			areaPartnerEntity.setDeleteStatus(false);
			boolean save = areaPartnerEntityService.save(areaPartnerEntity);
			if (save) {
				AppHomePageSwitchEntity ahpse = (AppHomePageSwitchEntity) commonService
						.getById("AppHomePageSwitchEntity", "1");
				AreaHomePageConfig areaHomePageConfig = new AreaHomePageConfig(
						new Date(), ahpse.getPayType(), ahpse.getMaxPayNum(),
						true, areaPartnerEntity);
				areaHomePageConfig.setDeleteStatus(false);
				boolean is_save = areaHomePageConfigService
						.save(areaHomePageConfig);
				if (is_save) {
					AreaPartnerUtils.establishCommonAndBanner(
							areaHomePageConfig, commonService,
							areaCommonpositionService,
							areaBannerpositionService);
				}
			}
		}
		if (user != null && areaSiteRankConfig != null) {
			Date year = ApiUtils.getYear(areaSiteRankConfig.getLengthOfTime());
			areaPartnerEntity.setExpireTime(year);
			areaPartnerEntity.setAreaSiteRankConfig(areaSiteRankConfig);
			areaPartnerEntity.setUser(user);
			areaPartnerEntityService.update(areaPartnerEntity);
		}
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:分发区域合伙人订单金额,公司账户与管理员各50%,暂时全部给管理员
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static void distributionOrderAmount(AreaPartnerPayRecord appr,
			ICommonService commonService,
			IPredepositLogService predepositLogService, IUserService userService) {
		AllocateWagesUtils.allocateMoneyToUser("1", appr.getPayMoney(), appr
				.getUser().getUserName()
				+ "购买"
				+ appr.getAreaGradeOfUser().getName() + "区域站点收入", "",
				predepositLogService, userService, commonService, 1);
//		AllocateWagesUtils.allocateMoneyToUser("1", appr.getPayMoney()/2, appr
//				.getUser().getUserName()
//				+ "购买"
//				+ appr.getAreaGradeOfUser().getName() + "区域站点收入", "",
//				predepositLogService, userService, commonService, 1);
	}
	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:创建区域站点首页付费轮播图与顺序位图
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static void establishCommonAndBanner(
			AreaHomePageConfig areaHomePageConfig,
			ICommonService commonService,
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService) {
		String hql = "select obj from AppHomePageCommonPosition as obj order by obj.id";
		@SuppressWarnings("unchecked")
		List<AppHomePageCommonPosition> appHomeCommon = commonService.query(
				hql, null, 0, 12);
		for (AppHomePageCommonPosition a : appHomeCommon) {
			AreaCommonposition common = new AreaCommonposition(new Date());
			common.setDeleteStatus(false);
			common.setAreaHomePageConfig(areaHomePageConfig);
			common.setCommonPosition_price(a.getCommonPosition_price());
			common.setDefaultGoods(a.getDefaultGoods());
			common.setIs_can_buy(true);
			common.setPosition_name(areaHomePageConfig.getRegionPartner()
					.getArea().getName()
					+ a.getPosition_name());
			common.setSequence(a.getSequence());
			areaCommonpositionService.save(common);
		}

		hql = "select obj from AppHomePageEntity as obj order by obj.id";
		@SuppressWarnings("unchecked")
		List<AppHomePageEntity> appHomeBanner = commonService.query(hql, null,
				0, 5);
		for (AppHomePageEntity a : appHomeBanner) {
			AreaBannerposition banner = new AreaBannerposition(new Date());
			banner.setDeleteStatus(false);
			banner.setAreaHomePageConfig(areaHomePageConfig);
			banner.setBanner_price(a.getBanner_price());
			banner.setDefaultGoods(a.getDefaultGoods());
			banner.setIs_can_buy(true);
			banner.setPosition_name(areaHomePageConfig.getRegionPartner()
					.getArea().getName()
					+ a.getPosition_name());
			banner.setSequence(a.getSequence());
			areaBannerpositionService.save(banner);
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:初始化已有的区域站点轮播图
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static void initiaAreaCommonAndBanner(
			AreaGradeOfUser area,BuMen bumen,
			ICommonService commonService,
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService) {
		String hql = "select obj from AppHomePageCommonPosition as obj order by obj.id";
		@SuppressWarnings("unchecked")
		List<AppHomePageCommonPosition> appHomeCommon = commonService.query(
				hql, null, 0, 12);
		hql = "select obj from AreaCommonposition as obj where obj.areaHomePageConfig.regionPartner.area.id = "
				+ area.getId() + " and obj.areaHomePageConfig.regionPartner.buMen.id = " + bumen.getId() + " order by obj.id";
		List<AreaCommonposition> acs = areaCommonpositionService.query(hql,
				null, -1, -1);
		if (acs.size() == appHomeCommon.size()) {
			int i = 0;
			for (AppHomePageCommonPosition a : appHomeCommon) {
				AreaCommonposition common = acs.get(i);
				common.setCommonPosition_price(a.getCommonPosition_price());
				common.setDefaultGoods(a.getDefaultGoods());
				areaCommonpositionService.save(common);
				i++;
			}
		} else {
			// 发送错误消息！！！！
			System.out.println("顺序图错误");
		}

		hql = "select obj from AppHomePageEntity as obj order by obj.id";
		@SuppressWarnings("unchecked")
		List<AppHomePageEntity> appHomeBanner = commonService.query(hql, null,
				0, 5);
		hql = "select obj from AreaBannerposition as obj where obj.areaHomePageConfig.regionPartner.area.id = "
				+ area.getId() + " and obj.areaHomePageConfig.regionPartner.buMen.id = " + bumen.getId() + " order by obj.id";
		;
		List<AreaBannerposition> abs = areaBannerpositionService.query(hql,
				null, -1, -1);
		if (abs.size() == appHomeBanner.size()) {
			int i = 0;
			for (AppHomePageEntity a : appHomeBanner) {
				AreaBannerposition banner = abs.get(i);
				banner.setBanner_price(a.getBanner_price());
				banner.setDefaultGoods(a.getDefaultGoods());
				areaBannerpositionService.save(banner);
				i++;
			}
		} else {
			// 发送错误消息！！！！
			System.out.println("轮播图错误");
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:初始化区域首页配置
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static boolean initiaArea(
			AreaGradeOfUser area,BuMen buMen,
			ICommonService commonService,
			IPartnerFunctionService<AreaHomePageConfig> areaHomePageConfigService) {
		AppHomePageSwitchEntity ahpse = (AppHomePageSwitchEntity) commonService
				.getById("AppHomePageSwitchEntity", "1");
		String hql = "select obj from AreaHomePageConfig as obj where obj.regionPartner.area.id = "
				+ area.getId() + " AND obj.regionPartner.buMen.id = " + buMen.getId();
		List<AreaHomePageConfig> query = areaHomePageConfigService.query(hql,
				null, -1, -1);
		if (query.size() > 0) {
			AreaHomePageConfig areaHomePageConfig = query.get(0);
			areaHomePageConfig.setMaxPayNum(ahpse.getMaxPayNum());
			areaHomePageConfig.setOpen(true);
			areaHomePageConfig.setPayType(ahpse.getPayType());
			return areaHomePageConfigService.update(areaHomePageConfig);
		}
		return false;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:复制区域合伙人旧配置到新的区域，用于管理员调换区域合伙人的地区
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static void backupsConfigToNewArea(
			AreaGradeOfUser area, BuMen bumen,
			AreaGradeOfUser newArea,BuMen newBumen,
			ICommonService commonService,
			IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService,
			IPartnerFunctionService<AreaHomePageConfig> areaHomePageConfigService,
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService) {
		String hql = "select obj from AreaPartnerEntity as obj where obj.area.id = "
				+ area.getId() + " and obj.buMen.id = " + bumen.getId();
		List<AreaPartnerEntity> apes = areaPartnerEntityService.query(hql,
				null, -1, -1);
		hql = "select obj from AreaPartnerEntity as obj where obj.area.id = "
				+ newArea.getId() + " and obj.buMen.id = " + newBumen.getId();
		List<AreaPartnerEntity> as = areaPartnerEntityService.query(hql, null,
				-1, -1);
		if (apes.size() > 0 && as.size() > 0) {
			AreaPartnerEntity areaPartner = apes.get(0);
			AreaPartnerEntity newAreaPartner = as.get(0);
			newAreaPartner.setExpireTime(areaPartner.getExpireTime());
			areaPartnerEntityService.update(newAreaPartner);

			hql = "select obj from AreaHomePageConfig as obj where obj.regionPartner.id = "
					+ areaPartner.getId();
			List<AreaHomePageConfig> ahpcs = areaHomePageConfigService.query(
					hql, null, -1, -1);
			hql = "select obj from AreaHomePageConfig as obj where obj.regionPartner.id = "
					+ newAreaPartner.getId();
			List<AreaHomePageConfig> acs = areaHomePageConfigService.query(hql,
					null, -1, -1);
			AreaHomePageConfig areaHomePageConfig = ahpcs.get(0);
			AreaHomePageConfig newAreaHomePageConfig = acs.get(0);
			newAreaHomePageConfig.setMaxPayNum(areaHomePageConfig
					.getMaxPayNum());
			newAreaHomePageConfig.setOpen(areaHomePageConfig.isOpen());
			newAreaHomePageConfig.setPayType(areaHomePageConfig.getPayType());
			areaHomePageConfigService.update(newAreaHomePageConfig);
			AreaPartnerUtils.initiaArea(area,bumen, commonService,
					areaHomePageConfigService);

			hql = "select obj from AreaBannerposition as obj where obj.areaHomePageConfig.id = "
					+ areaHomePageConfig.getId() + " order by obj.id";
			List<AreaBannerposition> abps = areaBannerpositionService.query(
					hql, null, -1, -1);
			hql = "select obj from AreaBannerposition as obj where obj.areaHomePageConfig.id = "
					+ newAreaHomePageConfig.getId() + " order by obj.id";
			List<AreaBannerposition> abs = areaBannerpositionService.query(hql,
					null, -1, -1);
			if (abps.size() == abs.size()) {
				for (int i = 0; i < abps.size(); i++) {
					AreaBannerposition b = abps.get(i);
					AreaBannerposition a = abs.get(i);
					a.setDefaultGoods(b.getDefaultGoods());
					a.setBanner_price(b.getBanner_price());
					areaBannerpositionService.update(a);
				}
			}

			hql = "select obj from AreaCommonposition as obj where obj.areaHomePageConfig.id = "
					+ areaHomePageConfig.getId() + " order by obj.id";
			List<AreaCommonposition> acps = areaCommonpositionService.query(
					hql, null, -1, -1);
			hql = "select obj from AreaCommonposition as obj where obj.areaHomePageConfig.id = "
					+ newAreaHomePageConfig.getId() + " order by obj.id";
			List<AreaCommonposition> newAcps = areaCommonpositionService.query(
					hql, null, -1, -1);
			if (acps.size() == newAcps.size()) {
				for (int i = 0; i < acps.size(); i++) {
					AreaCommonposition ac = acps.get(i);
					AreaCommonposition newAc = newAcps.get(i);
					newAc.setDefaultGoods(ac.getDefaultGoods());
					newAc.setCommonPosition_price(ac.getCommonPosition_price());
					areaCommonpositionService.update(newAc);
				}
			}
			AreaPartnerUtils.initiaAreaCommonAndBanner(area,bumen, commonService,
					areaCommonpositionService, areaBannerpositionService);
		}
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:payType:balance 积分支付； alipay 支付宝支付；weixin 微信支付
	 * @description:
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static boolean appPaymentAreaPlace(
			String payType,
			AreaAppHomePayTemporary aahpt,
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService,
			IUserService userService,
			ICommonService commonService,
			IPartnerFunctionService<AreaAppHomePayTemporary> areaAppHomePayTemporaryService,
			IPredepositLogService predepositLogService) {
		boolean is = false;
		aahpt.setOrderStatus(20);
		aahpt.setPayTime(new Date());
		aahpt.setPayType(payType);
		boolean update = areaAppHomePayTemporaryService.update(aahpt);
		if (!update) {
			return is;
		}
		AreaHomePageConfig areaHomePageConfig = null;
		String explain = "";
		if ("banner".equals(aahpt.getVacantPositionType())) {
			AreaBannerposition areaBannerposition = aahpt
					.getAreaBannerposition();
			if (areaBannerposition != null
					&& areaBannerposition.getGoods() == null) {
				areaBannerposition.setGoods(aahpt.getGoods());
				areaBannerposition.setFlush_time(aahpt.getFlush_time());
				areaBannerposition.setIs_can_buy(false);
				areaBannerposition.setPurchase_timeDuan(aahpt
						.getPurchase_timeDuan());
				areaBannerposition.setStart_time(aahpt.getStart_time());
				is = areaBannerpositionService.update(areaBannerposition);
				areaHomePageConfig = areaBannerposition.getAreaHomePageConfig();
				explain = areaBannerposition.getPosition_name() + "空位收入";
			}
		} else {
			AreaCommonposition areaCommonposition = aahpt
					.getAreaCommonposition();
			if (areaCommonposition != null
					&& areaCommonposition.getGoods() == null) {
				areaCommonposition.setGoods(aahpt.getGoods());
				areaCommonposition.setFlush_time(aahpt.getFlush_time());
				areaCommonposition.setIs_can_buy(false);
				areaCommonposition.setPurchase_timeDuan(aahpt
						.getPurchase_timeDuan());
				areaCommonposition.setStart_time(aahpt.getStart_time());
				is = areaCommonpositionService.update(areaCommonposition);
				areaHomePageConfig = areaCommonposition.getAreaHomePageConfig();
				explain = areaCommonposition.getPosition_name() + "空位收入";
			}
		}
		if (is) {
			// 将钱分发给区域合伙人，如果区域合伙人不存在则给管理员
			User user = userService.getObjById(1l);
			AreaPartnerEntity regionPartner = areaHomePageConfig
					.getRegionPartner();
			if (areaHomePageConfig != null && regionPartner.getUser() != null
					&& regionPartner.getExpireTime().after(new Date())) {
				user = regionPartner.getUser();
			}
			AllocateWagesUtils.allocateMoneyToUser(user.getId() + "",
					aahpt.getTotal(), explain, "", predepositLogService,
					userService, commonService,
					CommUtil.null2Int(aahpt.getTotal()));
			aahpt.setPartnerUser(user);
			aahpt.setAreaHomePageConfig(areaHomePageConfig);
			areaAppHomePayTemporaryService.update(aahpt);
		}
		return is;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @function:**
	 * @description:格式化起始时间与结束时间，返回两个时间之间的天数
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static Map<String, Object> getToolsData(String beginTime,
			String endTime, String currentPage) {
		SimpleDateFormat sft = new SimpleDateFormat("yyyy-MM-dd");
		if (beginTime != null || endTime != null) {
			if (beginTime == null) {
				String newTime = sft.format(new Date());
				long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
				if (days < 0) {
					endTime = newTime;
				}
				try {
					beginTime = ApiUtils.getFirstday_Lastday(
							sft.parse(endTime), 0, 0);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (endTime == null) {
				try {
					endTime = ApiUtils.getFirstday_Lastday(
							sft.parse(beginTime), 1, 0);
					String newTime = sft.format(new Date());
					long days = ApiUtils.acquisitionTimeSegment(endTime,
							newTime);
					if (days < 0) {
						endTime = newTime;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				String newTime = sft.format(new Date());
				long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
				if (days < 0) {
					endTime = newTime;
				}
			}
		} else {
			Date date = new Date();
			beginTime = ApiUtils.getFirstday_Lastday(date, 0, 0);
			endTime = sft.format(date);
		}
		int current_page = 0;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		String begin_Time = ApiUtils.weeHours(beginTime, 0);
		String end_Time = ApiUtils.weeHours(endTime, 1);
		Map<String, Object> map = new HashMap<String, Object>();
		long days = ApiUtils.acquisitionTimeSegment(beginTime, endTime);
		map.put("beginTime", begin_Time);
		map.put("endTime", end_Time);
		map.put("days", days);
		map.put("current_page", current_page);
		return map;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:返回某个区域的轮播图和顺序位图，如果该区域未被购买，则直接返回首页数据
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static Map<String, Object> getAreaAppHomeData(
			IPartnerFunctionService<AreaCommonposition> areaCommonpositionService,
			ICommonService commonService,
			IPartnerFunctionService<AreaBannerposition> areaBannerpositionService,
			IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService,
			String areaId, String bumenId) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (!CommUtil.null2String(areaId).equals("")
				&& !CommUtil.null2String(bumenId).equals("")) {
			String hql = "select obj from AreaPartnerEntity as obj where obj.area.id = "
					+ areaId + " and obj.buMen.id = " + bumenId;
			List<AreaPartnerEntity> query = areaPartnerEntityService.query(hql,
					null, -1, -1);
			if (query.size() > 0) {
				AreaPartnerEntity areaPartnerEntity = query.get(0);
				// 有区域合伙人
				hql = "select obj from AreaBannerposition as obj where obj.areaHomePageConfig.regionPartner.id = "
						+ areaPartnerEntity.getId();
				List<AreaBannerposition> abs = areaBannerpositionService.query(
						hql, null, -1, -1);
				hql = "select obj from AreaCommonposition as obj where obj.areaHomePageConfig.regionPartner.id = "
						+ areaPartnerEntity.getId();
				List<AreaCommonposition> acs = areaCommonpositionService.query(
						hql, null, -1, -1);
				if (abs.size() > 0 && acs.size() > 0) {
					map.put("abs", abs);
					map.put("acs", acs);
				}
			} else {
				ApiUtils.asynchronousUrl(SystemResPath.hostAddr
						+ "/appTriggerAreaHomePageData.htm?areaId=" + areaId
						+ "&&bumenId=" + bumenId, "GET");
			}
		}
		if (map.isEmpty()) {
			String carouselHql = "select obj from AppHomePageEntity as obj";
			String commonHql = "select obj from AppHomePageCommonPosition as obj";
			@SuppressWarnings("unchecked")
			List<AppHomePageEntity> abs = commonService.query(carouselHql,
					null, -1, -1);
			@SuppressWarnings("unchecked")
			List<AppHomePageCommonPosition> acs = commonService.query(
					commonHql, null, -1, -1);
			map.put("abs", abs);
			map.put("acs", acs);
		}
		return map;
	}

	/***
	 * @author:gaohao
	 * @param **
	 * @return:void
	 * @param:**
	 * @description:查询首页轮播图，顺序图有无重复商品
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	public static boolean judgeIsRepetition(String goodsId, String classType,
			ICommonService commonService) {
		boolean ret = false;
		String judgeHql = "select obj from " + classType
				+ " as obj where obj.goods.id=" + goodsId;
		List<?> appJudgeList = commonService.query(judgeHql, null, -1, -1);
		if (appJudgeList.size() > 0) {
			ret = true;
		}
		return ret;
	}
}
