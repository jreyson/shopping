package com.shopping.api.paymentNotice.areaPaymentCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.domain.regionPartner.AreaHomePageConfig;
import com.shopping.api.domain.regionPartner.AreaPartnerEntity;
import com.shopping.api.domain.regionPartner.AreaPartnerPayRecord;
import com.shopping.api.domain.regionPartner.AreaSiteRankConfig;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;
@Controller
public class AppAlipayAreaCallBack {
	@Autowired
	private ICommonService commonService;
	@Autowired
	@Qualifier("areaPartnerPayRecordServiceImpl")
	private IPartnerFunctionService<AreaPartnerPayRecord> areaPartnerPayRecordService;
	@Autowired
	@Qualifier("areaPartnerEntityServiceImpl")
	private IPartnerFunctionService<AreaPartnerEntity> areaPartnerEntityService;
	@Autowired
	@Qualifier("areaHomePageConfigServiceImpl")
	private IPartnerFunctionService<AreaHomePageConfig> areaHomePageConfigService;
	@Autowired
	@Qualifier("areaCommonpositionServiceImpl")
	private IPartnerFunctionService<AreaCommonposition> areaCommonpositionService;
	@Autowired
	@Qualifier("areaBannerpositionServiceImpl")
	private IPartnerFunctionService<AreaBannerposition> areaBannerpositionService;
	@Autowired
	@Qualifier("areaSiteRankConfigServiceImpl")
	private IPartnerFunctionService<AreaSiteRankConfig> areaSiteRankConfigService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IUserService userService;
	@RequestMapping({ "/appAlipayAreaPartnerCallBack.htm"})	
	public void app_alipayRedPacketCallBack(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端购买区域合伙人notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if("WAIT_SELLER_SEND_GOODS".equals(trade_status)
				||"TRADE_FINISHED".equals(trade_status)
				||"TRADE_SUCCESS".equals(trade_status)){
			String hql="select obj from AreaPartnerPayRecord as obj where obj.orderNum = " + order_no;
			List<AreaPartnerPayRecord> apprs = this.areaPartnerPayRecordService.query(hql, null, -1, -1);
			if (apprs.size()>0) {
				AreaPartnerPayRecord appr = apprs.get(0);
				if (appr.getPayStatus()==10) {
					appr.setPayStatus(20);
					appr.setRewardStatus(false);
					appr.setPayType("alipay");
					appr.setPayTime(new Date());
					boolean save = this.areaPartnerPayRecordService.save(appr);
					if (save) {
						AreaPartnerUtils.distributionOrderAmount(appr, commonService, predepositLogService, userService);
						AreaPartnerUtils.saveAreaAppHome(appr.getUser(), appr.getAreaSiteRankConfig(), appr.getAreaGradeOfUser(), appr.getBuMen(),this.commonService, this.areaPartnerEntityService, 
								this.areaHomePageConfigService, this.areaCommonpositionService, this.areaBannerpositionService,this.areaSiteRankConfigService);
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
