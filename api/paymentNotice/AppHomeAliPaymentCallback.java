package com.shopping.api.paymentNotice;

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

import com.shopping.api.domain.AppHomePageEntity;
import com.shopping.api.domain.appHomePage.AppHomePageCommonPosition;
import com.shopping.api.domain.appHomePage.AppHomePageTemporaryData;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.PredepositLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AppHomeAliPaymentCallback {
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ICommonService commonService;
	@RequestMapping({ "/app_HomePageAliPay_Callback.htm"})
	public void app_HomePageAliPay_Callback(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端首页空位付费购买notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if(trade_status.equals("WAIT_SELLER_SEND_GOODS")
				||trade_status.equals("TRADE_FINISHED")
				||trade_status.equals("TRADE_SUCCESS")){
			String hql="select obj from AppHomePageTemporaryData as obj where obj.orderNum="+order_no;
			List<?> appHomeList=this.commonService.query(hql, null, -1, -1);
			if(appHomeList.size()>0){
				AppHomePageTemporaryData appHome=(AppHomePageTemporaryData) appHomeList.get(0);
				if(appHome!=null){
					if(appHome.getOrderStatus()==10){
						appHome.setOrderStatus(20);
						this.commonService.update(appHome);
						String judgeHql="";
						Goods goods=this.goodsService.getObjById(appHome.getGoodsId());
						if("banner".equals(appHome.getVacantPositionType())){
							judgeHql="select obj from AppHomePageEntity as obj where obj.id="+appHome.getVacantPositionId();
							List<?> appHomeBannerList=this.commonService.query(judgeHql, null, -1, -1);
							if(appHomeBannerList.size()>0){
								AppHomePageEntity appHomeBanner=(AppHomePageEntity) appHomeBannerList.get(0);
								appHomeBanner.setFlush_time(appHome.getFlush_time());
								appHomeBanner.setGoods(goods);
								appHomeBanner.setIs_can_buy(false);
								appHomeBanner.setPurchase_timeDuan(appHome.getPurchase_timeDuan());
								appHomeBanner.setStart_time(appHome.getStart_time());
								this.commonService.update(appHomeBanner);
							}
						}else if("common".equals(appHome.getVacantPositionType())){
							judgeHql="select obj from AppHomePageCommonPosition as obj where obj.id="+appHome.getVacantPositionId();
							List<?> appHomeCommonList=this.commonService.query(judgeHql, null, -1, -1);
							if(appHomeCommonList.size()>0){
								AppHomePageCommonPosition appHomeCommon=(AppHomePageCommonPosition) appHomeCommonList.get(0);
								appHomeCommon.setFlush_time(appHome.getFlush_time());
								appHomeCommon.setGoods(goods);
								appHomeCommon.setIs_can_buy(false);
								appHomeCommon.setPurchase_timeDuan(appHome.getPurchase_timeDuan());
								appHomeCommon.setStart_time(appHome.getStart_time());
								this.commonService.update(appHomeCommon);
							}
						}
						User countUser=this.userService.getObjById(1L);
						double toatal=appHome.getTotal();
						countUser.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(
								countUser.getAvailableBalance().doubleValue(), toatal)));
						boolean ret=this.userService.update(countUser);
						if(ret){
							PredepositLog countUser_log = new PredepositLog();
							countUser_log.setAddTime(new Date());
							countUser_log.setPd_log_user(countUser);
							countUser_log.setPd_op_type("增加");
							countUser_log.setPd_log_amount(BigDecimal.valueOf(toatal));
							countUser_log.setPd_log_info("首页空位收入");
							countUser_log.setPd_type("可用预存款");
							countUser_log.setCurrent_price(countUser.getAvailableBalance().doubleValue());
							this.predepositLogService.save(countUser_log);
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
}
