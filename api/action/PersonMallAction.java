package com.shopping.api.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.PersonMallApi;
import com.shopping.api.domain.PersonMallPhotoUrl;
import com.shopping.api.domain.regionPartner.InvitePartnerRewardRatio;
import com.shopping.api.domain.userFavorite.FavoriteStore;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.service.IPersonMallApiService;
import com.shopping.api.service.Favorite.IFavoriteStoreService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Favorite;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;

@Controller
public class PersonMallAction {
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IPersonMallApiService personMallApiService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IFavoriteStoreService<FavoriteStore> favoriteStoreService;
	@Autowired
	private IStoreService storeService;
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:goods_id:商品的id(主索引)
	 *@description:将商品加入到个人商城首页的方法
	 *@function:接口是通过解析上传的mulit_goods_id(逗号分隔的字符串),将该goods_id对应的商品批量存入到数据库
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "加入到个人的商城", value = "/join_to_persion_mall.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/join_to_persion_mall.htm"})
	public void join_to_persion_mall(HttpServletRequest request,HttpServletResponse response,
			String mulit_goods_id,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String[] goods_ids=mulit_goods_id.split(",");
		boolean ret=false;
		User user=this.userService.getObjById(Long.valueOf(user_id));
		List<PersonMallApi> person_mall_goods=user.getPerson_malls();
		String hql="select obj.person_goods from PersonMallApi as obj where obj.user.id="+user_id+" and obj.delete_status=0";
		List<?> person_all_goods=this.commonService.query(hql, null, -1, -1);
		Iterator<?> ite=person_all_goods.iterator();
		String person_goods_ids="";
		while(ite.hasNext()){
			Goods temp=(Goods)ite.next();
			person_goods_ids=temp.getId().toString()+","+person_goods_ids;
		}
		for(int i=0;i<goods_ids.length;i++){
			ret=false;
			String goods_id=goods_ids[i];
			Goods goods=this.goodsService.getObjById(Long.valueOf(goods_id));
			int is_exist=person_goods_ids.indexOf(goods_id);
			if(goods!=null&&is_exist<0){
				PersonMallApi person_mall=new PersonMallApi();
				person_mall.setAddTime(new Date());
				person_mall.setDelete_status(0);
				person_mall.setPerson_goods(goods);
				person_mall.setUser(user);
				this.commonService.save(person_mall);
				person_mall_goods.add(person_mall);
				ret=true;
			}
		}
		user.setPerson_malls(person_mall_goods);
		boolean is_ok=this.userService.update(user);
		if(ret&&is_ok){
			ApiUtils.json(response, "","加入到个人商城成功", 0);
		}else{
			ApiUtils.json(response, "","加入了部分商品", 1);
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取个人商城的所有商品
	 *@function:通过user_id获取到个人商城所有的商品,只出输出商品的一些信息
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取个人商城首页的所有商品", value = "/get_all_person_mall_goods.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/get_all_person_mall_goods.htm"})
	public void get_all_person_mall_goods(HttpServletRequest request,HttpServletResponse response,
			String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="select obj from PersonMallApi as obj where obj.user.id="+user_id+" and obj.delete_status=0";
		List<?> out_persion_mall_goods=this.commonService.query(hql, null, -1,-1);
		if(out_persion_mall_goods.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(PersonMallApi.class, "id,addTime,delete_status,person_goods"));
			objs.add(new FilterObj(Goods.class,"id,store_price,goods_price,goods_main_photo,goods_name,goods_photos,goods_salenum,goods_collect,goods_details,goods_status,goods_inventory,goods_click"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, out_persion_mall_goods, "查询成功",0,filter);
		}else{
			ApiUtils.json(response, "","个人商城还没有对应的商品", 1);
		}
	}
	/***
	 *@author:aknagah
	 *@return:void
	 *@param:muilt_person_mall_ids:个人商城记录表的id字符串,中间逗号隔开
	 *@description:修改商品的状态达到删除商品的方法
	 *@function:通过修改商品的状态达到删除的效果,给用户那边看不到它之前加入的个人商品
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "删除个人商城首页的所有或者单件商品", value = "/delete_person_mall_goods.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/delete_person_mall_goods.htm"})
	public void delete_person_mall_goods(HttpServletRequest request,HttpServletResponse response,
			String muilt_person_mall_ids){
		String[] person_mall_ids=muilt_person_mall_ids.split(",");
		boolean ret=false;
		for(int i=0;i<person_mall_ids.length;i++){
			ret=false;
			PersonMallApi personMallApi=this.personMallApiService.getObjById(Long.valueOf(person_mall_ids[i]));
			if(personMallApi!=null){
				personMallApi.setDelete_status(1);
				this.personMallApiService.update(personMallApi);
			}
			ret=true;
		}
		if(ret){
			ApiUtils.json(response, "","删除成功", 0);
		}else{
			ApiUtils.json(response, "","删除失败", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:goods_id:商品的id,user_id:用户的id
	 *@description:修改商品的状态达到删除商品的方法
	 *@function:从商品详情里面更改商品的删除状态,先找出正在在个人商城显示的商品,因为正在个人商城显示的商品不能重复显示的。所以从这个里面可以找出要修改的商品
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "删除个人商城首页的所有或者单件商品", value = "/from_goods_details_delete.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/from_goods_details_delete.htm"})
	public void from_goods_details_delete(HttpServletRequest request,HttpServletResponse response,
			String goods_id,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="select obj from PersonMallApi as obj where obj.user.id="+user_id+" and obj.delete_status=0";
		List<?> person_all_goods=this.commonService.query(hql, null, -1, -1);
		Iterator<?> ite=person_all_goods.iterator();
		long exist_personMallApi=0;
		while(ite.hasNext()){
			PersonMallApi temp=(PersonMallApi)ite.next();
			if(temp.getPerson_goods().getId().toString().equals(goods_id)){
				exist_personMallApi=temp.getId().longValue();
			}
		}
		if(exist_personMallApi!=0){
			PersonMallApi personMallApi=this.personMallApiService.getObjById(exist_personMallApi);
			if(personMallApi!=null){
				personMallApi.setDelete_status(1);
				boolean ret=this.personMallApiService.update(personMallApi);
				if(ret){
					ApiUtils.json(response, "","删除成功", 0);
					return;
				}
			}
		}
		ApiUtils.json(response, "","个人商城没有对应的商品", 1);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:person_mall_ids_self_talks:上传的商品和对美片的看法的字符串
	 *@description:上传自己的美片
	 *@function:通过解析person_mall_ids_self_talks这个参数将每个用户要保存的美片插入到数据库
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SuppressWarnings("rawtypes")
	@SecurityMapping(display = false, rsequence = 0, title = "上传自己的美片", value = "/edit_self_photo.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/edit_self_photo.htm"})
	public void edit_self_photo(HttpServletRequest request,HttpServletResponse response,
			String person_mall_ids_self_talks,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		Goods goods=null;
		boolean ret=false;
		String person_mall_ids=new String();
		List<HashMap> id_string_list=JSON.parseArray(person_mall_ids_self_talks, HashMap.class); 
		for(int a=0;a<id_string_list.size();a++){
			ret=false;
			HashMap map=id_string_list.get(a);
			String person_mall_id=map.get("person_mall_id").toString();
			String self_photo_introduce=map.get("self_photo_introduce").toString();
			PersonMallApi personMallApi=this.personMallApiService.getObjById(Long.valueOf(person_mall_id));
			if(personMallApi!=null){
				personMallApi.setSelf_photo_introduce(self_photo_introduce);
				boolean result=this.personMallApiService.update(personMallApi);
				if(result){
					ret=true;
					person_mall_ids=person_mall_ids+person_mall_id+"_";
				}
			}
			if(a==0){
				goods=this.goodsService.getObjById(personMallApi.getPerson_goods().getId());
			}
		}
		String url=CommUtil.getURL(request)+"/go_to_self_h5.htm?"+"person_mall_ids="+person_mall_ids.toString();
		if(ret){
			PersonMallPhotoUrl personMallPhotoUrl=new PersonMallPhotoUrl();
			personMallPhotoUrl.setAddTime(new Date());
			personMallPhotoUrl.setDeleteStatus(false);
			personMallPhotoUrl.setHref_url(url);
			personMallPhotoUrl.setUser(user);
			personMallPhotoUrl.setGoods(goods);
			this.commonService.save(personMallPhotoUrl);
			ApiUtils.json(response, url,"制作成功", 0);
		}else{
			ApiUtils.json(response, url,"部分制作成功", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取该用户美片的所有的url
	 *@function:通过用户的user_id取出该用户美片的所有的url
	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取自己的h5界面的url", value = "/get_all_prettyPhoto_url.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/get_all_prettyPhoto_url.htm"})
	public void get_all_prettyPhoto_url(HttpServletRequest request,HttpServletResponse response,
			String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(user!=null){
			List<PersonMallPhotoUrl> out_put_personMallPhotoUrl=user.getPersonMallPhotoUrl();
			if(out_put_personMallPhotoUrl.size()>0){
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(User.class, "id,userName"));
				objs.add(new FilterObj(PersonMallPhotoUrl.class, "id,goods,href_url,addTime,user"));
				objs.add(new FilterObj(Goods.class,"id,goods_main_photo,goods_status,goods_inventory,goods_click"));
				objs.add(new FilterObj(Accessory.class, "name,path,ext"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, out_put_personMallPhotoUrl, "查询成功",0,filter);
			}
		}else{
			ApiUtils.json(response, "","对应用户不存在", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:person_mall_ids:外面商品的id集合
	 *@description:返回自己的美片h5界面
	 *@function:遍历person_mall_ids数组,通过循环的方式取出一个个personMallApi对象,然后加入到集合里面
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "返回一个h5界面", value = "/go_to_self_h5.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping({"/go_to_self_h5.htm"})
	public ModelAndView go_to_self_h5(HttpServletRequest request,HttpServletResponse response,
			String person_mall_ids){
		ModelAndView mv = new JModelAndView("/go_to_self_h5.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String[] goods_mall_ids=person_mall_ids.split("_");
		List<PersonMallApi> personMallApi_list=new ArrayList<PersonMallApi>();
		for(int i=0;i<goods_mall_ids.length;i++){
			PersonMallApi personMallApi=this.personMallApiService.getObjById(Long.valueOf(goods_mall_ids[i]));
			if(personMallApi!=null){
				personMallApi_list.add(personMallApi);
			}
		}
		mv.addObject("obj", personMallApi_list);
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:添加某个店铺到收藏夹
	 *@function:**
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@RequestMapping(value="/addFavoriteStore.htm",method=RequestMethod.POST)
	public void addFavoriteStore(HttpServletRequest request,HttpServletResponse response,String userId,String password,String storeId){
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user==null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		Store store = this.storeService.getObjById(CommUtil.null2Long(storeId));
		if (store==null) {
			ApiUtils.json(response, "", "该店铺不存在", 1);
			return;
		}
		if (store.getStore_status()!=2) {
			ApiUtils.json(response, "", "该店铺未开通,无法收藏", 1);
			return;
		}
		String hql = "select obj from FavoriteStore as obj where obj.deleteStatus = false and obj.store.id = " + store.getId() + " and obj.user.id = " + user.getId();
		List<FavoriteStore> favoriteStores = this.favoriteStoreService.query(hql, null, -1, -1);
		if (favoriteStores.size()>0) {
			ApiUtils.json(response, "", "该店铺已经收藏", 1);
			return;
		}
		hql = "select count(obj) from FavoriteStore as obj where obj.deleteStatus = false and obj.user.id = " + user.getId();
		List<?> count = this.commonService.query(hql, null, -1, -1);
		if (count.size()>0&&CommUtil.null2Int(count.get(0))>=50) {
			ApiUtils.json(response, "", "最多收藏50个店铺", 1);
			return;
		}
		FavoriteStore favoriteStore = new FavoriteStore(new Date(),user,store);
		favoriteStore.setDeleteStatus(false);
		boolean save = this.favoriteStoreService.save(favoriteStore);
		if (save) {
			ApiUtils.json(response, "", "收藏成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "收藏失败", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:删除店铺收藏夹中的店铺
	 *@function:**
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@RequestMapping(value="/deleteFavoriteStores.htm",method=RequestMethod.POST)
	public void deleteFavoriteStores(HttpServletRequest request,HttpServletResponse response,String userId,String password,String storeIds){
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user==null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		if (CommUtil.null2String(storeIds).equals("")) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		List<String> list = Arrays.asList(storeIds.split(","));
		for (String s : list) {
			String hql = "select obj from FavoriteStore as obj where obj.deleteStatus = false and obj.store.id = " + s + " and obj.user.id = " + user.getId();
			List<FavoriteStore> favoriteStores = this.favoriteStoreService.query(hql, null, -1, -1);
			if (favoriteStores.size()>0) {
				FavoriteStore favoriteStore = favoriteStores.get(0);
				favoriteStore.setDeleteStatus(true);
				this.favoriteStoreService.update(favoriteStore);
			}
		}
		List<FavoriteStore> favoriteStores = this.getFavoriteStores(user, 0, 20);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Store.class, "id,store_name,store_logo,store_status,gradey"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(FavoriteStore.class, "id,store,addTime"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, favoriteStores, "取消收藏成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:查看店铺收藏夹中的店铺
	 *@function:**
，	 *@exception:*******
	 *@method_detail:*********
	 *@variable:*******
	 ***/
	@RequestMapping(value="/GetFavoriteStore.htm",method=RequestMethod.POST)
	public void GetFavoriteStore(HttpServletRequest request,HttpServletResponse response,String userId,String password,String currentPage){
		User user = ApiUtils.erifyUser(userId, password, this.userService);
		if (user==null) {
			ApiUtils.json(response, "", "用户名或密码错误", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		List<FavoriteStore> favoriteStores = this.getFavoriteStores(user, current_page, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Store.class, "id,store_name,store_logo,store_status,gradey"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(FavoriteStore.class, "id,store,addTime"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, favoriteStores, "success", 0,filter);
		return;
	}
	private List<FavoriteStore> getFavoriteStores(User user,int current_page,int pageSize){
		String hql = "select obj from FavoriteStore as obj where obj.deleteStatus = false and obj.user.id = " + user.getId() + " order by obj.addTime DESC";
		List<FavoriteStore> favoriteStores = this.favoriteStoreService.query(hql, null, current_page*pageSize, pageSize);
		return favoriteStores;
	}
}
