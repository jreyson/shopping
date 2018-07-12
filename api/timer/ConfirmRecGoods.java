package com.shopping.api.timer;
import org.springframework.stereotype.Component;
import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
@Component("autoConfirmRecGoods")
public class ConfirmRecGoods {
	public void execute(){
		System.out.println("开始启动确认收货......");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/confirmRecGoodsAndAllocateMoney.htm", "GET");
	}
}
