package com.shopping.api.timer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.shopping.api.domain.userFunction.RedPacket;
import com.shopping.api.service.IRedPacketService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

/**
 * @author:gaohao
 * @description:定时检测红包过期状态
 */
@Component("appRedPacketInspect")
public class AppRedPacketInspect {
//	@Autowired
//	private ICommonService commonService;
//	@Autowired
//	private IUserService userService;
//	@Autowired
//	private IRedPacketService redPacketService;
//	@Autowired
//	private IPredepositLogService predepositLogService;
	public void execute(){
		System.out.println("红包检测开始..");
		ApiUtils.asynchronousUrl("http://www.d1sc.com/inspectRedPackgetStatus.htm", "GET");
//		Date newDate = new Date();
//		int current_page=0;
//		int pageSize=50;
//		String count="select count(obj) from RedPacket as obj where obj.orderStatus = 20 and obj.overdueState = false and obj.overdueTime <=:overdueTime";
//		Map<String, Date> paramMap=new HashMap<String, Date>();
//		paramMap.put("overdueTime", newDate);
//		List<?> counts = commonService.query(count, paramMap, -1, -1);
//		int cycleNum=0;
//		if (counts.size()>0) {
//			int sum = CommUtil.null2Int(counts.get(0));
//			cycleNum=sum%pageSize==0?sum/pageSize:sum/pageSize+1;
//		}
//		for (int i = 0; i < cycleNum; i++) {
//			String hql="select obj from RedPacket as obj where obj.orderStatus = 20 and obj.overdueState = false and obj.overdueTime <= :overdueTime";		
//			List<RedPacket> redPackets = redPacketService.query(hql, paramMap, pageSize*current_page, pageSize);
//			for (RedPacket redPacket : redPackets) {
//				if (redPacket!=null&&redPacket.isOverdueState()==false) {
//					redPacket.setOverdueState(true);
//					boolean is = redPacketService.update(redPacket);
//					if (is&&redPacket.getOrderStatus()==20) {
//						double amount=CommUtil.null2Int(redPacket.getSurplusNum());
//						double money=CommUtil.null2Double(redPacket.getSingleMoney());
//						BigDecimal a1 = new BigDecimal(Double.toString((amount)));  
//				        BigDecimal b1 = new BigDecimal(Double.toString(money));  
//				        double surplusSum = a1.multiply(b1).doubleValue();
//				        surplusSum=CommUtil.formatDouble(surplusSum, 2);
//						User user = redPacket.getProvideUser();
//						boolean update = ApiUtils.distributeWages(user, surplusSum, surplusSum, redPacket.getRunningWaterNum()+"分享红包退款", commonService, predepositLogService, "");
//						if (!update) {
//							redPacket.setOverdueState(false);
//							redPacketService.update(redPacket);
//						}						
//					}
//				}
//			}
//		}
	}
}
