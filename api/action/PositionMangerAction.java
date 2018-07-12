package com.shopping.api.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.AreaGradeOfUser;
import com.shopping.api.domain.ZhiWeiRecoderEntity;
import com.shopping.api.domain.ZhiXianEntity;
import com.shopping.api.domain.ZhiXianRecorderEntity;
import com.shopping.api.domain.errorUser.UserPositionError;
import com.shopping.api.domain.power.DepartmentPower;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.rank.UserRankName;
import com.shopping.api.domain.userAttribute.AppClickNum;
import com.shopping.api.domain.userBill.UserWeekActivity;
import com.shopping.api.domain.zhiwei.DeputyPosition;
import com.shopping.api.output.AppTransferData;
import com.shopping.api.output.UserTemp;
import com.shopping.api.output.UserTempData;
import com.shopping.api.output.ZhiWeiTemp;
import com.shopping.api.service.IGroupApiService;
import com.shopping.api.service.IMyTeamService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.BuMen;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.ZhiWei;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IRoleService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
/***
 *@author:akangah
 *@description:职位职衔管理控制器
 ***/
@Controller
public class PositionMangerAction{
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
	/***
	 *@author:akangah
	 *@return:void
	 *@param:userId:用户的id
	 *@description:集团领导和总指挥修改指定的用户部门
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_update_userDepartment.htm", method = RequestMethod.POST)
	public void app_update_userDepartment(HttpServletRequest request,
			HttpServletResponse response,Long userId,Long acceptUserId,
			String bumenId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		User demotion_user=this.userService.getObjById(acceptUserId);
		String bumenGroupId="";
		BuMen bumen=(BuMen) this.commonService.getById("BuMen", bumenId);
		ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
		if(demotion_user==null){
			ApiUtils.json(response, "", "接受用户不存在", 1);
			return;
		}
		if(bumen==null){
			ApiUtils.json(response, "", "授予的部门不存在", 1);
			return;
		}
		if(demotion_user.getBumen()!=null){
			bumenGroupId=demotion_user.getBumen().getGroup_id();
		}
		if(user.getZhiwei()!=null){
			long zhiweiId=user.getZhiwei().getId();
			if(zhiweiId==109||zhiweiId==300){
				demotion_user.setBumen(bumen);
				demotion_user.setAreaGradeOfUser(null);
				demotion_user.setZhiwei(zhiwei);
				demotion_user.setIs_yet_add_group(0);
				if(!"".equals(demotion_user.getSelf_group_id())&&demotion_user.getSelf_group_id()!=null){
					JSONObject jsonobject=CommUtil.delete_group(demotion_user.getSelf_group_id());
					boolean is_ok=jsonobject.getJSONObject("data").getBoolean("success");
					if(is_ok){ 
						demotion_user.setSelf_group_id("");
					}
				}
				boolean ret=this.userService.update(demotion_user);
				if(ret){
					if(demotion_user.getIs_yet_add_group()==0){
						JSONObject jsobject=CommUtil.add_group_member(demotion_user.getBumen().getGroup_id().toString(), demotion_user.getId().toString());
						boolean is_succ=false;
						if(jsobject.getJSONObject("data")!=null){
							is_succ=jsobject.getJSONObject("data").getBoolean("result");
						}
						if(is_succ){
							user.setIs_yet_add_group(1);
							this.userService.update(demotion_user);
						}
					}//移除群组成员[单个]
					CommUtil.app_remove_groupMember(bumenGroupId, demotion_user.getId().toString());
					ZhiWeiRecoderEntity zre=new ZhiWeiRecoderEntity();
					zre.setAddTime(new Date());
					zre.setDeleteStatus(false);
					zre.setUser(user);
					zre.setMyselfUser(demotion_user);
					zre.setZhiwei(zhiwei);
					zre.setMsg(zhiwei.getName());
					this.commonService.save(zre);
					ApiUtils.json(response, "", "修改部门成功", 0);
					return;
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:userId:用户的id
	 *@description:集团领导和总指挥能获取的部门,并为修改部门做铺垫
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_obtain_department.htm", method = RequestMethod.POST)
	public void app_obtain_department(HttpServletRequest request,
			HttpServletResponse response,Long userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		if(user.getZhiwei()!=null){
			String hql="";
			Long zhiweiId=user.getZhiwei().getId();
			if(zhiweiId==109){
				hql="select obj from BuMen as obj";
			}
			if(zhiweiId==300){
				String ids=ApiUtils.getSubclassBumenIds(user.getBumen().getId(),commonService);
				hql="select obj from BuMen as obj where obj.id in(" + ids + ")";
			}
			if(zhiweiId==109||zhiweiId==300){
				List<?> bumen_list=this.commonService.query(hql, null, -1,-1);
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(BuMen.class, "id,name,sort,group_id"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, bumen_list, "查询成功", 0, filter);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id,current_page:当前页,beginTime:开始时间,endTime:结束时间
	 *@description:获取用户的职位明细
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/get_zhiwei_mingxi.htm", method = RequestMethod.POST)
	public void get_zhiwei_mingxi(HttpServletRequest request,
			HttpServletResponse response,String user_id,String current_page,
			String beginTime,String endTime){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		int pageSize=20;
		int currentPage=0;
		String hql=new String();
		List<?> output=new ArrayList<ZhiWeiRecoderEntity>();
		if("".equals(current_page)||current_page==null){
			currentPage=0;
		}else{
			currentPage=Integer.valueOf(current_page).intValue();
		}
		if(beginTime==null&&endTime==null){
			hql="select obj from ZhiWeiRecoderEntity as obj where obj.myselfUser.id="+user_id+" order by obj.addTime desc ";
		}
		if(beginTime!=null&&endTime!=null){
			hql="select obj from ZhiWeiRecoderEntity as obj where obj.myselfUser.id="+user_id+" and obj.addTime>'"+beginTime+"' and obj.addTime<'"+endTime+"' order by obj.addTime desc ";
		}
		output=this.commonService.query(hql, null, currentPage*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "userName,areaGradeOfUser,zhiwei,bumen"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(ZhiWeiRecoderEntity.class, "id,addTime,msg,user,myselfUser"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, output, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id,current_page:当前页,beginTime:开始时间,endTime:结束时间
	 *@description:获取用户的职衔明细
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/get_zhixian_mingxi.htm", method = RequestMethod.POST)
	public void get_zhixian_mingxi(HttpServletRequest request,
			HttpServletResponse response,String user_id,String current_page,
			String beginTime,String endTime){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		int pageSize=20;
		int currentPage=0;
		String hql=new String();
		List<?> output=new ArrayList<ZhiXianRecorderEntity>();
		if("".equals(current_page)||current_page==null){
			currentPage=0;
		}else{
			currentPage=Integer.valueOf(current_page).intValue();
		}
		if(beginTime==null&&endTime==null){
			hql="select obj from ZhiXianRecorderEntity as obj where obj.myselfUser.id="+user_id+" order by obj.addTime desc ";
		}
		if(beginTime!=null&&endTime!=null){
			hql="select obj from ZhiXianRecorderEntity as obj where obj.myselfUser.id="+user_id+" and obj.addTime>'"+beginTime+"' and obj.addTime<'"+endTime+"' order by obj.addTime desc ";
		}
		output=this.commonService.query(hql, null, currentPage*pageSize, pageSize);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "userName,areaGradeOfUser,zhixian"));
		objs.add(new FilterObj(ZhiXianRecorderEntity.class, "id,addTime,msg,user,myselfUser"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, output, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@function:标记用户为vip会员,具有该标记的会员职位保持不动
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/markUserToVip.htm", method = RequestMethod.POST)
	public void markUserToVip(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		user_id=CommUtil.null2String(user_id);
		if(user_id.equals("")){
			ApiUtils.json(response, "", "请输入相关用户万手号", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(user!=null){
			user.setIs_vipOfUser(1);
			boolean ret=this.userService.update(user);
			if(ret){
				ApiUtils.json(response, "", "标记会员成功", 0);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取用户自己的信息和上级的信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:职位id:109 创始人职位
	 *				 #2:职位id:121,122,123,124,130集团总指挥职位
	 *				 #3:执行总裁职位(职位id:17  十八部门   执行总裁)
	 *				 #4:大区总裁职位(职位id:120 八大区   总裁)
	 *				 #5:总裁职位(职位id:14  34省          总裁)
	 *				 #6:总经理职位(职位id:11 市          总经理)
	 *				 #7:执行总监职位(职位id:8  区县	 执行总监)
	 *				 #8:运营总监职位(职位id:5  乡镇		运营总监)
	 *				 #9:团队长(职位id:3  村庄  	团队长)
	 *				 #10:将帅学院分院长(职位id:125  五大院(部门) 将帅学院分院长)
	 *				 #11:教委主任(职位id:126  八大区  	教委主任)
	 *				 #12:大队长(职位id:127  34省  	大队长)
	 *				 #13:教导员(职位id:128  市  	教导员)
	 *				 #14:教官(职位id:129  区县  	教官)
	 *				 #15:助教(职位id:131  乡镇)
	 *				 #16:无职位
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "得到自己和上级的用户信息", value = "/get_user_position_info.htm*", rtype = "seller", rname = "出售中的商品", rcode = "get_user_position_info", rgroup = "商品管理")
	@RequestMapping(value = "/get_user_position_info.htm", method = RequestMethod.POST)
	public void get_user_position_info(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		if(user.getZhiwei()==null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
			this.userService.update(user);
		}
		if(user.getBumen()==null){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
			this.userService.update(user);
		}
		User superior = null;
		long zhiwei_id=user.getZhiwei().getId();
		long bumen_id=user.getBumen().getId();
		if(zhiwei_id==109){//#1
			superior=null;
		}else if(zhiwei_id==300||zhiwei_id==124){//#2
			superior=this.userService.getObjById(1L);
		}else if(zhiwei_id==17){//#3
			String sql="SELECT obj.superiorBumen_id FROM ecm_bumen as obj WHERE obj.id = " + bumen_id;
			List<?> ids = commonService.executeNativeNamedQuery(sql);
			if (ids.size()>0) {
				String hql="select obj from User as obj where obj.zhiwei.id=300 and obj.bumen.id="+ ids.get(0);
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else {
				superior=null;
			}
		}else if(zhiwei_id==120){//#4
			String hql="select obj from User as obj where obj.zhiwei.id=17 and obj.bumen.id="+bumen_id;
			List<User> user_superior=this.userService.query(hql, null, -1, -1);
			if(!user_superior.isEmpty()){
				superior=user_superior.get(0);
			}
		}else if(zhiwei_id==14){//#5
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=120 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==11){//#6
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=14 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==8){//#7
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=11 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==5){//#8
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=8 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==3){//#9
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=5 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==125){//#10
			if(bumen_id==506||bumen_id==507||bumen_id==508||bumen_id==509||bumen_id==510){
				String hql="select obj from User as obj where obj.zhiwei.id=124";
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}
		}else if(zhiwei_id==126){//#11
			String hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id="+bumen_id;
			List<User> user_superior=this.userService.query(hql, null, -1, -1);
			if(!user_superior.isEmpty()){
				superior=user_superior.get(0);
			}
		}else if(zhiwei_id==127){//#12
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==128){//#13
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==129){//#14
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else if(zhiwei_id==131){//#15
			if(user.getAreaGradeOfUser()!=null){
				String hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getPid();
				List<User> user_superior=this.userService.query(hql, null, -1, -1);
				if(!user_superior.isEmpty()){
					superior=user_superior.get(0);
				}
			}else{
				superior=null;
			}
		}else{//#16
			superior=null;
		}
		List<User> out_put_list=new ArrayList<User>();
		out_put_list.add(user);
		out_put_list.add(superior);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(User.class, "id,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put_list, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:获取用户所属下级的信息,旧接口，淘汰
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:职位id:109创始人职位看到自己的下级
	 *				 #2:职位id:121四集团总指挥职位看到自己的下级
	 *				 #3:职位id:122六集团总指挥职位看到自己的下级
	 *				 #4:职位id:123八集团总指挥职位看到自己的下级
	 *				 #5:执行总裁职位(职位id:17  十八部门   执行总裁)看到自己的下级
	 *				 #6:大区总裁职位(职位id:120 八大区   总裁)看到自己的下级
	 *				 #7:总裁职位(职位id:14 34省          总裁)看到自己的下级
	 *				 #8:总经理职位(职位id:11 市          总经理)看到自己的下级
	 *				 #9:执行总监职位(职位id:8 区县	 执行总监)看到自己的下级
	 *				 #10:运营总监职位(职位id:5  乡镇	运营总监)看到自己的下级
	 *				 #11:团队长(职位id:3  村庄  	团队长)看到自己的下级
	 *				 #12:将帅学院院长(职位id:124) 将帅学院院长看到自己的下级
	 *				 #13:将帅学院分院长(职位id:125  五大院(部门) 将帅学院分院长)看到自己的下级
	 *				 #14:教委主任(职位id:126  八大区  	教委主任)看到自己的下级
	 *				 #15:大队长(职位id:127  34省  	大队长)看到自己的下级
	 *				 #16:教导员(职位id:128  市  	教导员)看到自己的下级
	 *				 #17:教官(职位id:129  区县  	教官)看到自己的下级
	 *				 #18:职位id:130一集团总指挥职位看到自己的下级
	 *				 #19:助教(职位id:131  乡镇)看到自己的下级
	 *				 #20:无职位看到的用户
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/get_self_subordinate.htm",method = RequestMethod.POST)
	public void get_self_subordinate(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
//		if(!"".equals(CommUtil.null2String(user_id))){
//			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
//			return;
//		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		if(user.getZhiwei()==null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
			this.userService.update(user);
		}
		if(user.getBumen()==null){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
			this.userService.update(user);
		}
		List<User> deputyPositionList=new ArrayList<User>();;
		List<User> subordinate_list=new ArrayList<User>();;
		String hql="";
		String deputyPositionHql="";
		long zhiwei_id=user.getZhiwei().getId();
		long bumen_id=user.getBumen().getId();
		if(zhiwei_id==109){//#1
			hql="select obj from User as obj where obj.zhiwei.id in (121,122,123,124,130) order by obj.zhiwei.sequence asc";
			deputyPositionHql="";
		}else if(zhiwei_id==121){//#2
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (103,106,104,504,102,101)";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id IN (137,138,139,140,141)";
		}else if(137<=zhiwei_id&&zhiwei_id<=141){
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (103,106,104,504)";
		}else if(zhiwei_id==122){//#3
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (1,105,502,3,501,503)";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id IN (142,143,144,145,146)";
		}else if(142<=zhiwei_id&&zhiwei_id<=146){
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (1,105,502,3,501,503)";
		}else if(zhiwei_id==123){//#4
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (2,4,5,302,401,107)";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id IN (147,148,149,150,151)";
		}else if(147<=zhiwei_id&&zhiwei_id<=151){
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (2,4,5,102,101,302,401,107)";
		}else if(zhiwei_id==17){//#5
			hql="select obj from User as obj where obj.zhiwei.id IN (120) and obj.bumen.id="+bumen_id;
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id IN (152,153,154,155,156) and obj.bumen.id="+bumen_id;
		}else if(152<=zhiwei_id&&zhiwei_id<=156){
			hql="select obj from User as obj where obj.zhiwei.id IN (120) and obj.bumen.id="+bumen_id;
		}else if(zhiwei_id==120){//#6
			hql="select obj from User as obj where obj.zhiwei.id in (14) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (157,158,159,160,161) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(157<=zhiwei_id&&zhiwei_id<=161){
			hql="select obj from User as obj where obj.zhiwei.id in (14) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==14){//#7
			hql="select obj from User as obj where obj.zhiwei.id in (11) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (162,163,164,165,166) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(162<=zhiwei_id&&zhiwei_id<=166){
			hql="select obj from User as obj where obj.zhiwei.id in (11) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==11){//#8
			hql="select obj from User as obj where obj.zhiwei.id in (8) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (167,168,169,170,171) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(167<=zhiwei_id&&zhiwei_id<=171){
			hql="select obj from User as obj where obj.zhiwei.id in (8) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==8){//#9
			hql="select obj from User as obj where obj.zhiwei.id in (5) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (172,173,174,175,176) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(172<=zhiwei_id&&zhiwei_id<=176){
			hql="select obj from User as obj where obj.zhiwei.id in (5) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==5){//#10
			hql="select obj from User as obj where obj.zhiwei.id in (3) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (177,178,179,180,181) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(177<=zhiwei_id&&zhiwei_id<=181){
			hql="select obj from User as obj where obj.zhiwei.id in (3) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==3){//#11==>团队长
			hql="select obj from User as obj where obj.zhiwei.id in (182,183,184,185,186) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="";
		}else if(182<=zhiwei_id&&zhiwei_id<=186){//副团队长
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}else if(zhiwei_id==124){//#12
			hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id in (506,507,508,509,510) order by obj.bumen.id asc";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (187,188,189,190,191)";
		}else if(187<=zhiwei_id&&zhiwei_id<=191){
			hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id in (506,507,508,509,510)  order by obj.bumen.id asc";
		}else if(zhiwei_id==125){//#13
			hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id;
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (192,193,194,195,196) and obj.bumen.id="+bumen_id;
		}else if(192<=zhiwei_id&&zhiwei_id<=196){
			hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id;
		}else if(zhiwei_id==126){//#14
			hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (197,198,199,200,201) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(197<=zhiwei_id&&zhiwei_id<=201){
			hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==127){//#15
			hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (202,203,204,205,206) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(202<=zhiwei_id&&zhiwei_id<=206){
			hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==128){//#16
			hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (207,208,209,210,211) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(207<=zhiwei_id&&zhiwei_id<=211){
			hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==129){//#17
			hql="select obj from User as obj where obj.zhiwei.id=131 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (212,213,214,215,216) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(212<=zhiwei_id&&zhiwei_id<=216){
			hql="select obj from User as obj where obj.zhiwei.id=131 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==130){//#18
			hql="select obj from User as obj where obj.zhiwei.id=17 and obj.bumen.id in (301,515)";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (132,133,134,135,136)";
		}else if(132<=zhiwei_id&&zhiwei_id<=136){
			hql="select obj from User as obj where obj.zhiwei.id=17 and obj.bumen.id in (301,515)";
		}else if(zhiwei_id==131){//#19
			hql="select obj from User as obj where obj.zhiwei.id in (217,218,219,220,221) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="";
		}else if(217<=zhiwei_id&&zhiwei_id<=221){
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}
		if(!"".equals(hql)){
			subordinate_list=this.userService.query(hql, null, -1, -1);
		}
		if(!"".equals(deputyPositionHql)){
			deputyPositionList=this.userService.query(deputyPositionHql, null, -1, -1);
		}
		List<UserTempData> userTempData = getUserTempData(subordinate_list);
		List<UserTempData> userTempData2 = getUserTempData(deputyPositionList);
		ZhiWeiTemp zhiWeiTemp=new ZhiWeiTemp();
		zhiWeiTemp.setSubordinateList(userTempData);
		zhiWeiTemp.setDeputyPositionList(userTempData2);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(ZhiWeiTemp.class, "subordinateList,deputyPositionList"));
		objs.add(new FilterObj(UserTempData.class, "id,zhixian,areaGradeOfUser,availableBalance,userName,mobile,photo,zhiwei,bumen,availableBalance,appClickapps"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, zhiWeiTemp, "查询成功", 0, filter);
	}
	private List<UserTempData> getUserTempData(List<User> list){
		List<UserTempData> users=new ArrayList<UserTempData>();
		for (User user:list) {
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
			String sql="select count(1) from shopping_user_clickapps as obj where user_id ="+user.getId();
			List<?> count = commonService.executeNativeNamedQuery(sql);
			if (count.size()>0) {
				userTempData.setAppClickapps(((BigInteger)count.get(0)).intValue());
			}
			users.add(userTempData);
		}
		return users;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:取出用户能看到的部门,区域,职位信息，，，，旧接口，淘汰
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:职位id:109 创始人职位能授予的职位
	 *				 #2:职位id:121 四集团总指挥能授予的职位,部门
	 *				 #3:职位id:122 六集团总指挥能授予的职位,部门
	 *				 #4:职位id:123 八集团总指挥能授予的职位,部门
	 *				 #5:执行总裁职位(职位id:17 十八部门   执行总裁)能授予的职位,部门,区域
	 *				 #6:大区总裁职位(职位id:120 八大区   总裁)能授予的职位,部门,区域
	 *				 #7:总裁职位(职位id:14 34省          总裁)能授予的职位,部门,区域
	 *				 #8:总经理职位(职位id:11 市          总经理)能授予的职位,部门,区域
	 *				 #9:执行总监职位(职位id:8 区县	  执行总监)能授予的职位,部门,区域
	 *				 #10:运营总监职位(职位id:5 乡镇	运营总监)能授予的职位,部门,区域
	 *				 #11:将帅学院院长(职位id:124) 将帅学院院长能授予的职位,部门,区域
	 *				 #12:将帅学院分院长(职位id:125  五大院(部门) 将帅学院分院长)能授予的职位,部门,区域
	 *				 #13:教委主任(职位id:126  八大区  	教委主任)能授予的职位,部门,区域
	 *				 #14:大队长(职位id:127  34省  	大队长)能授予的职位,部门,区域
	 *				 #15:教导员(职位id:128  市  	教导员)能授予的职位,部门,区域
	 *				 #16:教官(职位id:129  区县  	教官)能授予的职位,部门,区域
	 *				 #18:职位id:130 一集团总指挥能授予的职位,部门
	 *				 #19:团队长,助教(职位id:109 村庄  	助教 职位  id:131  教官)和普通用户不能授予其他用户职位
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "取出用户能看到的部门,区域,职位信息", value = "/get_position_message.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "get_self_subordinate", rgroup = "商品管理")
	@RequestMapping(value = "/get_position_message.htm", method = RequestMethod.POST)
	public void get_position_message(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "为了您更好的体验，请更新新版APP", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(user_id));
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		ZhiWei zhiwei=user.getZhiwei();
		BuMen bumen=user.getBumen();
		if(zhiwei==null){
			zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
			this.userService.update(user);
		}
		if(bumen==null){
			bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
			this.userService.update(user);
		}
		List<?> position_list=new ArrayList<Object>();
		String position_hql="";
		List<?> bumen_list=new ArrayList<Object>();
		String bumen_hql="";
		List<?> area_list=new ArrayList<Object>(); 
		String area_hql="";
 		long zhiwei_id=zhiwei.getId();
		long bumen_id=bumen.getId();
		if(zhiwei_id==109){//#1
			position_hql="select obj from ZhiWei as obj where obj.id in (121,122,123,124,130) order by obj.sequence asc";
			position_list=this.commonService.query(position_hql, null, -1, -1);							   
		}else if(zhiwei_id==130){//#17
			position_hql="select obj from ZhiWei as obj where obj.id in (17,132,133,134,135,136)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(301,515)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(132<=zhiwei_id&&zhiwei_id<=136){
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(301,515)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==121){//#2
			position_hql="select obj from ZhiWei as obj where obj.id in (17,137,138,139,140,141)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(103,106,104,504,102,101)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(137<=zhiwei_id&&zhiwei_id<=141){
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(103,106,104,504,102,101)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==122){//#3
			position_hql="select obj from ZhiWei as obj where obj.id in (17,142,143,144,145,146) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(1,105,502,3,501,503)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(142<=zhiwei_id&&zhiwei_id<=146){
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(1,105,502,3,501,503)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==123){//#4
			position_hql="select obj from ZhiWei as obj where obj.id in (17,147,148,149,150,151)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(2,4,5,302,401,107)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(147<=zhiwei_id&&zhiwei_id<=151){
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(2,4,5,302,401,107)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==17){//#5
			position_hql="select obj from ZhiWei as obj where obj.id in (120,152,153,154,155,156)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			area_hql="select obj from AreaGradeOfUser as obj where obj.id in (767476,767477,767478,767479,767480,767481,767482,767483)";
			area_list=this.commonService.query(area_hql, null, -1, -1);
		}else if(152<=zhiwei_id&&zhiwei_id<=156){
			position_hql="select obj from ZhiWei as obj where obj.id in (120)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			area_hql="select obj from AreaGradeOfUser as obj where obj.id in (767476,767477,767478,767479,767480,767481,767482,767483)";
			area_list=this.commonService.query(area_hql, null, -1, -1);
		}else if(zhiwei_id==120){//#6
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (14,157,158,159,160,161) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(157<=zhiwei_id&&zhiwei_id<=161){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (14)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==14){//#7
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (11,162,163,164,165,166) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(162<=zhiwei_id&&zhiwei_id<=166){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (11)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==11){//#8
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (8,167,168,169,170,171) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(167<=zhiwei_id&&zhiwei_id<=171){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (8)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==8){//#9
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (5,172,173,174,175,176) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(172<=zhiwei_id&&zhiwei_id<=176){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (5)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==5){//#10
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (3,177,178,179,180,181) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(177<=zhiwei_id&&zhiwei_id<=181){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (3)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==3){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.id ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (182,183,184,185,186) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==124){//#11
			position_hql="select obj from ZhiWei as obj where obj.id in (125,187,188,189,190,191) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(506,507,508,509,510)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(187<=zhiwei_id&&zhiwei_id<=191){
			position_hql="select obj from ZhiWei as obj where obj.id in (125)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(506,507,508,509,510)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==125){//#12
			position_hql="select obj from ZhiWei as obj where obj.id in (126,192,193,194,195,196)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			area_hql="select obj from AreaGradeOfUser as obj where obj.id in (767476,767477,767478,767479,767480,767481,767482,767483)";
			area_list=this.commonService.query(area_hql, null, -1, -1);
		}else if(192<=zhiwei_id&&zhiwei_id<=196){
			position_hql="select obj from ZhiWei as obj where obj.id in (126)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			area_hql="select obj from AreaGradeOfUser as obj where obj.id in (767476,767477,767478,767479,767480,767481,767482,767483)";
			area_list=this.commonService.query(area_hql, null, -1, -1);
		}else if(zhiwei_id==126){//#13
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (127,197,198,299,200,201)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(197<=zhiwei_id&&zhiwei_id<=201){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (127)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==127){//#14
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (128,202,203,204,205,206) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(202<=zhiwei_id&&zhiwei_id<=206){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (128)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==128){//#15
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (129,207,208,209,210,211) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(207<=zhiwei_id&&zhiwei_id<=211){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (129,207,208,209,210,211)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==129){//#16
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (131,212,213,214,215,216)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(212<=zhiwei_id&&zhiwei_id<=216){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.pid ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (131)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==131){
			if(user.getAreaGradeOfUser()!=null){
				area_hql="select obj from AreaGradeOfUser as obj where obj.id ="+user.getAreaGradeOfUser().getId();
				area_list=this.commonService.query(area_hql, null, -1, -1);
			}else{
				ApiUtils.json(response, "", "当前用户没有对应的区域", 1);
				return;
			}
			position_hql="select obj from ZhiWei as obj where obj.id in (217,218,219,220,221)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else{//#18
			ApiUtils.json(response, "", "当前用户不能够授予职位", 1);
			return;
		}
		List<Object> out_put_list=new ArrayList<Object>();
		out_put_list.add(position_list);
		out_put_list.add(bumen_list);
		out_put_list.add(area_list);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DeputyPosition.class, "id,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,sort,is_deputyPosition"));
		objs.add(new FilterObj(BuMen.class, "id,name,sort,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put_list, "查询成功", 0, filter);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:用户的id,accept_user_id:接受用户的id,bumen_id:部门id,zhiwei_id:职位id,area_id:区域id
	 *@description:确认给自己的下级授予职位(不能授予给自己的同级和上级授予职位)
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:职位id:109 创始人职位能授予的职位
	 *				 #2:职位id:121 四集团总指挥能授予的职位,部门
	 *				        职位id:122 六集团总指挥能授予的职位,部门
	 *				 	职位id:123 八集团总指挥能授予的职位,部门
	 *				 #3:执行总裁职位(职位id:17 十八部门   执行总裁)能授予的职位,部门,区域
	 *					将帅学院分院长(职位id:125  五大院(部门) 将帅学院分院长)能授予的职位,部门,区域
	 *				 #4:大区总裁职位(职位id:120 八大区   总裁)能授予的职位,部门,区域
	 *					教委主任(职位id:126  八大区  	教委主任)能授予的职位,部门,区域
	 *				 #5:总裁职位(职位id:14 34省          总裁)能授予的职位,部门,区域
	 *					大队长(职位id:127  34省  	大队长)能授予的职位,部门,区域
	 *				 #6:总经理职位(职位id:11 市          总经理)能授予的职位,部门,区域
	 *					教导员(职位id:128  市  	教导员)能授予的职位,部门,区域
	 *				 #7:执行总监职位(职位id:8 区县	  执行总监)能授予的职位,部门,区域
	 *				        教官(职位id:129  区县  	教官)能授予的职位,部门,区域
	 *				 #8:运营总监职位(职位id:5 乡镇	运营总监)能授予的职位,部门,区域
	 *				 #9:没有职位(或其他职位)的用户的处理
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "确认给自己的下级授予职位", value = "/add_new_position.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/add_new_position.htm", method = RequestMethod.POST)
	public void add_new_position(HttpServletRequest request,
			HttpServletResponse response,String user_id,
			String accept_user_id,String bumen_id,String area_id,
			String zhiwei_id,String login_user_id){
		Long  userId= CommUtil.null2Long(user_id);
		if(userId==-1){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		boolean is_null = ApiUtils.is_null(accept_user_id,zhiwei_id,login_user_id);
		if (is_null) {
			ApiUtils.json(response, "", "参数错误!", 1);
			return;
		}
		if (CommUtil.null2Long(login_user_id)!=1) {
			ApiUtils.json(response, "", "目前为系统自动授职,手动授职功能已关闭", 1);
			return;
		}
		//授予职位的用户
		User user=this.userService.getObjById(Long.valueOf(user_id));
		//接受职位的用户
		User accept_user=this.userService.getObjById(Long.valueOf(accept_user_id));
		//当前登陆的用户
		User login_user=this.userService.getObjById(Long.valueOf(login_user_id));
		ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", zhiwei_id);
		ZhiWei zhiwei_1=(ZhiWei) this.commonService.getById("ZhiWei", "0");
		BuMen bumen_1=(BuMen) this.commonService.getById("BuMen", "301");
		if(user==null){
			ApiUtils.json(response, "", "该用户不存在", 1);
			return;
		}
		if(accept_user==null){
			ApiUtils.json(response, "", "接受职位的用户不存在", 1);
			return;
		}
		//检查会员是否有接收职位的权限
		if (!accept_user.getUserRank().getIsHaveZhiweiRight()) {
			ApiUtils.json(response, "", "接受职位的用户没有任职权限", 1);
			return;
		}
		//无职位用户15内不能被其他部门的人员任命，超过15天可以被其他部门任命
		if (accept_user.getBumen()!=null&&user.getZhiwei()!=null&&user.getBumen()!=null&&login_user.getZhiwei().getId()!=109) {
			Long superiorBumenId = accept_user.getBumen().getSuperiorBumen()==null?-1:accept_user.getBumen().getSuperiorBumen().getId();						
			//判断是否为同一部门
			if (accept_user.getBumen().getId()!=user.getBumen().getId()&&superiorBumenId!=user.getBumen().getId()) {
				//判断有无职位，如果没有职位，判断时间，如果有职位则return
				if (accept_user.getZhiwei()!=null&&accept_user.getZhiwei().getId()!=0) {
					ApiUtils.json(response, "", "不能给其他部门的用户授予职位!", 1);
					return;
				}else {
					Date operateDate = CommUtil.formatDate(ApiUtils.getFirstday_Lastday(new Date(), 0, 15));
					if (accept_user.getAddTime().after(operateDate)) {
						ApiUtils.json(response, "", "不能给其他部门的用户授予职位!", 1);
						return;
					}
				}
			}
		}
		if(user.getZhiwei()==null){
			user.setZhiwei(zhiwei_1);
			this.userService.update(user);
		}
		if(accept_user.getZhiwei()==null){
			accept_user.setZhiwei(zhiwei_1);
			this.userService.update(accept_user);
		}
		if(user.getBumen()==null){
			user.setBumen(bumen_1);
			this.userService.update(user);
		}
		if(accept_user.getBumen()==null){
			accept_user.setBumen(bumen_1);
			this.userService.update(accept_user);
		}
		long userZhiweiId=user.getZhiwei().getId();
		long acceptUserZhiweiId=accept_user.getZhiwei().getId();
		if(accept_user.getZhiwei().getEquativePosition()==zhiwei.getEquativePosition()){
			ApiUtils.json(response, "", "该用户不能兼任副职位", 1);
			return;
		}
		if(userZhiweiId==109){//#1
			if(!"".equals(zhiwei_id)){
				String hql="select obj from User as obj where obj.bumen.id = " + bumen_id + " and obj.zhiwei.id="+zhiwei_id;
				List<User> user_list=this.userService.query(hql, null, -1, -1);
				if(user_list.size()>0){
					ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
					return;
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门", 1);
				return;
			}
		}else if(userZhiweiId==300||userZhiweiId==124){//#2
			if(!"".equals(zhiwei_id)&&!"".equals(bumen_id)){
				if(acceptUserZhiweiId==300||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门", 1);
				return;
			}
		}else if(187<=userZhiweiId&&userZhiweiId<=191||301<=userZhiweiId&&userZhiweiId<=305){//#2
			if(!"".equals(zhiwei_id)&&!"".equals(bumen_id)){
				if(187<=acceptUserZhiweiId&&acceptUserZhiweiId<=191||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==17||user.getZhiwei().getId()==125){//#3
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(187<=acceptUserZhiweiId&&acceptUserZhiweiId<=191||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(192<=userZhiweiId&&userZhiweiId<=196||152<=userZhiweiId&&userZhiweiId<=156){//#3
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(192<=acceptUserZhiweiId&&acceptUserZhiweiId<=196||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=156||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==120||user.getZhiwei().getId()==126){//#3
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(192<=acceptUserZhiweiId&&acceptUserZhiweiId<=196||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=156||acceptUserZhiweiId==126||acceptUserZhiweiId==120||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(157<=userZhiweiId&&userZhiweiId<=161||197<=userZhiweiId&&userZhiweiId<=201){//#3
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(152<=acceptUserZhiweiId&&acceptUserZhiweiId<=161||192<=acceptUserZhiweiId&&acceptUserZhiweiId<=201||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==14||user.getZhiwei().getId()==127){//#5
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(152<=acceptUserZhiweiId&&acceptUserZhiweiId<=161||192<=acceptUserZhiweiId&&acceptUserZhiweiId<=201||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(202<=userZhiweiId&&userZhiweiId<=206||162<=userZhiweiId&&userZhiweiId<=166){//#5
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(202<=acceptUserZhiweiId&&acceptUserZhiweiId<=206||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=166||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==11||user.getZhiwei().getId()==128){//#6
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(202<=acceptUserZhiweiId&&acceptUserZhiweiId<=206||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=166||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(207<=userZhiweiId&&userZhiweiId<=211||167<=userZhiweiId&&userZhiweiId<=171){//#6
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(207<=acceptUserZhiweiId&&acceptUserZhiweiId<=211||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=171||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==8||user.getZhiwei().getId()==129){//#7
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(207<=acceptUserZhiweiId&&acceptUserZhiweiId<=211||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=171||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(212<=userZhiweiId&&userZhiweiId<=216||172<=userZhiweiId&&userZhiweiId<=176){//#7
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(212<=acceptUserZhiweiId&&acceptUserZhiweiId<=216||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=176||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==5){//#8
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(212<=acceptUserZhiweiId&&acceptUserZhiweiId<=216||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=176||acceptUserZhiweiId==5||acceptUserZhiweiId==131||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{//#9
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(177<=userZhiweiId&&userZhiweiId<=181){//#8
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(187<=acceptUserZhiweiId&&acceptUserZhiweiId<=221||300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=181||acceptUserZhiweiId==5||acceptUserZhiweiId==131||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{//#9
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==3){
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=181||187<=acceptUserZhiweiId&&acceptUserZhiweiId<=221||acceptUserZhiweiId==3||acceptUserZhiweiId==5||acceptUserZhiweiId==131||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{//#9
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else if(user.getZhiwei().getId()==131){
			if(!"".equals(zhiwei_id)&&!"".equals(area_id)&&!"".equals(bumen_id)){
				if(300<=acceptUserZhiweiId&&acceptUserZhiweiId<=305||152<=acceptUserZhiweiId&&acceptUserZhiweiId<=171||187<=acceptUserZhiweiId&&acceptUserZhiweiId<=216||acceptUserZhiweiId==3||acceptUserZhiweiId==5||acceptUserZhiweiId==131||acceptUserZhiweiId==8||acceptUserZhiweiId==129||acceptUserZhiweiId==11||acceptUserZhiweiId==128||acceptUserZhiweiId==14||acceptUserZhiweiId==127||acceptUserZhiweiId==120||acceptUserZhiweiId==126||acceptUserZhiweiId==17||acceptUserZhiweiId==125||acceptUserZhiweiId==109||acceptUserZhiweiId==124){
					ApiUtils.json(response, "", "不能给比自己职位高和自己同级的用户授职", 1);
					return;
				}else{
					String hql="select obj from User as obj where obj.zhiwei.id="+zhiwei_id+" and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+area_id;
					List<User> user_list=this.userService.query(hql, null, -1, -1);
					if(user_list.size()>0){
						ApiUtils.json(response, "", "该职位已经有人,请先卸任担任该职位的用户,然后再授予职位", 1);
						return;
					}
				}
			}else{//#9
				ApiUtils.json(response, "", "请选择对应的职位或部门或区域", 1);
				return;
			}
		}else{
			ApiUtils.json(response, "", "该用户不能授予职位", 1);
			return;
		}
		if(!"".equals(user.getSelf_group_id())){
			if(accept_user.getIs_huanxin()==0){
				CommUtil.huanxin_reg(accept_user.getId().toString(),
						accept_user.getPassword(), accept_user.getUsername());
				accept_user.setIs_huanxin(1);
				this.userService.update(accept_user);
			}
			CommUtil.add_group_member(user.getSelf_group_id(), accept_user_id);
		}
		accept_user.setZhiwei(zhiwei);
		if(bumen_id!=null||!"".equals(bumen_id)){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", bumen_id);
			accept_user.setBumen(bumen);
		}
		ZhiWeiRecoderEntity zre=new ZhiWeiRecoderEntity();
		zre.setAddTime(new Date());
		zre.setDeleteStatus(false);
		zre.setUser(login_user);
		zre.setMyselfUser(accept_user);
		zre.setZhiwei(zhiwei);
		if(area_id!=null&&!"".equals(area_id)){
			AreaGradeOfUser areaGradeOfUser=(AreaGradeOfUser) this.commonService.getById("AreaGradeOfUser", area_id);
			accept_user.setAreaGradeOfUser(areaGradeOfUser);
			String areaName=areaGradeOfUser==null?"":areaGradeOfUser.getName();
			zre.setMsg(areaName+zhiwei.getName());
		}else{
			zre.setMsg(zhiwei.getName());
		}
		this.commonService.save(zre);
		List<ZhiWeiRecoderEntity> rec_lsit=accept_user.getZhiweiRec();
		rec_lsit.add(zre);
		accept_user.setZhiweiRec(rec_lsit);
		boolean ret=this.userService.update(accept_user);
		ApiUtils.pushNotice(accept_user);
		String msg=accept_user.getUserName()+"战友你好，祝贺你升职为"+zhiwei.getName()+"，请尽快熟悉辖区名录，并帮助下辖商区的战友们完成布局和任命工作。为你的梦想，加油";
		this.send_message(accept_user, msg);
		if (login_user!=null&&accept_user.getZhiwei().getId()!=0) {
			String area="";
			AreaGradeOfUser areaGradeOfUser = accept_user.getAreaGradeOfUser();
			if (areaGradeOfUser!=null&&zhiwei.getPositionOrder()>=15&&zhiwei.getId()!=120) {
				area = areaGradeOfUser.getName()+"商区";
			}
			User seniorUser=login_user;
			try {
				int i=0;
				do {
					seniorUser = ApiUtils.getUserSenior(seniorUser, userService);
					if (seniorUser!=null) {				
						msg=seniorUser.getUserName()+"战友你好,你的下属"+login_user.getZhiwei().getName()+login_user.getUserName()+"刚刚任命"+accept_user.getUserName()+"为"+area+accept_user.getZhiwei().getName()+"。";
						this.send_message(seniorUser, msg);
					}
					i++;
					if (i>8) {
						break;
					}
				} while (seniorUser!=null);	
			} catch (Exception e) {
				e.printStackTrace();
			}				
		}			
		if(ret){
			ApiUtils.json(response, "", "授予职位成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:当前登陆的用户id,accept_user_id:接受职位的用户id
	 *@description:确认授予职衔
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "确认授予职衔", value = "/confirm_award_zhixian.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/confirm_award_zhixian.htm", method = RequestMethod.POST)
	public void confirm_award_zhixian(HttpServletRequest request,
			HttpServletResponse response,String user_id,String accept_user_id,
			String zhixian_id){
		if(ApiUtils.is_null(user_id,accept_user_id,zhixian_id)){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if (CommUtil.null2Long(user_id)!=1) {
			ApiUtils.json(response, "", "目前为系统自动授衔,手动授衔功能已关闭", 1);
			return;
		}
		if(!"".equals(user_id)&&!"".equals(accept_user_id)){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			User accetp_user=this.userService.getObjById(Long.valueOf(accept_user_id));
			ZhiXianEntity zhiXian=(ZhiXianEntity) this.commonService.getById("ZhiXianEntity", zhixian_id);
			if(accetp_user==null){
				ApiUtils.json(response, "", "接受职衔的用户不存在", 1);
				return;
			}else{
				ZhiXianEntity accetpZhiXian=accetp_user.getZhixian();
				if (accetpZhiXian!=null&&accetp_user.getZhixian().getId()==Long.valueOf(zhixian_id)) {
					ApiUtils.json(response, "", "授予用户的职衔与该用户的职衔相同", 1);
					return;
				}
				if(user.getZhiwei()==null){
					ApiUtils.json(response, "", "用户的职位不存在,不能授予职衔", 1);
					return;
				}
				Integer positionOrder=user.getZhiwei().getPositionOrder();
				if(positionOrder==1){
					accetp_user.setZhixian(zhiXian);
				}else if(positionOrder<=45){				
					if(accetp_user.getZhiwei()!=null){
						long aPositionOrder=accetp_user.getZhiwei().getPositionOrder();
						if(aPositionOrder<=positionOrder){
							ApiUtils.json(response, "", "不能给自己同级和上级授予职衔", 1);
							return;
						}else{
							if (accetpZhiXian!=null&&accetpZhiXian.getRankOrder()<zhiXian.getRankOrder()) {
								ApiUtils.json(response, "", "权限不足，不能对用户进行降衔操作！", 1);
								return;
							}	
							accetp_user.setZhixian(zhiXian);
						}
					}else{
						if (accetpZhiXian!=null&&accetpZhiXian.getRankOrder()<zhiXian.getRankOrder()) {
							ApiUtils.json(response, "", "权限不足，不能对用户进行降衔操作！", 1);
							return;
						}	
						accetp_user.setZhixian(zhiXian);
					}
				}else{
					ApiUtils.json(response, "", "该用户没有权限授衔", 1);
					return;
				}
				ZhiXianRecorderEntity zrc=new ZhiXianRecorderEntity();
				zrc.setAddTime(new Date());
				zrc.setDeleteStatus(false);
				zrc.setUser(user);
				zrc.setMyselfUser(accetp_user);
				zrc.setZhixian(zhiXian);
				zrc.setMsg("授为"+zhiXian.getName()+"职衔");
				this.commonService.save(zrc);
				List<ZhiXianRecorderEntity> res=accetp_user.getZhixianRec();
				res.add(zrc);
				accetp_user.setZhixianRec(res);
				boolean ret=this.userService.update(accetp_user);
				String msg=accetp_user.getUserName()+"你好！你目前被授予为"+zhiXian.getName()+"职衔，请你继续保持昂扬斗志，帮助战友们做好布局和服务工作，加油";
				this.send_message(accetp_user, msg);
				if(ret){
					ApiUtils.pushRemindTitleSMS(accetp_user.getUserName(), accetp_user.getMobile());
					ApiUtils.json(response, "", "授衔成功", 0);
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:当前登陆的用户id
	 *@description:获取当前登陆的用户能授予的职衔
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "获取当前登陆的用户能授予的职衔", value = "/get_can_award_zhixian.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/get_can_award_zhixian.htm", method = RequestMethod.POST)
	public void get_can_award_zhixian(HttpServletRequest request,
			HttpServletResponse response,String user_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(user_id)){
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(user!=null&&user.getZhiwei()!=null){
				long zhiwei_id=user.getZhiwei().getId();
				String hql="";
				List<?> ret_list=null;
//				if(zhiwei_id==109){
//					hql="select obj from ZhiXianEntity as obj";
//					ret_list=this.commonService.query(hql, null, -1, -1);
//				}else if(zhiwei_id==17||zhiwei_id==120||zhiwei_id==121||zhiwei_id==122||zhiwei_id==123||zhiwei_id==124||zhiwei_id==130){
//					hql="select obj from ZhiXianEntity as obj where obj.id in(6,7,8,9)";
//					ret_list=this.commonService.query(hql, null, -1, -1);
//				}else if(zhiwei_id==14){
//					hql="select obj from ZhiXianEntity as obj where obj.id in(1,2,3,4,5)";
//					ret_list=this.commonService.query(hql, null, -1, -1);
//				}else{
//					ret_list=new ArrayList<ZhiXianEntity>();
//				}
				if(zhiwei_id==109){//#1创始人
					hql="select obj from ZhiXianEntity as obj order by obj.id asc";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if(zhiwei_id==116||zhiwei_id==124||zhiwei_id==300){//#2总指挥，院长
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 40";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=187&&zhiwei_id<=191)||(zhiwei_id>=301&&zhiwei_id<=305)||zhiwei_id==17){//#3执行总裁，副院长
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 45";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=152&&zhiwei_id<=156)||zhiwei_id==120){//#4大区总裁，副执行总裁
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 50";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=157&&zhiwei_id<=161)||zhiwei_id==14){//#5总裁，副大区总裁
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 55";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=162&&zhiwei_id<=166)||zhiwei_id==11){//#6总经理，副总裁
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 60";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=167&&zhiwei_id<=171)||zhiwei_id==8){//#7执行总监，副总经理
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 65";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=172&&zhiwei_id<=176)||zhiwei_id==5){//#8运营总监，副执行总监
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 70";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if((zhiwei_id>=177&&zhiwei_id<=181)||zhiwei_id==3){//#9团队长，副运营总监
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 75";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else if(zhiwei_id>=182&&zhiwei_id<=186){//#10副团队长
					hql="select obj from ZhiXianEntity as obj where obj.rankOrder >= 80";
					ret_list=this.commonService.query(hql, null, -1, -1);
				}else{
					ret_list=new ArrayList<ZhiXianEntity>();
				}
				List<FilterObj> objs = new ArrayList<FilterObj>();
				objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
				CustomerFilter filter = ApiUtils.addIncludes(objs);
				ApiUtils.json(response, ret_list, "查询成功", 0, filter);
			}else{
				ApiUtils.json(response, "", "该用户职位为空,不能授衔", 1);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:user_id:点进去群主的用户id,holder_id:当前登陆的用户id
	 *@description:以点进去(当前)的用户id为群主到环信那边去创建一个群,并且拉入当前用户的id
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:查一下点进去的用户有没有在环信那边创建相应的会议群
	 *				        如果有的话,不需要在环信那边重新创建,直接取group_id,
	 *					因为没有做相应的记录表,所以不知道当前用户是否在这个群里面,
	 *					所以都要加入一下环信这个群,对于重复加入同一样的群,环信
	 *					服务器会有自己的处理
	 *				 #2:再判断一下该用户是否在环信注册,如果没有注册,要到环信去
	 *					注册,不然的话,环新服务器会拒绝以该用户建群
	 *@variable:*******
	 ***/
	@SecurityMapping(display = false, rsequence = 0, title = "以当前的用户id为群主到环信那边去创建一个群", value = "/create_self_group.htm*", rtype = "seller", rname = "得到自己的下级列表", rcode = "add_new_position", rgroup = "商品管理")
	@RequestMapping(value = "/create_self_group.htm", method = RequestMethod.POST)
	public void create_self_group(HttpServletRequest request,
			HttpServletResponse response,String user_id,String holder_id){
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		if(!"".equals(user_id)&&!"".equals(holder_id)){
			User holder=this.userService.getObjById(Long.valueOf(holder_id));
			User user=this.userService.getObjById(Long.valueOf(user_id));
			if(user==null||holder==null){
				ApiUtils.json(response, "", "上传的用户不存在", 1);
				return;
			}else{
				String groupName="";
				if(user.getAreaGradeOfUser()==null){
					groupName=user.getBumen().getName()+user.getZhiwei().getName()+"会议群";
				}else{
					groupName=user.getBumen().getName()+user.getAreaGradeOfUser().getName()+user.getZhiwei().getName()+"会议群";
				}
				String groupDesc="工作会议交流群";
				if(!"".equals(user.getSelf_group_id())&&user.getSelf_group_id()!=null){//#1
					CommUtil.add_group_member(user.getSelf_group_id(), holder.getId().toString());
					ApiUtils.json(response, user.getSelf_group_id(),"获取群成功" , 0);
					return;
				}else{
					if(user.getIs_huanxin()==0){//#2
						CommUtil.huanxin_reg(user.getId().toString(),
								user.getPassword(), user.getUsername());
						user.setIs_huanxin(1);
						this.userService.update(user);
					}
					JSONObject ret=CommUtil.create_group(groupName, groupDesc, false, true, user_id);
					if(ret!=null){
						String self_group_id=JSONObject.parseObject(ret.getString("data")).getString("groupid");
						user.setSelf_group_id(self_group_id);
						boolean is_ok=this.userService.update(user);
						if(is_ok){
							CommUtil.add_group_member(user.getSelf_group_id(), holder.getId().toString());
							List<User> subordinate_list=null;
							String hql="";
							if(user.getZhiwei()==null){
								ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
								user.setZhiwei(zhiwei);
								this.userService.update(user);
							}
							if(user.getBumen()==null){
								BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
								user.setBumen(bumen);
								this.userService.update(user);
							}
							long zhiwei_id=user.getZhiwei().getId();
							long bumen_id=user.getBumen().getId();
							if(zhiwei_id==109){
								hql="select obj from User as obj where obj.zhiwei.id = 300";
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==300&&(bumen_id>=601&&bumen_id<=610)){
								String ids=ApiUtils.getSubclassBumenIds(bumen_id,commonService);
								hql="select obj from User as obj where obj.zhiwei.id=17 and obj.bumen.id in (" + ids + ")";
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==17){
								hql="select obj from User as obj where obj.zhiwei.id=120 and obj.bumen.id="+bumen_id;
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==120){
								hql="select obj from User as obj where obj.zhiwei.id=14 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==14){
								hql="select obj from User as obj where obj.zhiwei.id=11 and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==11){
								hql="select obj from User as obj where obj.zhiwei.id=8 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==8){
								hql="select obj from User as obj where obj.zhiwei.id=5 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==5){
								hql="select obj from User as obj where obj.zhiwei.id=3 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==3){
								hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==124){
								hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id in (505,506,507,508,509,510)";
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==125){
								hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id;
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==126){
								hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==127){
								hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==128){
								hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==129){
								hql="select obj from User as obj where obj.zhiwei.id=131 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else if(zhiwei_id==131){
								hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
								subordinate_list=this.userService.query(hql, null, -1, -1);
							}else{
								subordinate_list=new ArrayList<User>();
							}
							for(User add_user:subordinate_list){
								if(add_user.getIs_huanxin()==0){
									CommUtil.huanxin_reg(add_user.getId().toString(),
											add_user.getPassword(), add_user.getUsername());
									add_user.setIs_huanxin(1);
									this.userService.update(add_user);
								}
								if (ApiUtils.isHaveHuanxin(add_user.getId())) {
									CommUtil.add_group_member(user.getSelf_group_id(), add_user.getId().toString());
								}							
							}
							ApiUtils.json(response, self_group_id, "创建会议群成功", 0);
							return;
						}else{
							ApiUtils.json(response, "", "保存group_id失败", 1);
							return;
						}
					}else{
						ApiUtils.json(response, "", "从环信创建群失败,可能原因,群主没登陆万手", 1);
					}
				}
			}
		}else{
			ApiUtils.json(response, "", "没有上传用户的id", 1);
			return;
		}
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
	 *@author:gaohao
	 *@return:void
	 *@param:userId:用户的id
	 *@description:获取用户所属下级的信息 新
	 *@function:**
	 *@exception:*******
	 *@method_detail:#1:职位id:109创始人职位看到自己的下级
	 *				 #2:职位id:300 战区总指挥看到自己的下级
	 *				 #3:职位id:301-305 战区副总指挥看到自己的下级
	 *				 #5:执行总裁职位(职位id:17  十八部门   执行总裁)看到自己的下级
	 *				 #6:大区总裁职位(职位id:120 八大区   总裁)看到自己的下级
	 *				 #7:总裁职位(职位id:14 34省          总裁)看到自己的下级
	 *				 #8:总经理职位(职位id:11 市          总经理)看到自己的下级
	 *				 #9:执行总监职位(职位id:8 区县	 执行总监)看到自己的下级
	 *				 #10:运营总监职位(职位id:5  乡镇	运营总监)看到自己的下级
	 *				 #11:团队长(职位id:3  村庄  	团队长)看到自己的下级
	 *				 #12:将帅学院院长(职位id:124) 将帅学院院长看到自己的下级
	 *				 #13:将帅学院分院长(职位id:125  五大院(部门) 将帅学院分院长)看到自己的下级
	 *				 #14:教委主任(职位id:126  八大区  	教委主任)看到自己的下级
	 *				 #15:大队长(职位id:127  34省  	大队长)看到自己的下级
	 *				 #16:教导员(职位id:128  市  	教导员)看到自己的下级
	 *				 #17:教官(职位id:129  区县  	教官)看到自己的下级
	 *				 #18:职位id:130一集团总指挥职位看到自己的下级
	 *				 #19:助教(职位id:131  乡镇)看到自己的下级
	 *				 #20:无职位看到的用户
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/getSelfSubordinate.htm",method = RequestMethod.POST)
	public void getSelfSubordinate(HttpServletRequest request,
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
		if(user.getZhiwei()==null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
			this.userService.update(user);
		}
		if(user.getBumen()==null){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
			this.userService.update(user);
		}
		List<User> deputyPositionList=new ArrayList<User>();;
		List<User> subordinate_list=new ArrayList<User>();;
		String hql="";
		String deputyPositionHql="";
		long zhiwei_id=user.getZhiwei().getId();
		long bumen_id=user.getBumen().getId();
		if(zhiwei_id==109){//#1
			hql="select obj from User as obj where obj.zhiwei.id in (124,300) order by obj.bumen.id asc";
			deputyPositionHql="";
		}else if(zhiwei_id==300&&(bumen_id>=601&&bumen_id<=610)){//#2
			String ids=ApiUtils.getSubclassBumenIds(bumen_id,commonService);
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (" + ids + ")";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id >= 301 and obj.zhiwei.id<=305 and obj.bumen.id = " +bumen_id;
		}else if((301<=zhiwei_id&&zhiwei_id<=305)&&(bumen_id>=601&&bumen_id<=610)){//#3
			String ids=ApiUtils.getSubclassBumenIds(bumen_id,commonService);
			hql="select obj from User as obj where obj.zhiwei.id IN (17) and obj.bumen.id in (" + ids + ")";
		}else if(zhiwei_id==17){//#5
			hql="select obj from User as obj where obj.zhiwei.id IN (120) and obj.bumen.id="+bumen_id;
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id IN (152,153,154,155,156) and obj.bumen.id="+bumen_id;
		}else if(152<=zhiwei_id&&zhiwei_id<=156){
			hql="select obj from User as obj where obj.zhiwei.id IN (120) and obj.bumen.id="+bumen_id;
		}else if(zhiwei_id==120){//#6
			hql="select obj from User as obj where obj.zhiwei.id in (14) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (157,158,159,160,161) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(157<=zhiwei_id&&zhiwei_id<=161){
			hql="select obj from User as obj where obj.zhiwei.id in (14) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==14){//#7
			hql="select obj from User as obj where obj.zhiwei.id in (11) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (162,163,164,165,166) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(162<=zhiwei_id&&zhiwei_id<=166){
			hql="select obj from User as obj where obj.zhiwei.id in (11) and obj.bumen.id="+bumen_id +" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==11){//#8
			hql="select obj from User as obj where obj.zhiwei.id in (8) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (167,168,169,170,171) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(167<=zhiwei_id&&zhiwei_id<=171){
			hql="select obj from User as obj where obj.zhiwei.id in (8) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==8){//#9
			hql="select obj from User as obj where obj.zhiwei.id in (5) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (172,173,174,175,176) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(172<=zhiwei_id&&zhiwei_id<=176){
			hql="select obj from User as obj where obj.zhiwei.id in (5) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==5){//#10
			hql="select obj from User as obj where obj.zhiwei.id in (3) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (177,178,179,180,181) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==3){//#11==>团队长
			hql="select obj from User as obj where obj.zhiwei.id in (182,183,184,185,186) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="";
		}else if(182<=zhiwei_id&&zhiwei_id<=186){//副团队长
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}else if(177<=zhiwei_id&&zhiwei_id<=181){
			hql="select obj from User as obj where obj.zhiwei.id in (3) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==3){//#11
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}else if(zhiwei_id==124){//#12
			hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id in (506,507,508,509,510) order by obj.bumen.id asc";
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (187,188,189,190,191)";
		}else if(187<=zhiwei_id&&zhiwei_id<=191){
			hql="select obj from User as obj where obj.zhiwei.id=125 and obj.bumen.id in (506,507,508,509,510)  order by obj.bumen.id asc";
		}else if(zhiwei_id==125){//#13
			hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id;
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (192,193,194,195,196) and obj.bumen.id="+bumen_id;
		}else if(192<=zhiwei_id&&zhiwei_id<=196){
			hql="select obj from User as obj where obj.zhiwei.id=126 and obj.bumen.id="+bumen_id;
		}else if(zhiwei_id==126){//#14
			hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (197,198,199,200,201) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(197<=zhiwei_id&&zhiwei_id<=201){
			hql="select obj from User as obj where obj.zhiwei.id=127 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==127){//#15
			hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (202,203,204,205,206) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(202<=zhiwei_id&&zhiwei_id<=206){
			hql="select obj from User as obj where obj.zhiwei.id=128 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==128){//#16
			hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (207,208,209,210,211) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(207<=zhiwei_id&&zhiwei_id<=211){
			hql="select obj from User as obj where obj.zhiwei.id=129 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==129){//#17
			hql="select obj from User as obj where obj.zhiwei.id=131 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="select obj from User as obj where obj.zhiwei.id in (212,213,214,215,216) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
		}else if(212<=zhiwei_id&&zhiwei_id<=216){
			hql="select obj from User as obj where obj.zhiwei.id=131 and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.pid="+user.getAreaGradeOfUser().getId();
		}else if(zhiwei_id==131){//#19
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}else if(zhiwei_id==131){//#19
			hql="select obj from User as obj where obj.zhiwei.id in (217,218,219,220,221) and obj.bumen.id="+bumen_id+" and obj.areaGradeOfUser.id="+user.getAreaGradeOfUser().getId();
			deputyPositionHql="";
		}else if(217<=zhiwei_id&&zhiwei_id<=221){
			hql="select obj from User as obj where obj.zhiwei.id=0 and obj.bumen.id="+bumen_id+" and obj.dan_bao_ren= '"+user.getUserName()+"'";
			deputyPositionHql="";
		}
		if(!"".equals(hql)){
			subordinate_list=this.userService.query(hql, null, -1, -1);
		}
		if(!"".equals(deputyPositionHql)){
			deputyPositionList=this.userService.query(deputyPositionHql, null, -1, -1);
		}
		AppTransferData appTransferData=new AppTransferData();
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		out_put = ApiUtils.getAppUserTemps(subordinate_list, null, commonService, userService);
		appTransferData.setThirdData(out_put);
		out_put = ApiUtils.getAppUserTemps(deputyPositionList, null, commonService, userService);
		appTransferData.setSecondData(out_put);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppTransferData.class, "fifthData,thirdData,secondData"));
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance,userRank"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(UserRank.class, "userRankName"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, appTransferData, "查询成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:通过userId 获取用户属性
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserDetailInfo.htm", method = RequestMethod.POST)
	public void appGetUserDetailInfo(HttpServletRequest request,
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
		UserTemp userTemp = this.getAppUserTemp(user, null);
		userTemp.setAppSeeDataPower(ApiUtils.erifySeeDataPowerUser(userId, user.getPassword(), userService, 10, commonService)==null?false:true);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser,fenhongNum,influences,leader,affinitys,userActiveState,appSeeDataPower"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance,userRank"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(UserRank.class, "userRankName"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, userTemp, "查询用户成功", 0, filter);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:执行总裁以上用户以及特定用户查看部门未被任命的人
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetUserByBumen.htm", method = RequestMethod.POST)
	public void appGetUserByBumen(HttpServletRequest request,
			HttpServletResponse response,String userId,String password,String currentPage){
		User user=ApiUtils.erifyPowerUser(userId, password, userService,10);
		DepartmentPower departmentPower=null;
		User erifyUser=null;
		if (user==null) {
			erifyUser = ApiUtils.erifyUser(userId, password, userService);
			if (erifyUser!=null) {
				departmentPower = (DepartmentPower) commonService.getByWhere("DepartmentPower", "obj.user.id = " + CommUtil.null2String(erifyUser.getId()));
			}			
		}		
		if (user==null&&departmentPower==null) {
			ApiUtils.json(response, "", "权限不足！", 1);
			return;
		}
		user=user==null?erifyUser:user;
		if(user.getZhiwei()==null){
			ZhiWei zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
		}
		if(user.getBumen()==null){
			BuMen bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
		}
		int current_page=0;
		int pageSize=20;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from User as obj where obj.bumen.id = " + user.getBumen().getId() + " and (obj.zhiwei.id = 0 or obj.zhiwei is null) order by obj.addTime DESC";
		long zhiweiId = user.getZhiwei().getId();
		BuMen bumen = user.getBumen();
		if (zhiweiId>=300&&zhiweiId<=305) {
			String bumenIdSql="select id from ecm_bumen where superiorBumen_id =" + bumen.getId();
			List<?> bumenIds = commonService.executeNativeNamedQuery(bumenIdSql);
			String ids="";
			for (Object obj : bumenIds) {
				ids+=CommUtil.null2String(obj)+",";
			}
			hql="select obj from User as obj where obj.bumen.id in (" + ids + user.getBumen().getId() + ") and (obj.zhiwei.id = 0 or obj.zhiwei is null) order by obj.addTime DESC";
		}
		List<User> users = userService.query(hql, null, current_page*pageSize, pageSize);
		List<UserTemp> out_put=new ArrayList<UserTemp>();
		out_put=ApiUtils.getAppUserTemps(users, null, commonService, userService);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(UserTemp.class, "user,liveness,guaranteeUser"));
		objs.add(new FilterObj(User.class, "id,addTime,zhixian,areaGradeOfUser,userName,mobile,photo,zhiwei,bumen,loginDate,loginCount,availableBalance,userRank"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,positionOrder"));
		objs.add(new FilterObj(BuMen.class, "id,name,group_id,superiorBumen"));
		objs.add(new FilterObj(AreaGradeOfUser.class, "id,name,pid"));
		objs.add(new FilterObj(ZhiXianEntity.class, "id,name,img_url"));
		objs.add(new FilterObj(AppTransferData.class, "fifthData,secondData"));
		objs.add(new FilterObj(UserRank.class, "userRankName"));
		objs.add(new FilterObj(UserRankName.class, "id,rankName"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put, "查询成功", 0, filter);
	}
	private UserTemp getAppUserTemp(User user,AppTransferData guaranteeUser){
		UserTemp usertemp=new UserTemp();
		String hql="select obj from AppClickNum as obj where obj.user.id = " + user.getId();
		@SuppressWarnings("unchecked")
		List<AppClickNum> query = commonService.query(hql, null, -1, -1);		
		usertemp.setUser(user);
		AppTransferData gt=new AppTransferData();
		if (guaranteeUser==null&&user.getDan_bao_ren()!=null&&(!user.getDan_bao_ren().equals(""))) {
			String hql1="select obj from User AS obj where obj.userName = '"+user.getDan_bao_ren()+"'";
			List<User> danbao = userService.query(hql1, null, 0, 1);
			if (danbao.size()>0) {
				gt.setFifthData(danbao.get(0).getId());
				gt.setSecondData(danbao.get(0).getUsername());
			}			
		}
		if (guaranteeUser==null) {
			usertemp.setGuaranteeUser(gt);
		}else {
			usertemp.setGuaranteeUser(guaranteeUser);
		}
		if (query.size()>0) {
			usertemp.setLiveness(query.get(0).getClickNum().toString());		
		}else {
			usertemp.setLiveness("0");
		}
		try {
			String userActiveState = ApiUtils.getUserState(commonService,user);
			usertemp.setUserActiveState(userActiveState);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usertemp;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:通过部门，职位获取地区信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGetRegionInfo.htm", method = RequestMethod.POST)
	public void appGetRegionInfo(HttpServletRequest request,HttpServletResponse response,String userId,String bumenId,String zhiweiId){
		User user = this.userService.getObjById(CommUtil.null2Long(userId));
		if (user==null) {
			ApiUtils.json(response, "", "用户不存在", 1);
			return;
		}
		BuMen bumen = user.getBumen();
		ZhiWei zhiwei = user.getZhiwei();
		AreaGradeOfUser areaGradeOfUser = user.getAreaGradeOfUser();
		if (bumen==null||zhiwei==null) {
			ApiUtils.json(response, "", "权限不足", 1);
			return;
		}
		BuMen operationBumen = (BuMen) commonService.getById("BuMen", CommUtil.null2Long(bumenId).toString());
		if (operationBumen==null&&user.getId()!=1) {
			ApiUtils.json(response, "", "部门不存在", 1);
			return;
		}
		BuMen superiorBumen = operationBumen.getSuperiorBumen();
		Long superiorBumenId=superiorBumen==null?-1l:superiorBumen.getId();
		if (user.getId()!=1) {
			if (bumen.getId()!=CommUtil.null2Long(bumenId)&&superiorBumenId!=bumen.getId()) {
				ApiUtils.json(response, "", "部门不同，不能操作", 1);
				return;
			}
		}
		ZhiWei operationZhiwei = (ZhiWei) commonService.getById("ZhiWei", CommUtil.null2Long(zhiweiId).toString());
		if (operationZhiwei==null) {
			ApiUtils.json(response, "", "职位不存在", 1);
			return;
		}
		if (zhiwei.getPositionOrder()>operationZhiwei.getPositionOrder()||zhiwei.getName().equals(operationZhiwei.getName())) {
			ApiUtils.json(response, "", "没有操作权限", 1);
			return;
		}
		if (zhiwei.getPositionOrder().equals(operationZhiwei.getPositionOrder())&&zhiwei.getName().indexOf("副")==-1) {
			ApiUtils.json(response, "", "没有操作权限", 1);
			return;
		}
		String sql="";
		List<?> query=new ArrayList<>();
		String where="";
		//无地区,副执行总裁以上
		if (operationZhiwei.getPositionOrder()<=15&&operationZhiwei.getName().indexOf("大区总裁")==-1) {
			where="AND su.bumen_id = "+ bumenId;
			if (operationZhiwei.getName().indexOf("副")!=-1) {
				bumenId=user.getBumen().getId()+"";
				where="AND su.bumen_id = "+ bumenId;;
			}else if(user.getId()==1){
				where="";
			}
			sql="SELECT "+
					"'-1',"+
					"'数量',"+
					  "COUNT(1) "+
					"FROM "+
					  "shopping_user AS su "+
					"WHERE su.zhiwei_id =" + operationZhiwei.getId() + " "+ 
					where;
					
			query=commonService.executeNativeNamedQuery(sql);
		}else {//有地区			
			if (areaGradeOfUser==null&&user.getZhiwei().getName().indexOf("执行总裁")==-1) {
				ApiUtils.json(response, "", "区域错误", 1);
				return;
			}
			if (operationZhiwei.getName().indexOf("副")!=-1) {
				where=" WHERE sagou.id = " + areaGradeOfUser.getId();
				bumenId=user.getBumen().getId()+"";
			}else {
				if (user.getZhiwei().getName().indexOf("执行总裁")!=-1) {
					where=" WHERE sagou.pid = 1 ";
				}else {
					where=" WHERE sagou.pid = " + areaGradeOfUser.getId();
				}			
			}
			sql = (new StringBuilder("SELECT temp5.id, temp5.name, ( CASE WHEN temp5.areaGradeOfUser_id IS NOT NULL THEN 1 ELSE 0 END ) AS isHave FROM (SELECT temp.areaGradeOfUser_id, sagou.id, sagou.name FROM shopping_area_grade_of_user AS sagou LEFT JOIN (SELECT su.areaGradeOfUser_id FROM shopping_user AS su WHERE su.zhiwei_id = ")).append(zhiweiId).append(" ").append("AND su.bumen_id = ").append(bumenId).append(") AS temp ").append("ON sagou.id = temp.areaGradeOfUser_id ").append(where).append(") AS temp5 order by temp5.id").toString();
			query = commonService.executeNativeNamedQuery(sql);
		}
		ApiUtils.json(response, query, "获取成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user_id:用户的id
	 *@description:取出用户能看到的部门,职位信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appGetpositionDepartmentInfo.htm", method = RequestMethod.POST)
	public void appGetpositionDepartmentInfo(HttpServletRequest request,
			HttpServletResponse response,String userId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(Long.valueOf(userId));
		if(user==null){
			ApiUtils.json(response, "", "没有该用户", 1);
			return;
		}
		ZhiWei zhiwei=user.getZhiwei();
		BuMen bumen=user.getBumen();
		if(bumen==null){
			bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
			this.userService.update(user);
			ApiUtils.json(response, "", "部门为空", 1);
			return;
		}
		if(zhiwei==null){
			zhiwei=(ZhiWei) this.commonService.getById("ZhiWei", "0");
			user.setZhiwei(zhiwei);
			this.userService.update(user);
			ApiUtils.json(response, "", "职位为空", 1);
			return;
		}
		List<ZhiWei> position_list=new ArrayList<ZhiWei>();
		List<ZhiWei> deputyPosition=new ArrayList<ZhiWei>();
		String position_hql="";
		List<?> bumen_list=new ArrayList<Object>();
		String bumen_hql="";
 		long zhiwei_id=zhiwei.getId();
		long bumen_id=bumen.getId();
		if(zhiwei_id==109){//#1 创始人
			position_hql="select obj from ZhiWei as obj where obj.id = 300 order by obj.sequence asc";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id >= 601 and obj.id <= 610";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==300&&(bumen_id>=601&&bumen_id<=610)){//#2  战区总指挥
			String ids=ApiUtils.getSubclassBumenIds(bumen_id,commonService);
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(" + ids + ")";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"301,302,303,304,305");
			position_list.addAll(deputyPosition);
		}else if(301<=zhiwei_id&&zhiwei_id<=305&&(bumen_id>=601&&bumen_id<=610)){//#3  战区副总指挥
			String ids=ApiUtils.getSubclassBumenIds(bumen_id,commonService);
			position_hql="select obj from ZhiWei as obj where obj.id in (17)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(" + ids + ")";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==17){//#5
			position_hql="select obj from ZhiWei as obj where obj.id in (120)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"152,153,154,155,156");
			position_list.addAll(deputyPosition);
		}else if(152<=zhiwei_id&&zhiwei_id<=156){
			position_hql="select obj from ZhiWei as obj where obj.id in (120)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==120){//#6
			position_hql="select obj from ZhiWei as obj where obj.id in (14) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"157,158,159,160,161");
			position_list.addAll(deputyPosition);
		}else if(157<=zhiwei_id&&zhiwei_id<=161){
			position_hql="select obj from ZhiWei as obj where obj.id in (14)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==14){//#7
			position_hql="select obj from ZhiWei as obj where obj.id in (11) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"162,163,164,165,166");
			position_list.addAll(deputyPosition);
		}else if(162<=zhiwei_id&&zhiwei_id<=166){
			position_hql="select obj from ZhiWei as obj where obj.id in (11)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==11){//#8
			position_hql="select obj from ZhiWei as obj where obj.id in (8) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"167,168,169,170,171");
			position_list.addAll(deputyPosition);
		}else if(167<=zhiwei_id&&zhiwei_id<=171){
			position_hql="select obj from ZhiWei as obj where obj.id in (8)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==8){//#9
			position_hql="select obj from ZhiWei as obj where obj.id in (5) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"172,173,174,175,176");
			position_list.addAll(deputyPosition);
		}else if(172<=zhiwei_id&&zhiwei_id<=176){
			position_hql="select obj from ZhiWei as obj where obj.id in (5)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==5){//#10
			position_hql="select obj from ZhiWei as obj where obj.id in (3) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"177,178,179,180,181");
			position_list.addAll(deputyPosition);
		}else if(177<=zhiwei_id&&zhiwei_id<=181){
			position_hql="select obj from ZhiWei as obj where obj.id in (3)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==3){
//			position_hql="select obj from ZhiWei as obj where obj.id in (182,183,184,185,186) ";
//			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			position_list=getFormatZhiweis(user,"182,183,184,185,186");
			position_list.addAll(deputyPosition);
		}else if(zhiwei_id==124){//#11
			position_hql="select obj from ZhiWei as obj where obj.id in (125) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(506,507,508,509,510)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"187,188,189,190,191");
			position_list.addAll(deputyPosition);
		}else if(187<=zhiwei_id&&zhiwei_id<=191){
			position_hql="select obj from ZhiWei as obj where obj.id in (125)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id in(506,507,508,509,510)";
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==125){//#12
			position_hql="select obj from ZhiWei as obj where obj.id in (126)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"192,193,194,195,196");
			position_list.addAll(deputyPosition);
		}else if(192<=zhiwei_id&&zhiwei_id<=196){
			position_hql="select obj from ZhiWei as obj where obj.id in (126)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==126){//#13
			position_hql="select obj from ZhiWei as obj where obj.id in (127)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"197,198,299,200,201");
			position_list.addAll(deputyPosition);
		}else if(197<=zhiwei_id&&zhiwei_id<=201){
			position_hql="select obj from ZhiWei as obj where obj.id in (127)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==127){//#14
			position_hql="select obj from ZhiWei as obj where obj.id in (128) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"202,203,204,205,206");
			position_list.addAll(deputyPosition);
		}else if(202<=zhiwei_id&&zhiwei_id<=206){
			position_hql="select obj from ZhiWei as obj where obj.id in (128)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==128){//#15
			position_hql="select obj from ZhiWei as obj where obj.id in (129) ";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"207,208,209,210,211");
			position_list.addAll(deputyPosition);
		}else if(207<=zhiwei_id&&zhiwei_id<=211){
			position_hql="select obj from ZhiWei as obj where obj.id in (129)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"207,208,209,210,211");
			position_list.addAll(deputyPosition);
		}else if(zhiwei_id==129){//#16
			position_hql="select obj from ZhiWei as obj where obj.id in (131)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			deputyPosition=getFormatZhiweis(user,"212,213,214,215,216");
			position_list.addAll(deputyPosition);
		}else if(212<=zhiwei_id&&zhiwei_id<=216){
			position_hql="select obj from ZhiWei as obj where obj.id in (131)";
			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
		}else if(zhiwei_id==131){
//			position_hql="select obj from ZhiWei as obj where obj.id in (217,218,219,220,221)";
//			position_list=this.commonService.query(position_hql, null, -1, -1);
			bumen_hql="select obj from BuMen as obj where obj.id ="+bumen_id;
			bumen_list=this.commonService.query(bumen_hql, null, -1, -1);
			position_list=getFormatZhiweis(user,"217,218,219,220,221");
			position_list.addAll(deputyPosition);
		}else{//#18
			ApiUtils.json(response, "", "当前用户不能够授予职位", 1);
			return;
		}
		List<Object> out_put_list=new ArrayList<Object>();
		out_put_list.add(position_list);
		out_put_list.add(bumen_list);
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(DeputyPosition.class, "id,name"));
		objs.add(new FilterObj(ZhiWei.class, "id,name,sort,is_deputyPosition,jobNum,total"));
		objs.add(new FilterObj(BuMen.class, "id,name,sort,group_id,superiorBumen"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, out_put_list, "查询成功", 0, filter);
	}
	@SuppressWarnings("unchecked")
	public List<ZhiWei> getFormatZhiweis(User user,String zhiweiIds){
		BuMen bumen = user.getBumen();
		if (bumen==null) {
			return null;
		}
		AreaGradeOfUser areaGradeOfUser = user.getAreaGradeOfUser();
		String where="";
		if (areaGradeOfUser!=null) {
			where=" AND su.areaGradeOfUser_id = " + areaGradeOfUser.getId()+" ";
		}
		String sql="SELECT "+
				  "temp2.id, "+
				  "temp2.name, "+
				  "( "+
				    "CASE "+
				      "WHEN temp2.userName IS NULL "+
				      "THEN 0 "+
				      "ELSE 1 "+
				    "END "+
				  ") AS num "+ 
				"FROM "+
				  "(SELECT "+ 
				    "obj.id, "+
				    "obj.name, "+
				    "temp.userName "+ 
				  "FROM "+
				    "(SELECT "+ 
				      "su.userName, "+
				      "su.zhiwei_id "+ 
				    "FROM "+
				      "shopping_user AS su "+
				    "WHERE su.zhiwei_id IN (" + zhiweiIds + ") "+
				      "AND su.bumen_id = " + bumen.getId() + where + ") AS temp "+
				    "RIGHT JOIN "+
				      "(SELECT "+
				        "ez.name, "+
				        "ez.id "+
				      "FROM "+
				        "ecm_zhiwei AS ez "+
				      "WHERE ez.id IN (" + zhiweiIds + ")) AS obj "+
				      "ON obj.id = temp.zhiwei_id) AS temp2 "+
				"ORDER BY temp2.id ";
		List<Object[]> query = commonService.executeNativeNamedQuery(sql);
		String position_hql="select obj from ZhiWei as obj where obj.id in (" + zhiweiIds + ") order by obj.id";
		List<ZhiWei> position_list=this.commonService.query(position_hql, null, -1, -1);
		if (query.size()==position_list.size()) {
			for (int i = 0; i < position_list.size(); i++) {
				position_list.get(i).setTotal(1);
				position_list.get(i).setJobNum(CommUtil.null2Int(query.get(i)[2]));
			}
		}		
		return position_list;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:根据用户等级自动给用户赋予职位，根据手机号的归属地来赋职到对应区域,如果手机号归属地错误或者手机号格式错误就随机任命
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	@SuppressWarnings("all")
	@RequestMapping(value="/automaticAppointmentByUserRank.htm")
	public void automaticAppointmentByUserRank(HttpServletRequest request,HttpServletResponse response,String userId){
		Long uId = CommUtil.null2Long(userId);
		if (uId.longValue()==-1l) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		User user = userService.getObjById(uId);
		if (user==null) {
			ApiUtils.json(response, "","用户不存在",1);
			return;
		}
		if (user.getZhiwei()!=null&&user.getZhiwei().getId()!=0) {
			ApiUtils.json(response, "","该用户已有职位",1);
			return;
		}
//		if (user.getUserRank()==null) {
//			user.setUserRank((UserRank) commonService.getById("UserRank", "4"));
//		}
		if (!user.getUserRank().getIsHaveZhiweiRight()) {
			ApiUtils.json(response, "","该用户没有任职权限",1);
			return;
		}
		String mobile = user.getMobile();
		String type = ApiUtils.judgmentType(CommUtil.null2String(mobile));
		String city="";
		String hql="";
		if ("mobile".equals(type)) {
			Map<String, Object> phoneNumBelongingPlace = ApiUtils.getPhoneNumBelongingPlace(mobile);
			city = (String) phoneNumBelongingPlace.get("city");
		}
		BuMen bumen = user.getBumen();
		if (bumen==null) {
			bumen=(BuMen) this.commonService.getById("BuMen", "301");
			user.setBumen(bumen);
		}
		if (CommUtil.null2String(user.getDan_bao_ren()).equals("")) {
			user.setDan_bao_ren("夏天");
		}
		if ((bumen.getId()>=601&&bumen.getId()<=610)) {
			hql="select obj from BuMen as obj where obj.superiorBumen.id = " + user.getBumen().getId();
			List<BuMen> bumens = commonService.query(hql, null, -1, -1);
			if (bumens.size()>0) {
				Integer randoms = ApiUtils.randoms(bumens.size(), 0);
				user.setBumen(bumens.get(randoms));
			}
		}
		AreaGradeOfUser area;
		area = this.getFreePosition(city);
		if (area==null) {
			area = this.getRandomArea(1);
		}
		Map<String, Object> freeInfo = this.getFreeInfo(user.getUserRank().getPositionOrder(), user.getBumen().getId()+"", area,1);
		Object areas = freeInfo.get("area");
		if (areas!=null) {//赋职
			area=(AreaGradeOfUser)commonService.getById("AreaGradeOfUser", CommUtil.null2String(areas));
			ZhiWei zhiwei=(ZhiWei) freeInfo.get("zhiwei");
			user.setAreaGradeOfUser(area);
			user.setZhiwei(zhiwei);
			boolean update = userService.update(user);
			if (update) {
				ApiUtils.pushNotice(user);//发送职位短信
				ZhiWeiRecoderEntity zre=new ZhiWeiRecoderEntity();
				zre.setAddTime(new Date());
				zre.setDeleteStatus(false);
				zre.setUser(userService.getObjById(150382l));//第一商城职位职衔管理系统
				zre.setMyselfUser(user);
				zre.setZhiwei(zhiwei);
				if(user.getAreaGradeOfUser()!=null){
					String areaName=user.getAreaGradeOfUser()==null?"":user.getAreaGradeOfUser().getName();
					zre.setMsg(areaName+zhiwei.getName());
				}else{
					zre.setMsg(zhiwei.getName());
				}
				this.commonService.save(zre);
				ApiUtils.json(response, "","授职成功",0);
				return;
			}
		}else {
			ApiUtils.json(response, "","授职失败，职位已满",1);
			return;
		}
	}
	/***
	 *@author:gaohao
	 *@return:AreaGradeOfUser
	 *@param:**
	 *@description:根据用户手机号的归属地获取地区信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private AreaGradeOfUser getFreePosition(String city){
		if ("未知".equals(city)||"".equals(city)) {
			return null;
		}
		String sql="SELECT "+
				" obj.id "+ 
				"FROM "+
				  "shopping_area_grade_of_user AS obj "+ 
				"WHERE pid IN "+ 
				  "(SELECT "+
				    "id "+ 
				  "FROM "+
				    "shopping_area_grade_of_user AS obj "+ 
				  "WHERE obj.pid >= 767476 "+ 
				    "AND obj.pid <= 767483 "+
				    "AND obj.id NOT IN (767484, 767487, 767486)) "+
				  "AND obj.NAME LIKE '%"+city+"%'";
		List<?> query=commonService.executeNativeNamedQuery(sql);
		if (query.size()<=0) {
			return null;
		}
		return (AreaGradeOfUser) commonService.getById("AreaGradeOfUser", CommUtil.null2String(query.get(0)));
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getFreeInfo(Integer positionOrder,String bumenId,AreaGradeOfUser area,int i){
		Map<String, Object> info=new HashMap<>();
		i++;
		if (i>5) {
			return info;
		}
		String hql="select obj from ZhiWei AS obj where obj.name NOT LIKE '%副%' AND obj.positionOrder <= " + positionOrder + " order by obj.positionOrder DESC";
		List<ZhiWei> zhiweis = commonService.query(hql, null, 0, 1);
		if (zhiweis.size()<=0) {
			return null;
		}
		ZhiWei zhiWei=zhiweis.get(0);
		info.put("zhiwei", zhiWei);
		boolean is=true;
		if (area!=null) {
			String areaGradeOfUserIds = this.getAreaGradeOfUserIds(positionOrder, area);
			String sql="SELECT "+
					  "sagou.id "+
					"FROM "+
					  "shopping_area_grade_of_user AS sagou "+
					  "LEFT JOIN "+
					    "(SELECT "+
					      "su.areaGradeOfUser_id "+
					    "FROM "+
					      "shopping_user AS su "+
					    "WHERE su.zhiwei_id = " + zhiWei.getId() + " "+
					      "AND su.bumen_id = "+bumenId+") AS temp "+
					    "ON sagou.id = temp.areaGradeOfUser_id "+
					"WHERE sagou.id in (" + areaGradeOfUserIds + ") " +
					  " AND temp.areaGradeOfUser_id IS NULL limit 0,1";
			List<?> list = commonService.executeNativeNamedQuery(sql);
			if (list.size()>0) {
				info.put("area", list.get(0));
				is=false;
			}
		}
		if (is&&this.getAreaByUserRank(positionOrder)<=4) {//大区、省不需要递归，每次查询到的都是一样的
			return this.getFreeInfo(positionOrder, bumenId,this.getRandomArea(1),i);
		}
		return info;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:根据职位权限高低判断地区等级，1:村;2:镇;3:县;4:市;5:省;6:大区;
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private Integer getAreaByUserRank(Integer positionOrder){
		Integer rank=1;
		if (positionOrder>=40&&positionOrder<=45) {
			rank=1;
		}else if (positionOrder>=35&&positionOrder<40) {
			rank=2;
		}else if (positionOrder>=30&&positionOrder<35) {
			rank=3;
		}else if (positionOrder>=25&&positionOrder<30) {
			rank=4;
		}else if (positionOrder>=20&&positionOrder<25) {
			rank=5;
		}else if (positionOrder>=15&&positionOrder<20) {
			rank=6;
		}
		return rank;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:获取上下级地区的id
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private String getAreaGradeOfUserIds(Integer positionOrder,AreaGradeOfUser area){
		Integer areaByUserRank = this.getAreaByUserRank(positionOrder);
		areaByUserRank=areaByUserRank-4;
		String id=area.getId()+"";
		String sql="";
		if (areaByUserRank>=0) {//大区、省、市
			sql="SELECT obj.id FROM shopping_area_grade_of_user as obj WHERE obj.pid=1 AND obj.id <> 767483";
			List<?> query = commonService.executeNativeNamedQuery(sql);
			id=query.toString().substring(1, query.toString().length()-1);
			areaByUserRank=2-areaByUserRank;
		}else {
			areaByUserRank=-areaByUserRank;
		}
		for (int i = 0; i < areaByUserRank; i++) {
			if (id.length()<=0) {
				break;
			}
			sql="select count(1) from shopping_area_grade_of_user as obj where obj.pid in( " + id +")";
			List<?> num=commonService.executeNativeNamedQuery(sql);
			String where="";
			if (CommUtil.null2Int(num.get(0))>100) {
				where=" limit 0,100";
			}
			sql="select obj.id from shopping_area_grade_of_user as obj where obj.pid in( " + id +") ORDER BY rand()" + where;
			List<?> query = commonService.executeNativeNamedQuery(sql);
			id=query.toString().substring(1, query.toString().length()-1);
		}
		if (id.length()<=0) {
			return getAreaGradeOfUserIds(positionOrder,this.getRandomArea(1));
		}
		return id;
	}
	/***
	 *@author:gaohao
	 *@return:**
	 *@param:**
	 *@description:随机生成一个市
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private AreaGradeOfUser getRandomArea(int i){
		if (i>5) {
			return null;
		}
		i++;
		String sql="";
		String areaId="";
		Integer randoms = ApiUtils.randoms(434, 0);
		sql="SELECT "+
				" obj.id "+ 
				"FROM "+
				  "shopping_area_grade_of_user AS obj "+ 
				"WHERE pid IN "+ 
				  "(SELECT "+
				    "id "+ 
				  "FROM "+
				    "shopping_area_grade_of_user AS obj "+ 
				  "WHERE obj.pid >= 767476 "+ 
				    "AND obj.pid <= 767483 "+
				    "AND obj.id NOT IN (767484, 767487, 767486)) "+
				  " limit " + randoms + ",1";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		if (query.size()>0) {
			areaId=CommUtil.null2String(query.get(0));
		}else {
			return this.getRandomArea(i);
		}
		return (AreaGradeOfUser) commonService.getById("AreaGradeOfUser", areaId);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:自动授予职衔
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/autoConferZhiXian.htm")
	public void autoConferZhiXian(HttpServletRequest request,
			HttpServletResponse response,String userId){
		Long uId = CommUtil.null2Long(userId);
		if (uId.longValue()==-1l) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		User user = userService.getObjById(uId);
		if (user==null) {
			ApiUtils.json(response, "","用户不存在",1);
			return;
		}
		if (user.getZhixian()==null) {
			user.setZhixian((ZhiXianEntity) this.commonService.getById("ZhiXianEntity", "1"));
			userService.update(user);
		}
		String sql="";
		ZhiXianEntity zhixian=null;
		sql="SELECT "+
				  "id "+
				"FROM "+
				  "shopping_zhixian AS sz "+ 
				"WHERE sz.upgradeMoney <= "+ 
				  "(SELECT "+ 
				    "SUM(obj.totalPrice) "+ 
				  "FROM "+
				    "shopping_orderform AS obj "+ 
				  "WHERE obj.order_status IN (40, 50, 60) "+ 
				    "AND obj.user_id = " + uId + ") "+
				"ORDER BY rankOrder "+ 
				"LIMIT 0, 1 ";
		List<?> query = commonService.executeNativeNamedQuery(sql);
		if (query.size()>0) {
			zhixian = (ZhiXianEntity) commonService.getById("ZhiXianEntity", CommUtil.null2String(query.get(0)));
		}
		if (zhixian==null) {
			sql="SELECT "+
					  "id "+
					"FROM "+
					  "shopping_zhixian AS sz "+
					"WHERE sz.upgradeOrderNum <= "+
					  "(SELECT "+
					    "COUNT(1) "+
					  "FROM "+
					    "shopping_orderform AS obj "+
					  "WHERE obj.order_status IN (40, 50, 60) "+
					    "AND obj.user_id = " + uId + ") "+
					"ORDER BY rankOrder "+
					"LIMIT 0, 1 ";
			query = commonService.executeNativeNamedQuery(sql);
			if (query.size()>0) {
				zhixian = (ZhiXianEntity) commonService.getById("ZhiXianEntity", CommUtil.null2String(query.get(0)));
			}
		}
		if (zhixian!=null&&zhixian.getRankOrder()<user.getZhixian().getRankOrder()) {
			ZhiXianRecorderEntity zrc=new ZhiXianRecorderEntity();
			zrc.setAddTime(new Date());
			zrc.setDeleteStatus(false);
			zrc.setUser(userService.getObjById(150382l));//第一商城职位职衔管理系统
			zrc.setMyselfUser(user);
			zrc.setZhixian(zhixian);
			zrc.setMsg("授为"+zhixian.getName()+"职衔");
			this.commonService.save(zrc);
			List<ZhiXianRecorderEntity> res=user.getZhixianRec();
			res.add(zrc);
			user.setZhixian(zhixian);
			user.setZhixianRec(res);
			boolean ret=this.userService.update(user);
			String msg=user.getUserName()+"你好！你目前被授予为"+zhixian.getName()+"职衔，请你继续保持昂扬斗志，帮助战友们做好布局和服务工作，加油";
			this.send_message(user, msg);
			if(ret){
				ApiUtils.pushRemindTitleSMS(user.getUserName(), user.getMobile());
				ApiUtils.json(response, "", "授衔成功", 0);
				return;
			}
		}	
		ApiUtils.json(response, "", "职衔无变动", 1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:自动升降职位,需求变更已淘汰，后期可删除
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
//	@RequestMapping(value = "/autoManagePosition.htm")
	public void autoManagePosition(HttpServletRequest request,HttpServletResponse response){
		String time=ApiUtils.getFirstday_Lastday_Month(CommUtil.formatTime( "yyyy-MM-dd",new Date()), 0);
		String hql="select obj from UserMonthlyBill as obj where obj.addTime > '" + time + "'";
		hql="SELECT obj.user_id,obj.daogouMoney,obj.zhaoshangMoney,obj.danbaoMoney FROM shopping_user_monthly_bill AS obj WHERE obj.addTime>'" + time + "' order by obj.daogouMoney desc";
		@SuppressWarnings("unchecked")
		List<Object[]> bills=this.commonService.executeNativeNamedQuery(hql);
		if (bills.size()<=0) {
			return;
		}
		Map<Long, Double> billMap=new HashMap<>();
		for (Object[] umb : bills) {
			billMap.put(CommUtil.null2Long(umb[0]), CommUtil.null2Double(umb[1])+CommUtil.null2Double(umb[2])+CommUtil.null2Double(umb[3]));
		}
		User user=null;
		Set<Long> keySet = billMap.keySet();
		for (Long uId : keySet) {
			System.out.println(uId);
			try {
				user = this.userService.getObjById(uId);
				if (user.getBumen()==null) {
					user.setBumen((BuMen) this.commonService.getById("BuMen", "301"));
				}
				String userState = ApiUtils.getUserState(commonService, user);
				if ("1".equals(userState)||!user.getUserRank().getIsHaveZhiweiRight()) {//如果为非活跃会员或者非领袖会员，则不需要晋升
					continue;
				}
				Double userMoney = billMap.get(user.getId());
				ApiUtils.updateUserRenk(0, user, this.commonService, this.userService);
				if (user!=null&&user.getUserRank().getIsHaveZhiweiRight()&&user.getZhiwei().getPositionOrder()>=10) {
					ZhiWei leaderMember = this.getLeaderMember(user, 1);//上级职位
					List<User> users = this.getLeaderMemberUser(user, 1, leaderMember,null);//上级
					if (users!=null&&users.size()>0) {
						User superiorUser=users.get(0);
						//存在上级
						String leaderMemberState = ApiUtils.getUserState(commonService, superiorUser);
						if (leaderMemberState.equals("1")) {//上级没有动态
							users = this.getLeaderMemberUser(user, 0, leaderMember,superiorUser);//下级
							if (users!=null&&users.size()>1) {
								for (User u : users) {
									//递归找最活跃的会员替换职位
									Double money = billMap.get(u.getId());
									if (money!=null&&money>userMoney) {
										userMoney=money;
										user=u;
									}
								}
							}
							//替换职位
							boolean is = this.correctPosition(user);
							if (!is&&(user.getZhiwei().getId()==14||user.getZhiwei().getId()==120)) {
								continue;
							}
							this.exchangePosition(user, superiorUser);
						}
					}else{
						//上级没人，可以直接晋升；
						boolean is = this.correctPosition(user);
						if (!is&&(user.getZhiwei().getId()==14||user.getZhiwei().getId()==120)) {
							continue;
						}
						if (leaderMember.getPositionOrder()<=15&&leaderMember.getId()!=120) {
							user.setAreaGradeOfUser(null);
						}else {
							if (user.getZhiwei().getName().indexOf("副")==-1) {
								user.setAreaGradeOfUser((AreaGradeOfUser) commonService.getById("AreaGradeOfUser", user.getAreaGradeOfUser().getPid()+""));
							}
						}
						user.setZhiwei(leaderMember);
						boolean update = userService.update(user);
						this.savePositionLog(user);
						if (update) {
							//发送晋升信息
						}
					}
				}
			} catch (Exception e) {
				UserPositionError userPositionError=new UserPositionError(new Date());
				userPositionError.setDeleteStatus(false);
				userPositionError.setUser(user);
				this.commonService.save(userPositionError);
				e.printStackTrace();
			}
		}
		hql="SELECT obj.id FROM shopping_userpositionerror AS obj WHERE obj.addTime >='"+time+"'";
		List<?> query = commonService.executeNativeNamedQuery(hql);
		if (query.size()>0) {
			CommUtil.send_messageToSpecifiedUser(userService.getObjById(1l),"本月自动升职检测完毕，有部分会员职位与地区不匹配，请到管理页面查看",userService);
		}
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:type:0为下级；1为上级
	 *@description:获取上下级职位
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private ZhiWei getLeaderMember(User user,int type){
		String hql="";
		if (type==0) {
			hql="select obj from ZhiWei as obj where obj.positionOrder > "+user.getZhiwei().getPositionOrder()+" and obj.name not like '%副%' order by obj.positionOrder DESC";
		}else {
			hql="select obj from ZhiWei as obj where obj.positionOrder < "+user.getZhiwei().getPositionOrder()+" and obj.name not like '%副%' order by obj.positionOrder DESC";
		}
		@SuppressWarnings("unchecked")
		List<ZhiWei> zhiWeis=this.commonService.query(hql, null, 0, 1);
		if (zhiWeis.size()>0) {
			return zhiWeis.get(0);
		}
		return null;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:type:0为下级；1为上级
	 *@description:获取上下级用户
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private List<User> getLeaderMemberUser(User user,int type,ZhiWei zhiWei,User superiorUser){
		String where ="";
		List<User> users=null;
		String hql="";
		ZhiWei zw = user.getZhiwei();
		BuMen bumen = user.getBumen();
		if (type==1) {
			if (zw.getId()==17) {
				bumen=bumen.getSuperiorBumen();
			}
			if (zhiWei.getPositionOrder()>=15) {
				if (zw.getName().indexOf("副")==-1) {//正职
					where=" and obj.areaGradeOfUser.id = " + user.getAreaGradeOfUser().getPid();
				}else {//副职
					where=" and obj.areaGradeOfUser.id = " + user.getAreaGradeOfUser().getId();
				}
			}
			hql="select obj from User as obj where obj.zhiwei.id = " + zhiWei.getId() +" and obj.bumen.id = " + bumen.getId() + where;
			users = this.userService.query(hql, null, -1, -1);
		}else if (type==0) {
			String sql="select obj.id from ecm_zhiwei as obj where obj.positionOrder = "+zw.getPositionOrder()+" and obj.name not like '%副%'";
			List<?> query = commonService.executeNativeNamedQuery(sql);
			if (query.size()>0) {
				String bumenids=user.getBumen().getId()+"";
				if (CommUtil.null2Long(query.get(0))==17) {
					String ids = ApiUtils.getSubclassBumenIds(superiorUser.getBumen().getId(), commonService);
					if (!ids.equals("")) {
						bumenids=bumenids+","+ids;
					}
				}
				if (zw.getPositionOrder()>15) {
					where=" and obj.areaGradeOfUser.pid = "+user.getAreaGradeOfUser().getPid();
				}else {
					where = "";
				}
				hql="select obj from User as obj where obj.zhiwei.id = " + query.get(0) +" and obj.bumen.id in (" + bumenids + ") " + where;
				users = userService.query(hql, null, -1, -1);
			}
			sql="select obj.id from ecm_zhiwei as obj where obj.positionOrder = "+zw.getPositionOrder()+" and obj.name like '%副%'";
			query = commonService.executeNativeNamedQuery(sql);
			if (query.size()>0) {
				String ids = query.toString().substring(1, query.toString().length()-1);
				if (superiorUser.getAreaGradeOfUser()!=null) {
					where=" and obj.areaGradeOfUser.id = "+superiorUser.getAreaGradeOfUser().getId();
				}else {
					where = "";
				}
				hql="select obj from User as obj where obj.zhiwei.id in (" + ids + ") and obj.bumen.id = " + superiorUser.getBumen().getId() + where;
				List<User> us = userService.query(hql, null, -1, -1);
				users.addAll(us);
			}
		}
		if (users.size()>0) {
			return users;
		}
		return null;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:user:下级用户 superiorUser：要替换掉的上级用户
	 *@description:互换职位
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private Boolean exchangePosition(User user,User superiorUser){
		ZhiWei zhiwei = user.getZhiwei();
		AreaGradeOfUser area = user.getAreaGradeOfUser();
		BuMen bumen = user.getBumen();
		user.setZhiwei(superiorUser.getZhiwei());
		user.setAreaGradeOfUser(superiorUser.getAreaGradeOfUser());
		user.setBumen(superiorUser.getBumen());
		superiorUser.setZhiwei(zhiwei);
		superiorUser.setAreaGradeOfUser(area);
		superiorUser.setBumen(bumen);
		User[] users={user,superiorUser};
		boolean update = this.userService.updateBatchEntity(users);
		if (update) {
			this.savePositionLog(user);
			this.savePositionLog(superiorUser);
		}
		return update;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:保存职位操作日志
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private void savePositionLog(User user){
		ZhiWeiRecoderEntity zre=new ZhiWeiRecoderEntity();
		zre.setAddTime(new Date());
		zre.setDeleteStatus(false);
		zre.setUser(userService.getObjById(150382l));//第一商城职位职衔管理系统
		zre.setMyselfUser(user);
		zre.setZhiwei(user.getZhiwei());
		if(user.getAreaGradeOfUser()!=null){
			String areaName=user.getAreaGradeOfUser()==null?"":user.getAreaGradeOfUser().getName();
			zre.setMsg(user.getBumen().getName()+areaName+user.getZhiwei().getName());
		}else{
			zre.setMsg(user.getBumen().getName()+user.getZhiwei().getName());
		}
		this.commonService.save(zre);
	}
	/***
	 *@author:gaohao
	 *@return:boolean-true:
	 *@param:**
	 *@description:职位矫正，省，大区
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	private boolean correctPosition(User user){
		String sql="";
		String id="";
		if (user.getZhiwei().getId()==120) {//省总裁
			sql="SELECT obj.id FROM shopping_area_grade_of_user as obj WHERE obj.pid=1";
		}
		if (user.getZhiwei().getId()==14) {//大区总裁
			sql="SELECT id FROM shopping_area_grade_of_user AS sagou WHERE pid IN (SELECT obj.id FROM shopping_area_grade_of_user AS obj WHERE obj.pid=1)";
		}
		if (!sql.equals("")) {
			List<?> query = commonService.executeNativeNamedQuery(sql);
			id=query.toString().substring(1, query.toString().length()-1);
			int num = id.indexOf(user.getAreaGradeOfUser().getId()+"");
			if (num!=-1) {
				return true;
			}
			UserPositionError userPositionError=new UserPositionError(new Date());
			userPositionError.setDeleteStatus(false);
			userPositionError.setUser(user);
			this.commonService.save(userPositionError);
		}
		return false;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:每周根据业绩进行升降职位
	 *@function:**
	 *@exception:*******
	 *@method_detail:
	 *@variable:*******
	 ***/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/autoWeekManagePosition.htm")
	public void autoWeekManagePosition(HttpServletRequest request,HttpServletResponse response){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (1 == dayWeek) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		int day = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
		String newTime = ApiUtils.weeHours(CommUtil.formatLongDate(cal.getTime()), 0);
		String dayTime = ApiUtils.weeHours(ApiUtils.getFirstday_Lastday(CommUtil.formatDate(newTime), 0, 7),0);
		String hql = "select obj from UserWeekActivity as obj where obj.addTime >= '" + CommUtil.formatLongDate(cal.getTime()) + "'";
		List<UserWeekActivity> is_execute = this.commonService.query(hql, null, 0, 1);
		if (is_execute.size()>0) {
			ApiUtils.json(response, "", "本周晋升体系已被执行", 1);
			return;
		}
		String sql = "SELECT "+
						  "SUM(so.totalPrice), "+
						  "so.user_id "+
						"FROM "+
						  "shopping_orderform AS so "+
						"WHERE so.order_status IN (20, 30, 40, 50, 60) "+
						  "AND so.payTimes >= '" + dayTime + "' "+ 
						  "AND so.payTimes < '" + newTime + "' "+
						"GROUP BY so.user_id order by SUM(so.totalPrice) desc";
		List<Object[]> query = this.commonService.executeNativeNamedQuery(sql);
		for (Object[] objs : query) {
			User u = this.userService.getObjById(CommUtil.null2Long(objs[1]));
			UserWeekActivity uwa = new UserWeekActivity();
			uwa.setAddTime(new Date());
			uwa.setDeleteStatus(false);
			uwa.setUser(u);
			uwa.setMoneySum(CommUtil.null2Double(objs[0]));
			this.commonService.save(uwa);
		}
		sql = "SELECT "+
				  "su.id, "+
				  "obj.num "+
				"FROM "+
				  "shopping_user AS su "+
				  "RIGHT JOIN "+
				    "(SELECT "+
				      "COUNT(1) AS num, "+
				      "su.dan_bao_ren "+ 
				    "FROM "+
				      "shopping_user AS su "+
				    "WHERE su.addTime >= '" + dayTime + "' "+
				      "AND su.addTime < '" + newTime + "' "+
				    "GROUP BY dan_bao_ren) AS obj "+
				    "ON su.userName = obj.dan_bao_ren "+
				"ORDER BY num DESC";
		query = this.commonService.executeNativeNamedQuery(sql);
		for (Object[] objs : query) {
			User u = this.userService.getObjById(CommUtil.null2Long(objs[0]));
			hql = "select obj from UserWeekActivity as obj where obj.user.id = " + objs[0] + " and obj.addTime >= '" + newTime + "'";
			List<UserWeekActivity> list = this.commonService.query(hql, null, -1, -1);
			UserWeekActivity uwa = null;
			int num = CommUtil.null2Int(objs[1]);
			if (list.size()>0) {
				uwa = list.get(0);
				uwa.setInviteNum(num);
				this.commonService.update(uwa);
			}else {
				uwa = new UserWeekActivity();
				uwa.setAddTime(new Date());
				uwa.setDeleteStatus(false);
				uwa.setUser(u);
				uwa.setInviteNum(num);
				this.commonService.save(uwa);
			}
		}
		hql = "select obj from UserWeekActivity as obj where obj.addTime >= '" + newTime + "'";
		List<UserWeekActivity> uwas = this.commonService.query(hql, null, -1, -1);
		Map<Long, UserWeekActivity> map = new HashMap<>();
		for (UserWeekActivity uwa : uwas) {
			map.put(uwa.getUser().getId(),uwa);
		}
		User user = null;
		for (UserWeekActivity uwa : uwas) {
			UserWeekActivity uw = uwa;
			try{
				user = uw.getUser();
				if (user.getBumen()==null) {
					user.setBumen((BuMen) this.commonService.getById("BuMen", "301"));
				}
				String userState = ApiUtils.getUserState(commonService, user);
				if ("1".equals(userState)||!user.getUserRank().getIsHaveZhiweiRight()) {//如果为非活跃会员或者非领袖会员，则不需要晋升
					continue;
				}
				ApiUtils.updateUserRenk(0, user, this.commonService, this.userService);//更新会员等级
				if (user!=null&&user.getUserRank().getIsHaveZhiweiRight()&&user.getZhiwei().getPositionOrder()>=10) {//排除战区领导人和创始人
					ZhiWei leaderMember = this.getLeaderMember(user, 1);//上级职位
					List<User> users = this.getLeaderMemberUser(user, 1, leaderMember,null);//上级
					if (users!=null&&users.size()>0) {
						User superiorUser=users.get(0);
						if(map.get(superiorUser.getId())!=null){//上级也有下单邀请记录，则直接跳过
							continue;
						}
						//存在上级
						String leaderMemberState = ApiUtils.getUserState(commonService, superiorUser);
						if (leaderMemberState.equals("1")) {//上级没有动态
							users = this.getLeaderMemberUser(user, 0, leaderMember,superiorUser);//下级
							if (users!=null&&users.size()>1) {
								for (User u : users) {
									//递归找最活跃的会员替换职位，优先比较销售额，相同则比较邀请人数
									UserWeekActivity userWeekActivity = map.get(u.getId());
									if (userWeekActivity==null) {
										continue;
									}
									if (CommUtil.null2Double(uw.getMoneySum())<CommUtil.null2Double(userWeekActivity.getMoneySum())) {
										uw = userWeekActivity;
										user=u;
									}else if (CommUtil.null2Double(uw.getMoneySum())==CommUtil.null2Double(userWeekActivity.getMoneySum())) {
										if (CommUtil.null2Int(uw.getInviteNum())<CommUtil.null2Int(userWeekActivity.getInviteNum())) {
											uw = userWeekActivity;
											user=u;
										}
									}
								}
							}
							//替换职位
							boolean is = this.correctPosition(user);
							if (!is&&(user.getZhiwei().getId()==14||user.getZhiwei().getId()==120)) {
								continue;
							}
							this.exchangePosition(user, superiorUser);
						}
					}else{
						//上级没人，可以直接晋升；
						boolean is = this.correctPosition(user);
						if (!is&&(user.getZhiwei().getId()==14||user.getZhiwei().getId()==120)) {
							continue;
						}
						if (leaderMember.getPositionOrder()<=15&&leaderMember.getId()!=120) {
							user.setAreaGradeOfUser(null);
						}else {
							if (user.getZhiwei().getName().indexOf("副")==-1) {
								user.setAreaGradeOfUser((AreaGradeOfUser) commonService.getById("AreaGradeOfUser", user.getAreaGradeOfUser().getPid()+""));
							}
						}
						user.setZhiwei(leaderMember);
						boolean update = userService.update(user);
						this.savePositionLog(user);
					}
				}
			}catch (Exception e) {
				UserPositionError userPositionError=new UserPositionError(new Date());
				userPositionError.setDeleteStatus(false);
				userPositionError.setUser(user);
				this.commonService.save(userPositionError);
				e.printStackTrace();
			}
		}
	}
}
