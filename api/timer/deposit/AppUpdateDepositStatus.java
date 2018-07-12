package com.shopping.api.timer.deposit;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;

/**
 * @author:gaohao
 * @description:检测积分理财过期状态
 */
@Component
public class AppUpdateDepositStatus {
	public void execute(){
		System.out.println(CommUtil.formatLongDate(new Date())+"检测理财过期状态");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appUpdateDepositStatus.htm", "POST");
	}
}
