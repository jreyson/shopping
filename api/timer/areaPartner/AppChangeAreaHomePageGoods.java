package com.shopping.api.timer.areaPartner;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;

/**
 * @author:gaohao
 * @description:检查区域首页默认商品是否已经下架
 */
@Component("appChangeAreaHomePageGoods")
public class AppChangeAreaHomePageGoods {
	public void execute(){
		System.out.println(CommUtil.formatLongDate(new Date())+"检查区域首页付费位默认商品是否下架");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/appChangeAreaHomePageGoods.htm", "POST");
	}
}
