package com.shopping.api.timer.manageposition;

import org.springframework.stereotype.Component;

import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;

@Component("appWeekManagePosition")
public class AppWeekManagePosition {
	public void execute(){
		System.out.println("每周一自动授职");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/autoWeekManagePosition.htm", "GET");
	}
}
