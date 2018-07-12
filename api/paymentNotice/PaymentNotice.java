package com.shopping.api.paymentNotice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IUserService;
@Controller
public class PaymentNotice {
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IOrderFormService orderFormService;
	@RequestMapping({ "/app_alipay_notify.htm" })
	public void app_alipay_notify(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知notify--------");
		String trade_no = request.getParameter("trade_no");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		String[] tradeNumArray=order_no.split("orderId");
		if(trade_status.equals("WAIT_SELLER_SEND_GOODS")||trade_status.equals("TRADE_FINISHED")
			||trade_status.equals("TRADE_SUCCESS")){
			for(int i=0;i<tradeNumArray.length;i++){
				String hql="select obj from OrderForm obj where obj.order_id="+tradeNumArray[i];
				List<OrderForm> orderList=this.orderFormService.query(hql, null, -1, -1);
				OrderForm order=orderList.get(0);
				if(order.getOrder_status()==10){
					order.setOrder_status(20);
					order.setOut_order_id(trade_no);
					order.setPayTimes(new Date());
					order.setPayTime(new Date());
					boolean ret=this.orderFormService.update(order);
					if(ret){
						ApiUtils.statSaleNum(order.getId(), goodsCartService, goodsService);
						String goodsMsg="";
						for(GoodsCart goodsCart:order.getGcs()){
							goodsMsg=goodsMsg+"<===>"+goodsCart.getGoods().getGoods_name();
						}
						//<===>D1SC扇子 天气开始热值得拥有它 全国包邮
						String msg=goodsMsg.substring(5, goodsMsg.length());
						this.send_message(order.getStore().getUser(), order.getStore().getUser().getUserName()+"战友,您好,您店铺的"+msg+"等商品,已被"+order.getUser().getUserName()+"战友购买,请及时发货");
						this.send_message(order.getUser(), order.getUser().getUserName()+"战友,您好,您已成功购买"+order.getStore().getStore_name()+"(店铺)的"+msg+"商品,请等待发货");
						OrderFormLog ofl = new OrderFormLog();
						ofl.setAddTime(new Date());
						ofl.setLog_info("支付宝在线支付");
						ofl.setLog_user(order.getUser());
						ofl.setOf(order);
						this.orderFormLogService.save(ofl);
						ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appRemindSellerDelivery.htm?orderIds=" + tradeNumArray[i], "GET");
					}
				}
			}
		}
		PrintWriter writer=null;
		try {
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
		String sender="150383";
		CommUtil.send_message_to_user(users, messages, sender);
	}
}
