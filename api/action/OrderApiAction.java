package com.shopping.api.action;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Evaluate;
import com.shopping.foundation.domain.ExpressCompany;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StorePoint;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IEvaluateService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStorePointService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
@Controller
public class OrderApiAction {
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	IUserService userService;
	@Autowired
	ICommonService commonService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	IRoleService roleService;
	@Autowired
	IAlbumService albumService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IPredepositLogService predepositLogService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取订单列表,根据各种订单的状态来查询
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_order_list.htm" })
	public void app_order_list(HttpServletRequest request,
			HttpServletResponse response,String userId,String currentPage,
			String orderStatus){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="select obj from OrderForm as obj where obj.user.id="+userId;
		int current_page=CommUtil.null2Int(currentPage);
		int pageSize=20;
		int order_status=0;
		String conditionHql="order by obj.addTime desc";
		if(!"".equals(CommUtil.null2String(orderStatus))){
			if (orderStatus.equals("order_submit")) {
				order_status=10;
				hql=hql+"and obj.order_status="+order_status;
			}
			if (orderStatus.equals("order_pay")) {
				order_status=20;
				hql=hql+"and obj.order_status="+order_status;
			}
			if (orderStatus.equals("order_shipping")) {
				order_status=30;
				hql=hql+"and obj.order_status="+order_status;
			}
			if (orderStatus.equals("order_receive")) {
				order_status=40;
				hql=hql+"and obj.order_status="+order_status;
			}
			if (orderStatus.equals("order_finish")) {
				order_status=60;
				hql=hql+"and obj.order_status="+order_status;
			}
			if (orderStatus.equals("order_cancel")) {
				order_status=0;
				hql=hql+"and obj.order_status="+order_status;
			}
			if(orderStatus.equals("order_salesReturnAndRefund")){
				hql=hql+"and obj.order_status in (45,70)";
			}
		}
		List<OrderForm> list =this.orderFormService.query(hql+conditionHql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				OrderForm.class,
				"id,is_auction,addr,order_id,store,order_status,gcs,totalPrice,goods_amount,shipCode,ec,ship_price,addTime,isAdditional"));
		objs.add(new FilterObj(GoodsCart.class,
				"id,gcs,count,goods,spec_info_key,price"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_name,goods_price,store_price,goods_main_photo"));
		objs.add(new FilterObj(Store.class, "store_name,id,store_telephone"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(ExpressCompany.class, "company_name"));
		objs.add(new FilterObj(Address.class, "id,area_info,zip,telephone,mobile,trueName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "添加成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:对订单的搜索
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_order_search.htm" })
	public void app_order_search(HttpServletRequest request,
			HttpServletResponse response,String searchType,String userId,
			String currentPage,String beginTime,String endTime,
			String orderId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="";
		int current_page=CommUtil.null2Int(currentPage);
		int pageSize=20;
		if(!"".equals(CommUtil.null2String(searchType))){
			if("orderId".equals(searchType)){
				hql="select obj from OrderForm as obj where obj.user.id="+userId+"and obj.order_id like '%"+orderId+"%'";
			}
			if("addTime".equals(searchType)){
				hql="select obj from OrderForm as obj where obj.user.id="+userId+" and obj.addTime>'"+beginTime+"' and obj.addTime<'"+endTime+"'";
			}
		}
		List<OrderForm> list =this.orderFormService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(
				OrderForm.class,
				"id,is_auction,addr,order_id,store,order_status,gcs,totalPrice,goods_amount,shipCode,ec,ship_price,addTime,isAdditional"));
		objs.add(new FilterObj(GoodsCart.class,
				"id,gcs,count,goods,spec_info_key,price"));
		objs.add(new FilterObj(Goods.class,
				"id,goods_name,goods_price,store_price,goods_main_photo"));
		objs.add(new FilterObj(Store.class, "store_name,id,store_telephone"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(ExpressCompany.class, "company_name"));
		objs.add(new FilterObj(Address.class, "id,area_info,zip,telephone,mobile,trueName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "添加成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:对订单的删除,只能删除订单状态为0，10，40，50，60的订单
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_order_delete.htm" })
	public void app_order_delete(HttpServletRequest request,
			HttpServletResponse response,String orderId){
		Long Id=CommUtil.null2Long(orderId);
		OrderForm order=this.orderFormService.getObjById(Id);
		int orderStatus=order.getOrder_status();
		if(orderStatus==0||orderStatus==10||orderStatus==40||orderStatus==50||orderStatus==60||orderStatus==71){
			boolean ret=this.orderFormService.delete(Id);
			if(ret){
				ApiUtils.json(response, "", "删除成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "订单不能被删除", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:订单的详情
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
 	@RequestMapping({ "/api_order_detail.htm" })
	public void api_order_detail(HttpServletRequest request,
			HttpServletResponse response, String order_id) {
		OrderForm of = this.orderFormService.getObjById(CommUtil
				.null2Long(order_id));
		if (of != null) {
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(
					OrderForm.class,
					"id,return_content,order_id,store,order_status,gcs,totalPrice,goods_amount,shipCode,ec,ship_price,addTime,msg,addr,isAdditional"));
			objs.add(new FilterObj(GoodsCart.class,
					"id,gcs,count,goods,spec_info_key,price"));
			objs.add(new FilterObj(Goods.class,
					"id,goods_name,goods_price,store_price,goods_main_photo"));
			objs.add(new FilterObj(Store.class, "user,store_name,id,store_telephone"));
			objs.add(new FilterObj(User.class, "id,userName,mobile"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			objs.add(new FilterObj(Address.class,
					"trueName,area_info,telephone,mobile,id"));
			objs.add(new FilterObj(ExpressCompany.class, "company_name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, of, "查询成功", 0, filter);
		} else {
			ApiUtils.json(response, "", "没有该订单", 1);
		}
	}
 	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端订单申请退款
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
 	@RequestMapping({ "/app_order_refund.htm" })
 	public void app_order_refund(HttpServletRequest request,
			HttpServletResponse response, Long orderId,
			String refundContent){
 		OrderForm of=this.orderFormService.getObjById(orderId);
 		String queryGoodsCart="select obj from GoodsCart as obj where obj.of.id="+orderId.toString();
 		if(of.getOrder_status()==20){
 			of.setOrder_status(70);
 			of.setReturn_content(refundContent);
 			List<GoodsCart> goodsCartList=goodsCartService.query(queryGoodsCart, null, -1, -1);
 			Goods goods=null;
			for(GoodsCart goodsCart:goodsCartList){
				goods=goodsCart.getGoods();
				goods.setGoods_salenum(goods.getGoods_salenum()-goodsCart.getCount());
				goodsService.update(goods);
				goods=null;
			}
 			boolean ret=this.orderFormService.update(of);
 			if(ret){
 				String goodsMsg="";
				for(GoodsCart goodsCart:of.getGcs()){
					goodsMsg=goodsMsg+"<===>"+goodsCart.getGoods().getGoods_name();
				}
				//<===>D1SC扇子 天气开始热值得拥有它 全国包邮
				String msg=goodsMsg.substring(5, goodsMsg.length());
				this.send_message(of.getStore().getUser(), of.getStore().getUser().getUserName()+"战友,您好,您店铺的"+msg+"等商品,已被"+of.getUser().getUserName()+"战友申请退款,请及时处理");
				this.send_message(of.getUser(), of.getUser().getUserName()+"战友,您好,您已成功申请"+of.getStore().getStore_name()+"(店铺)的"+msg+"商品退款,请等待退款处理");
 				ApiUtils.json(response, "", "提交成功", 0);
 			}
 		}
 	}
 	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端订单申请退货
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
 	@RequestMapping({ "/app_order_salesReturn.htm" })
 	public void app_order_salesReturn(HttpServletRequest request,
			HttpServletResponse response, Long orderId,
			String salesReturnContent){
 		OrderForm of=this.orderFormService.getObjById(orderId);
 		String queryGoodsCart="select obj from GoodsCart as obj where obj.of.id="+orderId.toString();
 		if(of.getOrder_status()==30){
 			of.setOrder_status(45);
 			of.setReturn_content(salesReturnContent);
 			boolean ret=this.orderFormService.update(of);
 			List<GoodsCart> goodsCartList=goodsCartService.query(queryGoodsCart, null, -1, -1);
 			Goods goods=null;
			for(GoodsCart goodsCart:goodsCartList){
				goods=goodsCart.getGoods();
				goods.setGoods_salenum(goods.getGoods_salenum()-goodsCart.getCount());
				goodsService.update(goods);
				goods=null;
			}
 			if(ret){
 				String goodsMsg="";
				for(GoodsCart goodsCart:of.getGcs()){
					goodsMsg=goodsMsg+"<===>"+goodsCart.getGoods().getGoods_name();
				}
				//<===>D1SC扇子 天气开始热值得拥有它 全国包邮
				String msg=goodsMsg.substring(5, goodsMsg.length());
				this.send_message(of.getStore().getUser(), of.getStore().getUser().getUserName()+"战友,您好,您店铺的"+msg+"等商品,已被"+of.getUser().getUserName()+"战友申请退货,请及时处理");
				this.send_message(of.getUser(), of.getUser().getUserName()+"战友,您好,您已成功申请"+of.getStore().getStore_name()+"(店铺)的"+msg+"商品退货,请等待退货处理");
 				ApiUtils.json(response, "", "提交成功", 0);
 			}
 		}
 	}
 	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:app端确认收货
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
 	@RequestMapping({ "/api_order_cofirm_save.htm" })
	public void api_order_cofirm_save(HttpServletRequest request,
			HttpServletResponse response, String id, String user_id){
		if(ApiUtils.is_null(id,user_id)){
			ApiUtils.json(response, "", "所需参数缺失，请重试", 1);
			return;
		}
		OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if(obj==null||user==null){
			ApiUtils.json(response, "", "对应的用户或订单不存在", 1);
			return;
		}
		if(CommUtil.null2String(obj.getUser().getId()).equals(user_id)&&obj.getOrder_status()==30){
			boolean ret=AllocateWagesUtils.createDistributionParams(obj, user, predepositLogService, commonService, orderFormService, userService, orderFormLogService);
			if(ret){
				OrderFormLog ofl = new OrderFormLog();
				ofl.setAddTime(new Date());
				ofl.setLog_info("确认收货");
				ofl.setLog_user(user);
				ofl.setOf(obj);
				this.orderFormLogService.save(ofl);
				ApiUtils.asynchronousUrl("http://www.d1sc.com/autoConferZhiXian.htm?userId="+user.getId(), "GET");
				ApiUtils.json(response, "", "确认成功", 0);
				return;
			}else{
				ApiUtils.json(response, "", "确认收货成功", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "用户或订单不存在", 1);
		}
	}
	@RequestMapping({ "/api_order_evaluate_save.htm" })
	public void api_order_evaluate_save(HttpServletRequest request,
			HttpServletResponse response, String id, String user_id,
			String goods_id, String description_evaluate_val,
			String service_evaluate_val, String ship_evaluate_val,
			String evaluate_info) throws Exception {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if (CommUtil.null2String(obj.getUser().getId()).equals(user_id)) {
			if (obj.getOrder_status() == 40) {
				obj.setOrder_status(50);
				this.orderFormService.update(obj);
				OrderFormLog ofl = new OrderFormLog();
				ofl.setAddTime(new Date());
				ofl.setLog_info("评价订单");
				ofl.setLog_user(SecurityUserHolder.getCurrentUser());
				ofl.setOf(obj);
				this.orderFormLogService.save(ofl);
				for (GoodsCart gc : obj.getGcs()) {
					if (CommUtil.null2String(gc.getGoods().getId()).equals(
							goods_id)) {
						Evaluate eva = new Evaluate();
						eva.setAddTime(new Date());
						eva.setEvaluate_goods(gc.getGoods());
						eva.setEvaluate_info(evaluate_info);
						eva.setEvaluate_buyer_val(CommUtil.null2Int(1));
						eva.setDescription_evaluate(BigDecimal.valueOf(CommUtil
								.null2Double(description_evaluate_val)));
						eva.setService_evaluate(BigDecimal.valueOf(CommUtil
								.null2Double(service_evaluate_val)));
						eva.setShip_evaluate(BigDecimal.valueOf(CommUtil
								.null2Double(ship_evaluate_val)));
						eva.setEvaluate_type("goods");
						eva.setEvaluate_user(user);
						eva.setOf(obj);
						eva.setGoods_spec(gc.getSpec_info());
						this.evaluateService.save(eva);
						Map params = new HashMap();
						params.put("store_id", obj.getStore().getId());
						List<Evaluate> evas = this.evaluateService
								.query("select obj from Evaluate obj where obj.of.store.id=:store_id",
										params, -1, -1);
						double store_evaluate1 = 0.0D;
						double store_evaluate1_total = 0.0D;
						double description_evaluate = 0.0D;
						double description_evaluate_total = 0.0D;
						double service_evaluate = 0.0D;
						double service_evaluate_total = 0.0D;
						double ship_evaluate = 0.0D;
						double ship_evaluate_total = 0.0D;
						DecimalFormat df = new DecimalFormat("0.0");
						for (Evaluate eva1 : evas) {
							store_evaluate1_total = store_evaluate1_total
									+ eva1.getEvaluate_buyer_val();

							description_evaluate_total = description_evaluate_total
									+ CommUtil.null2Double(eva1
											.getDescription_evaluate());

							service_evaluate_total = service_evaluate_total
									+ CommUtil.null2Double(eva1
											.getService_evaluate());

							ship_evaluate_total = ship_evaluate_total
									+ CommUtil.null2Double(eva1
											.getShip_evaluate());
						}
						store_evaluate1 = CommUtil.null2Double(df
								.format(store_evaluate1_total / evas.size()));
						description_evaluate = CommUtil.null2Double(df
								.format(description_evaluate_total
										/ evas.size()));
						service_evaluate = CommUtil.null2Double(df
								.format(service_evaluate_total / evas.size()));
						ship_evaluate = CommUtil.null2Double(df
								.format(ship_evaluate_total / evas.size()));
						Store store = obj.getStore();
						store.setStore_credit(store.getStore_credit()
								+ eva.getEvaluate_buyer_val());
						this.storeService.update(store);
						params.clear();
						params.put("store_id", store.getId());
						List sps = this.storePointService
								.query("select obj from StorePoint obj where obj.store.id=:store_id",
										params, -1, -1);
						StorePoint point = null;
						if (sps.size() > 0)
							point = (StorePoint) sps.get(0);
						else {
							point = new StorePoint();
						}
						point.setAddTime(new Date());
						point.setStore(store);
						point.setDescription_evaluate(BigDecimal
								.valueOf(description_evaluate));
						point.setService_evaluate(BigDecimal
								.valueOf(service_evaluate));
						point.setShip_evaluate(BigDecimal
								.valueOf(ship_evaluate));
						point.setStore_evaluate1(BigDecimal
								.valueOf(store_evaluate1));
						if (sps.size() > 0)
							this.storePointService.update(point);
						else {
							this.storePointService.save(point);
						}

						user.setIntegral(user.getIntegral()
								+ this.configService.getSysConfig()
										.getIndentComment());
						this.userService.update(user);
						ApiUtils.json(response, "", "评价成功", 0);
						return;
					}
				}
			}
		}
		ApiUtils.json(response, "", "评价失败", 1);
		return;
	}
	@RequestMapping({ "/api_return_money.htm" })
	public void api_return_money(HttpServletRequest request,
			HttpServletResponse response, String id, String return_content,
			String user_id) throws Exception {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (CommUtil.null2String(obj.getUser().getId()).equals(user_id)) {
			obj.setOrder_status(70);
			obj.setReturn_content(return_content);
			this.orderFormService.update(obj);
			ApiUtils.json(response, "", "退款成功", 0);
		} else {
			ApiUtils.json(response, "", "退款失败", 1);
		}
	}

	@RequestMapping({ "/api_return_goods.htm" })
	public void api_return_goods(HttpServletRequest request,
			HttpServletResponse response, String id, String user_id,
			String return_content) throws Exception {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (CommUtil.null2String(obj.getUser().getId()).equals(user_id)) {
			obj.setOrder_status(45);
			obj.setReturn_content(return_content);
			this.orderFormService.update(obj);
			ApiUtils.json(response, "", "退货成功", 0);
		} else {
			ApiUtils.json(response, "", "退货失败", 1);
		}
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
}
