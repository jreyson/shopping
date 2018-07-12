package com.shopping.api.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.api.domain.countBuy.CountPriceDomain;
import com.shopping.api.service.ICountOrderService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;

@Controller
public class CountsAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ICountOrderService countOrderService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:获取app扩客点数的数据
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_acquire_coutntsList.htm", method = RequestMethod.POST)
	public void app_acquire_coutntsList(HttpServletRequest request,
			HttpServletResponse response){
		String hql="select obj from CountPriceDomain as obj";
		List<?> countPriceList=this.commonService.query(hql, null, -1, -1);
		if(countPriceList.size()>0){
			ApiUtils.json(response, countPriceList, "获取列表成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:积分支付购买扩客点数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_balancePayment_counts.htm", method = RequestMethod.POST)
	public void app_balancePayment_counts(HttpServletRequest request,
			HttpServletResponse response,String userId,
			String countPriceId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		String queryHql="select obj from CountPriceDomain as obj where obj.id="+countPriceId;
		List<?> countPriceList=this.commonService.query(queryHql, null, -1, -1);
		if(user==null||countPriceList.size()==0){
			ApiUtils.json(response, "", "参数错误,该用户不存在或者购买项不存在", 0);
			return;
		}
		double total=((CountPriceDomain) countPriceList.get(0)).getCurrent_price();
		boolean is_can_buy=false;
		String msg="";
		double userBalance=user.getAvailableBalance().doubleValue();
		if(user.getFreezeBlance().intValue()==1){
			if(userBalance-total-1000>=0){
				is_can_buy=true;
			}else{
				msg="您的余额有一千块诚信保证金被锁定,要完成此次支付,余额大于1000";
			}
		}else{
			if(userBalance-total>=0){
				is_can_buy=true;
			}else{
				msg="您的余额不足,谢谢惠顾";
			}
		}
		if(is_can_buy){ 
			user.setAvailableBalance(BigDecimal.valueOf(CommUtil.subtract(
					userBalance, total)));
			boolean up_ret=this.userService.update(user);
			User countUser=this.userService.getObjById(1L);
			if(up_ret){
				PredepositLog buyBanner_log = new PredepositLog();
				buyBanner_log.setAddTime(new Date());
				buyBanner_log.setPd_log_user(user);
				buyBanner_log.setPd_op_type("减少");
				buyBanner_log.setPd_log_amount(BigDecimal.valueOf(-total));
				buyBanner_log.setPd_log_info("用于购买拓客点数");
				buyBanner_log.setPd_type("可用预存款");
				buyBanner_log.setCurrent_price(user.getAvailableBalance().doubleValue());
				boolean rr=this.predepositLogService.save(buyBanner_log);
				if(rr){
					Long order_num = ApiUtils.integralOrderNum(user.getId());
					Long orderNum=CommUtil.null2Long(order_num);
					CountOrderDomain countOrder=new CountOrderDomain();//生成点数订单
					countOrder.setAddTime(new Date());
					countOrder.setDeleteStatus(false);
					countOrder.setOrder_status(20);
					countOrder.setPay_way("积分支付");
					countOrder.setPayTime(new Date());
					countOrder.setTotal_price(total);
					countOrder.setNeededUser(user);
					countOrder.setCountPrice((CountPriceDomain) countPriceList.get(0));
					countOrder.setOrder_remark(user.getUserName());
					countOrder.setOrder_num(orderNum);
					boolean sa_ret=this.countOrderService.save(countOrder);
					if(sa_ret){
						countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
								countUser.getAvailableBalance().doubleValue(), total)));
						this.userService.update(countUser);
						PredepositLog countUser_log = new PredepositLog();
						countUser_log.setAddTime(new Date());
						countUser_log.setPd_log_user(countUser);
						countUser_log.setPd_op_type("增加");
						countUser_log.setPd_log_amount(BigDecimal.valueOf(total));
						countUser_log.setPd_log_info("订单"+countOrder.getId()+"积分购买拓客点数增加预存款");
						countUser_log.setPd_type("可用预存款");
						countUser_log.setOrder_id(ApiUtils.integralOrderNum(user.getId()));
						countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
						boolean save_ret=this.predepositLogService.save(countUser_log);
						if(save_ret){
							String sendMsg="您已成功购买点数,等待管理员充值";
							ApiUtils.json(response, "", sendMsg, 0);
							this.send_message(user, sendMsg);
							User adminUser=this.userService.getObjById(new Long(20717));
							if(adminUser!=null){
								this.send_message(adminUser, countOrder.getOrder_remark()+"战友已经购买点数,请及时充值");
								return;
							}
						}
					}
				}
			}
		}else{
			ApiUtils.json(response, "", msg, 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:支付宝支付购买拓客点数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_aliPayment_counts.htm", method = RequestMethod.POST)
	public void app_aliPayment_counts(HttpServletRequest request,
			HttpServletResponse response,Long userId,
			String countPriceId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		String queryHql="select obj from CountPriceDomain as obj where obj.id="+countPriceId;
		List<?> countPriceList=this.commonService.query(queryHql, null, -1, -1);
		double total=0.0D;
		if(user==null||countPriceList.size()==0){
			ApiUtils.json(response, "", "参数错误,该用户不存在或者购买项不存在", 1);
			return;
		}else{
			Long order_num = ApiUtils.integralOrderNum(user.getId());
			Long orderNum=CommUtil.null2Long(order_num);
			total=((CountPriceDomain) countPriceList.get(0)).getCurrent_price();
			CountOrderDomain countOrder=new CountOrderDomain();//生成点数订单
			countOrder.setAddTime(new Date());
			countOrder.setDeleteStatus(false);
			countOrder.setOrder_status(10);
			countOrder.setPay_way("支付宝支付");
			countOrder.setPayTime(new Date());
			countOrder.setTotal_price(total);
			countOrder.setNeededUser(user);
			countOrder.setOrder_remark(user.getUserName());
			countOrder.setCountPrice((CountPriceDomain) countPriceList.get(0));
			countOrder.setOrder_num(orderNum);
			boolean sa_ret=this.countOrderService.save(countOrder);
			if(sa_ret){
				String inform_url=CommUtil.getURL(request)+"/app_countAliPayment_CallBack.htm";
				String str = ApiUtils.getAlipayStr(countOrder.getId()+"",
						inform_url,total+"");//total==》
				ApiUtils.json(response, str, "获取支付信息成功", 0);
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:微信支付购买拓客点数
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_weixinPayment_counts.htm", method = RequestMethod.POST)
	public void app_weixinPayment_counts(HttpServletRequest request,
			HttpServletResponse response,Long userId,
			String countPriceId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		User user=this.userService.getObjById(userId);
		String queryHql="select obj from CountPriceDomain as obj where obj.id="+countPriceId;
		List<?> countPriceList=this.commonService.query(queryHql, null, -1, -1);
		double total=0.0D;
		if(user==null||countPriceList.size()==0){
			ApiUtils.json(response, "", "参数错误,该用户不存在或者购买项不存在", 1);
			return;
		}else{
			Long order_num = ApiUtils.integralOrderNum(user.getId());
			Long orderNum=CommUtil.null2Long(order_num);
			total=((CountPriceDomain) countPriceList.get(0)).getCurrent_price();
			CountOrderDomain countOrder=new CountOrderDomain();//生成点数订单
			countOrder.setAddTime(new Date());
			countOrder.setDeleteStatus(false);
			countOrder.setOrder_status(10);
			countOrder.setPay_way("微信支付");
			countOrder.setPayTime(new Date());
			countOrder.setTotal_price(total);
			countOrder.setNeededUser(user);
			countOrder.setOrder_remark(user.getUserName());
			countOrder.setCountPrice((CountPriceDomain) countPriceList.get(0));
			countOrder.setOrder_num(orderNum);
			boolean sa_ret=this.countOrderService.save(countOrder);
			if(sa_ret){
				Map<String, String> out_put_params = null;//payTotal
				try {
					String notify_url=CommUtil.getURL(request)+"/app_countWeixinPayment_CallBack.htm";
					out_put_params = ApiUtils.get_weixin_sign_string(countOrder.getId()+"",
							notify_url,total+"");//total
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ApiUtils.json(response, out_put_params, "获取支付信息成功", 0);
				return;
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:后台管理员看到点数购买的相应订单
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/admin/admin_acquire_coutntsOrderList.htm")
	public ModelAndView admin_acquire_coutntsOrderList(HttpServletRequest request,
				HttpServletResponse response,String currentPage){
		//System.out.println(99);
		ModelAndView mv = new JModelAndView("admin/blue/admin_acquire_coutntsOrderList.html", 
				       this.configService.getSysConfig(),
				       this.userConfigService.getUserConfig(), 0, request, response);
		int current_page=Integer.valueOf(currentPage).intValue();
		int page_size=12;
		float total_record=0F;
		String sql="select count(*) from shopping_count_order where order_status=20";
		total_record=CommUtil.null2Float(this.commonService.executeNativeNamedQuery(sql).get(0));
		int total_pages=(int)(total_record%page_size==0?total_record/page_size:Math.ceil(total_record/page_size));
		String hql="select obj from CountOrderDomain as obj where obj.order_status=20";
		List<CountOrderDomain> coutntsOrderList=this.countOrderService.query(hql, null, (current_page-1)*page_size, page_size);
		mv.addObject("coutntsOrderList", coutntsOrderList);
		mv.addObject("gotoPageFormHTML",CommUtil.showPageFormHtml(current_page, total_pages));
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:给指定的用户发送账户和密码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_sendMessage_toAccout.htm", method = RequestMethod.POST)
	public void app_sendMessage_toAccout(HttpServletRequest request,
			HttpServletResponse response,String userId,String coutntsOrderId,
			String isSendPassword){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		CountOrderDomain countOrder=this.countOrderService.getObjById(CommUtil.null2Long(coutntsOrderId));
		if(countOrder!=null){
			countOrder.setOrder_status(30);
			boolean ret=this.countOrderService.update(countOrder);
			if(ret){
				User user=this.userService.getObjById(CommUtil.null2Long(userId));
				if(user!=null){
					String msg="";
					if("Yes".equals(CommUtil.null2String(isSendPassword))){
						msg="您好,您已成功购买拓客系统"+countOrder.getCountPrice().getCounts()+"点数，用户名是："+user.getUserName()+"密码是123456,现在去使用吧";
					}else{
						msg="您好,您已成功购买拓客系统"+countOrder.getCountPrice().getCounts()+"点数，用户名是："+user.getUserName()+"密码是123456,,现在去使用吧";
					}
					this.send_message(user, msg);
					ApiUtils.json(response, "", "操作成功", 0);
				}
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:给指定的用户发送账户和密码
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/admin_delete_coutntsOrderRecord.htm", method = RequestMethod.POST)
	public void admin_delete_coutntsOrderRecord(HttpServletRequest request,
			HttpServletResponse response,String coutntsOrderId){
		Long countOrder_id=CommUtil.null2Long(coutntsOrderId);
		CountOrderDomain countOrder=this.countOrderService.getObjById(countOrder_id);
		if(countOrder!=null){
			countOrder.setNeededUser(null);
			countOrder.setCountPrice(null);
			boolean up_ret=this.countOrderService.update(countOrder);
			if(up_ret){
				boolean ret=this.countOrderService.remove(countOrder_id);
				if(ret){
					ApiUtils.json(response, "", "删除成功", 0);
				}
			}
		}
	}
	private void send_message(User user,String msg){
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
	}
}
