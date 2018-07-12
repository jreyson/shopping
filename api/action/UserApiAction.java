package com.shopping.api.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.asm.internal.HandleProviderDelimiter;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.GroupApi;
import com.shopping.api.domain.MyTeamEntity;
import com.shopping.api.domain.RecordUserClickEntity;
import com.shopping.api.domain.ZhiWeiRecoderEntity;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.domain.browseRecords.UserBrowseRecords;
import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.api.domain.countBuy.CountPriceDomain;
import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.api.domain.invitationRank.InvitationRank;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.rank.UserRankName;
import com.shopping.api.domain.reserve.ReserveScale;
import com.shopping.api.domain.userAttribute.AppClickNum;
import com.shopping.api.output.APPBillData;
import com.shopping.api.output.AppBillsDataTemp;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.UserTemp;
import com.shopping.api.output.UserTempData;
import com.shopping.api.service.IGroupApiService;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.api.service.IIntegralRechargeService;
import com.shopping.api.service.IMyTeamService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.api.tools.BigDecimalUtil;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.core.tools.WebForm;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.BankType;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.PredepositCash;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Role;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.SysConfig;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.Xianji;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositCashService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.xml.internal.bind.v2.model.core.ID;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
/***
 *@author:akangah
 *@description:用户相关信息管理控制器
 ***/
@Controller
public class UserApiAction {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IMyTeamService myTeamService;
	@Autowired
	private IGroupApiService groupApiService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IIntegralRechargeService integralRechargeService;
	@Autowired
	private IIntegralDepositService integralDepositService;
	@Autowired
	private IPredepositCashService predepositCashService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:searchValue:搜索值,loginType:登陆类型,password:密码
	 *@description:通过登陆接口获取的用户信息,并初始化一些数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_login.htm"})
	public void api_login(HttpServletRequest request,
			HttpServletResponse response, String password, String searchValue) {
		System.out.println("用户:"+searchValue+"--"+password+"登陆");
//		String hql="select obj from User as obj where obj.id='"+searchValue+"'"+" or obj.mobile='"+searchValue+"'"+" or obj.userName='"+searchValue+"'";
//		List<User> userList=this.userService.query(hql, null, -1,-1);
		if (CommUtil.null2String(password).equals("")||CommUtil.null2String(searchValue).equals("")) {
			ApiUtils.json(response, "", "登陆帐号与密码不能为空！", 1);
			return;
		}
		String type = ApiUtils.judgmentType(searchValue.trim());
		String hql;
		if (type.equals("mobile")) {
			hql="select obj from User as obj where obj.mobile='"+searchValue+"'";
		}else if (type.equals("id")) {
			hql="select obj from User as obj where obj.id='"+searchValue+"'";
		}else if (type.equals("userName")){
			hql="select obj from User as obj where obj.userName='"+searchValue+"'";
		}else {
			hql="select obj from User as obj where obj.id='"+searchValue+"'"+" or obj.mobile='"+searchValue+"'"+" or obj.userName='"+searchValue+"'";
		}
		List<User> userList=this.userService.query(hql, null, -1,-1);
		if(userList.size()>1){
			ApiUtils.json(response, "", "用户不唯一,请选择其他方式登陆,请换其他方式登陆", 1);
			return;
		}
		if(userList.size()>0){
			User user = userList.get(0);
			String psw = user.getPassword();
			String lowerCase=Md5Encrypt.md5(password).toLowerCase();
			if(lowerCase.equals(psw)){
				if(user.getZhixian()==null){
					ZhiXianEntity zhixian=(ZhiXianEntity) this.commonService.getById("ZhiXianEntity", "1");
					user.setZhixian(zhixian);
				}
				if(user.getZhiwei()==null){
					ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
					user.setZhiwei(zhiwei);
				}
				if(user.getBumen()==null){
					BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
					user.setBumen(bumen);
				}
				boolean isHuanxin=false;
				boolean ishaveHuanxin=false;
				if(user.getIs_huanxin()==0){//注册环信
					JSONObject huanxin_reg=CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUsername());
					if(huanxin_reg!=null){
						JSONArray jsonArray = huanxin_reg.getJSONArray("entities");
						String error=huanxin_reg.getString("error");
						if(jsonArray!=null||"duplicate_unique_property_exists".equals(error)){
							user.setIs_huanxin(1);
							isHuanxin=true;
						}
					}
				}
				if (user.getIs_huanxin()==1) {
					ishaveHuanxin=ApiUtils.isHaveHuanxin(user.getId());
					if (!ishaveHuanxin) {
						user.setIs_huanxin(0);
					}
				}
				user.setIshaveHuanxin(ishaveHuanxin);
				if (isHuanxin) {
					CommUtil.update_user_password(user.getPassword().toString(), user.getId().toString());//将用户的密码同步至环信服务器
					if(user.getIs_yet_add_group()==0){//自动加入部门群
						JSONObject jsobject=CommUtil.add_group_member(user.getBumen().getGroup_id().toString(), user.getId().toString());
						boolean is_succ=false;
						if(jsobject.getJSONObject("data")!=null){
							is_succ=jsobject.getJSONObject("data").getBoolean("result");
						}
						if(is_succ){
							user.setIs_yet_add_group(1);
						}
					}
					if(user.getAddSelfDanBaoRenToFriend()==0){//自动和担保人加好友
						if(!"".equals(user.getDan_bao_ren())){
							String queryUser="select obj from User as obj where obj.userName='"+user.getDan_bao_ren()+"'"; 
							List<User> danBaoUserList=this.userService.query(queryUser, null, -1, -1);
							if(danBaoUserList.size()>0){
								User danBaoRen=danBaoUserList.get(0);
								if(danBaoRen.getIs_huanxin()==0){
									JSONObject huanxin_reg=CommUtil.huanxin_reg(danBaoRen.getId().toString(), danBaoRen.getPassword(), danBaoRen.getUsername());
									JSONArray jsonArrays = huanxin_reg.getJSONArray("entities");
									String errors=huanxin_reg.getString("error");
									if(jsonArrays!=null||"duplicate_unique_property_exists".equals(errors)){
										danBaoRen.setIs_huanxin(1);
										this.userService.update(danBaoRen);
									}
								}
								CommUtil.app_add_friendToUser(user.getId().toString(), danBaoRen.getId().toString());
								user.setAddSelfDanBaoRenToFriend(1);
							}
						}
					}
				}
				user.setLastLoginDate(new Date());
				user.setLoginCount(user.getLoginCount() + 1);
				user.setLastLoginIp(ApiUtils.getWebIp());
				boolean ret=this.userService.update(user);
				if(ret){
					List<FilterObj> objs = new ArrayList<FilterObj>();
					objs.add(new FilterObj(User.class,
							"id,userName,password,availableBalance,mobile,photo,tj_status,ishaveHuanxin,userRank"));
					objs.add(new FilterObj(Accessory.class, "path,name"));
					objs.add(new FilterObj(UserRank.class, "userRankName,gradeSmallIcon"));
					objs.add(new FilterObj(UserRankName.class, "id,rankName"));
					CustomerFilter filter = ApiUtils.addIncludes(objs);
					ApiUtils.updateUserRenk(0, user, commonService, userService);//更新会员等级
					ApiUtils.json(response, user, "登陆成功", 0, filter);
					return;
				}
			}else{
				ApiUtils.json(response, "", "账户密码不匹配", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该用户不存在或登陆方式出错", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:searchValue:搜索值,searchType:搜索类型,currentPage:当前页
	 *@description:得到用户列表或单个用户,完成添加好友的任务
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/search_user.htm"})
	public void search_user(HttpServletRequest request,
			HttpServletResponse response, String searchValue, 
			String searchType,String currentPage){
		String chooserHql="";
		String hql="select obj from User as obj ";
		List<User> userList=new ArrayList<User>();
		int current_page=0;
		int pageSize=20;
		searchValue = CommUtil.null2String(searchValue);
		searchType=CommUtil.null2String(searchType);
		if(!"".equals(CommUtil.null2String(currentPage))){
			current_page=CommUtil.null2Int(currentPage);
		}
		if("userName".equals(searchType)){
			chooserHql="where obj.userName like '%"+searchValue+"%'";
		}
		if("mobile".equals(searchType)){
			chooserHql="where obj.mobile="+searchValue;
		}
		if("userId".equals(searchType)){
			chooserHql="where obj.id="+searchValue;
		}
		userList=this.userService.query(hql+chooserHql, null, current_page*pageSize,pageSize);
		if(userList.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class,
					"id,userName,password,availableBalance,mobile,photo"));
			objs.add(new FilterObj(Accessory.class, "name,path,ext"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, userList, "查询成功", 0, filter);
			return;
		}else{
			ApiUtils.json(response, "", "没有相应的用户", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:store_id:店铺id,user_id:用户的id
	 *@description:得到用户的店铺或者用户的具体信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/get_user_by_id.htm" })
	public void get_user_by_id(HttpServletRequest request,
			HttpServletResponse response, String store_id,
			String user_id){
		if(!"".equals(CommUtil.null2String(store_id))){
			Store store = this.storeService.getObjById(CommUtil.null2Long(store_id));
			if(store!=null){
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(User.class, "id,userName,mobile,photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, store.getUser(), "查询成功", 0, filter);
				return;
			}
		}
		if(!"".equals(CommUtil.null2String(user_id))){  
			Long userId = CommUtil.null2Long(user_id);
			if (userId.longValue()==-1) {
				return;
			}
			User user = this.userService.getObjById(userId);
			if(user!=null){
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(User.class, "id,userName,mobile,photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, user, "查询成功", 0, filter);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:usernames:是userId的集合,类似于这样的结构["1","4282","88757"]
	 *@description:得到用户的店铺或者用户的具体信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_get_userlist.htm" })
	public void api_get_userlist(HttpServletRequest request,
			HttpServletResponse response, String usernames){
		List<Long> ids_list=JSON.parseArray(usernames,Long.class);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", ids_list);
		String hql="select obj from User obj where obj.id in (:id)";
		List<User> user_list = new ArrayList<User>();
		if(ids_list.size()>0){
			user_list = this.userService.query(hql, map, -1, -1);
		}
		if(user_list.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class, "id,userName,mobile,photo"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, user_list, "查询成功", 0, filter);
		}else{
			ApiUtils.json(response, "", "查询失败", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:usernames:是group_id的集合,类似于这样的结构["1","4282","88757"]
	 *@description:得到群的信息和群的头像
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/init_group_avater.htm"})
	public void init_group_avater(HttpServletRequest request,
			HttpServletResponse response){
		String group_id_list=request.getParameter("group_id_list");
		List<Long> group_list=new ArrayList<Long>();
		try {
			group_list=JSON.parseArray(group_id_list, Long.class);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("group_id", group_list);
		String hql="select obj from GroupApi obj where obj.group_id in (:group_id)";
		List<?> groupList=new ArrayList<GroupApi>();
		if(group_list.size()>0){
			groupList=this.commonService.query(hql,map, -1, -1);
		}
		if(groupList.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(GroupApi.class, "photo"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, groupList, "查询成功", 0, filter);
		}else{		
			ApiUtils.json(response, "", "查询失败", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:生成带有userId的二维码图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/generate.htm" })
	public void generate(HttpServletRequest request,
			HttpServletResponse response){
		String userId=request.getParameter("userId");
		String url="http://qr.liantu.com/api.php?text=userId="+userId;
		try {
			 PrintWriter ss=response.getWriter();
			 ss.write(url);
			if(ss!=null){
				ss.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:上传用户的头像
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/upload_photo.htm" })
	public void upload_photo(HttpServletRequest request,
			HttpServletResponse response){
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0L);
//		String realPath = request.getRealPath("/");
		try {
//			String filePath = request.getSession().getServletContext().getRealPath("")
			String filePath = SystemResPath.imgUploadUrl+File.separator
					+"upload"+File.separator+"avatar";
			File uploadDir = new File(filePath);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			Long user_id = CommUtil.null2Long(request.getParameter("user_id"));
			String imageType = CommUtil.null2String(request
					.getParameter("image_type"));
			if ("".equals(imageType)) {
				imageType = ".jpg";
			}
			String bigAvatarContent = CommUtil.null2String(request
					.getParameter("big_avatar"));
			User user =this.userService.getObjById(user_id);
			String bigAvatarName = user.getId()+System.currentTimeMillis()
					/1000 + "_big";
			//生成对应的图片
			CommUtil.saveImage(filePath, imageType, bigAvatarContent,
					bigAvatarName);
			//将图片保存到数据库中
			Accessory photo = new Accessory();
			if (user.getPhoto() != null) {
				photo = user.getPhoto();
			}
			photo.setAddTime(new Date());
			photo.setWidth(132);
			photo.setHeight(132);
			photo.setName(bigAvatarName + imageType);
			photo.setExt(imageType);
			photo.setPath(this.configService.getSysConfig().getUploadFilePath()
					+ "/avatar");
			if (user.getPhoto() != null){
				this.accessoryService.update(photo);
			}else{
				this.accessoryService.save(photo);
			}
			user.setPhoto(photo);
			this.userService.update(user);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Accessory.class, "path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, photo, "添加成功", 0, filter);
		} catch (Exception e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "添加失败", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:上传群头像
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/upload_groupPhoto.htm"})
	public void upload_groupPhoto(HttpServletRequest request,
			HttpServletResponse response){
		//设置响应类型
		response.setContentType("application/json;charset=UTF-8");
		//设置不要客户端缓存返回的数据
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0L);
		try {
//			String FilePath=request.getSession().getServletContext()
			String FilePath=SystemResPath.imgUploadUrl
					+"\\upload\\groupAvater";
			File uploadDir=new File(FilePath);
			if(!uploadDir.exists()){
				uploadDir.mkdirs();
			}
			Long group_id=CommUtil.null2Long(
					request.getParameter("group_id"));
			String image_type=".jpg";
			String groupContent=CommUtil.null2String(
					request.getParameter("groupContent"));
			String groupAvaterName=group_id+"_groupbig";
			String groupName=request.getParameter("group_name");
			//生成了一张图片
			CommUtil.saveImage(FilePath, image_type, groupContent,
					groupAvaterName);
			GroupApi group=(GroupApi) this.commonService.getByWhere("GroupApi",
					"obj.group_id="+group_id);
			Accessory photo=null;
			if(group==null){
				photo=new Accessory();
				photo.setWidth(132);
				photo.setHeight(132);
				photo.setAddTime(new Date());
				photo.setName(groupAvaterName+image_type);
				photo.setExt("jpg");
				photo.setPath(this.configService.getSysConfig().getUploadFilePath()
						+"/groupAvater");
				//将图片保存到数据库
				this.accessoryService.save(photo);
				group=new GroupApi();
				group.setGroup_id(group_id);
				group.setGroupName(groupName);
				group.setPhoto(photo);
				//将群组保存到数据库中
				this.groupApiService.save(group);	
			}else{
				photo=group.getPhoto();
				if(photo==null){
					photo=new Accessory();
					photo.setWidth(132);
					photo.setHeight(132);
					photo.setAddTime(new Date());
					photo.setName(groupAvaterName+image_type);
					photo.setExt("jpg");
					photo.setPath(this.configService.getSysConfig().getUploadFilePath()
							+"/groupAvater");
					//将图片保存到数据库
					this.accessoryService.save(photo);
					group.setPhoto(photo);
					this.groupApiService.update(group);
				}else{
					photo.setName(groupAvaterName+image_type);
					photo.setExt(image_type);
					photo.setPath(this.configService.getSysConfig().getUploadFilePath()
							+"/groupAvater");
					this.accessoryService.update(photo);
					group.setPhoto(photo);
					this.groupApiService.update(group);
				}
			}	
			List<FilterObj> objs=new ArrayList<FilterObj>();
			objs.add(new FilterObj(Accessory.class,"path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);	
			ApiUtils.json(response, photo, "添加成功", 0, filter);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			ApiUtils.json(response, "", "添加失败", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:registValue:注册值,registType:注册类型,password:密码,dan_bao_ren:担保人
	 *@description:用户在app端的注册,并初始化一些数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/api_regist.htm"})
	public void api_regist(HttpServletRequest request,
			HttpServletResponse response, String password
			, String dan_bao_ren,String userName,String mobile,String verificationCode){
		synchronized (this) {
			if(ApiUtils.is_null(userName,mobile,verificationCode)){
				ApiUtils.json(response, "", "参数错误", 1);
				return;
			}
			boolean verifyPhoneExistenceState = ApiUtils.verifyPhoneExistenceState(mobile, userService);
			if (verifyPhoneExistenceState) {
				ApiUtils.json(response, "","号码已存在或者号码格式不正确", 1);
				return;
			}
			HttpSession session = request.getSession();
			Object sessionRecNum = session.getAttribute("safePhone");
			Object sessionVerificationCode = session.getAttribute("safeCode");
			if (!mobile.equals(sessionRecNum)||!verificationCode.equals(sessionVerificationCode)) {
				ApiUtils.json(response, "","验证码错误", 1);
				return;
			}
			session.removeAttribute("safePhone");
			session.removeAttribute("safeCode");
			if("".equals(CommUtil.null2String(dan_bao_ren))){
				dan_bao_ren="夏天";
			}
			String hql="select obj from User as obj where obj.mobile='"+mobile+"' or obj.userName='"+userName+"'";
			List<User> userList=this.userService.query(hql, null, -1, -1);
			if(userList.size()>0){
				ApiUtils.json(response, "", "您注册的电话号码或用户名已经存在,不能注册", 1);
				return;
			}
			String daoQql="select obj from  User obj where obj.userName='"+dan_bao_ren+"'";
			List<User> daoList=this.userService.query(daoQql, null, -1, -1);
			if(daoList.size()==0){
				ApiUtils.json(response, "", "担保人不存在", 1);
				return;
			}else{
				int is_finish_userName=0;//当时将手机号码注册和用户名注册区分开,用此字段作为用户是否能够修改用户名的标识符,现在俩者合并到一起，0表示不用修改用户名
				User user = new User();
				ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
				ZhiXianEntity zhixian=(ZhiXianEntity) this.commonService.getById("ZhiXianEntity", "1");
				user.setZhiwei(zhiwei);
				user.setZhixian(zhixian);
				user.setIs_finish_userName(is_finish_userName);
				user.setBumen(daoList.get(0).getBumen());
				user.setAreaGradeOfUser(daoList.get(0).getAreaGradeOfUser());//和担保人所属区域一样
				user.setDan_bao_ren(dan_bao_ren);
				user.setTj_status(false);
				user.setUserName(userName);
				user.setMobile(mobile);
				user.setUserRole("BUYER");
				user.setAddTime(new Date());
				user.setEmail("");
				user.setPassword(Md5Encrypt.md5(password).toLowerCase());
				user.setFreezeBlance(BigDecimal.valueOf(0));
				user.setAvailableBalance(BigDecimal.valueOf(0));
				user.setIs_yet_add_group(0);
				Map<String,String> params=new HashMap<String, String>();
				params.put("type", "BUYER");
				
				List<Role> roles = this.roleService.query("select obj from Role as obj where obj.type=:type", params,-1, -1);
				Set<Role> set=user.getRoles();
				set.addAll(roles);//给注册的用户指定角色
				user.setRoles(set);
				user.setUserRank((UserRank) commonService.getById("UserRank", "1"));
				this.userService.save(user);
				Album album = new Album(new Date(),true,"默认相册",-10000,user);
				this.albumService.save(album);
//				 检测和更新担保人衔级
//				List<?> xianjiList = this.commonService.query( 
//									"select obj from Xianji obj ", null, -1, -1);
//				List<?> danbaoList =this.commonService
//									.executeNativeNamedQuery("select count(id) from shopping_user where dan_bao_ren='"
//											+ dan_bao_ren + "'");
//				List<?> danbaorenId = this.commonService
//									.executeNativeNamedQuery("select id from shopping_user where userName='"
//											+ dan_bao_ren + "'");
//				if (danbaoList.size() > 0 && danbaorenId.size() > 0) {
//					int dbId = CommUtil.null2Int(danbaorenId.get(0));
//					for (int i = 0; i < xianjiList.size(); i++) {
//						Xianji xianji = (Xianji) xianjiList.get(i);
//						int count = CommUtil.null2Int(danbaoList.get(0));
//						if (CommUtil.null2Int(count) >= xianji.getStartTd()
//								&& CommUtil.null2Int(count) <= xianji
//												.getTuanduiNum()) {
//							this.commonService.executeNativeSQL("update shopping_user set xianji_id="
//									+ xianji.getId()
//									+ " where id="
//									+ dbId);
//						}
//					}
//				}
//				JSONObject huanxin_reg = CommUtil.huanxin_reg(user.getId().toString(),
//						user.getPassword(), user.getUsername());
//				if (huanxin_reg != null) {
//					JSONArray jsonArray = huanxin_reg.getJSONArray("entities");
//					if (jsonArray.size() > 0) {
//						user.setIs_huanxin(1);
//						this.userService.update(user);
//					}
//				}
//				JSONObject jsobject=CommUtil.add_group_member(user.getBumen().getGroup_id().toString(), user.getId().toString());
//				boolean is_succ=jsobject.getJSONObject("data").getBoolean("result");
//				if(is_succ){
//					user.setIs_yet_add_group(1);
//				}
				boolean ret=this.userService.update(user);
				if(ret){
					ApiUtils.json(response, "", "注册成功", 0);
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取app端用户的通讯录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/api_user_concat.htm"})
	public void api_user_concat(HttpServletRequest request,
			HttpServletResponse response, String user_id) {
		if ("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "查询失败,请传用户id", 0);
			return;
		}
		JSONArray arr = CommUtil.huanxin_get_contacts(user_id);
		String where_str = "";
		String hql="";
		for (Object object : arr) {
			String id = object.toString();
			where_str = where_str + "," + id;
		}
		List<User> user_list = new ArrayList<User>();
		if (!"".equals(where_str)) {
			where_str = where_str.substring(1, where_str.length());
			hql="select obj from User obj where obj.id in (" + where_str+ ")";
			user_list = this.userService.query(hql, null, -1, -1);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,userName,mobile,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, user_list, "查询成功", 0, filter);
	}
	private void send_message(User accept_user,String msg){
		if(accept_user.getIs_huanxin()==0){
			CommUtil.huanxin_reg(accept_user.getId().toString(),
					accept_user.getPassword(), accept_user.getUsername());
			accept_user.setIs_huanxin(1);
			this.userService.update(accept_user);
		}
		String[] users={accept_user.getId().toString()};
		JSONObject messages=new JSONObject();
		messages.put("type", "txt");
		messages.put("msg", msg);
		String sender="150382";
		CommUtil.send_message_to_user(users, messages, sender);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:操作者的用户id,demotion_id:降级用户的id
	 *@description:将demotion_id所对应的用户的职位降为0
	 *@function:**
	 *@exception:*******
	 *@method_detail:**
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "将指定的用户的职位将为0", value = "/add_new_position.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/demotion_to_zero.htm", method = RequestMethod.POST)
	public void demotion_to_zero(HttpServletRequest request,
			HttpServletResponse response,String demotion_id,
			String login_user_id,String user_id,String messageState){
		if (CommUtil.null2Long(login_user_id)!=1) {
			ApiUtils.json(response, "", "目前为系统自动升降职位,手动操作职位功能已关闭", 1);
			return;
		}
		User executeUser=this.userService.getObjById(CommUtil.null2Long(user_id));
		if(executeUser!=null){
			ZhiWei zhiwei=executeUser.getZhiwei();
			if(zhiwei!=null){
				long zhiweId=zhiwei.getId();
				if(zhiweId>=132&&zhiweId<=221||zhiweId>=301&&zhiweId<=305){
					ApiUtils.json(response, "", "副职没有卸任职位权限", 1);
					return;
				}
			}else{
				ApiUtils.json(response, "", "当前用户没有职位不能卸职", 1);
				return;
			}
		}
		User demotion_user=null;
		User login_user=this.userService.getObjById(Long.valueOf(login_user_id));
		if(!"".equals(demotion_id)){
			demotion_user=this.userService.getObjById(Long.valueOf(demotion_id));
		}else{
			ApiUtils.json(response, "", "请上传卸任用户的id", 1);
			return;
		}
		if(demotion_user!=null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			AreaGradeOfUser agou=null;
			if(zhiwei!=null){
				if(demotion_user.getIs_vipOfUser()==1){
					if(login_user.getZhiwei().getId()!=1){
						ApiUtils.json(response, "", "该用户是付费会员,不允许卸职", 1);
						return;
					}
				}
				ZhiWei restoreZhiwei=demotion_user.getZhiwei();
				demotion_user.setZhiwei(zhiwei);
				demotion_user.setAreaGradeOfUser(agou);
				ZhiWeiRecoderEntity zre=new ZhiWeiRecoderEntity();
				zre.setAddTime(new Date());
				zre.setDeleteStatus(false);
				zre.setUser(login_user);
				zre.setMyselfUser(demotion_user);
				zre.setZhiwei(zhiwei);
				zre.setMsg(zhiwei.getName());
				this.commonService.save(zre);
				List<ZhiWeiRecoderEntity> rec_lsit=demotion_user.getZhiweiRec();
				rec_lsit.add(zre);
				demotion_user.setZhiweiRec(rec_lsit);
				boolean ret=this.userService.update(demotion_user);
				String msg=demotion_user.getUserName()+"你好，你的职位被降为无职位，请联系你的上级";
				this.send_message(demotion_user, msg);
				if(!"".equals(demotion_user.getSelf_group_id())&&demotion_user.getSelf_group_id()!=null){
					JSONObject jsonobject=CommUtil.delete_group(demotion_user.getSelf_group_id());
					boolean is_ok=jsonobject.getJSONObject("data").getBoolean("success");
					if(is_ok){ 
						demotion_user.setSelf_group_id("");
					}
				}
				if(ret){
					restoreZhiwei.setHideStatus(0);
					this.commonService.update(restoreZhiwei);
					//卸职以后判断是否需要发送短信
//					if (CommUtil.null2String(messageState).equals("send")) {
//						ApiUtils.pushQuitNoticeSMS(, )
//					}
					ApiUtils.json(response, "", "卸任职位成功", 0);
					return;
				}
			}
		}else{
			ApiUtils.json(response, "", "卸任职位的用户不存在", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:解散群主的用户id
	 *@description:获取用户id,找到相应的用户,将用户的self_group_id清空
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "以当前的用户id为群主到环信那边去创建一个群", value = "/create_self_group.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/clean_self_group_id.htm", method = RequestMethod.POST)
	public void clean_self_group_id(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(!"".equals(user.getSelf_group_id())&&user.getSelf_group_id()!=null){
			user.setSelf_group_id("");
			boolean ret=this.userService.update(user);
			if(ret){
				ApiUtils.json(response, "", "更新用户信息成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "更新用户信息成功", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:根据user_id获取用户下的团队(以该用户为担保人的用户)
	 *@description:获取用户的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取用户的团队成员", value = "/get_self_team.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/get_self_team.htm", method = RequestMethod.POST)
	public void get_self_team(HttpServletRequest request,
			HttpServletResponse response,String user_id,String currentPage){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(user_id)){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			int current_page=0;
			if("".equals(currentPage)||currentPage==null){
				current_page=0;
			}else{
				current_page=Integer.valueOf(currentPage).intValue();
			}
			int pageSize=20;
			String sql="SELECT "+
					   "COUNT(suc.id) AS clicks,su.id "+
					   "FROM shopping_user AS su "+
					   "LEFT JOIN shopping_user_clickapps AS suc "+
					      "ON su.id = suc.user_id "+
					   "WHERE su.id IN(SELECT "+
					                   "su.id "+
					                 "FROM shopping_user AS su "+
					                 "WHERE su.dan_bao_ren = '"+user.getUserName().toString()+"') "+
					   "GROUP BY su.id "+
					   "ORDER BY clicks DESC "+
					   "LIMIT "+current_page*pageSize+","+pageSize;
			List<?> user_ids=this.commonService.executeNativeNamedQuery(sql);
			List<UserTemp> out_put=new ArrayList<UserTemp>();
			if(user_ids.size()>0){
				for(Object obj:user_ids){
					Object[] ids=(Object[]) obj;
					User dan_bao_user=this.userService.getObjById(CommUtil.null2Long(ids[1].toString()));
					UserTemp usertemp=new UserTemp();
					usertemp.setUser(dan_bao_user);
					usertemp.setLiveness(ids[0].toString());
					out_put.add(usertemp);
				}
			}
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(UserTemp.class, "user,liveness"));
			objs.add(new FilterObj(User.class, "id,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			objs.add(new FilterObj(ZhiWei.class, "id,name"));
			objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
			objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
			objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, out_put, "查询成功", 0, filter);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_message:查询信息,search_type:搜索类型,currentPage:当前页
	 *@description:搜索用户的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "搜索用户的团队成员", value = "/search_team_member.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/search_team_member.htm", method = RequestMethod.POST)
	public void search_team_member(HttpServletRequest request,
			HttpServletResponse response,String user_message,
			String search_type,String currentPage){
		if(!"".equals(user_message)&&user_message!=null){
			String hql="";
			int current_page=0;
			int pageSize=20;
			if("".equals(currentPage)||currentPage==null){
				current_page=0;
			}else{
				current_page=Integer.valueOf(currentPage).intValue();
			}
			if("0".endsWith(search_type)){
				hql="select obj from User as obj where obj.id="+user_message;
			}
			if("1".endsWith(search_type)){
				hql="select obj from User as obj where obj.userName like '%"+user_message+"%'";
			}
			List<User> out_put=this.userService.query(hql, null, current_page*pageSize, pageSize);
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class, "id,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			objs.add(new FilterObj(ZhiWei.class, "id,name"));
			objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
			objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
			objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, out_put, "查询成功", 0, filter);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id currentPage:当前页
	 *@description:按添加的时间排序用户的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "按添加的时间排序用户的团队成员", value = "/sort_team_byAddTime.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/sort_team_byAddTime.htm", method = RequestMethod.POST)
	public void sort_team_byAddTime(HttpServletRequest request,
			HttpServletResponse response,String user_id,
			String currentPage,String orderBy){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		User user=this.userService.getObjById(Long.valueOf(user_id)); 
		String hql="select obj from User as obj where obj.dan_bao_ren='"+user.getUsername()+"' order by obj.addTime "+orderBy;
		List<User> out_put=this.userService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:当前点击用户的id
	 *@description:记录用户点击的次数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "记录用户点击的次数", value = "/record_user_click.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/record_user_click.htm", method = RequestMethod.POST)
	public void record_user_click(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if (user_id==null) {
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
		}else {
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(user!=null){
				RecordUserClickEntity click=new RecordUserClickEntity();
				click.setAddTime(new Date());
				click.setDeleteStatus(false);
				click.setUser(user);
				this.commonService.save(click);
//				invitationInvestiture(user.getId());
				String hql="select obj from AppClickNum as obj where obj.user.id ="+user.getId();
				List<AppClickNum> query = commonService.query(hql, null, -1, -1);
				if (query.size()>0) {
					AppClickNum appClickNum = query.get(0);
					appClickNum.setClickNum(appClickNum.getClickNum()+1);
					appClickNum.setLoginDate(new Date());
					commonService.update(appClickNum);
				}else {
					AppClickNum appClickNum=new AppClickNum();
					appClickNum.setAddTime(new Date());
					appClickNum.setClickNum(1);
					appClickNum.setDeleteStatus(false);
					appClickNum.setLoginDate(new Date());
					appClickNum.setUser(user);
					commonService.save(appClickNum);
				}
				ApiUtils.json(response, "", "添加成功", 0);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:send_user_id:当前登陆用户的id,accept_user_ids:接受用户的id,message:发送的文本消息内容
	 *@description:给用户发送群组消息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "给用户发送群组消息", value = "/send_group_message.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/send_group_message.htm", method = RequestMethod.POST)
	public void send_group_message(HttpServletRequest request,
			HttpServletResponse response,String send_user_id,String accept_user_ids,String message){
		User user=this.userService.getObjById(Long.valueOf(send_user_id));
		String[] accept_users=null;
		if(!"".equals(accept_user_ids)&&accept_user_ids!=null){
			accept_users=accept_user_ids.split(",");
			for(String user_id:accept_users){
				User acpt_user=this.userService.getObjById(Long.valueOf(user_id));
				if(acpt_user.getIs_huanxin()==0){
					CommUtil.huanxin_reg(acpt_user.getId().toString(), acpt_user.getPassword(), acpt_user.getUsername());
					continue;
				}
			}
		}
		JSONObject messages=new JSONObject();
		messages.put("type", "txt");
		messages.put("msg",message );
//		String sender=user.getUserName().toString();
		String sender=user.getId().toString();
		JSONObject ret=CommUtil.send_message_to_user(accept_users, messages, sender);
		if(ret.getJSONObject("data")!=null){
				ApiUtils.json(response, "", "发送成功", 0);
		}else{
			ApiUtils.json(response, "", "发送失败,请重试", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:当前登陆用户的id,accept_user_ids:加入用户的id
	 *@description:加入到我的团队
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "加入到我的团队", value = "/join_my_team.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/join_my_team.htm", method = RequestMethod.POST)
	public void join_my_team(HttpServletRequest request,
			HttpServletResponse response,String user_id,String accept_user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		User accept_user=this.userService.getObjById(Long.valueOf(accept_user_id));
		boolean is_can=false;
		if(accept_user_id==null){
			ApiUtils.json(response, "", "加入的用户不存在", 1);
			return;
		}
		List<MyTeamEntity> myTeamList=user.getMyteam();
		if(myTeamList.size()>0){
			for(MyTeamEntity myteam:myTeamList){
				long userId=myteam.getUser().getId();
				if(userId==Long.valueOf(accept_user_id)){
					is_can=true;
					break;
				}
			}
		}
		if(is_can){
			ApiUtils.json(response, "", "不能重复加入", 1);
			return;
		}
		MyTeamEntity myteam=new MyTeamEntity();
		myteam.setAddTime(new Date());
		myteam.setDeleteStatus(false);
		myteam.setUser(accept_user);
		this.commonService.save(myteam);
		myTeamList.add(myteam);
		user.setMyteam(myTeamList);
		boolean ret=this.userService.update(user);
		if(ret){
			ApiUtils.json(response, "", "添加成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:当前登陆用户的id
	 *@description:得到我的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "得到我的团队成员", value = "/get_myTeamMember.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/get_myTeamMember.htm", method = RequestMethod.POST)
	public void get_myTeamMember(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		List<MyTeamEntity> myTeamList=user.getMyteam();
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(MyTeamEntity.class, "user,id,addTime"));
		objs.add(new FilterObj(User.class, "id,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, myTeamList, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:id:MyTeamEntity的id
	 *@description:删除我的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "删除我的团队成员", value = "/delete_myteam_member.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/delete_myteam_member.htm", method = RequestMethod.POST)
	public void delete_myteam_member(HttpServletRequest request,
			HttpServletResponse response,String id){
		boolean ret=this.myTeamService.remove(Long.valueOf(id));
		String sql="DELETE FROM shopping_user_shopping_my_team_entity WHERE myteam_id="+id;
		int is_ok=this.commonService.executeNativeSQL(sql);
		if(ret&&is_ok>0){
			ApiUtils.json(response, "", "删除成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:recNum:电话号码,接受者的电话号码
	 *@description:给用户推送短信通知
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/get_safe_code.htm", method = RequestMethod.POST)
	public void get_safe_code(HttpServletRequest request,
			HttpServletResponse response,String recNum){
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, recNum,templateId);
		if(ret){
			System.out.println(safeCode);
			JSONObject retobj=new JSONObject();
			retobj.put("safeCode", safeCode);
			retobj.put("recNum", recNum);
			HttpSession session = request.getSession();
			session.setAttribute("safeCode", safeCode);
			session.setAttribute("safePhone", recNum);
			ApiUtils.json(response, "", "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:recNum:电话号码,接受者的电话号码
	 *@description:给用户推送短信通知
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/get_userInfo.htm", method = RequestMethod.POST)
	public void get_userInfo(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if(!"".equals(user_id)&&user_id!=null){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(user!=null){
//				String sql="select count(1) from shopping_user_clickapps as obj where user_id ="+user_id;
//				List<?> count = commonService.executeNativeNamedQuery(sql);
				String hql="select obj from AppClickNum as obj where obj.user.id = "+user_id;
				@SuppressWarnings("unchecked")
				List<AppClickNum> count = commonService.query(hql, null, -1, -1);
				ApiUtils.updateDepositStatus(Long.valueOf(user_id), integralDepositService, predepositLogService, userService,commonService);
				UserTempData userTempData=new UserTempData();
				userTempData.setId(user.getId());
				userTempData.setIs_finish_userName(user.getIs_finish_userName());
				userTempData.setZhixian(user.getZhixian());
				userTempData.setAvailableBalance(user.getAvailableBalance());
				userTempData.setAreaGradeOfUser(user.getAreaGradeOfUser());
				userTempData.setEmail(user.getEmail());
				userTempData.setUserName(user.getUsername());
				userTempData.setMobile(user.getMobile());
				userTempData.setPhoto(user.getPhoto());
				userTempData.setZhiwei(user.getZhiwei());
				userTempData.setBumen(user.getBumen());
				userTempData.setLoginCount(user.getAddSelfDanBaoRenToFriend());
				if (count.size()>0) {
					userTempData.setAppClickapps(count.get(0).getClickNum());
				}
				try {
					String userActiveState = ApiUtils.getUserActiveState(user, commonService);
//					Integer fenhongNum = ApiUtils.getFenhongNum(user, commonService);
//					Map<String, Integer> userAttribute = ApiUtils.getUserAttribute(user, commonService, userService);
//					userTempData.setAffinitys(userAttribute.get("affinitys"));
//					userTempData.setLeader(userAttribute.get("leader"));
//					userTempData.setInfluences(userAttribute.get("influences"));
//					userTempData.setFenhongNum(fenhongNum);
					userTempData.setUserActiveState(userActiveState);
				} catch (Exception e) {
					e.printStackTrace();
				}		
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(UserTempData.class, "id,is_finish_userName,zhixian,availableBalance,areaGradeOfUser,email,userName,mobile,photo,zhiwei,bumen,loginCount,appClickapps,fenhongNum,influences,leader,affinitys,userActiveState"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				objs.add(new FilterObj(ZhiWei.class, "id,name"));
				objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
				objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
				objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, userTempData, "查询成功", 0, filter);
			}else{
				ApiUtils.json(response, "", "没有该用户", 1);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:修改用户的信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/update_userInfo.htm", method = RequestMethod.POST)
	public void update_userInfo(HttpServletRequest request,
			HttpServletResponse response,String user_id,String mobile,
			String userName,String email){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(user_id)&&user_id!=null){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(user!=null){
				if(user.getIs_finish_userName()==1){
					if(!"".equals(userName)&&userName!=null){
						if(userName.equals(user.getUserName())){
							user.setUserName(userName);
						}else{
							String hql="select obj from User as obj where obj.userName='"+userName+"'";
							List<User> userList=new ArrayList<User>();
							userList=this.userService.query(hql, null, -1, -1); 
							if(userList.size()>0){
								ApiUtils.json(response, "", "该用户名已存在,请使用其他用户名", 1);
								return;
							}else{
								user.setIs_finish_userName(0);
								user.setUserName(userName);
							}
						}
					}
				}
				if(!"".equals(mobile)&&mobile!=null){
					if(mobile.equals(user.getMobile())){
						user.setMobile(mobile);
					}else{
						String hql="select obj from User as obj where obj.mobile='"+mobile+"'";
						List<User> userMobileList=new ArrayList<User>();
						userMobileList=this.userService.query(hql, null, -1, -1);
						if(userMobileList.size()>0){
							ApiUtils.json(response, "", "该手机号已被注册,请使用其他手机号", 1);
							return;
						}else{
							user.setMobile(mobile);
						}
					}
				}
				if(!"".equals(email)&&email!=null){
					if(email.equals(user.getEmail())){
						user.setEmail(email);
					}else{
						String hql="select obj from User as obj where obj.email='"+email+"'";
						List<User> userEmailList=new ArrayList<User>();
						userEmailList=this.userService.query(hql, null, -1, -1);
						if(userEmailList.size()>0){
							ApiUtils.json(response, "", "该邮箱已被注册,请使用其他邮箱", 1);
							return;
						}else{
							user.setEmail(email);;
						}
					}
				}
				boolean ret=this.userService.update(user);
				if(ret){
					ApiUtils.json(response, "", "修改成功", 0);
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:用户是否能够修改密码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/change_password.htm", method = RequestMethod.POST)
	public void change_password(HttpServletRequest request,
			HttpServletResponse response,String user_id,String password,
			String newPwd){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(user_id)&&user_id!=null){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(!"".equals(password)&&password!=null){
				String lowCasePwd=Md5Encrypt.md5(password).toLowerCase();
				if(lowCasePwd.equals(user.getPassword())){
					user.setPassword(Md5Encrypt.md5(newPwd).toLowerCase());
					boolean ret=this.userService.update(user);
					if(ret){
						boolean haveHuanxin = ApiUtils.isHaveHuanxin(user.getId());
						if (haveHuanxin) {
							CommUtil.update_user_password(user.getPassword().toString(), user.getId().toString());//将用户的密码同步至环信服务器
						}
						ApiUtils.json(response, user.getPassword().toString(), "修改成功", 0);
					}
				}else{
					ApiUtils.json(response, "", "原密码错误", 1);
					return;
				}
			}else{
				ApiUtils.json(response, "", "请上传原密码", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取该用户的收支明细
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_predepositLog.htm", method = RequestMethod.POST)
	public void app_acquire_predepositLog(HttpServletRequest request,
			HttpServletResponse response,String userId,String currentPage){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误,用户不存在！", 1);
			return;
		}
		int current_page=0;
		int pageSize=20;
		String hql="";
		if("".equals(currentPage)||currentPage==null){ 
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		hql="select obj from PredepositLog as obj where obj.pd_log_user.id="+userId+"order by obj.addTime desc";
		List<PredepositLog> predList=this.predepositLogService.query(hql, null,current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(PredepositLog.class, "id,addTime,order_id,pd_log_user,pd_log_amount,pd_type,pd_op_type,pd_log_info,current_price"));
		objs.add(new FilterObj(User.class, "id,userName,availableBalance"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, predList, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:userId 用户id
	 *@description:通过用户id,获取用户积分
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_integralBalance.htm", method = RequestMethod.POST)
	public void app_integralBalance(HttpServletRequest request,
			HttpServletResponse response, Long userId) {
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		String hql="select obj.availableBalance from User as obj where obj.id = "+userId;
		List<?> query = this.commonService.query(hql, null, -1, -1);
		BigDecimal integral=new BigDecimal(0);
		if (query.size()>0) {
			integral=(BigDecimal) query.get(0);
		}
		//predepositLogService.delete(2l);
		ApiUtils.json(response, integral, "积分查询成功",0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:userId 用户id,acceptUserId受赠积分用户id,count 转赠积分数,password 用户密码
	 *@description:积分转赠，#1用户积分不为0可以使用转赠功能，#2普通会员用户，
	 *					#3卖家用户，转赠时判断用户的积分是否大于1000，#4用户积分为0，
	 *					不能赠送积分，#5收赠方用户不存在
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_handselIntegral.htm", method = RequestMethod.POST)
	public void handselIntegral(HttpServletRequest request,
			HttpServletResponse response, Long userId,Long acceptUserId,Double count,String password,String fontContent) {
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user = userService.getObjById(userId);
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (lowerCase.equals(psw)) {
			if (count<=0) {
				ApiUtils.json(response, "","转赠金额必须大于0",1);
				return;
			}else {
				if (!userId.equals(acceptUserId)) {
					User acceptUser = userService.getObjById(acceptUserId);
					if (acceptUser!=null) {
						BigDecimal availableBalance = user.getAvailableBalance();
						Double integral = availableBalance.doubleValue();
						if (integral>0) {//#1		
							Integer freezeBlance = user.getFreezeBlance().intValue();
							if (freezeBlance==0) {
								Double over = integral-count;
								if (over>0) {//#2
									updateUserIntegral(acceptUser, user, count,response,fontContent);
									return;
								}else {
									ApiUtils.json(response, "","用户积分不足,转赠失败！",1);
									return;
								}
							}else{//#3
								if (integral-count>1000) {
									updateUserIntegral(acceptUser, user, count,response,fontContent);
									return;
								}else{
									ApiUtils.json(response, "","您的余额里面有部分被锁定为诚信保证金,导致可用积分不足,转赠失败！",1);
									return;
								}					
							}
						}else{//#4				
							ApiUtils.json(response, "","用户积分不足,转赠失败！",1);
							return;
						}
					}else{//#5
						ApiUtils.json(response, "","该用户不存在,请检查用户id是否正确",1);
						return;
					}
				}else {
					ApiUtils.json(response, "","不能给自己转赠！",1);
					return;
				}	
			}
		}else {
			ApiUtils.json(response, "","账户密码不匹配！",1);
			return;
		}
	}
	private void updateUserIntegral(User acceptUser,User user,Double count,HttpServletResponse response,String fontContent){
		acceptUser.setAvailableBalance(acceptUser.getAvailableBalance().add(BigDecimal.valueOf(count)));
		user.setAvailableBalance(user.getAvailableBalance().subtract(BigDecimal.valueOf(count)));
		User[] users={user,acceptUser};
		boolean isSuccess = userService.updateBatchEntity(users);
		if (isSuccess) {
			PredepositLog predepositLog=new PredepositLog();
			predepositLog.setAddTime(new Date());
			predepositLog.setDeleteStatus(false);
			predepositLog.setPd_log_user(user);
			predepositLog.setCurrent_price(user.getAvailableBalance().doubleValue());
			predepositLog.setPd_log_amount(BigDecimal.valueOf(-count));
			predepositLog.setPd_op_type("减少");
			predepositLog.setPd_type("积分转赠");
			predepositLog.setPd_log_info("转赠积分给"+acceptUser.getUserName());
			predepositLog.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
			PredepositLog predepositLog2=new PredepositLog();
			predepositLog2.setAddTime(new Date());
			predepositLog2.setDeleteStatus(false);
			predepositLog2.setPd_type("积分转赠");
			predepositLog2.setPd_log_user(acceptUser);
			predepositLog2.setCurrent_price(acceptUser.getAvailableBalance().doubleValue());
			predepositLog2.setPd_log_amount(BigDecimal.valueOf(count));
			predepositLog2.setPd_log_info("收到"+user.getUserName()+"积分转赠");
			if (!"".equals(CommUtil.null2String(fontContent))) {
				if (fontContent.length()>10) {
					fontContent=fontContent.substring(0, 10);
				}
				predepositLog2.setPd_log_info("收到"+user.getUserName()+"积分转赠," + fontContent);
			}
			predepositLog2.setPd_op_type("增加");
			predepositLog2.setOrder_id(ApiUtils.integralOrderNum(acceptUser.getId()));
			PredepositLog[] pls={predepositLog,predepositLog2};
			boolean is=predepositLogService.saveBatchEntity(pls);
			if (is) {
				String msg=acceptUser.getUserName()+"战友，你好，"+user.getUserName()+"给您转赠"+count+"积分，请在用户个人资料里面查看积分余额";
				CommUtil.send_messageToSpecifiedUser(acceptUser,msg,userService);
				ApiUtils.updateUserRenk(0, acceptUser, commonService, userService);//更新会员等级
				ApiUtils.json(response, "","转赠成功！",0);
				return;
			}		
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:积分充值，获取app积分的数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_integralsList.htm", method = RequestMethod.POST)
	public void app_acquire_IntegralsList(HttpServletRequest request,
			HttpServletResponse response){
		String hql="select obj from IntegralRechargeListEntity as obj order by obj.current_price";
		List<?> list = commonService.query(hql, null, -1, -1);
		if (list.size()>0) {
			ApiUtils.json(response, list, "获取列表成功", 0);
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:支付宝支付购买积分
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_aliPayment_integral.htm", method = RequestMethod.POST)
	public void appAliPaymentIntegral(HttpServletRequest request,
			HttpServletResponse response,Long userId,
			Double count){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		if (user==null) {
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}else{
			Long runningWaterNum=ApiUtils.integralOrderNum(userId);
			IntegralRechargeEntity integralEntity=new IntegralRechargeEntity();
			integralEntity.setAddTime(new Date());
			integralEntity.setDeleteStatus(false);
			integralEntity.setOrderStatus(10);
			integralEntity.setUser(user);
			integralEntity.setRechargeQuantity(count);
			integralEntity.setRunningWaterNum(runningWaterNum);
			integralEntity.setRechargeWay("alipay");
			integralEntity.setRechargeExplain(user.getUserName());
			boolean is = integralRechargeService.save(integralEntity);
			if (is) {
				String alipayUrl=CommUtil.getURL(request)+"/app_alipayIntegralRecharge_callBack.htm";
				String str=ApiUtils.getAlipayStr(integralEntity.getRunningWaterNum().toString(), alipayUrl, count.toString());
				ApiUtils.json(response, str,"获取支付信息成功",0);
			}
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:微信支付购买积分
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_weixinPayment_integral.htm", method = RequestMethod.POST)
	public void weixinAliPaymentIntegral(HttpServletRequest request,
			HttpServletResponse response,Long userId,
			Double count){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user = userService.getObjById(userId);
		if (user==null) {
			ApiUtils.json(response, "","参数错误，该用户不存在",1);
		}else {
			Long runningWaterNum=ApiUtils.integralOrderNum(userId);
			IntegralRechargeEntity integralEntity=new IntegralRechargeEntity();
			integralEntity.setAddTime(new Date());
			integralEntity.setDeleteStatus(false);
			integralEntity.setOrderStatus(10);
			integralEntity.setUser(user);
			integralEntity.setRechargeQuantity(count);
			integralEntity.setRunningWaterNum(runningWaterNum);
			integralEntity.setRechargeWay("weixin");
			integralEntity.setRechargeExplain(user.getUserName());
			boolean is = integralRechargeService.save(integralEntity);
			if (is) {
				String weixinpayUrl=CommUtil.getURL(request)+"/app_weiXinIntegralRechargeCallBack.htm";
				Map<String, String> params = null;
				try {
					params=ApiUtils.get_weixin_sign_string(integralEntity.getRunningWaterNum().toString(),weixinpayUrl,count+"");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ApiUtils.json(response, params, "获取支付信息成功", 0);
				return;
			}
			
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:userId 用户id,beginTime 账单开始时间 ,endTime 账单结束时间 ,currentPage 当前页
	 *@description:账单查询
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_getPredepositLogs.htm", method = RequestMethod.POST)
	public void app_getPredepositLogs(HttpServletRequest request,HttpServletResponse response,String userId,String beginTime,String endTime,String currentPage){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		if (userId!=null) {
			SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
			boolean is=false;
			if (beginTime!=null||endTime!=null) {
				if (beginTime==null) {
					String newTime=sft.format(new Date());
					long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
					if (days<0) {
						endTime=newTime;
					}
					try {
						beginTime=ApiUtils.getFirstday_Lastday(sft.parse(endTime), 0, 7);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else if (endTime==null) {
					try {
						endTime=ApiUtils.getFirstday_Lastday(sft.parse(beginTime), 1, 7);
						String newTime=sft.format(new Date());
						long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
						if (days<0) {
							endTime=newTime;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else {
					String newTime=sft.format(new Date());
					long days = ApiUtils.acquisitionTimeSegment(endTime, newTime);
					if (days<0) {
						endTime=newTime;
					}
				}
			}else {
				is=true;
			}
			String begin_Time="";
			String end_Time="";
			long days = 0l;
			if (!is) {
				begin_Time=ApiUtils.weeHours(beginTime, 0);
				end_Time=ApiUtils.weeHours(endTime, 1);
				days = ApiUtils.acquisitionTimeSegment(beginTime, endTime);
			}else {
				days=0;
			}
			if (days<=60) {	
				String hql="";
				if (is) {
					hql="select obj from PredepositLog as obj where obj.pd_log_user.id="+userId+"order by obj.addTime desc";
				}else {
					hql="select obj from PredepositLog as obj where obj.pd_log_user.id="+userId+" and addTime >'"+begin_Time+"' and addTime <'"+end_Time+"' order by obj.addTime desc";
				}		
				int pageSize=20;
				int current_page=0;
				if (currentPage==null) {
					current_page=0;
				}else{
					current_page=Integer.valueOf(currentPage).intValue();
				}
				List<PredepositLog> list = predepositLogService.query(hql, null,current_page*pageSize ,pageSize );
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(AppBillsDataTemp.class, "beginTime,endTime,predepositLogList"));
				objs.add(new FilterObj(PredepositLog.class, "id,addTime,order_id,pd_log_user,pd_log_amount,pd_type,pd_op_type,pd_log_info,current_price"));
				objs.add(new FilterObj(User.class, "id,userName,availableBalance"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				/*result.add(beginTime);
				result.add(endTime);
				result.add(list);*/
				AppBillsDataTemp appBillsDataTemp=new AppBillsDataTemp();
				appBillsDataTemp.setBeginTime(beginTime);
				appBillsDataTemp.setEndTime(endTime);
				appBillsDataTemp.setPredepositLogList(list);
				ApiUtils.json(response, appBillsDataTemp, "查询成功。", 0,filter);
				return;
			}else {
				ApiUtils.json(response, "", "查询时间不能大于60天", 1);
				return;
			}
		}else {
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取用户个人格言
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserMotto.htm", method = RequestMethod.POST)
	public void appGetUserMotto(HttpServletRequest request,HttpServletResponse response,Long userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user = userService.getObjById(userId);
		if (user!=null) {
			String motto = user.getMotto();
			if (motto==null) {
				motto="品格就是财富，分享就能挣钱...";
			}
			ApiUtils.json(response, motto, "获取用户格言成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:更改用户个人格言
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUpdateUserMotto.htm", method = RequestMethod.POST)
	public void appUpdateUserMotto(HttpServletRequest request,HttpServletResponse response,Long userId,String password,String motto){
		boolean is_null = ApiUtils.is_null(password,motto);
		if("".equals(CommUtil.null2String(userId))||is_null){
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		if (motto.length()>15) {
			ApiUtils.json(response, "", "格言字数不能超过15字", 1);
			return;
		}
		User user = userService.getObjById(userId);
		if (user!=null) {
			if (!user.getPassword().equals(password)) {
				ApiUtils.json(response, "", "密码错误", 1);
				return;
			}
			user.setMotto(motto);
			boolean is = userService.update(user);
			if (is) {
				ApiUtils.json(response, "", "修改用户格言成功", 0);
				return;
			}else {
				ApiUtils.json(response, "", "系统繁忙", 1);
				return;
			}		
		}else {
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:获取格言例句
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMotto.htm")
	public void appGetMotto(HttpServletRequest request,HttpServletResponse response){
		String hql="from Motto as obj order by id desc";
		List<?> list = commonService.query(hql, null, 0, 10);
		if (list.size()>0) {
			ApiUtils.json(response, list, "获取格言例句成功", 0);
			return;
		}
	}


	private void invitationInvestiture(Long userId){
		User user = userService.getObjById(userId);
		if (user!=null) {
			if (user.getId()==1) {
//				ApiUtils.json(response, "", "您已是最高等级", 1);
				return;
			}
			List<User> users=new ArrayList<User>();
			boolean is=true;
			int num=0;
			int dynamicNum=1;
			Date date = new Date();
			String time = ApiUtils.getFirstday_Lastday(date, 0, 7);
			String sql="";
			do {
				String hql="from User AS obj where obj.dan_bao_ren = '" + user.getUserName()+"' order by id desc";
				users = userService.query(hql, null, num*100, 100);
				num++;
				for (User u : users) {
					sql="select obj.id from shopping_user_clickapps as obj where obj.addTime >= '"+ ApiUtils.weeHours(time, 0) +"' and user_id ="+u.getId().toString() + " limit 0,1";
					List<?> count = commonService.executeNativeNamedQuery(sql);
					if (count.size()>0) {
						dynamicNum++;
					}
					if (dynamicNum>=50) {
						is=false;
						break;
					}
				}
			} while (!(users.size()<100));
			String junxianMsg="";
			String zhijiMsg="";
			String demotionJunxianMsg="";
			String demotionZhijiMsg="";
			if (dynamicNum>0) {
				sql="select obj from InvitationRank as obj where obj.rankType=2 and obj.dynamicNum <= " +dynamicNum +" order by obj.dynamicNum DESC";
				List<InvitationRank> list = commonService.query(sql, null, 0, 1);
				if (list.size()>0) {
					if (user.getJunxian()==null) {
						user.setJunxian(list.get(0));
						boolean update = userService.update(user);
						if (update) {
							junxianMsg=user.getUserName()+"战友，你好，"+"恭喜你的动态战友人数增加到"+user.getJunxian().getDynamicNum()+"人，你的军衔升为"+user.getJunxian().getInvitationRankName()+"，加油！";
						}
					}else {
						if (list.get(0).getDynamicNum()>user.getJunxian().getDynamicNum()) {
							user.setJunxian(list.get(0));//升职
							boolean update = userService.update(user);
							if (update) {
								junxianMsg=user.getUserName()+"战友，你好，"+"恭喜你的动态战友人数增加到"+user.getJunxian().getDynamicNum()+"人，你的军衔升为"+user.getJunxian().getInvitationRankName()+"，加油！";
							}
						}else if (list.get(0).getDynamicNum()<user.getJunxian().getDynamicNum()){
							user.setJunxian(list.get(0));//降
							boolean update = userService.update(user);
							if (update) {
								demotionJunxianMsg=user.getUserName()+"战友，你好，"+"你的动态战友人数减少到"+user.getJunxian().getDynamicNum()+"人，军衔被降为"+user.getJunxian().getInvitationRankName()+"，要努力哦。";
							}
						}				
					}
				}
			}		
			if (dynamicNum>=3) {
				sql="select obj from InvitationRank as obj where obj.rankType=1 and obj.dynamicNum <= " +dynamicNum +" order by obj.dynamicNum DESC";
				List<InvitationRank> list = commonService.query(sql, null, 0, 1);
				if (user.getZhiji()==null) {
					user.setZhiji(list.get(0));//升职
					boolean update = userService.update(user);
					if (update) {
						zhijiMsg=user.getUserName()+"战友，你好，"+"恭喜你的动态战友人数增加到"+user.getZhiji().getDynamicNum()+"人，职级升为"+user.getZhiji().getInvitationRankName()+"，加油！";
					}
				}else {
					if (list.get(0).getDynamicNum()>user.getZhiji().getDynamicNum()) {
						user.setZhiji(list.get(0));//升职
						boolean update = userService.update(user);
						if (update) {
							zhijiMsg=user.getUserName()+"战友，你好，"+"恭喜你的动态战友人数增加到"+user.getZhiji().getDynamicNum()+"人，职级升为"+user.getZhiji().getInvitationRankName()+"，加油！";
						}
					}else if (list.get(0).getDynamicNum()<user.getZhiji().getDynamicNum()){
						user.setZhiji(list.get(0));//降
						boolean update = userService.update(user);
						if (update) {
							demotionZhijiMsg=user.getUserName()+"战友，你好，"+"你的动态战友人数减少到"+user.getZhiji().getDynamicNum()+"人，职级被降为"+user.getZhiji().getInvitationRankName()+"，要努力哦。";
						}	
					}
				}
			}
			int zhijiNum = 0;
			int junxianNum = 0;
			//升职消息
			if (!zhijiMsg.equals("")&&!junxianMsg.equals("")) {
				zhijiNum = user.getZhiji().getDynamicNum();
				junxianNum = user.getJunxian().getDynamicNum();
				if (zhijiNum>junxianNum) {				
					this.send_message(user,junxianMsg);
					this.send_message(user,zhijiMsg);
				}else {				
					this.send_message(user,zhijiMsg);
					this.send_message(user,junxianMsg);
				}			
			}else if (zhijiMsg.equals("")&&!junxianMsg.equals("")) {
				this.send_message(user,junxianMsg);
			}else if (!zhijiMsg.equals("")&&junxianMsg.equals("")) {
				this.send_message(user,zhijiMsg);
			}
			//降职消息
			if (!demotionZhijiMsg.equals("")&&!demotionJunxianMsg.equals("")) {
				zhijiNum = user.getZhiji().getDynamicNum();
				junxianNum = user.getJunxian().getDynamicNum();
				if (zhijiNum<junxianNum) {				
					this.send_message(user,demotionJunxianMsg);
					this.send_message(user,demotionZhijiMsg);
				}else {				
					this.send_message(user,demotionZhijiMsg);
					this.send_message(user,demotionJunxianMsg);
				}			
			}else if (demotionZhijiMsg.equals("")&&!demotionJunxianMsg.equals("")) {
				this.send_message(user,demotionJunxianMsg);
			}else if (!demotionZhijiMsg.equals("")&&demotionJunxianMsg.equals("")) {
				this.send_message(user,demotionZhijiMsg);
			}
			if (dynamicNum<=2) {
				if (user.getZhiji()!=null) {
					user.setZhiji(null);
					boolean update = userService.update(user);
					if (update) {
						zhijiMsg=user.getUserName()+"战友，你好，"+"你的动态战友人数减少到"+dynamicNum+"人，职级被取消，要努力哦。";
						this.send_message(user,zhijiMsg);
					}	
				}
			}
		}else {
//			ApiUtils.json(response, "", "该用户不存在", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:userId:用户的id currentPage:当前页  rankingType:排序类型（1.recentlyLogin最近登陆;2.openingTime开通时间;3.loginNum登陆次数;4.activity活跃度）
	 *@description:APP获取我的战友
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMyComrades.htm", method = RequestMethod.POST)
	public void appGetMyComrades(HttpServletRequest request,
			HttpServletResponse response,String userId,
			String currentPage,String rankingType,String password,String adminUserId){
		if (CommUtil.null2String(rankingType).equals("")) {
			ApiUtils.json(response, "", "排序参数缺失", 1);
			return;
		}
		boolean is_admin=false;
		if (!ApiUtils.is_null(adminUserId,password)) {
			is_admin = ApiUtils.isAdmin(adminUserId, password, userService);
		}
		User user = ApiUtils.erifyUser(userId, password, userService);
		Long user_id = CommUtil.null2Long(userId);
		if (is_admin) {
			user=userService.getObjById(user_id);
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		if (user!=null) {
			String hql="";
			AppTransferData guaranteeUser=new AppTransferData();
			guaranteeUser.setFifthData(user.getId());
			guaranteeUser.setSecondData(user.getUsername());
			List<UserTemp> out_put=new ArrayList<UserTemp>();
			if (rankingType.equals("recentlyLogin")) {//#1
				hql="select obj from User as obj where obj.dan_bao_ren='"+user.getUsername()+"' order by obj.loginDate DESC";
				List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
				out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);
			}else if (rankingType.equals("openingTime")) {//#2
				hql="select obj from User as obj where obj.dan_bao_ren='"+user.getUsername()+"' order by obj.addTime DESC";
				List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
				out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);
			}else if (rankingType.equals("loginNum")) {//#3
				hql="select obj from User as obj where obj.dan_bao_ren='"+user.getUsername()+"' order by obj.loginCount DESC";
				List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
				out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);
			}else if (rankingType.equals("activity")) {//#4
				String sql="SELECT "+
						   "COUNT(suc.id) AS clicks,su.id "+
						   "FROM shopping_user AS su "+
						   "LEFT JOIN shopping_user_clickapps AS suc "+
						      "ON su.id = suc.user_id "+
						   "WHERE su.id IN(SELECT "+
						                   "su.id "+
						                 "FROM shopping_user AS su "+
						                 "WHERE su.dan_bao_ren = '"+user.getUserName().toString()+"') "+
						   "GROUP BY su.id "+
						   "ORDER BY clicks DESC "+
						   "LIMIT "+current_page*pageSize+","+pageSize;
				List<?> user_ids=this.commonService.executeNativeNamedQuery(sql);
				if(user_ids.size()>0){
					for(Object obj:user_ids){
						Object[] ids=(Object[]) obj;
						User dan_bao_user=this.userService.getObjById(CommUtil.null2Long(ids[1].toString()));
						UserTemp usertemp=new UserTemp();
						usertemp.setUser(dan_bao_user);
						usertemp.setLiveness(ids[0].toString());
						usertemp.setGuaranteeUser(guaranteeUser);
						out_put.add(usertemp);
					}
				}
			}else {
				ApiUtils.json(response, "", "排序参数错误", 1);
				return;
			}
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
			objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
			objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
			objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
			objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
			objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, out_put, "查询我的战友成功", 0, filter);
		}else {
			ApiUtils.json(response, "", "该用户不存在或密码错误", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_message:查询信息,currentPage:当前页
	 *@description:搜索用户的团队成员,如果为管理员，则可以搜索全站人员，否则只搜索由登陆用户为担保人的人员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "搜索用户的团队成员", value = "/search_team_member.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/appSearchMember.htm", method = RequestMethod.POST)
	public void appSearchMember(HttpServletRequest request,
			HttpServletResponse response,String userMessage,String currentPage,String userId,String password){
		if (CommUtil.null2String(userMessage).equals("")||CommUtil.null2String(password).equals("")) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String type = ApiUtils.judgmentType(userMessage.trim());
		if (type.equals("")) {
			ApiUtils.json(response, "", "参数错误,没有该类型用户", 1);
			return;
		}
		User user = userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		if (!user.getPassword().equals(password)) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		String term=" and obj.dan_bao_ren = '" + user.getUserName()+"'";
		if (user.getId()==1||user.getId()==20717) {
			term=" ";
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="";
		if (type.equals("mobile")) {
			hql="select obj from User as obj where obj.mobile="+userMessage + term +" order by obj.addTime DESC";
		}else if (type.equals("id")) {
			hql="select obj from User as obj where obj.id="+userMessage + term +" order by obj.addTime DESC";
		}else if (type.equals("userName")) {
			hql="select obj from User as obj where obj.userName like '%"+userMessage+"%'" + term +" order by obj.addTime DESC";
		}
		List<User> out_put=this.userService.query(hql, null, current_page*pageSize, pageSize);
		List<UserTemp> userTemps = ApiUtils.getAppUserTemps(out_put, null, commonService, userService);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, userTemps, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_id:当前登陆用户的id
	 *@description:得到我的团队成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMyTeamMember.htm", method = RequestMethod.POST)
	public void appGetMyTeamMember(HttpServletRequest request,
			HttpServletResponse response,String userId,String password){
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在或密码错误", 1);
			return;
		}
		List<MyTeamEntity> myTeamList=user.getMyteam();
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		if (myTeamList!=null&&myTeamList.size()>0) {
			List<User> users=new ArrayList<User>();
			for (MyTeamEntity myTeamEntity : myTeamList) {
				User user2 = myTeamEntity.getUser();
				user2.setTeamId(myTeamEntity.getId());
				users.add(user2);
			}
			out_put = ApiUtils.getAppUserTemps(users, null, commonService, userService);
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,teamId,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_id:当前登陆用户的id
	 *@description:得到我的民兵,以我为担保人，没有下载过app的人
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMyMilitia.htm", method = RequestMethod.POST)
	public void appGetMyMilitia(HttpServletRequest request,
			HttpServletResponse response,String userId,String currentPage){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user=userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
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
		AppTransferData guaranteeUser=new AppTransferData();
		guaranteeUser.setFifthData(user.getId());
		guaranteeUser.setSecondData(user.getUsername());
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		hql="select obj from AppClickNum as acn right join acn.user as obj where obj.dan_bao_ren='"+user.getUsername()+"' and acn.clickNum is null order by obj.loginDate DESC";
		List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
		out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);		
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询我的民兵成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_id:当前登陆用户的id
	 *@description:得到我的士兵,以我为担保人，没有担任职位
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMySoldiers.htm", method = RequestMethod.POST)
	public void appGetMySoldiers(HttpServletRequest request,
			HttpServletResponse response,String userId,String currentPage){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user=userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
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
		AppTransferData guaranteeUser=new AppTransferData();
		guaranteeUser.setFifthData(user.getId());
		guaranteeUser.setSecondData(user.getUsername());
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		hql="select obj from User as obj where obj.dan_bao_ren='"+user.getUsername()+"' and (obj.zhiwei is null or obj.zhiwei.id=0) order by obj.loginDate DESC";
		List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
		out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询我的士兵成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_id:当前登陆用户的id
	 *@description:得到我的军官,以我为担保人，下载过app并且已经担任职位的人
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetMyOfficer.htm", method = RequestMethod.POST)
	public void appGetMyOfficer(HttpServletRequest request,
			HttpServletResponse response,String userId,String currentPage){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user=userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
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
		AppTransferData guaranteeUser=new AppTransferData();
		guaranteeUser.setFifthData(user.getId());
		guaranteeUser.setSecondData(user.getUsername());
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		hql="select obj from AppClickNum as acn right join acn.user as obj where obj.dan_bao_ren='"+user.getUsername()+"' and acn.clickNum is not null and (obj.zhiwei is not null and obj.zhiwei.id <> 0) order by obj.loginDate DESC";
		List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
		out_put=ApiUtils.getAppUserTemps(users, guaranteeUser, commonService, userService);		
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询我的军官成功", 0, filter);
	}
	 /***
	 *@author:gaohao
	 *@return:void
	 *@param:cash_amount 提现金额；password  明文密码
	 *@description:app用户提现
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUserTakeCash.htm", method = RequestMethod.POST)
	public void appUserTakeCash(HttpServletRequest request,HttpServletResponse response,String cashAmount,String userId,String password,String phoneType) {
		boolean is_null = ApiUtils.is_null(cashAmount,phoneType,password);
		if (is_null) {
			ApiUtils.json(response, "", "参数不能为空！", 1);
			return;
		}
		double cash_amount = CommUtil.null2Double(cashAmount);
		if (cash_amount<=0) {
			ApiUtils.json(response, "", "提现金额错误！", 1);
			return;
		}
		SysConfig sc = this.configService.getSysConfig();
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		if (!CommUtil.isNotNull(user.getOpen_account_name())|| !CommUtil.isNotNull(user.getBank_card_number())|| !CommUtil.isNotNull(user.getBank_type())) {
			ApiUtils.json(response, "", "请先在个人中心完善银行信息，提现失败！", 1);
			return;
		}
		double cash = AllocateWagesUtils.getCashAmount(user);
		if (cash < cash_amount) {
			ApiUtils.json(response, "", "超过可提现的最大额度！", 1);
			return;
		}
		PredepositCash obj = new PredepositCash();
		obj.setCash_sn("appCash"+ CommUtil.formatTime("yyyyMMddHHmmss", new Date())+ user.getId());
		obj.setAddTime(new Date());
		obj.setCash_user(user);
		obj.setCash_payment("chinabank");
		obj.setDeleteStatus(false);
		obj.setCash_account(user.getBank_card_number());
		obj.setCash_bank(user.getBank_type());
		obj.setCash_info(phoneType+"APP提现");
		obj.setCash_userName(user.getUserName());
		// cash_amount表示这笔提现的总额
		cash_amount=CommUtil.null2Double(String.format("%.2f", cash_amount-0.005));
		double cash_fee = 0;
		if (sc.getIs_open_lixifafang() == 1) {
			cash_fee = BigDecimalUtil.mul(cash_amount, 0.006).doubleValue();
			//手续费向上取2位小数
			BigDecimal b = new BigDecimal(String.valueOf(cash_fee));
			b = b.divide(BigDecimal.ONE,2,BigDecimal.ROUND_CEILING);
			cash_fee=b.doubleValue();			
		}
		//实际到账金额，手续费从提现的金额里面扣除
		obj.setCash_amount(BigDecimalUtil.sub(cash_amount, cash_fee));
		obj.setCash_commission(cash_fee);//手续费
		//银行报文
		obj.setRequest_xml(CommUtil.getBankXML(user.getBank_type(),
				obj.getCash_amount() + "", user.getBank_card_number(),
				user.getOpen_account_name()));
		boolean is = AllocateWagesUtils.allocateMoneyToUser(user.getId()+"", -cash_amount, "APP申请提现", "", predepositLogService, userService, commonService, 0);
		if (is) {
			this.predepositCashService.save(obj);
			ApiUtils.json(response, "", "提现申请成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "提现申请失败", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app获取银行列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetBankTypeList.htm", method = RequestMethod.POST)
	public void appGetBankTypeList(HttpServletRequest request,HttpServletResponse response,String currentPage) {
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from BankType as obj";
		List<BankType> banks = commonService.query(hql, null, current_page*pageSize, pageSize);
		ApiUtils.json(response, banks, "获取银行列表成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app绑定银行卡
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBindingBankCard.htm", method = RequestMethod.POST)
	public void appBindingBankCard(HttpServletRequest request,HttpServletResponse response,String bankType,String userId,String password,String bankCardNumber,String openAccountName) {
		boolean is_null = ApiUtils.is_null(bankType,bankCardNumber,password,openAccountName);
		if (is_null) {
			ApiUtils.json(response, "", "参数不能为空！", 1);
			return;
		}
		User user = ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误或用户不存在！", 1);
			return;
		}
		user.setBank_type(bankType);
		user.setBank_card_number(bankCardNumber);
		user.setOpen_account_name(openAccountName);
		boolean is = userService.update(user);
		if (is) {
			ApiUtils.json(response, "", "绑定银行卡成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "绑定失败，请稍后重试！", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:app获取用户可提现金额
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SuppressWarnings({ "unchecked"})
	@RequestMapping(value = "/appGetUserBalance.htm", method = RequestMethod.POST)
	public void appGetUserBalance(HttpServletRequest request,HttpServletResponse response,String userId,String password) {
		User user = ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误或用户不存在！", 1);
			return;
		}
		double cash_amount = AllocateWagesUtils.getCashAmount(user);
		AppTransferData data=new AppTransferData();
		if (CommUtil.isNotNull(user.getOpen_account_name())&&CommUtil.isNotNull(user.getBank_card_number())&&CommUtil.isNotNull(user.getBank_type())) {
			String hql="select obj from BankType as obj where obj.banktype = " + user.getBank_type();
			List<BankType> banks = commonService.query(hql, null, -1, -1);
			if (banks.size()>0) {
				data.setThirdData(banks.get(0).getBankname());
			}		
		}
		if (CommUtil.null2Double(user.getFreezeBlance()) == 1 && user.getStore().getStore_status() != 3) {
			data.setFourthData("lock");
		}
		data.setFirstData(cash_amount);
		data.setSecondData(user);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,bank_card_number,open_account_name"));
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData,thirdData,fourthData"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response,data,"查询银行卡信息成功", 0,filter);
		return;
	}
//	private double getCashAmount(User user){
//		Store store = user.getStore();
//		store = store == null ? new Store() : store;
//		SysConfig sc = this.configService.getSysConfig();
//		double cash_amount=0;//最多提现金额
//		double availableBalance = CommUtil.null2Double(user.getAvailableBalance());
//		if (sc.getIs_open_lixifafang() == 1) {
//			if (CommUtil.null2Double(user.getFreezeBlance()) == 1 && store.getStore_status() != 3) {//卖家
//				availableBalance = BigDecimalUtil.sub(availableBalance, 1000d).doubleValue();
//				if (availableBalance <= 0) {
//					cash_amount = 0;
//				}else {
//					cash_amount = BigDecimalUtil.mul(availableBalance, 0.994).doubleValue();
//				}
//			}else {
//				cash_amount =  BigDecimalUtil.mul(availableBalance, 0.994).doubleValue();
//			}
//		} else {
//			if (CommUtil.null2Double(user.getFreezeBlance()) == 1 && store.getStore_status() != 3) {
//				cash_amount = BigDecimalUtil.sub(availableBalance, 1000d).doubleValue();
//			}else {
//				cash_amount = availableBalance;
//			}
//		}
//		BigDecimal bd = new BigDecimal(cash_amount+"");
//		BigDecimal setScale = bd.setScale(2, BigDecimal.ROUND_DOWN);
//		cash_amount=setScale.doubleValue();
//		return cash_amount;
//	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**password  明文密码
	 *@description:app解锁担保金
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appThawUserGuaranty.htm", method = RequestMethod.POST)
	public void appThawUserGuaranty(HttpServletRequest request,HttpServletResponse response,String userId,String password) {
		boolean is_null = ApiUtils.is_null(userId,password);
		if (is_null) {
			ApiUtils.json(response, "", "参数不能为空！", 1);
			return;
		}
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误，用户不存在！", 1);
			return;
		}
		User user = this.userService.getObjById(user_id);
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		Store store = user.getStore();
		store = store == null ? new Store() : store;
		if (store.getId()==null) {
			ApiUtils.json(response, "", "店铺不存在！", 1);
			return;
		}
		if (store.getStore_status() != 3) {
			store.setStore_status(3);
			boolean is = this.storeService.update(store);
			if (is) {
				String sql="UPDATE shopping_goods AS obj SET obj.goods_status = 1 WHERE obj.goods_store_id = " +store.getId();
				commonService.executeNativeSQL(sql);
				user.setFreezeBlance(BigDecimal.valueOf(0));
				boolean isThaw = userService.update(user);
				if (isThaw) {
					ApiUtils.json(response, "", "已解锁保证金，并关闭店铺。", 0);
					return;
				}
			}
		}
		ApiUtils.json(response, "", "操作失败！", 1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password  密文密码
	 *@description:app查看个人收入清单
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUserIncomeBill.htm", method = RequestMethod.POST)
	public void appUserIncomeBill(HttpServletRequest request,HttpServletResponse response,String userId,String password,String beginTime) {
		User user = ApiUtils.erifyUser(userId, password, userService);
		if (user==null||CommUtil.null2String(beginTime).equals("")) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM");
		try {
			Date parse = df.parse(beginTime);
			beginTime = CommUtil.formatLongDate(parse);
		} catch (ParseException e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "时间参数错误", 1);
			return;
		}
		long timeSegment = ApiUtils.acquisitionTimeSegment("2018-01-01", beginTime);
		if (timeSegment<0) {
			ApiUtils.json(response, "", "只能查询2018年之后的账单数据", 1);
			return;
		}
		String Lastday_Month = ApiUtils.getFirstday_Lastday_Month(CommUtil.formatShortDate(new Date()), 1);
		long timeInterval = ApiUtils.acquisitionTimeSegment(Lastday_Month, beginTime);
		if (timeInterval>0) {
			ApiUtils.json(response, "", "只能查询本月及本月之前的数据", 1);
			return;
		}
		String begin =ApiUtils.getFirstday_Lastday_Month(beginTime, 0);
		String end = ApiUtils.getFirstday_Lastday_Month(beginTime, 1);
		Double chubeiMoney = this.getUserIncomeMoneys(begin, end, user, "储备金");
		Double daogouMoney = this.getUserIncomeMoneys(begin, end, user, "导购金");
		Double danbaoMoney = this.getUserIncomeMoneys(begin, end, user, "担保金");
		Double zhaoshangMoney = this.getUserIncomeMoneys(begin, end, user, "招商金");
		Double xianjiMoney = this.getUserIncomeMoneys(begin, end, user, "衔级金");
		Double fenhongMoney = this.getUserIncomeMoneys(begin, end, user, "分红股");
		Double zengguMoney = this.getUserIncomeMoneys(begin, end, user, "赠股金");
		Double moneySum=chubeiMoney+daogouMoney+zhaoshangMoney+xianjiMoney+danbaoMoney+fenhongMoney+zengguMoney;
		APPBillData appBillData=new APPBillData();
		appBillData.setUser(user);
		appBillData.setChubeiMoney(chubeiMoney);
		appBillData.setDaogouMoney(daogouMoney);
		appBillData.setDanbaoMoney(danbaoMoney);
		appBillData.setXianjiMoney(xianjiMoney);
		appBillData.setMoneySum(CommUtil.formatDouble(moneySum, 2));
		appBillData.setZhaoshangMoney(zhaoshangMoney);
		appBillData.setFenhongMoney(fenhongMoney);
		appBillData.setZengguMoney(zengguMoney);
		if (user.getStore()!=null) {
			Double userHuoKuan = this.getUserIncomeMoneys(begin, end, user, "确认收货增加预存款");
			Double userHuoKuanMoney = this.getUserIncomeMoneys(begin, end, user, "货款金");
			Double userExpenditure = this.getUserIncomeMoneys(begin, end, user, "申请提现");
			appBillData.setUserHuoKuan( userHuoKuanMoney + userHuoKuan );
			appBillData.setUserExpenditure(Math.abs(userExpenditure));
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		if (user.getStore()!=null) {
			objs.add(new FilterObj(APPBillData.class, "user,chubeiMoney,daogouMoney,danbaoMoney,xianjiMoney,zhaoshangMoney,moneySum,userHuoKuan,userExpenditure,fenhongMoney,zengguMoney"));
		}else {
			objs.add(new FilterObj(APPBillData.class, "user,chubeiMoney,daogouMoney,danbaoMoney,xianjiMoney,zhaoshangMoney,moneySum,fenhongMoney,zengguMoney"));
		}
		objs.add(new FilterObj(User.class, "id,userName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appBillData, "查询个人收入清单成功", 0,filter);
		return;
	}
	private Double getUserIncomeMoneys(String begin,String end,User user,String type){
		String sql="SELECT ROUND(SUM(obj.pd_log_amount),2) FROM shopping_predeposit_log AS obj WHERE obj.addTime >= '" + begin + "' AND obj.addTime <= '" + end + "' AND obj.pd_log_user_id=" + user.getId() + " AND obj.pd_log_info LIKE '%" + type + "%'";
		List<?> money = commonService.executeNativeNamedQuery(sql);
		if (money.size()>0) {
			Double userMoney=CommUtil.formatDouble(CommUtil.null2Double(money.get(0)), 2);
			return userMoney;
		}
		return 0d;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:password  密文密码
	 *@description:app卖家查看个人货款收入 以及提现明细
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSellerUserBill.htm", method = RequestMethod.POST)
	public void appSellerUserBill(HttpServletRequest request,HttpServletResponse response,String userId,String password,String beginTime) {
		User user = ApiUtils.erifyUser(userId, password, userService);
		if (user==null||CommUtil.null2String(beginTime).equals("")) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM");
		try {
			Date parse = df.parse(beginTime);
			beginTime = CommUtil.formatLongDate(parse);
		} catch (ParseException e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "时间参数错误", 1);
			return;
		}
		if (user.getStore()==null) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}
		String Lastday_Month = ApiUtils.getFirstday_Lastday_Month(CommUtil.formatShortDate(new Date()), 1);
		long timeInterval = ApiUtils.acquisitionTimeSegment(Lastday_Month, beginTime);
		if (timeInterval>0) {
			ApiUtils.json(response, "", "只能查询本月及本月之前的数据", 1);
			return;
		}
		String begin =ApiUtils.getFirstday_Lastday_Month(beginTime, 0);
		String end = ApiUtils.getFirstday_Lastday_Month(beginTime, 1);
		Double userHuoKuan = this.getUserIncomeMoneys(begin, end, user, "确认收货增加预存款");
		Double userHuoKuanMoney = this.getUserIncomeMoneys(begin, end, user, "货款金");
		Double userExpenditure = this.getUserIncomeMoneys(begin, end, user, "申请提现");
		APPBillData appBillData=new APPBillData();
		appBillData.setUser(user);
		appBillData.setUserHuoKuan( userHuoKuanMoney + userHuoKuan );		
		appBillData.setUserExpenditure(Math.abs(userExpenditure));
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(APPBillData.class, "user,userHuoKuan,userExpenditure"));
		objs.add(new FilterObj(User.class, "id,userName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appBillData, "查询卖家收支成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:检查是否注册环信
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/inspectIsRegisterHuanxin.htm", method = RequestMethod.POST)
	public void inspectIsRegisterHuanxin(HttpServletRequest request,HttpServletResponse response,String userIds){
		if (userIds==null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		boolean haveHuanxin=true;
		String[] ids = userIds.split(",");
		for (String id : ids) {
			Long userId = CommUtil.null2Long(id);
			if (userId!=-1) {
				haveHuanxin = ApiUtils.isHaveHuanxin(userId);
				if (!haveHuanxin) {
					break;
				}
			}
		}
		ApiUtils.json(response, haveHuanxin, "验证结束", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:通过userId 获取用户的亲和力：担保用户中动态人数，领袖力：全部下属人数，统计到省一级，影响力：担保用户数量；分红股数量
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserOtherInfo.htm", method = RequestMethod.POST)
	public void appGetUserOtherInfo(HttpServletRequest request,
			HttpServletResponse response,String userId){
		Long user_id = CommUtil.null2Long(userId);
		if (user_id.longValue()==-1) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		User user=this.userService.getObjById(user_id);
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		UserTemp usertemp =new UserTemp();
		usertemp.setUser(user);
		try {
			Integer fenhongNum = ApiUtils.getFenhongNum(user, commonService);
			Map<String, Integer> userAttribute = ApiUtils.getUserAttributes(user, commonService, userService);
			usertemp.setAffinitys(userAttribute.get("affinitys"));
			usertemp.setLeader(userAttribute.get("leader"));
			usertemp.setInfluences(userAttribute.get("influences"));
			usertemp.setFenhongNum(fenhongNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,fenhongNum,influences,leader,affinitys"));
		objs.add(new FilterObj(User.class, "id,userName,mobile"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, usertemp, "查询用户信息成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:recNum:电话号码,接收者的电话号码
	 *@description:用户找回密码获取验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getVerificationCode.htm", method = RequestMethod.POST)
	public void getVerificationCode(HttpServletRequest request,HttpServletResponse response,String recNum){	
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(recNum));
		if (judgmentType!="mobile"&&judgmentType!="id") {
			ApiUtils.json(response, "", "手机号或id格式错误", 1);
			return;		
		}
		String hql="";
		if (judgmentType=="mobile") {
			hql="select obj from User AS obj where obj.mobile = " + recNum;
		}else {
			hql="select obj from User AS obj where obj.id = " + recNum;
		}
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>1) {
			ApiUtils.json(response, "", "手机号码不唯一", 1);
			return;		
		}
		if (users.size()==0) {
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;		
		}
		String phone=users.get(0).getMobile();
		judgmentType = ApiUtils.judgmentType(CommUtil.null2String(phone));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "该用户手机号码格式错误", 1);
			return;
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, phone,templateId);
		if(ret){
			JSONObject retobj=new JSONObject();
			retobj.put("safeCode", safeCode);
			retobj.put("recNum", recNum);
			HttpSession session = request.getSession();
			session.setAttribute("recNum", recNum);
			session.setAttribute("verificationCode", safeCode);
			phone=phone.replaceAll("(\\d{2})\\d{7}(\\d{2})","$1*******$2");
			ApiUtils.json(response, phone, "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:recNum:电话号码,接受者的电话号码
	 *@description:用户通过手机号找回密码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appResetPassword.htm", method = RequestMethod.POST)
	public void appResetPassword(HttpServletRequest request,HttpServletResponse response,String password){	
		HttpSession session = request.getSession();	
		Object sessionUId = session.getAttribute("UID");
		if (CommUtil.null2Long(sessionUId)==-1) {
			ApiUtils.json(response, "", "验证超时", 1);
			return;
		}
		if (CommUtil.null2String(password).length()<6) {
			ApiUtils.json(response, "", "密码长度不能小于6位", 0);
			return;
		}
		User user = userService.getObjById(CommUtil.null2Long(sessionUId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		user.setPassword(Md5Encrypt.md5(password).toLowerCase());
		userService.update(user);
		boolean haveHuanxin = ApiUtils.isHaveHuanxin(user.getId());
		if (haveHuanxin) {
			CommUtil.update_user_password(user.getPassword().toString(), user.getId().toString());//将用户的密码同步至环信服务器
		}
		session.removeAttribute("UID");
		ApiUtils.json(response, "", "密码更改成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:验证手机验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appVerificationMobileNum.htm", method = RequestMethod.POST)
	public void appVerificationMobileNum(HttpServletRequest request,HttpServletResponse response,String recNum,String verificationCode){
		boolean is_null = ApiUtils.is_null(recNum,verificationCode);
		if (is_null) {
			ApiUtils.json(response, "", "手机号或验证码不能为空", 1);
			return;
		}
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(recNum));
		String hql="";
		if (judgmentType=="mobile") {
			hql="select obj from User AS obj where obj.mobile = " + recNum;
		}else {
			hql="select obj from User AS obj where obj.id = " + recNum;
		}
		HttpSession session = request.getSession();
		Object sessionRecNum = session.getAttribute("recNum");
		Object sessionVerificationCode = session.getAttribute("verificationCode");
		if (recNum.equals(sessionRecNum)&&verificationCode.equals(sessionVerificationCode)) {
			List<User> users = userService.query(hql, null, -1, -1);
			if (users.size()==1) {
				ApiUtils.json(response, "", "success", 0);
				session.removeAttribute("recNum");
				session.removeAttribute("verificationCode");
				session.setAttribute("UID", users.get(0).getId());
				return;
			}		
		}
		ApiUtils.json(response, "", "验证码错误", 1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:更新app用户信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUpdateUserInfo.htm", method = RequestMethod.POST)
	public void appUpdateUserInfo(HttpServletRequest request,HttpServletResponse response,String userId,String password){
		boolean is_null = ApiUtils.is_null(userId,password);
		if (is_null) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "","该用户不存在或密码错误",1);
			return;
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class,
				"id,userName,availableBalance,mobile,photo,tj_status,userRank,areaGradeOfUser,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name"));
		objs.add(new FilterObj(UserRank.class, "userRankName,gradeSmallIcon"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.updateUserRenk(0, user, commonService, userService);//更新会员等级
		ApiUtils.json(response, user,"success",0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:换绑手机号
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUserChangeMobile.htm", method = RequestMethod.POST)
	public void appUserChangeMobile(HttpServletRequest request,HttpServletResponse response,String userId,String password){	
		if (ApiUtils.is_null(userId,password)) {
			ApiUtils.json(response, "","参数缺失",1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		String phone=user.getMobile();
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(phone));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "该用户手机号码格式错误", 1);
			return;
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, phone,templateId);
		if(ret){
			HttpSession session = request.getSession();
			session.setAttribute("changeId", user.getId());
			session.setAttribute("changePhone", phone);
			session.setAttribute("changeSafeCode", safeCode);
			phone=phone.replaceAll("(\\d{2})\\d{7}(\\d{2})","$1*******$2");
			ApiUtils.json(response, phone, "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:换绑手机,检验验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appChangeMobileVerification.htm", method = RequestMethod.POST)
	public void appChangeMobileVerification(HttpServletRequest request,HttpServletResponse response,String verificationCode){	
		verificationCode = CommUtil.null2String(verificationCode);
		if ("".equals(verificationCode)) {
			ApiUtils.json(response, "", "参数不能为空", 1);
			return;
		}
		HttpSession session = request.getSession();
		Object safeCode = session.getAttribute("changeSafeCode");
		if (verificationCode.equals(CommUtil.null2String(safeCode))) {
			session.setAttribute("status", "success");
			session.removeAttribute("changeSafeCode");
			ApiUtils.json(response, "", "验证成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "验证码错误，请重新输入验证码", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:换绑手机,输入新手机号获取验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appChangeNewMobile.htm", method = RequestMethod.POST)
	public void appChangeNewMobile(HttpServletRequest request,HttpServletResponse response,String newPhone){	
		if ("".equals(CommUtil.null2String(newPhone))) {
			ApiUtils.json(response, "", "参数不能为空", 1);
			return;
		}
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(newPhone));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "手机号格式错误", 1);
			return;		
		}
		HttpSession session = request.getSession();
		Object changePhone = session.getAttribute("changePhone");
		if (CommUtil.null2String(changePhone).equals(newPhone)) {
			ApiUtils.json(response, "", "换绑手机号与原手机号不能一样", 1);
			return;	
		}	
		String hql="select obj from User AS obj where obj.mobile = " + newPhone;
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>0) {
			ApiUtils.json(response, "", "该手机号已被绑定,请更换其他手机号", 1);
			return;	
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, newPhone,templateId);
		if(ret){
			session.setAttribute("newPhone", newPhone);
			session.setAttribute("newSafeCode", safeCode);
			newPhone=newPhone.replaceAll("(\\d{2})\\d{7}(\\d{2})","$1*******$2");
			ApiUtils.json(response, newPhone, "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:验证新手机验证码，绑定新手机号
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBindingNewMobile.htm", method = RequestMethod.POST)
	public void appBindingNewMobile(HttpServletRequest request,HttpServletResponse response,String verificationCode){	
		if ("".equals(CommUtil.null2String(verificationCode))) {
			ApiUtils.json(response, "", "参数不能为空", 1);
			return;
		}
		HttpSession session = request.getSession();
		Object status = session.getAttribute("status");
		if (!"success".equals(CommUtil.null2String(status))) {
			ApiUtils.json(response, "", "验证超时", 1);
			return;
		}
		Object newSafeCode = session.getAttribute("newSafeCode");
		if (!CommUtil.null2String(newSafeCode).equals(verificationCode)) {
			ApiUtils.json(response, "", "验证码错误", 1);
			return;
		}
		Object newPhone = session.getAttribute("newPhone");
		Object changeId = session.getAttribute("changeId");
		User user = userService.getObjById(CommUtil.null2Long(changeId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		user.setMobile(CommUtil.null2String(newPhone));
		boolean update = userService.update(user);
		if (update) {
			session.removeAttribute("newSafeCode");
			session.removeAttribute("newPhone");
			session.removeAttribute("changeId");
			ApiUtils.json(response, "", "修改手机号成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "修改失败，请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:未绑定手机号的老用户，获取验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBeforeUserGetCode.htm", method = RequestMethod.POST)
	public void appBeforeUserGetCode(HttpServletRequest request,HttpServletResponse response,String newPhone){	
		if (ApiUtils.is_null(newPhone)) {
			ApiUtils.json(response, "","参数缺失",1);
			return;
		}
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(newPhone));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "该用户手机号码格式错误", 1);
			return;
		}
		String hql="select obj from User AS obj where obj.mobile = " + newPhone;
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>0) {
			ApiUtils.json(response, "", "该手机号已被绑定,请更换其他手机号", 1);
			return;	
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, newPhone,templateId);
		if(ret){
			HttpSession session = request.getSession();
			session.setAttribute("beforePhone", newPhone);
			session.setAttribute("beforeSafeCode", safeCode);
			newPhone=newPhone.replaceAll("(\\d{2})\\d{7}(\\d{2})","$1*******$2");
			ApiUtils.json(response, newPhone, "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}	
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:未绑定用户绑定手机号
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appBeforeUserBindingMobile.htm", method = RequestMethod.POST)
	public void appBeforeUserBindingMobile(HttpServletRequest request,HttpServletResponse response,String verificationCode,String userId,String password){	
		if (ApiUtils.is_null(verificationCode,password,userId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		HttpSession session = request.getSession();
		Object safeCode = session.getAttribute("beforeSafeCode");
		if (!verificationCode.equals(CommUtil.null2String(safeCode))) {
			ApiUtils.json(response, "", "验证码错误", 1);
			return;
		}
		Object phone = session.getAttribute("beforePhone");
		if (!"".equals(CommUtil.null2String(user.getMobile()))) {
			ApiUtils.json(response, "", "请先验证原手机号", 1);
			return;
		}
		user.setMobile(CommUtil.null2String(phone));
		boolean update = userService.update(user);
		if (update) {
			session.removeAttribute("beforePhone");
			session.removeAttribute("beforeSafeCode");
			ApiUtils.json(response, "", "修改手机号成功", 0);
			return;
		}else {
			ApiUtils.json(response, "", "修改失败，请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:等级详情
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserRankInfo.htm", method = RequestMethod.POST)
	public void appGetUserRankInfo(HttpServletRequest request,HttpServletResponse response,String userId){
		String hql="select obj from UserRank AS obj order by obj.integralNum";
		@SuppressWarnings("unchecked")
		List<UserRank> userRanks = commonService.query(hql, null, -1, -1);
		Long user_id = CommUtil.null2Long(userId);
		AppTransferData appTransferData=new AppTransferData();
		if (user_id!=-1) {
			User user = userService.getObjById(user_id);
			int ava = (int) CommUtil.null2Double(user.getAvailableBalance());
			if (user!=null) {
				String sql="select (obj.integralNum - " + ava + ") from shopping_userrank AS obj where obj.integralNum > " + ava +" order by obj.integralNum limit 0,1";
				List<?> integralNum = commonService.executeNativeNamedQuery(sql);
				int surplus=0;
				if (integralNum.size()>0) {
					surplus=CommUtil.null2Int(integralNum.get(0));
				}
				appTransferData.setFirstData(user);
				appTransferData.setSecondData(surplus);
			}
		}
		appTransferData.setThirdData(userRanks);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppTransferData.class, "firstData,secondData,thirdData"));
		objs.add(new FilterObj(User.class, "id,userName,userRank,zhiwei,photo"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(UserRank.class, "id,rankNum,integralNum,userRankName,rankExplain,isHaveDanbaoPrice," +
				"isHaveChubeiPrice,isHaveDaogouPrice,isHaveZhaoshangPrice,isHaveXianjiPrice,isHaveZengguPrice," +
				"isHaveFenhongPrice,isHaveZhiweiRight,gradeSmallIcon,gradeBigIcon,gradeTrophy"));
		objs.add(new FilterObj(UserRankName.class, "rankName,rankExplain"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appTransferData, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:活跃等级说明
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserActiveRankExplain.htm", method = RequestMethod.POST)
	public void appGetUserActiveRankExplain(HttpServletRequest request,HttpServletResponse response){
		String hql="select obj from ReserveScale as obj order by obj.activeRank DESC";
		@SuppressWarnings("unchecked")
		List<ReserveScale> info=commonService.query(hql, null, -1, -1);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(ReserveScale.class, "id,activeRank,scale,activeScaleExplain,percentage"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, info, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:保存用户的浏览记录，用于app首页开发猜你喜欢模块,最多存放30条数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSaveUserBrowseRecords.htm")
	public void appSaveUserBrowseRecords(HttpServletRequest request,HttpServletResponse response,String userId,String goodsId){
		Long uid = CommUtil.null2Long(userId);
		Long gid = CommUtil.null2Long(goodsId);
		if (uid==-1||gid==-1) {
			ApiUtils.json(response, "", "error", 1);
			return;
		}
		User user = this.userService.getObjById(uid);
		Goods goods=(Goods) this.commonService.getById("Goods", gid+"");
		if (user==null||goods==null) {
			ApiUtils.json(response, "", "error", 1);
			return;
		}
		String hql="select obj from UserBrowseRecords as obj where obj.goods.deleteStatus=false and obj.user.id = " + userId +" and obj.goods.id = " + goodsId;
		List<UserBrowseRecords> ubrs = this.commonService.query(hql, null, 0, 1);
		if (ubrs.size()==0) {
			UserBrowseRecords userBrowseRecords=new UserBrowseRecords(user,goods,new Date(),new Date());
			userBrowseRecords.setDeleteStatus(false);
			this.commonService.save(userBrowseRecords);
		}else {
			UserBrowseRecords userBrowseRecords=ubrs.get(0);
			userBrowseRecords.setLastAccessTime(new Date());
			this.commonService.update(userBrowseRecords);
		}
		hql="select obj from UserBrowseRecords as obj where obj.user.id = " + userId+" order by obj.lastAccessTime";
		ubrs = this.commonService.query(hql, null, -1, -1);
		if (ubrs.size()>30) {
			int executeNativeSQL = this.commonService.executeNativeSQL("DELETE FROM shopping_userBrowseRecords WHERE id = " + ubrs.get(0).getId());
		}
		ApiUtils.json(response, "", "success", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:验证码登陆，获取验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetLoginSMSCode.htm",method=RequestMethod.POST)
	public void appGetLoginSMSCode(HttpServletRequest request,HttpServletResponse response,String phoneNum){
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(phoneNum));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "手机号格式错误", 1);
			return;		
		}
		String hql="";
		hql="select obj from User AS obj where obj.mobile = " + phoneNum;
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>1) {
			ApiUtils.json(response, "", "手机号码不唯一", 1);
			return;		
		}
		if (users.size()==0) {
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;		
		}
		String phone=users.get(0).getMobile();
		judgmentType = ApiUtils.judgmentType(CommUtil.null2String(phone));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "该用户手机号码格式错误", 1);
			return;
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, phone,templateId);
		if(ret){
			JSONObject retobj=new JSONObject();
			retobj.put("safeCode", safeCode);
			retobj.put("recNum", phoneNum);
			HttpSession session = request.getSession();
			session.setAttribute("SMSCodeLoginPhone", phoneNum);
			session.setAttribute("SMSCodeLoginCode", safeCode);
			phone=phone.replaceAll("(\\d{2})\\d{7}(\\d{2})","$1*******$2");
			ApiUtils.json(response, phone, "发送成功", 0);
			return;
		}else{
			ApiUtils.json(response, "", "推送失败,请重试", 1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:验证码登陆
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appSMSCodeLogin.htm",method=RequestMethod.POST)
	public void appSMSCodeLogin(HttpServletRequest request,HttpServletResponse response,String codeNum){
		if ("".equals(CommUtil.null2String(codeNum))) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		HttpSession session = request.getSession();
		String phoneNum = (String) session.getAttribute("SMSCodeLoginPhone");
		String safeCode = (String) session.getAttribute("SMSCodeLoginCode");
		if ("".equals(CommUtil.null2String(safeCode))) {
			ApiUtils.json(response, "", "请先获取验证码", 1);
			return;
		}
		if (!codeNum.equals(safeCode)) {
			String verifyLoginNum = (String) session.getAttribute("verifyLoginNum");
			if (CommUtil.null2Int(verifyLoginNum)>=2) {
				session.removeAttribute("SMSCodeLoginPhone");
				session.removeAttribute("SMSCodeLoginCode");
				session.removeAttribute("verifyLoginNum");
				ApiUtils.json(response, "", "错误次数过多，请重新获取验证码", 1);
				return;
			}
			session.setAttribute("verifyLoginNum", CommUtil.null2String(CommUtil.null2Int(verifyLoginNum)+1));
			ApiUtils.json(response, "", "验证码错误，请重新输入", 1);
			return;
		}
		session.removeAttribute("SMSCodeLoginPhone");
		session.removeAttribute("SMSCodeLoginCode");
		session.removeAttribute("verifyLoginNum");
		String hql="";
		hql="select obj from User AS obj where obj.mobile = " + phoneNum;
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>1) {
			ApiUtils.json(response, "", "手机号码不唯一", 1);
			return;		
		}
		if (users.size()==0) {
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;		
		}
		User user = users.get(0);
		if(user.getZhixian()==null){
			ZhiXianEntity zhixian=(ZhiXianEntity) this.commonService.getById("ZhiXianEntity", "1");
			user.setZhixian(zhixian);
		}
		if(user.getZhiwei()==null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
		}
		if(user.getBumen()==null){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
		}
		boolean ishaveHuanxin=false;
		if (user.getIs_huanxin()==1) {
			ishaveHuanxin=ApiUtils.isHaveHuanxin(user.getId());
			if (!ishaveHuanxin) {
				user.setIs_huanxin(0);
			}
		}
		user.setIshaveHuanxin(ishaveHuanxin);
		if (user.getIs_huanxin()==1) {
			CommUtil.update_user_password(user.getPassword().toString(), user.getId().toString());//将用户的密码同步至环信服务器
		}
		user.setLastLoginDate(new Date());
		user.setLoginCount(user.getLoginCount() + 1);
		user.setLastLoginIp(ApiUtils.getWebIp());
		boolean ret=this.userService.update(user);
		if(ret){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class,
					"id,userName,password,availableBalance,mobile,photo,tj_status,ishaveHuanxin,userRank"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			objs.add(new FilterObj(UserRank.class, "userRankName,gradeSmallIcon"));
			objs.add(new FilterObj(UserRankName.class, "id,rankName"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.updateUserRenk(0, user, commonService, userService);//更新会员等级
			ApiUtils.json(response, user, "登陆成功", 0, filter);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:用户转赠记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appIntegralTransferRecord.htm",method=RequestMethod.POST)
	public void appIntegralTransferRecord(HttpServletRequest request,HttpServletResponse response,String userId,String password,String currentPage,String beginTime,String endTime){
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		int pageSize=20;
		Map<String, Object> data = AreaPartnerUtils.getToolsData(beginTime, endTime, currentPage);
		String begin = (String) data.get("beginTime");
		String end = (String) data.get("endTime");
		int current_page = CommUtil.null2Int(data.get("current_page"));
		long days = CommUtil.null2Long(data.get("days"));
		if (days > 90) {
			ApiUtils.json(response, "", "查询时间不能大于90天", 1);
			return;
		}
		AreaPartnerUtils.getToolsData(beginTime, endTime, currentPage);
		String hql = "select obj from PredepositLog as obj where obj.pd_type = '积分转赠' or obj.pd_log_info like '%转赠%' and obj.pd_log_user.id="+user.getId()+" and obj.addTime>='" + begin +"' and obj.addTime<='" + end + "' order by obj.id DESC";
		List<PredepositLog> list = this.predepositLogService.query(hql, null, current_page*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(PredepositLog.class, "id,addTime,order_id,pd_log_user,pd_log_amount,pd_type,pd_op_type,pd_log_info,current_price"));
		objs.add(new FilterObj(User.class, "id,userName,availableBalance"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, list, "success", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:更新理财到期状态
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUpdateDepositStatus.htm",method=RequestMethod.POST)
	public void updateDepositStatus(HttpServletRequest request,HttpServletResponse response){
		String time = CommUtil.formatLongDate(new Date());
		String hql="from IntegralDepositEntity as obj where obj.orderStatus=20 and obj.depositStatus=0 and obj.endTime <= '" + time + "'";
		List<IntegralDepositEntity> list = this.integralDepositService.query(hql, null, -1, -1);
		if (list.size()>0) {
			for (IntegralDepositEntity i:list) {
				i.setDepositStatus(1);
				boolean is = integralDepositService.update(i);
				if(!is){
					System.out.println(i.getId()+"理财状态更新异常。");
				}else {
					AllocateWagesUtils.allocateMoneyToUser(
							i.getUser().getId() + "", i.getDepositAll(), "订单号"+i.getDepositOrderNum()+"积分理财到期", "",
							predepositLogService, userService, commonService, 1);
				}		
			}
		}
		ApiUtils.json(response,"","success",0);
		return;
	}
}
