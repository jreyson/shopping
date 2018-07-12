package com.shopping.api.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.shopping.api.domain.evaluate.AppraiseMessageEntity;
import com.shopping.api.domain.evaluate.AssessingDiscourseEntity;
import com.shopping.api.domain.evaluate.StartsExplainEntity;
import com.shopping.api.domain.evaluate.StoreEvaluteEntity;
import com.shopping.api.domain.evaluate.StoreScoreEntity;
import com.shopping.api.domain.evaluate.VVPResourceEntity;
import com.shopping.api.service.evaluate.IEvaluateFunctionService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.api.tools.WriteFileUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IUserService;
import com.shopping.core.tools.FileUtil;
/***
 *@author:akangah
 *@description:评价相关的控制器
 ***/
@Controller
public class EvaluteFunction {
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;
	@Autowired
	@Qualifier("startsExplain")
	private IEvaluateFunctionService<StartsExplainEntity> startsExplainService;
	@Autowired
	@Qualifier("appraiseMessage")
	private IEvaluateFunctionService<AppraiseMessageEntity> appraiseMessageService;
	@Autowired
	@Qualifier("assessingDiscourse")
	private IEvaluateFunctionService<AssessingDiscourseEntity> assessingDiscourseService;
	@Autowired
	@Qualifier("vVPResource")
	private IEvaluateFunctionService<VVPResourceEntity> vVPResourceService;  
	@Autowired
	@Qualifier("storeEvalute")
	private IEvaluateFunctionService<StoreEvaluteEntity> storeEvaluteService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	@Qualifier("storeScore")
	private IEvaluateFunctionService<StoreScoreEntity> storeScoreService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:
	 *@description:上传文件的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/appUploadResource.htm", method = RequestMethod.POST)
	public void appUploadResource(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsId){
		if(ApiUtils.is_null(userId,goodsId)){
			ApiUtils.json(response, "", "userId或者goodsId等参数不能为空", 1);
			return;
		}
		Map<String,String> viewMap=(Map<String, String>) request.getSession().getAttribute(goodsId);
		if(this.judgeIsLawful(viewMap, "")){
			ApiUtils.json(response, "", "每个商品对应的视频，音频，照片文件最多是7个，请删除之后再上传", 1);
			return;
		}
		String paramsKey="";//上传文件对应的key值
		String extend="";//上传文件的扩展名
		String folderPath="";//文件夹路径
		String fileName="";//文件名字
		String originalFilename="";//原始文件名字
		String surfacePlotName="";//封面图名字
		String filePathName="";//文件路径名字
		String mediaPicPath="";//转化得出的文件名字  
		String saveResOfFloder="shoppingEvaluateVVP";
		String saveDataBaseFileName="";
		String saveDataBasePath="";
		String saveDataBasePlotName="";
		String returnRet="";
		String outMess="";
		Map<String,String> retMap=new HashMap<String,String>();
		Map<String,String> sessionMap=new HashMap<String,String>();
		boolean isSuccess=false;
		boolean isDelete=false;
		boolean ret=true;
		Integer count=0;
		User user=null;
		Goods goods=null;
		float fileSize=0F;
		CommonsMultipartFile file=null;
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		if(multipartResolver.isMultipart(request)){//判断 request 是否有文件上传,即多部分请求  
			user=this.userService.getObjById(CommUtil.null2Long(userId));
			goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
			if(user==null||goods==null){
				ApiUtils.json(response, "", "userId或者goodsId等参数不合法", 1);
				return;
			}
			MultipartHttpServletRequest multipartRequest=(MultipartHttpServletRequest)request;
			Iterator<?> iter =multipartRequest.getFileNames();
			while(iter.hasNext()){
				paramsKey=(String) iter.next();
				file=(CommonsMultipartFile) multipartRequest.getFile(paramsKey);//获取到要写入的文件
				originalFilename=file.getOriginalFilename();//原始文件名
				if(this.judgeIsLawful(viewMap, originalFilename)){
					outMess="同一文件不能重复上传";
					continue;
				}
				if(this.judgeIsLawful(viewMap, "")){
					ApiUtils.json(response, "", "每个商品对应的视频，音频，照片文件最多是7个，请删除之后再上传", 1);
					return;
				}
				extend =originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
										.toLowerCase();//文件扩展名字
				fileSize = Float.valueOf(file.getSize()/1024).floatValue();//取出上传文件大小,并且转化成kb形式
				folderPath=SystemResPath.shoppingEvaluateVVP+File.separator+saveResOfFloder+//要创建的文件夹路径
						   File.separator+user.getId()+File.separator+goods.getId();
				fileName=UUID.randomUUID().toString();//文件名字
				surfacePlotName=UUID.randomUUID().toString();//封面图名字
				filePathName=folderPath+File.separator+//要写入的文件名
							 fileName+"."+extend;
				mediaPicPath=folderPath+File.separator+//要写入的文件名
									    surfacePlotName;
				saveDataBaseFileName=fileName+"."+extend;
				saveDataBasePath="/"+saveResOfFloder+"/"+user.getId()+"/"+goods.getId()+"/";
				saveDataBasePlotName=surfacePlotName+".jpg";
				ret=CommUtil.createFolder(folderPath);
				if(ret){
					returnRet=WriteFileUtils.detectionFileFormat(extend.toUpperCase());
					if("videoFile".equals(returnRet)){
						if(fileSize>3072){//如果是视频的话，最大不能超过3M
							outMess="视频文件最大不能超过3M";
							break;//因为视频只支持一个，所以这里break
						}
						if(WriteFileUtils.judgeIsContainVv(viewMap, (short)0)){
							outMess="每一个商品只能上传一个视频";
							break;//因为视频只支持一个，所以这里break
						}
						isSuccess=WriteFileUtils.writeFile(filePathName, file, fileSize);//写入文件
						if(isSuccess){
							retMap=this.saveResToDatabase(saveDataBaseFileName, 
												extend, saveDataBasePath, fileSize, filePathName, mediaPicPath,
												saveDataBasePlotName,originalFilename);
							if(retMap.size()>0){
								count++;
								this.sessionMap(request, retMap, sessionMap,goodsId);//更新session
							}
						}
					}else if("voiceFile".equals(returnRet)){
						if(fileSize>1204){//如果是音频的话，最大不能超过1M
							outMess="音频文件最大不能超过1M";
							break;//因为音频只支持一个，所以这里break
						}
						if(WriteFileUtils.judgeIsContainVv(viewMap, (short)1)){
							outMess="每一个商品只能上传一个音频";
							break;//因为视频只支持一个，所以这里break
						}
						isSuccess=WriteFileUtils.writeFile(filePathName, file, fileSize);//写入文件
						if(isSuccess){
							retMap=this.saveVoiceResToDataBase(saveDataBaseFileName, saveDataBasePath, 
									fileSize, extend, originalFilename);
							if(retMap.size()>0){
								count++;
								this.sessionMap(request, retMap, sessionMap,goodsId);
							}
						}
					}else if("pictureFile".equals(returnRet)){
						if(fileSize>1024){//如果是图片的话，最大不能超过200k
							outMess="图片文件最大不能超过1024k";
							continue;
						}
						boolean pictureRet=WriteFileUtils.writeFile(filePathName, file, fileSize);//写入文件
						if(pictureRet){
							isSuccess=WriteFileUtils.writePhotoToServer(filePathName, mediaPicPath+"."+extend, 610,610,true);//转化大小
							if(isSuccess){
								isDelete=this.deleteFile(filePathName);
								if(isDelete){
									Double photoSize=this.acquireFileSize(mediaPicPath+"."+extend);
									VVPResourceEntity photoRes=new VVPResourceEntity(surfacePlotName+"."+extend, 
											saveDataBasePath, photoSize.floatValue(), extend, "图片资源", 
											"photo", null, null,originalFilename);
									photoRes.setAddTime(new Date());
									boolean saveRet=this.vVPResourceService.save(photoRes);
									if(saveRet){
										boolean generateRet=WriteFileUtils.writePhotoToServer(mediaPicPath+"."+extend, mediaPicPath+"_surface"+"."+extend, 170,170,true);
										if(generateRet){
											Double surfaceSize=this.acquireFileSize(mediaPicPath+"_surface"+"."+extend);
											VVPResourceEntity surfaceRes=new VVPResourceEntity(surfacePlotName+"_surface"+"."+extend, 
													saveDataBasePath, surfaceSize.floatValue(), 
													extend, "图片资源", "photo", null, null,"");
											surfaceRes.setAddTime(new Date());
											boolean photoSaveRet=this.vVPResourceService.save(surfaceRes);
											if(photoSaveRet){
												photoRes.setSurfacePlot(surfaceRes);
												this.vVPResourceService.update(photoRes);
												retMap.put(photoRes.getOriginalFilename(),photoRes.getId().toString());
												if(retMap.size()>0){
													count++;
													this.sessionMap(request, retMap, sessionMap,goodsId);
												}
											}
										}
									}
								}
							}
						}
					}else{
						outMess="不支持的文件格式上传,视频支持mp4,mov。音频支持mp3。图片支持png,jpg,gif";
						continue;
					}
				}else{
					ApiUtils.json(response, "", "创建路径失败,不能上传文件", 1);
					return;
				}
			}
			if(count==0){
				ApiUtils.json(response, "", outMess, 1);
				return;
			}else{
				ApiUtils.json(response, count, "上传"+count+"个文件成功", 0);
				return;
			}
		}else{
			ApiUtils.json(response, "", "请选择要上传的文件", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:[{"goodsId":8882,"originalFilename":"kfah.mp4,af.mp3,jh.png"},{}]
	 *@description:删除文件的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/deleteDataFromSession.htm", method = RequestMethod.POST)
	public void deleteDataFromSession(HttpServletRequest request,HttpServletResponse response,
			String goodsFilelist){
		if(ApiUtils.is_null(goodsFilelist)){
			ApiUtils.json(response, "", "此参数不能为空", 1);
			return;
		}
		Integer count=0;
		String vvpId="";
		String retuanRetView=null;
		String goodsId="";
		String originalFilename="";
		VVPResourceEntity vVPResourceEntity=null;
		boolean flag=false;
		boolean ret=false;
		Map<String,String> sessMap=null;
		Map<String,String> sessionMap=new HashMap<String,String>();
		List<JSONObject> paramList=JSON.parseArray(goodsFilelist,JSONObject.class);
		if(paramList.size()<=0){
			ApiUtils.json(response, "", "没有要删除的文件", 1);
			return;
		}else{
			for(JSONObject obj:paramList){
				goodsId=obj.getString("goodsId");
				originalFilename=obj.getString("originalFilename");
				if("".equals(CommUtil.null2String(goodsId))){
					continue;
				}
				if("".equals(CommUtil.null2String(originalFilename))){
					continue;
				}
				sessMap=(Map<String, String>) request.getSession().getAttribute(goodsId);
				String[] originalFileArray=originalFilename.replace(" ", "").split(",");
				if(sessMap==null){
					continue;
				}else{
					if(sessMap.isEmpty()){
						continue;
					}else{
						for(String keyName:originalFileArray){
							flag=sessMap.containsKey(keyName);
							if(flag){
								vvpId=sessMap.get(keyName);
								if(!"".equals(CommUtil.null2String(vvpId))){
									retuanRetView=sessMap.remove(keyName);//因为操作的是引用类型的变量,所以这里操作完成之后,session也会相应改变
									if(retuanRetView!=null){
										vVPResourceEntity=this.vVPResourceService.getObjById(CommUtil.null2Long(vvpId));
										if(vVPResourceEntity!=null){
											if(vVPResourceEntity.getResType()=="voice"){
												ret=this.deleteFileFromServer(vVPResourceEntity, 0);
												if(ret){
													//更新session  sessMap为对session修改之后的数据
													this.sessionMap(request, sessMap, sessionMap,goodsId);
													count++;
												}
											}else{//非音频文件删除
												ret=this.deleteFileFromServer(vVPResourceEntity, 1);
												if(ret){
													//更新session
													this.sessionMap(request, sessMap, sessionMap,goodsId);
													count++;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if(count==0){
			ApiUtils.json(response, count, "没有要移除的文件", 1);
			return;
		}else{
			ApiUtils.json(response, count, "移除"+count+"个文件成功", 0);
			return;
		}
	}
	//每次判断map集合的大小，以及是否要添加
	private boolean judgeIsLawful(Map<String,String> viewMap,String originalFilename){
		boolean isLawful=false;
		boolean ret=false;
		if(viewMap==null){
			return isLawful;
		}else{
			if(!"".equals(CommUtil.null2String(originalFilename))){
				ret=viewMap.containsKey(originalFilename);
				if(ret){
					isLawful=true;
				}
			}else{
				if(viewMap.size()==7){
					isLawful=true;
				}
			}
		}
		return isLawful;
	}
	//从服务器删除文件并且从数据库中也要删除
	public boolean deleteFileFromServer(VVPResourceEntity vVPResourceEntity,int flag){
		boolean ret=false;
		String filePath="";
		if(vVPResourceEntity==null){
			return ret;
		}else{
			filePath=SystemResPath.shoppingEvaluateVVP+
					 this.returnFilePath(vVPResourceEntity.getResPath())+
					 vVPResourceEntity.getResName();
			if(flag==0){//如果是音频文件的话,只删除一次
				ret=this.deleteFile(filePath);
				if(ret){
					this.vVPResourceService.remove(vVPResourceEntity.getId());
				}
			}else{
				ret=this.deleteFile(filePath);
				if(ret){//这里删除文件，无法检测时同时成功还是同时失败，所以只要第一个文件删除成功的话，就进行以下操作
					filePath=SystemResPath.shoppingEvaluateVVP+
							 this.returnFilePath(vVPResourceEntity.getSurfacePlot().getResPath())+
							 vVPResourceEntity.getSurfacePlot().getResName();
					this.deleteFile(filePath);
					this.vVPResourceService.remove(vVPResourceEntity.getId());//级联删除数据库记录
				}
			}
		}
		return ret;
	}
	//修改string，变成需要的格式
	private String returnFilePath(String resPath){
		resPath=resPath.replace("/", "\\");
		return resPath;
	}
	//更新session
	private void sessionMap(HttpServletRequest request,Map<String,String> retMap,
			Map<String,String> sessionMap,String goodsId){
		if(request.getSession(false).getAttribute(goodsId)==null){
			request.getSession(true).setAttribute(goodsId, retMap);
		}else{
			sessionMap=(Map<String, String>) request.getSession().getAttribute(goodsId);
			sessionMap.putAll(retMap);//合并
			request.getSession().setAttribute(goodsId, sessionMap);
		}
		System.out.println(JSON.toJSONString(retMap));
	}
	//保存音频文件到数据库
	private Map<String,String> saveVoiceResToDataBase(String saveDataBaseFileName,String saveDataBasePath,
			Float fileSize,String extend,String originalFilename){
		Map<String,String> voiceResList=new HashMap<String, String>();
		VVPResourceEntity voiceRes=new VVPResourceEntity(saveDataBaseFileName, 
				saveDataBasePath, fileSize, extend, "音频资源", "voice", null, null,originalFilename);
		voiceRes.setAddTime(new Date());
		boolean ret=this.vVPResourceService.save(voiceRes);//入库
		if(ret){
			VVPResourceEntity photoRes=new VVPResourceEntity("img_default_evaluation_audio.png", 
					"/default/",1.1F , "png", "图片资源", "photo", null, null,"");
			photoRes.setAddTime(new Date());
			boolean isSuc=this.vVPResourceService.save(photoRes);
			if(isSuc){
				voiceRes.setSurfacePlot(photoRes);
				this.vVPResourceService.update(voiceRes);
				voiceResList.put(voiceRes.getOriginalFilename(),voiceRes.getId().toString());
			}
		}
		return voiceResList;
	}
	//将视频路径保存到数据库
	private  Map<String,String> saveResToDatabase(String saveDataBaseFileName,String extend,
			String saveDataBasePath,Float fileSize,
					String filePathName,String mediaPicPath,String saveDataBasePlotName,
					String originalFilename){
		Map<String,String> retMap=new HashMap<String,String>();
		VVPResourceEntity videoRes=new VVPResourceEntity(saveDataBaseFileName, 
				saveDataBasePath, fileSize, extend, "视频资源", "video", null, null,originalFilename);
		videoRes.setAddTime(new Date());
		boolean videoIsSave=this.vVPResourceService.save(videoRes);//入库
		if(videoIsSave){
			boolean saveRet=WriteFileUtils.generateSurfacePlot(filePathName, mediaPicPath+".jpg","170*170");
			if(saveRet){
				VVPResourceEntity photoRes=new VVPResourceEntity(saveDataBasePlotName, 
						saveDataBasePath,6F , "jpg", "图片资源", "photo", null, null,"");
				photoRes.setAddTime(new Date());
				boolean videoSaveRet=this.vVPResourceService.save(photoRes);
				if(videoSaveRet){
					videoRes.setSurfacePlot(photoRes);
					this.vVPResourceService.update(videoRes);
					retMap.put(videoRes.getOriginalFilename(),videoRes.getId().toString());
				}
			}
		}
		return retMap;
	 }
	//获取文件大小
	public Double acquireFileSize(String filePath){
		File file=null;
		Double fileSize=0.0D;
		FileChannel fc= null; 
		FileInputStream fis=null;
	    try{  
	    	file= new File(filePath);  
	        if (file.exists() && file.isFile()){  
	        	fis = new FileInputStream(file);  
	            fc= fis.getChannel();
	            fileSize=(double) fc.size();
	        }else{  
	            System.out.println("file doesn't exist or is not a file");
	        }  
	    } catch (FileNotFoundException e) {  
	        e.printStackTrace();  
	        fileSize=0.0D;
	    } catch (IOException e) {  
	        e.printStackTrace();
	        fileSize=0.0D;
	    } finally {  
	        if (null!=fc){  
	            try{
	                fc.close();  
	            }catch(IOException e){  
	                e.printStackTrace(); 
	            }  
	        }   
	    }
	    if(fileSize>0){
	    	fileSize/=1024;
	    }
	    return fileSize;
	}
	//删除文件
	 public boolean deleteFile(String fileName) {
		 File file = new File(fileName);
		 if(file.exists() && file.isFile()){//如果文件路径所对应的文件存在，并且是一个文件，则直接删除
			 if(file.delete()){
	            System.out.println("删除单个文件" + fileName + "成功！");
	            return true;
	         }else{
	            System.out.println("删除单个文件" + fileName + "失败！");
	            return false;
	         }
	     }else{
	    	 System.out.println("删除单个文件失败：" + fileName + "不存在！");
	         return false;
	     }
	 }
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:
	 *@description:保存订单的评价
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appSaveOrderEvaluateInfo.htm",method=RequestMethod.POST)
	public void appSaveOrderEvaluateInfo(HttpServletRequest request,HttpServletResponse response,String evaluateInfo,
			String userId,String password,String orderFormId) {
		if (ApiUtils.is_null(evaluateInfo,userId,password,orderFormId)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		JSONArray goodsEvaluates;
		JSONObject storeEvaluate;
		try {
			JSONObject jsonobj = JSON.parseObject(evaluateInfo);
			goodsEvaluates = jsonobj.getJSONArray("goodsEvaluates");
			storeEvaluate = jsonobj.getJSONObject("storeEvaluate");
		} catch (Exception e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "参数格式错误", 1);
			return;
		}		
		
		OrderForm order = orderFormService.getObjById(CommUtil.null2Long(orderFormId));
		if (order==null) {
			ApiUtils.json(response, "", "订单不存在", 1);
			return;
		}
		if (order.getOrder_status()!=40) {
			ApiUtils.json(response, "", "订单状态错误", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		if (user.getId()!=order.getUser().getId()) {
			ApiUtils.json(response, "", "用户不匹配", 1);
			return;
		}
		order.setOrder_status(50);
		order.setIsAdditional("0");
		this.orderFormService.update(order);
		OrderFormLog ofl = new OrderFormLog();
		ofl.setAddTime(new Date());
		ofl.setLog_info("评价订单");
		ofl.setLog_user(SecurityUserHolder.getCurrentUser());
		ofl.setOf(order);
		this.orderFormLogService.save(ofl);
		
		Date date = new Date();
		StartsExplainEntity logisticsSee = startsExplainService.getObjById(10l);//物流5星
		StartsExplainEntity serviceSee = startsExplainService.getObjById(15l);//服务5星
		StartsExplainEntity goodsSee = startsExplainService.getObjById(5l);//商品5星
		//服务星级，快递星级
		String logisticsRank = storeEvaluate.getString("logisticsRank");
		String serviceRank = storeEvaluate.getString("serviceRank");
		
		StartsExplainEntity logistics = startsExplainService.getObjById(CommUtil.null2Long(logisticsRank));
		StartsExplainEntity service = startsExplainService.getObjById(CommUtil.null2Long(serviceRank));
		logistics=logistics==null?logisticsSee:logistics;
		service=service==null?serviceSee:service;
		StoreEvaluteEntity storeEvaluteEntity=new StoreEvaluteEntity();
		storeEvaluteEntity.setAddTime(date);
		storeEvaluteEntity.setDeleteStatus(false);
		storeEvaluteEntity.setLogisticsEvaluation(logistics);
		storeEvaluteEntity.setServiceAttitude(service);
		storeEvaluteEntity.setStoreEvalute(order.getStore());
		this.storeEvaluteService.save(storeEvaluteEntity);
		
		String hql="select obj from StoreScoreEntity as obj where obj.store.id = " + order.getStore().getId().toString();
		List<StoreScoreEntity> query = this.storeScoreService.query(hql, null, -1, -1);
		if (query.size()>0) {
			this.updateStoreGrade(query.get(0), storeEvaluteEntity);
		}else {
			StoreScoreEntity scoreEntity=new StoreScoreEntity();
			scoreEntity.setAddTime(new Date());
			scoreEntity.setDeleteStatus(false);
			scoreEntity.setExpressSiledExplainShow(this.gradeType(storeEvaluteEntity.getLogisticsEvaluation().getStartsNum()));
			scoreEntity.setFiledExplainShow(this.gradeType(storeEvaluteEntity.getServiceAttitude().getStartsNum()));
			scoreEntity.setStore(order.getStore());
			scoreEntity.setStoreAverageScore(storeEvaluteEntity.getServiceAttitude().getStartsNum().floatValue());
			scoreEntity.setStoreEvalutePerNum(1);
			scoreEntity.setStoreExpressAverageScore(storeEvaluteEntity.getLogisticsEvaluation().getStartsNum().floatValue());
			this.storeScoreService.save(scoreEntity);
		}
		
		List<GoodsCart> gcs = order.getGcs();
		HttpSession session = request.getSession();
		for (GoodsCart goodsCart : gcs) {
			AppraiseMessageEntity ame=new AppraiseMessageEntity();
			StartsExplainEntity goodsSe=goodsSee;
			ame.setAddTime(date);
			ame.setDeleteStatus(false);
			ame.setGoods(goodsCart.getGoods());
			ame.setDescribeStarts(goodsSe);
			ame.setIsAnonymity(true);
			ame.setOrder(order);
			ame.setUser(user);
			
			AssessingDiscourseEntity assessingDiscourse=new AssessingDiscourseEntity();
			assessingDiscourse.setAddTime(date);
			assessingDiscourse.setDeleteStatus(false);
			assessingDiscourse.setAssessingCharacter("好评。");
			assessingDiscourse.setAssessingTime(date);
			
			for (int i = 0; i < goodsEvaluates.size(); i++) {
				JSONObject goodsEvaluate = goodsEvaluates.getJSONObject(i);
				String goodsId = goodsEvaluate.getString("goodsId");
				Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
				if (goods==null) {//商品不存在则跳过
					break;
				}
				if (goodsCart.getGoods().getId()!=goods.getId()) {
					continue;
				}
				String evaluates = goodsEvaluate.getString("evaluates");
				String goodsRank = goodsEvaluate.getString("goodsRank");
				String isAnonymity = goodsEvaluate.getString("isAnonymity");
				goodsSe = startsExplainService.getObjById(CommUtil.null2Long(goodsRank));
				goodsSe=goodsSe==null?goodsSee:goodsSe;
				ame.setDescribeStarts(goodsSe);
				ame.setIsAnonymity(CommUtil.null2Boolean(isAnonymity));
				assessingDiscourse.setAssessingCharacter(CommUtil.null2String(evaluates).equals("")?"好评。":CommUtil.null2String(evaluates));
			}
			boolean save = this.assessingDiscourseService.save(assessingDiscourse);
			if (save) {
				ame.setAssessingDiscourse(assessingDiscourse);
			}
			this.appraiseMessageService.save(ame);				
			Object retList = session.getAttribute(goodsCart.getGoods().getId()+"");
			if (retList!=null) {
				@SuppressWarnings("unchecked")
				Map<String,String> ret=(Map<String, String>) retList;
				Collection<String> values = ret.values();
				for (String s : values) {
					VVPResourceEntity vv = this.vVPResourceService.getObjById(CommUtil.null2Long(s));
					if (vv!=null) {
						vv.setAppraiseMessage(ame);
						vv.setFileIsDelete("no");
						this.vVPResourceService.update(vv);
						VVPResourceEntity surfacePlot = vv.getSurfacePlot();
						if (surfacePlot!=null) {
							surfacePlot.setFileIsDelete("no");
							this.vVPResourceService.update(surfacePlot);
						}
					}
				}
				session.removeAttribute(goodsCart.getGoods().getId()+"");
			}
		}
		ApiUtils.json(response, "", "评价成功", 0);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:
	 *@description:商家回复
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appSaveStoreEvaluate.htm",method=RequestMethod.POST)
	public void appSaveStoreEvaluate(HttpServletRequest request,HttpServletResponse response,String appraiseId,String evaluateContent,String userId,String password) {
		if (ApiUtils.is_null(appraiseId,evaluateContent,userId,password)) {
			ApiUtils.json(response, "", "参数不能为空", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		if (user.getStore()==null) {
			ApiUtils.json(response, "", "店铺不存在", 1);
			return;
		}
		AppraiseMessageEntity appraiseMessageEntity = appraiseMessageService.getObjById(CommUtil.null2Long(appraiseId));
		if (appraiseMessageEntity==null) {
			ApiUtils.json(response, "", "该评价不存在", 1);
			return;
		}
		System.out.println(appraiseMessageEntity.getGoods().getGoods_store().getId());
		System.out.println(user.getStore().getId());
		if (appraiseMessageEntity.getGoods().getGoods_store().getId()!=user.getStore().getId()) {
			ApiUtils.json(response, "", "没有权限", 1);
			return;
		}		
		AssessingDiscourseEntity assessingDiscourse = appraiseMessageEntity.getAssessingDiscourse();
		if (!"".equals(CommUtil.null2String(assessingDiscourse.getReplyCharacter()))) {
			ApiUtils.json(response, "", "该评价已经回复过了", 1);
			return;
		}
		assessingDiscourse.setReplyCharacter(evaluateContent);
		assessingDiscourse.setReplyTime(new Date());
		boolean update = assessingDiscourseService.update(assessingDiscourse);
		if (update) {
			ApiUtils.json(response, "", "回复成功", 0);
			return;
		}
		ApiUtils.json(response, "", "回复失败", 1);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:
	 *@description:卖家获取店铺商品评价
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appGetStoreEvaluate.htm",method=RequestMethod.POST)
	public void appGetStoreEvaluate(HttpServletRequest request,HttpServletResponse response,String userId,String password,String currentPage,String goodsId) {
		if (ApiUtils.is_null(userId,password)) {
			ApiUtils.json(response, "", "参数不能为空", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		Store store = user.getStore();
		if (store==null) {
			ApiUtils.json(response, "", "店铺不存在", 1);
			return;
		}
		String where = "";
		if (CommUtil.null2Long(goodsId)!=-1) {
			Goods goods = goodsService.getObjById(CommUtil.null2Long(goodsId));
			if (goods!=null) {
				where=" and obj.goods.id = " + goods.getId();
			}		
		}
		int current_page=0;
		int pageSize=10;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String hql="select obj from AppraiseMessageEntity as obj where obj.deleteStatus=false and obj.goods.goods_store.id = " + store.getId() + where + " order by obj.addTime DESC";
		List<AppraiseMessageEntity> query = appraiseMessageService.query(hql, null, current_page*pageSize, pageSize);
		for (AppraiseMessageEntity ame : query) {
			Boolean isAnonymity = ame.getIsAnonymity();
			if (isAnonymity) {
				ame.setUser(null);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppraiseMessageEntity.class, "id,describeStarts,vvrResource,assessingDiscourse,isAnonymity,user"));
		objs.add(new FilterObj(StartsExplainEntity.class, "startsNum,startExplain"));
		objs.add(new FilterObj(AssessingDiscourseEntity.class, "assessingCharacter,replyCharacter,appendWord,assessingTime,replyTime,appendTime"));
		objs.add(new FilterObj(VVPResourceEntity.class, "resName,resPath,ext,resType,surfacePlot"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query,"success",0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:goodsId 商品id,currentPage 页码,evaluateType 评价类型（好评：1；中评：2；差评：3）
	 *@description:获取商品评价
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appGetGoodsEvaluates.htm",method=RequestMethod.POST)
	public void appGetGoodsEvaluates(HttpServletRequest request,HttpServletResponse response,String goodsId,String currentPage,String evaluateType) {
		if ("".equals(CommUtil.null2String(goodsId))) {
			ApiUtils.json(response, "","参数错误",1);
			return;
		}
		Goods goods = goodsService.getObjById(CommUtil.null2Long(goodsId));
		if (goods==null) {
			ApiUtils.json(response, "","商品不存在",1);
			return;
		}
		int current_page=0;
		int pageSize=10;
		if("".equals(currentPage)||currentPage==null){
			current_page=0;
		}else{
			current_page=Integer.valueOf(currentPage).intValue();
		}
		String where="";
		if (CommUtil.null2Int(evaluateType)==1) {//好评
			where=" and obj.describeStarts.startsNum in (4,5)";
		}else if (CommUtil.null2Int(evaluateType)==2) {//中评
			where=" and obj.describeStarts.startsNum in (3)";
		}else if (CommUtil.null2Int(evaluateType)==3) {//差评
			where=" and obj.describeStarts.startsNum in (1,2)";
		}
		String hql="select obj from AppraiseMessageEntity as obj where obj.deleteStatus=false and obj.goods.id = " + goodsId + where + " order by obj.addTime DESC";
		List<AppraiseMessageEntity> query = appraiseMessageService.query(hql, null, current_page*pageSize, pageSize);
		for (AppraiseMessageEntity ame : query) {
			Boolean isAnonymity = ame.getIsAnonymity();
			if (isAnonymity) {
				ame.setUser(null);
			}
		}
		List<FilterObj> objs = new ArrayList<FilterObj>();
		objs.add(new FilterObj(AppraiseMessageEntity.class, "describeStarts,vvrResource,assessingDiscourse,isAnonymity,user"));
		objs.add(new FilterObj(StartsExplainEntity.class, "startsNum,startExplain"));
		objs.add(new FilterObj(AssessingDiscourseEntity.class, "assessingCharacter,replyCharacter,appendWord,assessingTime,replyTime,appendTime"));
		objs.add(new FilterObj(VVPResourceEntity.class, "resName,resPath,ext,resType,surfacePlot"));
		objs.add(new FilterObj(User.class, "id,userName,photo"));
		objs.add(new FilterObj(Accessory.class, "path,name"));
		CustomerFilter filter = ApiUtils.addIncludes(objs);
		ApiUtils.json(response, query,"success",0,filter);
		return;
	}
	/***
	 *@author:gaohao
	 *@return:void
	 *@param:
	 *@description:买家追评
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value= "/appAdditionalEvaluation.htm",method=RequestMethod.POST)
	public void appAdditionalEvaluation(HttpServletRequest request,HttpServletResponse response,String userId,String password,
			String orderFormId,String content) {
		if (ApiUtils.is_null(userId,password,orderFormId,content)) {
			ApiUtils.json(response, "", "参数缺失", 1);
			return;
		}
		JSONArray additionalArray;
		try {
			additionalArray = JSON.parseArray(content);
		} catch (Exception e) {
			e.printStackTrace();
			ApiUtils.json(response, "", "参数格式错误", 1);
			return;
		}
		OrderForm order = orderFormService.getObjById(CommUtil.null2Long(orderFormId));
		if (order==null) {
			ApiUtils.json(response, "", "订单不存在", 1);
			return;
		}
		if (order.getOrder_status()!=50) {
			ApiUtils.json(response, "", "订单状态错误", 1);
			return;
		}
		if (order.getOrder_status()==50&&!CommUtil.null2String(order.getIsAdditional()).equals("0")) {
			ApiUtils.json(response, "", "该订单已经被追评", 1);
			return;
		}
		User user=ApiUtils.erifyUser(userId, password, userService);
		if (user==null) {
			ApiUtils.json(response, "", "密码错误", 1);
			return;
		}
		if (user.getId()!=order.getUser().getId()) {
			ApiUtils.json(response, "", "用户不匹配", 1);
			return;
		}
		
		List<GoodsCart> gcs = order.getGcs();
		for (GoodsCart goodsCart : gcs) {
			for (int i = 0; i < additionalArray.size(); i++) {
				JSONObject goodsEvaluate = additionalArray.getJSONObject(i);
				String goodsId = goodsEvaluate.getString("goodsId");
				String words = CommUtil.null2String(goodsEvaluate.getString("words"));
				if (CommUtil.null2String(words).length()>200) {
					ApiUtils.json(response, "", "字数不能超过200字", 1);
					return;
				}
				Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
				if (goods==null) {//商品不存在则跳过
					break;
				}
				if (goodsCart.getGoods().getId()!=goods.getId()) {
					continue;
				}
				String hql="select obj from AppraiseMessageEntity as obj where obj.goods.id = " + goodsId + " and obj.user.id = " + userId + " and obj.order.id = " + orderFormId;
				List<AppraiseMessageEntity> query = appraiseMessageService.query(hql, null, -1, -1);
				if (query.size()>0) {
					AssessingDiscourseEntity assessingDiscourse = query.get(0).getAssessingDiscourse();
					if ("".equals(CommUtil.null2String(assessingDiscourse.getAppendWord()))&&words.length()>0) {
						assessingDiscourse.setAppendTime(new Date());
						assessingDiscourse.setAppendWord(words);
						assessingDiscourseService.update(assessingDiscourse);
					}
				}
			}
		}
		order.setIsAdditional("1");//追评
		this.orderFormService.update(order);
		OrderFormLog ofl = new OrderFormLog();
		ofl.setAddTime(new Date());
		ofl.setLog_info("追评订单");
		ofl.setLog_user(SecurityUserHolder.getCurrentUser());
		ofl.setOf(order);
		this.orderFormLogService.save(ofl);
		ApiUtils.json(response, "", "追评成功", 0);
		return;
	}
	//修改店铺的评分
	private boolean updateStoreGrade(StoreScoreEntity storeScoreEntity,StoreEvaluteEntity storeEvaluteEntity){
		Float storeAverageScore = storeScoreEntity.getStoreAverageScore();
		Float storeExpressAverageScore = storeScoreEntity.getStoreExpressAverageScore();
		Integer storeEvalutePerNum = storeScoreEntity.getStoreEvalutePerNum();
		if (!(storeAverageScore>0&&storeAverageScore<=5)||!(storeExpressAverageScore>0&&storeExpressAverageScore<=5)) {
			storeScoreEntity = this.updateStoreScoreEntity(storeScoreEntity);
			storeAverageScore = storeScoreEntity.getStoreAverageScore();
			storeExpressAverageScore = storeScoreEntity.getStoreExpressAverageScore();
			storeEvalutePerNum = storeScoreEntity.getStoreEvalutePerNum();
		}
		Integer logisticsStartsNum = storeEvaluteEntity.getLogisticsEvaluation().getStartsNum();
		Integer serviceStartsNum = storeEvaluteEntity.getServiceAttitude().getStartsNum();
		storeAverageScore=(storeAverageScore*storeEvalutePerNum+serviceStartsNum)/(storeEvalutePerNum+1);
		storeExpressAverageScore=(storeExpressAverageScore*storeEvalutePerNum+logisticsStartsNum)/(storeEvalutePerNum+1);
		storeScoreEntity.setStoreEvalutePerNum(storeEvalutePerNum+1);
		storeScoreEntity.setStoreAverageScore(CommUtil.formatDouble((storeAverageScore),2).floatValue());
		storeScoreEntity.setStoreExpressAverageScore(CommUtil.formatDouble((storeExpressAverageScore),2).floatValue());
		storeScoreEntity.setFiledExplainShow(this.gradeType(storeScoreEntity.getStoreAverageScore()));
		storeScoreEntity.setExpressSiledExplainShow(this.gradeType(storeScoreEntity.getStoreExpressAverageScore()));
		boolean update = this.storeScoreService.update(storeScoreEntity);
		return update;
	}
	//重新对店铺评价进行计算
	@SuppressWarnings("unchecked")
	private StoreScoreEntity updateStoreScoreEntity(StoreScoreEntity storeScoreEntity){
		String hql="select avg(obj.logisticsEvaluation.startsNum),avg(obj.serviceAttitude.startsNum),count(obj) from StoreEvaluteEntity as obj where obj.storeEvalute.id = " + storeScoreEntity.getStore().getId().toString();
		List<Object[]> query = this.commonService.query(hql, null, -1, -1);
		if (query.size()==0) {
			storeScoreEntity.setStoreAverageScore(5f);
			storeScoreEntity.setStoreExpressAverageScore(5f);
			storeScoreEntity.setStoreEvalutePerNum(0);
			storeScoreEntity.setFiledExplainShow("非常好");
			storeScoreEntity.setExpressSiledExplainShow("非常好");
		}else {
			int num = CommUtil.null2Int(query.get(0)[3]);
			storeScoreEntity.setStoreAverageScore(CommUtil.formatDouble((query.get(0)[1]),2).floatValue());
			storeScoreEntity.setStoreExpressAverageScore(CommUtil.formatDouble(query.get(0)[0], 2).floatValue());
			storeScoreEntity.setStoreEvalutePerNum(num);
		}
		this.storeScoreService.update(storeScoreEntity);
		return storeScoreEntity;
	}
	private String gradeType(float fraction){
		if (fraction>0&&fraction<=2) {
			return "差";
		}else if (fraction>2&&fraction<=3) {
			return "一般";
		}else if (fraction>3&&fraction<=4) {
			return "好";
		}else{
			return "非常好";
		}
	}
}
