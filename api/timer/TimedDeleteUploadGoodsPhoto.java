package com.shopping.api.timer;

import org.springframework.stereotype.Component;

import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
/**
 * @author:akangah
 * @description:删除在app上传商品未完成,但是已经上传的一些文件
 */
@Component("timedDeleteUploadGoodsPhoto")
public class TimedDeleteUploadGoodsPhoto {
	public void execute(){
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/timedDeleteUploadGoodsPhoto.htm", "GET");
		ApiUtils.asynchronousUrl(SystemResPath.hostAddr + "/timedCleanMcUnusedRes.htm", "GET");
	}
}
