package com.shopping.api.paymentNotice.areaapphomepaycall;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.api.domain.regionPartner.AreaAppHomePayTemporary;
import com.shopping.api.domain.regionPartner.AreaBannerposition;
import com.shopping.api.domain.regionPartner.AreaCommonposition;
import com.shopping.api.service.partner.IPartnerFunctionService;
import com.shopping.api.tools.AllocateWagesUtils;
import com.shopping.api.tools.AreaPartnerUtils;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IPredepositLogService;
import com.shopping.foundation.service.IUserService;

@Controller
public class AreaAppHomeAliPayCall {
	@Autowired
	@Qualifier("areaCommonpositionServiceImpl")
	private IPartnerFunctionService<AreaCommonposition> areaCommonpositionService;
	@Autowired
	@Qualifier("areaBannerpositionServiceImpl")
	private IPartnerFunctionService<AreaBannerposition> areaBannerpositionService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	@Qualifier("areaAppHomePayTemporaryServiceImpl")
	private IPartnerFunctionService<AreaAppHomePayTemporary> areaAppHomePayTemporaryService;
	@RequestMapping({ "/appAliPayAreaAppHomeCallBack.htm"})
	public void app_HomePageAliPay_Callback(HttpServletRequest request,
			HttpServletResponse response){
		System.out.println("----------通知app端区域站点首页空位付费购买notify--------");
		String order_no = request.getParameter("out_trade_no");
		String trade_status = request.getParameter("trade_status");
		if(trade_status.equals("WAIT_SELLER_SEND_GOODS")
				||trade_status.equals("TRADE_FINISHED")
				||trade_status.equals("TRADE_SUCCESS")){
			String hql="select obj from AreaAppHomePayTemporary as obj where obj.orderNum = " + order_no;
			List<AreaAppHomePayTemporary> aahpts = this.areaAppHomePayTemporaryService.query(hql, null, -1, -1);
			if(aahpts.size()>0){
				AreaAppHomePayTemporary aahpt = aahpts.get(0);
				if(aahpt!=null){
					if(aahpt.getOrderStatus()==10){
						boolean appPaymentAreaPlace = AreaPartnerUtils.appPaymentAreaPlace("AliPay",aahpt,areaCommonpositionService,areaBannerpositionService,userService,commonService,areaAppHomePayTemporaryService,predepositLogService);
						if (appPaymentAreaPlace) {
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
