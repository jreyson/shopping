package com.shopping.api.timer.areaPartner;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;

/**
 * @author:gaohao
 * @description:检查区域首页付费位过期状态
 */
@Component("appInspectAreaDataStatus")
public class AppInspectAreaDataStatus {
	public void execute(){
		System.out.println(CommUtil.formatLongDate(new Date())+"检查区域首页付费位过期状态");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/AppInspectAreaDataStatus.htm", "POST");
	}
}
