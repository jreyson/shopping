package com.shopping.api.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.shopping.api.domain.headlines.ArticleContextEntity;
import com.shopping.api.service.headlines.IHeadlinesFunctionService;
import com.shopping.api.tools.ApiUtils;

@Controller
public class HeadlinesAction {
	@Autowired
	@Qualifier("newsTestServiceImpl")
	private IHeadlinesFunctionService<ArticleContextEntity> newsTestService;
	/***
	 * @author:gaohao
	 * @return:void
	 * @param:**
	 * @description:测试
	 * @function:**
	 * @exception:*******
	 * @method_detail:***
	 * @variable:*******
	 ***/
	@RequestMapping(value = "/newsTest.htm", method = RequestMethod.POST)
	public void newsTest(HttpServletRequest request,
			HttpServletResponse response, String userId, String password) {
		ApiUtils.json(response, "", "success", 0);
		return;
	}
}
