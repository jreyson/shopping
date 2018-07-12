package com.shopping.api.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.ZhiWeiRecoderEntity;
import com.shopping.api.tools.ApiUtils;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;

@Controller
public class AddUserGladTidings {
	@Autowired
	private ICommonService commonService;
	@RequestMapping(value="/app_acquire_gladTids.htm")
	public void app_acquire_gladTids(HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
	    long millis=System.currentTimeMillis();
		Date startDate=new Date(millis);
		Date endDate=new Date(millis+(86400*1000));
		String beginTime=sft.format(startDate);
		String endTime=sft.format(endDate);
		/*String sql1="SELECT temp.addUserTime,temp.addUserName,sur.userName,sur.bumen_id,sur.areaGradeOfUser_id FROM shopping_user AS sur RIGHT JOIN "+ 
					"( "+
							"SELECT  su.userName AS addUserName,su.addTime AS addUserTime,su.dan_bao_ren FROM shopping_user AS su WHERE su.addTime>'2017-09-07 00:00:00' AND su.addTime<'2017-09-07 23:59:59' "+
					" ) AS temp ON temp.dan_bao_ren=sur.userName ";*/
		//注意  在sqlYog中对俩张关联表中同样名称的字段查询同时起别名是没有影响的,但是在jdbc查询中,不能够同时起别名,否则会抛出异常
		String sql="SELECT temp4.addUserTime,temp4.addUserName,temp4.userName,temp4.areaName,temp4.bumenName,eb.name  FROM ecm_bumen AS eb RIGHT JOIN "+ 
					"( "+
						"SELECT temp3.addUserTime, temp3.addUserName,temp3.userName,temp3.areaName,eb.name AS bumenName,eb.superiorBumen_id FROM  ecm_bumen  AS eb RIGHT JOIN "+ 
							"( "+
								"SELECT temp2.addUserTime, temp2.addUserName,temp2.userName,temp2.bumen_id,sagou.name as areaName FROM shopping_area_grade_of_user AS sagou RIGHT JOIN "+
									"( "+
										"SELECT temp.addUserTime,temp.addUserName,sur.userName,sur.bumen_id,sur.areaGradeOfUser_id FROM shopping_user AS sur RIGHT JOIN  "+ 
										"( "+
											"SELECT  su.userName AS addUserName,su.addTime AS addUserTime,su.dan_bao_ren FROM shopping_user AS su WHERE su.addTime>'"+beginTime+"' AND su.addTime<'"+endTime+"' "+ 
										") AS temp ON temp.dan_bao_ren=sur.userName  "+
									") AS temp2 ON temp2.areaGradeOfUser_id=sagou.id "+ 
							") AS temp3  ON  temp3.bumen_id=eb.id "+
						") AS temp4 ON temp4.superiorBumen_id=eb.id order by temp4.addUserTime desc LIMIT 0,20";
		List<?> gladTids=this.commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, gladTids,"修改店铺成功", 0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:职位公告浮窗
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_acquire_zhiwei.htm"})
	public void appAcquireZhiwei(HttpServletRequest request,
			HttpServletResponse response) {
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
	    long millis=System.currentTimeMillis();
		Date startDate=new Date(millis);
		Date endDate=new Date(millis+(86400*1000));
		String beginTime=sft.format(startDate);
		String endTime=sft.format(endDate);
		List<Object> result=new ArrayList<Object>();
		int i=1;
		do {
			String sql="SELECT temp6.addTime,temp6.mUserName,temp6.userName,temp6.areaName,temp6.bumenName,temp6.sName,temp6.msg,uzw.name from ecm_zhiwei AS uzw RIGHT JOIN"+
					"("+
					"SELECT temp5.addTime,temp5.mUserName,temp5.userName,temp5.areaName,temp5.bumenName,sbm.name AS sName,temp5.msg,temp5.zhiwei_id from ecm_bumen AS sbm RIGHT JOIN"+
					"("+
						"SELECT temp4.addTime,temp4.msg,temp4.userName,temp4.mUserName,temp4.name AS areaName,bm.name AS bumenName,bm.superiorBumen_id,temp4.zhiwei_id from ecm_bumen AS bm RIGHT JOIN"+
							"("+
								"SELECT temp3.addTime,temp3.msg,temp3.userName,temp3.bumen_id,temp3.mUserName,sagu.name,temp3.zhiwei_id from shopping_area_grade_of_user AS sagu RIGHT JOIN"+
									"("+
										"SELECT temp2.addTime,temp2.msg,temp2.userName,temp2.bumen_id,temp2.areaGradeOfUser_id,msu.userName AS mUserName,temp2.zhiwei_id from shopping_user AS msu RIGHT JOIN"+ 
											"("+
												"SELECT temp1.addTime,temp1.msg,temp1.myselfUser_id,su.userName,su.bumen_id,su.areaGradeOfUser_id,su.zhiwei_id from shopping_user AS su RIGHT JOIN"+ 
											"("+
										"SELECT obj.user_id,obj.addTime,obj.msg,obj.myselfUser_id FROM shopping_zhiwei_recorder AS obj where obj.addTime>'"+beginTime+"' AND obj.addTime<'"+endTime+"' ORDER BY obj.addTime DESC LIMIT 0,20"+
									") AS temp1 ON temp1.user_id=su.id"+
								") AS temp2 ON temp2.myselfUser_id=msu.id"+
							") AS temp3 ON temp3.areaGradeOfUser_id=sagu.id"+
						") AS temp4 ON temp4.bumen_id=bm.id"+
					") AS temp5 ON temp5.superiorBumen_id=sbm.id"+
						") AS temp6 ON temp6.zhiwei_id= uzw.id";
			List<?> list=this.commonService.executeNativeNamedQuery(sql);
			endTime=beginTime;
			startDate=new Date(millis-i*(86400*1000));
			beginTime=sft.format(startDate);
			if (list.size()!=0) {
				result.addAll(list);
			}		
			i++;
		} while (result.size()<5);
		ApiUtils.json(response, result,"获取职位公告成功", 0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:职位公告浮窗（最新20条）
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_acquire_zhiweis.htm"})
	public void appAcquireZhiweis(HttpServletRequest request,
			HttpServletResponse response) {
		String sql="SELECT temp6.addTime,temp6.mUserName,temp6.userName,temp6.areaName,temp6.bumenName,temp6.sName,temp6.msg,uzw.name from ecm_zhiwei AS uzw RIGHT JOIN"+
				"("+
				"SELECT temp5.addTime,temp5.mUserName,temp5.userName,temp5.areaName,temp5.bumenName,sbm.name AS sName,temp5.msg,temp5.zhiwei_id from ecm_bumen AS sbm RIGHT JOIN"+
				"("+
					"SELECT temp4.addTime,temp4.msg,temp4.userName,temp4.mUserName,temp4.name AS areaName,bm.name AS bumenName,bm.superiorBumen_id,temp4.zhiwei_id from ecm_bumen AS bm RIGHT JOIN"+
						"("+
							"SELECT temp3.addTime,temp3.msg,temp3.userName,temp3.bumen_id,temp3.mUserName,sagu.name,temp3.zhiwei_id from shopping_area_grade_of_user AS sagu RIGHT JOIN"+
								"("+
									"SELECT temp2.addTime,temp2.msg,temp2.userName,temp2.bumen_id,temp2.areaGradeOfUser_id,msu.userName AS mUserName,temp2.zhiwei_id from shopping_user AS msu RIGHT JOIN"+ 
										"("+
											"SELECT temp1.addTime,temp1.msg,temp1.myselfUser_id,su.userName,su.bumen_id,su.areaGradeOfUser_id,su.zhiwei_id from shopping_user AS su RIGHT JOIN"+ 
										"("+
									"SELECT obj.user_id,obj.msg,obj.myselfUser_id,obj.addTime FROM shopping_zhiwei_recorder AS obj WHERE obj.msg!='无职位' ORDER BY obj.addTime DESC LIMIT 0,20"+
								") AS temp1 ON temp1.user_id=su.id"+
							") AS temp2 ON temp2.myselfUser_id=msu.id"+
						") AS temp3 ON temp3.areaGradeOfUser_id=sagu.id"+
					") AS temp4 ON temp4.bumen_id=bm.id"+
				") AS temp5 ON temp5.superiorBumen_id=sbm.id"+
					") AS temp6 ON temp6.zhiwei_id= uzw.id";
			List<?> result=this.commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, result,"获取职位公告成功", 0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:职衔公告浮窗
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_acquire_zhixian.htm"})
	public void appAcquireZhixian(HttpServletRequest request,
			HttpServletResponse response) {
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
	    long millis=System.currentTimeMillis();
		Date startDate=new Date(millis);
		Date endDate=new Date(millis+(86400*1000));
		String beginTime=sft.format(startDate);
		String endTime=sft.format(endDate);
		List<Object> result=new ArrayList<Object>();
		int i=1;
		do {
				String sql="SELECT temp6.addTime,temp6.mUserName,temp6.userName,temp6.areaName,temp6.bumenName,temp6.sName,temp6.msg,uzx.name from shopping_zhixian AS uzx RIGHT JOIN"+
						"("+
							"SELECT temp5.addTime,temp5.mUserName,temp5.userName,temp5.areaName,temp5.bumenName,sbm.name AS sName,temp5.msg,temp5.zhixian_id from ecm_bumen AS sbm RIGHT JOIN"+
								"("+
									"SELECT temp4.addTime,temp4.msg,temp4.userName,temp4.mUserName,temp4.name AS areaName,bm.name AS bumenName,bm.superiorBumen_id,temp4.zhixian_id from ecm_bumen AS bm RIGHT JOIN"+
										"("+
											"SELECT temp3.addTime,temp3.msg,temp3.userName,temp3.bumen_id,temp3.mUserName,sagu.name,temp3.zhixian_id from shopping_area_grade_of_user AS sagu RIGHT JOIN"+
												"("+
													"SELECT temp2.addTime,temp2.msg,temp2.userName,temp2.bumen_id,temp2.areaGradeOfUser_id,msu.userName AS mUserName,temp2.zhixian_id from shopping_user AS msu RIGHT JOIN"+ 
														"("+
															"SELECT temp1.addTime,temp1.msg,temp1.myselfUser_id,su.userName,su.bumen_id,su.areaGradeOfUser_id,su.zhixian_id from shopping_user AS su RIGHT JOIN"+ 
														"("+
													"SELECT obj.user_id,obj.addTime,obj.msg,obj.myselfUser_id FROM shopping_zhixian_recorder AS obj where obj.addTime>'"+beginTime+"' AND obj.addTime<'"+endTime+"' ORDER BY obj.addTime DESC LIMIT 0,20"+
												") AS temp1 ON temp1.user_id=su.id"+
											") AS temp2 ON temp2.myselfUser_id=msu.id"+
										") AS temp3 ON temp3.areaGradeOfUser_id=sagu.id"+
									") AS temp4 ON temp4.bumen_id=bm.id"+
								") AS temp5 ON temp5.superiorBumen_id=sbm.id"+
							") AS temp6 ON temp6.zhixian_id= uzx.id";
			List<?> list=this.commonService.executeNativeNamedQuery(sql);
			endTime=beginTime;
			startDate=new Date(millis-i*(86400*1000));
			beginTime=sft.format(startDate);
			if (list.size()!=0) {
				result.addAll(list);
			}		
			i++;
		} while (result.size()<5);
		ApiUtils.json(response, result,"获取职衔公告成功", 0);
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:**
	 *@description:职衔公告浮窗(显示最新20条)
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/app_acquire_zhixians.htm"})
	public void appAcquireZhixians(HttpServletRequest request,
			HttpServletResponse response) {
		String sql="SELECT temp6.addTime,temp6.mUserName,temp6.userName,temp6.areaName,temp6.bumenName,temp6.sName,temp6.msg,uzx.name from shopping_zhixian AS uzx RIGHT JOIN"+
				"("+
					"SELECT temp5.addTime,temp5.mUserName,temp5.userName,temp5.areaName,temp5.bumenName,sbm.name AS sName,temp5.msg,temp5.zhixian_id from ecm_bumen AS sbm RIGHT JOIN"+
						"("+
							"SELECT temp4.addTime,temp4.msg,temp4.userName,temp4.mUserName,temp4.name AS areaName,bm.name AS bumenName,bm.superiorBumen_id,temp4.zhixian_id from ecm_bumen AS bm RIGHT JOIN"+
								"("+
									"SELECT temp3.addTime,temp3.msg,temp3.userName,temp3.bumen_id,temp3.mUserName,sagu.name,temp3.zhixian_id from shopping_area_grade_of_user AS sagu RIGHT JOIN"+
										"("+
											"SELECT temp2.addTime,temp2.msg,temp2.userName,temp2.bumen_id,temp2.areaGradeOfUser_id,msu.userName AS mUserName,temp2.zhixian_id from shopping_user AS msu RIGHT JOIN"+ 
												"("+
													"SELECT temp1.addTime,temp1.msg,temp1.myselfUser_id,su.userName,su.bumen_id,su.areaGradeOfUser_id,su.zhixian_id from shopping_user AS su RIGHT JOIN"+ 
												"("+
											"SELECT obj.user_id,obj.addTime,obj.msg,obj.myselfUser_id FROM shopping_zhixian_recorder AS obj ORDER BY obj.addTime DESC LIMIT 0,20"+
										") AS temp1 ON temp1.user_id=su.id"+
									") AS temp2 ON temp2.myselfUser_id=msu.id"+
								") AS temp3 ON temp3.areaGradeOfUser_id=sagu.id"+
							") AS temp4 ON temp4.bumen_id=bm.id"+
						") AS temp5 ON temp5.superiorBumen_id=sbm.id"+
					") AS temp6 ON temp6.zhixian_id= uzx.id";
			List<?> result=this.commonService.executeNativeNamedQuery(sql);
			ApiUtils.json(response, result,"获取职衔公告成功", 0);
	}
	@RequestMapping(value="/app_acquire_gladTid.htm")
	public void app_acquire_gladTid(HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat sft=new SimpleDateFormat("yyyy-MM-dd");
	    long millis=System.currentTimeMillis();
		Date startDate=new Date(millis);
		Date endDate=new Date(millis+(86400*1000));
		String beginTime=sft.format(startDate);
		String endTime=sft.format(endDate);
		/*String sql1="SELECT temp.addUserTime,temp.addUserName,sur.userName,sur.bumen_id,sur.areaGradeOfUser_id FROM shopping_user AS sur RIGHT JOIN "+ 
					"( "+
							"SELECT  su.userName AS addUserName,su.addTime AS addUserTime,su.dan_bao_ren FROM shopping_user AS su WHERE su.addTime>'2017-09-07 00:00:00' AND su.addTime<'2017-09-07 23:59:59' "+
					" ) AS temp ON temp.dan_bao_ren=sur.userName ";*/
		//注意  在sqlYog中对俩张关联表中同样名称的字段查询同时起别名是没有影响的,但是在jdbc查询中,不能够同时起别名,否则会抛出异常
		String sql="SELECT temp4.addUserTime,temp4.addUserName,temp4.userName,temp4.areaName,temp4.bumenName,eb.name  FROM ecm_bumen AS eb RIGHT JOIN "+ 
					"( "+
						"SELECT temp3.addUserTime, temp3.addUserName,temp3.userName,temp3.areaName,eb.name AS bumenName,eb.superiorBumen_id FROM  ecm_bumen  AS eb RIGHT JOIN "+ 
							"( "+
								"SELECT temp2.addUserTime, temp2.addUserName,temp2.userName,temp2.bumen_id,sagou.name as areaName FROM shopping_area_grade_of_user AS sagou RIGHT JOIN "+
									"( "+
										"SELECT temp.addUserTime,temp.addUserName,sur.userName,sur.bumen_id,sur.areaGradeOfUser_id FROM shopping_user AS sur RIGHT JOIN  "+ 
										"( "+
											"SELECT  su.userName AS addUserName,su.addTime AS addUserTime,su.dan_bao_ren FROM shopping_user AS su order by su.id desc LIMIT 0,20"+ 
										") AS temp ON temp.dan_bao_ren=sur.userName  "+
									") AS temp2 ON temp2.areaGradeOfUser_id=sagou.id "+ 
							") AS temp3  ON  temp3.bumen_id=eb.id "+
						") AS temp4 ON temp4.superiorBumen_id=eb.id";
		List<?> gladTids=this.commonService.executeNativeNamedQuery(sql);
		ApiUtils.json(response, gladTids,"获取喜报成功", 0);
	}
}
