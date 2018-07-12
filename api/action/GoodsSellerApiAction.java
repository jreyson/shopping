package com.shopping.api.action;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.annotations.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.AuctionDetailsApi;
import com.shopping.api.domain.AuctionRecordApi;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.output.AppStorePageData;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.WebForm;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Evaluate;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Payment;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreClass;
import com.shopping.foundation.domain.StoreGrade;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.UserGoodsClass;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.domain.query.GoodsQueryObject;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IEvaluateService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsClassService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPaymentService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserGoodsClassService;
import com.shopping.foundation.service.IUserService;
import com.shopping.lucene.LuceneUtil;
import com.shopping.lucene.LuceneVo;
import com.shopping.manage.admin.tools.StoreTools;
@Controller
public class GoodsSellerApiAction {
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IUserGoodsClassService userGoodsClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStoreService storeService;
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:currentPage:当前页,orderBy:依据那个字段排序,orderType:排序的类型(升序还是降序),
	 *		 goods_name:商品的名字(可以作为模糊查询),user_id:用户的id,goods_status:商品的状态(3为拍卖品0,出售中的商品,1仓库中的商品),
	 *		 user_class_id:拍卖品状态(0为正在拍卖的商品)
	 *@description:获取卖家所有的出售中的商品
	 *@function:接口是通过上传的参数获取所有的卖家所有的商品(包括出售中的和仓库中的),0是上架1是下架3拍卖
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "出售中的商品列表", value = "/get_all_onSale.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/get_all_onSale.htm"})
	public void get_all_onSale(HttpServletRequest request,
			HttpServletResponse response,String currentPage,
			String user_id,String goods_status){
		Long userId = CommUtil.null2Long(user_id);
		if (userId.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "", "参数错误，店铺不存在", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="";
		if ("".equals(CommUtil.null2String(goods_status))) {
			hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_store.id = " + store.getId() + " order by obj.addTime DESC";
		}else {
			hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_store.id = " + store.getId() + " and obj.goods_status = " + CommUtil.null2Int(goods_status) + " order by obj.addTime DESC";
		}
		List<Goods> goods = goodsService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> obj=new ArrayList<FilterObj>();
		obj.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_main_photo,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory,isHideRebate"));
		obj.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(obj);
		ApiUtils.json(response, goods, "查询成功", 0, filter);
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,mulit_goods_ids:要修改的商品的id,用字符串拼接,中间逗号隔开的形式
	 *@description:修改商品状态的方法
	 *@function:该方法主要是根据传上来的id修改对应的商品的状态,并写入tomact的bin目录下
	 *@exception:*******
	 *@method_detail:#1:做搜索引擎用的
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "商品上下架", value = "/goods_take_off.htm*", rtype = "seller", rname = "商品上下架", rcode = "goods_sale_seller", rgroup = "商品管理")
	@RequestMapping({"/goods_take_off.htm"})
	public void goods_take_off(HttpServletRequest request,
			HttpServletResponse response,String mulit_goods_id,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
			String goods_ids[]=mulit_goods_id.split(",");
		for(int i=0;i<goods_ids.length;i++){
			String goods_id=goods_ids[i];
			if(!"".equals(goods_id)){
				Goods goods=this.goodsService.getObjById(Long.valueOf(goods_id));
				User  current_user=this.userService.getObjById(Long.valueOf(user_id));
				Long  check_id=goods.getGoods_store().getUser().getId();
				if(current_user.getId().equals(check_id)){
					int goods_status=goods.getGoods_status()==0?1:0;
					goods.setGoods_status(goods_status);
					this.goodsService.update(goods);
					if(goods_status==0){
						String goods_lucene_path=System.getProperty("user.dir")+ 
												File.separator+
												"luence"+File.separator+"goods";
						File file=new File(goods_lucene_path);		
						if(!file.exists()){
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneVo vo = new LuceneVo();//#1
						vo.setVo_id(goods.getId());
						vo.setVo_title(goods.getGoods_name());
						vo.setVo_content(goods.getGoods_details());
						vo.setVo_type("goods");
						vo.setVo_store_price(CommUtil.null2Double(goods
									.getStore_price()));
						vo.setVo_add_time(goods.getAddTime().getTime());
						vo.setVo_goods_salenum(goods.getGoods_salenum());
						LuceneUtil lucene = LuceneUtil.instance();
						LuceneUtil.setIndex_path(goods_lucene_path);
						lucene.update(CommUtil.null2String(goods.getId()), vo);
					}else{
						String goods_lucene_path = System
								.getProperty("user.dir")
								+ File.separator
								+ "luence" + File.separator + "goods";
						File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.delete_index(CommUtil.null2String(goods.getId()));
					}
					ApiUtils.json(response, "","修改店铺成功", 0);
				}
			}
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,mulit_goods_ids:要删除商品的id,用字符串拼接,中间逗号隔开的形式
	 *@description:删除商品的方法
	 *@function:该方法主要是根据传上来的id删除对应的商品
	 *@exception:*******
	 *@method_detail:*******
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "商品删除", value = "/any_goods_delete.htm*", rtype = "seller", rname = "商品删除", rcode = "goods_del_seller", rgroup = "商品管理")
	@RequestMapping({ "/any_goods_delete.htm" })
	public void any_goods_delete(HttpServletRequest request,HttpServletResponse response,
			String mulit_goods_ids,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String[] goods_ids=mulit_goods_ids.split(",");
		for(int i=0;i<goods_ids.length;i++){
			String goods_id=goods_ids[i];
			if(!"".equals(goods_id)){
				Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goods_id));
				User user=this.userService.getObjById(CommUtil.null2Long(user_id));
				if(goods.getGoods_store().getUser().getId().equals(user.getId())){
					Map<String, Long> map=new HashMap<String, Long>();
					map.put("gid", goods.getId());
					String hql_query="select obj from GoodsCart obj where obj.goods.id = :gid";
					List<GoodsCart> goodsCarts=this.goodsCartService.query(hql_query, map, -1, -1);
					for(GoodsCart gc:goodsCarts){
						Long of_id =gc.getOf().getId();
						//this.goodsCartService.delete(gc.getId());
						OrderForm of=this.orderFormService.getObjById(of_id);
						if(of!=null&&of.getGcs().size()==0){
							of.setDeleteStatus(true);
							this.orderFormService.update(of);
						}
					}
					List<Evaluate> evaluates=goods.getEvaluates();
					Iterator<Evaluate> it=evaluates.iterator();
					while(it.hasNext()){
						Evaluate evaluate=it.next();
						this.evaluateService.delete(evaluate.getId());
					}
					goods.getGoods_ugcs().clear();
					goods.getGoods_ugcs().clear();
					goods.getGoods_photos().clear();
					goods.getGoods_ugcs().clear();
					goods.getGoods_specs().clear();
					this.goodsService.delete(goods.getId());
					String goods_lucene_path = System.getProperty("user.dir")
							+ File.separator + "luence" + File.separator
							+ "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneUtil lucene = LuceneUtil.instance();
					LuceneUtil.setIndex_path(goods_lucene_path);
					lucene.delete_index(CommUtil.null2String(goods_id));
				}
			}
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id
	 *@description:获取商品分类的方法
	 *@function:该方法主要是在商品发布的第一步取出父id为null的所有商品的子分类,并且
	 *			判断该用户能上传多少件商品,依据店铺的类别的goodsCount来校准
	 *@exception:*******
	 *@method_detail:#1:生成指定的过滤器
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "发布商品第一步", value = "/add_goods_first.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/add_seller_goods_first.htm" })
	public void add_seller_goods_first(HttpServletRequest request,HttpServletResponse response,
			String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(user_id));
		List<Payment> payments=new ArrayList<Payment>();
		Map<String, Object> params=new HashMap<String, Object>();
		if(this.configService.getSysConfig().getConfig_payment_type()==1){
			String hql="select obj from Payment obj where obj.type=:type and obj.install=:install";
			params.put("type", "admin");
			params.put("install", Boolean.valueOf(true));
			payments=this.paymentService.query(hql, params, -1, -1);
		}else{
			params.put("store_id", user.getStore().getId());
			params.put("install", Boolean.valueOf(true));
			payments = this.paymentService
					.query("select obj from Payment obj where obj.store.id=:store_id and obj.install=:install",
							params, -1, -1);
		}
		if(payments.size()==0){
			ApiUtils.json(response, "", "请选择一种支付方式", 1);
			return;
		}
		int store_status=user.getStore()==null?0:user.getStore().getStore_status();
		if(store_status==2){
			StoreGrade sg=user.getStore().getGrade();
			int user_goods_count=user.getStore().getGoods_list().size();
			if(user_goods_count==0||(user_goods_count<sg.getGoodsCount())){
				List<GoodsClass> out_put = this.goodsClassService
						.query("select obj from GoodsClass obj where obj.parent.id is null order by obj.sequence asc",
								null, -1, -1);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(GoodsClass.class, "className,sequence,id"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);//#1
				ApiUtils.json(response, out_put, "查询成功",0,filter);
			}else{
				ApiUtils.json(response, "","你只能上传"+sg.getGoodsCount()+"件商品", 1);
			}
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:id:商品分类的id
	 *@description:获取商品分类所有下属子类的方法
	 *@function:该方法主要是通过传上来的id作为父id来查找该类别下所有的子类
	 *@exception:*******
	 *@method_detail:#1:生成指定的过滤器
	 *@variable:*******
	 ***/
	@RequestMapping({"/out_put_child_goodsClass.htm"})
	public void out_put_child_goodsClass(HttpServletRequest request,HttpServletResponse response,
			String id){
		Map<String,Long> mp=new HashMap<String,Long>();
		mp.put("id", Long.valueOf(id));
		List<GoodsClass> goodsClass=this.goodsClassService.query("select obj from GoodsClass obj where obj.parent.id=:id", 
				mp, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(GoodsClass.class, "className,sequence,id"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsClass, "查询成功",0,filter);
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:goods_class_id:商品分类的id,goods_contents:商品的图片(由base64编码之后的字符串),
	 *		 imageType:图片的格式(默认是".jpg"),start_auction_price:拍卖品的起拍价
	 *		 reserve_auction_price:拍卖品的保留价,auction_finish_time:拍卖品的预计结束时间,goods_status:商品的状态(3为拍卖品),
	 *		 auction_status:拍卖品状态(0为正在拍卖的商品),ensure_gold:保证金,
	 *		 is_required_gold:是否需要保证金,is_auction:是否是拍卖品上传，add_range:加价幅度
	 *@description:商品上传的方法
	 *@function:商品上传和拍卖品上传都走这个方法,通过is_auction来区分是那一种上传
	 *@exception:*******
	 *@method_detail:#1:因为是同一个action即做商品上传又做拍卖品上传,所以这里加校验
	 *        		 如果是拍卖品的话,没有商品分类的
	 *        		 #2:如果是拍卖品,就执行if里面的代码,根据客户端传
	 *		    	 上来的is_auction这个属性来判断是否执行这段代码.
	 *		  		 #3:添加主图,默认是上传的第一张图片是主图
	 *@variable:*******              
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "发布商品完成", value = "/add_seller_goods_finish.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({"/add_seller_goods_finish.htm"})
	public void add_seller_goods_finish(HttpServletRequest request,HttpServletResponse response,
			String user_id,String goods_class_id,String goods_contents,
			String imageType,String start_auction_price,
			String reserve_auction_price,String auction_finish_time,
			String ensure_gold,String is_required_gold,String is_auction,
			String add_range,String inventory_type,String goods_spec_str){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(user_id));
		String url1 = this.storeTools.createUserFolderURL(//保存到数据库里面的url
				this.configService.getSysConfig(), user.getStore());
		if(user.getStore()==null){
			ApiUtils.json(response, "","没有开通店铺,不能进行商品上传", 1);
			return;
		}
		WebForm wf=new WebForm();
		Goods goods=wf.toPo(request, Goods.class);//这里给goods对象装配属性,只能装配一些简单的数据类型
		goods.setAddTime(new Date());
		goods.setGoods_store(user.getStore());
		goods.setCtj(1.00D);
		double settlement_price=CommUtil.null2Double(goods.getSettlement_price());
		double store_price=CommUtil.null2Double(goods.getStore_price());
		if(settlement_price>store_price*0.9){
			ApiUtils.json(response, "","结算价必须小于商城价的90%", 1);
			return;
		}
		if ((goods.getCombin_status() != 2)&&(goods.getDelivery_status() != 2)
				&& (goods.getBargain_status() != 2)
				&& (goods.getActivity_status() != 2)) {
			goods.setGoods_current_price(goods.getStore_price());
		}
		goods.setGoods_name(this.clearContent(goods.getGoods_name()));
		if("".equals(CommUtil.null2String(is_auction))){//#1
			GoodsClass gc = this.goodsClassService.getObjById(Long.valueOf(goods_class_id));
			goods.setGc(gc);
		}
		if("yes".equals(CommUtil.null2String(is_auction))){//#2
			AuctionDetailsApi ada=new AuctionDetailsApi();
			ada.setCurrent_auction_price(BigDecimal.valueOf(CommUtil.null2Double(start_auction_price)));
			ada.setStart_auction_price(BigDecimal.valueOf(CommUtil.null2Double(start_auction_price)));
			ada.setReserve_auction_price(BigDecimal.valueOf(CommUtil.null2Double(reserve_auction_price)));
			ada.setEnsure_gold(CommUtil.null2Double(ensure_gold));
			ada.setIs_required_gold(CommUtil.null2Int(is_required_gold));
			ada.setAuction_finish_time(CommUtil.formatDate(auction_finish_time, "yyyy-MM-dd HH:mm"));
			ada.setAddTime(new Date());
			ada.setAdd_range(Double.valueOf(add_range));
			this.commonService.save(ada);
			goods.setAuction_details(ada);
		}
		String[] all_photo=goods_contents.split(",");
		String filePath=this.storeTools.return_createUserFolder(request,
				this.configService.getSysConfig(), user.getStore());
		String url = this.storeTools.createUserFolderURL(//保存到数据库里面的url
				this.configService.getSysConfig(), user.getStore());
		if ("".equals(imageType)) {
			imageType = ".jpg";
		}
		String photoName=user.getId() + System.currentTimeMillis()
				/1000 + "_photo";
		for(int i=0;i<all_photo.length;i++){
			try {
				CommUtil.saveImage(filePath, imageType, all_photo[i],
						photoName+i);
				CommUtil.addSmallGoodsPhoto(filePath, imageType, all_photo[i], 
						photoName+i);
				CommUtil.addMiddleGoodsPhoto(filePath, imageType, all_photo[i], 
						photoName+i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Accessory accessory=new Accessory();
			accessory.setAddTime(new Date());
			accessory.setWidth(400);
			accessory.setHeight(400);
			accessory.setName(photoName+i+imageType);
			accessory.setExt(imageType.substring(imageType.lastIndexOf(".")+1));
			accessory.setPath(url);
			accessory.setUser(user);
			Album album = this.albumService.getDefaultAlbum(CommUtil
					.null2Long(user_id));
			if (album == null) {
				album = new Album();
				album.setAddTime(new Date());
				album.setAlbum_name("默认相册");
				album.setAlbum_sequence(-10000);
				album.setAlbum_default(true);
				album.setUser(user);
				album.setAlblum_info("个人影集");
				this.albumService.save(album);
			}
			accessory.setAlbum(album);
			this.accessoryService.save(accessory);
			if(i==0){//#3
				goods.setGoods_main_photo(accessory);
			}
			goods.getGoods_photos().add(accessory);
		}
		boolean ret=this.goodsService.save(goods);
		if(ret){
			if("spec".equals(CommUtil.null2String(inventory_type))){
				 this.specStrToList(goods_spec_str,goods);
			}
			ApiUtils.json(response, "","发布成功", 0);
			return;
		}else{
			ApiUtils.json(response, "","发布失败", 1);
			return;
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:currentPage:当前页,orderBy:依据那个字段排序,orderType:排序的类型(升序还是降序),
	 *		 goods_name:商品的名字,user_id:用户的id,goods_status:商品的状态(3为拍卖品),
	 *		 auction_status:拍卖品状态(0为正在拍卖的商品)
	 *@description:获取所有的拍卖品和个人所有的拍卖品的方法
	 *@function:接口是获取所有的拍卖品和个人所有的拍卖,可用于分页和模糊查询和排序
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取所有的拍卖品和个人所有的拍卖品", value = "/get_all_auction_goods.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/get_all_auction_goods.htm" })
	public void  get_all_auction_goods(HttpServletRequest request,HttpServletResponse response,
			String currentPage,String orderBy,String orderType,String goods_name,
			String user_id,String goods_status,String auction_status){
		int current_page=0;
		int pageSize=12;
		String hql="";
		Map<String ,Object> params=new HashMap<String, Object>();
		if(currentPage==null||"".equals(currentPage)){
			current_page=0;
		}else{
			current_page=CommUtil.null2Int(currentPage);
		}
		if(orderType==null||"".equals(orderType)){
			orderType="desc";
		}
		if(orderBy==null||"".equals(orderBy)){
			orderBy="addTime";
		}
		if(user_id==null||"".equals(user_id)){
			hql="select obj from Goods obj where obj.deleteStatus=false and obj.goods_status=:goods_status ";
		}else{
			User user=this.userService.getObjById(Long.valueOf(user_id));
			Long store_id=user.getStore().getId();
			hql="select obj from Goods obj where obj.deleteStatus=false and obj.goods_status=:goods_status "
					+"and obj.goods_store.id="+store_id;
		}
		if(auction_status!=null&&!"".equals(auction_status)){
			hql=hql+" and obj.auction_details.auction_status=:auction_status ";
		}
		if(goods_name!=null&&!"".equals(goods_name)){
			hql=hql+" and obj.goods_name like '%"+goods_name+"%'";
		}
		hql=hql+"order by :order_by "+orderType;
		params.put("goods_status", Integer.valueOf(goods_status));
		params.put("order_by", orderBy);
		if(auction_status!=null&&!"".equals(auction_status)){
			params.put("auction_status", Integer.valueOf(auction_status));
		}
		List<Goods> get_auction_goods=this.goodsService.query(hql, params, 
				current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class,"id,goods_name,goods_main_photo,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory,auction_records,auction_details,goods_click"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(AuctionDetailsApi.class,"current_auction_price,add_price_times,auction_status,auction_finish_time"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, get_auction_goods, "查询成功",0,filter);	
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,goods_id:商品的id
	 *@description:获取商品详情的方法
	 *@function:该方法主要是通过商品的id来获取到商品的所有详情信息
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取指定商品的详情", value = "/get_auction_goods_details.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/get_auction_goods_details.htm" })
	public void get_auction_goods_details(HttpServletRequest request,HttpServletResponse response,
			String goods_id){
		Goods goods=this.goodsService.getObjById(Long.valueOf(goods_id));
		if(goods!=null){
			goods.setGoods_click(goods.getGoods_click()+1);
			this.goodsService.update(goods);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Goods.class,"id,goods_main_photo,goods_store,goods_name,goods_photos,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory,auction_records,auction_details,goods_click"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			objs.add(new FilterObj(User.class, "userName,id,mobile,photo"));
			objs.add(new FilterObj(AuctionDetailsApi.class,"add_range,ensure_gold,joined_user,current_auction_price,add_price_times,auction_status,auction_finish_time,start_auction_price"));
			objs.add(new FilterObj(AuctionRecordApi.class, "contend_user,plus_price_time,current_auction_price"));
			objs.add(new FilterObj(Store.class, "id,user"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, goods, "查询成功",0,filter);	
		}else{
			ApiUtils.json(response, "","没有对应的该商品", 1);
		}	
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,goods_id:商品的id
	 *@description:参与报名的方法
	 *@function:该方法主要功能是增加拍卖品的报名用户.主要的逻辑是要
	 *			参与拍卖就要先来报名.
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "参与报名的方法", value = "/go_sign_up.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/go_sign_up.htm" })
	public void go_sign_up(HttpServletRequest request,HttpServletResponse response,
			String user_id,String goods_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		if(goods!=null){
			AuctionDetailsApi auction_dectail=goods.getAuction_details();
			List<User> users = auction_dectail.getJoined_user();
			User goods_master=goods.getGoods_store().getUser();
			User current_user=this.userService.getObjById(CommUtil.null2Long(user_id));
			boolean is_add=false;
			for(int i=0;i<users.size();i++){
				User user=users.get(i);
				if(user.getId().longValue()==current_user.getId().longValue()){
					is_add=true;
					ApiUtils.json(response, "","请不要重复报名", 1);
					return;
				}
			}
			if(!is_add){
				if(goods_master.getId().longValue()!=current_user.getId().longValue()){
					users.add(current_user);
					auction_dectail.setJoined_user(users);
					goods.setAuction_details(auction_dectail);
					boolean ret=this.goodsService.update(goods);
					if(ret){
						List<FilterObj> objs = new ArrayList<FilterObj>();
						objs.add(new FilterObj(Goods.class,"id,goods_main_photo,goods_store,goods_name,goods_photos,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory,auction_records,auction_details,goods_click"));
						objs.add(new FilterObj(Accessory.class, "name,path,ext"));
						objs.add(new FilterObj(User.class, "userName,id,mobile,photo"));
						objs.add(new FilterObj(AuctionDetailsApi.class,"add_range,ensure_gold,joined_user,current_auction_price,add_price_times,auction_status,auction_finish_time,start_auction_price"));
						objs.add(new FilterObj(AuctionRecordApi.class, "contend_user,plus_price_time,current_auction_price"));
						objs.add(new FilterObj(Store.class, "id,user"));
						CustomerFilter filter = ApiUtils.addIncludes(objs);
						ApiUtils.json(response, goods, "查询成功",0,filter);	
					}else{
						ApiUtils.json(response, "","报名失败", 2);
					}
				}else{
					ApiUtils.json(response, "","商品的主人不能参加报名", 3);
				}
			}
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,goods_id:商品的id,current_auction_price:该商品的当前价
	 *@description:拍卖品加价的方法
	 *@function:该方法主要功能是增加拍卖品价格.主要的逻辑是用户
	 *			参加拍卖报名后就可以来加价
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "拍卖品加价的方法", value = "/plus_auction_price.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/plus_auction_price.htm" })
	public void plus_auction_price(HttpServletRequest request,HttpServletResponse response,
			String user_id,String goods_id,String current_auction_price){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		User current_user=this.userService.getObjById(CommUtil.null2Long(user_id));
		if(goods!=null){
			List<User> all_joiner=goods.getAuction_details().getJoined_user();
			boolean is_can_plus=false;
			for(int i=0;i<all_joiner.size();i++){	
				User user=all_joiner.get(i);
				if(user.getId().longValue()==current_user.getId().longValue()){
					is_can_plus=true;
				}
			}	
			if(is_can_plus){		
				AuctionRecordApi auction_record=new AuctionRecordApi();
				Date current_time=new Date();
				auction_record.setAddTime(current_time);
				auction_record.setPlus_price_time(current_time);
				auction_record.setContend_user(current_user);
				auction_record.setCurrent_auction_price(BigDecimal.valueOf(CommUtil.null2Double(current_auction_price)));
				this.commonService.save(auction_record);
				List<AuctionRecordApi> auction_record_list=goods.getAuction_records();
				auction_record_list.add(auction_record);
				AuctionDetailsApi auction_details=goods.getAuction_details();
				auction_details.setAdd_price_times(auction_details.getAdd_price_times()+1);
				auction_details.setCurrent_auction_price(BigDecimal.valueOf(CommUtil.null2Double(current_auction_price)));
				goods.setAuction_records(auction_record_list);
				goods.setAuction_details(auction_details);
				boolean ret=this.goodsService.update(goods);
				if(ret){
					ApiUtils.json(response, "","加价成功", 0);
				}
			}else{
				ApiUtils.json(response, "","请先去报名", 1);
			}
		}else{
			ApiUtils.json(response, "","没有对应的该拍卖品", 2);
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:当前登录用户id,goods_id:商品的id,current_auction_price:该商品的当前价
	 *@description:拍卖品加价的方法
	 *@function:该方法主要功能是增加拍卖品价格.主要的逻辑是用户
	 *			参加拍卖报名后就可以来加价
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "更新订单地址的方法", value = "/update_orderForm_addr.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/update_orderForm_addr.htm" })
	public void update_orderForm_addr(HttpServletRequest request,HttpServletResponse response,
			String order_id,String addr_id){
		OrderForm of=this.orderFormService.getObjById(Long.valueOf(order_id));
		Address addr=this.addressService.getObjById(Long.valueOf(addr_id));
		of.setAddr(addr);
		boolean ret=this.orderFormService.update(of);
		if(ret){
			ApiUtils.json(response, "","修改地址成功", 0);
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:order_id:订单的id(是主索引的id),user_id:用户的id
	 *@description:买家确认收货的方法
	 *@function:该方法主要功能是买家收到货后,点击确认收获按钮,给卖家和平台分钱(以积分的形式存入user的可用余额)
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "拍卖品确认收货的订单", value = "/confirm_get_auction_goods.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/confirm_get_auction_goods.htm" })
	public void confirm_get_auction_goods(HttpServletRequest request,HttpServletResponse response,
			String order_id,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		OrderForm order = this.orderFormService.getObjById(Long.valueOf(order_id));
		if(CommUtil.null2String(order.getUser().getId()).equals(user_id)
			&& order.getOrder_status()==30){
			order.setOrder_status(40);
			boolean ret=this.orderFormService.update(order);
			if(ret){
				User seller = this.userService.getObjById(order.getStore()
						.getUser().getId());
				seller.setAvailableBalance(BigDecimal
						.valueOf(CommUtil.add(seller
								.getAvailableBalance(), Double.valueOf(order
								.getMaijia_get_price()))));
				this.userService.update(seller);
				PredepositLog seller_log = new PredepositLog();
				seller_log.setAddTime(new Date());
				seller_log.setPd_log_user(seller);
				seller_log.setPd_op_type("增加");
				seller_log.setPd_log_amount(BigDecimal.valueOf(order
						.getMaijia_get_price()));
				seller_log.setPd_log_info("订单" + order.getOrder_id()
						+ "拍卖所得");
				seller_log.setPd_type("可用预存款");
				seller_log.setCurrent_price(seller
						.getAvailableBalance().doubleValue());
				seller_log.setOrder_id(Long.valueOf(order.getOrder_id()));
				this.predepositLogService.save(seller_log);
				
				User company_get_user=this.userService.getObjById(Long.valueOf(1));
				seller.setAvailableBalance(BigDecimal
						.valueOf(CommUtil.add(company_get_user
								.getAvailableBalance(), Double.valueOf(order
								.getGet_by_auction_gold()))));
				this.userService.update(company_get_user);
				PredepositLog company_get_user_log = new PredepositLog();
				company_get_user_log.setAddTime(new Date());
				company_get_user_log.setPd_log_user(company_get_user);
				company_get_user_log.setPd_op_type("增加");
				company_get_user_log.setPd_log_amount(BigDecimal.valueOf(order
						.getGet_by_auction_gold()));
				company_get_user_log.setPd_log_info("订单" + order.getOrder_id()
						+ "拍卖所得");
				company_get_user_log.setPd_type("可用预存款");
				company_get_user_log.setCurrent_price(company_get_user
						.getAvailableBalance().doubleValue());
				company_get_user_log.setOrder_id(Long.valueOf(order.getOrder_id()));
				this.predepositLogService.save(company_get_user_log);
				
				OrderFormLog ofl = new OrderFormLog();
				ofl.setAddTime(new Date());
				ofl.setLog_info("确认收货");
				ofl.setLog_user(user);
				ofl.setOf(order);
				this.orderFormLogService.save(ofl);
				ApiUtils.updateUserRenk(0, seller, commonService, userService);//更新会员等级
				ApiUtils.json(response, "", "确认成功", 0);
			}else{
				ApiUtils.json(response, "", "失败", 1);
			}
		}else{
			ApiUtils.json(response, "", "用户或订单不存在", 1);
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:userName:环信的id
	 *@description:删除环信用户的接口
	 *@function:主要是通过得到传过来的userName调用CommUtil.delete_huanxing_user(userName)方法去环信那边删除该用户
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "删除环信的用户", value = "/delete_huan_xin_user.htm*", rtype = "seller", rname = "商品发布", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping({ "/delete_huan_xin_user.htm" })
	public void delete_huan_xin_user(HttpServletRequest request,HttpServletResponse response,
			String userName){
		JSONObject	json_obj=CommUtil.delete_huanxing_user(userName);
		System.out.println(json_obj.toJSONString());
		JSONArray jsonArray = json_obj
				.getJSONArray("entities");
		if(jsonArray.size()>0){
			ApiUtils.json(response, "", "删除用户成功", 0);
		}
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
	private List<Specifi> specStrToList(String str, Goods goods) {
		List<Specifi> spec_list = new ArrayList<Specifi>();
		if (!CommUtil.isNotNull(str)) {
			return spec_list;
		}
		String[] spec_price_arr = str.split("<_>");
		for (int i = 0; i < spec_price_arr.length; i++) {
			String[] arr1 = spec_price_arr[i].split("<->");
			Specifi spec = new Specifi();
			spec.setSpecifi(arr1[0]);
			spec.setInventory(Integer.parseInt(arr1[1]));
			if (CommUtil.null2Double(arr1[2])<=0) {
				spec.setPrice(CommUtil.null2Double(goods.getGoods_price()));
			}else {
				spec.setPrice(Double.parseDouble(arr1[2]));
			}
			if (CommUtil.null2Double(arr1[2])<=0) {
				spec.setSettlement_price(CommUtil.null2Double(goods.getSettlement_price()));
			}else {
				spec.setSettlement_price(Double.parseDouble(arr1[3]));
			}			
			spec.setNumber(arr1[4]);
			spec.setGoods(goods);
			this.commonService.save(spec);
			spec_list.add(spec);
		}
		return spec_list;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:storeId  店铺ID
	 *@description:商店首页数据 #1 店铺名称 #2店铺客服 #3客服职位 #4客服部门 #5店铺保证金 #6店铺总销量 #7店铺近三月销售额 #8店铺商品数量
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getStoreHomePage.htm", method = RequestMethod.POST)
	public void getStoreHomePage(HttpServletRequest request,
			HttpServletResponse response,String storeId,String currentPage){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Long store_id = CommUtil.null2Long(storeId);
		if (store_id.longValue()==-1) {
			ApiUtils.json(response, "", "店铺参数错误", 1);
			return;
		}
		Store store = storeService.getObjById(store_id);//#1
		if (store==null) {
			ApiUtils.json(response, "", "店铺不存在", 1);
			return;
		}
		if (store.getStore_status()!=2&&(store.getStore_status()==3&&"".equals(CommUtil.null2String(store.getWarnType())))) {
			ApiUtils.json(response, "", "店铺状态异常", 1);
			return;
		}
		AppStorePageData as=new AppStorePageData();
		if (current_page==0) {
			User user = store.getUser();//#2
			if (user!=null) {
				ZhiWei zhiwei = user.getZhiwei();//#3
				BuMen bumen = user.getBumen();//#4
				Accessory photo = user.getPhoto();
				int bail=0;//#5
				ZhiXianEntity zhixian = user.getZhixian();
				Integer freezeBlance = user.getFreezeBlance().intValue();
				int availableBalance = user.getAvailableBalance().intValue();
				if (freezeBlance!=0) {
					if (availableBalance>1000) {
						bail=1000;
					}else {
						bail=availableBalance;
					}
				}
				as.setBail(bail);
				as.setBumen(bumen);
				as.setUser(user);
				as.setZhiWei(zhiwei);
				as.setZhiXian(zhixian);
			}
			String sale_hql="select sum(obj.goods_salenum) from Goods as obj where obj.deleteStatus=false and obj.goods_store.id =" + store_id;
			List<?> sale = commonService.query(sale_hql, null, -1, -1);//#6
			String money_hql="select sum(obj.totalPrice) from OrderForm as obj where obj.deleteStatus=false and obj.addTime>=:addTime and obj.store.id =:store_id  AND obj.order_status IN (20,30,40,50,60)";
			Map<String, Object> param=new HashMap<String, Object>();
			String date = ApiUtils.getFirstday_Lastday(new Date(), 0, 90);
			Date addTime=CommUtil.formatDate(date, "yyyy-MM-dd");
			param.put("addTime", addTime);
			param.put("store_id", store_id);
			List<?> money = commonService.query(money_hql, param, -1, -1);//#7
			String goods_hql="select count(obj) from Goods as obj where obj.deleteStatus=false and obj.goods_status = 0 and obj.goods_inventory>0 and obj.goods_store.id =" + store_id ;
			List<?> goods = commonService.query(goods_hql, null, -1, -1);//#8
			if (sale.size()>0) {
				if (sale.get(0)!=null) {
					as.setSale(((Long)sale.get(0)).intValue());
				}			
			}
			if (money.size()>0) {
				if (money.get(0)!=null) {
					as.setMoney(((BigDecimal)money.get(0)).intValue());
				}
			}
			if (goods.size()>0) {
				as.setGoodNum(((Long)goods.get(0)).intValue());
			}
		}
		String sc_hql="select count(obj),obj.gc.id from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.id="+storeId +" GROUP BY obj.gc.id order by obj.gc.id DESC";
		@SuppressWarnings("unchecked")
		List<Object[]> sc=this.commonService.query(sc_hql, null, current_page*pageSize, pageSize);
		List<Goods> goodsList=new ArrayList<Goods>();
		String hql="";
		if (sc.size()>3) {
			for (Object[] obj:sc) {
				if (obj[1]!=null){
					hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.gc.id="+obj[1]+" and obj.goods_store.id="+storeId +" order by obj.addTime desc";
					List<Goods> query = this.goodsService.query(hql, null, 0, 1);
					goodsList.addAll(query);
				}else {
					hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.gc.id IS NULL and obj.goods_store.id="+storeId +" order by obj.addTime desc";
					List<Goods> query = this.goodsService.query(hql, null, 0, 1);
					goodsList.addAll(query);
				}
			}
		}else {
			hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.id="+storeId +" order by obj.addTime desc";			
			goodsList=this.goodsService.query(hql, null, current_page*pageSize, pageSize);
		}	
		as.setStore(store);
		as.setGoods(goodsList);
		if (store.getStore_status()==3) {
			as.setBail(0);
			as.setGoodNum(0);
			as.setGoods(new ArrayList<Goods>());
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppStorePageData.class,"store,user,zhiWei,bumen,bail,sale,money,goodNum,goods,accessory,zhiXian"));
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,goods_salenum,gc"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone,warnType,warnContent,store_status"));
		objs.add(new FilterObj(User.class, "id,userName,photo,lastLoginDate,loginDate"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(BuMen.class, "id,name"));		
		objs.add(new FilterObj(GoodsClass.class, "id,className"));		
		objs.add(new FilterObj(ZhiXianEntity.class, "name"));		
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, as, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:storeId  店铺ID
	 *@description:商店分类下的商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getStoreClassGoods.htm", method = RequestMethod.POST)
	public void getStoreClassGoods(HttpServletRequest request,
			HttpServletResponse response,String storeId,String currentPage,String goodsClassId,String goodsPrice,String salesNum,
			String goodsClicks,String addTime,String orderType){
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		Long store_id = CommUtil.null2Long(storeId);
		if (store_id.longValue()==-1) {
			ApiUtils.json(response, "", "店铺参数错误", 1);
			return;
		}
		Store store = storeService.getObjById(store_id);
		if (store==null) {
			ApiUtils.json(response, "", "店铺不存在", 1);
			return;
		}
		if (store.getStore_status()!=2) {
			ApiUtils.json(response, "", "店铺状态异常", 1);
			return;
		}
		Long goodsClass_id = CommUtil.null2Long(goodsClassId);
		String hql="";
		if (goodsClass_id.longValue()==-1) {
			hql="select obj from Goods as obj  where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.id="+storeId+" and obj.gc.id is null ";
		}else {
			hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and obj.goods_inventory>0 and obj.goods_store.id="+storeId+" and obj.gc.id = " + goodsClass_id +" ";
		}
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
		List<Goods> goodsList=this.goodsService.query(hql+chooseCondition, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Goods.class, "id,goods_main_photo,goods_name,goods_price,store_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:店铺内搜索，根据商品的名字进行商品的查询
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/searchGoodsByNameAndStoreId.htm", method = RequestMethod.POST)
	public void searchGoodsByName(HttpServletRequest request,
			HttpServletResponse response,String goodsName,
			String currentPage,String goodsPrice,String salesNum,
			String goodsClicks,String addTime,String orderType,String storeId){
		Long store_id = CommUtil.null2Long(storeId);
		if (store_id.longValue()==-1) {
			ApiUtils.json(response, "", "店铺参数错误", 1);
			return;
		}
		Store store = storeService.getObjById(store_id);
		if (store==null) {
			ApiUtils.json(response, "", "店铺不存在", 1);
			return;
		}
		if (store.getStore_status()!=2) {
			ApiUtils.json(response, "", "店铺状态异常", 1);
			return;
		}
		String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_store.id= "+store_id+" and obj.goods_status=0 and obj.goods_inventory>0  and obj.goods_store.store_status in (2,3)";
		String chooseCondition="";
		if(!"".equals(CommUtil.null2String(goodsName))){
			chooseCondition=" and obj.goods_name like '%"+goodsName+"%' ";
		}
		if("".equals(CommUtil.null2String(orderType))){
			orderType="";
		}
		if(!"".equals(CommUtil.null2String(goodsPrice))){
			chooseCondition=chooseCondition+"order by obj.store_price "+orderType;
		}
		if(!"".equals(CommUtil.null2String(goodsClicks))){
			chooseCondition=chooseCondition+"order by obj.goods_click "+orderType;
		}
		if(!"".equals(CommUtil.null2String(salesNum))){
			chooseCondition=chooseCondition+"order by obj.goods_salenum "+orderType;
		}
		if(!"".equals(CommUtil.null2String(addTime))){
			chooseCondition=chooseCondition+"order by obj.addTime "+orderType;
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
		objs.add(new FilterObj(Goods.class, "id,goods_details,goods_main_photo,goods_name,goods_price,store_price,settlement_price,goods_salenum"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, goodsList, "店铺商品查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:商品的状态0是上架1是下架
	 *@description:app上下架商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSetGoodsStatusByGoodsId.htm", method = RequestMethod.POST)
	public void appSetGoodsStatusByGoodsId(HttpServletRequest request,
			HttpServletResponse response, String goodsId,String userId,String password,String goodsStatus) {			
		if("".equals(CommUtil.null2String(password))){
			ApiUtils.json(response, "", "密码不能为空", 1);
			return;
		}
		if("".equals(CommUtil.null2String(goodsStatus))){
			ApiUtils.json(response, "", "状态不能为空", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		Long goods_id = CommUtil.null2Long(goodsId);
		if (user_id.longValue()==-1||goods_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user = userService.getObjById(user_id);
		if (user!=null) {
			if (!user.getPassword().equals(password)) {
				ApiUtils.json(response, "", "密码错误", 1);
				return;
			}else {
				Goods goods = this.goodsService.getObjById(goods_id);
				if (goods==null) {
					ApiUtils.json(response, "", "商品参数错误", 1);
					return;
				}
				Store goods_store = goods.getGoods_store();
				if (user.getStore()!=null) {
					if (user.getStore().getStore_status()==2&&goods_store.getId()==user.getStore().getId()) {
						Integer goods_status = goods.getGoods_status();
						if (!goods_status.toString().equals(goodsStatus)) {
							if (goodsStatus.equals("0")) {
								goods.setGoods_status(0);
								boolean update = goodsService.update(goods);
								if (update) {
									ApiUtils.json(response, "", "上架成功", 0);
									return;
								}
							}
							if (goodsStatus.equals("1")) {
								goods.setGoods_status(1);
								boolean update = goodsService.update(goods);
								if (update) {
									ApiUtils.json(response, "", "下架成功", 0);
									return;
								}
							}
							ApiUtils.json(response, "", "状态值错误", 1);
							return;
						}else {
							ApiUtils.json(response, "", "商品状态与要操作的状态一样", 1);
							return;
						}				
					}else {
						ApiUtils.json(response, "", "参数错误，店铺id不匹配或店铺未开通", 1);
						return;
					}
				}else {
					ApiUtils.json(response, "", "店铺不存在", 1);
					return;
				}
			}		
		}else {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}		
	}
}
