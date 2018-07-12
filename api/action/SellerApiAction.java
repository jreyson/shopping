package com.shopping.api.action;

import java.io.File;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestMethod;

import com.shopping.api.output.AppTransferData;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.core.tools.QRCodeEncoderHandler;
import com.shopping.core.tools.WebForm;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Role;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreClass;
import com.shopping.foundation.domain.StoreGrade;
import com.shopping.foundation.domain.StoreGradeLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreClassService;
import com.shopping.foundation.service.IStoreGradeLogService;
import com.shopping.foundation.service.IStoreGradeService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
@Controller
public class SellerApiAction {
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IStoreClassService storeClassService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IStoreGradeService storeGradeService;
	@Autowired
	private IStoreGradeLogService storeGradeLogService;
	@Autowired
	private IGoodsService goodsService;
	@RequestMapping({"/judge_store.htm"})
	public void judge_store(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		Long userId=new Long(user_id);
		User current_user=this.userService.getObjById(userId);
		Store store=this.storeService.getObjByProperty("user.id", current_user.getId());
		if(store!=null){
			ApiUtils.json(response, "", "存在店铺", 0);
		}else{
			ApiUtils.json(response, "", "没有店铺", 1);
		}
	}
	@SecurityMapping(display = false, rsequence = 0, title = "申请店铺第一步", value = "/app_store_create_first.htm*", rtype = "buyer", rname = "申请店铺", rcode = "create_store", rgroup = "申请店铺")
	@RequestMapping({"/app_store_create_first.htm"})
	public void app_store_create_first(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		int store_status=0;
		Long userId=new Long(user_id);
		User current_user=this.userService.getObjById(userId);
		Store store=this.storeService.getObjByProperty("user.id", current_user.getId());
		if(store!=null){
			store_status=store.getStore_status();
		}
		if(this.configService.getSysConfig().isStore_allow()){
			if(store_status==0){
				List<StoreGrade> store_grade=this.storeGradeService.query(
						"select obj from StoreGrade obj where obj.sequence=0 order by obj.sequence asc", 
						null, -1, -1);
				String store_create_session=CommUtil.randomString(32);
				request.getSession().setAttribute("store_create_session", store_create_session);
				if(store_grade.size()>0){
					List<Object> out_put=new ArrayList<Object>();
					//out_put.add(store_create_session);
					out_put.add(store_grade);
					List<FilterObj> obj=new ArrayList<FilterObj>();
					obj.add(new FilterObj(StoreGrade.class,"id,gradeName,sysGrade,audit,goodsCount,sequence,spaceSize,content,price,add_funciton,templates"));
					CustomerFilter filter = ApiUtils.addIncludes(obj);
					ApiUtils.json(response, out_put, "查询成功", 0, filter);
				}else{
					ApiUtils.json(response, "", "查询失败", 1);
				}
			}
		}
	}
	@SecurityMapping(display = false, rsequence = 0, title = "申请店铺第二步", value = "/app_store_create_first.htm*", rtype = "buyer", rname = "申请店铺", rcode = "create_store", rgroup = "申请店铺")
	@RequestMapping({"/app_store_create_second.htm"})
	public void app_store_create_second(HttpServletRequest request,
			HttpServletResponse response,String grade_id,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		Long userId=new Long(user_id);
		User current_user=this.userService.getObjById(userId);
		Store store=this.storeService.getObjByProperty("user.id", current_user.getId());
		int store_status=store==null?0:store.getStore_status();
		if(this.configService.getSysConfig().isStore_allow()){
			if(grade_id==null||"".equals(grade_id)){
				ApiUtils.json(response, "", "请传grade_id参数", 1);
			}else{
				if(store_status==0){
					List<Area> area=this.areaService.query("select obj from Area obj where obj.parent.id=null", 
							null, -1, -1);
					List<StoreClass> storeClass=this.storeClassService.query("select obj from StoreClass obj where obj.parent.id=null", 
							null, -1, -1);		
					List<Object> out_put=new ArrayList<Object>();
					out_put.add(area);
					out_put.add(storeClass);
					//out_put.add(grade_id);
					List<FilterObj> objs = new ArrayList<FilterObj>();
					objs.add(new FilterObj(Area.class, "areaName,parent,sequence,level,id"));
					objs.add(new FilterObj(StoreClass.class, "className,sequence,parent,id"));
					//生成指定的过滤器
					CustomerFilter filter = ApiUtils.addIncludes(objs);
					ApiUtils.json(response, out_put, "查询成功",0,filter);
				}else{
					ApiUtils.json(response, "","查询失败", 1);
				}
			}
		}
	}
	@RequestMapping({"/out_put_child_area.htm"})
	public void out_put_child_area(HttpServletRequest request,HttpServletResponse response,
			String parent_area_id){
		Map<String, Long> parame=new HashMap<String, Long>();
		parame.put("id", Long.valueOf(parent_area_id));
		List<Area> out_put_child=this.areaService.query("select obj from Area obj where obj.parent.id=:id",
				parame, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Area.class, "areaName,id"));
		//生成指定的过滤器
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put_child, "查询成功",0,filter);
	}
	@RequestMapping({"/out_put_child_storeClass.htm"})
	public void out_put_child_storeClass(HttpServletRequest request,HttpServletResponse response,
			String id){
		Map<String,Long> mp=new HashMap<String,Long>();
		mp.put("id", Long.valueOf(id));
		List<StoreClass> storeClass=this.storeClassService.query("select obj from StoreClass obj where obj.parent.id=:id", 
				mp, -1, -1);
		///Iterator<Area> it=area.iterator();
		//List aa=new ArrayList();
	/*	while(it.hasNext()){
			Area area1=it.next();
			Map mp=new HashMap();
			mp.put("id", area1.getId());
			List<Area> area2=this.areaService.query("select obj from Area obj where obj.parent.id=:id", 
					mp, -1, -1);
			area1.setChilds(area2);
			aa.add(area1);
		}*/
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(StoreClass.class, "className,sequence,id"));
		//objs.add(new FilterObj(StoreClass.class, "className,sequence,parent,id"));
		//生成指定的过滤器
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, storeClass, "查询成功",0,filter);
	}
	
	@RequestMapping({"/app_store_create_finish.htm"})
	public void app_store_create_finish(HttpServletRequest request,
			HttpServletResponse response,String area_id,String storeClass_id,
			String grade_id,String store_create_session,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		//String store_create_session_judge=CommUtil.null2String(request.getSession(false).getAttribute("store_create_session"));
		//if(!"".equals(store_create_session)&&store_create_session_judge.equals(store_create_session)){
			//request.getSession().removeAttribute("store_create_session");
			//取出当前正在登陆的用户
			Long userId=new Long(user_id);
			User current_user=this.userService.getObjById(userId);
			Store user_store=this.storeService.getObjByProperty("user.id", current_user.getId());
			int store_status=user_store==null?0:user_store.getStore_status();
			//如果店铺的状态是0的话,说明店铺是正在在创建的  
			if(store_status==0){
				//创建一个WebForm的实例,将提交的form表单的基本类型的字段属性提取出来,自动装配
				WebForm wf=new WebForm();
				//调用ef.toPo这个方法给store这个对象装配属性值
				Store store = (Store) wf.toPo(request, Store.class);
				StoreClass storeClass=this.storeClassService.getObjById(
						Long.valueOf(storeClass_id));
				store.setSc(storeClass);
				//下面开始给这个area对象装配属性
				Area area=this.areaService.getObjById(Long.valueOf(area_id));
				store.setArea(area);
				StoreGrade storeGrade=this.storeGradeService.getObjById(
						Long.valueOf(grade_id));
				store.setGrade(storeGrade);
				store.setTemplate("default");
				store.setAddTime(new Date());
				store.setDeleteStatus(false);
				store.setStore_second_domain("shop"+current_user.getId().toString());
				//保存到数据库
				this.storeService.save(store);
				/*生成关于店铺的二维码
				System.out.println(request.getSession().getServletContext().getRealPath("/"));
				request.getSession.getServletContent.getRealPath("/")得到该servlet所在项目的物理地址
				取出了该项目对应的tomact的根路径,一个项目的servlet都集中管理在web应用下面
				request.getContextPath()返回webapps下web应用的根目录名,如果是root默认项目返回"/",不是根目录返回该项目("/shopping"),得到该servlet所在项目的项目名
				request.getRealpath("/")得到的是实际的物理路径，也就是你的项目所在服务器中的路径
				request.getScheme() 等到的是协议名称，默认是http
				request.getServerName() 获取到的是本次请求中的域名
				request.getServerPort() 得到的是服务器的配置文件中配置的端口号 比如 8080等等*/
				//tomcat的默认端口号是80
				//String path=request.getSession().getServletContext().getRealPath("/")+
				String path=SystemResPath.imgUploadUrl+
						File.separator+"upload"+
						File.separator+"store"+
						File.separator+store.getId().longValue();
				/*创建了一个文件夹
				myFilePath.exists()测试此抽象路径名表示的文件或目录是否存在。
				myFilePath.isDirectory() 测试此抽象路径名表示的文件是否是一个目录*/
				CommUtil.createFolder(path);
				String store_url = CommUtil.getURL(request) + "/store_"
						+ store.getId() + ".htm";
				QRCodeEncoderHandler handler = new QRCodeEncoderHandler();
				handler.encoderQRCode(store_url, path + "/code.png");
				current_user.setStore(store);
				//取出店铺的等级去校验该店铺是否需要审核
				if (store.getGrade().isAudit()){
					store.setStore_status(2);
				}else{
					store.setStore_status(2);
				}
				//跟新用户的角色
				if (current_user.getUserRole().equals("BUYER")) {
					current_user.setUserRole("BUYER_SELLER");
				}
				if (current_user.getUserRole().equals("ADMIN")) {
					current_user.setUserRole("ADMIN_BUYER_SELLER");
				}
				Map<String, String> params = new HashMap<String, String>();
				params.put("type", "SELLER");
				List<Role> roles = this.roleService.query(
						"select obj from Role obj where obj.type=:type",
						params, -1, -1);
				current_user.getRoles().addAll(roles);
				current_user.setFreezeBlance(BigDecimal.valueOf(1));
//				if(current_user.getId()>130000){
//					if (current_user.getZhiwei()==null||current_user.getZhiwei().getId()==0) {
//						BuMen bumen=(BuMen) this.commonService.getById("BuMen", "515");
//						current_user.setBumen(bumen);
//						this.userService.update(current_user);
//						CommUtil.add_group_member(bumen.getGroup_id().toString(), current_user.getId().toString());
//					}			
//					if(current_user.getIs_huanxin()==0){//如果用户没有注册环信
//						CommUtil.huanxin_reg(current_user.getId().toString(), current_user.getPassword(), current_user.getUserName());
//						current_user.setIs_huanxin(1);
//						userService.update(current_user);
//					}					
//				}
				boolean ret=this.userService.update(current_user);
				if(ret){
					ApiUtils.json(response, "","店铺申请成功", 0);
				}
			}
		/*}else{
			ApiUtils.json(response, "","表单失效,请重新填写", 1);
		}*/
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取店铺等级
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetStoreRank.htm", method = RequestMethod.POST)
	public void appGetStoreRank(HttpServletRequest request,HttpServletResponse response,String userId){
		Long user_id=CommUtil.null2Long(userId);
		if (user_id==-1) {
			ApiUtils.json(response, "","参数错误", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "","用户不存在", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "","该用户没有店铺", 1);
			return;
		}
		String hql="select obj from StoreGrade as obj where obj.deleteStatus = false order by gradeLevel";
		List<StoreGrade> storeGrades = this.storeGradeService.query(hql, null, -1, -1);
		AppTransferData info=new AppTransferData();
		info.setFirstData(store);
		info.setSecondData(storeGrades);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(Store.class, "id,store_name,store_telephone,store_logo,store_status,grade,update_grade"));
		objs.add(new FilterObj(Accessory.class, "name,path,ext"));
		objs.add(new FilterObj(StoreGrade.class, "id,gradeName,gradeLevel,audit,goodsCount,storegradeIcon"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, info,"获取店铺等级成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:升级店铺等级
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUpgradeStoreRank.htm", method = RequestMethod.POST)
	public void appUpgradeStoreRank(HttpServletRequest request,HttpServletResponse response,String userId,String password,String storeGradeId){
		if (ApiUtils.is_null(storeGradeId,password,userId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "","该用户没有店铺", 1);
			return;
		}
		if (store.getUpdate_grade()!=null) {
			ApiUtils.json(response, "","升级审核中...", 1);
			return;
		}
		StoreGrade storeGrade = this.storeGradeService.getObjById(CommUtil.null2Long(storeGradeId));
		if (storeGrade==null) {
			ApiUtils.json(response, "", "该店铺等级不存在", 1);
			return;
		}
		if (store.getGrade().getGradeLevel()>=storeGrade.getGradeLevel()) {
			ApiUtils.json(response, "","申请店铺等级不能比现店铺等级低", 1);
			return;
		}
		store.setUpdate_grade(storeGrade);
		boolean update = this.storeService.update(store);
		if (update) {
			StoreGradeLog grade_log = new StoreGradeLog();
			grade_log.setAddTime(new Date());
			grade_log.setStore(store);
			this.storeGradeLogService.save(grade_log);
			ApiUtils.json(response, "","申请提交成功", 0);
			return;
		}else {
			ApiUtils.json(response, "","申请失败,请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:卖家设置单件商品佣金隐藏，显示
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSetGoodsHideStatus.htm", method = RequestMethod.POST)
	public void appSetGoodsHideStatus(HttpServletRequest request,HttpServletResponse response,String userId,String password,String goodsId,String status){
		if (ApiUtils.is_null(goodsId,password,userId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "","该用户没有店铺", 1);
			return;
		}
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if (goods==null) {
			ApiUtils.json(response, "","该商品不存在", 1);
			return;
		}
		if (goods.getGoods_store().getId()!=store.getId()) {
			ApiUtils.json(response, "","没有权限", 1);
			return;
		}
		if (CommUtil.null2String(status).equals("hide")) {
			goods.setIsHideRebate(status);
		}else {
			goods.setIsHideRebate(null);
		}
		boolean update = this.goodsService.update(goods);
		if (update) {
			ApiUtils.json(response, "","商品佣金状态设置成功", 0);
			return;
		}else {
			ApiUtils.json(response, "","设置失败，请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:卖家设置全部商品佣金隐藏，显示
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSetAllGoodsHideStatus.htm", method = RequestMethod.POST)
	public void appSetAllGoodsHideStatus(HttpServletRequest request,HttpServletResponse response,String userId,String password,String status){
		if (ApiUtils.is_null(password,userId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "","该用户没有店铺", 1);
			return;
		}
		if (!CommUtil.null2String(status).equals("hide")) {
			status=null;
		}
		String sql="UPDATE shopping_goods AS obj SET obj.isHideRebate ='" + status + "' WHERE obj.goods_store_id = " + store.getId();
		int num = this.commonService.executeNativeSQL(sql);
		if (num>0) {
			ApiUtils.json(response, "","修改店铺佣金状态成功", 0);
			return;
		}else {
			ApiUtils.json(response, "","修改失败，请重试", 1);
			return;
		}
	}
}
