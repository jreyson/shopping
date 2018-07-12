package com.shopping.api.action;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.userFunction.RedPacket;
import com.shopping.api.domain.userFunction.RedPacketRecorder;
import com.shopping.api.domain.weChat.WeChatAccountInfoEntity;
import com.shopping.api.domain.weChat.WeChatUserMessageEntity;
import com.shopping.api.service.IRedPacketService;
import com.shopping.api.service.IWeChatAccountInfoService;
import com.shopping.api.service.IWeChatUserMessageService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.Role;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
/***
 *@author:akangah
 *@description:微信用户登陆以及存储微信用户信息
 ***/
@Controller
public class WeChatUserLogin {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IRedPacketService redPacketService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IWeChatAccountInfoService weChatAccountInfoService;
	@Autowired
	private IWeChatUserMessageService weChatUserMessageService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IUserConfigService userConfigService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:weChatAccountInfoId appid
	 *@description:获取微信appid
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_getWeChatAccoutMessage.htm"})
	public void app_getWeChatAccoutMessage(HttpServletRequest request,
			HttpServletResponse response,Long weChatAccountInfoId){
		weChatAccountInfoId=CommUtil.null2Long(weChatAccountInfoId);
		WeChatAccountInfoEntity weChatAccountInfo=this.weChatAccountInfoService.getObjById(weChatAccountInfoId);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(WeChatAccountInfoEntity.class, "id,applyId,applySecretKey"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, weChatAccountInfo, "success", 0, filter);
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:code 微信code
	 *@description:微信app登录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_weChatUserLogin.htm"})
	public void  app_weChatUserLogin(HttpServletRequest request,
			HttpServletResponse response,String code,
			Long weChatAccountInfoId,Integer flag){
		Map<String,Object> acquireReturnInfo=this.getWeChatUserInfo(request, code, weChatAccountInfoId, flag);
		int retStatus=CommUtil.null2Int(acquireReturnInfo.get("disposeAtatus"));
		if(retStatus==0){
			User user=(User) acquireReturnInfo.get("showMessage");
			boolean ishaveHuanxin=false;
			if(user.getIs_huanxin()==0){//注册环信
				JSONObject huanxin_reg=CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUsername());
				if(huanxin_reg!=null){
					JSONArray jsonArray = huanxin_reg.getJSONArray("entities");
					String error=huanxin_reg.getString("error");
					if(jsonArray!=null||"duplicate_unique_property_exists".equals(error)){
						user.setIs_huanxin(1);
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
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class,
					"id,userName,password,availableBalance,mobile,photo,tj_status,ishaveHuanxin"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, user, "获取用户信息成功", 0, filter);
			return;
		}else{
			ApiUtils.json(response, "", acquireReturnInfo.get("showMessage").toString(), 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:code 微信code
	 *@description:微信公众号登录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/weChatPublicAccountsUserLogin.htm")//, method = RequestMethod.POST
	public ModelAndView weChatPublicAccountsUserLogin(HttpServletRequest request,
			HttpServletResponse response,String code,
			Long weChatAccountInfoId,Integer flag,String redPacketSign){
		ModelAndView mv=new JModelAndView("h5Register/wapRegisterError.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		Map<String,Object> acquireReturnInfo=this.getWeChatUserInfo(request, code, weChatAccountInfoId, flag);
		int retStatus=CommUtil.null2Int(acquireReturnInfo.get("disposeAtatus"));
		if(retStatus==0){
			mv.addObject("op_title", "此活动目前只对新用户开放");
			mv.addObject("url", CommUtil.getURL(request)+"/redPacketH5Get.htm?redPacketSign="+redPacketSign);
		}else if(retStatus==1){
			mv = new JModelAndView("redPacket/acquireMobileVerificationCode.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			mv.addObject("url", CommUtil.getURL(request)+"/registerWeChatNewUser.htm");
			mv.addObject("redPacketSign", redPacketSign);
		}else{
			mv.addObject("op_title", acquireReturnInfo.get("showMessage"));
			mv.addObject("url", CommUtil.getURL(request)+"/redPacketH5Get.htm?redPacketSign="+redPacketSign);
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:verificationCode 手机号验证码，mobilePhone 手机号   danDaoRen  担保人
	 *@description:注册为微信新用户
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/registerWeChatNewUser.htm")
	public ModelAndView registerWeChatNewUser(HttpServletRequest request,
			HttpServletResponse response,String verificationCode,String mobilePhone,
			String redPacketSign){
		ModelAndView mv= new JModelAndView("redPacket/beOverdue.html",  //红包失效页面
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		Map<String,Object> mapAttrInfo=new HashMap<String, Object>();
		mapAttrInfo.put("op_title", "此红包失效了！");
		mapAttrInfo.put("promptWord", "温馨提示：红包的有效时间为72小时");
		if(ApiUtils.is_null(verificationCode,mobilePhone,redPacketSign)){
			mv=new JModelAndView("h5Register/wapRegisterError.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			mapAttrInfo.clear();
			mapAttrInfo.put("op_title", "参数缺失，请重试");
			mapAttrInfo.put("url", CommUtil.getURL(request)+"/redPacketH5Get.htm?redPacketSign="+redPacketSign);
			mv=this.addAttr(mv,mapAttrInfo);
			return mv;
		}
		Map<String,Object> recRetInfoMap=new HashMap<String, Object>();
		String hql="select obj from RedPacket as obj where obj.sign='"+redPacketSign+"'";
		User user=null;
		RedPacket redPacket=null;
		List<RedPacket> redPacketRecordList=this.redPacketService.query(hql, null, -1, -1);
		if(redPacketRecordList.size()>0){
			redPacket=redPacketRecordList.get(0);
			if(redPacket!=null){
				user=redPacket.getProvideUser();
				recRetInfoMap=this.weChatRegist(request, mobilePhone, verificationCode, user.getId()+"");
				if(CommUtil.null2Int(recRetInfoMap.get("status"))==0){
					user=(User) recRetInfoMap.get("msg");
					boolean redPacketIsDue=redPacket.getOverdueTime().after(new Date())&&redPacket.getSurplusNum()>0&&!redPacket.isOverdueState();
					if(redPacketIsDue){//红包未失效
						Double singleMoney=CommUtil.formatDouble(redPacket.getSingleMoney(), 2);
						user.setAvailableBalance(BigDecimal.valueOf(singleMoney));
						boolean updateUserInfo=this.userService.update(user);
						if(updateUserInfo){
							if(redPacket.getSurplusNum()-1==0){
								redPacket.setOverdueState(true);
							}
							redPacket.setSurplusNum(redPacket.getSurplusNum()-1);
							redPacket.setSurplusMoney(CommUtil.formatDouble(redPacket.getSurplusMoney()-(redPacket.getSingleMoney()*1), 2));
							boolean redPackUpdateRet=this.redPacketService.update(redPacket);
							if(redPackUpdateRet){//生成收支明细
								PredepositLog positLog = new PredepositLog();
								positLog.setAddTime(new Date());
								positLog.setPd_log_user(user);
								positLog.setPd_op_type("增加");
								positLog.setPd_log_amount(BigDecimal.valueOf(singleMoney));
								positLog.setPd_log_info("领取分享招人红包");
								positLog.setPd_type("可用预存款");
								positLog.setCurrent_price(singleMoney);
								boolean saveRet=this.predepositLogService.save(positLog);
								if(saveRet){//生成红包领取详情
									RedPacketRecorder redPacketRecorder=new RedPacketRecorder();
									redPacketRecorder.setAddTime(new Date());
									redPacketRecorder.setDeleteStatus(false);
									redPacketRecorder.setReceiveUser(user);
									redPacketRecorder.setRedPacket(redPacket);
									this.commonService.save(redPacketRecorder);
								}
							}
						}
						mv = new JModelAndView("redPacket/getSuccess.html",
								this.configService.getSysConfig(),
								this.userConfigService.getUserConfig(), 5, request, response);
						mapAttrInfo.clear();
						mapAttrInfo.put("opTitle", "恭喜您领取成功");
						mapAttrInfo.put("promptWord", "红包已存入您余额，请在下载app登陆提现");
						mv=this.addAttr(mv,mapAttrInfo);
						return mv;
					}
				}else{
					mv=new JModelAndView("h5Register/wapRegisterError.html",
							this.configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 5, request, response);
					mapAttrInfo.clear();
					mapAttrInfo.put("op_title", recRetInfoMap.get("msg"));
					mapAttrInfo.put("url", CommUtil.getURL(request)+"/redPacketH5Get.htm?redPacketSign="+redPacketSign);
					mv=this.addAttr(mv,mapAttrInfo);
					return mv;
				}
			}else{
				mv=this.addAttr(mv,mapAttrInfo);
				return mv;
			}
		}else{
			mv=this.addAttr(mv,mapAttrInfo);
			return mv;
		}
		return mv;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:绑定老用户信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/renewalUserInfo.htm", method = RequestMethod.POST)
	public void renewalUserInfo(HttpServletRequest request,HttpServletResponse response,String loginName,String password){
		boolean is_null = ApiUtils.is_null(loginName,password);
		if (is_null) {
			ApiUtils.json(response, "", "参数错误", 1);
			return;
		}
		String type = ApiUtils.judgmentType(loginName.trim());
		String hql;
		if (type.equals("mobile")) {
			hql="select obj from User as obj where obj.mobile='"+loginName+"'";
		}else if (type.equals("id")) {
			hql="select obj from User as obj where obj.id='"+loginName+"'";
		}else{
			hql="select obj from User as obj where obj.userName='"+loginName+"'";
		}
		List<User> userList=this.userService.query(hql, null, -1,-1);
		if(userList.size()>1){
			ApiUtils.json(response, "", "用户不唯一,请选择其他方式登陆,请换其他方式登陆", 1);
			return;
		}
		if(userList.size()==0){
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		User user = userList.get(0);
		String psw = user.getPassword();
		String lowerCase=Md5Encrypt.md5(password).toLowerCase();
		if (!psw.equals(lowerCase)) {
			ApiUtils.json(response, "", "密码错误！", 1);
			return;
		}
		HttpSession session = request.getSession();
		Object weChatUserInfoId = session.getAttribute("weChatUserInfoId");
		if ("".equals(CommUtil.null2String(weChatUserInfoId))) {
			ApiUtils.json(response, "", "验证超时", 1);
			return;
		}
		session.removeAttribute("weChatUserInfoId");
		WeChatUserMessageEntity weChatUserMessageEntity = weChatUserMessageService.getObjById(CommUtil.null2Long(weChatUserInfoId));
		if (weChatUserMessageEntity==null) {
			ApiUtils.json(response, "", "验证超时,请重新验证", 1);
			return;
		}
		WeChatUserMessageEntity weChatUserMessage = user.getWeChatUserMessage();
		weChatUserMessage.setOwerSystemUser(null);
		this.weChatUserMessageService.update(weChatUserMessage);
		weChatUserMessageEntity.setOwerSystemUser(user);
		this.weChatUserMessageService.save(weChatUserMessageEntity);
		if (user.getPhoto()==null) {
			this.saveWeiXinImg(weChatUserMessageEntity.getHeadimgurl(),user);
		}
		boolean ishaveHuanxin=false;
		if(user.getIs_huanxin()==0){//注册环信
			JSONObject huanxin_reg=CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUsername());
			if(huanxin_reg!=null){
				JSONArray jsonArray = huanxin_reg.getJSONArray("entities");
				String error=huanxin_reg.getString("error");
				if(jsonArray!=null||"duplicate_unique_property_exists".equals(error)){
					user.setIs_huanxin(1);
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
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class,
				"id,userName,password,availableBalance,mobile,photo,tj_status,ishaveHuanxin"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, user, "绑定成功", 0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:recNum:电话号码,接收者的电话号码
	 *@description:微信用户注册获取验证码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getWeChatVerificationCode.htm", method = RequestMethod.POST)
	public void getWeChatVerificationCode(HttpServletRequest request,HttpServletResponse response,String recNum){	
		String judgmentType = ApiUtils.judgmentType(CommUtil.null2String(recNum));
		if (judgmentType!="mobile") {
			ApiUtils.json(response, "", "手机号或id格式错误", 1);
			return;		
		}
		String hql="select obj from User AS obj where obj.mobile = " + recNum;
		List<User> users = userService.query(hql, null, -1, -1);
		if (users.size()>0) {
			ApiUtils.json(response, "", "该手机号已被注册，请更换手机号或绑定已有帐号，重新获取验证码", 1);
			return;		
		}
		String safeCode=CommUtil.get_fixation_length(6);
		String templateId="SMS_69255023";//万手app验证码
		JSONObject obj=new JSONObject();
		obj.put("code",safeCode);
		boolean ret=CommUtil.sendNote(safeCode, obj, recNum,templateId);
		System.out.println("微信注册用户验证码"+safeCode);
		if(ret){
			JSONObject retobj=new JSONObject();
			retobj.put("safeCode", safeCode);
			retobj.put("recNum", recNum);
			HttpSession session = request.getSession();
			session.setAttribute("vxPhone", recNum);
			session.setAttribute("vxVerificationCode", safeCode);
			ApiUtils.json(response, "", "推送成功", 0);
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
	 *@description:绑定手机号注册新用户
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/weChatRegistUser.htm", method = RequestMethod.POST)
	public void weChatRegistUser(HttpServletRequest request,HttpServletResponse response,String phone,String verificationCode,String danBaoRen){
		Map<String, Object> msg = this.weChatRegist(request, phone, verificationCode, danBaoRen);
		String status = CommUtil.null2String(msg.get("status"));
		if ("0".equals(status)) {
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(User.class,
					"id,userName,password,availableBalance,mobile,photo,tj_status,ishaveHuanxin"));
			objs.add(new FilterObj(Accessory.class, "path,name"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, msg.get("msg"), "注册成功", 0,filter);
			return;
		}else {
			ApiUtils.json(response, "", CommUtil.null2String(msg.get("msg")), 1);
			return;
		}		
	}
	//判断微信用户是否是新用户以及返回相应的信息
	private Map<String,Object> getWeChatUserInfo(HttpServletRequest request,
			String code,Long weChatAccountInfoId,
			Integer flag){
		Map<String,String> weChatAccountInfoMap=new LinkedHashMap<String,String>();
		Map<String,Object> disposeMessage=new TreeMap<String, Object>();
		WeChatAccountInfoEntity weChatAccountInfo=null;
		JSONObject jsonobj=null;
		JSONObject userJsonObj=null;
		code=CommUtil.null2String(code);
		flag=CommUtil.null2Int(flag);
		weChatAccountInfoId=CommUtil.null2Long(weChatAccountInfoId);
		if(ApiUtils.is_null(code,weChatAccountInfoId+"",flag+"")){
			disposeMessage.put("showMessage", "参数不能为空");
			disposeMessage.put("disposeAtatus", "3");
			return disposeMessage;
		}
		weChatAccountInfo=this.weChatAccountInfoService.getObjById(weChatAccountInfoId);
		if(weChatAccountInfo!=null){
			jsonobj=this.acquireWeChatAccessToken(weChatAccountInfo, weChatAccountInfoMap, code, flag);
			if(jsonobj.toJSONString().indexOf("openid")>-1){
				weChatAccountInfoMap.clear();
				userJsonObj=this.acquireWeChatUserMessage(jsonobj,weChatAccountInfoMap, flag);
				if(userJsonObj.toJSONString().indexOf("openid")>-1){
					String hql="select obj from WeChatUserMessageEntity as obj where obj.unionid='"+userJsonObj.getString("unionid")+"'";
					List<WeChatUserMessageEntity> weChatUserMessageList=this.weChatUserMessageService.query(hql, null, -1, -1);
					if(weChatUserMessageList.size()>0){
						User user=weChatUserMessageList.get(0).getOwerSystemUser();
						this.getWeiXinUserInfo(userJsonObj,weChatUserMessageList.get(0));
						if(user!=null){
							disposeMessage.put("showMessage", user);
							disposeMessage.put("disposeAtatus", "0");
						}else{
							request.getSession().setAttribute("weChatUserInfoId", weChatUserMessageList.get(0).getId());
							disposeMessage.put("showMessage", "该账户为新用户，需要绑定手机号或者绑定原有万手账号");
							disposeMessage.put("disposeAtatus", "1");
						}
						return disposeMessage;
					}else{
						Long weChatUserMessageId=this.getWeiXinUserInfo(userJsonObj,null);
						if(weChatUserMessageId!=0){
							request.getSession().setAttribute("weChatUserInfoId", weChatUserMessageId);
							disposeMessage.put("showMessage", "该账户为新用户，需要绑定手机号或者绑定原有万手账号");
							disposeMessage.put("disposeAtatus", "1");
						}else{
							disposeMessage.put("showMessage", "保存微信用户失败，请重试");
							disposeMessage.put("disposeAtatus", "3");
						}
						return disposeMessage;
					}
				}else{
					disposeMessage.put("showMessage", "access_token等参数不合法，获取不到微信用户信息，请重试");
					disposeMessage.put("disposeAtatus", "3");
					return disposeMessage;
				}
			}else{
				disposeMessage.put("showMessage", "code等参数不合法，获取不到access_token信息，请重试");
				disposeMessage.put("disposeAtatus", "3");
				return disposeMessage;
			}
		}else{
			disposeMessage.put("showMessage", "weChatAccountInfoId参数不正确");
			disposeMessage.put("disposeAtatus", "3");
			return disposeMessage;
		}
	}
	//获取微信token
	private JSONObject acquireWeChatAccessToken(WeChatAccountInfoEntity weChatAccountInfo,
			Map<String,String> weChatAccountInfoMap,String code,Integer flag){
		JSONObject jsonobj=new JSONObject();
		String requestUrl="";
		String weChatAcquireTokenURl="";
		if(weChatAccountInfo==null||weChatAccountInfoMap==null||ApiUtils.is_null(code,flag+"")){
			return jsonobj;
		}
		weChatAcquireTokenURl="https://api.weixin.qq.com/sns/oauth2/access_token?";
		weChatAccountInfoMap.put("appid", weChatAccountInfo.getApplyId());
		weChatAccountInfoMap.put("secret", weChatAccountInfo.getApplySecretKey());
		weChatAccountInfoMap.put("code", code);
		weChatAccountInfoMap.put("grant_type", weChatAccountInfo.getGrant_type());
		requestUrl=ApiUtils.conactRequestUrl(weChatAccountInfoMap, weChatAcquireTokenURl,flag);//获得url请求路径
		jsonobj=CommUtil.acquireWeChatUserData(requestUrl);
		return jsonobj;
	}
	//获取微信用户信息
	private JSONObject acquireWeChatUserMessage(JSONObject jsonobject,
			Map<String,String> weChatAccountInfoMap,Integer flag){
		JSONObject jsonobj=new JSONObject();
		String weChatAcquireUserMessageURL="";
		String requestUrl="";
		if(jsonobject==null||weChatAccountInfoMap==null||ApiUtils.is_null(flag+"")){
			return jsonobj;
		}
		weChatAcquireUserMessageURL="https://api.weixin.qq.com/sns/userinfo?";
		weChatAccountInfoMap.put("access_token", jsonobject.getString("access_token"));
		weChatAccountInfoMap.put("openid", jsonobject.getString("openid"));
		weChatAccountInfoMap.put("lang", "zh_CN ");
		requestUrl=ApiUtils.conactRequestUrl(weChatAccountInfoMap, weChatAcquireUserMessageURL,flag);//获得url请求路径
		jsonobj=CommUtil.acquireWeChatUserData(requestUrl);
		return jsonobj;
	}
	//保存并更新微信用户信息
	private Long getWeiXinUserInfo(JSONObject obj,WeChatUserMessageEntity weChatUserMessage){
		WeChatUserMessageEntity weiXinInfo=null;
		boolean ret=false;
		boolean flag=weChatUserMessage==null;
		Long weiXinUserInfoId=0L;
		if(flag){
			weiXinInfo=new WeChatUserMessageEntity();
		}else{
			weiXinInfo=weChatUserMessage;
		}
		System.out.println(obj.getString("nickname"));
		weiXinInfo.setAddTime(new Date());
		weiXinInfo.setDeleteStatus(false);
		weiXinInfo.setCity(obj.getString("city"));
		weiXinInfo.setCountry(obj.getString("country"));
		weiXinInfo.setHeadimgurl(obj.getString("headimgurl"));//系统用户未生成，不能将头像存入资源服务器，需在下一步完成
		Pattern pattern = Pattern.compile("[^\u4E00-\u9FA5\\w+]");
		String nickname=pattern.matcher(CommUtil.null2String(obj.getString("nickname"))).replaceAll("");
		weiXinInfo.setNickname(nickname);
		weiXinInfo.setOpenId(obj.getString("openid"));
		weiXinInfo.setProvince(obj.getString("province"));
		weiXinInfo.setSex(CommUtil.null2Int(obj.getString("sex")));
		weiXinInfo.setUnionid(obj.getString("unionid"));	
		weiXinInfo.setLanguage(obj.getString("language"));
		if(flag){
			ret=this.weChatUserMessageService.save(weiXinInfo);
		}else{
			ret=this.weChatUserMessageService.update(weChatUserMessage);
		}
		if(ret){
			weiXinUserInfoId=weiXinInfo.getId();
		}
		return weiXinUserInfoId;
	}
	//上传微信头像
	private boolean saveWeiXinImg(String imgUrl,User user){
		String imageType =".png";
		String bigAvatarName = user.getId()+System.currentTimeMillis()
				/1000 + "_big";
		InputStream is=null;
		OutputStream os=null;
		try {
			URL url = new URL(imgUrl);
			URLConnection connection = url.openConnection();
			is = connection.getInputStream();
			byte[] bs = new byte[1024];
			int len;
			String filePath = SystemResPath.imgUploadUrl+File.separator
					+"upload"+File.separator+"avatar";
			File file = new File(filePath);
			if (!file.exists()) {
				CommUtil.createFolder(filePath);//创建文件夹
			}
			os = new FileOutputStream(filePath + "\\" + bigAvatarName + imageType);
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			if (user.getPhoto()==null) {
				Accessory photo = new Accessory();
				photo.setAddTime(new Date());
				photo.setWidth(132);
				photo.setHeight(132);
				photo.setName(bigAvatarName + imageType);
				photo.setExt(imageType);
				photo.setPath(this.configService.getSysConfig().getUploadFilePath()
						+ "/avatar");
				this.accessoryService.save(photo);
				user.setPhoto(photo);
				this.userService.update(user);
			}			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			try {
				os.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//验证用户名是否存在
	private boolean verifyUserName(String userName){
		String sql="select id from shopping_user where userName = '" + userName + "'";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		if (query.size()>0) {
			return true;
		}
		return false;
	}
	//绑定微信新用户
	private Map<String, Object> weChatRegist(HttpServletRequest request,String phone,String verificationCode,String danBaoRen){
		Map<String, Object> msg=new HashMap<>();
		if(ApiUtils.is_null(phone,verificationCode)){
			msg.put("status", "1");
			msg.put("msg", "参数错误");
			return msg;
		}
		boolean verifyPhoneExistenceState = ApiUtils.verifyPhoneExistenceState(phone, userService);
		if (verifyPhoneExistenceState) {
			msg.put("status", "1");
			msg.put("msg", "号码已存在或者号码格式不正确");
			return msg;
		}
		HttpSession session = request.getSession();
		Object sessionRecNum = session.getAttribute("vxPhone");
		Object sessionVerificationCode = session.getAttribute("vxVerificationCode");
		if (!phone.equals(sessionRecNum)||!verificationCode.equals(sessionVerificationCode)) {
			msg.put("status", "1");
			msg.put("msg", "验证码错误");
			return msg;
		}		
		Object weChatUserInfoId = session.getAttribute("weChatUserInfoId");
		if ("".equals(CommUtil.null2String(weChatUserInfoId))) {
			msg.put("status", "1");
			msg.put("msg", "验证码超时");
			return msg;
		}
		WeChatUserMessageEntity weChatUserMessageEntity = weChatUserMessageService.getObjById(CommUtil.null2Long(weChatUserInfoId));
		if (weChatUserMessageEntity==null) {
			msg.put("status", "1");
			msg.put("msg", "参数错误，请重新注册");
			return msg;
		}
		String userName="";
		boolean verifyUserName=true;
		int i=0;
		do {
			userName=weChatUserMessageEntity.getNickname().trim();
			if (i!=0) {
				userName=userName+ApiUtils.getRandomString(2);
			}
			verifyUserName = this.verifyUserName(userName);
			i++;
		} while (verifyUserName);
		if("".equals(CommUtil.null2String(danBaoRen))){
			danBaoRen="夏天";
		}
		String type = ApiUtils.judgmentType(danBaoRen.trim());
		String hql;
		if (type.equals("mobile")) {
			hql="select obj from User as obj where obj.mobile='"+danBaoRen+"'";
		}else if (type.equals("id")) {
			hql="select obj from User as obj where obj.id='"+danBaoRen+"'";
		}else{
			hql="select obj from User as obj where obj.userName='"+danBaoRen+"'";
		}
		List<User> daoList=this.userService.query(hql, null, -1, -1);
		if(daoList.size()==0){
			msg.put("status", "1");
			msg.put("msg", "担保人不存在");
			return msg;
		}
		session.removeAttribute("vxPhone");
		session.removeAttribute("vxVerificationCode");
		session.removeAttribute("weChatUserInfoId");
		User danbaoUser = daoList.get(0);
		int is_finish_userName=0;//当时将手机号码注册和用户名注册区分开,用此字段作为用户是否能够修改用户名的标识符,现在俩者合并到一起，0表示不用修改用户名
		User user = new User();
		ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
		ZhiXianEntity zhixian=(ZhiXianEntity) this.commonService.getById("ZhiXianEntity", "1");
		user.setZhiwei(zhiwei);
		user.setZhixian(zhixian);
		user.setIs_finish_userName(is_finish_userName);
		user.setBumen(danbaoUser.getBumen());
		user.setAreaGradeOfUser(danbaoUser.getAreaGradeOfUser());//和担保人所属区域一样
		user.setDan_bao_ren(danbaoUser.getUserName());
		user.setTj_status(false);
		user.setUserName(userName);
		user.setMobile(phone);
		user.setUserRole("BUYER");
		user.setAddTime(new Date());
		user.setEmail("");
		user.setPassword(Md5Encrypt.md5(ApiUtils.getRandomString(8)).toLowerCase());
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
		boolean ret=this.userService.update(user);
		weChatUserMessageEntity.setOwerSystemUser(user);
		weChatUserMessageService.save(weChatUserMessageEntity);
		this.saveWeiXinImg(weChatUserMessageEntity.getHeadimgurl(),user);
		if(ret){
			msg.put("status", "0");
			msg.put("msg", user);
			return msg;
		}
		msg.put("status", "1");
		msg.put("msg", "注册失败");
		return msg;
	}
	private ModelAndView addAttr(ModelAndView mv,Map<String,Object> addAttrInfo){
		Set<Map.Entry<String, Object>> setAttrEntry=addAttrInfo.entrySet();
		Iterator<Map.Entry<String, Object>> setAttrIterator=setAttrEntry.iterator();
		while(setAttrIterator.hasNext()){
			Map.Entry<String, Object> mapAttrEntry=setAttrIterator.next();
			mv.addObject(mapAttrEntry.getKey(), mapAttrEntry.getValue());
		}
		return mv;
	}
}
