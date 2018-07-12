package com.shopping.api.action;


import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.api.tools.FormAssemblyUtils;
import com.shopping.api.tools.WriteFileUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Album;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.SysConfig;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAlbumService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsClassService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.manage.admin.tools.StoreTools;
/**
 * @author:akangah
 * @description:app端关于商品的增删改查
 * @classType:action类
 */
@Controller
public class SellerManageAction {
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private ICommonService commonService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:卖家在app端上传商品细节图完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appGoodsCarouselImgUpload.htm")
	public void  appGoodsCarouselImgUpload(HttpServletRequest request,
			HttpServletResponse response,String userId){
		Long begT=System.currentTimeMillis();
		MultipartHttpServletRequest multipartRequest=null;//声明reqest的多部分的文件,用于批量上传
		CommonsMultipartFile file=null;//声明要写入的file对象，指具体的对象
		Map<String,Object> fileInfo=null;
		List<Map<String,Object>> retList=null;//声明list,用于启动线程时,向新线程传递值
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		String paramsKey="";//传递上来的文件名
		String originalName="";//文件的原始名字
		String extend="";//文件的后缀名
		String photoName="";//图片的名字
		String photoPath="";//图片的路径
		Accessory photo=null;//需要新建的图片
		SysConfig sysConfig=null;//和图片路径配置相关
		Album album=null;
		boolean ret=false;
		HttpSession session=null;
		boolean flag=false;//标志文件内容是否为空
		Integer succNum=0;
		if(multipartResolver.isMultipart(request)){//这里判断request请求中是否有文件上传
			retList=new ArrayList<Map<String,Object>>();
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			album=this.getDefaultAlbum(album, user);
			sysConfig=this.configService.getSysConfig();
			multipartRequest= (MultipartHttpServletRequest) request;
			Iterator<?> fileNameIter=multipartRequest.getFileNames();
			Integer flagNum=0;
			session=request.getSession();
			String outStr="";
			String outReptName="";
			while(fileNameIter.hasNext()){//如果只有文件名，没有对应的文件内容的话，这里面是不进去的
				flag=true;
				paramsKey=(String) fileNameIter.next();
				file = (CommonsMultipartFile) multipartRequest.getFile(paramsKey);
				photoName=UUID.randomUUID().toString();
				originalName=file.getOriginalFilename();
				extend=originalName.substring(originalName.lastIndexOf(".") + 1);
				if(!this.fileIsAlreadyExist(session, originalName)){
					outReptName=originalName+","+outReptName;
					outStr=outReptName+"等文件不能重复上传，请删除后再上传";
					continue;
				}
				if(!this.isBeyondNum(session, originalName)){
					outStr="图片最多上传5张，请删除后在上传";
					continue;
				}
				if(file.getSize()>1048576){//最大支持1M上传 不管处于是那种情况,文件的大小都是不能超过1M
					outStr=originalName+"文件过大,最大支持单个图片1M上传";
					continue;
				}
				photoPath=this.storeTools.createUserFolderURL(sysConfig, user.getStore());
				photo=new Accessory(true, new Date(), 400, 400, photoName+"."+extend, extend, photoPath, user, "app端上传商品", file.getSize(), album, sysConfig);
				ret=this.accessoryService.save(photo);
				if(ret){
					fileInfo=new HashMap<String,Object>();
					fileInfo.put("photo", photo);
					fileInfo.put("file", file);
					retList.add(fileInfo);
					this.updateSession(session, originalName, photo.getId());
					flagNum++;
				}
			}
			if(flagNum>0){
				succNum=this.generateImage(retList, sysConfig);//开始进行图片IO处理
				ApiUtils.json(response, "", "上传"+succNum+"个文件成功", 0);
			}else{
				if(!flag){//如果只有文件名，没有对应的文件内容的话，是出现这种情况的
					ApiUtils.json(response, "", "文件内容不能为空", 1);
				}else{
					ApiUtils.json(response, "", outStr, 1);
				}
			}
		}
		Long endT=System.currentTimeMillis();
		System.out.println("app上传图片写入"+succNum+"张图片完成,总共耗时"+(endT-begT)+"毫秒");
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:卖家在app端上传商品详情图完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUploadGoodsDetailPhoto.htm")
	public void appUploadGoodsDetailPhoto(HttpServletRequest request,
			HttpServletResponse response,String userId){
		Long begT=System.currentTimeMillis();
		MultipartHttpServletRequest multipartRequest=null;//声明reqest的多部分的文件,用于批量上传
		CommonsMultipartFile file=null;//声明要写入的file对象，指具体的对象
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		String paramsKey="";//传递上来的文件名
		String originalName="";//文件的原始名字
		String extend="";//文件的后缀名
		String photoName="";//图片的名字
		String photoPath="";//图片的路径
		Accessory photo=null;//需要新建的图片
		SysConfig sysConfig=null;//和图片路径配置相关
		Album album=null;
		boolean ret=false;
		HttpSession session=null;
		Integer succNum=0;
		String filePathName="";
		String outStr="";
		String folderPath="";
		Map<String,Long> goodsDetailMap=null;
		if(multipartResolver.isMultipart(request)){//这里判断request请求中是否有文件上传
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			album=this.getDefaultAlbum(album, user);
			sysConfig=this.configService.getSysConfig();
			multipartRequest= (MultipartHttpServletRequest) request;
			Iterator<?> fileNameIter=multipartRequest.getFileNames();
			session=request.getSession();
			goodsDetailMap=(Map<String, Long>) session.getAttribute("goodsDetailMap");
			String outReptName="";
			while(fileNameIter.hasNext()){//如果只有文件名，没有对应的文件内容的话，这里面是不进去的
				paramsKey=(String) fileNameIter.next();
				file = (CommonsMultipartFile) multipartRequest.getFile(paramsKey);
				photoName=UUID.randomUUID().toString();
				originalName=file.getOriginalFilename();
				extend=originalName.substring(originalName.lastIndexOf(".") + 1);
				photoPath=this.storeTools.createUserFolderURL(sysConfig, user.getStore());
				if(goodsDetailMap!=null&&goodsDetailMap.containsKey(originalName)){
					outReptName=originalName+","+outReptName;
					outStr=outReptName+"等文件不能重复上传，请删除后再上传";
					continue;
				}
				if(file.getSize()>1048576){
					outStr=originalName+"此文件太大，请选择小于1M的图片上传";
					continue;
				}
				if(goodsDetailMap!=null&&goodsDetailMap.size()>10){
					outStr="商品详情图片最多支持10张图片上传";
					continue;
				}
				photo=new Accessory(true, new Date(), 400, 400, photoName+"."+extend, extend, photoPath, user, "app端上传商品", file.getSize(), album, sysConfig);
				ret=this.accessoryService.save(photo);
				if(ret){
					folderPath=SystemResPath.imgUploadUrl+"/"+photo.getPath();
					CommUtil.createFolder(folderPath);//创建文件夹
					filePathName=SystemResPath.imgUploadUrl +"/"+ photo.getPath() + File.separator + photo.getName();
					ret=WriteFileUtils.writeFile(filePathName, file, file.getSize());
					if(ret){//写入成功之后，更新session
						if(goodsDetailMap==null){
							goodsDetailMap=new HashMap<String, Long>();
						}
						goodsDetailMap.put(originalName, photo.getId());
						session.setAttribute("goodsDetailMap", goodsDetailMap);
						succNum++;
					}
				}
			}
		}
		if(succNum>0){
			ApiUtils.json(response, "","上传商品详情图"+succNum+"张成功", 0);
		}else{
			ApiUtils.json(response, "",outStr, 1);
		}
		Long endT=System.currentTimeMillis();
		System.out.println("app上传图片写入商品详情==>goods_details"+succNum+"张图片完成,总共耗时"+(endT-begT)+"毫秒");
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:app删除上传的图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appDeleteUploadImg.htm")
	public void appDeleteUploadImg(HttpServletRequest request,
			HttpServletResponse response,String photoNames,Short flagType){
		if(ApiUtils.is_null(photoNames,flagType+"")){
			ApiUtils.json(response, "","所需参数不能缺少", 1);
			return;
		}
		Map<String,Long> photoNameMap=null;
		if(flagType==0){//如果是0,表示删除细节图
			photoNameMap=(Map<String, Long>) request.getSession().getAttribute("appPhotoUploadMap");
		}else if(flagType==1){//如果是1的话,删除详情图
			photoNameMap=(Map<String, Long>) request.getSession().getAttribute("goodsDetailMap");
		}
		if(photoNameMap==null){
			ApiUtils.json(response, "","会话超时,已经全部删除", 2);
			return;
		}
		File file=null;
		boolean excuRet=false;
		Accessory photo=null;
		Long photoId=0L;
		String photoExt="";
		String pname="";//得到图片的原始名字
		List<String> pnameList=JSON.parseArray(photoNames, String.class);
		Iterator<String> pnameIter=pnameList.iterator();
		Integer sunccNum=0;
		while(pnameIter.hasNext()){
			pname=pnameIter.next();
			photoId=photoNameMap.get(pname);
			if(photoId!=null){
				photo=this.accessoryService.getObjById(photoId);
				photoExt= photo.getExt().indexOf(".") < 0 ? "."+ photo.getExt() : photo.getExt();//得到后缀名
				file=new File(SystemResPath.imgUploadUrl+"/"+photo.getPath()+"/"+photo.getName());
				if(file.exists()){
					excuRet=file.delete();//从磁盘删除文件
					if(excuRet){
						excuRet=this.accessoryService.delete(photoId);//从数据库删除记录
						if(excuRet){
							file=new File(SystemResPath.imgUploadUrl+"/"+photo.getPath()+"/"+photo.getName()+"_small"+photoExt);
							if(file.exists()){
								file.delete();
							}
							file=new File(SystemResPath.imgUploadUrl+"/"+photo.getPath()+"/"+photo.getName()+"_middle"+photoExt);
							if(file.exists()){
								file.delete();
							}
							photoNameMap.remove(pname);//因为这里引用类型传递，所以无需更新session,session里面的map和取出来的map共用,指向同一数据块
							sunccNum++;
						}
					}
				}
			}
		}
		ApiUtils.json(response, sunccNum,"成功删除"+sunccNum+"张图片", 0);//647963
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:卖家在app端发布商品完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appPublishGoodsFinish.htm")
	public void appPublishGoodsFinish(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsClassId,String goodsSpecStr){
		if(ApiUtils.is_null(userId,goodsClassId)){
			ApiUtils.json(response, "","userId和goodsClassId不能缺少", 1);
			return;
		}
		boolean ret=false;
		User  user=this.userService.getObjById(CommUtil.null2Long(userId));
		Goods goods=FormAssemblyUtils.assemblyForm(request, Goods.class,null);//创建goods,并且装配属性
		Double storePrice=CommUtil.null2Double(goods.getStore_price());
		Double settlementPrice=CommUtil.null2Double(goods.getSettlement_price());
		String goodsName=CommUtil.null2String(goods.getGoods_name());
		Iterator<Entry<String, Long>> photokvIter=null;//商品图片map的迭代器
		Entry<String, Long> entry=null;//声明具体的条目
		Long val=0L;
		Accessory photo=null;
		GoodsClass goodsClass=null;
		Map<String,Long> photoMap=(Map<String, Long>) request.getSession().getAttribute("appPhotoUploadMap");
		Map<String,Long> goodsDetailMap=(Map<String, Long>) request.getSession().getAttribute("goodsDetailMap");
		Integer flagMainPhoto=0;
		String goods_details="";
		if(user==null){
			ApiUtils.json(response, "","userId不合法", 1);
			return;
		}
		if(photoMap==null){
			ApiUtils.json(response, "","发布商品会话超时，请重新上传", 1);
			return;
		}
		if(photoMap.size()!=5){
			ApiUtils.json(response, "","商品必须带有5张轮播图片", 1);
			return;
		}
		if(goodsDetailMap==null){
			ApiUtils.json(response, "","发布商品会话超时,商品必须至少带有5张详情图片", 1);
			return;
		}
		if(goodsDetailMap.size()<5||goodsDetailMap.size()>10){
			ApiUtils.json(response, "","商品必须至少带有5张详情图片,最多为10张详情图", 1);
			return;
		}
		if(storePrice<=0||settlementPrice<=0){
			ApiUtils.json(response, "","店铺价和结算不能小于0", 1);
			return;
		}
		if(settlementPrice>storePrice*0.9){//如果结算价大于店铺价的90%
			ApiUtils.json(response, "","结算价不能大于供货价的90%", 1);
			return;
		}
		if("".equals(goodsName)||goodsName.length()>50){
			ApiUtils.json(response, "","商品名字不能为空并且长度不能超过50个字符", 1);
			return;
		}
		goodsClass=this.goodsClassService.getObjById(CommUtil.null2Long(goodsClassId));
		goods.setGc(goodsClass);
		goods.setGoods_current_price(goods.getStore_price());
		goods.setAddTime(new Date());
		goods.setGoods_store(user.getStore());
		goods.setCtj(1.00D);
		goods.setZhanlue_price(BigDecimal.valueOf(0));
		//设置推广价
		goods.setTuiguang_price(BigDecimal.valueOf(goods.getSettlement_price().doubleValue() + goods.getCtj()));
		photokvIter=photoMap.entrySet().iterator();
		while(photokvIter.hasNext()){//进行绑定图片
			entry=photokvIter.next();
			val=(Long) entry.getValue();
			photo=this.accessoryService.getObjById(val);
			photo.setDeleteStatus(false);
			this.accessoryService.update(photo);//将所有的图片记录删除状态改为false
			if(flagMainPhoto==0){//主图只写入一次,不绑定在商品细节图里面
				goods.setGoods_main_photo(photo);//主图绑定
			}else{
				goods.getGoods_photos().add(photo);
			}
			flagMainPhoto++;
		}
		photokvIter=goodsDetailMap.entrySet().iterator();//取出商品详情图
		while(photokvIter.hasNext()){//生成商品详情图
			entry=photokvIter.next();
			val=(Long) entry.getValue();
			photo=this.accessoryService.getObjById(val);
			photo.setDeleteStatus(false);
			this.accessoryService.update(photo);//将所有的图片记录删除状态改为false
			goods_details="<img  src="+SystemResPath.hostAddr+"/"+photo.getPath()+"/"+photo.getName()+"  />"+goods_details;
		}
		goods.setGoods_details(goods_details);//设置商品详情
		ret=this.goodsService.save(goods);
		if(ret){
			request.getSession().removeAttribute("appPhotoUploadMap");
			request.getSession().removeAttribute("goodsDetailMap");
			if("spec".equals(goods.getInventory_type())){
				WriteFileUtils.specStrToList(goodsSpecStr,goods,commonService);
			}
			ApiUtils.json(response, "","操作商品成功", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:mulGoodsId==>["79525","79524","79523"]
	 *@description:app端删除商品
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/fromAppDeleteGoods.htm")
	public void fromAppDeleteGoods(HttpServletRequest request,
			HttpServletResponse response,String mulGoodsId,Long userId){
		if(ApiUtils.is_null(mulGoodsId,userId+"")){
			ApiUtils.json(response, "","所需参数不能为空", 1); 
			return;
		}
		Integer succNum=0;
		boolean excuRet=false;
		Goods goods=null;
		List<Long> goodsIdList=JSON.parseArray(mulGoodsId, Long.class);
		User user=this.userService.getObjById(userId);
		for(Long goodsId:goodsIdList){
			goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
			if(goods!=null&&user!=null){//用户和店主是同一个人
				if(userId.longValue()==goods.getGoods_store().getUser().getId().longValue()){
					goods.setDeleteStatus(true);
					goods.setGoods_status(1);
					excuRet=this.goodsService.update(goods);
					if(excuRet){
						succNum++;
					}
				}
			}
		}
		ApiUtils.json(response, "","删除"+succNum+"个商品成功", 0); 
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:app端获取要编辑的商品信息
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/acquireGoodsInfo.htm")
	public void acquireGoodsInfo(HttpServletRequest request,
			HttpServletResponse response,String goodsId){
		if(ApiUtils.is_null(goodsId)){
			ApiUtils.json(response, "","所需参数goodsId不能为空", 1); 
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		if(goods!=null&&!goods.isDeleteStatus()){//商品存在并且商品没有并删除
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Goods.class,"id,goods_name,goods_price,store_price,goods_photos,"+ 
						"inventory_type,settlement_price,goods_inventory,goods_status,isHideRebate,"+ 
						"goods_weight,goods_volume,seo_keywords,seo_description,goods_choice_type,"+ 
						"goods_recommend,gc,goods_main_photo,speifi_list"));
			objs.add(new FilterObj(Accessory.class, "id,name,path,ext"));
			objs.add(new FilterObj(GoodsClass.class, "id,className,parent"));
			objs.add(new FilterObj(Specifi.class, "id,specifi,settlement_price,price,inventory,tuiguang_price,number"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, goods, "接口调用成功", 0, filter);
		}else{
			ApiUtils.json(response, "", "商品被删除,不能查看", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:卖家在app端编辑商品时新增图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/whenEditGoodsNewAddPhoto.htm")
	public void whenEditGoodsNewAddPhoto(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsId){
		Long begT=System.currentTimeMillis();
		MultipartHttpServletRequest multipartRequest=null;//声明reqest的多部分的文件,用于批量上传
		CommonsMultipartFile file=null;//声明要写入的file对象，指具体的对象
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		String paramsKey="";//传递上来的文件名
		String originalName="";//文件的原始名字
		String extend="";//文件的后缀名
		String photoName="";//图片的名字
		String photoPath="";//图片的路径
		Accessory photo=null;//需要新建的图片
		SysConfig sysConfig=null;//和图片路径配置相关
		Album album=null;
		boolean ret=false;
		String filePathName="";
		String folderPath="";
		List<Accessory> outputPhoto=new ArrayList<Accessory>();
		String outStr="";
		if(multipartResolver.isMultipart(request)){//这里判断request请求中是否有文件上传
			User user=this.userService.getObjById(CommUtil.null2Long(userId));
			Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
			album=this.getDefaultAlbum(album, user);
			sysConfig=this.configService.getSysConfig();
			multipartRequest= (MultipartHttpServletRequest) request;
			Iterator<?> fileNameIter=multipartRequest.getFileNames();
			List<Accessory> photoList=null;
			String ext="";
			String samllPhoto="";
			String midPhoto="";
			while(fileNameIter.hasNext()){//如果只有文件名，没有对应的文件内容的话，这里面是不进去的
				paramsKey=(String) fileNameIter.next();
				file = (CommonsMultipartFile) multipartRequest.getFile(paramsKey);
				photoName=UUID.randomUUID().toString();
				originalName=file.getOriginalFilename();
				extend=originalName.substring(originalName.lastIndexOf(".") + 1);
				photoPath=this.storeTools.createUserFolderURL(sysConfig, user.getStore());
				photoList=goods.getGoods_photos();
				if(photoList!=null&&photoList.size()==4){//再一次进入之后,size的大小为4的话,说明已经达到标准
					outStr="商品细节图片最多上传4张";
					continue;
				}
				if(file.getSize()>1048576){
					outStr=originalName+"此文件太大，请选择小于1M的图片上传";
					continue;
				}
				photo=new Accessory(false, new Date(), 400, 400, photoName+"."+extend, extend, photoPath, user, "app端上传商品", file.getSize(), album, sysConfig);
				ret=this.accessoryService.save(photo);
				if(ret){
					photoList.add(photo);//因为是引用类型传递,所以不需要重新set
					outputPhoto.add(photo);
					ret=this.goodsService.update(goods);
					if(ret){
						folderPath=SystemResPath.imgUploadUrl+"/"+photo.getPath();
						CommUtil.createFolder(folderPath);//创建文件夹
						filePathName=SystemResPath.imgUploadUrl +"/"+ photo.getPath() + "/" + photo.getName();
						ext= photo.getExt().indexOf(".") < 0 ? "."+ photo.getExt() : photo.getExt();//得到后缀名
						samllPhoto= filePathName + "_small" + ext;
						midPhoto= filePathName + "_middle" + ext;
						WriteFileUtils.writeFile(filePathName, file, file.getSize());//写入原图
						WriteFileUtils.writePhotoToServer(filePathName, samllPhoto, sysConfig.getSmallWidth(), sysConfig.getSmallHeight(), true);
						WriteFileUtils.writePhotoToServer(filePathName, midPhoto,sysConfig.getMiddleWidth(), sysConfig.getMiddleHeight(), true);
					}
				}
			}
		}
		if(outputPhoto.size()>0){
			List<FilterObj> objs = new ArrayList<FilterObj>();
			objs.add(new FilterObj(Accessory.class, "id,name,path,ext"));
			CustomerFilter filter = ApiUtils.addIncludes(objs);
			ApiUtils.json(response, outputPhoto, "上传成功", 0, filter);
		}else{
			ApiUtils.json(response, "", outStr, 0);
		}
		Long endT=System.currentTimeMillis();
		System.out.println("app编辑图片时上传细节图==>"+outputPhoto.size()+"张图片完成,总共耗时"+(endT-begT)+"毫秒");
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:multPhotoId==>["648029","648028","648027"]
	 *@description:app端编辑商品时删除细节图的图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/whenEditGoodsDeletePhoto.htm")
	public void whenEditGoodsDeletePhoto(HttpServletRequest request,
			HttpServletResponse response,String goodsId,String multPhotoId){
		if(ApiUtils.is_null(goodsId,multPhotoId)){
			ApiUtils.json(response, "","goodsId,photoId等所需参数不能为空", 0); 
			return;
		}
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		List<Accessory> photoList=null;
		Accessory photo=null;
		Accessory mainPhoto=null;
		Integer succNum=0;
		boolean flagIsSame=false;//用来标识商品和图片是属于同一个
		if(goods!=null){
			List<Long> photoIdList=JSON.parseArray(multPhotoId, Long.class);
			for(Long photoId:photoIdList){
				flagIsSame=false;
				photoList=goods.getGoods_photos();//取出商品细节图
				mainPhoto=goods.getGoods_main_photo();//得到主图
				photo=this.accessoryService.getObjById(photoId);
				if(mainPhoto!=null&&mainPhoto.getId().longValue()==photoId.longValue()){//如果是主图
					if(photoList.size()>1){//删除主图就先删除细节图,保证主图不可缺失
						goods.setGoods_main_photo(photoList.get(0));//默认取集合中的第一条为主图,保证主图不可删除
						photoList.remove(0);//清空这张图片在细节图中的引用,因为这里是引用类型传值,所以不用重新set
						flagIsSame=true;
						succNum++;
					}
				}else{
					for(int i=0;i<photoList.size();i++){
						if(photoList.get(i).getId().longValue()==photoId.longValue()){
							photoList.remove(i);//因为这里是引用类型传值,所以不用重新set
							flagIsSame=true;
							succNum++;
						}
					}
				}
				if(flagIsSame){
					photo.setDeleteStatus(true);
					photo.setUser(null);
					photo.setAlbum(null);
					photo.setConfig(this.configService.getSysConfig());//保证能让这条记录进入定时清理
					this.accessoryService.update(photo);//保证接口的快速响应,这里不去删除磁盘文件,等待定时器去清理
					this.goodsService.update(goods);//更新商品
				}
			}
		}
		if(succNum>0){
			ApiUtils.json(response, "", "删除"+succNum+"张图片成功,每一个商品都必须保持有一张主图", 0);
		}else{
			ApiUtils.json(response, "", "没有要删除的照片", 1);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:在app端编辑规格或者是新增规格,整体进行提交
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appCreateOrEditSpec.htm")
	public void appEditSpec(HttpServletRequest request,HttpServletResponse response,
			String specInfo,String goodsId){
		Specifi specifiEntity=null;//==>规格实体
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		List<JSONObject> specInfoList=JSON.parseArray(specInfo, JSONObject.class);
		String sepcId="";
		Double stroePrice=0D;
		Double settlePrice=0D;
		Integer succNum=0;
		Integer totalNum=0;
		for(JSONObject jsonobj:specInfoList){
			totalNum++;
			sepcId=jsonobj.getString("id");
			stroePrice=CommUtil.null2Double(jsonobj.getString("price"));
			settlePrice=CommUtil.null2Double(jsonobj.getString("settlement_price"));
			if("".equals(CommUtil.null2String(sepcId))){//说明这条记录不存在,此时则要新建
				specifiEntity=new Specifi();
			}else{
				specifiEntity=(Specifi) this.commonService.getById("Specifi", sepcId);
			}
			specifiEntity.setGoods(goods);
			specifiEntity.setInventory(CommUtil.null2Int(jsonobj.getString("inventory")));
			specifiEntity.setNumber(jsonobj.getString("number"));
			specifiEntity.setPrice(stroePrice);
			specifiEntity.setSettlement_price(settlePrice);
			specifiEntity.setSpecifi(jsonobj.getString("specifi"));
			specifiEntity.setTuiguang_price(CommUtil.null2Double(jsonobj.getString("tuiguang_price")));
			if(settlePrice>=stroePrice*0.9){//如果结算价大于店铺价的90%
				continue;
			}
			if("".equals(CommUtil.null2String(sepcId))){//说明这条记录不存在,此时则要新建
				this.commonService.save(specifiEntity);
			}else{
				this.commonService.update(specifiEntity);
			}
			succNum++;
		}
		if(totalNum.intValue()==succNum.intValue()){
			ApiUtils.json(response, "", "修改规格"+succNum+"条成功", 0);
		}else{
			ApiUtils.json(response, "", "修改规格"+succNum+"条成功,其中"+(totalNum-succNum)+"条没有修改,结算价不能大于等于店铺家的90%", 1);
		}
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:multSpecId==>["648029","648028","648027"]
	 *@description:在app端编辑商品时进行规格的删除
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appDeleteGoodsSpec.htm")
	public void appDeleteGoodsSpec(HttpServletRequest request,HttpServletResponse response,
			String multSpecId,Long goodsId){
		List<Long> specIdList=JSON.parseArray(multSpecId,Long.class);
		Short succNum=0;
		for(Long specId:specIdList){
			Specifi specifi=(Specifi) this.commonService.getById("Specifi", specId+"");
			if(goodsId.longValue()==specifi.getGoods().getId().longValue()){
				if(specifi!=null){
					String sql="DELETE FROM ecm_spec WHERE id="+specId;
					this.commonService.executeNativeSQL(sql);
				}
				succNum++;
			}
		}
		ApiUtils.json(response, "", "删除"+succNum+"条规格成功", 0);
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:卖家在app端编辑商品完成
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appEditGoodsFinish.htm")
	public void appEditGoodsFinish(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsClassId,Long goodsId){
		if(ApiUtils.is_null(userId,goodsClassId)){
			ApiUtils.json(response, "","userId和goodsClassId不能缺少", 1);
			return;
		}
		boolean ret=false;
		User  user=this.userService.getObjById(CommUtil.null2Long(userId));
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		goods=FormAssemblyUtils.assemblyForm(request, null,goods);//更新商品,并且批量装配属性
		Double storePrice=CommUtil.null2Double(goods.getStore_price());
		Double settlementPrice=CommUtil.null2Double(goods.getSettlement_price());
		String goodsName=CommUtil.null2String(goods.getGoods_name());
		if(user==null){
			ApiUtils.json(response, "","userId不合法", 1);
			return;
		}
		//因为goods在加载的时候,会进行赖加载,查询的时候会自动封装对应的实体,如果查不到那么就返回一个空list(hibernate的鲁棒性),不需判断是否为空
		if(goods.getGoods_photos().size()!=4){
			goods.setGoods_status(1);//做下架处理
			this.goodsService.update(goods);
			ApiUtils.json(response, "","编辑商品不规范，商品必须带有4张细节图片", 1);
			return;
		}
		if(storePrice<=0||settlementPrice<=0){
			ApiUtils.json(response, "","店铺价和结算价不能小于0", 1);
			return;
		}
		if(settlementPrice>storePrice*0.9){//如果结算价大于店铺价的90%
			ApiUtils.json(response, "","结算价不能大于店铺价的90%", 1);
			return;
		}
		if("".equals(goodsName)||goodsName.length()>50){
			ApiUtils.json(response, "","商品名字不能为空并且长度不能超过50个字符", 1);
			return;
		}
		GoodsClass goodsClass=this.goodsClassService.getObjById(CommUtil.null2Long(goodsClassId));
		goods.setGc(goodsClass);
		goods.setGoods_current_price(goods.getStore_price());
		goods.setAddTime(new Date());
		goods.setGoods_store(user.getStore());
		goods.setCtj(1.00D);
		goods.setZhanlue_price(BigDecimal.valueOf(0));
		//设置推广价
		goods.setTuiguang_price(BigDecimal.valueOf(goods.getSettlement_price().doubleValue() + goods.getCtj()));
		ret=this.goodsService.update(goods);
		if(ret){
			ApiUtils.json(response, "","编辑商品成功", 0);
			return;
		}
	}
	//得到用户的相册,使相册和照片管理起来,相册区管理照片
	private Album getDefaultAlbum(Album album,User user){
		album = this.albumService.getDefaultAlbum(user.getId());
		if (album == null) {
			album=new Album(new Date(),true,"默认相册",-10000,user);
			this.albumService.save(album);
		}
		return album;
	}
	//更新session
	public void updateSession(HttpSession session,String originalName,Long photoId){
		Map<String,Long> sessMap=(Map<String, Long>) session.getAttribute("appPhotoUploadMap");
		if(sessMap==null){
			sessMap=new LinkedHashMap<String, Long>();
			sessMap.put(originalName, photoId);
			session.setAttribute("appPhotoUploadMap", sessMap);//将图片的id放入session
		}else{
			sessMap.put(originalName,  photoId);
			session.setAttribute("appPhotoUploadMap", sessMap);
		}
		System.out.println(sessMap.toString());
	}
	//判断文件名是否重复==>是否可以上传
	public boolean fileIsAlreadyExist(HttpSession session,String originalName){
		boolean ret=false;
		Map<String,Long> sessMap=(Map<String, Long>) session.getAttribute("appPhotoUploadMap");
		if(sessMap!=null&&sessMap.containsKey(originalName)){
			ret=false;
		}else{
			ret=true;
		}
		return ret;
	}
	//图片总共上传是5张
	public boolean isBeyondNum(HttpSession session,String originalName){
		boolean ret=true;
		Map<String,Long> sessMap=(Map<String,Long>) session.getAttribute("appPhotoUploadMap");
		if(sessMap!=null&&sessMap.size()==5){
			ret=false;
		}
		return ret;
	}
	//写入图片到服务器
	private Integer generateImage(List<Map<String,Object>> retList,SysConfig sysConfig) {
		// TODO Auto-generated method stub
		CommonsMultipartFile file=null;//声明要写入的file对象，指具体的对象
		String folderPath="";
		Accessory photo=null;
		String source="";
		String ext="";
		String samllPhoto="";
		String midPhoto="";
		Integer succNum=0;
		boolean ret=false;
		if(retList!=null&&retList.size()>0){
			Iterator<Map<String,Object>> retIte=retList.iterator();
			while(retIte.hasNext()){
				Map<String,Object> fileInfoMap=retIte.next();
				file=(CommonsMultipartFile) fileInfoMap.get("file");
				photo=(Accessory) fileInfoMap.get("photo");
				if(photo!=null){
					folderPath=SystemResPath.imgUploadUrl+"/"+photo.getPath();
					CommUtil.createFolder(folderPath);//创建文件夹
					ext= photo.getExt().indexOf(".") < 0 ? "."+ photo.getExt() : photo.getExt();//得到后缀名
					source = SystemResPath.imgUploadUrl +"/"+ photo.getPath() + File.separator + photo.getName();
					samllPhoto= source + "_small" + ext;
					midPhoto= source + "_middle" + ext;
					ret=WriteFileUtils.writeFile(source, file, file.getSize());//写入原图
					WriteFileUtils.writePhotoToServer(source, samllPhoto, sysConfig.getSmallWidth(), sysConfig.getSmallHeight(), true);
					WriteFileUtils.writePhotoToServer(source, midPhoto,sysConfig.getMiddleWidth(), sysConfig.getMiddleHeight(), true);
					if(ret){
						succNum++;
					}
				}
			}
		}
		return succNum;
	}
}
