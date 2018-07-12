package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.ZhiWeiRecoderEntity;
import com.shopping.api.domain.appDynamicImg.AppShareImg;
import com.shopping.api.domain.appDynamicImg.AppWelcomeImg;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageSwitchEntity;
import com.shopping.api.domain.appHomePage.AppHomePageTemporaryData;
import com.shopping.api.domain.recommend.RecommendStore;
import com.shopping.api.output.AppAdminData;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.HomePageData;
import com.shopping.api.output.StoreListData;
import com.shopping.api.service.IAppHomePageService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.BigDecimalUtil;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsClassService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AppHomePageAction {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAppHomePageService apphomeService;
	@Autowired
	private IOrderFormService orderFormService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取app首页的数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getHomePageDate.htm", method = RequestMethod.POST)
	public void getHomePageDate(HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat yy = new SimpleDateFormat("yyyy");
		String yy_begin = yy.format(new Date());
		String carouselHql="select obj from AppHomePageEntity as obj";
		String goodsHql="select obj from Goods as obj "
							+ "where obj.goods_store.store_status in (2,3) and obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and "
							+ "obj.addTime>'"+yy_begin+"' order by "
							+ "obj.goods_salenum desc ";
		String commonHql="select obj from AppHomePageCommonPosition as obj";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String begin = sdf.format(new Date(System.currentTimeMillis()));
		String end = sdf.format(new Date(System.currentTimeMillis()
				+ (86400 * 1000)));
		String sql_str ="SELECT "+
						  "temp2.name2, "+
						  "temp2.payTimes, "+
						  "temp2.userName, "+
						  "temp2.id, "+
						  "temp2.daogou_get_price, "+
						  "temp2.totalPrice, "+
						  "temp2.orderId, "+
						  "sagou.name "+
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
						  "temp.areaGradeOfUser_id "+
						"FROM ecm_bumen AS eb "+
						  "RIGHT JOIN (SELECT "+
						               "DATE_FORMAT(so.payTimes,'%Y-%m-%d %H:%i:%s') AS payTimes, so.id as orderId, "+
						               "su.areaGradeOfUser_id, "+
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
						     "on temp2.areaGradeOfUser_id=sagou.id";
		List<AppHomePageEntity> homePageCarousel=this.apphomeService.query(carouselHql, null, -1, -1);
		List<AppHomePageCommonPosition> homePageComm=this.commonService.query(commonHql, null, -1, -1);
		List<Goods> goodsSalum=this.goodsService.query(goodsHql, null, 0, 12);
		List<?> xibao =this.commonService.executeNativeNamedQuery(sql_str);
		//app首页共享商品数量
		String sharedGoodsNum_sql="select count(obj) from Goods as obj where obj.deleteStatus=false and obj.goods_status <> 1 and obj.goods_store.store_status = 2";
		List<?> sharedGoodsNum_list = goodsService.query(sharedGoodsNum_sql, null, -1, -1);
		String sharedGoodsNum="";
		if (sharedGoodsNum_list.size()>0) {
			sharedGoodsNum=sharedGoodsNum_list.get(0)+"";
		}
		HomePageData homePageData=new HomePageData();
		homePageData.setHomePageCarousel(homePageCarousel);
		homePageData.setHomePageCommon(homePageComm);
		homePageData.setGoodsSalum(goodsSalum);
		homePageData.setXibao(xibao);
		homePageData.setYear(yy_begin);
		homePageData.setSharedGoodsNum(sharedGoodsNum);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(HomePageData.class, "homePageCommon,year,homePageCarousel,goodsSalum,xibao,sharedGoodsNum"));
		objs.add(new FilterObj(AppHomePageEntity.class, "goods,sequence,defaultGoods,id,position_name,purchase_timeDuan"));
		objs.add(new FilterObj(AppHomePageCommonPosition.class, "goods,sequence,defaultGoods,id,position_name,purchase_timeDuan"));
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		objs.add(new FilterObj(GoodsClass.class, "id,className,sequence,level,icon_acc,parent"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, homePageData, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取类别的子类
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getGoodsClassChildren.htm", method = RequestMethod.POST)
	public void getGoodsClassChildren(HttpServletRequest request,
			HttpServletResponse response,String self_id){
		GoodsClass goodsClass=this.goodsClassService.getObjById(Long.valueOf(self_id));
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(GoodsClass.class,"id,className,childs,icon_acc"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsClass, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取某类别下的商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getGoodsOfGoodsClass.htm", method = RequestMethod.POST)
	public void getGoodsOfGoodsClass(HttpServletRequest request,
			HttpServletResponse response,String goodsClass_id,
			String currentPage,String goodsPrice,String salesNum,
			String goodsClicks,String addTime,String orderType){
		String hql="";
		String chooseCondition="";
		hql="select obj from Goods as obj where obj.deleteStatus=false and obj.gc.id="+goodsClass_id+" and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.store_status in (2,3)";
		if("".equals(CommUtil.null2String(orderType))){
			orderType="";
		}
		if(!"".equals(CommUtil.null2String(goodsPrice))){
			chooseCondition="order by obj.store_price "+orderType;
		}
		if(!"".equals(CommUtil.null2String(goodsClicks))){
			chooseCondition="order by obj.goods_click "+orderType;
		}
		if(!"".equals(CommUtil.null2String(salesNum))){
			chooseCondition="order by obj.goods_salenum "+orderType;
		}
		if(!"".equals(CommUtil.null2String(addTime))){
			chooseCondition="order by obj.addTime "+orderType;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<Goods> goodsList=this.goodsService.query(hql+chooseCondition, null, current_page*pageSize, pageSize);
		for(Goods goods:goodsList){
			String goods_detail=goods.getGoods_details();
			if(!"".equals(CommUtil.null2String(goods_detail))){
				String detail=this.clearContent(goods_detail);
				goods.setGoods_details(detail);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "查询成功", 0, filter);
	}
	private String clearContent(String inputString) {
		String htmlStr = inputString;
		String textStr = "";
		try {
			String regEx_script = "<[//s]*?script[^>]*?>[//s//S]*?<[//s]*?///[//s]*?script[//s]*?>";
			String regEx_style = "<[//s]*?style[^>]*?>[//s//S]*?<[//s]*?///[//s]*?style[//s]*?>";
			String regEx_html = "<[^>]+>";
			String regEx_html1 = "<[^>]+";
			Pattern p_script = Pattern.compile(regEx_script, 2);
			Matcher m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll("");

			Pattern p_style = Pattern.compile(regEx_style, 2);
			Matcher m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll("");

			Pattern p_html = Pattern.compile(regEx_html, 2);
			Matcher m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll("");

			Pattern p_html1 = Pattern.compile(regEx_html1, 2);
			Matcher m_html1 = p_html1.matcher(htmlStr);
			htmlStr = m_html1.replaceAll("");
			
			textStr = htmlStr;
		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}
		return textStr;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:根据商品的名字进行商品的查询
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/searchGoodsByName.htm", method = RequestMethod.POST)
	public void searchGoodsByName(HttpServletRequest request,
			HttpServletResponse response,String goodsName,
			String currentPage,String goodsPrice,String salesNum,
			String goodsClicks,String addTime,String orderType){
		String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_name like '%"+goodsName+"%' and obj.goods_status=0 and obj.goods_inventory>0  and obj.goods_store.store_status =2";
		String chooseCondition="";
		if("".equals(CommUtil.null2String(orderType))){
			orderType="";
		}
		if(!"".equals(CommUtil.null2String(goodsPrice))){
			chooseCondition="order by obj.store_price "+orderType;
		}
		if(!"".equals(CommUtil.null2String(goodsClicks))){
			chooseCondition="order by obj.goods_click "+orderType;
		}
		if(!"".equals(CommUtil.null2String(salesNum))){
			chooseCondition="order by obj.goods_salenum "+orderType;
		}
		if(!"".equals(CommUtil.null2String(addTime))){
			chooseCondition="order by obj.addTime "+orderType;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<Goods> goodsList=this.goodsService.query(hql+chooseCondition, null, current_page*pageSize, pageSize);
		for(Goods goods:goodsList){
			String goods_detail=goods.getGoods_details();
			if(!"".equals(CommUtil.null2String(goods_detail))){
				String detail=this.clearContent(goods_detail);
				goods.setGoods_details(detail);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:上滑首页加载所需要的数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_loadGoodsBySales.htm", method = RequestMethod.POST)
	public void app_loadGoodsBySales(HttpServletRequest request,
			HttpServletResponse response,String currentPage){
		String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.store_status in (2,3) order by obj.goods_salenum desc";
		int current_page=0;
		int pageSize=12;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<Goods> goodsList=this.goodsService.query(hql, null, current_page*pageSize, pageSize);
		for(Goods goods:goodsList){
			String goods_detail=goods.getGoods_details();
			if(!"".equals(CommUtil.null2String(goods_detail))){
				String detail=this.clearContent(goods_detail);
				goods.setGoods_details(detail);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_photos,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:通过订单的id找到商品的id
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getGoodsByOrderId.htm", method = RequestMethod.POST)
	public void getGoodsByOrderId(HttpServletRequest request,
			HttpServletResponse response,String orderId){
		Long order_id=CommUtil.null2Long(orderId);
		OrderForm order=this.orderFormService.getObjById(order_id);
		List<GoodsCart> goodsCart=order.getGcs();
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(GoodsCart.class, "goods"));
		objs.add(new FilterObj(Goods.class, "id,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsCart, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:通过订单的id找到商品的id
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getGoodsOfStore.htm", method = RequestMethod.POST)
	public void getGoodsOfStore(HttpServletRequest request,
			HttpServletResponse response,String storeId,
			String currentPage,String goodsPrice,String salesNum,
			String goodsClicks,String addTime,String orderType){
		String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.id="+storeId+"  ";
		String chooseCondition="";
		orderType=CommUtil.null2String(orderType);
		if(!"".equals(CommUtil.null2String(goodsPrice))){
			chooseCondition="order by obj.store_price "+orderType;
		}
		if(!"".equals(CommUtil.null2String(goodsClicks))){
			chooseCondition="order by obj.goods_click "+orderType;
		}
		if(!"".equals(CommUtil.null2String(salesNum))){
			chooseCondition="order by obj.goods_salenum "+orderType;
		}
		if(!"".equals(CommUtil.null2String(addTime))){
			chooseCondition="order by obj.addTime "+orderType;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<Goods> goodsList=this.goodsService.query(hql+chooseCondition, null, current_page*pageSize, pageSize);
		for(Goods goods:goodsList){
			String goods_detail=goods.getGoods_details();
			if(!"".equals(CommUtil.null2String(goods_detail))){
				String detail=this.clearContent(goods_detail);
				goods.setGoods_details(detail);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum,goods_store"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:通过的店铺的名字搜索店铺得到店铺列表,店铺的总销量是按照店铺出单情况进行统计的（改版淘汰接口）
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_searchStoreByName.htm", method = RequestMethod.POST)
	public void app_searchStoreByName(HttpServletRequest request,
			HttpServletResponse response,String storeName,String currentPage){
		int current_page=CommUtil.null2Int(currentPage);
		int pageSize=20;
		List<StoreListData> storeListDataList=new ArrayList<StoreListData>();
		String sql="SELECT * "+
					"FROM (SELECT "+
					        "COUNT(*) AS sales,  ss.id "+
					      "FROM shopping_store AS ss "+
					      "LEFT JOIN shopping_orderform AS so "+
					          "ON so.store_id = ss.id "+
					      "WHERE ss.store_name LIKE '%"+storeName+"%' "+
					          " AND so.order_status IN(40,50,60) AND ss.store_status IN(2,3) "+
					      "GROUP BY ss.id) AS temp "+
					"ORDER BY temp.sales DESC limit "+current_page*pageSize+","+pageSize ;
		List<?> storeIdAndSales=this.commonService.executeNativeNamedQuery(sql);
		for(int i=0;i<storeIdAndSales.size();i++){
			Object[] storeIdAndSalesArray=(Object[]) storeIdAndSales.get(i);
			Store store=this.storeService.getObjById(CommUtil.null2Long(storeIdAndSalesArray[1]));
			int  totalNums=store.getGoods_list().size();
			String hql="select obj from Goods as obj where obj.deleteStatus=false and  obj.goods_store.id="+store.getId().longValue()+" and obj.goods_status=0 and obj.goods_inventory>0 order by obj.goods_salenum desc";
			List<Goods> goodsList=this.goodsService.query(hql, null, 0, 3);
			store.setGoods_list(goodsList);
			StoreListData storeListData=new StoreListData();
			storeListData.setStore(store);
			storeListData.setSalesNum(CommUtil.null2Long(storeIdAndSalesArray[0]));
			storeListData.setGoodsTotal(CommUtil.null2Long(totalNums));
			storeListDataList.add(storeListData);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(StoreListData.class, "store,salesNum,goodsTotal"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone,store_logo,goods_list,user"));
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(User.class, "id,userName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, storeListDataList, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:统计订单的数量
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_getOrderNums.htm", method = RequestMethod.POST)
	public void app_getOrderNums(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		List<Long> orderNumsList=new ArrayList<Long>();
		String sql="SELECT "+
				   "temp1.a1, "+
				   "temp2.a2, "+
				   "temp3.a3, "+
				   "temp4.a4 "+
				"FROM (SELECT "+
				        "COUNT(so.id) AS a1, "+
				        "so.user_id "+
				      "FROM shopping_orderform AS so "+
				      "WHERE so.user_id ="+user_id+" ) AS temp1 "+
				  "JOIN (SELECT "+
				          "COUNT(so.id) AS a2, "+
				          "so.user_id "+
				        "FROM shopping_orderform AS so "+
				        "WHERE so.user_id ="+user_id+
				            " AND so.order_status = 10) AS temp2 "+
				  "JOIN (SELECT "+
				          "COUNT(so.id) AS a3, "+
				          "so.user_id "+
				        "FROM shopping_orderform AS so "+
				        "WHERE so.user_id ="+user_id+
				            " AND so.order_status = 20) AS temp3 "+
				  "JOIN (SELECT "+
				          "COUNT(so.id) AS a4, "+
				          "so.user_id "+
				        "FROM shopping_orderform AS so "+
				        "WHERE so.user_id = "+user_id+
				            " AND so.order_status = 30) AS temp4";
		List<?> orderNums=this.commonService.executeNativeNamedQuery(sql);
		Object[] orderNumsArray=(Object[]) orderNums.get(0);
		for(int i=0;i<orderNumsArray.length;i++){
			orderNumsList.add(CommUtil.null2Long(orderNumsArray[i]));
		}
		ApiUtils.json(response, orderNumsList, "查询成功", 0);		
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端判断是否开启首页付费购买功能
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_judge_isOpenPayment.htm", method = RequestMethod.POST)
	public void app_judge_isOpenPayment(HttpServletRequest request,
			HttpServletResponse response){
		String hql="select obj from AppHomePageSwitchEntity as obj where obj.id=1";
		List<?> appSwitchList=this.commonService.query(hql, null, -1,-1);
		AppHomePageSwitchEntity appSwitch=(AppHomePageSwitchEntity) appSwitchList.get(0);
		if(appSwitch.getIs_turnOn()){
			ApiUtils.json(response, "", "可以进行购买", 0);
		}else{
			ApiUtils.json(response, "", "该功能只在每天12点到13点开放一个小时,请到时再来,谢谢", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端获取banner位,得到banner的空位列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_bannerPosition.htm", method = RequestMethod.POST)
	public void app_acquire_bannerPosition(HttpServletRequest request,
			HttpServletResponse response){
//		if(true){
//			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
//			return;
//		}
		
		String hql="select obj from AppHomePageEntity as obj where obj.is_can_buy=true";
		List<?> appHomeBanner=this.commonService.query(hql, null, -1, -1);
		if(appHomeBanner.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AppHomePageEntity.class, "id,sequence,position_name,is_can_buy,banner_price"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, appHomeBanner, "查询成功", 0, filter);
		}else{
			ApiUtils.json(response, "", "目前没有轮播空位了,请下次再来,谢谢", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端获取common位,得到common的空位列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_commonPosition.htm", method = RequestMethod.POST)
	public void app_acquire_commonPosition(HttpServletRequest request,
			HttpServletResponse response){
//		if(true){
//			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
//			return;
//		}
		
		String hql="select obj from AppHomePageCommonPosition as obj where obj.is_can_buy=true ";
		List<?> appHomeCommon=this.commonService.query(hql, null, -1, -1);
		if(appHomeCommon.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(AppHomePageCommonPosition.class, "id,sequence,position_name,is_can_buy,commonPosition_price"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, appHomeCommon, "查询成功", 0, filter);
		}else{
			ApiUtils.json(response, appHomeCommon, "目前没有普通空位了,请下次再来,谢谢", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端获取商品状态是0或1  并且商品的库存大于0  并且商品所在的店铺状态是2,3状态的列表数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_selfEligibleGoods.htm", method = RequestMethod.POST)
	public void app_acquire_selfEligibleGoods(HttpServletRequest request,
			HttpServletResponse response,String currentPage,String userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		String hql="";
		Store store=null;
		if(user.getStore()==null){
			ApiUtils.json(response, "", "您目前还没有开通店铺", 2);
			return;
		}else{
			store=user.getStore();
			hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_inventory>0 and obj.goods_status in(0) and obj.goods_store.store_status in (2,3) and obj.goods_store.id="+store.getId()+" order by obj.addTime desc";
			int current_page=0;
			int pageSize=20;
			if("".equals(CommUtil.null2String(currentPage))){
				current_page=0;
			}else{
				current_page=Integer.valueOf(currentPage).intValue();
			}
			List<?> goodsList=this.goodsService.query(hql, null, current_page*pageSize,pageSize);
			if(goodsList.size()>0){
				List<FilterObj> obj=new ArrayList<FilterObj>();
				obj.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_main_photo,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory"));
				obj.add(new FilterObj(Accessory.class, "name,path,ext"));
				CustomerFilter filter = ApiUtils.addIncludes(obj);
				ApiUtils.json(response, goodsList, "查询成功", 0, filter);
				return;
			}else{
				ApiUtils.json(response, goodsList, "您的店铺目前没有商品", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:首页banner空位的积分支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_balancePayment.htm", method = RequestMethod.POST)
	public void app_vacantPosition_balancePayment(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			String goodsId,String userId){
		if(true){
			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="select obj from AppHomePageEntity as obj where obj.id="+id;
		List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
		AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
		boolean ret=appHome.getIs_can_buy();
		if(ret){
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			double toatal=CommUtil.formatDouble(purchase_timeDuan*appHome.getBanner_price(), 2);
			double userBalance=user.getAvailableBalance().doubleValue();
			boolean is_can_buy=false;
			if(user.getFreezeBlance().intValue()==1){
				if(userBalance-toatal-1000>=0){
					is_can_buy=true;
				}
			}else{
				if(userBalance-toatal>=0){
					is_can_buy=true;
				}
			}
			if(is_can_buy){
				User countUser=this.userService.getObjById(1L);
				boolean isRepeat=this.judgeIsRepetition(goodsId,"AppHomePageEntity");
				if(isRepeat){
					ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
					return;
				}
				user.setAvailableBalance(BigDecimal.valueOf(CommUtil.subtract(
						userBalance, toatal)));
				boolean up_ret=this.userService.update(user);
				if(up_ret){
					PredepositLog buyBanner_log = new PredepositLog();
					buyBanner_log.setAddTime(new Date());
					buyBanner_log.setPd_log_user(user);
					buyBanner_log.setPd_op_type("减少");
					buyBanner_log.setPd_log_amount(BigDecimal.valueOf(-toatal));
					buyBanner_log.setPd_log_info("用于支付首页banner的空位");
					buyBanner_log.setPd_type("可用预存款");
					buyBanner_log.setCurrent_price(user.getAvailableBalance().doubleValue());
					boolean rr=this.predepositLogService.save(buyBanner_log);
					
					countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
							countUser.getAvailableBalance().doubleValue(), toatal)));
					this.userService.update(countUser);
					PredepositLog countUser_log = new PredepositLog();
					countUser_log.setAddTime(new Date());
					countUser_log.setPd_log_user(countUser);
					countUser_log.setPd_op_type("增加");
					countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
					countUser_log.setPd_log_info("首页banner空位的收入");
					countUser_log.setPd_type("可用预存款");
					countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
					boolean rr1=this.predepositLogService.save(countUser_log);
					if(rr&&rr1){
						goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
						Date date=new Date();
						appHome.setIs_can_buy(false);
						appHome.setPurchase_timeDuan(purchase_timeDuan);
						appHome.setGoods(goods);
						appHome.setStart_time(date);
						appHome.setFlush_time(this.getDateAfter(date,purchase_timeDuan));
						this.commonService.update(appHome);
						ApiUtils.json(response, "", "购买成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 2);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:首页普通空位的积分支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_commonPayment.htm", method = RequestMethod.POST)
	public void app_vacantPosition_commonPayment(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			String goodsId,String userId){
		if(true){
			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
			return;
		}
		
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
		List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
		AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
		boolean ret=appHome.getIs_can_buy();
		if(ret){
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			double toatal=CommUtil.formatDouble(purchase_timeDuan*appHome.getCommonPosition_price(), 2);
			double userBalance=user.getAvailableBalance().doubleValue();
			boolean is_can_buy=false;
			if(user.getFreezeBlance().intValue()==1){
				if(userBalance-toatal-1000>=0){
					is_can_buy=true;
				}
			}else{
				if(userBalance-toatal>=0){
					is_can_buy=true;
				}
			}
			if(is_can_buy){
				User countUser=this.userService.getObjById(1L);
				boolean isRepeat=this.judgeIsRepetition(goodsId,"AppHomePageCommonPosition");
				if(isRepeat){
					ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
					return;
				}
				user.setAvailableBalance(BigDecimal.valueOf(CommUtil.subtract(
						userBalance, toatal)));
				boolean up_ret=this.userService.update(user);
				if(up_ret){
					PredepositLog buyBanner_log = new PredepositLog();
					buyBanner_log.setAddTime(new Date());
					buyBanner_log.setPd_log_user(user);
					buyBanner_log.setPd_op_type("减少");
					buyBanner_log.setPd_log_amount(BigDecimal.valueOf(-toatal));
					buyBanner_log.setPd_log_info("用于支付首页普通区域的空位");
					buyBanner_log.setPd_type("可用预存款");
					buyBanner_log.setCurrent_price(user.getAvailableBalance().doubleValue());
					boolean rr=this.predepositLogService.save(buyBanner_log);
					
					countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
							countUser.getAvailableBalance().doubleValue(), toatal)));
					this.userService.update(countUser);
					PredepositLog countUser_log = new PredepositLog();
					countUser_log.setAddTime(new Date());
					countUser_log.setPd_log_user(countUser);
					countUser_log.setPd_op_type("增加");
					countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
					countUser_log.setPd_log_info("首页banner空位的收入");
					countUser_log.setPd_type("可用预存款");
					countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
					boolean rr1=this.predepositLogService.save(countUser_log);
					if(rr&&rr1){
						goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
						Date date=new Date();
						appHome.setIs_can_buy(false);
						appHome.setPurchase_timeDuan(purchase_timeDuan);
						appHome.setGoods(goods);
						appHome.setStart_time(date);
						appHome.setFlush_time(this.getDateAfter(date,purchase_timeDuan));
						this.commonService.update(appHome);
						ApiUtils.json(response, "", "购买成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 2);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:首页空位的微信支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_weixinPayment.htm", method = RequestMethod.POST)
	public void app_vacantPosition_weixinPayment(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			Long goodsId,String userId,String vacantPositionType){
		if(true){
			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="";
		boolean ret=false;
		boolean isRepeat=false;
		double price=0.00D;
		String notify_url=CommUtil.getURL(request)+"/app_HomePageWeixinPay_Callback.htm";
		if("banner".equals(vacantPositionType)){
			hql="select obj from AppHomePageEntity as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getBanner_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageEntity");
		}else if("common".equals(vacantPositionType)){
			hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getCommonPosition_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageCommonPosition");
		}
		if(ret){
			if(isRepeat){
				ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
				return;
			}else{
				double payTotal=purchase_timeDuan*price;
				Date date=new Date();
				AppHomePageTemporaryData appHome=new AppHomePageTemporaryData();
				appHome.setVacantPositionId(CommUtil.null2Long(id));
				appHome.setPurchase_timeDuan(purchase_timeDuan);
				appHome.setGoodsId(goodsId);
				appHome.setAddTime(new Date());
				appHome.setVacantPositionType(vacantPositionType);
				appHome.setIs_can_buy(CommUtil.null2Boolean(false));
				appHome.setStart_time(date);
				appHome.setFlush_time(this.getDateAfter(date,purchase_timeDuan));
				appHome.setTotal(payTotal);
				appHome.setOrderStatus(10);
				this.commonService.save(appHome);
				Map<String, String> out_put_params = null;//payTotal
				try {
					out_put_params = ApiUtils.get_weixin_sign_string(appHome.getOrderNum()+"",
							notify_url,payTotal+"");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ApiUtils.json(response, out_put_params, "获取支付信息成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢惠顾", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:首页空位的支付宝支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_albbPayment.htm", method = RequestMethod.POST)
	public void app_vacantPosition_albbPayment(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			Long goodsId,String userId,String vacantPositionType){
		if(true){
			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="";
		boolean ret=false;
		boolean isRepeat=false;
		double price=0.00D;
		if("banner".equals(vacantPositionType)){
			hql="select obj from AppHomePageEntity as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getBanner_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageEntity");
		}else if("common".equals(vacantPositionType)){
			hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getCommonPosition_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageCommonPosition");
		}
		if(ret){
			if(isRepeat){
				ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
				return;
			}else{
				double payTotal=purchase_timeDuan*price;//payTotal
				Date date=new Date();
				AppHomePageTemporaryData appHome=new AppHomePageTemporaryData();
				appHome.setVacantPositionId(CommUtil.null2Long(id));
				appHome.setPurchase_timeDuan(purchase_timeDuan);
				appHome.setGoodsId(goodsId);
				appHome.setAddTime(new Date());
				appHome.setVacantPositionType(vacantPositionType);
				appHome.setIs_can_buy(CommUtil.null2Boolean(false));
				appHome.setStart_time(date);
				appHome.setFlush_time(this.getDateAfter(date,purchase_timeDuan));
				appHome.setTotal(payTotal);
				appHome.setOrderStatus(10);
				this.commonService.save(appHome);
				String str = ApiUtils.getAlipayStr(appHome.getOrderNum()+"",
						CommUtil.getURL(request)+"/app_HomePageAliPay_Callback.htm", payTotal+"");
				ApiUtils.json(response, str, "获取支付信息成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢惠顾", 1);
			return;
		}
	}
	private Date getStartTime(){
		Date date=new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
		String nowTime=dateFormat.format(date)+" 11:59:59";
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		Date nowDate=null;
		try {
			nowDate = dateFormat2.parse(nowTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nowDate;
	}
	private Date getDateAfter(Date d,int day){
		Calendar now =Calendar.getInstance();  
		now.setTime(d);  
		now.set(Calendar.DATE,now.get(Calendar.DATE)+day);  
		return now.getTime(); 
	}
	private boolean judgeIsRepetition(String goodsId,String classType){
		boolean ret=false;
		String judgeHql="select obj from "+classType+" as obj where obj.goods.id="+goodsId;
		List<?>  appJudgeList=this.commonService.query(judgeHql, null, -1, -1);
		if(appJudgeList.size()>0){
			ret=true;
		}
		return ret;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页销量排行
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetGoodsSequence.htm", method = RequestMethod.POST)
	public void appGetGoodsSequence(HttpServletRequest request,HttpServletResponse response,Integer currentPage){
		int pageSize=20;
		if (currentPage==null) {
			currentPage=0;
		}
		String sql="from Goods as obj where obj.deleteStatus=false and obj.goods_status <> 1 and obj.goods_store.store_status = 2 order by goods_salenum DESC";
		List<Goods> list = goodsService.query(sql, null, currentPage*pageSize, pageSize);
		if (list.size()>0) {
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, list, "销量排行榜", 0,filter);
			return;
		}	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:喜报
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetXiBao.htm", method = RequestMethod.POST)
	public void appGetXiBao(HttpServletRequest request,HttpServletResponse response){
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
		List<?> xibao =this.commonService.executeNativeNamedQuery(sql_str);
		ApiUtils.json(response, xibao, "喜报获取成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页奖金日排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetBonusRanking.htm", method = RequestMethod.POST)
	public void appGetBonusRanking(HttpServletRequest request,HttpServletResponse response,String beginTime,String currentPage){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin =ApiUtils.weeHours(beginTime, 0);
		String end = ApiUtils.weeHours(beginTime, 1);
		String sql_str="SELECT "+
				  "temp4.name2, "+
				  "temp4.userName, "+
				  "temp4.id, "+
				  "ROUND(temp4.daogou, 2) AS daogou, "+
				  "temp4.name3, "+
				  "temp4.zhixianName, "+
				  "sa.path, "+
				  "sa.name "+ 
				"FROM "+
				  "shopping_accessory AS sa "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "temp3.name2, "+
				      "temp3.userName, "+
				      "temp3.id, "+
				      "temp3.daogou, "+
				      "temp3.name AS name3, "+
				      "temp3.photo_id, "+
				      "ez.name AS zhixianName "+ 
				    "FROM "+
				      "shopping_zhixian AS ez "+ 
				      "RIGHT JOIN "+ 
				        "(SELECT "+ 
				          "temp2.name2, "+
				          "temp2.userName, "+
				          "temp2.id, "+
				          "temp2.daogou, "+
				          "sagou.name, "+
				          "temp2.photo_id, "+
				          "temp2.zhixian_id "+ 
				        "FROM "+
				          "shopping_area_grade_of_user AS sagou "+ 
				          "RIGHT JOIN "+ 
				            "(SELECT "+ 
				              "eb.name AS name2, "+
				              "temp.userName, "+
				              "temp.id, "+
				              "temp.daogou, "+
				              "temp.areaGradeOfUser_id, "+
				              "temp.photo_id, "+
				              "temp.zhixian_id "+ 
				            "FROM "+
				              "ecm_bumen AS eb "+ 
				              "RIGHT JOIN "+ 
				                "(SELECT "+ 
				                  "su.areaGradeOfUser_id, "+
				                  "su.photo_id, "+
				                  "su.zhixian_id, "+
				                  "su.bumen_id, "+
				                  "su.userName, "+
				                  "su.id, "+
				                  "temp.daogou "+ 
				                "FROM "+
				                  "shopping_user AS su "+ 
				                  "RIGHT JOIN "+ 
				                    "(SELECT "+
				                      "o.user_id, "+
				                      "ROUND(SUM(o.daogou_get_price), 2) AS daogou "+ 
				                    "FROM "+
				                      "shopping_orderform AS o "+ 
				                    "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
				                      "AND o.payTimes >= '"+begin+"' "+  
				                      "AND o.payTimes <= '"+end+"' "+ 
				                    "GROUP BY o.user_id "+ 
				                    "ORDER BY daogou DESC "+ 
				                    "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp "+
				                    "ON su.id = temp.user_id) AS temp "+ 
				                "ON temp.bumen_id = eb.id) AS temp2 "+ 
				            "ON temp2.areaGradeOfUser_id = sagou.id) AS temp3 "+ 
				        "ON temp3.zhixian_id = ez.id) AS temp4 "+
				    "ON temp4.photo_id = sa.id "+ 
				"ORDER BY daogou DESC ";
		List<?> list =this.commonService.executeNativeNamedQuery(sql_str);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppAdminData.class, "firstData,sortData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "获取奖金日排行榜成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页奖金月排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMonthBonusRanking.htm", method = RequestMethod.POST)
	public void appGetMonthBonusRanking(HttpServletRequest request,HttpServletResponse response,String beginTime,String currentPage){
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new Date(System.currentTimeMillis()));
		if (!"".equals(CommUtil.null2String(beginTime))) {
			try {
				Date parse = df.parse(beginTime);
				date = CommUtil.formatShortDate(parse);
			} catch (ParseException e) {
				e.printStackTrace();
				ApiUtils.json(response, "", "时间参数错误", 1);
				return;
			}	
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin = ApiUtils.getFirstday_Lastday_Month(date, 0);
		String end = ApiUtils.getFirstday_Lastday_Month(date, 1);
		String sql_str="SELECT "+
				  "temp4.name2, "+
				  "temp4.userName, "+
				  "temp4.id, "+
				  "ROUND(temp4.daogou, 2) AS daogou, "+
				  "temp4.name3, "+
				  "temp4.zhixianName, "+
				  "sa.path, "+
				  "sa.name "+
				"FROM "+
				  "shopping_accessory AS sa "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+
				      "temp3.name2, "+
				      "temp3.userName, "+
				      "temp3.id, "+
				      "temp3.daogou, "+
				      "temp3.name AS name3, "+
				      "temp3.photo_id, "+
				      "ez.name AS zhixianName "+ 
				    "FROM "+
				      "shopping_zhixian AS ez "+
				      "RIGHT JOIN "+
				        "(SELECT "+ 
				          "temp2.name2, "+
				          "temp2.userName, "+
				          "temp2.id, "+
				          "temp2.daogou, "+
				          "sagou.name, "+
				          "temp2.photo_id, "+
				          "temp2.zhixian_id "+ 
				        "FROM "+
				          "shopping_area_grade_of_user AS sagou "+ 
				          "RIGHT JOIN "+ 
				            "(SELECT "+
				              "eb.name AS name2, "+
				              "temp.userName, "+
				              "temp.id, "+
				              "temp.daogou, "+
				              "temp.areaGradeOfUser_id, "+
				              "temp.photo_id, "+
				              "temp.zhixian_id "+
				            "FROM "+
				              "ecm_bumen AS eb "+ 
				              "RIGHT JOIN "+
				                "(SELECT "+
				                  "su.areaGradeOfUser_id, "+
				                  "su.photo_id, "+
				                  "su.zhixian_id, "+
				                  "su.bumen_id, "+
				                  "su.userName, "+
				                  "su.id, "+
				                  "temp.daogou "+
				                "FROM "+
				                  "shopping_user AS su "+
				                  "RIGHT JOIN "+ 
				                    "(SELECT "+ 
				                      "o.user_id, "+
				                      "ROUND(SUM(o.daogou_get_price), 2) AS daogou "+
				                    "FROM "+
				                      "shopping_orderform AS o "+ 
				                    "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
				                      "AND o.payTimes >= '"+begin+"' "+ 
				                      "AND o.payTimes <= '"+end+"' "+ 
				                    "GROUP BY o.user_id "+ 
				                    "ORDER BY daogou DESC "+ 
				                    "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp "+
				                    "ON su.id = temp.user_id) AS temp "+ 
				                "ON temp.bumen_id = eb.id) AS temp2 "+ 
				            "ON temp2.areaGradeOfUser_id = sagou.id) AS temp3 "+ 
				        "ON temp3.zhixian_id = ez.id) AS temp4 "+
				    "ON temp4.photo_id = sa.id "+ 
				"ORDER BY daogou DESC ";
		List<?> list =this.commonService.executeNativeNamedQuery(sql_str);
		ApiUtils.json(response, list, "获取奖金月排行榜成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页奖金年排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetYearBonusRanking.htm", method = RequestMethod.POST)
	public void appGetYearBonusRanking(HttpServletRequest request,HttpServletResponse response,String beginTime,String currentPage){
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
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		StringBuffer begin = new StringBuffer().append(date).append("-1-1 00:00:00");
		StringBuffer end = new StringBuffer().append(date).append("-12-31 23:59:59");
		String sql_str="SELECT "+ 
				  "temp4.name2, "+
				  "temp4.userName, "+
				  "temp4.id, "+
				  "ROUND(temp4.daogou, 2) AS daogou, "+
				  "temp4.name3, "+
				  "temp4.zhixianName, "+
				  "sa.path, "+
				  "sa.name "+
				"FROM "+
				  "shopping_accessory AS sa "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "temp3.name2, "+
				      "temp3.userName, "+
				      "temp3.id, "+
				      "temp3.daogou, "+
				      "temp3.name AS name3, "+
				      "temp3.photo_id, "+
				      "ez.name AS zhixianName "+ 
				    "FROM "+
				      "shopping_zhixian AS ez "+ 
				      "RIGHT JOIN "+ 
				        "(SELECT "+
				          "temp2.name2, "+
				          "temp2.userName, "+
				          "temp2.id, "+
				          "temp2.daogou, "+
				          "sagou.name, "+
				          "temp2.photo_id, "+
				          "temp2.zhixian_id "+ 
				        "FROM "+
				          "shopping_area_grade_of_user AS sagou "+
				          "RIGHT JOIN "+
				            "(SELECT "+
				              "eb.name AS name2, "+
				              "temp.userName, "+
				              "temp.id, "+
				              "temp.daogou, "+
				              "temp.areaGradeOfUser_id, "+
				              "temp.photo_id, "+
				              "temp.zhixian_id "+
				            "FROM "+
				              "ecm_bumen AS eb "+ 
				              "RIGHT JOIN "+ 
				                "(SELECT "+
				  "su.areaGradeOfUser_id, "+
				  "su.photo_id, "+
				  "su.zhixian_id, "+
				  "su.bumen_id, "+
				  "su.userName, "+
				  "su.id, "+
				  "temp.daogou "+ 
				"FROM "+
				  "shopping_user AS su "+
				  "RIGHT JOIN "+ 
				    "(SELECT "+
				      "o.user_id, "+
				      "ROUND(SUM(o.daogou_get_price), 2) AS daogou "+ 
				    "FROM "+
				      "shopping_orderform AS o "+
				    "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
				      "AND o.payTimes >= '"+begin+"' "+ 
				      "AND o.payTimes <= '"+end+"' "+ 
				    "GROUP BY o.user_id "+ 
				    "ORDER BY daogou DESC "+ 
				    "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp "+
				    "ON su.id = temp.user_id ) AS temp "+
				                "ON temp.bumen_id = eb.id) AS temp2 "+ 
				            "ON temp2.areaGradeOfUser_id = sagou.id) AS temp3 "+ 
				        "ON temp3.zhixian_id = ez.id) AS temp4 "+ 
				    "ON temp4.photo_id = sa.id "+
				"ORDER BY daogou DESC ";
		List<?> list =this.commonService.executeNativeNamedQuery(sql_str);
		ApiUtils.json(response, list, "获取奖金年排行榜成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页奖金总排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetTotalBonusRanking.htm", method = RequestMethod.POST)
	public void appGetTotalBonusRanking(HttpServletRequest request,HttpServletResponse response,String currentPage){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String sql_str="SELECT "+
				  "temp4.name2, "+
				  "temp4.userName, "+
				  "temp4.id, "+
				  "ROUND(temp4.daogou, 2) AS daogou, "+
				  "temp4.name3, "+
				  "temp4.zhixianName, "+
				  "sa.path, "+
				  "sa.name "+
				"FROM "+
				  "shopping_accessory AS sa "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "temp3.name2, "+
				      "temp3.userName, "+
				      "temp3.id, "+
				      "temp3.daogou, "+
				      "temp3.name AS name3, "+
				      "temp3.photo_id, "+
				      "ez.name AS zhixianName "+ 
				    "FROM "+
				      "shopping_zhixian AS ez "+ 
				      "RIGHT JOIN "+ 
				        "(SELECT "+ 
				          "temp2.name2, "+
				          "temp2.userName, "+
				          "temp2.id, "+
				          "temp2.daogou, "+
				          "sagou.name, "+
				          "temp2.photo_id, "+
				          "temp2.zhixian_id "+
				        "FROM "+
				          "shopping_area_grade_of_user AS sagou "+ 
				          "RIGHT JOIN "+
				            "(SELECT "+ 
				              "eb.name AS name2, "+
				              "temp.userName, "+
				              "temp.id, "+
				              "temp.daogou, "+
				              "temp.areaGradeOfUser_id, "+
				              "temp.photo_id, "+
				              "temp.zhixian_id "+ 
				            "FROM "+
				              "ecm_bumen AS eb "+
				              "RIGHT JOIN "+ 
				                "(SELECT "+ 
				                  "su.areaGradeOfUser_id, "+
				                  "su.photo_id, "+
				                  "su.zhixian_id, "+
				                  "su.bumen_id, "+
				                  "su.userName, "+
				                  "su.id, "+
				                  "temp.daogou "+ 
				                "FROM "+
				                  "shopping_user AS su "+
				                  "RIGHT JOIN "+ 
				                    "(SELECT "+ 
				                      "o.user_id, "+
				                      "ROUND(SUM(o.daogou_get_price), 2) AS daogou "+ 
				                    "FROM "+
				                      "shopping_orderform AS o "+
				                    "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
				                    "GROUP BY o.user_id "+ 
				                    "ORDER BY daogou DESC "+ 
				                    "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp "+ 
				                    "ON su.id = temp.user_id) AS temp "+
				                "ON temp.bumen_id = eb.id) AS temp2 "+ 
				            "ON temp2.areaGradeOfUser_id = sagou.id) AS temp3 "+ 
				        "ON temp3.zhixian_id = ez.id) AS temp4 "+ 
				    "ON temp4.photo_id = sa.id "+ 
				"ORDER BY daogou DESC ";
		List<?> list =this.commonService.executeNativeNamedQuery(sql_str);
		ApiUtils.json(response, list, "获取奖金总排行榜成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:通过的店铺的名字搜索店铺得到店铺列表,#1 模糊搜索店铺 #2 随机推荐的店铺，当模糊搜索的店铺大于20的时候，不推荐店铺信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSearchStoreByName.htm", method = RequestMethod.POST)
	public void appSearchStoreByName(HttpServletRequest request,
			HttpServletResponse response,String storeName,String currentPage){
		if (CommUtil.null2String(storeName).equals("")) {
			ApiUtils.json(response, "", "店铺名不能为空", 1);
			return;
		}
		//#1
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String sql="SELECT * "+
					"FROM (SELECT " +
					"SUM(obj.goods_salenum) AS sales," +
					"ss.id " +
					"FROM shopping_goods AS obj," +
					"shopping_store AS ss " +
					"WHERE ss.store_name LIKE '%"+storeName+"%' AND " +
					"ss.store_status =2 AND " +
					"obj.deleteStatus=false and "+
					"obj.goods_status = 0 AND  " +
					"obj.goods_inventory>0 AND "+
					"obj.goods_store_id=ss.id GROUP BY ss.id) AS temp "+
					"ORDER BY temp.sales DESC limit "+current_page*pageSize+","+pageSize ;
		List<?> storeIdAndSales=this.commonService.executeNativeNamedQuery(sql);
		List<List<StoreListData>> list=new ArrayList<List<StoreListData>>();
		List<StoreListData> storeListData = getStoreListData(storeIdAndSales);
		list.add(storeListData);
		//#2
		int size = storeIdAndSales.size();
		List<Object[]> recommendStoreList=new ArrayList<Object[]>();
		if (current_page==0) {
			if (size<20) {
				int st=20-size;
				if (st>10) {
					st=10;
				}
				String hql="select obj from RecommendStore as obj order by id desc";
				@SuppressWarnings("unchecked")
				List<RecommendStore> query = commonService.query(hql, null, 0, st);
				for (RecommendStore recommendStore:query) {
					Object[] obj=new Object[2];
					String sale_hql="select sum(obj.goods_salenum) from Goods as obj where obj.deleteStatus=false and obj.goods_store.id =" + recommendStore.getStore().getId();
					List<?> sale = commonService.query(sale_hql, null, -1, -1);
					obj[0]=0;
					if (sale.size()>0) {
						if (sale.get(0)!=null) {
							obj[0]=(Long)sale.get(0);
						}			
					}
					obj[1]=recommendStore.getStore().getId();
					recommendStoreList.add(obj);
				}
			}
		}
		List<StoreListData> recommendstoreListData = getStoreListData(recommendStoreList);
		list.add(recommendstoreListData);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(StoreListData.class, "store,salesNum,goodsTotal"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone,store_logo,goods_list,user,store_logo"));
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_price,store_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(User.class, "id"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "模糊查询店铺成功", 0, filter);
	}
	
	private List<StoreListData> getStoreListData(List<?> storeIdAndSales){
		List<StoreListData> storeListDataList=new ArrayList<StoreListData>();
		for(int i=0;i<storeIdAndSales.size();i++){
			Object[] storeIdAndSalesArray=(Object[]) storeIdAndSales.get(i);
			Store store=this.storeService.getObjById(CommUtil.null2Long(storeIdAndSalesArray[1]));
			String goods_hql="select count(obj) from Goods as obj where obj.deleteStatus=false and obj.goods_status = 0 and obj.goods_inventory>0 and obj.goods_store.id =" + store.getId() ;
			List<?> goods = commonService.query(goods_hql, null, -1, -1);
			int  totalNums=0;
			if (goods.size()>0) {
				totalNums=((Long)goods.get(0)).intValue();
				String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_store.id="+store.getId().longValue()+" and obj.goods_status=0 and obj.goods_inventory>0 order by obj.goods_salenum desc";
				List<Goods> goodsList=this.goodsService.query(hql, null, 0, 3);
				store.setGoods_list(goodsList);
				StoreListData storeListData=new StoreListData();
				storeListData.setStore(store);
				storeListData.setSalesNum(CommUtil.null2Long(storeIdAndSalesArray[0]));
				storeListData.setGoodsTotal(CommUtil.null2Long(totalNums));
				storeListDataList.add(storeListData);	
			}
		}
		return storeListDataList;
	}
	
	
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页销量排行(年月日) #1 日排行榜 #2 月排行榜  #3 年排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGainGoodSequence.htm", method = RequestMethod.POST)
	public void appGainGoodSequence(HttpServletRequest request,HttpServletResponse response,String Choice,String currentPage){
//		if (CommUtil.null2String(currentPage).equals("0")||CommUtil.null2String(currentPage).equals("")) {
			String msg="销量排行榜";
			String time="";
			if (CommUtil.null2String(Choice).equals("0")) {//#1
				msg="获取日销量排行榜";
				time=ApiUtils.weeHours(CommUtil.formatTime( "yyyy-MM-dd",new Date()), 0);
			}else if (CommUtil.null2String(Choice).equals("1")) {//#2
				msg="获取月销量排行榜";
				time=ApiUtils.getFirstday_Lastday_Month(CommUtil.formatTime( "yyyy-MM-dd",new Date()), 0);
			}else if (CommUtil.null2String(Choice).equals("2")) {//#3
				msg="获取年销量排行榜";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
				String date = sdf.format(new Date(System.currentTimeMillis()));	
				StringBuffer begin = new StringBuffer().append(date).append("-1-1 00:00:00");
				time=begin.toString();
			}else{
				ApiUtils.json(response, "", "参数有误", 1);
				return;
			}
			int current_page=0;
			int pageSize=20;
			if("".equals(currentPage)||currentPage==null){
				current_page=0;
			}else{
				current_page=Integer.valueOf(currentPage).intValue();
			}
			String sql="SELECT " +
						"temp2.goods_id," +
						"temp2.goods_name," +
						"temp2.num," +
						"temp2.store_price," +
						"sa.path,sa.name," +
						"sa.ext " +
					"FROM shopping_accessory AS sa " +
					"RIGHT JOIN " +
						"(" +
							"SELECT " +
							"temp.goods_id," +
							"temp.num," +
							"sg.goods_name," +
							"sg.store_price," +
							"sg.goods_main_photo_id " +
							"FROM shopping_goods AS sg," +
								"(" +
									"SELECT sg.goods_id," +
									"SUM(sg.count) AS num " +
									"FROM shopping_goodscart AS sg," +
									"shopping_orderform AS so " +
									"WHERE so.order_status IN (20,30,40,50,60) " +
									"AND so.id=sg.of_id " +
									"AND so.addTime>'"+time+"' " +
									"GROUP BY sg.goods_id " +
//									"ORDER BY num DESC LIMIT 0,20 " +
									"ORDER BY num DESC LIMIT " + current_page*pageSize + "," + pageSize+
								") AS temp WHERE temp.goods_id=sg.id and " +
								"sg.deleteStatus=false" +
							") AS temp2 ON temp2.goods_main_photo_id=sa.id";
			List<?> query = commonService.executeNativeNamedQuery(sql);
			ApiUtils.json(response, query, msg, 0);
			return;
//		}else {
//			List<?> list=new ArrayList();
//			ApiUtils.json(response, list, "", 0);
//			return;
//		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页奖金日排行榜
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetBonusRankings.htm", method = RequestMethod.POST)
	public void appGetBonusRankings(HttpServletRequest request,HttpServletResponse response,String beginTime,String currentPage){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String begin =ApiUtils.weeHours(beginTime, 0);
		String end = ApiUtils.weeHours(beginTime, 1);
		String sql_str="SELECT "+
				  "temp4.name2, "+
				  "temp4.userName, "+
				  "temp4.id, "+
				  "ROUND(temp4.daogou, 2) AS daogou, "+
				  "temp4.name3, "+
				  "temp4.zhixianName, "+
				  "sa.path, "+
				  "sa.name "+ 
				"FROM "+
				  "shopping_accessory AS sa "+ 
				  "RIGHT JOIN "+ 
				    "(SELECT "+ 
				      "temp3.name2, "+
				      "temp3.userName, "+
				      "temp3.id, "+
				      "temp3.daogou, "+
				      "temp3.name AS name3, "+
				      "temp3.photo_id, "+
				      "ez.name AS zhixianName "+ 
				    "FROM "+
				      "shopping_zhixian AS ez "+ 
				      "RIGHT JOIN "+ 
				        "(SELECT "+ 
				          "temp2.name2, "+
				          "temp2.userName, "+
				          "temp2.id, "+
				          "temp2.daogou, "+
				          "sagou.name, "+
				          "temp2.photo_id, "+
				          "temp2.zhixian_id "+ 
				        "FROM "+
				          "shopping_area_grade_of_user AS sagou "+ 
				          "RIGHT JOIN "+ 
				            "(SELECT "+ 
				              "eb.name AS name2, "+
				              "temp.userName, "+
				              "temp.id, "+
				              "temp.daogou, "+
				              "temp.areaGradeOfUser_id, "+
				              "temp.photo_id, "+
				              "temp.zhixian_id "+ 
				            "FROM "+
				              "ecm_bumen AS eb "+ 
				              "RIGHT JOIN "+ 
				                "(SELECT "+ 
				                  "su.areaGradeOfUser_id, "+
				                  "su.photo_id, "+
				                  "su.zhixian_id, "+
				                  "su.bumen_id, "+
				                  "su.userName, "+
				                  "su.id, "+
				                  "temp.daogou "+ 
				                "FROM "+
				                  "shopping_user AS su "+ 
				                  "RIGHT JOIN "+ 
				                    "(SELECT "+
				                      "o.user_id, "+
				                      "ROUND(SUM(o.daogou_get_price), 2) AS daogou "+ 
				                    "FROM "+
				                      "shopping_orderform AS o "+ 
				                    "WHERE o.order_status IN (20, 30, 40, 50, 60) "+ 
				                      "AND o.payTimes >= '"+begin+"' "+  
				                      "AND o.payTimes <= '"+end+"' "+ 
				                    "GROUP BY o.user_id "+ 
				                    "ORDER BY daogou DESC "+ 
				                    "LIMIT " + current_page*pageSize + ", " + pageSize + ") AS temp "+
				                    "ON su.id = temp.user_id) AS temp "+ 
				                "ON temp.bumen_id = eb.id) AS temp2 "+ 
				            "ON temp2.areaGradeOfUser_id = sagou.id) AS temp3 "+ 
				        "ON temp3.zhixian_id = ez.id) AS temp4 "+
				    "ON temp4.photo_id = sa.id "+ 
				"ORDER BY daogou DESC ";
		List<?> list =this.commonService.executeNativeNamedQuery(sql_str);
		AppAdminData data=new AppAdminData();
		data.setFirstData(CommUtil.formatShortDate(CommUtil.formatDate(begin, "yyyy-MM-dd")));
		data.setSortData(list);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppAdminData.class, "firstData,sortData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, data, "获取奖金日排行榜成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app首页闪屏页
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetAppWelcomeImg.htm", method = RequestMethod.POST)
	public void appGetAppWelcomeImg(HttpServletRequest request,HttpServletResponse response){
		String hql="select obj from AppWelcomeImg as obj where obj.imgState = 1 order by obj.addTime DESC";
		List<AppWelcomeImg> appWelcomeImgs = commonService.query(hql, null, -1, -1);
		AppWelcomeImg appWelcomeImg=new AppWelcomeImg();
		if (appWelcomeImgs.size()>0) {
			appWelcomeImg=appWelcomeImgs.get(0);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppWelcomeImg.class, "id,imgState,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appWelcomeImg, "获取app欢迎页成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app分享页图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetAppShareImg.htm", method = RequestMethod.POST)
	public void appGetAppShareImg(HttpServletRequest request,HttpServletResponse response){
		String hql="select obj from AppShareImg as obj where obj.imgState = 1 order by obj.imgOrder";
		List<AppShareImg> appShareImgs  = commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppShareImg.class, "id,showPhoto,operatePhoto,imgState,imgOrder,headPortraitState,fontState,fontSize,fontColor,fontContent,fontUpRange,fontLeftRange,qRCodeUpRange,qRCodeLeftRange,qRCodeWidth,qRCodeHeight,headPortraitUpRange,headPortraitLeftRange,headPortraitWidth,headPortraitHeight"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appShareImgs, "获取app分享图片成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:共享商品 ,按添加顺序排序
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetShareGoods.htm", method = RequestMethod.POST)
	public void appGetShareGoods(HttpServletRequest request,HttpServletResponse response,String currentPage){
		int current_page=0;
		int pageSize=10;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from Goods AS obj where obj.deleteStatus=false and obj.goods_status = 0 and obj.goods_store.store_status = 2 order by obj.addTime DESC";
		List<Goods> goods = goodsService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goods, "获取共享商品成功", 0,filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:招商动态浮窗
	 *@function:**   
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value="/appGetRecommendSellerList.htm", method = RequestMethod.POST )
	public void appGetRecommendSellerList(HttpServletRequest request,HttpServletResponse response){
		String sql="SELECT "+
					  "temp5.id, "+
					  "temp5.userName, "+
					  "temp5.zhiweiName, "+
					  "temp5.bumenName, "+
					  "sz.name, "+
					  "temp5.sellerUserName "+ 
					"FROM "+
					  "shopping_zhixian AS sz "+ 
					  "RIGHT JOIN "+ 
					    "(SELECT "+ 
					      "temp4.id, "+
					      "temp4.userName, "+
					      "temp4.addTime, "+
					      "temp4.sellerUserName, "+
					      "temp4.zhiweiName, "+
					      "eb.name AS bumenName, "+
					      "temp4.zhixian_id "+ 
					    "FROM "+
					      "ecm_bumen AS eb "+ 
					      "RIGHT JOIN "+ 
					        "(SELECT "+ 
					          "temp3.id, "+
					          "temp3.userName, "+
					          "temp3.addTime, "+
					          "temp3.sellerUserName, "+
					          "ez.name AS zhiweiName, "+
					          "temp3.bumen_id, "+
					          "temp3.zhixian_id "+ 
					        "FROM "+
					          "ecm_zhiwei AS ez "+ 
					          "RIGHT JOIN "+ 
					            "(SELECT "+ 
					              "obj.id, "+
					              "obj.userName, "+
					              "temp1.addTime, "+
					              "temp1.sellerUserName, "+
					              "obj.zhiwei_id, "+
					              "obj.bumen_id, "+
					              "obj.zhixian_id "+ 
					            "FROM "+
					              "shopping_user AS obj "+ 
					              "RIGHT JOIN "+ 
					                "(SELECT "+ 
					                  "temp2.addTime, "+
					                  "temp2.sellerUserName, "+
					                  "( "+
					                    "CASE "+
					                      "WHEN temp2.dan_bao_ren = '' "+ 
					                      "OR temp2.dan_bao_ren IS NULL "+ 
					                      "THEN '夏天先生' "+ 
					                      "ELSE temp2.dan_bao_ren "+ 
					                    "END "+
					                  ") AS danbaoren "+ 
					                "FROM "+
					                  "(SELECT "+ 
					                    "temp.addTime, "+
					                    "su.userName AS sellerUserName, "+
					                    "su.dan_bao_ren "+ 
					                  "FROM "+
					                    "shopping_user AS su "+ 
					                    "RIGHT JOIN "+ 
					                      "(SELECT "+ 
					                        "ss.addTime, "+
					                        "ss.id "+ 
					                      "FROM "+
					                        "shopping_store AS ss "+ 
					                      "ORDER BY ss.addTime DESC "+ 
					                      "LIMIT 0, 20) AS temp "+ 
					                      "ON temp.id = su.store_id) AS temp2) AS temp1 "+ 
					                "ON temp1.danbaoren = obj.userName) AS temp3 "+ 
					            "ON ez.id = temp3.zhiwei_id) AS temp4 "+ 
					        "ON temp4.bumen_id = eb.id) AS temp5 "+ 
					    "ON temp5.zhixian_id = sz.id "+ 
					"ORDER BY temp5.addTime DESC";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, query, "获取招商浮窗成功", 0);
		return;	
	}
	private int getAppHomePayHour(int purchase_timeDuan){
		int hour=purchase_timeDuan;
		AppHomePageSwitchEntity ahpse=(AppHomePageSwitchEntity) this.commonService.getById("AppHomePageSwitchEntity", "1");
		if (!(ahpse.getMaxPayNum()>=purchase_timeDuan&&purchase_timeDuan>0)) {
			return 0;
		}
		if ("元/天".equals(ahpse.getPayType())) {
			hour=24*purchase_timeDuan;
		}
		return hour;
	}
	/**
     * 给时间加上几个小时
     * @param date 当前时间
     * @param hour 需要加的时间
     * @return
     */
	private Date addDateMinut(Date date, int hour){   
        Calendar cal = Calendar.getInstance();   
        cal.setTime(date);   
        cal.add(Calendar.HOUR, hour);// 24小时制   
        date = cal.getTime();   
        cal = null;   
        return date;   

    }
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页banner空位的积分支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_balancePayments.htm", method = RequestMethod.POST)
	public void app_vacantPosition_balancePayments(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			String goodsId,String userId){
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="select obj from AppHomePageEntity as obj where obj.id="+id;
		List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
		AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
		boolean ret=appHome.getIs_can_buy();
		if(ret){
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			double toatal=CommUtil.formatDouble(purchase_timeDuan*appHome.getBanner_price(), 2);
			double userBalance=user.getAvailableBalance().doubleValue();
			boolean is_can_buy=false;
			if(user.getFreezeBlance().intValue()==1){
				if(userBalance-toatal-1000>=0){
					is_can_buy=true;
				}
			}else{
				if(userBalance-toatal>=0){
					is_can_buy=true;
				}
			}
			if(is_can_buy){
				int hour=this.getAppHomePayHour(purchase_timeDuan);
				if (hour==0) {
					ApiUtils.json(response, "", "购买时间超出系统允许的范围", 1);
					return;
				}
				User countUser=this.userService.getObjById(1L);
				boolean isRepeat=this.judgeIsRepetition(goodsId,"AppHomePageEntity");
				if(isRepeat){
					ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
					return;
				}
				user.setAvailableBalance(BigDecimalUtil.sub(
						userBalance, toatal));
				boolean up_ret=this.userService.update(user);
				if(up_ret){
					PredepositLog buyBanner_log = new PredepositLog();
					buyBanner_log.setAddTime(new Date());
					buyBanner_log.setPd_log_user(user);
					buyBanner_log.setPd_op_type("减少");
					buyBanner_log.setPd_log_amount(BigDecimal.valueOf(-toatal));
					buyBanner_log.setPd_log_info("用于支付首页banner的空位");
					buyBanner_log.setPd_type("可用预存款");
					buyBanner_log.setCurrent_price(user.getAvailableBalance().doubleValue());
					boolean rr=this.predepositLogService.save(buyBanner_log);
					
					countUser.setAvailableBalance(BigDecimalUtil.add(
							countUser.getAvailableBalance().doubleValue(), toatal));
					this.userService.update(countUser);
					PredepositLog countUser_log = new PredepositLog();
					countUser_log.setAddTime(new Date());
					countUser_log.setPd_log_user(countUser);
					countUser_log.setPd_op_type("增加");
					countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
					countUser_log.setPd_log_info("首页banner空位的收入");
					countUser_log.setPd_type("可用预存款");
					countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
					boolean rr1=this.predepositLogService.save(countUser_log);
					if(rr&&rr1){
						goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
						Date date=new Date();
						appHome.setIs_can_buy(false);
						appHome.setPurchase_timeDuan(purchase_timeDuan);
						appHome.setGoods(goods);
						appHome.setStart_time(date);
						appHome.setFlush_time(this.addDateMinut(date,hour));
						this.commonService.update(appHome);
						ApiUtils.json(response, "", "购买成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 2);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页普通空位的积分支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_commonPayments.htm", method = RequestMethod.POST)
	public void app_vacantPosition_commonPayments(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			String goodsId,String userId){
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
		List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
		AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
		boolean ret=appHome.getIs_can_buy();
		if(ret){
			int hour=this.getAppHomePayHour(purchase_timeDuan);
			if (hour==0) {
				ApiUtils.json(response, "", "购买时间超出系统允许的范围", 1);
				return;
			}
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			double toatal=CommUtil.formatDouble(purchase_timeDuan*appHome.getCommonPosition_price(), 2);
			double userBalance=user.getAvailableBalance().doubleValue();
			boolean is_can_buy=false;
			if(user.getFreezeBlance().intValue()==1){
				if(userBalance-toatal-1000>=0){
					is_can_buy=true;
				}
			}else{
				if(userBalance-toatal>=0){
					is_can_buy=true;
				}
			}
			if(is_can_buy){
				User countUser=this.userService.getObjById(1L);
				boolean isRepeat=this.judgeIsRepetition(goodsId,"AppHomePageCommonPosition");
				if(isRepeat){
					ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
					return;
				}
				user.setAvailableBalance(BigDecimalUtil.sub(
						userBalance, toatal));
				boolean up_ret=this.userService.update(user);
				if(up_ret){
					PredepositLog buyBanner_log = new PredepositLog();
					buyBanner_log.setAddTime(new Date());
					buyBanner_log.setPd_log_user(user);
					buyBanner_log.setPd_op_type("减少");
					buyBanner_log.setPd_log_amount(BigDecimal.valueOf(-toatal));
					buyBanner_log.setPd_log_info("用于支付首页普通区域的空位");
					buyBanner_log.setPd_type("可用预存款");
					buyBanner_log.setCurrent_price(user.getAvailableBalance().doubleValue());
					boolean rr=this.predepositLogService.save(buyBanner_log);
					
					countUser.setAvailableBalance(BigDecimalUtil.add(
							countUser.getAvailableBalance().doubleValue(), toatal));
					this.userService.update(countUser);
					PredepositLog countUser_log = new PredepositLog();
					countUser_log.setAddTime(new Date());
					countUser_log.setPd_log_user(countUser);
					countUser_log.setPd_op_type("增加");
					countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
					countUser_log.setPd_log_info("首页banner空位的收入");
					countUser_log.setPd_type("可用预存款");
					countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
					boolean rr1=this.predepositLogService.save(countUser_log);
					if(rr&&rr1){
						goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
						Date date=new Date();
						appHome.setIs_can_buy(false);
						appHome.setPurchase_timeDuan(purchase_timeDuan);
						appHome.setGoods(goods);
						appHome.setStart_time(date);
						appHome.setFlush_time(this.addDateMinut(date,hour));
						this.commonService.update(appHome);
						ApiUtils.json(response, "", "购买成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "您的余额不足,谢谢惠顾", 2);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页空位的微信支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_weixinPayments.htm", method = RequestMethod.POST)
	public void app_vacantPosition_weixinPayments(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			Long goodsId,String userId,String vacantPositionType){
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="";
		boolean ret=false;
		boolean isRepeat=false;
		double price=0.00D;
		String notify_url=CommUtil.getURL(request)+"/app_HomePageWeixinPay_Callback.htm";
		if("banner".equals(vacantPositionType)){
			hql="select obj from AppHomePageEntity as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getBanner_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageEntity");
		}else if("common".equals(vacantPositionType)){
			hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getCommonPosition_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageCommonPosition");
		}
		if(ret){
			if(isRepeat){
				ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
				return;
			}else{
				int hour=this.getAppHomePayHour(purchase_timeDuan);
				if (hour==0) {
					ApiUtils.json(response, "", "购买时间超出系统允许的范围", 1);
					return;
				}
				double payTotal=purchase_timeDuan*price;
				Date date=new Date();
				AppHomePageTemporaryData appHome=new AppHomePageTemporaryData();
				appHome.setVacantPositionId(CommUtil.null2Long(id));
				appHome.setPurchase_timeDuan(purchase_timeDuan);
				appHome.setGoodsId(goodsId);
				appHome.setAddTime(new Date());
				appHome.setVacantPositionType(vacantPositionType);
				appHome.setIs_can_buy(CommUtil.null2Boolean(false));
				appHome.setStart_time(date);
				appHome.setFlush_time(this.addDateMinut(date,hour));
				appHome.setTotal(payTotal);
				appHome.setOrderStatus(10);
				appHome.setOrderNum(ApiUtils.integralOrderNum(CommUtil.null2Long(userId)).toString());
				this.commonService.save(appHome);
				Map<String, String> out_put_params = null;//payTotal
				try {
					out_put_params = ApiUtils.get_weixin_sign_string(appHome.getOrderNum()+"",
							notify_url,payTotal+"");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ApiUtils.json(response, out_put_params, "获取支付信息成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢惠顾", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:首页空位的支付宝支付
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_vacantPosition_albbPayments.htm", method = RequestMethod.POST)
	public void app_vacantPosition_albbPayments(HttpServletRequest request,
			HttpServletResponse response,String id,Integer purchase_timeDuan,
			Long goodsId,String userId,String vacantPositionType){
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods.getGoods_salenum()<1){
			ApiUtils.json(response, "", "为了更好的客户体验，请选择销量高于1的商品上首页", 3);
			return;
		}
		String hql="";
		boolean ret=false;
		boolean isRepeat=false;
		double price=0.00D;
		if("banner".equals(vacantPositionType)){
			hql="select obj from AppHomePageEntity as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageEntity appHome=(AppHomePageEntity) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getBanner_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageEntity");
		}else if("common".equals(vacantPositionType)){
			hql="select obj from AppHomePageCommonPosition as obj where obj.id="+id;
			List<?>  AppHomeList=this.commonService.query(hql, null, -1, -1);
			AppHomePageCommonPosition appHome=(AppHomePageCommonPosition) AppHomeList.get(0);
			ret=appHome.getIs_can_buy();
			price=appHome.getCommonPosition_price();
			isRepeat=this.judgeIsRepetition(goodsId+"","AppHomePageCommonPosition");
		}
		if(ret){
			if(isRepeat){
				ApiUtils.json(response, "", "为了让首页多元化,您不能同时购买同一件商品", 3);
				return;
			}else{
				int hour=this.getAppHomePayHour(purchase_timeDuan);
				if (hour==0) {
					ApiUtils.json(response, "", "购买时间超出系统允许的范围", 1);
					return;
				}
				double payTotal=purchase_timeDuan*price;//payTotal
				Date date=new Date();
				AppHomePageTemporaryData appHome=new AppHomePageTemporaryData();
				appHome.setVacantPositionId(CommUtil.null2Long(id));
				appHome.setPurchase_timeDuan(purchase_timeDuan);
				appHome.setGoodsId(goodsId);
				appHome.setAddTime(new Date());
				appHome.setVacantPositionType(vacantPositionType);
				appHome.setIs_can_buy(CommUtil.null2Boolean(false));
				appHome.setStart_time(date);
				appHome.setFlush_time(this.addDateMinut(date,hour));
				appHome.setTotal(payTotal);
				appHome.setOrderStatus(10);
				appHome.setOrderNum(ApiUtils.integralOrderNum(CommUtil.null2Long(userId)).toString());
				this.commonService.save(appHome);
				String str = ApiUtils.getAlipayStr(appHome.getOrderNum()+"",
						CommUtil.getURL(request)+"/app_HomePageAliPay_Callback.htm", payTotal+"");
				ApiUtils.json(response, str, "获取支付信息成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该空位已被购买,请下次再来,谢谢惠顾", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app端获取banner位,common的空位
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appAcquireBannersAndCommons.htm", method = RequestMethod.POST)
	public void appAcquireBannersAndCommons(HttpServletRequest request,
			HttpServletResponse response){
		String hql="select obj from AppHomePageEntity as obj";
		List<?> appHomeBanner=this.commonService.query(hql, null, 0, 5);
		AppTransferData appTransferData=new AppTransferData();
		appTransferData.setFirstData(appHomeBanner);
		hql="select obj from AppHomePageCommonPosition as obj";
		List<?> appHomeCommon=this.commonService.query(hql, null, 0, 12);
		appTransferData.setSecondData(appHomeCommon);
		appTransferData.setFifthData(commonService.getById("AppHomePageSwitchEntity", "1"));
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppHomePageCommonPosition.class, "id,sequence,position_name,is_can_buy,commonPosition_price"));
		objs.add(new FilterObj(AppHomePageSwitchEntity.class, "payType,maxPayNum"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData,fifthData"));
		objs.add(new FilterObj(AppHomePageEntity.class, "id,sequence,position_name,is_can_buy,banner_price"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appTransferData, "查询成功", 0, filter);
		return;
	}
}
