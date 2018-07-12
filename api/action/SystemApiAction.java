package com.shopping.api.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.shopping.api.domain.CenterListApi;
import com.shopping.api.tools.ApiUtils;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;

@Controller
public class SystemApiAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	IUserService userService;

	@Autowired
	ICommonService commonService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IUserConfigService userConfigService;

	@RequestMapping({ "/center_list.htm" })
	public ModelAndView center_list(HttpServletRequest request,
			HttpServletResponse response, String user_id) throws IOException {
		ModelAndView mv = new JModelAndView("center_list.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		mv.addObject("user", user);
		return mv;
	}
	@RequestMapping({ "/app_down.htm" })
	public ModelAndView app_down(HttpServletRequest request,
			HttpServletResponse response, String user_id) throws IOException {
		ModelAndView mv = new JModelAndView("app_down.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);

		return mv;
	}
}
