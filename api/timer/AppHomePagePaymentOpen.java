package com.shopping.api.timer;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageSwitchEntity;
import com.shopping.api.service.IAppHomePageService;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IUserService;

/**
 * @author:akangah
 * @description:在app上开启首页付费功能,并通知用户参与抢购
 */
@Component("appHomePagePaymentOpen_excute")
public class AppHomePagePaymentOpen {
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IAppHomePageService apphomeService;
	public void execute(){
		/*System.out.println("功能开启啦");
		String switchQql="select obj from AppHomePageSwitchEntity as obj";
		List<?> appSwitchList=this.commonService.query(switchQql, null, -1,-1);
		AppHomePageSwitchEntity appSwitch=(AppHomePageSwitchEntity) appSwitchList.get(0);
		appSwitch.setIs_turnOn(true);*/
		System.out.println(CommUtil.formatLongDate(new Date())+"检测首页付费");
		String appSql="UPDATE shopping_apphome_switch  AS sa SET sa.is_turnOn=TRUE WHERE sa.id=1";
		//this.commonService.executeNativeNamedQuery(appSql);
		this.commonService.executeNativeSQL(appSql);
		Date date=new Date();
		String hql="select obj from AppHomePageEntity as obj";
		List<AppHomePageEntity> appHomeBannerList=this.apphomeService.query(hql, null, -1, -1);
		for(AppHomePageEntity appHomeBanner:appHomeBannerList){
			if(appHomeBanner.getFlush_time().before(date)&&appHomeBanner.getGoods()!=null){//如果从数据库中查出的时间比当前时间小的话
				appHomeBanner.setIs_can_buy(true);
				appHomeBanner.setGoods(null);
				this.apphomeService.update(appHomeBanner);
			}
		}
		String commonHql="select obj from AppHomePageCommonPosition as obj";
		List<AppHomePageCommonPosition> appHomeCommon=this.commonService.query(commonHql, null, -1, -1);
		for(AppHomePageCommonPosition commonPosition:appHomeCommon){//如果从数据库中查出的时间比当前时间小的话
			if(commonPosition.getFlush_time().before(date)&&commonPosition.getGoods()!=null){
				commonPosition.setIs_can_buy(true);
				commonPosition.setGoods(null);
				this.commonService.update(commonPosition);
			}
		}
		//if(appSwitch.getIs_sendMessage()){
/*			if(appHomeBannerList.size()>0||appHomeCommon.size()>0){
				String userSql="select count(*) from shopping_user";
				List<?> countList=this.commonService.executeNativeNamedQuery(userSql);
				float total_record=0f;
				int pageSize=20;
				int total_pages=0;
				total_record=CommUtil.null2Float(countList.get(0));
				System.out.println(countList.get(0));
				total_pages=(int)(total_record%pageSize==0?total_record/pageSize:Math.ceil(total_record/pageSize));
				for(int i=0;i<=total_pages;i++){
					String userHql="select obj from User as obj";
					List<User> userList=this.userService.query(userHql, null, pageSize*i, pageSize);
					for(User user:userList){
						String msg=user.getUserName()+"你好，抢占首页广告位开始了，快去抢吧！";
						this.send_message(user, msg);
					}
					userHql=null;
					userList=null;
				}
			}*/
		//}
	}
/*	private void send_message(User user,String msg){
		if(user.getIs_huanxin()==0){//如果用户没有注册环信
			CommUtil.huanxin_reg(user.getId().toString(), user.getPassword(), user.getUserName());
			user.setIs_huanxin(1);
			this.userService.update(user);
		}
		String[] users={user.getId().toString()};
		JSONObject messages=new JSONObject();
		messages.put("type", "txt");
		messages.put("msg", msg);
		String sender="150381";
		CommUtil.send_message_to_user(users, messages, sender);
	}*/
}
