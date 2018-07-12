package com.shopping.api.paymentNotice.redPacketCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.userFunction.RedPacket;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.api.service.IRedPacketService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AppAlipayRedPacketCallBack {
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IRedPacketService redPacketService;
	@RequestMapping({ "/app_alipayRedPacketCallBack.htm"})	
	public void app_alipayRedPacketCallBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端发送红包notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if("WAIT_SELLER_SEND_GOODS".equals(trade_status)
				||"TRADE_FINISHED".equals(trade_status)
				||"TRADE_SUCCESS".equals(trade_status)){
			String hql="select obj from RedPacket AS obj where obj.runningWaterNum = " + order_no;
			List<RedPacket> query = redPacketService.query(hql, null, -1, -1);
			if (query.size()>0) {
				RedPacket redPacket=query.get(0);
				if (redPacket!=null) {
					if (redPacket.getOrderStatus()==10) {
						redPacket.setOrderStatus(20);
						redPacket.setRechargeWay("alipay");
						redPacket.setPayTime(new Date());
						boolean save = redPacketService.save(redPacket);
						if (save) {
							response.setContentType("text/plain");
							response.setHeader("Cache-Control", "no-cache");
							response.setCharacterEncoding("UTF-8");
							try {
								PrintWriter writer = response.getWriter();
								writer.print("success");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}else{
			response.setContentType("text/plain");
			response.setHeader("Cache-Control", "no-cache");
			response.setCharacterEncoding("UTF-8");
			try {
				PrintWriter writer = response.getWriter();
				writer.print("fail");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
