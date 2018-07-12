package com.shopping.api.timer.timingAction;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.materialCircle.VPResource;
import com.shopping.api.service.materialCircle.IMCFunctionService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

/***
 * @author:akangah
 * @description:自动确认收货action
 ***/
@Controller
public class TimeProcessingAction {
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	@Qualifier("vPResourceServiceImpl")
	private IMCFunctionService<VPResource> vPResourceService;

	/***
	 * @author:akangah
	 * @return:void
	 * @param:**
	 * @description:使用配置好的定时器去触发这个action,然后再给指定的人员分配钱
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping({ "/confirmRecGoodsAndAllocateMoney.htm" })
	public synchronized final void confirmRecGoodsAndAllocateMoney(
			HttpServletRequest request, HttpServletResponse response) {
		String nowTime = CommUtil.formatLongDate(new Date());
		int num = 0;
		double totalTime = 0D;
		String hql = "select obj from OrderForm as obj where obj.order_status=30 and obj.finishTime<'"
				+ nowTime + "'" + "and obj.finishTime is not null";
		List<OrderForm> ofList = this.orderFormService.query(hql, null, -1, -1);
		System.out.println(CommUtil.formatLongDate(new Date())
				+ "自动确认收货开始执行。。。。。");
		Long timeStamp1 = System.currentTimeMillis();
		for (OrderForm of : ofList) {// 如果订单的完成时间是在现在时间的前面，说明该订单超时了
			if (!"".equals(CommUtil.null2String(of.getFinishTime()))
					&& of.getFinishTime().before(new Date())) {
				User user = of.getUser();
				boolean ret = AllocateWagesUtils.createDistributionParams(of,
						user, predepositLogService, commonService,
						orderFormService, userService, orderFormLogService);
				if (ret) {
					OrderFormLog ofl = new OrderFormLog();
					ofl.setAddTime(new Date());
					ofl.setLog_info("确认收货");
					ofl.setLog_user(user);
					ofl.setOf(of);
					this.orderFormLogService.save(ofl);
					num++;
				}
			}
		}
		Long timeStamp2 = System.currentTimeMillis();
		totalTime = (timeStamp2 - timeStamp1) / 1000;
		CommUtil.send_messageToSpecifiedUser(userService.getObjById(137054L),
				CommUtil.formatLongDate(new Date()).toString()+ "系统自动确认收货总共耗时=" + totalTime + "秒" + "，总共" + num
						+ "条订单", userService);
		System.out.println(CommUtil.formatLongDate(new Date())+ "自动确认收货结束执行。。。。。");
	}

	@RequestMapping(value = "/timedDeleteUploadGoodsPhoto.htm")
	public void timedDeleteUploadGoodsPhoto(HttpServletRequest request,
			HttpServletResponse response) {
		String hql = "select obj from Accessory as obj where obj.deleteStatus=true and obj.config is not null";
		List<Accessory> photoList = this.accessoryService.query(hql, null, -1,-1);
		Iterator<Accessory> photoIter = photoList.iterator();
		Accessory photo = null;
		File file = null;
		boolean excuRet = false;
		String photoExt = "";
		Long photoId = 0L;
		while (photoIter.hasNext()) {
			photo = photoIter.next();
			file = new File(SystemResPath.imgUploadUrl + "/" + photo.getPath()+ "/" + photo.getName());
			photoExt = photo.getExt().indexOf(".") < 0 ? "." + photo.getExt(): photo.getExt();// 得到后缀名
			photoId = photo.getId();
			if (file.exists()) {
				excuRet = file.delete();
				if (excuRet) {
					file = new File(SystemResPath.imgUploadUrl + "/"+ photo.getPath() + "/" + photo.getName()+ "_small" + photoExt);
					if (file.exists()) {
						file.delete();
					}
					file = new File(SystemResPath.imgUploadUrl + "/"+ photo.getPath() + "/" + photo.getName()+ "_middle" + photoExt);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			try {
				this.accessoryService.delete(photoId);
			} catch (Exception e) {
				// TODO: handle exception
				photo.setDeleteStatus(false);
				this.accessoryService.update(photo);
			}
		}
	}
	@RequestMapping(value = "/timedCleanMcUnusedRes.htm")
	public void timedCleanMcUnusedRes(HttpServletRequest request,
			HttpServletResponse response) {
		String hql="select obj from VPResource as obj where obj.deleteStatus=true";
		List<VPResource> retList=this.vPResourceService.query(hql, null, -1, -1);
		File file=null;
		for(VPResource vResource:retList){
			if(vResource!=null){
				if(vResource.getCoverPhoto()!=null){//只有主图才可以删除
					file=new File(SystemResPath.imgUploadUrl+vResource.getPath()+"/"+vResource.getName());
					if(file!=null&&file.exists()){
						file.delete();//删除原文件
						file=new File(SystemResPath.imgUploadUrl+vResource.getCoverPhoto().getPath()+"/"+vResource.getCoverPhoto().getName());
						file.delete();//删除封面图
						this.vPResourceService.remove(CommUtil.null2Long(vResource.getId()));
					}
				}
			}
		}
	}
}
