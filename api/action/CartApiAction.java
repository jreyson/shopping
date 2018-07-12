package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import sun.jdbc.odbc.OdbcDef;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.output.OrderShowData;
import com.shopping.api.service.IGoodsApiService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.FenPei;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Payment;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.SpecifiList;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.Transport;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IFenPeiService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IGroupGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPaymentService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.ITransportService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.view.web.tools.SpecTools;
/**
 * @author:akangah
 * @description:购物车相关的控制器类
 * @classType:action类
 */
@Controller
public class CartApiAction {
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsApiService goodsApiService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IStoreCartService storeCartService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IFenPeiService fenPeiService;
	@Autowired
	private IPredepositLogService predepositLogService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:添加购物车
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:如果这个用户从来没有店铺购物车,也就是size大小为0
	 *				 #2:如果这个用户店铺购物车不空的话,也就是size的大小大于0,
	 *					这里分以下几种情况考虑
	 *					第一种情况:先判断是不是同一家店铺,用户是否以前曾在这家店铺
	 *							    买过东西,如果存在这样的店铺购物车的话,再判断用户
	 *							    要买的商品是不是已经存在,如果存在,再判断规格是否
	 *							    统一,如果规格统一的话,修改商品购物车的数量即可,
	 *							    如果规格不统一,那么新建一个商品购物车,店铺购物车
	 *							    保存通过遍历找到的那个
	 *				    第二种情况:如果用户没有这样的店铺购物车,那么新建店铺购物车和新建
	 *							商品购物车,根据对应的关系保存即可.
	 *				#3:有规格的同一件商品合并更新
	 *				#4:没有规格的同一件商品合并更新
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_add_cart.htm" })
	public void api_add_cart(HttpServletRequest request,
			HttpServletResponse response, String id, String count, String gsp,
			String buy_type, String user_id, String tuijian_id) {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if (CommUtil.null2Int(count)==0) {
			ApiUtils.json(response, "", "数量参数错误", 1);
			return;
		}
		List<StoreCart> cart = this.cart_calc(user_id,"");
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		User user=this.userService.getObjById(Long.valueOf(user_id));
		StoreCart storeCart=new StoreCart();
		GoodsCart goodsCart=new GoodsCart();
		tuijian_id = CommUtil.null2String(tuijian_id);
		if(cart.size()==0){//#1
			storeCart=this.generateStoreCart(storeCart,goods,user,gsp,count);
			this.storeCartService.save(storeCart);
			goodsCart=this.generateGoodsCart(tuijian_id, goodsCart, 
					gsp, count, goods, storeCart);
			this.goodsCartService.save(goodsCart);
			ApiUtils.json(response, "", "添加成功", 0);
		}else if(cart.size()>0){//#2
			for(StoreCart store_cart:cart){
				if(store_cart.getStore().getId().toString().equals(goods.getGoods_store().getId().toString())){
					Specifi specifi1 = (Specifi) commonService.getByWhere("Specifi",
							"goods_id=" + goods.getId() + " and specifi='" + gsp
									+ "'");
					specifi1 = specifi1 == null ? new Specifi() : specifi1;
					Double setTotal_price=store_cart.getTotal_price()==null?0:store_cart.getTotal_price().doubleValue();
					store_cart.setTotal_price(BigDecimal.valueOf(setTotal_price+CommUtil
							.null2Double(specifi1.getPrice() == 0 ? goods
									.getStore_price().doubleValue()*CommUtil.null2Double(count) : specifi1.getPrice())*CommUtil.null2Double(count)));
					this.storeCartService.update(store_cart);
					String hql="select obj from GoodsCart as obj where obj.sc.id="+store_cart.getId().toString()+" and obj.deleteStatus=false";
					List<GoodsCart> goodsCartList=this.goodsCartService.query(hql, null, -1, -1);
					for(GoodsCart goods_cart:goodsCartList){
						if(goods_cart.getGoods().getId().longValue()==Long.valueOf(id).longValue()){
							Specifi specifi = (Specifi) commonService.getByWhere("Specifi",
									"goods_id=" + goods.getId() + " and specifi='" + gsp
											+ "'");
							Specifi specifi2 = (Specifi) commonService.getByWhere("Specifi",
									"goods_id=" + goods.getId() + " and specifi='" + goods_cart.getSpec_info_key()
											+ "'");
							/*if(specifi!=null&&goods_cart.getSpecification()!=null){//#3
								if(specifi.getId()==goods_cart.getSpecification().getId()){
									goods_cart.setCount(Integer.valueOf(count)+goods_cart.getCount());
									this.goodsCartService.update(goods_cart);
									ApiUtils.json(response, "", "添加成功", 0);
									return;
								}
							}*/
							if(specifi!=null&&specifi2!=null){
								if(specifi.getId()==specifi2.getId()){
									goods_cart.setCount(Integer.valueOf(count)+goods_cart.getCount());
									this.goodsCartService.update(goods_cart);
									ApiUtils.json(response, "", "添加成功", 0);
									return;
								}
							}
							if(specifi==null&&specifi2==null){//#4
								goods_cart.setCount(Integer.valueOf(count)+goods_cart.getCount());
								this.goodsCartService.update(goods_cart);
								ApiUtils.json(response, "", "添加成功", 0);
								return;
							}
						}
					}
					goodsCart=this.generateGoodsCart(tuijian_id, goodsCart,
							gsp, count, goods, store_cart);
					this.goodsCartService.save(goodsCart);
					ApiUtils.json(response, "", "添加成功", 0);
					return;
				}
			}
			storeCart=this.generateStoreCart(storeCart,goods,user,gsp,count);
			this.storeCartService.save(storeCart);
			goodsCart=this.generateGoodsCart(tuijian_id, goodsCart, 
					gsp, count, goods, storeCart);
			this.goodsCartService.save(goodsCart);
			ApiUtils.json(response, "", "添加成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:查看购物车,支持分页获取查看
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/cart_list.htm" })
	public void cart_list(HttpServletRequest request,
			HttpServletResponse response, String user_id,String currentPage) {
		if("".equals(CommUtil.null2String(currentPage))){
			currentPage="0";
		}
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		List<StoreCart> cart_list = this.cart_calc(user_id,currentPage);
		for(StoreCart storeCart:cart_list){
			String hql="select obj from GoodsCart as obj where obj.sc.id="+storeCart.getId().toString()+" and obj.deleteStatus=false order by obj.addTime DESC";
			List<GoodsCart> goodsCartList=this.goodsCartService.query(hql, null, -1, -1);
			storeCart.setGcs(goodsCartList);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(StoreCart.class, "id,store,gcs,total_price"));
		objs.add(new FilterObj(GoodsCart.class,
				"id,gcs,count,goods,spec_info_key,price,specification"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_name,goods_price,store_price,goods_main_photo,goods_status,goods_inventory"));
		objs.add(new FilterObj(Store.class, "store_name,id,store_status"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Specifi.class, "id,specifi"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, cart_list, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:删除购物车
	 *@function:**
	 *@exception:#1:删除商品购物车
	 *			 #2:如果该用户的店铺购物车下面的商品购物车为空,那么删除该店铺购物车
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_remove_cart.htm" })
	public void api_remove_cart(HttpServletRequest request,
			HttpServletResponse response, String goodsCartIds,String userId) {
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String[] goodsCart_ids=goodsCartIds.split(",");
		for(String id:goodsCart_ids){//#1
			this.goodsCartService.delete(CommUtil.null2Long(id));
		}
		List<StoreCart> storeCarts=this.cart_calc(userId, "");
		for(StoreCart perStore:storeCarts){//#2
			String hql="select obj from GoodsCart as obj where obj.sc.id="+perStore.getId().toString()+" and obj.deleteStatus=false";
			List<GoodsCart> goodsCartList=this.goodsCartService.query(hql, null, -1, -1);
			if(goodsCartList.size()==0){ 
				perStore.setSc_status(1);
				this.storeCartService.update(perStore);
			}else {
				double total_price=0;
//						perStore.getTotal_price()==null?0:perStore.getTotal_price().doubleValue();;
				for (GoodsCart goodsCart : goodsCartList) {
					//计算该店铺剩余价钱
					Specifi specifi = goodsCart.getSpecification() == null ? new Specifi() : goodsCart.getSpecification();
					total_price+=CommUtil.null2Double((specifi.getPrice() == 0) ? (goodsCart.getGoods()
									.getStore_price().doubleValue()*CommUtil.null2Double(goodsCart.getCount())) : (specifi.getPrice())*CommUtil.null2Double(goodsCart.getCount()));
				}
				perStore.setSc_status(0);
				perStore.setTotal_price(BigDecimal.valueOf(total_price));
				this.storeCartService.update(perStore);
			}
		}
		ApiUtils.json(response, "", "删除成功", 0);
	}
	/***
	 *@author:akangah
	 *@param:****
	 *@return:void
	 *@param:数据的结构是[{"goodsCartId":"","gsp":"","count":""},{},{}]
	 *@description:编辑购物车
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_edit_cart.htm" })
	public void api_edit_cart(HttpServletRequest request,
			HttpServletResponse response,String paramsList){
		List<JSONObject> params=JSON.parseArray(paramsList,JSONObject.class);
		for(JSONObject obj:params){
			GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(obj.getString("goodsCartId")));
			Specifi specifi = (Specifi) commonService.getByWhere("Specifi",
					"goods_id=" + goodsCart.getGoods().getId() + " and specifi='" +obj.getString("gsp")
							+ "'");
			//goodsCart.setSpecification(specifi);
			goodsCart.setCount(CommUtil.null2Int(obj.getString("count")));
			if(specifi==null){
				goodsCart.setSpec_info_key("");
			}else{
				goodsCart.setSpec_info_key(specifi.getSpecifi());
			}
			boolean update = this.goodsCartService.update(goodsCart);
			if (update) {//修改购物车的总价
				String hql="select obj from GoodsCart as obj where obj.sc.id="+goodsCart.getSc().getId().toString()+" and obj.deleteStatus=false";
				List<GoodsCart> goodsCartList=this.goodsCartService.query(hql, null, -1, -1);
				double total_price=0;
				for (GoodsCart gc : goodsCartList) {
					//计算该店铺剩余价钱
					Specifi s = gc.getSpecification() == null ? new Specifi() : gc.getSpecification();
					total_price+=CommUtil.null2Double((s.getPrice() == 0) ? (gc.getGoods()
									.getStore_price().doubleValue()*CommUtil.null2Double(gc.getCount())) : (s.getPrice())*CommUtil.null2Double(gc.getCount()));
				}
				goodsCart.getSc().setTotal_price(BigDecimal.valueOf(total_price));
				goodsCart.getSc().setSc_status(0);
				this.storeCartService.update(goodsCart.getSc());
			}
		}
		ApiUtils.json(response, "", "修改成功", 0);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取购物车的规格列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/getSpecGoodsItems.htm" })
	public void getSpecGoodsItems(HttpServletRequest request,
			HttpServletResponse response,String goodsId){
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		List<Specifi> spec_list=goods.getSpeifi_list();
		SpecifiList goodsSpecList = SpecTools.getGoodsSpec(spec_list);
		goods.setSpecifiList(goodsSpecList);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_name,specifiList"));
		objs.add(new FilterObj(SpecifiList.class, "spec_name_list,spec_prop"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goods, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:arrayIds:数据的格式是[{\"goodsCartIds\":\"227844,227845\",\"storeCartId\":\"25228\"},{\"goodsCartIds\":\"227846\",\"storeCartId\":\"25229\"}]
	 *@description:在app上的购物车里点击结算时,需要的数据,让用户看到相应购买的商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({"/apiOrderShow.htm"})
	public void apiOrderShow(HttpServletRequest request,
			HttpServletResponse response,String arrayIds,
			String userId) {
		if (ApiUtils.is_null(userId)) {
			ApiUtils.json(response, "", "用户id", 1);
			return;
		}
		OrderShowData orderShowData=new OrderShowData();
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		List<StoreCart> storeCartList=orderShowData.getStoreCartList();
		List<JSONObject> array_ids=JSON.parseArray(arrayIds, JSONObject.class);
		Iterator<JSONObject> arrayIdsIterator=array_ids.iterator();
		while(arrayIdsIterator.hasNext()){
			JSONObject ids=arrayIdsIterator.next();
			String storeCartId=ids.getString("storeCartId");
			String[] goodsCartidArray=ids.getString("goodsCartIds").split(",");
			StoreCart storeCart=this.storeCartService.getObjById(Long.valueOf(storeCartId));
			List<GoodsCart> goodsCartList=new ArrayList<GoodsCart>();
			for(String goodsCartId:goodsCartidArray){
				GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(goodsCartId));
				goodsCartList.add(goodsCart);
			}
			storeCart.setGcs(goodsCartList);
			storeCartList.add(storeCart);
			goodsCartList=null;
		}
		Map<String, Long> params = new HashMap<String, Long>();
		params.put("userId", CommUtil.null2Long(userId));
		String hql="select obj from Address obj where obj.user.id=:userId order by obj.defaultStatus desc";
		List<Address> addrs = this.addressService.query(hql,params, 0, 1);
		orderShowData.setStoreCartList(storeCartList);
		orderShowData.setUser(user);
		orderShowData.setAddrs(addrs);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(OrderShowData.class, "user,storeCartList,addrs"));
		objs.add(new FilterObj(StoreCart.class, 
				"id,store,gcs,total_price"));
		objs.add(new FilterObj(GoodsCart.class,
				"id,gcs,count,goods,spec_info_key,price,specification"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_name,goods_price,store_price,goods_main_photo"));
		objs.add(new FilterObj(Store.class, "store_name,id,transport_list"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Specifi.class, "id,specifi"));
		objs.add(new FilterObj(User.class, "id,userName,mobile,photo"));
		objs.add(new FilterObj(Address.class, "id,trueName,area_info,mobile,telephone,area"));
		objs.add(new FilterObj(Area.class, "areaName,parent,id"));
		objs.add(new FilterObj(Transport.class, "id,trans_name,trans_time,trans_type,trans_mail,trans_mail_info,trans_express,trans_express_info,trans_ems"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, orderShowData, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@param <RedirectAttributes>
	 *@return:void
	 *@param:**
	 *@description:立即购买商品,构造一个店铺购物车和商品购物车,然后再走正常
	 *			      的购物流程
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_buyGoods_atNowTime.htm" })
	public void app_buyGoods_atNowTime(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsId,
			String gsp,String tuijian_id,String count){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if (goods.getGoods_status()!=0&&goods.getGoods_status()!=3) {
			ApiUtils.json(response, "", "该商品已下架", 1);
			return;
		}
		StoreCart storeCart=new StoreCart();
		GoodsCart goodsCart=new GoodsCart();
		storeCart=this.generateStoreCart(storeCart,goods,user,"","");
		storeCart.setSc_status(1);
		this.storeCartService.save(storeCart);
		goodsCart=this.generateGoodsCart(tuijian_id, goodsCart, 
				gsp, count, goods, storeCart);
		goodsCart.setDeleteStatus(true);
		this.goodsCartService.save(goodsCart);
		List<JSONObject> IdsArray=new ArrayList<JSONObject>();
		JSONObject params=new JSONObject();
		params.put("goodsCartIds", goodsCart.getId());
		params.put("storeCartId", storeCart.getId());
		IdsArray.add(params);
		String arrayIds=JSON.toJSONString(IdsArray).toString();
		this.apiOrderShow(request, response, arrayIds, userId);
	}
	/***
	 *@author:akangah
	 *@param <RedirectAttributes>
	 *@return:void
	 *@param:**
	 *@description:删除立即购买产生的堆积数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:*****
	 *@variable:*******
	 ***/
	@RequestMapping({ "/delete_goodsCartAndStorCartIDs.htm" })
	public void delete_goodsCartAndStorCartIDs(HttpServletRequest request,
			HttpServletResponse response,String idJsonobject){
		JSONObject jsonobject=JSON.parseObject(idJsonobject);
		String storeCartId=jsonobject.getString("storeCartId");
		String goodsCartIds=jsonobject.getString("goodsCartIds");
		GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(goodsCartIds));
		goodsCart.setSc(null);//清空外键约束
		boolean ret=this.goodsCartService.update(goodsCart);
		if(ret){
			boolean ret1=this.goodsCartService.delete(goodsCart.getId());
			boolean ret2=this.storeCartService.delete(CommUtil.null2Long(storeCartId));
			if(ret1&&ret2){
				ApiUtils.json(response, "", "删除成功", 0);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:payInfos:数据的格式是[{"storeCartId":"8474","goodsCartIds":"966,782",msg:"哈哈","addsId":"9939","transId":"8737","totalPrice":"38477","storeId":"38883"}]
	 *@description:在app上生成订单时需要的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:transport.getTrans_express_info()返回的数据格式是[{"city_id":"-1","city_name":"全国","trans_add_fee":0.0,"trans_fee":0.0,"trans_add_weight":1,"trans_weight":1}]
	 *@variable:*******
	 ***/
	@RequestMapping({ "/generate_order.htm" })
	public void generate_order(HttpServletRequest request,
			HttpServletResponse response,String payInfos,String userId){
		synchronized(this){
			if(ApiUtils.is_null(userId)){
				ApiUtils.json(response, "", "用户id不能为空", 1);
				return;
			}	
			//double lastMonthDaogou=this.statistics_lastMonthDaogouBonus(CommUtil.null2Long(userId));
			List<JSONObject> arrayInfo=JSON.parseArray(payInfos, JSONObject.class);
			Iterator<JSONObject> arrayInfoIterator=arrayInfo.iterator();
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			String order_ids=new String();
			int storeCount=arrayInfo.size();
			int successCount=0;
			while(arrayInfoIterator.hasNext()){//这里以店铺为结算单位,开始结算
				JSONObject obj=arrayInfoIterator.next();
				String[] goodsCartIds=obj.getString("goodsCartIds").split(",");
				double detection_total=this.getTotal(goodsCartIds);//检查总价
				if (detection_total==0) {
					ApiUtils.json(response, "", "请不要频繁点击", 1);
					return;
				}
				Transport  transport=null;
				double tranfee=0.0D;
				String transPortExplain="";//运输说明
				if(CommUtil.null2Long(obj.getLongValue("transId"))!=0){
					transport=this.transportService.getObjById(obj.getLongValue("transId"));
					List<JSONObject> tranList=JSON.parseArray(transport.getTrans_express_info(), JSONObject.class);
					tranfee=CommUtil.formatDouble(tranList.get(0).getDoubleValue("trans_fee"),2);
					transPortExplain=tranList.get(0).getString("city_name");
				}
				//transport.getTrans_express_info()返回的数据格式是[{"city_id":"-1","city_name":"全国","trans_add_fee":0.0,"trans_fee":0.0,"trans_add_weight":1,"trans_weight":1}]
				if(CommUtil.formatDouble(detection_total+tranfee, 2)!=obj.getDoubleValue("totalPrice")){
					if(arrayInfo.size()==1){
						ApiUtils.json(response, "", "订单金额不合适", 1);
						return;
					}else{
						continue;
					}
				}
				successCount++;//如果程序进入到这里,说明的订单的总额都合适
				OrderForm of=new OrderForm();
				of.setAddTime(new Date());
				of.setOrder_status(10);
				of.setUser(user);
				of.setMsg(obj.getString("msg"));
				Address addr = this.addressService.getObjById(CommUtil
						.null2Long(obj.getString("addsId")));
				of.setAddr(addr);
				Store store=this.storeService.getObjById(CommUtil
						.null2Long(obj.getString("storeId")));
				String order_id=store.getId()
						+ CommUtil.formatTime("MMddHHmmss", new Date());
				of.setOrder_id(order_id);
				order_ids=order_ids+order_id+"orderId";
				of.setStore(store);
				of.setOrder_type("app");
				of.setTransport(transPortExplain);
				this.orderFormService.save(of);
				double goodsCart_total_price=0.0D;
				double settlement_total_price=0.0D;
				double changtuijin_total=0.0D;
				double zhanlue_price_total=0.0D;
				for(String goodsCartId:goodsCartIds){
					GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(goodsCartId));
					goodsCart.setOf(of);
					goodsCart.setDeleteStatus(true);
					StoreCart owerStoreCart=goodsCart.getSc();
					goodsCart.setSc(null);//删除和店铺购物车的主外键约束
					this.goodsCartService.update(goodsCart);
					String hql="select obj from GoodsCart as obj where obj.sc.id="+owerStoreCart.getId().toString()+" and obj.deleteStatus=false";
					List<GoodsCart> goodsCartList=this.goodsCartService.query(hql, null, -1, -1);
					if(goodsCartList.size()==0){
						this.storeCartService.delete(owerStoreCart.getId());
					}else {
						double total_price=0;
						for (GoodsCart gc : goodsCartList) {
							//计算该店铺剩余价钱
							Specifi s = gc.getSpecification() == null ? new Specifi() : gc.getSpecification();
							total_price+=CommUtil.null2Double((s.getPrice()) == 0 ? (gc.getGoods()
											.getStore_price().doubleValue()*CommUtil.null2Double(gc.getCount())) : (s.getPrice())*CommUtil.null2Double(gc.getCount()));
						}
						owerStoreCart.setTotal_price(BigDecimal.valueOf(total_price));
						owerStoreCart.setSc_status(0);
						this.storeCartService.update(owerStoreCart);
					}
					double price=goodsCart.getPrice().doubleValue();//得到店铺价
					double settlement=goodsCart.getSettlement_price();//得到结算价
					double changtuijin=goodsCart.getGoods().getCtj();//得到厂推金
					double zhanlue_price=goodsCart.getGoods().getZhanlue_price().doubleValue();//得到战略金
					int count=goodsCart.getCount();
					goodsCart_total_price=goodsCart_total_price+count*price;
					settlement_total_price=settlement_total_price+count*settlement;
					changtuijin_total=changtuijin_total+changtuijin*count;
					zhanlue_price_total=zhanlue_price_total+zhanlue_price*count;
				}
				FenPei fenPei = this.fenPeiService.getObjById(Long.valueOf(1).longValue());
				double base_gold=goodsCart_total_price-settlement_total_price-changtuijin_total-zhanlue_price_total;
				of.setCtj(CommUtil.formatDouble(changtuijin_total,2));//厂推金
				of.setZhanlue_price(CommUtil.formatDouble(zhanlue_price_total,2));//战略金
				of.setMaijia_get_price(CommUtil.formatDouble(settlement_total_price,2));//结算价
				of.setMaijia_tuijian_get_price(CommUtil.formatDouble(base_gold*fenPei.getMaijia_tuijian_get_price(),2));
				of.setDaogou_get_price(CommUtil.formatDouble(base_gold*fenPei.getDaogou_get_price(),2));
				of.setDaogou_tuijian_get_price(CommUtil.formatDouble(base_gold*fenPei.getDaogou_tuijian_get_price(),2));
				of.setZeng_gu_price(CommUtil.formatDouble(base_gold*fenPei.getZeng_gu_price(),2));
				of.setShui_wu_price(CommUtil.formatDouble(base_gold*fenPei.getShui_wu_price(),2));
				of.setChu_pei_price(CommUtil.formatDouble(base_gold*fenPei.getChu_pei_price(),2));
				of.setZhi_ji_price(CommUtil.formatDouble(base_gold*fenPei.getZhi_ji_price(),2));
				of.setXian_ji_price(CommUtil.formatDouble(base_gold*fenPei.getXian_ji_price(),2));
				of.setYang_lao_price(CommUtil.formatDouble(base_gold*fenPei.getYang_lao_price(),2));
				of.setFen_hong_price(CommUtil.formatDouble(base_gold*fenPei.getFen_hong_price(),2));
				of.setShip_price(new BigDecimal(CommUtil.formatDouble(tranfee,2)));
				of.setGoods_amount(new BigDecimal(CommUtil.formatDouble(goodsCart_total_price,2)));
				of.setTotalPrice(new BigDecimal(goodsCart_total_price+tranfee));
				//of.setTotal_base_gold(base_gold);//保存每笔订单产生的总毛利
				/*if(lastMonthDaogou>500){//开始判断计算导购奖金
					if(lastMonthDaogou>500&&lastMonthDaogou<1000){
						of.setDao_gouBonusPrice(CommUtil.formatDouble(base_gold*0.05,2));
						of.setSurplus_price(CommUtil.formatDouble(base_gold*0.1,2));
					}else if(lastMonthDaogou>=1000&&lastMonthDaogou<3000){
						of.setDao_gouBonusPrice(CommUtil.formatDouble(base_gold*0.1,2));
						of.setSurplus_price(CommUtil.formatDouble(base_gold*0.05,2));
					}else if(lastMonthDaogou>=3000){
						of.setDao_gouBonusPrice(CommUtil.formatDouble(base_gold*0.15,2));
						of.setSurplus_price(0);
					}
				}else{
					of.setDao_gouBonusPrice(0);
					of.setSurplus_price(CommUtil.formatDouble(base_gold*0.15,2));
				}*/
				boolean ret=false;
				ret=this.orderFormService.update(of);
				if(ret){
					OrderFormLog ofl = new OrderFormLog();
					ofl.setAddTime(new Date());
					ofl.setOf(of);
					ofl.setLog_info("生成订单");
					ofl.setLog_user(user);
					this.orderFormLogService.save(ofl);
				}
			}
			String disposeStr=order_ids.substring(0, order_ids.lastIndexOf("orderId"));
			if(storeCount==successCount){
				ApiUtils.json(response, disposeStr, "订单生成成功", 0);
				return;
			}
			if(storeCount>successCount){
				ApiUtils.json(response, disposeStr, "订单生成部分成功", 0);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:orderIds:订单的ids "773764,84884"这样中间用逗号隔开的order_id
	 *@description:支付宝支付的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:******
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_albaba_pay.htm" })
	public void app_albaba_pay(HttpServletRequest request,
			HttpServletResponse response, String orderIds){
		System.out.println("支付宝支付==>订单的Id:"+orderIds);
		String[] orderIdsArray=orderIds.split("orderId");
		int orderIdsArraySize=orderIdsArray.length;
		int counts=0;
		double payTotal=0.0D;
		for(String order_id:orderIdsArray){
			String hql="select obj from OrderForm as obj where obj.order_id="+order_id;
			List<OrderForm> ofList=this.orderFormService.query(hql, null, -1, -1);
			OrderForm of = ofList.get(0);
			//boolean is_pay = ApiUtils.order_verify(of);
			if (of.getOrder_status() == 10) {
				counts++;
				of.setPayment(this.getPayment("alipay"));
				of.setPay_msg("alipay");
				this.orderFormService.update(of);
				payTotal=payTotal+of.getTotalPrice().doubleValue();
			}
		}
		if(counts==orderIdsArraySize){
			String str = ApiUtils.getAlipayStr(orderIds,
					CommUtil.getURL(request)+"/app_alipay_notify.htm", payTotal+ "");
			ApiUtils.json(response, str, "获取支付信息成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "获取支付信息出错", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:orderIds:订单的ids "773764orderId84884"这样中间用orderId隔开的order_id
	 *@description:微信支付的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:******
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_weixin_pay.htm"})
	public void app_weixin_pay(HttpServletRequest request,HttpServletResponse response,
			String orderIds){
		System.out.println("微信支付==>订单的Id:"+orderIds);
		String[] orderIdsArray=orderIds.split("orderId");
		int orderIdsArraySize=orderIdsArray.length;
		int counts=0;
		double payTotal=0.0D;
		String notify_url=CommUtil.getURL(request)+"/weixin_notify.htm";
		for(String order_id:orderIdsArray){
			String hql="select obj from OrderForm as obj where obj.order_id="+order_id;
			List<OrderForm> ofList=this.orderFormService.query(hql, null, -1, -1);
			OrderForm of = ofList.get(0);
			//boolean is_pay = ApiUtils.order_verify(of);
			if (of.getOrder_status() == 10 ) {
				counts++;
				of.setPayment(this.getPayment("weixin"));
				of.setPay_msg("weixin");
				this.orderFormService.update(of);
				payTotal=payTotal+of.getTotalPrice().doubleValue();
			}
		}
		if(counts==orderIdsArraySize){ 
			Map<String, String> out_put_params = null;
			try {
				out_put_params = ApiUtils.get_weixin_sign_string(orderIds,
						notify_url,payTotal+"");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ApiUtils.json(response, out_put_params, "获取支付信息成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "获取支付信息出错", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:orderIds:订单的ids"773764orderId84884"这样中间用orderId隔开的order_id,
	 *@description:积分支付的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:******
	 *@variable:*******
	 ***/
	@RequestMapping({"/app_balance_payment.htm"})
	public void app_balance_payment(HttpServletRequest request,
			HttpServletResponse response,String orderIds,String userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		System.out.println("积分支付==>订单的Id:"+orderIds);
		String[] orderIdsArray=orderIds.split("orderId");
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		boolean ret=this.getOrderTotal(orderIdsArray, user);
		if(!ret){
			ApiUtils.json(response, "", "余额不足,未完成支付", 1);
			return;
		}
		List<String> orderIdsList=java.util.Arrays.asList(orderIdsArray);
		int orderIdsListSize=orderIdsList.size();
		int successCount=0;
		Iterator<String> orderIdsIterator=orderIdsList.iterator();
		while(orderIdsIterator.hasNext()){
			String order_id=orderIdsIterator.next();
			String hql="select obj from OrderForm as obj where obj.order_id="+order_id;
			OrderForm of=this.orderFormService.query(hql, null, -1, -1).get(0);
			double userAvailableBalance=CommUtil.null2Double(user.getAvailableBalance());
			//boolean is_pay= ApiUtils.order_verify(of);
			if(of.getOrder_status()==10){
				of.setOrder_status(20);
				of.setPayment(this.getPayment("balance"));
				of.setPay_msg("app积分支付");
				of.setPayTime(new Date());
				of.setPayTimes(new Date());
				boolean is_update = this.orderFormService.update(of);
				if(is_update){//如果订单的状态更新成功就生成对应的明细记录和产生订单操作历史
					successCount++;
					ApiUtils.statSaleNum(of.getId(), goodsCartService, goodsService);
					user.setAvailableBalance(BigDecimal.valueOf(
							CommUtil.subtract(userAvailableBalance, of.getTotalPrice().doubleValue())));
					this.userService.update(user);
					PredepositLog log=new PredepositLog();
					log.setOrder_id(Long.valueOf(of.getId()).longValue());
					log.setAddTime(new Date());
					log.setPd_log_user(user);
					log.setPd_op_type("减少");
					log.setPd_log_amount(BigDecimal.valueOf(
							0-CommUtil.null2Double(of.getTotalPrice())));
					log.setPd_log_info("订单"+of.getOrder_id()+"购买减少可用预存款");
					log.setPd_type("预存款");
					log.setCurrent_price(user.getAvailableBalance().doubleValue());
					this.predepositLogService.save(log);
					OrderFormLog ofg=new OrderFormLog();
					ofg.setAddTime(new Date());
					ofg.setLog_info("积分支付");
					ofg.setLog_user(user);
					ofg.setOf(of);
					this.orderFormLogService.save(ofg);
					String goodsMsg="";
					for(GoodsCart goodsCart:of.getGcs()){
						goodsMsg=goodsMsg+"<===>"+goodsCart.getGoods().getGoods_name();
					}
					//<===>D1SC扇子 天气开始热值得拥有它 全国包邮
					String msg=goodsMsg.substring(5, goodsMsg.length());
					this.send_message(of.getStore().getUser(), of.getStore().getUser().getUserName()+"战友,您好,您店铺的"+msg+"等商品,已被"+of.getUser().getUserName()+"战友购买,请及时发货");
					this.send_message(of.getUser(), of.getUser().getUserName()+"战友,您好,您已成功购买"+of.getStore().getStore_name()+"(店铺)的"+msg+"商品,请等待发货");
				}
			}
		}
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appRemindSellerDelivery.htm?orderIds=" + orderIds, "GET");
		if(orderIdsListSize==successCount){
			ApiUtils.json(response, "", "订单支付成功", 0);
			return;
		}
		if(orderIdsListSize>successCount){
			ApiUtils.json(response, "", "因部分订单上对应的商品不规范,导致部分订单未支付完成", 1);
			return;
		}
	}
	private Payment getPayment(String mark) {
		List<Payment> payments = new ArrayList<Payment>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("mark", mark);
		params.put("type", "admin");
		payments = this.paymentService
				.query("select obj from Payment obj where obj.mark=:mark and obj.type=:type",
						params, -1, -1);
		return payments.get(0);
	}
	private boolean getOrderTotal(String[] orderIdArray,User user){
		double totalPrice=0.0D;
		boolean ret=true;
		for(String orderId:orderIdArray){
			String hql="select obj from OrderForm as obj where obj.order_id="+orderId;
			OrderForm of=this.orderFormService.query(hql, null, -1, -1).get(0);
			totalPrice=totalPrice+of.getTotalPrice().doubleValue();
		}
		double availableBalance=user.getAvailableBalance().doubleValue();
		if(user.getFreezeBlance().intValue()==1){
			if(availableBalance-totalPrice<1000){
				ret=false;
			}
		}else{
			if(availableBalance-totalPrice<0){
				ret=false;
			}
		}
		return ret;
	}
	private double getTotal(String[] goodsCartIds){
		double goodsCart_total_price=0.0D;
		for(String goodsCartId:goodsCartIds){
			GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(goodsCartId));
			if (goodsCart.getOf()!=null) {
				return 0;
			}
			double price=goodsCart.getPrice().doubleValue();//得到店铺价
			int count=goodsCart.getCount();
			goodsCart_total_price=goodsCart_total_price+count*price;
		}
		return  goodsCart_total_price;
	}
	private StoreCart generateStoreCart(StoreCart storeCart,Goods goods,User user,String gsp,String count){
		storeCart.setAddTime(new Date());
		storeCart.setStore(goods.getGoods_store());
		storeCart.setUser(user);
		if (!"".equals(CommUtil.null2String(gsp))) {
			Specifi specifi = (Specifi) commonService.getByWhere("Specifi",
					"goods_id=" + goods.getId() + " and specifi='" + gsp
							+ "'");
			//goodsCart.setSpecification(specifi);
			specifi = specifi == null ? new Specifi() : specifi;
			storeCart.setTotal_price(BigDecimal.valueOf(CommUtil
					.null2Double(specifi.getPrice() == 0 ? goods
							.getStore_price() : specifi.getPrice())*CommUtil.null2Double(count)));
		}else {
			storeCart.setTotal_price(goods.getStore_price());
		}
		return storeCart;
	}
	private GoodsCart generateGoodsCart(String tuijian_id,GoodsCart goodsCart,String gsp,String count,
			Goods goods,StoreCart storeCart){
		goodsCart.setAddTime(new Date());
		goodsCart.setSpec_info_key(gsp);
		goodsCart.setCount(CommUtil.null2Int(count));
		Specifi specifi = (Specifi) commonService.getByWhere("Specifi",
				"goods_id=" + goods.getId() + " and specifi='" + gsp+ "'");
		//goodsCart.setSpecification(specifi);
		specifi = specifi == null ? new Specifi() : specifi;
		goodsCart.setCount(CommUtil.null2Int(count));
		goodsCart.setPrice(BigDecimal.valueOf(CommUtil
				.null2Double(specifi.getPrice() == 0 ? goods
						.getStore_price() : specifi.getPrice())));
		goodsCart.setSettlement_price(specifi
				.getSettlement_price() == 0 ? goods
				.getSettlement_price().doubleValue() : specifi
				.getSettlement_price());
		goodsCart.setGoods(goods);
		goodsCart.setSc(storeCart);
		return goodsCart;
	}
	private List<StoreCart> cart_calc(String user_id,String currentPage) {
		List<StoreCart> user_cart = new ArrayList<StoreCart>();
		Map<String, Number> params = new HashMap<String, Number>();
		int current_page=0;
		int pageSize=20;
		String hql="select obj from StoreCart obj where obj.user.id=:user_id and obj.sc_status=:sc_status order by obj.addTime DESC";
		params.clear();
		params.put("user_id", CommUtil.null2Long(user_id));
		params.put("sc_status", Integer.valueOf(0));
		if(!"".equals(currentPage)){
			current_page=CommUtil.null2Int(currentPage);
			pageSize=20;
		}else{
			current_page=-1;
			pageSize=-1;
		}
		user_cart = this.storeCartService
				.query(hql,params,current_page*pageSize, pageSize);
		return user_cart;
	}
	private void send_message(User user,String msg){
		if(user.getIs_huanxin()==0){//如果用户没有注册环信
			CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUserName());
			user.setIs_huanxin(1);
			this.userService.update(user);
		}
		String[] users={user.getId().toString()};
		JSONObject messages=new JSONObject();
		messages.put("type", "txt");
		messages.put("msg", msg);
		String sender="150383";
		CommUtil.send_message_to_user(users, messages, sender);
	}
	private double statistics_lastMonthDaogouBonus(Long userId){
		double ret=0.00D;
		String lastMonthFirstDay=CommUtil.getLastMonthFirstDay();
		String lastMonthFinalDay=CommUtil.getLastMonthFinalDay();
		String sql="SELECT "+
				   "SUM(so.daogou_get_price) AS total "+
				   "FROM shopping_orderform AS so "+
				   "WHERE so.user_id ="+userId+
				      " AND so.addTime > '"+lastMonthFirstDay+"'"+
				      " AND so.addTime < '"+lastMonthFinalDay+"'"+
				      " AND so.order_status IN(20,30,40,50,60)";
		List<?> queryRet=this.commonService.executeNativeNamedQuery(sql);
		ret=CommUtil.null2Double(queryRet.get(0));
		ret=CommUtil.formatDouble(ret, 2);
		return ret;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:提醒卖家发货，订单必须是待发货状态并且卖家5天之内没有登录网站，才可以推送短信，防止恶意用户调用
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appRemindSellerDelivery.htm")
	public void appRemindSellerDelivery(HttpServletRequest request,HttpServletResponse response,String orderIds){
		if (CommUtil.null2String(orderIds).equals("")) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		String[] orderIdsArray=orderIds.split("orderId");
		boolean is = false;
		for(String order_id:orderIdsArray){
			String hql="select obj from OrderForm as obj where obj.order_id="+order_id;
			List<OrderForm> ofList=this.orderFormService.query(hql, null, -1, -1);
			if (ofList.size()<=0) {
				continue;
			}
			OrderForm of = ofList.get(0);
			if (of.getOrder_status()!=20) {
				ApiUtils.json(response, "", "订单状态错误", 1);
				return;
			}
			User user = of.getStore().getUser();
			String loginTime = CommUtil.formatLongDate(new Date());
			String loginDate = CommUtil.formatLongDate(user.getLoginDate());
			long days = ApiUtils.acquisitionTimeSegment(loginDate,loginTime);
			if (days <= 5) {
				loginDate = CommUtil.formatLongDate(user.getLastLoginDate());
				days = ApiUtils.acquisitionTimeSegment(loginDate,loginTime);
				if (days<=5) {
					ApiUtils.json(response, "", "店长5天内有登陆", 1);
					return;
				}
			}
			if (of.getOrder_status() == 20&&ApiUtils.judgmentType(user.getMobile()).equals("mobile")) {
				ApiUtils.pushRemindDeliverySMS(user.getUserName(), user.getMobile(),of.getUser().getUserName());
				is = true;
			}
		}
		if (is) {
			ApiUtils.json(response, "", "提醒成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "订单不存在", 1);
			return;
		}
	}
}
