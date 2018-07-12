package com.shopping.api.timer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.shopping.api.action.EvaluteFunction;
import com.shopping.api.domain.evaluate.VVPResourceEntity;
import com.shopping.api.service.evaluate.IEvaluateFunctionService;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.service.IUserService;
/**
 * @author:akangah
 * @description:删除在app上评价未完成,但是已经上传的一些文件,这里定时去清理
 */
@Component("timedDeleteUsedFile")
public class TimedDeleteUsedFile {
	@Autowired
	@Qualifier("vVPResource")
	private IEvaluateFunctionService<VVPResourceEntity> vVPResourceService;
	@Resource(name="evaluteFunction")
	private EvaluteFunction evaFun;
	@Autowired
	private IUserService userService;
	public void execute(){
		Integer count=0;//记录成功操作的次数
		boolean ret=false;//记录每次操作的结果值
		//这里因为VVPResourceEntity配置了级联，所以在删除的时候，只需要删除主图即可，封面图会随着级联而删除
		String hql="select obj from VVPResourceEntity as obj where obj.fileIsDelete='yes' " +
				   "and obj.surfacePlot is not null and obj.appraiseMessage is null";
		List<VVPResourceEntity> vvPList=this.vVPResourceService.query(hql, null, -1, -1);
		Integer vvpListSize=vvPList.size();
		List<Long> noDeleteIdCollList=new ArrayList<Long>();
		if(vvpListSize>0){
			for(VVPResourceEntity vvP:vvPList){
				if(vvP.getResType()=="voice"){
					ret=evaFun.deleteFileFromServer(vvP, 0);
					if(ret){
						count++;
					}else{
						noDeleteIdCollList.add(vvP.getId());
					}
				}else{
					ret=evaFun.deleteFileFromServer(vvP, 1);
					if(ret){
						count++;
					}else{
						noDeleteIdCollList.add(vvP.getId());
					}
				}
			}
			if(vvpListSize!=count){
				CommUtil.send_messageToSpecifiedUser(this.userService.getObjById(137054L),
						CommUtil.formatLongDate(new Date()).toString()+
						"系统删除文件部分成功"+"没有删除的id为"+JSON.toJSONString(noDeleteIdCollList),this.userService);
			}
		}
	}
}
