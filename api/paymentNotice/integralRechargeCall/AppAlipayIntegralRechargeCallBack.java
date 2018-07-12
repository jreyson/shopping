package com.shopping.api.paymentNotice.integralRechargeCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.integralRecharge.IntegralRechargeEntity;
import com.shopping.api.service.IIntegralRechargeService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AppAlipayIntegralRechargeCallBack {
	@Autowired
	private IUserService userService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IIntegralRechargeService integralRechargeService;
	@Autowired
	private ICommonService commonService;
	@RequestMapping({ "/app_alipayIntegralRecharge_callBack.htm"})
	public void app_alipayIntegralRecharge_callBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端积分购买notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if("WAIT_SELLER_SEND_GOODS".equals(trade_status)
				||"TRADE_FINISHED".equals(trade_status)
				||"TRADE_SUCCESS".equals(trade_status)){
			String hql="select obj from IntegralRechargeEntity as obj where obj.runningWaterNum="+order_no;
			List<IntegralRechargeEntity> integralRechargeList=this.integralRechargeService.query(hql, null, -1, -1);
			IntegralRechargeEntity integralRechargeEntity=integralRechargeList.get(0);
			if(integralRechargeEntity!=null){
				if(integralRechargeEntity.getOrderStatus().intValue()==10){
					integralRechargeEntity.setOrderStatus(20);
					boolean ret=this.integralRechargeService.update(integralRechargeEntity);
					if(ret){
						User user=integralRechargeEntity.getUser();
						if(user!=null){
							ApiUtils.updateUserAvailableBalance(integralRechargeEntity, userService, predepositLogService,commonService);
							String msg=user.getUserName()+"战友，你好，你已成功充值积分，请在用户个人资料里面查看余额";
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
