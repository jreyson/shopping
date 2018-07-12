package com.shopping.api.timer;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shopping.api.domain.userBill.UserMonthlyBill;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IUserService;
/**
 * @author:gaohao
 * @description:在app上每月初统计上月的用户收入
 */
@Component("appUserMonthlyIncomeStatisti")
public class AppUserMonthlyIncomeStatisti {
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	public void execute(){
		String beginTime = CommUtil.getLastMonthFirstDay();
		String endTime = CommUtil.getLastMonthFinalDay();
		int currentPage=0;
		int pageSize=50;
		String count_sql="select count(distinct obj.user_id) from shopping_orderform as obj where obj.addTime >='"+beginTime+"' and obj.addTime <='"+endTime+"' and obj.order_status in (20,30,40,50,60)";
		List<?> count = commonService.executeNativeNamedQuery(count_sql);
		String user_sql="";
		int num=0;
		if (count.size()>0) {
			num=CommUtil.null2Int(count.get(0));
			num=num%50==0?num/50:num/50+1;
		}
		for (int i = 0; i < num; i++) {
			user_sql="select distinct obj.user_id from shopping_orderform as obj where obj.addTime >='"+beginTime+"' and obj.addTime <='"+endTime+"' and obj.order_status in (20,30,40,50,60) limit "+currentPage*pageSize+","+pageSize;
			List<?> users = commonService.executeNativeNamedQuery(user_sql);
			currentPage++;
			for (Object obj : users) {
				Long userId = CommUtil.null2Long(obj);
				if (userId!=-1) {
					User user = userService.getObjById(userId);
					if (user!=null) {
						Double chubeiMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "储备金");
						Double daogouMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "导购金");
						Double danbaoMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "担保金");
						Double zhaoshangMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "招商金");
						Double xianjiMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "衔级金");
						Double fenhongMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "分红股");
						Double zengguMoney = this.getUserIncomeMoneys(beginTime, endTime, user, "赠股金");
						Double moneySum=chubeiMoney+daogouMoney+zhaoshangMoney+xianjiMoney+danbaoMoney+fenhongMoney+zengguMoney;
						if (moneySum>0) {
							UserMonthlyBill bill=new UserMonthlyBill();
							bill.setUser(user);
							bill.setChubeiMoney(chubeiMoney);
							bill.setDaogouMoney(daogouMoney);
							bill.setDanbaoMoney(danbaoMoney);
							bill.setXianjiMoney(xianjiMoney);
							bill.setMoneySum(CommUtil.formatDouble(moneySum, 2));
							bill.setZhaoshangMoney(zhaoshangMoney);
							bill.setAddTime(new Date());
							bill.setDeleteStatus(false);
							bill.setZengguMoney(zengguMoney);
							bill.setFenhongMoney(fenhongMoney);
							commonService.save(bill);
						}
					}
				}
			}
		}
		System.out.println(beginTime+"收入统计完毕。");
	}
	private Double getUserIncomeMoneys(String begin,String end,User user,String type){
		String where="";
		if ("分红股".equals(type)) {
			where=" and obj.pd_log_info not LIKE '%退款%'";
		}
		String sql="SELECT ROUND(SUM(obj.pd_log_amount),2) FROM shopping_predeposit_log AS obj WHERE obj.addTime >= '" + begin + "' AND obj.addTime <= '" + end + "' AND obj.pd_log_user_id=" + user.getId() + " AND obj.pd_log_info LIKE '%" + type + "%'" + where;
		List<?> money = commonService.executeNativeNamedQuery(sql);
		if (money.size()>0) {
			Double userMoney=CommUtil.formatDouble(CommUtil.null2Double(money.get(0)), 2);
			return userMoney;
		}
		return 0d;
	}
}
