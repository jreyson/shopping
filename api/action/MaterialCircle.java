package com.shopping.api.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.materialCircle.GeneralizeGoods;
import com.shopping.api.domain.materialCircle.LabelManage;
import com.shopping.api.domain.materialCircle.MCAuthorityEntity;
import com.shopping.api.domain.materialCircle.MaterialCircleMemberEntity;
import com.shopping.api.domain.materialCircle.MaterialItems;
import com.shopping.api.domain.materialCircle.ReportManage;
import com.shopping.api.domain.materialCircle.TranspondRecord;
import com.shopping.api.domain.materialCircle.VPResource;
import com.shopping.api.output.McTempOut;
import com.shopping.api.service.materialCircle.IMCFunctionService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.api.tools.WriteFileUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.SysConfig;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IUserService;
/***
 *@author:akangah
 *@description:app端素材圈管理控制
 *@classType:action类
 ***/
@Controller
public class MaterialCircle {
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	@Qualifier("mCAuthorityServiceImpl")
	private IMCFunctionService<MCAuthorityEntity> mCAuthorityService ;
	@Autowired
	@Qualifier("mCMemberServiceImpl")
	private IMCFunctionService<MaterialCircleMemberEntity> mCMemberService;
	@Autowired
	@Qualifier("generalizeGoodsServiceImpl")
	private IMCFunctionService<GeneralizeGoods> generalizeGoodsService;
	@Autowired
	@Qualifier("labelManageServiceImpl")
	private IMCFunctionService<LabelManage> labelManageService;
	@Autowired
	@Qualifier("reportManageServiceImpl")
	private IMCFunctionService<ReportManage> reportManageService;
	@Autowired
	@Qualifier("materialItemsServiceImpl")
	private IMCFunctionService<MaterialItems> materialItemsService;
	@Autowired
	@Qualifier("vPResourceServiceImpl")
	private IMCFunctionService<VPResource> vPResourceService;
	@Autowired
	@Qualifier("transpondRecordServiceImpl")
	private IMCFunctionService<TranspondRecord> transpondRecordService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈查看成员接口,如果是管理员,那么就查看所有的成员,如果不是,提示权限不足
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/judgeIsHasAuthViewMember.htm", method = RequestMethod.POST)
	public void judgeIsHasAuthViewMember(HttpServletRequest request,
			HttpServletResponse response,Long userId,Integer currentPage){
		MaterialCircleMemberEntity mcMember=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(mcMember!=null){
			if(mcMember.getMcAuthority()!=null&&mcMember.getMcAuthority().isAdmin()){//如果是管理员,才可以查看
				Integer pageSize=12;
				currentPage=CommUtil.null2Int(currentPage);
				//查出所有已经通过申请的人并且分组排序
				String hql="select obj from MaterialCircleMemberEntity as obj where obj.checkStatus=1 and obj.mcAuthority is not null group by obj.mcAuthority.memberLevel,obj.id order by obj.mcAuthority.memberLevel asc";
				List<MaterialCircleMemberEntity> outputList=this.mCMemberService.query(hql, null, currentPage*pageSize, pageSize);
				if(outputList.size()>0){
					List<FilterObj> objs = new ArrayList<FilterObj>();
					objs.add(new FilterObj(MaterialCircleMemberEntity.class, "id,addTime,member,transmitTotalNum,mcAuthority"));
					objs.add(new FilterObj(User.class, "id,userName,photo"));
					objs.add(new FilterObj(Accessory.class, "path,name"));
					objs.add(new FilterObj(MCAuthorityEntity.class, "rankNameExplain,authIllustrate"));
					CustomerFilter filter = ApiUtils.addIncludes(objs);
					ApiUtils.json(response, outputList, "获取素材圈成员成功", 0,filter);
				}
			}else{//普通读写用户(具有在素材圈新增素材的权限)点击查看成员列表时的输出
				ApiUtils.json(response, "", "权限不足,只有管理员才可以查看成员", 1);
				return;
			}
		}else{//普通只读用户(不具有在素材圈新增素材的权限)点击查看成员列表时的输出
			ApiUtils.json(response, "", "权限不足,只有管理员才可以查看成员", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员搜索加入到素材圈的成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminSearchMcMember.htm", method = RequestMethod.POST)
	public void adminSearchMcMember(HttpServletRequest request,HttpServletResponse response,
			Long userId,String searchValue){
		MaterialCircleMemberEntity mcMember=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(mcMember.getMcAuthority().isAdmin()){
			String filterStr="";
			if("mobile".equals(ApiUtils.judgmentType(CommUtil.null2String(searchValue)))){
				filterStr="mobile like '%"+searchValue+"%'";
			}else if("id".equals(ApiUtils.judgmentType(CommUtil.null2String(searchValue)))){
				filterStr="id like '%"+searchValue+"%'";
			}else{
				filterStr="userName like '%"+searchValue+"%'";
			}
			String hql="select obj from MaterialCircleMemberEntity as obj where obj.checkStatus=1 and obj.mcAuthority is not null and obj.member."+filterStr;
			List<MaterialCircleMemberEntity> mcMemberList=this.mCMemberService.query(hql, null, -1, -1);
			if(mcMemberList.size()>0){
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(MaterialCircleMemberEntity.class, "id,addTime,member,transmitTotalNum,mcAuthority"));
				objs.add(new FilterObj(User.class, "id,userName,photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				objs.add(new FilterObj(MCAuthorityEntity.class, "rankNameExplain,authIllustrate"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, mcMemberList, "获取素材圈成员成功", 0,filter);
			}else{
				ApiUtils.json(response, "", "没有找到要查找的成员", 1);
			}
			return;
		}else{
			ApiUtils.json(response, "", "权限不足,只有管理员才可以进行搜索查找", 2);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员查看申请加入到素材圈的成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminViewApplyForJoinMcUser.htm", method = RequestMethod.POST)
	public void adminViewApplyForJoinMcUser(HttpServletRequest request,HttpServletResponse response,
			Long userId,Integer currentPage){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()){
				Integer pageSize=12;
				currentPage=CommUtil.null2Int(currentPage);
				String hql="select obj from MaterialCircleMemberEntity as obj where obj.checkStatus=0 and obj.mcAuthority is null order by obj.addTime desc";
				List<MaterialCircleMemberEntity> outputList=this.mCMemberService.query(hql, null, currentPage*pageSize, pageSize);
				if(outputList.size()>0){
					List<FilterObj> objs = new ArrayList<FilterObj>();
					objs.add(new FilterObj(MaterialCircleMemberEntity.class, "id,addTime,member,transmitTotalNum,mcAuthority"));
					objs.add(new FilterObj(User.class, "id,userName,photo"));
					objs.add(new FilterObj(Accessory.class, "path,name"));
					objs.add(new FilterObj(MCAuthorityEntity.class, "rankNameExplain,authIllustrate"));
					CustomerFilter filter = ApiUtils.addIncludes(objs);
					ApiUtils.json(response, outputList, "获取素材圈成员成功", 0,filter);
				}else{
					ApiUtils.json(response, outputList, "暂无申请人员", 1);
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:用户向管理员发起申请,请求加入到素材圈
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/applyForJoinMc.htm", method = RequestMethod.POST)
	public void applyForJoinMc(HttpServletRequest request,HttpServletResponse response,
			Long userId){
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		MaterialCircleMemberEntity mcMember=null;
		if(user!=null){
			mcMember=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+userId);
			if(mcMember==null){//只有没有申请加入过的人才可以进行申请
				mcMember=new MaterialCircleMemberEntity(new Date(), false, user, 0L, Short.valueOf(0+""), null);
				boolean ret=this.mCMemberService.save(mcMember);
				if(ret){//向管理员随机发送申请
					String hql="select obj from MaterialCircleMemberEntity as obj where obj.mcAuthority.isAdmin=true";
					List<MaterialCircleMemberEntity> mcMemberList=this.mCMemberService.query(hql, null, -1, -1);
					Random random=new Random();
					MaterialCircleMemberEntity admin=mcMemberList.get(random.nextInt(mcMemberList.size()));
					if(admin!=null){
						String recNum=admin.getMember().getMobile();
						if("mobile".equals(ApiUtils.judgmentType(CommUtil.null2String(recNum)))){
							JSONObject jsonobj = new JSONObject();
							String templateId = "SMS_136394884";// 短信模板ID
							jsonobj.put("adminName", admin.getMcAuthority().getRankNameExplain()+admin.getMember().getUserName());
							jsonobj.put("userName", mcMember.getMember().getUserName());
							CommUtil.sendNote("", jsonobj, recNum, templateId);
						}
					}
					ApiUtils.json(response, "", "向管理员发送审核成功", 0);
					return;
				}
			}else{//已经加入的人员不能再进行申请
				if(mcMember.getCheckStatus().shortValue()==0||mcMember.getMcAuthority()==null){
					ApiUtils.json(response, "", "不能重复提交,您的申请已经提交,请等待管理员审核", 1);
				}else{
					ApiUtils.json(response, "", "您已加入素材圈,无需再申请", 1);
				}
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员同意用户加入到素材圈
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminAgreeUserApplyForJoin.htm", method = RequestMethod.POST)
	public void adminAgreeUserApplyForJoin(HttpServletRequest request,HttpServletResponse response,
			Long mCircleMemberId,boolean isAgree,Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			MaterialCircleMemberEntity mcMember=this.mCMemberService.getObjById(CommUtil.null2Long(mCircleMemberId));
			if(admin.getMcAuthority().isAdmin()){
				boolean ret=false;
				if(isAgree){
					//默认申请加入的用户级别为4,具有读写权限
					MCAuthorityEntity mcAuth=this.mCAuthorityService.getObjById(4L);
					mcMember.setCheckStatus((short)1);//审核通过
					mcMember.setMcAuthority(mcAuth);//赋值权限为默认读写权限
					ret=this.mCMemberService.update(mcMember);
					if(ret){
						ApiUtils.json(response, "", "管理员通过申请", 0);
						return;
					}
				}else{
					ret=this.mCMemberService.remove(mCircleMemberId);
					if(ret){
						ApiUtils.json(response, "", "操作成功,管理员拒绝申请", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足", 2);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员根据自己的权限范围获取可以设置的用户级别
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminAcquireSelfCanSetLevels.htm", method = RequestMethod.POST)
	public void adminSetUserLevel(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcMemberId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			MaterialCircleMemberEntity mcMember=this.mCMemberService.getObjById(CommUtil.null2Long(mcMemberId));
			//如果是自己的下级的话,则是可以获取到可以授权的权限列表的,这里平级之间不授权,数值越小,权限越大
			if(admin.getMcAuthority().getMemberLevel().intValue()<mcMember.getMcAuthority().getMemberLevel().intValue()){
				String hql="select obj from MCAuthorityEntity as obj where obj.memberLevel>"+admin.getMcAuthority().getMemberLevel();
				List<MCAuthorityEntity> outputList=this.mCAuthorityService.query(hql, null, -1, -1);
				if(outputList.size()>0){
					List<FilterObj> objs = new ArrayList<FilterObj>();
					objs.add(new FilterObj(MCAuthorityEntity.class, "id,rankNameExplain"));
					CustomerFilter filter = ApiUtils.addIncludes(objs);
					ApiUtils.json(response, outputList, "获取可以授权权限列表成功", 0,filter);
				}
			}else{
				ApiUtils.json(response, "", "权限不足,不能够给自己的上级和平级授权", 1);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员确定给自己的下级授予权限
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminConfirmGiveMemberAuth.htm", method = RequestMethod.POST)
	public void adminConfirmGiveMemberAuth(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcMemberId,Long authId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			MaterialCircleMemberEntity mcMember=this.mCMemberService.getObjById(CommUtil.null2Long(mcMemberId));
			//如果管理员权限高于成员的权限
			if(admin.getMcAuthority().getMemberLevel().intValue()<mcMember.getMcAuthority().getMemberLevel().intValue()){
				MCAuthorityEntity mcAuth=this.mCAuthorityService.getObjById(CommUtil.null2Long(authId));
				if(mcAuth!=null){
					if(admin.getMcAuthority().getMemberLevel().intValue()<mcAuth.getMemberLevel().intValue()){
						mcMember.setMcAuthority(mcAuth);
						boolean ret=this.mCMemberService.update(mcMember);
						if(ret){
							ApiUtils.json(response, "", "授予权限成功", 0);
							return;
						}
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足,不能够给自己的上级和平级授权", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员根据自己的权限判断能否移除成员
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminIsCanRemoveMember.htm", method = RequestMethod.POST)
	public void adminIsCanRemoveMember(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcMemberId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			MaterialCircleMemberEntity mcMember=this.mCMemberService.getObjById(CommUtil.null2Long(mcMemberId));
			//如果管理员权限高于成员的权限
			if(admin.getMcAuthority().getMemberLevel().intValue()<mcMember.getMcAuthority().getMemberLevel().intValue()){
				boolean ret=this.mCMemberService.remove(mcMemberId);
				if(ret){
					ApiUtils.json(response, "", "移除成员成功", 0);
					return;
				}
			}else{
				ApiUtils.json(response, "", "权限不足,不能够移除自己的上级和平级授权", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员提醒用户去转发素材圈
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminRemindMemberTranspond.htm", method = RequestMethod.POST)
	public void adminRemindMemberTranspond(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcMemberId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			MaterialCircleMemberEntity mcMember=this.mCMemberService.getObjById(CommUtil.null2Long(mcMemberId));
			//如果管理员权限高于成员的权限
			if(admin.getMcAuthority().getMemberLevel().intValue()<mcMember.getMcAuthority().getMemberLevel().intValue()){
				String recNum=mcMember.getMember().getMobile();
				if("mobile".equals(ApiUtils.judgmentType(CommUtil.null2String(recNum)))){
					JSONObject jsonobj = new JSONObject();
					String templateId = "SMS_136384919";// 短信模板ID
					jsonobj.put("userName", mcMember.getMember().getUserName());
					jsonobj.put("adminName", admin.getMcAuthority().getRankNameExplain()+admin.getMember().getUserName());
					boolean ret=CommUtil.sendNote("", jsonobj, recNum, templateId);
					if(ret){
						ApiUtils.json(response, "", "提醒用户转发成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足,不能够提醒自己的上级和平级", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:用户授权时判断用户是否有权限授权
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/judgeUserIsHasAuth.htm", method = RequestMethod.POST)
	public void judgeUserIsHasAuth(HttpServletRequest request,HttpServletResponse response,
			Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()){
				String hql="select obj from MCAuthorityEntity as obj where obj.memberLevel>"+admin.getMcAuthority().getMemberLevel();
				List<MCAuthorityEntity> authList=this.mCAuthorityService.query(hql, null, -1, -1);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(MCAuthorityEntity.class, "id,rankNameExplain"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, authList, "获取可以授权权限列表成功", 0,filter);
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈管理员", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此授权功能只开放给素材圈管理员", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员搜索没有加入过素材圈的用户
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminSearchNoJoinMcUser.htm", method = RequestMethod.POST)
	public void adminSearchNoJoinMcUser(HttpServletRequest request,HttpServletResponse response,
			Long userId,String searchValue,Integer currentPage){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()){
				String filterCondition="";
				if("mobile".equals(ApiUtils.judgmentType(CommUtil.null2String(searchValue)))){
					filterCondition="mobile like '%"+searchValue+"%'";
				}else if("id".equals(ApiUtils.judgmentType(CommUtil.null2String(searchValue)))){
					filterCondition="id like '%"+searchValue+"%'";
				}else{
					filterCondition="userName like '%"+searchValue+"%'";
				}
				currentPage=CommUtil.null2Int(currentPage);
				Integer pageSize=12;
				String hql="select obj from User as obj where obj."+filterCondition+" and obj.id not in (select obj.member.id from MaterialCircleMemberEntity as obj)";
				List<User> userList=this.userService.query(hql, null, currentPage*pageSize, pageSize);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, userList, "获取可以授权的用户列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈管理员", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈管理员", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈管理员给用户授权
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminAccreditCommonUser.htm", method = RequestMethod.POST)
	public void adminAccreditCommonUser(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long acceptUserId,Long authId){
		if(ApiUtils.is_null(userId+"",acceptUserId+"",authId+"")){
			ApiUtils.json(response, "", "参数缺失,请传递正确合法的参数", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()){
				MCAuthorityEntity mcAuth=this.mCAuthorityService.getObjById(CommUtil.null2Long(authId));
				if(mcAuth!=null){//权限合法
					if(mcAuth.getMemberLevel().intValue()>admin.getMcAuthority().getMemberLevel().intValue()){
						User user=this.userService.getObjById(CommUtil.null2Long(acceptUserId));
						if(user!=null){
							MaterialCircleMemberEntity mcMember=new MaterialCircleMemberEntity(new Date(), false, user, 0L, Short.valueOf(1+""), mcAuth);
							boolean ret=this.mCMemberService.save(mcMember);
							if(ret){
								ApiUtils.json(response, "", "授权成功", 0);
								return;
							}
						}
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈管理员", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈管理员", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈管理员获取推广商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminAcquireGeneralizeGoodsList.htm", method = RequestMethod.POST)
	public void adminAcquireGeneralizeGoodsList(HttpServletRequest request,HttpServletResponse response,
			Long userId,Integer currentPage){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				String hql="select obj from GeneralizeGoods as obj where obj.generalizeGoods.deleteStatus=false and obj.generalizeGoods.goods_status=0 order by obj.addTime desc";
				currentPage=CommUtil.null2Int(currentPage);
				Integer pageSize=12;
				List<GeneralizeGoods> generalizeGoodsList=this.generalizeGoodsService.query(hql, null, currentPage*pageSize, pageSize);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(GeneralizeGoods.class, "id,addTime,generalizeGoods,user"));
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_main_photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, generalizeGoodsList, "获取推广商品列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈高级管理员搜索商品,为新增商品做准备
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/superAdminSearchGoods.htm", method = RequestMethod.POST)
	public void superAdminSearchGoods(HttpServletRequest request,HttpServletResponse response,
			String searchVal,Long userId,Integer currentPgae){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				String filterStr="";
				if("id".equals(ApiUtils.judgmentType(CommUtil.null2String(searchVal)))){
					filterStr="obj.id like'%"+searchVal+"%'";
				}else{
					filterStr="obj.goods_name like'%"+searchVal+"%'";
				}
				String hql="select obj from Goods as obj where obj.deleteStatus=false and obj.goods_status=0 and"+filterStr+" order by obj.addTime desc";
				Integer pageSize=12;
				currentPgae=CommUtil.null2Int(currentPgae);
				List<Goods> goodsList=this.goodsService.query(hql, null, pageSize*currentPgae, pageSize);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_status,goods_main_photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, goodsList, "获取商品列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈高级管理员添加商品作为新的推广商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/superAdminNewAddGeneralizeGoods.htm", method = RequestMethod.POST)
	public void superAdminNewAddGeneralizeGoods(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long goodsId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				String hql="select obj from GeneralizeGoods as obj where obj.generalizeGoods.id="+goodsId;
				List<GeneralizeGoods> retList=this.generalizeGoodsService.query(hql, null, -1, -1);
				if(retList.size()>0){
					ApiUtils.json(response, "", "推广商品中已有该商品,请不要重复添加", 1);
					return;
				}
				User user=this.userService.getObjById(CommUtil.null2Long(userId));
				Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
				if(goods!=null&&!goods.isDeleteStatus()&&goods.getGoods_status()==0){
					GeneralizeGoods generalizeGoods=new GeneralizeGoods();
					generalizeGoods.setUser(user);
					generalizeGoods.setGeneralizeGoods(goods);
					generalizeGoods.setAddTime(new Date());
					generalizeGoods.setDeleteStatus(false);
					boolean ret=this.generalizeGoodsService.save(generalizeGoods);
					if(ret){
						ApiUtils.json(response, "", "添加推广商品成功", 0);
						return;
					}
				}else{
					ApiUtils.json(response, "", "该商品删除或者是下架,不能被添加", 2);
					return;
				}
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈高级管理员移除推广商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteGeneralizeGoods.htm", method = RequestMethod.POST)
	public void deleteGeneralizeGoods(HttpServletRequest request,HttpServletResponse response,
			Long generalizeGoodsId,Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				GeneralizeGoods generalizeGoods=this.generalizeGoodsService.getObjById(CommUtil.null2Long(generalizeGoodsId));
				if(generalizeGoods!=null){
					String sql="update shopping_mc_materialItems as smm set smm.generalizeGoods_id=null where smm.generalizeGoods_id="+generalizeGoods.getId();
					this.commonService.executeNativeSQL(sql);
					boolean ret=this.generalizeGoodsService.remove(CommUtil.null2Long(generalizeGoodsId));
					if(ret){
						ApiUtils.json(response, "", "移除推广商品成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈高级管理员查询标签记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/selectLabelManage.htm", method = RequestMethod.POST)
	public void selectLabelManage(HttpServletRequest request,HttpServletResponse response,
			Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				String hql="select obj from LabelManage as obj order by obj.addTime desc";
				List<LabelManage> outputList=this.labelManageService.query(hql, null, -1, -1);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(LabelManage.class, "id,addTime,lableName,user"));
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, outputList, "获取素材圈标签列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:新增标签
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/newAddMCLabel.htm", method = RequestMethod.POST)
	public void newAddMCLabel(HttpServletRequest request,HttpServletResponse response,
			Long userId,String lableName){
		if(ApiUtils.is_null(userId+"",lableName)){
			ApiUtils.json(response, "", "所需参数不能为空", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				LabelManage existLable=(LabelManage) this.commonService.getByWhere("LabelManage", "obj.lableName='"+lableName+"'");
				if(existLable!=null){
					ApiUtils.json(response, "", "该标签已存在,不能重复新增", 2);
					return;
				}
				LabelManage label=new LabelManage();
				label.setAddTime(new Date());
				label.setDeleteStatus(false);
				label.setLableName(lableName);
				label.setUser(admin.getMember());
				boolean ret=this.labelManageService.save(label);
				if(ret){
					ApiUtils.json(response, "", "新增素材圈标签成功", 0);
					return;
				}
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员删除标签
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteMcLable.htm", method = RequestMethod.POST)
	public void deleteMcLable(HttpServletRequest request,HttpServletResponse response,
			Long lableId,Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				LabelManage label=this.labelManageService.getObjById(CommUtil.null2Long(lableId));
				if(label!=null){
					String sql="update shopping_mc_materialItems as smm set smm.labelManage_id=null where smm.labelManage_id="+label.getId();
					this.commonService.executeNativeSQL(sql);
					boolean ret=this.labelManageService.remove(CommUtil.null2Long(lableId));
					if(ret){
						ApiUtils.json(response, "", "删除标签成功", 0);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员查看举报记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminSelectReportItems.htm", method = RequestMethod.POST)
	public void mcReportManage(HttpServletRequest request,HttpServletResponse response,
			Long userId,Integer currentPage){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				List<MaterialItems> outList=new ArrayList<MaterialItems>();
				MaterialItems materialItems=null;
				Integer pageSize=12;
				currentPage=CommUtil.null2Int(currentPage);
				String sql="SELECT smr.materialItems_id FROM shopping_mc_reportmanage AS smr GROUP BY smr.materialItems_id ORDER BY  COUNT(smr.materialItems_id) DESC LIMIT "+currentPage*pageSize+","+pageSize;
				List<?> idList=this.commonService.executeNativeNamedQuery(sql);
				for(Object id:idList){
					materialItems=this.materialItemsService.getObjById(CommUtil.null2Long(id));
					outList.add(materialItems);
				}
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(MaterialItems.class, "reportItems,topTime,id,addTime,mcTheme,transmitTotalNum,lastTransmitTime,user,materialTpye,generalizeGoods,vPResourceItems"));
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				objs.add(new FilterObj(GeneralizeGoods.class, "generalizeGoods,id,addTime"));
				objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_status,goods_main_photo"));
				objs.add(new FilterObj(VPResource.class, "id,addTime,path,name,coverPhoto"));
				objs.add(new FilterObj(ReportManage.class, "id,addTime,user,reportReason"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, outList, "获取素材圈举报列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:新增举报记录===>权限对于普通用户都可以
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/newAddReportItems.htm", method = RequestMethod.POST)
	public void newAddReportItems(HttpServletRequest request,HttpServletResponse response,
			String reportRes,Long userId,Long mcItemsId){
		if(ApiUtils.is_null(reportRes,userId+"")){
			ApiUtils.json(response, "", "所需参数,不能缺少", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		if(user!=null){
			MaterialItems materialItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			ReportManage repMan=new ReportManage();
			repMan.setAddTime(new Date());
			repMan.setDeleteStatus(false);
			repMan.setReportReason(reportRes);
			repMan.setUser(user);
			repMan.setMaterialItems(materialItems);
			boolean ret=this.reportManageService.save(repMan);
			if(ret){
				String hql="select obj from MaterialCircleMemberEntity as obj where obj.mcAuthority.isAdmin=true";
				List<MaterialCircleMemberEntity> mcMemberList=this.mCMemberService.query(hql, null, -1, -1);
				Random random=new Random();
				MaterialCircleMemberEntity admin=mcMemberList.get(random.nextInt(mcMemberList.size()));
				if(admin!=null){
					String recNum=admin.getMember().getMobile();
					if("mobile".equals(ApiUtils.judgmentType(CommUtil.null2String(recNum)))){
						JSONObject jsonobj = new JSONObject();
						String templateId = "SMS_137689232";// 短信模板ID,举报管理==>向上级推送消息
						jsonobj.put("superiorrName","素材圈"+admin.getMcAuthority().getRankNameExplain()+admin.getMember().getUserName());
						jsonobj.put("userName", user.getUserName());
						CommUtil.sendNote("", jsonobj, recNum, templateId);
					}
				}
			}
			ApiUtils.json(response, "", "举报成功", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员删除举报记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/adminDeleteReportItems.htm", method = RequestMethod.POST)
	public void adminDeleteReportItems(HttpServletRequest request,HttpServletResponse response,
			Long userId,String reportItemIds){
		if(ApiUtils.is_null(reportItemIds)){
			ApiUtils.json(response, "", "reportItemIds参数不能为空", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3){
				JSONArray reportItemIdsArray=JSON.parseArray(CommUtil.null2String(reportItemIds));
				ReportManage repMan=null;
				Integer succNum=0;
				boolean ret=false;
				for(Object id:reportItemIdsArray){
					repMan=this.reportManageService.getObjById(CommUtil.null2Long(id));
					if(repMan!=null){
						ret=this.reportManageService.remove(repMan.getId());
						if(ret){
							succNum++;
						}
					}
				}
				ApiUtils.json(response, "", "移除"+succNum+"条举报记录成功", 0);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈二级管理员以上的级别", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:判断用户是否可以发布素材
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/judgeUserIsCanNewAddMc.htm", method = RequestMethod.POST)
	public void judgeUserIsCanNewAddMc(HttpServletRequest request,HttpServletResponse response,
			Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().getMemberLevel()<=4){
				ApiUtils.json(response, "", "是素材圈成员,可以发布素材",0);
				return;
			}else{
				ApiUtils.json(response, "", "您的申请已经提交,请等待管理员审核", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "此功能只允许素材圈成员以上级别使用,请申请加入素材圈再上传", 2);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈上传素材资源
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/uploadMcResourceVp.htm", method = RequestMethod.POST)
	public void uploadMcResourceVp(HttpServletRequest request,HttpServletResponse response,
			Long userId,String materialTpye){
		boolean judgeType="video".equals(CommUtil.null2String(materialTpye))||"imgText".equals(CommUtil.null2String(materialTpye));
		if(!judgeType){
			ApiUtils.json(response, "", "materialTpye参数不能为空,必须为video或者是imgText", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().getMemberLevel()<=4){
				Long begT=System.currentTimeMillis();
				CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
				if(multipartResolver.isMultipart(request)){//这里判断request请求中是否有文件上传
					User user=this.userService.getObjById(CommUtil.null2Long(userId));
					//声明reqest的多部分的文件,用于批量上传
					MultipartHttpServletRequest multipartRequest= (MultipartHttpServletRequest) request;
					//声明要写入的file对象，指具体的对象
					CommonsMultipartFile file=null;
					Iterator<?> fileNameIter=multipartRequest.getFileNames();
					HttpSession session=request.getSession();
					Map<String, String> mcVpResource=(Map<String, String>) session.getAttribute("mcVpResource");
					String outReptName="";
					String fileType="";
					String resPath="/upload/mcResVp/"+user.getId();//资源的路径
					String folderPath=SystemResPath.imgUploadUrl+resPath;
					String paramsKey="";//传递上来的文件名
					String originalName="";//文件的原始名字
					String extend="";//文件的后缀名
					String resName="";//资源的名字
					String outStr="";
					boolean ret=false;
					Integer succNum=0;
					String surfacePlotName="";
					while(fileNameIter.hasNext()){//如果只有文件名，没有对应的文件内容的话，这里面是不进去的
						paramsKey=(String) fileNameIter.next();
						file = (CommonsMultipartFile) multipartRequest.getFile(paramsKey);
						resName=UUID.randomUUID().toString();
						originalName=file.getOriginalFilename();
						extend=originalName.substring(originalName.lastIndexOf(".") + 1);
						fileType=WriteFileUtils.detectionFileFormat(extend.toUpperCase());
						if("video".equals(CommUtil.null2String(materialTpye))){
							if("pictureFile".equals(fileType)){
								outStr="素材圈视频类型不允许上传图片";
								continue;
							}
						}else if("imgText".equals(CommUtil.null2String(materialTpye))){
							if("videoFile".equals(fileType)){
								outStr="素材圈图文类型不允许上传视频";
								continue;
							}
						}
						if("videoFile".equals(fileType)){
							if(file.getSize()>10485760){//3M==10485760b
								outStr=originalName+"此文件太大，请选择小于10M的视频上传";
								break;
							}
							if(WriteFileUtils.judgeIsContainVv(mcVpResource, Short.valueOf(0+""))){
								outStr="每一个素材只能上传一个视频";
								break;//因为视频只支持一个，所以这里break
							}
							surfacePlotName=resName+".jpg";
						}else if("pictureFile".equals(fileType)){
							if(mcVpResource!=null&&mcVpResource.containsKey(originalName)){
								outReptName=originalName+","+outReptName;
								outStr=outReptName+"等文件不能重复上传，请删除后再上传";
								continue;
							}
							if(mcVpResource!=null&&mcVpResource.size()>10){
								outStr="素材圈最多上传张图片";
								continue;
							}
							surfacePlotName=resName+"_surfacePlot."+extend;
						}
						CommUtil.createFolder(folderPath);//这里要记得先创建文件夹,不然会报错
						//写入原文件
						ret=WriteFileUtils.writeFile(folderPath+"/"+resName+"."+extend, file, file.getSize());
						if(ret){
							VPResource vPResource=new VPResource(resPath, resName+"."+extend, null, file.getSize(), user, fileType, null);
							ret=this.vPResourceService.save(vPResource);
							if(ret){
								if("videoFile".equals(fileType)){//如果是视频文件
									ret=WriteFileUtils.generateSurfacePlot(folderPath+"/"+resName+"."+extend,folderPath+"/"+surfacePlotName, "800*450");
								}else if("pictureFile".equals(fileType)){//如果是图片文件
									ret=WriteFileUtils.writePhotoToServer(folderPath+"/"+resName+"."+extend,folderPath+"/"+surfacePlotName,175,175,true);
								}
								if(ret){
									VPResource surfacePlot=new VPResource(resPath, surfacePlotName, null, file.getSize(), user, "pictureFile", null);
									this.vPResourceService.save(surfacePlot);
									vPResource.setCoverPhoto(surfacePlot);
									this.vPResourceService.update(vPResource);
									if(mcVpResource==null){
										mcVpResource=new HashMap<String, String>();
									}
									mcVpResource.put(originalName, vPResource.getId()+"");
									session.setAttribute("mcVpResource", mcVpResource);
									succNum++;
								}
							}
						}
					}
					if(succNum>0){
						ApiUtils.json(response, "","上传资源"+succNum+"个成功", 0);
					}else{
						ApiUtils.json(response, "",outStr, 1);
					}
					Long endT=System.currentTimeMillis();
					System.out.println("app新增素材"+succNum+"个资源完成,总共耗时"+(endT-begT)+"毫秒");
					return;
				}else{
					ApiUtils.json(response, "", "没有要上传的素材资源", 3);
					return;
				}
			}else{
				ApiUtils.json(response, "", "您还未加入素材圈,您的申请已经提交,请等待管理员审核", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "此功能只允许素材圈成员含成员以上级别使用,请申请加入素材圈再上传", 2);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈删除上传的素材资源
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteMcVpRes.htm", method = RequestMethod.POST)
	public void deleteMcVpRes(HttpServletRequest request,HttpServletResponse response,
			String fileNameArr){
		if(ApiUtils.is_null(fileNameArr)){
			ApiUtils.json(response, "", "所需参数不能为空", 1);
			return;
		}
		Map<String, String> mcVpResource=(Map<String, String>) request.getSession().getAttribute("mcVpResource");
		if(mcVpResource==null){
			ApiUtils.json(response, "", "没有要删除的文件", 1);
			return;
		}
		JSONArray nameArray=JSON.parseArray(fileNameArr);
		String vpResId="";
		VPResource vResource=null;
		File file=null;
		Integer sucNum=0;
		System.out.println(mcVpResource.toString());
		for(Object orgName:nameArray){
			vpResId=mcVpResource.get(orgName);
			vResource=this.vPResourceService.getObjById(CommUtil.null2Long(vpResId));
			if(vResource!=null){
				file=new File(SystemResPath.imgUploadUrl+vResource.getPath()+"/"+vResource.getName());
				if(file!=null&&file.exists()){
					file.delete();//删除原文件
					file=new File(SystemResPath.imgUploadUrl+vResource.getCoverPhoto().getPath()+"/"+vResource.getCoverPhoto().getName());
					file.delete();//删除封面图
					this.vPResourceService.remove(CommUtil.null2Long(vpResId));
					mcVpResource.remove(orgName);
					sucNum++;
				}
			}
		}
		if(sucNum>0){
			ApiUtils.json(response, "", "删除"+sucNum+"个文件成功", 0);
		}else{
			ApiUtils.json(response, "", "没有要删除的文件", 1);
		}
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:成员含成员以上级别获取推广商品列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/memberGetGeneralizeGoods.htm", method = RequestMethod.POST)
	public void memberGetGeneralizeGoods(HttpServletRequest request,HttpServletResponse response,
			Long userId,Integer currentPage){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().getMemberLevel()<=4){
				String hql="select obj from GeneralizeGoods as obj where obj.generalizeGoods.deleteStatus=false and obj.generalizeGoods.goods_status=0 order by obj.addTime desc";
				currentPage=CommUtil.null2Int(currentPage);
				Integer pageSize=12;
				List<GeneralizeGoods> generalizeGoodsList=this.generalizeGoodsService.query(hql, null, currentPage*pageSize, pageSize);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(GeneralizeGoods.class, "id,addTime,generalizeGoods,user"));
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_main_photo"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, generalizeGoodsList, "获取推广商品列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈成员含成员以上开放", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈成员含成员以上开放", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:成员含成员以上级别获取素材圈标签列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/memberGetMcLable.htm", method = RequestMethod.POST)
	public void memberGetMcLable(HttpServletRequest request,HttpServletResponse response,
			Long userId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().getMemberLevel()<=4){
				String hql="select obj from LabelManage as obj order by obj.addTime desc";
				List<LabelManage> outputList=this.labelManageService.query(hql, null, -1, -1);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(LabelManage.class, "id,addTime,lableName,user"));
				objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
				objs.add(new FilterObj(Accessory.class, "path,name"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, outputList, "获取素材圈标签列表成功", 0,filter);
				return;
			}else{
				ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈成员含成员以上开放", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,此功能只开放给素材圈成员含成员以上开放", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈新增素材条目
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/newAddMcMaterialItems.htm", method = RequestMethod.POST)
	public void newAddMcMaterialItems(HttpServletRequest request,HttpServletResponse response,
			String mcTheme,String userId,String materialTpye,Long generalizeGoodsId,Long labelManageId){
		if(ApiUtils.is_null(mcTheme,userId,materialTpye)){
			ApiUtils.json(response, "", "mcTheme,userId,materialTpye等参数不能为空", 1);
			return;
		}
		boolean judgeType="video".equals(CommUtil.null2String(materialTpye))||"imgText".equals(CommUtil.null2String(materialTpye));
		if(!judgeType){
			ApiUtils.json(response, "", "materialTpye参数必须为video或者是imgText", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			if(admin.getMcAuthority()!=null&&admin.getMcAuthority().getMemberLevel()<=4){
				Map<String, String> mcVpResource=(Map<String, String>) request.getSession().getAttribute("mcVpResource");
				if(mcVpResource==null){
					ApiUtils.json(response, "", "发布素材会话超时,素材记录必须要带有图片或者是视频", 1);
					return;
				}
				MaterialItems materialItems=new MaterialItems();
				GeneralizeGoods generalizeGoods=this.generalizeGoodsService.getObjById(CommUtil.null2Long(generalizeGoodsId));
				if(generalizeGoods!=null){
					materialItems.setGeneralizeGoods(generalizeGoods);
				}
				LabelManage labelManage=this.labelManageService.getObjById(CommUtil.null2Long(labelManageId));
				if(labelManage!=null){
					materialItems.setLabelManage(labelManage);
				}
				materialItems.setAddTime(new Date());
				materialItems.setDeleteStatus(false);
				if(mcTheme.length()>50){
					mcTheme=mcTheme.substring(0, 50);
				}
				materialItems.setMcTheme(mcTheme);
				materialItems.setMaterialTpye(materialTpye);
				materialItems.setUser(admin.getMember());
				materialItems.setTransmitTotalNum(0L);//转发总次数
				boolean ret=this.materialItemsService.save(materialItems);
				if(ret){
					VPResource vPResource=null;
					Entry<String, String> entry=null;
					Iterator<Entry<String, String>> mcKv=mcVpResource.entrySet().iterator();
					while(mcKv.hasNext()){
						entry=mcKv.next();
						vPResource=this.vPResourceService.getObjById(CommUtil.null2Long(entry.getValue()));
						vPResource.setMaterialItems(materialItems);
						vPResource.setDeleteStatus(false);
						this.vPResourceService.update(vPResource);
						vPResource=vPResource.getCoverPhoto();//变量复用
						vPResource.setDeleteStatus(false);
						this.vPResourceService.update(vPResource);
					}
					request.getSession().removeAttribute("mcVpResource");
					ApiUtils.json(response, "", "新增素材成功", 0);
					return;
				}
			}else{
				ApiUtils.json(response, "", "您的申请已经提交,请等待管理员审核", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "此功能只允许素材圈成员以上级别使用,请申请加入素材圈再上传", 2);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈查询素材条目
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/queryMcItems.htm", method = RequestMethod.POST)
	public void queryMcItems(HttpServletRequest request,HttpServletResponse response,
			Integer currentPage,Long lableId,String materialTpye,String searchVal){
		String hql="";
		if(!"".equals(CommUtil.null2String(lableId))){
			hql="select obj from MaterialItems as obj where obj.labelManage.id="+lableId+" group by obj.id,obj.topTime order by obj.topTime desc,obj.addTime desc";
		}else if(!"".equals(CommUtil.null2String(materialTpye))){
			hql="select obj from MaterialItems as obj where obj.materialTpye="+materialTpye+" group by obj.id,obj.topTime order by obj.topTime desc,obj.addTime desc";
		}else if(!"".equals(CommUtil.null2String(searchVal))){
			hql="select obj from MaterialItems as obj where obj.mcTheme like '%"+searchVal+"%' group by obj.id,obj.topTime order by obj.topTime desc,obj.addTime desc";
		}else{
			hql="select obj from MaterialItems as obj group by obj.id,obj.topTime order by obj.topTime desc,obj.addTime desc";
		}
		Integer pageSize=12;
		currentPage=CommUtil.null2Int(currentPage);
		List<MaterialItems> mcPutList=this.materialItemsService.query(hql, null, currentPage*pageSize, pageSize);
		McTempOut mcTemp=new McTempOut();
		mcTemp.setMcItems(mcPutList);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(McTempOut.class, "mcItems,lb"));
		objs.add(new FilterObj(MaterialItems.class, "topTime,id,addTime,mcTheme,transmitTotalNum,lastTransmitTime,user,materialTpye,generalizeGoods,vPResourceItems"));
		objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(GeneralizeGoods.class, "generalizeGoods,id,addTime"));
		objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_status,goods_main_photo"));
		objs.add(new FilterObj(VPResource.class, "id,addTime,path,name,coverPhoto"));
		boolean isQueryLable=currentPage==0&&"".equals(CommUtil.null2String(lableId))&&"".equals(CommUtil.null2String(materialTpye))&&"".equals(CommUtil.null2String(searchVal));
		if(isQueryLable){
			String labelHql="select obj from LabelManage as obj order by obj.addTime desc";
			List<LabelManage> labelList=this.labelManageService.query(labelHql, null, -1, -1);
			mcTemp.setLb(labelList);
			objs.add(new FilterObj(LabelManage.class, "id,lableName"));
		}
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, mcTemp, "获取素材圈条目列表成功", 0,filter);
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈编辑素材时对资源的删除
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteVpResource.htm", method = RequestMethod.POST)
	public void deleteVpResource(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcItemsId,String mcVpIds){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			boolean isAdmin=admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3;
			boolean isMcOwer=mcItems.getUser().getId().longValue()==CommUtil.null2Long(userId);
			if(isAdmin||isMcOwer){
				VPResource vPResource=null;
				File file=null;
				Integer sucNum=0;
				JSONArray mcVpIdsArray=JSON.parseArray(CommUtil.null2String(mcVpIds));
				for(Object mcVpId:mcVpIdsArray){
					vPResource=this.vPResourceService.getObjById(CommUtil.null2Long(mcVpId));
					file=new File(SystemResPath.imgUploadUrl+vPResource.getPath()+"/"+vPResource.getName());
					if(file!=null&&file.exists()){
						file.delete();//删除原文件
						file=new File(SystemResPath.imgUploadUrl+vPResource.getCoverPhoto().getPath()+"/"+vPResource.getCoverPhoto().getName());
						file.delete();//删除封面图
						this.vPResourceService.remove(CommUtil.null2Long(mcVpId));
						sucNum++;
					}
				}
				ApiUtils.json(response, "", "删除成功"+sucNum+"个资源", 0);
				return;
			}else{//这里防止俩部分人1,素材圈成员编辑不是自己发布的素材2,加入素材圈未经过审核的
				ApiUtils.json(response, "", "权限不足,成员以上级别可以修改任意素材,成员只能修改自己发布的素材", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,请申请加入素材圈", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:编辑素材条目时对素材资源的新增
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/addMcVpResource.htm", method = RequestMethod.POST)
	public void addMcVpResource(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcItemsId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			boolean isAdmin=admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3;
			boolean isMcOwer=mcItems.getUser().getId().longValue()==CommUtil.null2Long(userId);
			if(isAdmin||isMcOwer){
				CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
				if(multipartResolver.isMultipart(request)){//这里判断request请求中是否有文件上传
					User user=this.userService.getObjById(CommUtil.null2Long(userId));
					//声明reqest的多部分的文件,用于批量上传
					MultipartHttpServletRequest multipartRequest= (MultipartHttpServletRequest) request;
					//声明要写入的file对象，指具体的对象
					CommonsMultipartFile file=null;
					Iterator<?> fileNameIter=multipartRequest.getFileNames();
					String fileType="";
					String resPath="/upload/mcResVp/"+user.getId();//资源的路径
					String folderPath=SystemResPath.imgUploadUrl+resPath;
					String paramsKey="";//传递上来的文件名
					String originalName="";//文件的原始名字
					String extend="";//文件的后缀名
					String resName="";//资源的名字
					String outStr="";
					boolean ret=false;
					Integer succNum=0;
					String surfacePlotName="";
					while(fileNameIter.hasNext()){//如果只有文件名，没有对应的文件内容的话，这里面是不进去的
						paramsKey=(String) fileNameIter.next();
						file = (CommonsMultipartFile) multipartRequest.getFile(paramsKey);
						resName=UUID.randomUUID().toString();
						originalName=file.getOriginalFilename();
						extend=originalName.substring(originalName.lastIndexOf(".") + 1);
						fileType=WriteFileUtils.detectionFileFormat(extend.toUpperCase());
						if("video".equals(mcItems.getMaterialTpye())){
							if("pictureFile".equals(fileType)){
								outStr="素材圈视频类型不允许上传图片";
								continue;
							}
						}else if("imgText".equals(mcItems.getMaterialTpye())){
							if("videoFile".equals(fileType)){
								outStr="素材圈图文类型不允许上传视频";
								continue;
							}
						}
						if("videoFile".equals(fileType)){
							if(file.getSize()>10485760){//3M==10485760b
								outStr=originalName+"此文件太大，请选择小于10M的视频上传";
								break;
							}
							if(mcItems.getvPResourceItems().size()>=1){
								outStr=originalName+"一个素材允许只有一个视频";
								break;
							}
							surfacePlotName=resName+".jpg";
						}else if("pictureFile".equals(fileType)){
							if(mcItems.getvPResourceItems().size()>=10){
								outStr=originalName+"一个素材允许最多只能有10张图片";
								continue;
							}
							surfacePlotName=resName+"_surfacePlot."+extend;
						}
						CommUtil.createFolder(folderPath);//这里要记得先创建文件夹,不然会报错
						//写入原文件
						ret=WriteFileUtils.writeFile(folderPath+"/"+resName+"."+extend, file, file.getSize());
						if(ret){
							VPResource vPResource=new VPResource(resPath, resName+"."+extend, null, file.getSize(), user, fileType, null);
							vPResource.setDeleteStatus(false);
							ret=this.vPResourceService.save(vPResource);
							if(ret){
								if("videoFile".equals(fileType)){//如果是视频文件
									ret=WriteFileUtils.generateSurfacePlot(folderPath+"/"+resName+"."+extend,folderPath+"/"+surfacePlotName, "800*450");
								}else if("pictureFile".equals(fileType)){//如果是图片文件
									ret=WriteFileUtils.writePhotoToServer(folderPath+"/"+resName+"."+extend,folderPath+"/"+surfacePlotName,175,175,true);
								}
								if(ret){
									VPResource surfacePlot=new VPResource(resPath, surfacePlotName, null, file.getSize(), user, "pictureFile", null);
									surfacePlot.setDeleteStatus(false);
									this.vPResourceService.save(surfacePlot);
									vPResource.setMaterialItems(mcItems);//绑定素材条目
									vPResource.setCoverPhoto(surfacePlot);//绑定封面图
									this.vPResourceService.update(vPResource);
									succNum++;
								}
							}
						}
					}
					if(succNum>0){
						ApiUtils.json(response, "","上传资源"+succNum+"个成功", 0);
					}else{
						ApiUtils.json(response, "",outStr, 1);
					}
					return;
				}
			}else{//这里防止俩部分人1,素材圈成员编辑不是自己发布的素材2,加入素材圈未经过审核的
				ApiUtils.json(response, "", "权限不足,成员以上级别可以修改任意素材,成员只能修改自己发布的素材", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,请申请加入素材圈", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈编辑素材条目完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/editMcItemsSucc.htm", method = RequestMethod.POST)
	public void editMcItemsSucc(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcItemsId,Long generalizeGoodsId,Long labelManageId,
			String mcTheme){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			boolean isAdmin=admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3;
			boolean isMcOwer=mcItems.getUser().getId().longValue()==CommUtil.null2Long(userId);
			if(isAdmin||isMcOwer){
				if(ApiUtils.is_null(mcTheme)){
					ApiUtils.json(response, "", "素材圈主题不允许为空", 1);
					return;
				}
				GeneralizeGoods generalizeGoods=this.generalizeGoodsService.getObjById(CommUtil.null2Long(generalizeGoodsId));
				mcItems.setGeneralizeGoods(generalizeGoods);
				LabelManage labelManage=this.labelManageService.getObjById(CommUtil.null2Long(labelManageId));
				mcItems.setLabelManage(labelManage);
				mcTheme=CommUtil.null2String(mcTheme);
				if(mcTheme.length()>50){
					mcTheme=mcTheme.substring(0, 50);
				}
				mcItems.setMcTheme(mcTheme);//如果传值上来
				boolean ret=this.materialItemsService.update(mcItems);
				if(ret){
					ApiUtils.json(response, "", "更新素材圈条目成功", 0);
					return;
				}
			}else{//这里防止俩部分人1,素材圈成员编辑不是自己发布的素材2,加入素材圈未经过审核的
				ApiUtils.json(response, "", "权限不足,成员以上级别可以修改任意素材,成员只能修改自己发布的素材", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,请申请加入素材圈", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈删除素材条目完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteMcItem.htm", method = RequestMethod.POST)
	public void deleteMcItem(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcItemsId){
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			boolean isAdmin=admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3;
			boolean isMcOwer=mcItems.getUser().getId().longValue()==CommUtil.null2Long(userId);
			if(isAdmin||isMcOwer){
				List<VPResource> vPResList=mcItems.getvPResourceItems();
				File file=null;
				for(VPResource vPResource:vPResList){
					file=new File(SystemResPath.imgUploadUrl+vPResource.getPath()+"/"+vPResource.getName());
					if(file!=null&&file.exists()){
						file.delete();//删除原文件
						file=new File(SystemResPath.imgUploadUrl+vPResource.getCoverPhoto().getPath()+"/"+vPResource.getCoverPhoto().getName());
						file.delete();//删除封面图
					}
				}
				boolean ret=this.materialItemsService.remove(CommUtil.null2Long(mcItemsId));
				if(ret){
					ApiUtils.json(response, "", "删除成功", 0);
					return;
				}
			}else{//这里防止俩部分人1,素材圈成员编辑不是自己发布的素材2,加入素材圈未经过审核的
				ApiUtils.json(response, "", "权限不足,成员以上级别可以修改任意素材,成员只能修改自己发布的素材", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,请申请加入素材圈", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:管理员置顶素材
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/mangerTopMcItems.htm", method = RequestMethod.POST)
	public void mangerTopMcItems(HttpServletRequest request,HttpServletResponse response,
			Long userId,Long mcItemsId,Boolean topStatus){
		if(topStatus==null){
			ApiUtils.json(response, "", "topStatus所需参数不能为空", 1);
			return;
		}
		MaterialCircleMemberEntity admin=(MaterialCircleMemberEntity) this.commonService.getByWhere("MaterialCircleMemberEntity", "obj.member.id="+CommUtil.null2Long(userId));
		if(admin!=null){//此功能只开放给素材圈二级管理以上的级别
			MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
			boolean isAdmin=admin.getMcAuthority()!=null&&admin.getMcAuthority().isAdmin()&&admin.getMcAuthority().getMemberLevel()<3;
			if(isAdmin){//如果是管理员的话,才可以对素材进行置顶
				if(topStatus){//===>如果是置顶的话,那么更新时间
					mcItems.setTopTime(new Date());
				}else{
					mcItems.setTopTime(null);
				}
				boolean ret=this.materialItemsService.update(mcItems);
				if(ret){
					ApiUtils.json(response, "", "修改置顶成功", 0);
					return;
				}
			}else{//这里防止俩部分人1,素材圈成员编辑不是自己发布的素材2,加入素材圈未经过审核的
				ApiUtils.json(response, "", "权限不足,此功能只开放给成员以上的级别", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "权限不足,请申请加入素材圈", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:素材圈转发
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/transpondMcItemsRes.htm", method = RequestMethod.POST)
	public void transpondMcItemsRes(HttpServletRequest request,HttpServletResponse response,
			String userId,String mcItemsId){
		if(ApiUtils.is_null(userId,mcItemsId)){
			ApiUtils.json(response, "", "userId,mcItemsId等参数不能为空", 1);
			return;
		}
		MaterialItems mcItems=this.materialItemsService.getObjById(CommUtil.null2Long(mcItemsId));
		if(mcItems!=null){
			mcItems.setTransmitTotalNum(CommUtil.null2Long(mcItems.getTransmitTotalNum())+1);
			mcItems.setLastTransmitTime(new Date());
			this.materialItemsService.update(mcItems);
		}else{
			ApiUtils.json(response, "", "mcItemsId参数不正确", 1);
			return;
		}
		TranspondRecord transpondRecord=new TranspondRecord();
		transpondRecord.setAddTime(new Date());
		transpondRecord.setDeleteStatus(false);
		transpondRecord.setMaterialItems(mcItems);
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		if(user!=null){
			transpondRecord.setUser(user);
		}
		boolean ret=this.transpondRecordService.save(transpondRecord);
		if(ret){
			ApiUtils.json(response, "", "转发成功", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取转发记录
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/acquireTranspondRecord.htm", method = RequestMethod.POST)
	public void acquireTranspondRecord(HttpServletRequest request,HttpServletResponse response,
			Integer currentPage){
		currentPage=CommUtil.null2Int(currentPage);
		Integer pageSize=2;
		String hql="select obj from TranspondRecord as obj group by obj.materialItems.id having obj.materialItems is not null order by obj.materialItems.topTime desc,obj.addTime desc";
		List<TranspondRecord> transpondRecordList=this.transpondRecordService.query(hql, null, pageSize*currentPage, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(TranspondRecord.class, "addTime,materialItems"));
		objs.add(new FilterObj(MaterialItems.class, "topTime,id,addTime,mcTheme,transmitTotalNum,lastTransmitTime,user,materialTpye,generalizeGoods,vPResourceItems"));
		objs.add(new FilterObj(User.class, "id,userName,photo,addTime"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(GeneralizeGoods.class, "generalizeGoods,id,addTime"));
		objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_status,goods_main_photo"));
		objs.add(new FilterObj(VPResource.class, "id,addTime,path,name,coverPhoto"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, transpondRecordList, "获取素材圈条目列表成功", 0,filter);
	}
}

