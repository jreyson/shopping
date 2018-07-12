package com.shopping.api.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.AddressApi;
import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.CenterListApi;
import com.shopping.api.domain.FavoriteApi;
import com.shopping.api.domain.GoodsApi;
import com.shopping.api.domain.RespApi;
import com.shopping.api.domain.UserApi;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.browseRecords.UserBrowseRecords;
import com.shopping.api.domain.evaluate.AppraiseMessageEntity;
import com.shopping.api.domain.evaluate.AssessingDiscourseEntity;
import com.shopping.api.domain.evaluate.StartsExplainEntity;
import com.shopping.api.domain.evaluate.StoreEvaluteEntity;
import com.shopping.api.domain.evaluate.VVPResourceEntity;
import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.domain.regionPartner.AreaPartnerEntity;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.HomePageData;
import com.shopping.api.service.IAppHomePageService;
import com.shopping.api.service.IGoodsApiService;
import com.shopping.api.service.common.IUniversalService;
import com.shopping.api.service.evaluate.IEvaluateFunctionService;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.api.tools.ALIWapPayTools;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.GroupGoods;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.SpecifiList;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.Xianji;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IFavoriteService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.pay.weixin.WxIndex;
import com.shopping.pay.weixin.WxPayDto;
import com.shopping.pay.weixin.utils.GetWxOrderno;
import com.shopping.pay.weixin.utils.RequestHandler;
import com.shopping.view.web.tools.SpecTools;

@Controller
public class TestApiAction {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	@Qualifier("startsExplain")
	private IEvaluateFunctionService<StartsExplainEntity> startsExplainService;
	@Autowired
	@Qualifier("appraiseMessage")
	private IEvaluateFunctionService<AppraiseMessageEntity> appraiseMessageService;
	@Autowired
	@Qualifier("assessingDiscourse")
	private IEvaluateFunctionService<AssessingDiscourseEntity> assessingDiscourseService;
	@Autowired
	@Qualifier("vVPResource")
	private IEvaluateFunctionService<VVPResourceEntity> vVPResourceService;
	@Autowired
	@Qualifier("storeEvalute")
	private IEvaluateFunctionService<StoreEvaluteEntity> storeEvaluteService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IAppHomePageService apphomeService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IUserService userService;
	@Autowired
	@Qualifier("areaCommonpositionServiceImpl")
	private IPartnerFunctionService<AreaCommonposition> areaCommonpositionService;
	@Autowired
	@Qualifier("areaBannerpositionServiceImpl")
	private IPartnerFunctionService<AreaBannerposition> areaBannerpositionService;
	@Autowired
	@Qualifier("areaPartnerEntityServiceImpl")
	private IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService;
	@RequestMapping({ "/api_test.htm" })
	public ModelAndView api_add_cart(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("test.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}

	@RequestMapping({ "/apple_review.htm" })
	public ModelAndView apple_review(HttpServletRequest request,
			HttpServletResponse response, String user_id) throws IOException {
		ModelAndView mv = new JModelAndView("apple_review.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}

	@RequestMapping({ "/android_review.htm" })
	public ModelAndView android_review(HttpServletRequest request,
			HttpServletResponse response, String user_id) {
		ModelAndView mv = new JModelAndView("android_review.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}

	@RequestMapping({ "/app_h5_register.htm" })
	public ModelAndView app_h5_register(HttpServletRequest request,
			HttpServletResponse response) {

		/*
		 * 生成关于店铺的二维码
		 * System.out.println(request.getSession().getServletContext()
		 * .getRealPath("/"));
		 * request.getSession.getServletContent.getRealPath("/"
		 * )得到该servlet所在项目的物理地址 取出了该项目对应的tomact的根路径,一个项目的servlet都集中管理在web应用下面
		 * request
		 * .getContextPath()返回webapps下web应用的根目录名,如果是root默认项目返回"/",不是根目录返回该项目
		 * ("/shopping"),得到该servlet所在项目的项目名
		 * request.getRealpath("/")得到的是实际的物理路径，也就是你的项目所在服务器中的路径
		 * request.getScheme() 等到的是协议名称，默认是http request.getServerName()
		 * 获取到的是本次请求中的域名 request.getServerPort() 得到的是服务器的配置文件中配置的端口号 比如 8080等等
		 */
		/*
		 * System.out.println("request.getContextPath()"+request.getContextPath()
		 * ); System.out.println("项目的绝对地址是"+request.getRealPath("/"));
		 * System.out
		 * .println("request.getServerName()"+request.getServerName());
		 * System.out
		 * .println("request.getServerPort()"+request.getServerPort());
		 * System.out.println("request.getScheme()"+request.getScheme());
		 * System.
		 * out.println("项目的绝对地址是"+request.getSession().getServletContext()
		 * .getRealPath(""));
		 */
		System.out.println("tomcat_ecplise的sessionId是"
				+ request.getSession().getId());
		// System.out.println("tomcat_test的sessionId是"+request.getSession().getId());
		ModelAndView mv = new JModelAndView("regist_wap.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}

	/***
	 * @author:akangah
	 * @return:void
	 * @param:recNum:电话号码,接受者的电话号码
	 * @description:获取短信验证码
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/get_pcSafe_code.htm", method = RequestMethod.POST)
	public void get_pcSafe_code(HttpServletRequest request,
			HttpServletResponse response, String recNum) {
		String safeCode = CommUtil.get_fixation_length(6);
		String templateId = "SMS_104605007";// 阿里大于pc端用户注册时的短信模板ID
		JSONObject obj = new JSONObject();
		obj.put("code", safeCode);
		boolean ret = CommUtil.sendNote(safeCode, obj, recNum, templateId);
		if (ret) {
			JSONObject retobj = new JSONObject();
			retobj.put("safeCode", safeCode);
			retobj.put("recNum", recNum);
			System.out.println(retobj.toJSONString());
			System.out.println(safeCode);
			HttpSession session = request.getSession();
			session.setAttribute("safeCode", safeCode);
			session.setAttribute("safePhone", recNum);
			ApiUtils.json(response, "", "推送成功", 0);
			return;
		} else {
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}
	}

	public void write_consoleData(HttpServletRequest request) {
		// String
		// path=request.getSession().getServletContext().getRealPath("/")+
		String path = SystemResPath.imgUploadUrl + File.separator + "log"
				+ File.separator + "log.txt";
		File myFilePath = new File(path);
		boolean ret = false;
		if ((!myFilePath.exists()) && (!myFilePath.isDirectory())) {
			ret = myFilePath.mkdirs();
			if (!ret) {
				System.out.println("创建文件夹出错");
			}
		}
	}

	@RequestMapping(value = "/get_pcSafe_code.action", method = RequestMethod.POST)
	public void ahhs() {
		float a = 10F;
		float aj = a / 0;
		System.out.println(aj);
	}

	@RequestMapping(value = "/testServices.htm", method = RequestMethod.POST)
	public void testServices(HttpServletRequest request,
			HttpServletResponse response) {
		// StartsExplainEntity startsExplain=this.startsExplain.getObjById(1L);
		// System.out.println(startsExplain.getId());
		AppraiseMessageEntity appraiseMessageEntity = new AppraiseMessageEntity();
		appraiseMessageEntity.setAddTime(new Date());
		this.appraiseMessageService.save(appraiseMessageEntity);
	}

	public static void main(String[] args) {
		double a = 19.99;
		double b = 405.80;
		double sum = 0.0D;
		for (int i = 0; i < 20; i++) {
			sum += a;
		}
		sum = sum + 6;
		System.out.println(sum == 6 ? "相等" : "不想等");
	}

	@RequestMapping(value = "/testCertificate.htm", method = RequestMethod.POST)
	public void testCertificate(HttpServletRequest request,
			HttpServletResponse response) {
		String mch_id = "1463602702";
		String nonce_str = WxIndex.getNonceStr();
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.setKey("jrkBv2XBhBofyouKIrNEaEVopHkOHClz");
		String sign = reqHandler.createSign(packageParams);
		String request_url = "https://apitest.mch.weixin.qq.com/sandboxnew/pay/getsignkey";
		String xmlParam = "<xml>" + "<mch_id>" + mch_id + "</mch_id>"
				+ "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign
				+ "</sign>" + "</xml>";
		Map<String, String> prepayMap = GetWxOrderno.getPayNo(request_url,
				xmlParam);
	}

	@RequestMapping(value = "/testSaveService.htm", method = RequestMethod.POST)
	public void testSaveService(HttpServletRequest request,
			HttpServletResponse response) {
		/*
		 * PredepositLog xian_ji_log = new PredepositLog();
		 * xian_ji_log.setAddTime(new Date()); xian_ji_log.setPd_log_user(null);
		 * xian_ji_log.setPd_op_type("增加");
		 * xian_ji_log.setPd_log_amount(BigDecimal.valueOf(43D));
		 * xian_ji_log.setPd_log_info("订单" + 399 + "衔级金");
		 * xian_ji_log.setPd_type("可用预存款"); xian_ji_log.setCurrent_price(89);
		 * this.predepositLogService.save(xian_ji_log);
		 */
		PredepositLog test = this.predepositLogService.getObjById(643438L);
		test.setCurrent_price(198);
		test.setPd_log_info("test");
		this.predepositLogService.save(test);
	}

	@RequestMapping(value = "/invalidate.htm", method = RequestMethod.POST)
	public void invalidateSession(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().invalidate();
	}

	@RequestMapping(value = "/testWeChatPay.htm")
	public void testWeChatPay(HttpServletRequest request,
			HttpServletResponse response) {
		ApiUtils.app_h5WeiXin_payment("84776743342247", "www.d1sc.com", "8838",
				request);
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:获取app首页的数据,首页新接口
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getHomePageDates.htm", method = RequestMethod.POST)
	public void getHomePageDate(HttpServletRequest request,
			HttpServletResponse response, String userId, String password,
			String currentPage,String areaId,String bumenId) {
		int current_page = 0;
		int pageSize = 4;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			pageSize = 10;
			current_page = Integer.valueOf(currentPage).intValue();
		}
		// 格言
		User user = null;
		Long uid = CommUtil.null2Long(userId);
		if (uid != -1) {
			user = this.userService.getObjById(uid);
		}
		if (CommUtil.null2String(bumenId).equals("")) {
			bumenId = "301";
			if (user!=null) {
				bumenId = user.getBumen().getId()+"";
			}
		}
		HomePageData homePageData = new HomePageData();
		List<AppTransferData> modularDate = new ArrayList<>();
		AppTransferData appTransferData = new AppTransferData();
		String hql = "";
		List<AppHomePageEntity> homePageCarousel = null;
		List<AppHomePageCommonPosition> homePageComm = null;
		if (current_page == 0) {
			if (CommUtil.null2Long(areaId)!=-1) {
				Map<String, Object> areaAppHomeData = AreaPartnerUtils.getAreaAppHomeData(areaCommonpositionService, commonService, areaBannerpositionService, areaPartnerEntityService, areaId,bumenId);
				homePageCarousel=(List<AppHomePageEntity>) areaAppHomeData.get("abs");
				homePageComm=(List<AppHomePageCommonPosition>) areaAppHomeData.get("acs");
			}else {
				String carouselHql = "select obj from AppHomePageEntity as obj";
				String commonHql = "select obj from AppHomePageCommonPosition as obj";
				homePageCarousel = this.apphomeService
						.query(carouselHql, null, -1, -1);
				homePageComm = this.commonService
						.query(commonHql, null, -1, -1);
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String begin = sdf.format(new Date(System.currentTimeMillis()));
			String end = sdf.format(new Date(System.currentTimeMillis()
					+ (86400 * 1000)));
			String sql_str ="SELECT "+
					  "temp4.name2, "+
					 "temp4.payTimes, "+
					  "temp4.userName, "+
					  "temp4.id, "+
					  "temp4.daogou_get_price, "+
					  "temp4.totalPrice, "+
					 "temp4.orderId, "+
					  "temp4.name3 , "+
					  "temp4.zhiweiName, "+
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
					  "ez.name as zhiweiName from  ecm_zhiwei as ez right join "+
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
		              "temp2.zhiwei_id "+
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
		              "temp.zhiwei_id "+
					"FROM ecm_bumen AS eb "+
					  "RIGHT JOIN (SELECT "+
					               "DATE_FORMAT(so.payTimes,'%Y-%m-%d %H:%i:%s') AS payTimes, so.id as orderId, "+
					               "su.areaGradeOfUser_id, "+
					               "su.photo_id, "+
					               "su.zhiwei_id, "+
					               "su.bumen_id, "+
					               "su.userName, "+
					               "su.id, "+
					               "so.daogou_get_price, "+
					               "so.totalPrice "+
					             "FROM shopping_user AS su "+
					               "LEFT JOIN shopping_orderform AS so "+
					                 "ON so.user_id = su.id "+
					             "WHERE so.order_status IN(20,30,40,50,60) "+
					                 "AND so.payTimes > '"+begin+"' "+
					                 "AND so.payTimes < '"+end+"' "+
					             "ORDER BY so.payTimes DESC "+
					             "LIMIT 0,20 ) AS temp "+
					    "ON temp.bumen_id = eb.id "+
					             ") as temp2 "+
					     "on temp2.areaGradeOfUser_id=sagou.id"
					             +") as temp3 "+
					     "on temp3.zhiwei_id=ez.id"
					             +") as temp4 on temp4.photo_id=sa.id";
			List<?> xibao = this.commonService.executeNativeNamedQuery(sql_str);
			// app首页共享商品数量
			String sharedGoodsNum_sql = "select count(obj) from Goods as obj where obj.deleteStatus=false and obj.goods_status <> 1 and obj.goods_store.store_status = 2 and obj.goods_inventory > 0";
			List<?> sharedGoodsNum_list = this.goodsService.query(
					sharedGoodsNum_sql, null, -1, -1);
			String sharedGoodsNum = "";
			if (sharedGoodsNum_list.size() > 0) {
				sharedGoodsNum = sharedGoodsNum_list.get(0) + "";
			}

			// 销量排行
			List<?> appSalesVolumeGoods = this.getAppSalesVolumeGoods(begin, 0,
					3, 1);
			// 每日推荐
			appTransferData.setFirstData("每日推荐");
			appTransferData.setSecondData(homePageComm);
			homePageData.setDailyRecommend(appTransferData);

			// 商品上新
			hql = "select obj from Goods AS obj where obj.deleteStatus=false and obj.goods_status = 0 and obj.goods_store.store_status = 2 order by obj.addTime DESC";
			List<Goods> goods = this.goodsService.query(hql, null, 0, 4);
			appTransferData = new AppTransferData();
			appTransferData.setFirstData("新品上新");
			appTransferData.setSecondData(goods);
			modularDate.add(appTransferData);

			// 战略合作
			hql = "SELECT " + "sg.id " + "FROM " + "shopping_goods AS sg "
					+ "LEFT JOIN shopping_store AS ss "
					+ "ON sg.goods_store_id = ss.id  "
					+ "WHERE sg.zhanlue_price > 0 "
					+ "AND sg.goods_inventory > 0  "
					+ "AND sg.deleteStatus=false "
					+ "AND ss.store_status = 2 " + "AND sg.goods_status = 0 "
					+ "ORDER BY RAND() " + "LIMIT 4 ";
			List<?> query = this.commonService.executeNativeNamedQuery(hql);
			if (query.size() > 0) {
				String ids = query.toString();
				ids = ids.substring(1, ids.length() - 1);
				hql = "select obj from Goods as obj where obj.id in (" + ids
						+ ") order by obj.zhanlue_price desc";
				List<Goods> tacticCooperation = this.goodsService.query(hql,
						null, 0, 4);
				appTransferData = new AppTransferData();
				appTransferData.setFirstData("战略合作");
				appTransferData.setSecondData(tacticCooperation);
				modularDate.add(appTransferData);
			}
			homePageData.setHomePageCarousel(homePageCarousel);
			homePageData.setXibao(xibao);
			homePageData.setSharedGoodsNum(sharedGoodsNum);
			homePageData.setUser(user);
			appTransferData = new AppTransferData();
			appTransferData.setFirstData("销量排行榜");
			appTransferData.setSecondData(appSalesVolumeGoods);
			homePageData.setSalesRank(appTransferData);
		}

		// 猜你喜欢
		List<Goods> serBrowseRecords = new ArrayList<>();
		if (CommUtil.null2Long(userId) != -1) {
			hql = "SELECT sg FROM UserBrowseRecords as obj right join obj.goods as sg where obj.goods.deleteStatus=false and obj.goods.goods_status=0 and obj.goods.goods_store.store_status=2 and obj.goods.goods_inventory > 0 and obj.user.id = "
					+ CommUtil.null2Long(userId) + " ORDER BY obj.lastAccessTime DESC";
			serBrowseRecords = this.goodsService.query(hql, null, current_page
					* pageSize - 6, pageSize);
		}
		if (serBrowseRecords.size() < pageSize) {
			int num = pageSize - serBrowseRecords.size();
			hql = "select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_store.store_status=2 and obj.goods_inventory > 0 order by obj.goods_salenum DESC";
			List<Goods> list = goodsService.query(hql, null, current_page
					* pageSize, num);
			serBrowseRecords.addAll(list);
		}
		appTransferData = new AppTransferData();
		appTransferData.setFirstData("猜你喜欢");
		appTransferData.setSecondData(serBrowseRecords);
		modularDate.add(appTransferData);
		homePageData.setModularDate(modularDate);

		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				HomePageData.class,
				"homePageCarousel,xibao,sharedGoodsNum,modularDate,user,salesRank,dailyRecommend"));
		objs.add(new FilterObj(AppHomePageEntity.class,
				"goods,sequence,defaultGoods,id,position_name,purchase_timeDuan"));
		objs.add(new FilterObj(AppHomePageCommonPosition.class,
				"goods,sequence,defaultGoods,id,position_name,purchase_timeDuan"));
		objs.add(new FilterObj(
				Goods.class,
				"id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		objs.add(new FilterObj(GoodsClass.class,
				"id,className,sequence,level,icon_acc,parent"));
		objs.add(new FilterObj(User.class, "photo,motto"));
		objs.add(new FilterObj(AreaBannerposition.class, "id,is_can_buy,banner_price,position_name,defaultGoods,goods"));
		objs.add(new FilterObj(AreaCommonposition.class, "id,is_can_buy,commonPosition_price,position_name,defaultGoods,goods"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, homePageData, "查询成功", 0, filter);
		return;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:首页销量排行，如果当天销量不足三件商品则会递归查询昨天商品销量
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	private List<?> getAppSalesVolumeGoods(String time, int current_page,
			int pageSize, int i) {
		i++;
		if (i > 3) {
			return new ArrayList<>();
		}
		String sql = "SELECT " + "temp2.goods_id," + "temp2.goods_name,"
				+ "temp2.num," + "temp2.store_price," + "sa.path,sa.name,"
				+ "sa.ext " + "FROM shopping_accessory AS sa " + "RIGHT JOIN "
				+ "(" + "SELECT " + "temp.goods_id," + "temp.num,"
				+ "sg.goods_name," + "sg.store_price,"
				+ "sg.goods_main_photo_id " + "FROM shopping_goods AS sg,"
				+ "(" + "SELECT sg.goods_id," + "SUM(sg.count) AS num "
				+ "FROM shopping_goodscart AS sg,"
				+ "shopping_orderform AS so "
				+ "WHERE so.order_status IN (20,30,40,50,60) "
				+ "AND so.id=sg.of_id " + "AND so.addTime>'" + time + "' "
				+ "GROUP BY sg.goods_id " + "ORDER BY num DESC LIMIT "
				+ current_page * pageSize + "," + pageSize
				+ ") AS temp WHERE temp.goods_id=sg.id and sg.deleteStatus=false " 
				+ ") AS temp2 ON temp2.goods_main_photo_id=sa.id";
		List<?> query = this.commonService.executeNativeNamedQuery(sql);
		if (query.size() < 3) {
			return this
					.getAppSalesVolumeGoods(
							ApiUtils.getFirstday_Lastday(
									CommUtil.formatDate(time), 0, 1),
							current_page, pageSize, i);
		}
		return query;
	}

	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:获取战略合作的商品
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/getTacticCooperateGoodss.htm", method = RequestMethod.POST)
	public void getTacticCooperateGoodss(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		int current_page = 0;
		int pageSize = 20;
		if ("".equals(currentPage) || currentPage == null) {
			current_page = 0;
		} else {
			current_page = Integer.valueOf(currentPage).intValue();
		}
		String hql = "select obj from Goods as obj where obj.deleteStatus=false and obj.zhanlue_price > 0 order by obj.addTime DESC";
		List<Goods> tacticCooperation = this.goodsService.query(hql, null,
				current_page * pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				Goods.class,
				"id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		objs.add(new FilterObj(GoodsClass.class,
				"id,className,sequence,level,icon_acc,parent"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, tacticCooperation, "success", 0, filter);
		return;
	}
	@RequestMapping(value = "/testUserMoney.htm", method = RequestMethod.POST)
	public void testUserMoney(HttpServletRequest request,
			HttpServletResponse response) {
		User user=this.userService.getObjById(1L);
		System.out.println(user.getAvailableBalance());
		AllocateWagesUtils.allocateMoneyToUser(1+"", 100D, "yanglao", "yanglao", predepositLogService, userService, commonService, 2);
		System.out.println(user.getAvailableBalance());
	}
}
