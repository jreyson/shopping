package com.shopping.api.paymentNotice.countPaymentCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.countBuy.CountOrderDomain;
import com.shopping.api.service.ICountOrderService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

@Controller
public class CountAlibabaPaymentCallBack {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICountOrderService countOrderService;
	@RequestMapping({"/app_countAliPayment_CallBack.htm"})
	public void app_countAliPayment_CallBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端首页空位付费购买notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if(trade_status.equals("WAIT_SELLER_SEND_GOODS")
				||trade_status.equals("TRADE_FINISHED")
				||trade_status.equals("TRADE_SUCCESS")){
			CountOrderDomain countOrder=this.countOrderService.getObjById(CommUtil.null2Long(order_no));
			if(countOrder!=null){
				if(countOrder.getOrder_status()==10){
					countOrder.setOrder_status(20);
					countOrder.setPayTime(new Date());
					boolean ret=this.countOrderService.update(countOrder);
					if(ret){
						User countUser=this.userService.getObjById(1L);
						double toatal=countOrder.getTotal_price();
						countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
								countUser.getAvailableBalance().doubleValue(), toatal)));
						boolean up_ret=this.userService.update(countUser);
						if(up_ret){
							PredepositLog countUser_log = new PredepositLog();
							countUser_log.setAddTime(new Date());
							countUser_log.setPd_log_user(countUser);
							countUser_log.setPd_op_type("增加");
							countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
							countUser_log.setPd_log_info("订单"+countOrder.getId()+"支付宝购买拓客点数增加预存款");
							countUser_log.setPd_type("可用预存款");
							countUser_log.setOrder_id(ApiUtils.integralOrderNum(1L));
							countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
							boolean save_ret=this.predepositLogService.save(countUser_log);
							if(save_ret){
								String sendMsg="您已成功购买,等待管理员充值";
								ApiUtils.json(response, "", sendMsg, 0);
								this.send_message(countOrder.getNeededUser(), sendMsg);
								User adminUser=this.userService.getObjById(new Long(20717));
								if(adminUser!=null){
									this.send_message(adminUser, countOrder.getOrder_remark()+"战友已经购买点数,请及时充值");
									return;
								}
							}
						}
					}
				}
			}
		}
		PrintWriter writer=null;
		try{
			writer=response.getWriter();
			writer.write("success");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			writer.flush();
			writer.close();
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
