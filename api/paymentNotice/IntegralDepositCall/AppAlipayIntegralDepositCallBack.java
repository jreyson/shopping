package com.shopping.api.paymentNotice.IntegralDepositCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.integralDeposit.IntegralDepositEntity;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.api.service.IIntegralDepositService;
import com.shopping.api.service.IIntegralRechargeService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
@Controller
public class AppAlipayIntegralDepositCallBack {
	@Autowired
	private IUserService userService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IIntegralDepositService integralDepositService;
	@Autowired
	private ICommonService commonService;
	@RequestMapping({ "/app_alipayIntegralDepositCallBack.htm"})
	public void app_alipayIntegralRecharge_callBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端积分理财notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if("WAIT_SELLER_SEND_GOODS".equals(trade_status)
				||"TRADE_FINISHED".equals(trade_status)
				||"TRADE_SUCCESS".equals(trade_status)){
			String hql="select obj from IntegralDepositEntity as obj where obj.depositOrderNum="+order_no;
			List<IntegralDepositEntity> list = this.integralDepositService.query(hql, null, -1, -1);
			IntegralDepositEntity integralDepositEntity = list.get(0);
			if(integralDepositEntity!=null){
				if(integralDepositEntity.getOrderStatus().intValue()==10){
					integralDepositEntity.setOrderStatus(20);
					boolean ret=this.integralDepositService.update(integralDepositEntity);
					if(ret){
						User user=integralDepositEntity.getUser();
						if(user!=null){
							ApiUtils.updateUserAndDeposit(integralDepositEntity, userService, predepositLogService, integralDepositService, 1,commonService);
							String msg=user.getUserName()+"战友，你好，你已成功投资理财项目，请在积分理财里查看";
							CommUtil.send_messageToSpecifiedUser(user, msg,userService);
						}
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
