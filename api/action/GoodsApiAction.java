package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.shopping.api.domain.FavoriteApi;
import com.shopping.api.domain.GoodsApi;
import com.shopping.api.domain.PaymentWayVariety;
import com.shopping.api.domain.RespApi;
import com.shopping.api.domain.UserSelfPaymentDefault;
import com.shopping.api.domain.evaluate.AppraiseMessageEntity;
import com.shopping.api.domain.evaluate.AssessingDiscourseEntity;
import com.shopping.api.domain.evaluate.StartsExplainEntity;
import com.shopping.api.domain.evaluate.StoreScoreEntity;
import com.shopping.api.output.AppEvaluateData;
import com.shopping.api.service.IGoodsApiService;
import com.shopping.api.service.evaluate.IEvaluateFunctionService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.FenPei;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.SpecifiList;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IFenPeiService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreClassService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.view.web.tools.SpecTools;

@Controller
public class GoodsApiAction {
	@Autowired
	private IFenPeiService fenPeiService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsApiService goodsApiService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	IUserService userService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	ICommonService commonService;
	@Autowired
	private IStoreClassService storeClassService;
	@Autowired
	IRoleService roleService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	@Qualifier("appraiseMessage")
	private IEvaluateFunctionService<AppraiseMessageEntity> appraiseMessageService;
	@Autowired
	@Qualifier("storeScore")
	private IEvaluateFunctionService<StoreScoreEntity> storeScoreService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端获取用户默认的支付方式的列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_acquire_paymentList.htm"})
	public void  app_acquire_paymentList(HttpServletRequest request, 
			HttpServletResponse response,Long userId){
		User user=this.userService.getObjById(userId);
		if(user!=null){
			if(user.getUserSelfPaymentDefaultList().size()==0){
				String hql="select obj from PaymentWayVariety as obj";
				List<?> paymentWayVarietyList=this.commonService.query(hql, null, -1,-1);
				for(Object obj:paymentWayVarietyList){
					PaymentWayVariety paymentWayVariety=(PaymentWayVariety) obj;
					UserSelfPaymentDefault userSelfPaymentDefault=null;
					if(paymentWayVariety.getId().longValue()==1){
						userSelfPaymentDefault=new UserSelfPaymentDefault(new Date(), 1, user, paymentWayVariety);
					}else{
						userSelfPaymentDefault=new UserSelfPaymentDefault(new Date(), 0, user, paymentWayVariety);
					}
					this.commonService.save(userSelfPaymentDefault);
				}
			}
			String querySelfAllPaymentHql="select obj from UserSelfPaymentDefault as obj where obj.user.id="+userId+"order by obj.is_default desc";
			List<?> querySelfAllPaymentHqlList=this.commonService.query(querySelfAllPaymentHql, null, -1,-1);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(UserSelfPaymentDefault.class,
					"is_default,id,paymentWayVariety"));
			objs.add(new FilterObj(PaymentWayVariety.class,
					"iconUrl,id,paymentName"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, querySelfAllPaymentHqlList, "查询成功", 0, filter);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:更新app端用户默认的支付方式
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/app_update_slefPaymentList.htm"})
	public void app_update_slefPaymentList(HttpServletRequest request, 
			HttpServletResponse response,Long userSelfPaymentDefaultId,
			Long userId){
		String querySelfPaymentHql="select obj from UserSelfPaymentDefault as obj where obj.user.id="+userId;
		List<?> querySelfPaymentList=this.commonService.query(querySelfPaymentHql, null, -1,-1);
		for(Object obj:querySelfPaymentList){
			UserSelfPaymentDefault userSelfPaymentDefault=(UserSelfPaymentDefault) obj;
			if(userSelfPaymentDefault.getIs_default()==1){
				userSelfPaymentDefault.setIs_default(0);
				this.commonService.update(userSelfPaymentDefault);
				break;
			}
		}
		String queryUniquePayment="select obj from UserSelfPaymentDefault as obj where obj.id="+userSelfPaymentDefaultId;
		List<?> queryUniquePaymentList=this.commonService.query(queryUniquePayment, null, -1,-1);
		UserSelfPaymentDefault userUniquePaymentDefault=(UserSelfPaymentDefault) queryUniquePaymentList.get(0);
		if(userUniquePaymentDefault!=null){
			userUniquePaymentDefault.setIs_default(1);
			this.commonService.update(userUniquePaymentDefault);
			String querySelfAllPaymentHql="select obj from UserSelfPaymentDefault as obj where obj.user.id="+userId+"order by obj.is_default desc";
			List<?> querySelfAllPaymentHqlList=this.commonService.query(querySelfAllPaymentHql, null, -1,-1);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(UserSelfPaymentDefault.class,
					"is_default,id,paymentWayVariety"));
			objs.add(new FilterObj(PaymentWayVariety.class,
					"iconUrl,id,paymentName"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, querySelfAllPaymentHqlList, "查询成功", 0, filter);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:json格式化的一些具体用法
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_index.htm" })
	public void index(HttpServletRequest request, HttpServletResponse response,
			Model model){
		List<GoodsApi> goodsApiList = this.goodsApiService.query(
				"select obj from GoodsApi obj", null, 0, 100);
		for (GoodsApi goodsApi : goodsApiList) {
			JSONObject fromObject = JSONObject.fromObject(goodsApi);
			long long1 = fromObject.getLong("goods_store_id");
			System.out.println(long1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:商品详情的展示,是一段html的片段代码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_goods_detail_view.htm" })
	public ModelAndView api_goods_detail_view(HttpServletRequest request,
			HttpServletResponse response, String goods_id) {
		ModelAndView mv = new JModelAndView("default/api_goods_detail.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Goods goods = this.goodsService
				.getObjById(CommUtil.null2Long(goods_id));
		mv.addObject("obj", goods);
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:商品的详情接口和判断是否该商品已经加入到个人商城
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_goods_detail.htm" })
	public void api_goods_detail(HttpServletRequest request,
			HttpServletResponse response, String id,String user_id){
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		if (goods==null) {
			ApiUtils.json(response, "", "商品不存在", 1);
			return;
		}
		if (goods.getGoods_store().getStore_status()!=2) {
			ApiUtils.json(response, "", "该店铺已经关闭", 1);
			return;
		}
		if (goods.getGoods_status()!=0) {
			ApiUtils.json(response, "", "该商品已经下架", 1);
			return;
		}
		if(goods.isDeleteStatus()){
			ApiUtils.json(response, "", "该商品已经删除", 1);
			return;
		}
		if ("spec".equals(goods.getInventory_type())) {
			SpecifiList goodsSpec = SpecTools.getGoodsSpec(goods
					.getSpeifi_list());
			goods.setSpecifiList(goodsSpec);
		}
		String hql="select obj.person_goods from PersonMallApi as obj where obj.user.id="+user_id+" and obj.delete_status=0";
		List<?> person_all_goods=this.commonService.query(hql, null, -1, -1);
		Iterator<?> ite=person_all_goods.iterator();
		String person_goods_ids="";
		while(ite.hasNext()){
			Goods temp=(Goods)ite.next();
			person_goods_ids=temp.getId().toString()+","+person_goods_ids;
		}
		int i=person_goods_ids.indexOf(id);
		FenPei fenPei = this.fenPeiService.getObjById(Long.valueOf(1).longValue());
		double settlementPrice=goods.getSettlement_price().doubleValue();
		double changtuijin=goods.getCtj();
		double zhanlue_price=goods.getZhanlue_price().doubleValue();
		double storePrice=goods.getStore_price().doubleValue();
		double baseGold=storePrice-settlementPrice-changtuijin-zhanlue_price;
		double shoppingGuidePrice=baseGold*fenPei.getDaogou_get_price();
		goods.setShoppingGuidePrice(CommUtil.formatDouble(shoppingGuidePrice, 2));
		
		//新增评价,店铺保证金,店铺评分
		Map<String, Integer> goodsEvaluateCount = this.getGoodsEvaluateCount(goods.getId().toString());
		AppraiseMessageEntity goodsNewEvaluate = this.getGoodsNewEvaluate(goods.getId().toString());
		if (goodsNewEvaluate!=null&&goodsNewEvaluate.getIsAnonymity()) {
			goodsNewEvaluate.setUser(null);
		}
		int bail=0;
		User user = goods.getGoods_store().getUser();
		Integer freezeBlance = user.getFreezeBlance().intValue();
		int availableBalance = user.getAvailableBalance().intValue();
		if (freezeBlance!=0) {
			if (availableBalance>1000) {
				bail=1000;
			}else {
				bail=availableBalance;
			}
		}
		AppEvaluateData appEvaluateData=new AppEvaluateData(goodsEvaluateCount.get("praiseNum"), goodsEvaluateCount.get("commonlyNum"), goodsEvaluateCount.get("badNum"), goodsEvaluateCount.get("evaluateSum"), goodsNewEvaluate,bail);
		goods.setAppEvaluateDate(appEvaluateData);
		StoreScoreEntity storeScoreInfo = this.getStoreScoreInfo(goods.getGoods_store());
		goods.setStoreScoreEntity(storeScoreInfo);		
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appSaveUserBrowseRecords.htm?userId=" + user_id + "&goodsId=" + id, "GET");
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				Goods.class,
				"id,goods_name,goods_price,store_price,goods_photos,inventory_type,specifiList,settlement_price,goods_salenum,goods_collect,goods_inventory,goods_store,ctj,shoppingGuidePrice,appEvaluateDate,storeScoreEntity,goods_status,isHideRebate"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone,user"));
		objs.add(new FilterObj(SpecifiList.class, "spec_name_list,spec_prop"));
		objs.add(new FilterObj(AppEvaluateData.class, "praiseNum,commonlyNum,badNum,evaluateSum,evaluate,deposit"));
		objs.add(new FilterObj(AppraiseMessageEntity.class, "describeStarts,assessingDiscourse,isAnonymity,user"));
		objs.add(new FilterObj(StartsExplainEntity.class, "startsNum,startExplain"));
		objs.add(new FilterObj(AssessingDiscourseEntity.class, "assessingCharacter,,assessingTime"));
		objs.add(new FilterObj(User.class, "id,userName,photo,lastLoginDate,loginDate"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(StoreScoreEntity.class, "storeAverageScore,storeExpressAverageScore,filedExplainShow,expressSiledExplainShow"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		if(i<0){
			ApiUtils.json(response, goods, "没有加入到个人商城", 0, filter);
		}else{
			ApiUtils.json(response, goods, "已经加入到个人商城", 3, filter);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取用户地址列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/app_address_acquire.htm"})
	public void app_address_acquire(HttpServletRequest request,
			HttpServletResponse response, String user_id,String currentPage) {
		int current_page=CommUtil.null2Int(currentPage);
		int pageSize=20;
		String hql="select obj from Address obj where obj.user.id=" + user_id+" and obj.deleteStatus=false order by obj.defaultStatus desc";
		List<Address> addressList = this.addressService.query(hql,
				null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Address.class,
				"trueName,defaultStatus,area_info,telephone,mobile,id,province,city,county,area"));
		objs.add(new FilterObj(Area.class,
				"id,parent,areaName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, addressList, "查询成功", 0, filter);
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:更新用户地址列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_addr_update.htm" })
	public void app_addr_update(HttpServletRequest request,
			HttpServletResponse response, String addr_id, String trueName,
			String mobile, String address,String province,String addressId,
			String city,String county) {
		if (ApiUtils.is_null(addr_id, trueName, mobile, address,province,city)) {
			ApiUtils.json(response, "", "地址参数不能为空", 1);
			return;
		}
		Address addr = this.addressService.getObjById(CommUtil
				.null2Long(addr_id));
		addr.setProvince(province);
		addr.setCity(city);
		addr.setCounty(county);
		addr.setTrueName(trueName);
		addr.setMobile(mobile);
		addr.setTelephone(mobile);
		addr.setArea_info(address);
		addr.setArea((Area) commonService.getById("Area", CommUtil.null2Long(addressId)+""));
		this.addressService.update(addr);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Address.class,
				"trueName,area_info,telephone,mobile,id"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, addr, "查询成功", 0, filter);
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:删除用户地址列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_delete_addr.htm" })
	public void api_delete_add(HttpServletRequest request,
			HttpServletResponse response, String add_id) {
		Address addr=this.addressService.getObjById(CommUtil.null2Long(add_id));
		if(addr!=null){
			addr.setDeleteStatus(true);
			boolean ret=this.addressService.update(addr);
			if(ret){
				ApiUtils.json(response, "", "删除成功", 0);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:新增用户地址列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_save_add.htm" })
	public void app_save_add(HttpServletRequest request,
			HttpServletResponse response,String user_id, String trueName,
			String mobile, String address,String province,String addressId,
			String city,String county) {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(user_id));
		if(user!=null){
			Address addr=new Address();
			addr.setAddTime(new Date());
			addr.setProvince(province);
			addr.setCity(city);
			addr.setCounty(county);
			addr.setArea_info(address);
			addr.setTrueName(trueName);
			addr.setMobile(mobile);
			addr.setUser(user);
			addr.setArea((Area) commonService.getById("Area", CommUtil.null2Long(addressId)+""));
			boolean ret=this.addressService.save(addr);
			if(ret){
				this.app_set_defaultAddr(request, response, user_id,addr.getId().toString());
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:以收货人姓名为主进行模糊搜索
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_search_addr.htm" })
	public void app_search_addr(HttpServletRequest request,
			HttpServletResponse response,String trueName,String currentPage,
			String userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		int current_page=CommUtil.null2Int(currentPage);
		int pageSize=20;
		String hql="select obj from Address obj where obj.user.id="+userId+" and obj.trueName like '%"+trueName+"%'";
		List<Address> addressList = this.addressService.query(hql,
				null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Address.class,
				"trueName,defaultStatus,area_info,telephone,mobile,id,province,city,county"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, addressList, "查询成功", 0, filter);
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:设置默认地址
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_set_defaultAddr.htm" })
	public void app_set_defaultAddr(HttpServletRequest request,
			HttpServletResponse response,String userId,String addId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="select obj from Address obj where obj.user.id="+userId;
		List<Address> addressList = this.addressService.query(hql,
				null, -1, -1);
		for(int i=0;i<addressList.size();i++){
			Address addr=addressList.get(i);
			if(addr.getDefaultStatus()==1){
				addr.setDefaultStatus(0);
				this.addressService.update(addr);
				break;
			}
		}
		Address addr = this.addressService.getObjById(CommUtil
				.null2Long(addId));
		addr.setDefaultStatus(1);
		boolean ret=this.addressService.update(addr);
		if(ret){
			ApiUtils.json(response, "", "设置成功", 0);
		}
	}
	
	@RequestMapping({ "/api_load_goods_gsp.htm" })
	public void api_load_goods_gsp(HttpServletRequest request,
			HttpServletResponse response, String gsp, String id) {
		if (ApiUtils.is_null(gsp, id)) {
			ApiUtils.json(response, "", "参数都不传?", 1);
			return;
		}
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		int count = 0;
		double price = 0.0D;
		double settlement_price = 0.0D;

		Specifi spec = (Specifi) commonService.getByWhere("Specifi",
				"specifi='" + gsp + "' and goods_id=" + goods.getId());
		double commission=0;
		if (spec != null) {
			count = spec.getInventory();
			price = spec.getPrice();
			settlement_price = spec.getSettlement_price();
			FenPei fenPei = this.fenPeiService.getObjById(Long.valueOf(1).longValue());
			commission = SpecTools.getGoodsSpecCommission(goods, spec, fenPei);
		}
		JSONObject obj = new JSONObject();
		obj.put("count", Integer.valueOf(count));
		obj.put("price", Double.valueOf(price));
		obj.put("settlement_price", Double.valueOf(settlement_price));
		obj.put("commission",commission );
		ApiUtils.json(response, obj, "查询成功", 0);
	}

	@RequestMapping({ "/api_collect_goods.htm" })
	public void api_collect_goods(HttpServletRequest request,
			HttpServletResponse response, String userId) {
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		List<FavoriteApi> favoriteList = commonService.query(
				"select obj from FavoriteApi obj where obj.user.id=" + userId,
				null, -1, -1);
		RespApi respApi = new RespApi();
		if (favoriteList == null || favoriteList.size() == 0) {
			respApi.setMsg("收藏为空");
			respApi.setStatus(1);
		} else {
			respApi.setResult(favoriteList);
			respApi.setMsg("查询成功");
			respApi.setStatus(0);
		}
		ApiUtils.json(response, respApi);
		return;
	}
	@RequestMapping({"/api_share_goods.htm"})
	public ModelAndView api_share_goods(HttpServletRequest request,
			HttpServletResponse response, String goods_id,String user_id){
		ModelAndView mv = new JModelAndView("share.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		mv.addObject("obj",goods);
		mv.addObject("goods_id",goods_id);
		String url = CommUtil.getURL(request)+"/goods.htm?id="+goods_id+"&tjr="+user_id;
		try {
			url = URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		mv.addObject("url",url);
		mv.addObject("user_id",user_id);
		return mv;
	}
	@RequestMapping({"/app_mobile_viewTest.htm"})
	public ModelAndView test(HttpServletRequest request,
			HttpServletResponse response){
		ModelAndView mv = new JModelAndView("app_mobile_view.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:新商品提醒功能
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/newGoodsRemind.htm", method = RequestMethod.POST)
	public void newGoodsRemind(HttpServletRequest request,
			HttpServletResponse response){
		String time=ApiUtils.weeHours(CommUtil.formatTime( "yyyy-MM-dd",new Date()), 0);
		String hql="from Goods as obj where obj.addTime >= :begin and obj.goods_status = 0 and obj.goods_store.store_status = 2 order by obj.addTime DESC";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("begin", CommUtil.formatDate(time,"yyyy-MM-dd HH:mm:ss"));
		List<Goods> goods=goodsService.query(hql, params, 0, 20);
		if (goods.size()>0) {
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, goods, "获取新增商品成功", 0,filter);
			return;
		}else {
			ApiUtils.json(response, "", "今日没有新增商品", 1);
			return;
		}
	}
	//查询某个商品最新的一条评论
	private AppraiseMessageEntity getGoodsNewEvaluate(String goodsId){
		String hql="select obj from AppraiseMessageEntity as obj where obj.deleteStatus=false and obj.goods.id = " + goodsId + " order by obj.addTime DESC";
		List<AppraiseMessageEntity> query = appraiseMessageService.query(hql, null, 0, 1);
		if (query.size()>0) {
			return query.get(0);
		}
		return null;
	}
	//查询某个商品最新的一条评论
	@SuppressWarnings("unchecked")
	private Map<String, Integer> getGoodsEvaluateCount(String goodsId){
		String hql="select count(obj) from AppraiseMessageEntity as obj where obj.describeStarts.startsNum in (5,4) and obj.goods.id= " + goodsId;
		List<Object> query = commonService.query(hql, null, -1, -1);
		int praiseNum=0;//好评
		if (query.size()>0) {
			praiseNum=CommUtil.null2Int(query.get(0));
		}
		int commonlyNum=0;//中评
		hql="select count(obj) from AppraiseMessageEntity as obj where obj.describeStarts.startsNum =3 and obj.goods.id= " + goodsId;
		query = commonService.query(hql, null, -1, -1);
		if (query.size()>0) {
			commonlyNum=CommUtil.null2Int(query.get(0));
		}
		int badNum=0;//差评
		hql="select count(obj) from AppraiseMessageEntity as obj where obj.describeStarts.startsNum in (2,1) and obj.goods.id= " + goodsId;
		query = commonService.query(hql, null, -1, -1);
		if (query.size()>0) {
			badNum=CommUtil.null2Int(query.get(0));
		}
		int evaluateSum = commonlyNum + praiseNum + badNum;
		Map<String, Integer> info=new HashMap<String, Integer>();
		info.put("praiseNum", praiseNum);
		info.put("commonlyNum", commonlyNum);
		info.put("badNum", badNum);
		info.put("evaluateSum", evaluateSum);
		return info;
	}
	//获取店铺评分
	private StoreScoreEntity getStoreScoreInfo(Store store){
		String hql="select obj from StoreScoreEntity as obj where obj.store.id = " + store.getId().toString();
		List<StoreScoreEntity> query = this.storeScoreService.query(hql, null, -1, -1);
		if (query.size()>0) {
			return query.get(0);
		}else {
			StoreScoreEntity storeScoreEntity=new StoreScoreEntity();
			storeScoreEntity.setStoreAverageScore(5f);
			storeScoreEntity.setStoreExpressAverageScore(5f);
			storeScoreEntity.setStoreEvalutePerNum(0);
			storeScoreEntity.setFiledExplainShow("非常好");
			storeScoreEntity.setExpressSiledExplainShow("非常好");
			return storeScoreEntity;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:三级联动，获取地址
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appAddressLinkage.htm", method = RequestMethod.POST)
	public void appAddressLinkage(HttpServletRequest request,HttpServletResponse response,String addressId){
		String where = "";
		if ("".equals(CommUtil.null2String(addressId))) {
			where = "obj.parent is null and obj.id <> 0";
		}else {
			where = "obj.parent.id = " + CommUtil.null2Long(addressId);
		}
		String hql="select obj from Area as obj where " + where + " order by id ";
		List<Area> query = commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Area.class, "id,areaName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query, "获取成功", 0,filter);
		return;
	}
}
