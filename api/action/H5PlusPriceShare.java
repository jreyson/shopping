package com.shopping.api.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.PlusPriceGoodsRecordEntity;
import com.shopping.api.domain.rank.UserRank;
import com.shopping.api.domain.rank.UserRankName;
import com.shopping.api.service.IPlusPriceGoodsRecordService;
import com.shopping.api.tools.ALIWapPayTools;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.CustomerFilter;
import com.shopping.api.tools.FilterObj;
import com.shopping.config.SystemResPath;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.FenPei;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.Transport;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IFenPeiService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.ITransportService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.swetake.util.Qrcode;
/***
 *@author:akangah
 *@description:newH5加价购买的控制器
 ***/
@Controller
public class H5PlusPriceShare {
	@Autowired
	private EvaluteFunction evaluteFunction;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IFenPeiService fenPeiService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IStoreCartService storeCartService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IPlusPriceGoodsRecordService plusPriceGoodsRecordService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IAccessoryService accessoryService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:这是二次开发的业务接口,为了兼容原有的业务,之前的接口保留,h5加价的入口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PlusPriceEntrance.htm"})
	public void h5PlusPriceEntrance(HttpServletRequest request,
			HttpServletResponse response,String userId,String goodsId,String photoUrl,
			String priceIncrement){
		if(ApiUtils.is_null(userId,goodsId,photoUrl,priceIncrement)){
			ApiUtils.json(response, "", "参数缺少,请上传正确的参数", 1);
			return;
		}
		User user=null;
		Goods goods=null;
		boolean ret=false;//photoUrl的格式为upload/appSavePlusGoodsPoster/1524897993366.jpg
		photoUrl=SystemResPath.imgUploadUrl+File.separator+photoUrl;
		String userPPRes="";//用户存放生成加价图片的文件夹路径
		String separator="/";//默认的分割符
		String codePhotoName=UUID.randomUUID().toString()+".png";//二维码照片名字
		String codeFilePath="";//存放生成二维码图片的文件夹路径
		String textContent="";//生成二维码的内容
		String userHeaderPath="";//用户头像路径
		String targetFileName=UUID.randomUUID().toString()+".png";//加价图片的名字
		String userFloder="";//用户文件夹
		String userTargetPath="";
		File photoRes=new File(photoUrl);//得到服务器上的文件
		Map<String,String> retMap=new HashMap<String, String>();
		if(photoRes.exists()&&photoRes.isFile()){
			user=this.userService.getObjById(CommUtil.null2Long(userId.trim()));
			if(user!=null){
				goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId.trim()));
				if(goods!=null){//先生成加价记录
					PlusPriceGoodsRecordEntity ppRecord=new PlusPriceGoodsRecordEntity();
					ppRecord.setAddTime(new Date());
					ppRecord.setDeleteStatus(false);
					ppRecord.setPlusAfterPrice(CommUtil.null2Double(priceIncrement));
					ppRecord.setPricegoods(goods);
					ppRecord.setPriceGoodsCount(1);
					ppRecord.setUser(user);
					ret=this.plusPriceGoodsRecordService.save(ppRecord);
					if(ret){//生成加价记录之后,接着生成二维码图片
						userFloder=separator+//创建用户加价文件夹
								  "userPPRes"+separator+userId+separator+goodsId;
						userPPRes=SystemResPath.imgUploadUrl+userFloder;
						ret=CommUtil.createFolder(userPPRes);//创建文件夹
						if(ret){
							codeFilePath=userPPRes+separator+codePhotoName;
							textContent=CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecord.getId();
							ret=this.generateQRCode(textContent, codeFilePath, 
									SystemResPath.imgUploadUrl+separator+"logoAndHeader"+separator+
										"ic_launcher1.png");
							if(ret){
								if(user.getPhoto()==null){
									userHeaderPath=SystemResPath.imgUploadUrl+separator+"logoAndHeader"+
													separator+"header.png";
								}else{
									userHeaderPath=SystemResPath.imgUploadUrl+separator+
												user.getPhoto().getPath()+separator+user.getPhoto().getName();
									if(!new File(userHeaderPath).exists()){//数据库存在，但是磁盘上面没有的话
										userHeaderPath=SystemResPath.imgUploadUrl+separator+"logoAndHeader"+
												separator+"header.png";
									}else{
										ret=CommUtil.createFolder(SystemResPath.imgUploadUrl+separator+"imgTempDispose");
										if(ret){
											userTargetPath=SystemResPath.imgUploadUrl+separator+
													"imgTempDispose"+separator+UUID.randomUUID().toString()+".png";
											if(this.setClip(120, userHeaderPath, userTargetPath)){
												userHeaderPath=userTargetPath;//生成圆形图片的路径赋值给要参与制作海报的路径
											}
										}
									}
								}//生成加价的照片
								ret=this.generateAndUpdateImage(photoUrl, userPPRes+separator+targetFileName, 
										userHeaderPath, user.getUsername(), goods.getGoods_name(), 
										"￥"+CommUtil.null2String(goods.getStore_price().doubleValue()+CommUtil.null2Double(priceIncrement)), 
										codeFilePath);
								if(ret){
									Accessory photo=new Accessory();
									photo.setWidth(132);
									photo.setHeight(132);
									photo.setAddTime(new Date());
									photo.setName(targetFileName);
									photo.setExt("png");
									photo.setPath(userFloder);
									photo.setUser(user);
									photo.setInfo("h5加价生成海报");
									photo.setDeleteStatus(false);
									photo.setSize(CommUtil.null2Float(this.evaluteFunction.acquireFileSize(userPPRes+separator+targetFileName)));
									ret=this.accessoryService.save(photo);
									if(ret){
										ppRecord.setPhotos(photo);
										ret=this.plusPriceGoodsRecordService.update(ppRecord);
										if(ret){
											if(new File(codeFilePath).delete()){
												if(!"".equals(userTargetPath)){
													new File(userTargetPath).delete();
												}
												retMap.put("imgURL", CommUtil.getURL(request)+photo.getPath()+"/"+photo.getName());
												retMap.put("h5PPRURL", textContent);
												ApiUtils.json(response, JSON.toJSONString(retMap), "海报生成成功", 0);
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}else{
			ApiUtils.json(response, "", "照片在服务器上不存在,请选择正确的商品照片", 1);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:展示详细的商品信息页面
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PPGoodsDetailShow.htm"})
	public ModelAndView h5PPGoodsDetailShow(HttpServletRequest request,
			HttpServletResponse response,String ppRecordId){
		Goods goods=null;
		ModelAndView mv=null;
		List<Accessory> goodsDetail=null;
		JSONObject jsonobj=new JSONObject();
		Map<String,List<String>> specMap=null;
		PlusPriceGoodsRecordEntity ppRecordEntity=null;
		List<Accessory> allGoodsPhoto=new ArrayList<Accessory>();//保存所有的商品图片
		if(ApiUtils.is_null(ppRecordId)){
			mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
					"参数错误请重试", CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecordId);
			return mv;
		}
		ppRecordEntity=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(ppRecordId.trim()));
		if(ppRecordEntity!=null){
			goods=ppRecordEntity.getPricegoods();
			if(goods!=null&&goods.getGoods_store()!=null){//为了防止商品被删除
				if(goods.getGoods_status()==1||goods.getGoods_inventory()==0||goods.getGoods_store().getStore_status()==3){
					mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
							"该商品已下架，不能购买", CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecordId);
				}else{
					mv=this.exceptionReturnH5(request, response, mv,
							 "h5ShareBuy/shareBuyDetail.html","", "");
					allGoodsPhoto.add(goods.getGoods_main_photo());//先添加主图
					goodsDetail=goods.getGoods_photos();//再添加详情图
					allGoodsPhoto.addAll(goodsDetail);//合并图片
					mv.addObject("goods", goods);
					mv.addObject("allGoodsPhoto", allGoodsPhoto);
					mv.addObject("goods_store",goods.getGoods_store());
					mv.addObject("ppRecordId", ppRecordId);
					specMap=this.specificationDispose(goods);
					if(specMap!=null){
						mv.addObject("specSet", this.disposeRenderingMap(specMap));
					}
					jsonobj.put("showSupplyPrice", goods.getStore_price().doubleValue()+ppRecordEntity.getPlusAfterPrice());
					jsonobj.put("suggestPrice", goods.getGoods_price().doubleValue()+ppRecordEntity.getPlusAfterPrice());
					mv.addObject("jsonobj", jsonobj);
				}
			}else{
				mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
						"该商品不存在", CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecordId);
				return mv;
			}
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:新增地址页面和搜索地址列表的入口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PlusBuyNewAddr.htm"})
	public ModelAndView h5PlusBuyNewAddr(HttpServletRequest request,
			HttpServletResponse response,String ppRecordId,
			String shoppingCount,String specInfo){
		ModelAndView mv=null;
		if(ApiUtils.is_null(ppRecordId,shoppingCount)){
			mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
					"参数错误请重试", CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecordId);
			return mv;
		}
		mv=this.exceptionReturnH5(request, response, mv,"h5ShareBuy/addAddress.html","", "");
		mv.addObject("ppRecordId", ppRecordId);
		mv.addObject("shoppingCount", shoppingCount);
		mv.addObject("specInfo", specInfo);
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:地址列表
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PPAddrList.htm"})
	public ModelAndView  h5PPAddrList(HttpServletRequest request,
			HttpServletResponse response,String ppRecordId,
			String shoppingCount,String specInfo,String ppSearchMobile){
		ModelAndView mv=null;
		String hql="";
		if(ApiUtils.is_null(ppRecordId,shoppingCount,ppSearchMobile)){
			mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
					"参数错误请重试", CommUtil.getURL(request)+
					"/h5PlusBuyNewAddr.htm?ppRecordId="+ppRecordId+
					"&shoppingCount="+shoppingCount+"&specInfo="+specInfo);
			return mv;
		}
		if(!"mobile".equals(ApiUtils.judgmentType(ppSearchMobile.trim()))){
			mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
					"手机号码不合法", CommUtil.getURL(request)+
					"/h5PlusBuyNewAddr.htm?ppRecordId="+ppRecordId+
					"&shoppingCount="+shoppingCount+"&specInfo="+specInfo);
			return mv;
		}
		hql="select obj from Address as obj where obj.mobile like'%"+ppSearchMobile+"%'";
		List<Address> userAddrList=this.addressService.query(hql, null, -1, -1);
		if(userAddrList.size()>0){
			mv=this.exceptionReturnH5(request, response, mv,"h5ShareBuy/addrList.html","", "");
			mv.addObject("userAddrList", userAddrList);
			mv.addObject("ppRecordId", ppRecordId);
			mv.addObject("shoppingCount", shoppingCount);
			mv.addObject("specInfo", specInfo);
		}else{
			mv=this.exceptionReturnH5(request, response, mv,"h5ShareBuy/noAddrShow.html","", "");
			mv.addObject("url", CommUtil.getURL(request)+"/h5PlusBuyNewAddr.htm?ppRecordId="+ppRecordId+
								"&shoppingCount="+shoppingCount+"&specInfo="+specInfo);
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:预创建的订单详情展示
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PPShowOrderDetail.htm"})
	public ModelAndView h5PPShownOrderDetail(HttpServletRequest request,
			HttpServletResponse response,String ppRecordId,
			String shoppingCount,String specInfo,String addrId){
		Double temp=0D;
		User user=null;
		Goods goods=null;
		Store store=null;
		Address addr=null;
		boolean ret=false;
		Double transFee=0.0D;
		ModelAndView mv=null;
		StoreCart storeCart=null;
		GoodsCart goodsCart=null;
		JSONObject transObj=null;
		List<JSONObject> transPortInfo=null;
		JSONObject jsonobj=new JSONObject();
		PlusPriceGoodsRecordEntity ppRecordEntity=null;
		BigDecimal totalPrice=BigDecimal.valueOf(0);
		if(ApiUtils.is_null(ppRecordId,shoppingCount,addrId)){
			mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
					"参数错误请重试", CommUtil.getURL(request)+"/h5PlusBuyNewAddr.htm?ppRecordId="+ppRecordId+
					"&shoppingCount="+shoppingCount+"&specInfo="+specInfo);
			return mv;
		}
		ppRecordEntity=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(ppRecordId.trim()));
		if(ppRecordEntity!=null){
			user=ppRecordEntity.getUser();
			goods=ppRecordEntity.getPricegoods();
			if(goods!=null){
				store=goods.getGoods_store();
				storeCart=this.generateStoreCart(goods, user);//生成店铺购物车
				ret=this.storeCartService.save(storeCart);//店铺购物车是商品购物车的关系被维护端，所以先保存
				if(ret){
					goodsCart=this.generateGoodsCart(specInfo, shoppingCount, goods, storeCart);
					ret=this.goodsCartService.save(goodsCart);//生成商品购物车
					if(ret){
						transPortInfo=this.parseTransPortInfo(store.getTransport_list());
						temp=goodsCart.getPrice().doubleValue()*goodsCart.getCount();//这里是商品购物车的价格
						if(transPortInfo!=null){
							transObj=transPortInfo.get(0);
							transFee=CommUtil.null2Double(transObj.get("trans_fee"));
						}
						totalPrice=BigDecimal.valueOf(CommUtil.formatDouble(temp, 2));
						storeCart.setTotal_price(totalPrice);
						ret=this.storeCartService.update(storeCart);
						if(ret){
							addr=this.addressService.getObjById(CommUtil.null2Long(addrId.trim()));
							mv=this.exceptionReturnH5(request, response, mv, "h5ShareBuy/orderConfirm.html","","");
							mv.addObject("addr", addr);
							mv.addObject("storeCart", storeCart);
							mv.addObject("goodsCart", goodsCart);
							mv.addObject("transPortInfo", transPortInfo);//快递信息的所有条
							mv.addObject("transObj", transObj);//默认快递信息的第一条
							mv.addObject("ppRecordId", ppRecordId);
							mv.addObject("addrId",addrId);
							mv.addObject("goodsCartId", goodsCart.getId());
							jsonobj.put("showSupplyPrice", goodsCart.getPrice().doubleValue()+ppRecordEntity.getPlusAfterPrice());//单价
							jsonobj.put("goodsAmount",CommUtil.null2Double(jsonobj.get("showSupplyPrice"))*goodsCart.getCount());
							jsonobj.put("totalPrice", CommUtil.null2Double(jsonobj.get("goodsAmount"))+transFee);
							mv.addObject("jsonobj", jsonobj);
							request.getSession(true).setAttribute("cancelURL",CommUtil.getURL(request)+
									"/h5PPShowOrderDetail.htm?ppRecordId="+ppRecordId+
									"&shoppingCount="+shoppingCount+"&specInfo="+specInfo+"&addrId="+addrId);
						}
					}
				}
			}else{
				mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
						"该商品为空，不能购买", CommUtil.getURL(request)+"/h5PlusBuyNewAddr.htm?ppRecordId="+ppRecordId+
						"&shoppingCount="+shoppingCount+"&specInfo="+specInfo);
			}
		}
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:微信和支付宝的支付接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PPRPay.htm"})
	public String h5PPRPay(HttpServletRequest request,HttpServletResponse response,
			String addrId,String ppRecordId,String goodsCartId,String transId,
			String totalPrice,String orderRemark,String storeCartId,String payFlag){
		boolean ret=false;
		double tranfee=0D;
		OrderForm of=null;
		FenPei fenPei=null;
		String forwardPath ="";
		Transport transport=null;
		StoreCart storeCart=null;
		GoodsCart goodsCart=null;
		OrderFormLog ordeLog=null;
		PlusPriceGoodsRecordEntity ppRecordEntity=null;
		if(ApiUtils.is_null(addrId,ppRecordId,goodsCartId,totalPrice)){
			return "forward:/goToErrorHtml.htm?ppRecordId="+ppRecordId+"&hintInfo=参数错误，请重试";
		}
		ppRecordEntity=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(ppRecordId));
		goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(goodsCartId));//判断视频金额是否相等
		transport=this.transportService.getObjById(CommUtil.null2Long(transId));
		storeCart=this.storeCartService.getObjById(CommUtil.null2Long(storeCartId));
		if(ppRecordEntity!=null){
			ret=this.checkoutMoneyIsEqual(transport, storeCart,
					CommUtil.null2Double(totalPrice),ppRecordEntity,goodsCart.getCount());//判断总价是否相等
			if(!ret){
				return "forward:/goToErrorHtml.htm?ppRecordId="+ppRecordId+"&hintInfo=价钱总额不合适，请重新选择";
			}
			of=new OrderForm();
			of.setAddTime(new Date());
			of.setDeleteStatus(false);
			of.setOrder_status(10);
			of.setUser(ppRecordEntity.getUser());//设置订单的所有者
			of.setMsg(CommUtil.null2String(orderRemark));//设置备注
			of.setAddr(this.addressService.getObjById(CommUtil.null2Long(addrId)));//设置地址
			of.setStore(storeCart.getStore());//设置订单所在的店铺
			of.setOrder_id(ppRecordEntity.getUser().getId()+CommUtil.formatTime("MMddHHmmss", new Date()));
			of.setInvoice("");
			of.setInvoiceType(0);
			of.setOrder_type("web_h5_pp");
			if(transport!=null){
				of.setTransport(transport.getTrans_name());//设置快递信息
				tranfee=CommUtil.null2Double(JSONObject.parseArray(transport.getTrans_express_info(),JSONObject.class).get(0).get("trans_fee"));
			}else{
				of.setTransport("全国包邮");//设置快递信息
			}
			int count=goodsCart.getCount();//商品购物车的数量
			double plusRange=CommUtil.null2Double(ppRecordEntity.getPlusAfterPrice())*count;//加价幅度
			double allMoney=CommUtil.null2Double(storeCart.getTotal_price());//商品总价
			double settlement=count*goodsCart.getSettlement_price();//得到结算价
			double changtuijin=count*goodsCart.getGoods().getCtj();//得到厂推金
			double zhanlue_price=count*goodsCart.getGoods().getZhanlue_price().doubleValue();//得到战略金
			fenPei= this.fenPeiService.getObjById(Long.valueOf(1).longValue());
			double base_gold=allMoney-settlement-changtuijin-zhanlue_price;
			of.setCtj(CommUtil.formatDouble(changtuijin,2));
			of.setZhanlue_price(CommUtil.formatDouble(zhanlue_price,2));
			of.setMaijia_get_price(CommUtil.formatDouble(settlement,2));
			of.setMaijia_tuijian_get_price(CommUtil.formatDouble(base_gold*fenPei.getMaijia_tuijian_get_price(),2));
			of.setDaogou_get_price(CommUtil.formatDouble(base_gold*fenPei.getDaogou_get_price(),2));
			of.setDaogou_tuijian_get_price(CommUtil.formatDouble(base_gold*fenPei.getDaogou_tuijian_get_price(),2));
			of.setZeng_gu_price(CommUtil.formatDouble(base_gold*fenPei.getZeng_gu_price(),2));
			of.setShui_wu_price(CommUtil.formatDouble(base_gold*fenPei.getShui_wu_price(),2));
			of.setChu_pei_price(CommUtil.formatDouble(base_gold*fenPei.getChu_pei_price(),2));
			of.setZhi_ji_price(CommUtil.formatDouble(base_gold*fenPei.getZhi_ji_price(),2));
			of.setXian_ji_price(CommUtil.formatDouble(base_gold*fenPei.getXian_ji_price(),2));
			of.setYang_lao_price(CommUtil.formatDouble(base_gold*fenPei.getYang_lao_price(),2));
			of.setFen_hong_price(CommUtil.formatDouble(base_gold*fenPei.getFen_hong_price(),2));
			of.setPlusRange_price(CommUtil.formatDouble(plusRange, 2));//生成海报后得到的加价的钱
			of.setShip_price(new BigDecimal(CommUtil.formatDouble(tranfee,2)));
			of.setGoods_amount(new BigDecimal(CommUtil.formatDouble(allMoney,2)));
			of.setTotalPrice(new BigDecimal(allMoney+plusRange+tranfee));
			ret=this.orderFormService.save(of);
			if(ret){
				goodsCart.setOf(of);
				ret=this.goodsCartService.update(goodsCart);
				if(ret){
					ordeLog=new OrderFormLog();
					ordeLog.setAddTime(new Date());
					ordeLog.setOf(of);
					ordeLog.setLog_info("产生订单");
					ordeLog.setLog_user(ppRecordEntity.getUser());
					ret=this.orderFormLogService.save(ordeLog);
					if(ret){//of.getTotalPrice()
						ret=this.judgeIsWeChatBrowser(request);
						if(ret){
							if(!"".equals(CommUtil.null2String(payFlag))){//在微信浏览器中选择支付宝支付
								forwardPath="redirect:/alipayWapGuidePage.htm?payTotal="+of.getTotalPrice()+"&orderId="+of.getId();
							}else{//在微信浏览器中选择公众号支付
								forwardPath="forward:/app_acquire_weiXinOpenId.htm?totalFee="+of.getTotalPrice()+"&orderId="+of.getOrder_id();
							}
						}else{
							if(!"".equals(CommUtil.null2String(payFlag))){//在浏览器中选择支付宝支付
								forwardPath="forward:/h5PPBALIWapPay.htm?payTotal="+of.getTotalPrice()+"&orderId="+of.getOrder_id();
							}else{//在浏览器中选择微信支付
								forwardPath="forward:/app_posterH5_weixinPayment.htm?totalFee="+of.getTotalPrice()+"&orderId="+of.getOrder_id();
							}
						}
					}
				}
			}
		}
		return forwardPath;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:判断规格是否存在
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/judgeSpecIsExist.htm"})
	public void judgeSpecIsExist(HttpServletRequest request,
			HttpServletResponse response,String specInfo,String ppRecordId){
		JSONObject jsonobj=new JSONObject();
		if(ApiUtils.is_null(specInfo,ppRecordId)){
			ApiUtils.json(response, "", "所需参数不能缺少", 2);
			return;
		}
		Map<String,Object> retMap=this.specIsExist(specInfo, ppRecordId);
		if(retMap.get("specifiStr")==null){
			ApiUtils.json(response, "", "该商品规格库存为0,不能购买,请重新选择", 1);
		}else{
			jsonobj.put("specifiMoney", retMap.get("specifiMoney"));
			jsonobj.put("specifiStr", retMap.get("specifiStr"));
			jsonobj.put("specPrice", retMap.get("specPrice"));//该规格的价钱
			ApiUtils.json(response,jsonobj.toJSONString(), "商品规格存在", 0);
		}
		return;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:h5新增地址
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/h5PPGenerateAddress.htm"})
	public void h5PPGenerateAddress(HttpServletRequest request,
			HttpServletResponse response,String consigneeName,
			String mobile,String districtId,String areaInfo){
		Area area=null;
		Address addr=null;
		boolean ret=false;
		String mobileIsLawful="";
		if(ApiUtils.is_null(consigneeName,mobile,districtId,areaInfo)){
			ApiUtils.json(response, "", "所需参数不能缺少", 1);
			return;
		}
		mobileIsLawful=ApiUtils.judgmentType(mobile);//判断手机号码是否合法
		if(!"mobile".equals(mobileIsLawful)){
			ApiUtils.json(response, "", "手机号码不合法", 1);
			return;
		}
		area=this.areaService.getObjById(CommUtil.null2Long(districtId.trim()));//判断区域参数是否合法
		if(area==null){
			ApiUtils.json(response, "", "区域Id不合法", 1);
			return;
		}
		addr=new Address();
		addr.setAddTime(new Date());
		addr.setArea(area);
		addr.setArea_info(areaInfo);
		addr.setMobile(mobile);
		addr.setTrueName(consigneeName);
		addr.setTelephone(mobile);
		addr.setDeleteStatus(false);
		ret=this.addressService.save(addr);
		if(ret){
			ApiUtils.json(response, addr.getId(), "地址新增成功", 0);
			return;
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:跳转到异常界面
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({"/goToErrorHtml.htm"})
	public ModelAndView goToErrorHtml(HttpServletRequest request,
			HttpServletResponse response,String ppRecordId,String hintInfo){
		ModelAndView mv=null;
		mv=this.exceptionReturnH5(request, response, mv,"h5Register/wapRegisterError.html",
				hintInfo,CommUtil.getURL(request)+"/h5PPGoodsDetailShow.htm?ppRecordId="+ppRecordId);
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:微信公众号支付成功后，跳转到成功界面
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping({ "/h5PPPaySuccess.htm" })
	public ModelAndView h5PPPaySuccess(HttpServletRequest request,
			HttpServletResponse response){
		ModelAndView mv = new JModelAndView("h5ShareBuy/h5PPPaySuccess.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		return mv;
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:支付宝支付的接口
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/h5PPBALIWapPay.htm")
	public void h5PPBALIWapPay(HttpServletRequest request,
			HttpServletResponse response,String orderId,String payTotal){
		JSONObject jsonobj=new JSONObject();
		jsonobj.put("out_trade_no", orderId);
		jsonobj.put("total_amount", payTotal);
		jsonobj.put("subject", "第一商城订单");
		jsonobj.put("product_code", "QUICK_WAP_PAY");
		PrintWriter printWriter=null;
		String retStr=ALIWapPayTools.aLIWapPay(request, jsonobj);
		if(!"".equals(CommUtil.null2String(retStr))){
			try{
				response.setContentType("text/html;charset=UTF-8");
				printWriter=response.getWriter();
				printWriter.write(retStr);//直接将完整的表单html输出到页面
				printWriter.flush();
				printWriter.close();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:支付宝h5支付的引导页
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/alipayWapGuidePage.htm")
	public ModelAndView alipayWapGuidePage(HttpServletRequest request,
			HttpServletResponse response,String payTotal,String orderId){
		ModelAndView mv=null;
		boolean ret=false;
		ret=this.judgeIsWeChatBrowser(request);
		if(ret){//如果是微信浏览器,直接走引导页
			mv= new JModelAndView("h5ShareBuy/alipayWapGuidePage.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 5, request, response);
			return mv;
		}else{
			if(ApiUtils.is_null(payTotal,orderId)){
				mv= new JModelAndView("h5ShareBuy/alipayTimeOut.html",
						this.configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 5, request, response);
				return mv;
			}else{
				this.h5PPBALIWapPay(request, response, CommUtil.null2String(orderId), CommUtil.null2String(payTotal));
			}
		}
		return mv;
	}
	//生成和更新图片
	private boolean generateAndUpdateImage(String srcFilePath,
			String targetFilePath,String headerFilePath,
			String userName,String goodsName,String goodsPrice,
			String codeFilePath){
		boolean ret=false;
		if(ApiUtils.is_null(srcFilePath,targetFilePath,headerFilePath,userName,goodsName,
				goodsPrice,codeFilePath)){
			return ret;
		}
		//画图片时即调用graphics.drawImage这个方法时.
		//1，要对图片进行缩放有俩种方案:codeImg.getScaledInstance(530, 530, Image.SCALE_SMOOTH),//缩放二维码到500*500
		//						100,1000, null);
		//2.graphics.drawImage(logoImg, // cWidth/2, cHeight/2是距离gs两个边的距离，45,45是中间logo的大小
		//				100,1000,45,45, null); 
		//3.graphics.drawImage(logoImg,45,45,null)//对图片进行裁剪,设定画布为固定尺寸,然后调用这个方法开始画，最后写到磁盘
		String subfix="";
		BufferedImage srcImage=null;//要生成的商品主图
		BufferedImage outputImg=null;//保存图片的文件image
		BufferedImage headerImg=null;//用户头像的文件
		BufferedImage codeImg=null;//二维码文件
		Graphics2D graphics=null;
		AttributedCharacterIterator attrTextIterator=null;
		int height=0;
		int width=0;
		int canvasW=1242;//定义画布的宽度
		int canvasH=2208;//定义画布的高度
		try{
			srcImage=ImageIO.read(new File(srcFilePath));
			headerImg=ImageIO.read(new File(headerFilePath));
			codeImg=ImageIO.read(new File(codeFilePath));
			subfix =srcFilePath.substring(
					srcFilePath.lastIndexOf(".") + 1,srcFilePath.length());
			height=headerImg.getHeight();
			width=headerImg.getWidth();
			if(subfix.equals("png")) {
				outputImg = new BufferedImage(canvasW, canvasH,
						 BufferedImage.TYPE_INT_ARGB);
			}else{
				outputImg = new BufferedImage(canvasW, canvasH,
							BufferedImage.TYPE_INT_RGB);
			}
			graphics= outputImg.createGraphics();
			graphics.setBackground(Color.WHITE);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, canvasW, canvasH);
			graphics.drawImage(srcImage.getScaledInstance(canvasW, canvasH/2, Image.SCALE_SMOOTH),
					0,0, null);
			graphics.drawImage(headerImg.getScaledInstance(height, width, Image.SCALE_SMOOTH), 
					80,canvasH/2+50, null);//这里是画出用户的头像，再画布一半的位置x:80 y:canvasH/2+50
			graphics.setColor(Color.GRAY);
			graphics.drawString(this.saveTextAttr(userName, attrTextIterator,65,"楷体"),width+170,canvasH/2+height/2+68);//这里画出用户的姓名离头像X方向50处开始画
			this.piantLongStr(goodsName, graphics, 80, canvasH/2+height+200, attrTextIterator);
			graphics.setColor(Color.red);//设置画笔颜色为红色
			graphics.drawString(this.saveTextAttr(goodsPrice, attrTextIterator,75,"黑体"),width/2+80,canvasH/2+height+730);
			graphics.drawImage(
					codeImg.getScaledInstance(530, 530, Image.SCALE_SMOOTH),//缩放二维码到500*500
					canvasW/2+30,canvasH/2+420, null);
			ImageIO.write(outputImg, subfix, new File(targetFilePath));
			ret=true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			ret=false;
		}
		return ret;
	}
	//将字符串的文本属性保存到AttributedString类型对象中
	private AttributedCharacterIterator saveTextAttr(String userName,
			AttributedCharacterIterator attrTextIterator,int fontSize,String fontType){
		Font font = new Font(fontType, Font.BOLD, fontSize);//Font 类表示字体，可以使用它以可见方式呈现文本
		AttributedString attrStr = new AttributedString(userName);//保存文本及相关属性信息
		attrStr.addAttribute(TextAttribute.FONT, font, 0,userName.length());
		attrTextIterator = attrStr.getIterator();
		return attrTextIterator;
	}
	//画出长的字符串
	private void piantLongStr(String goodsName,Graphics2D graphics,int strX,
			int strY,AttributedCharacterIterator attrTextIterator){
		String tempStr="";//临时字符串
		int strIndex=0;//字符串的索引值
		int increment=10;//增量
		if(goodsName.length()>50){
			goodsName=goodsName.substring(0,50);
		}
		for(int i=0;i<goodsName.length()/increment;i++){
			tempStr=goodsName.substring(strIndex, strIndex+increment);
			graphics.drawString(this.saveTextAttr(tempStr, attrTextIterator,45,"宋体"),strX,strY);
			strY=strY+85;//改变y坐标
			strIndex=strIndex+increment;//改变索引y坐标
		}
		if(goodsName.length()%increment!=0){
			tempStr=goodsName.substring(strIndex, goodsName.length());
			graphics.drawString(this.saveTextAttr(tempStr, attrTextIterator,45,"宋体"),strX,strY);
		}
	}
	//生成二维码图片
	private boolean generateQRCode(String content,
				String goalFilePath,String logoFilePath){
		boolean ret=false;
		if(ApiUtils.is_null(content,goalFilePath,logoFilePath)){
			return ret;
		}
		Qrcode qrcodeHandler= new Qrcode();
		BufferedImage bufImg=null;
		BufferedImage logoImg=null;
		Graphics2D graphics=null;
		int cWidth=0;//二维码的宽度
		int cHeight=0;//二维码的高度
		int logoWidth=0;//logo的宽度
		int logoHeight=0;//logo的高度
		int cVersion=18;//二维码的版本
		int pixoff = 2; //设置下偏移量,如果不加偏移量，有时会导致出错。  
	    qrcodeHandler.setQrcodeErrorCorrect('M');//纠错等级（分为L、M、H三个等级）  
	    qrcodeHandler.setQrcodeEncodeMode('B');//N代表数字，A代表a-Z，B代表其它字符  
	    qrcodeHandler.setQrcodeVersion(cVersion);//版本 
	    byte[] contentBytes=null;
	    try {
	    	contentBytes= content.getBytes("utf-8");//得到字符串的字节数组
	    	cWidth=67+12*(cVersion-1);//这里是二维码的宽度计算公式
	    	cHeight=67+12*(cVersion-1);//这里是二维码的高度计算公式
	    	bufImg= new BufferedImage(cWidth, cHeight, BufferedImage.TYPE_INT_ARGB);//创建png背景的画布
	    	graphics= bufImg.createGraphics();
	    	graphics.setBackground(Color.WHITE);//设置画笔的背景颜色
	    	graphics.clearRect(0, 0, cWidth, cHeight);//清除这个区域的画布内容
	    	graphics.setColor(Color.BLACK);//设置画笔 颜色为黑色
	    	if((contentBytes.length > 0) && (contentBytes.length < 150)) {
	            boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);
	            for (int i = 0; i < codeOut.length; i++) {
	              for (int j = 0; j < codeOut.length; j++)
	                if (codeOut[j][i])
	                	graphics.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
	            }
	          }else{
	            System.err.println(
	            		"QRCode content bytes length="+contentBytes.length + " not in [ 0,150 ]. ");
	        }
	    	logoImg = ImageIO.read(new File(logoFilePath));  // 实例化一个Image对象。
	    	logoWidth=logoImg.getWidth();
	    	logoHeight=logoImg.getHeight();
	    	graphics.drawImage(logoImg, // cWidth/2, cHeight/2是距离gs两个边的距离，45,45是中间logo的大小
	    			cWidth/2-logoWidth/2, cHeight/2-logoHeight/2,logoWidth,logoHeight, null);       
	    	ImageIO.write(bufImg, "png", new File(goalFilePath));
	    	ret=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret=false;
		}
	    return ret;
	}
	//将图片进行倒角处理
	public boolean setClip(int radius,String srcPath,String targetPath){
		boolean ret=false;
    	BufferedImage srcPathImage=null;
    	BufferedImage targetPathImage=null;
    	Graphics2D graphics2D=null;
		try {
			srcPathImage = ImageIO.read(new File(srcPath));
			int width =117; //srcImage.getWidth();
	        int height =117; //srcImage.getHeight();
	        targetPathImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	        graphics2D=targetPathImage.createGraphics();
	        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        graphics2D.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
	        graphics2D.drawImage(srcPathImage.getScaledInstance(width, width, Image.SCALE_SMOOTH), 0, 0, null);
	        graphics2D.dispose();
	        ImageIO.write(targetPathImage, "png", new File(targetPath));
	        ret=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ret;
    }
	//示例数据:  颜色<:>肤色<,>尺码<:>XXXl<,>大小<:>标码<,> ,颜色<:>黑色<,>尺码<:>Xl<,>大小<:>标码<,>
	private Map<String,List<String>> specificationDispose(Goods goods){
		String[] firArr=null;//创建以"<,>"符号切割之后的数组
		String[] secArr=null;
		Map<String,List<String>> specMap=null;
		if(goods!=null){
			List<Specifi> speifiList=goods.getSpeifi_list();//得到商品规格list
			if(speifiList.size()>0){
				specMap=new HashMap<String, List<String>>();//创建渲染商品规格的map集合
				for(Specifi specifi:speifiList){
					firArr=specifi.getSpecifi().split("<,>");//这里得出的是字符串数组
					for(String tempStr:firArr){
						secArr=tempStr.split("<:>");
						for(int i=0;i<secArr.length;i++){
							List<String> specList=new ArrayList<String>();//这里要每次创建全新的集合，防止引用传递造成的值相同问题
							specList.add(secArr[1]);//添加value到secArr数组中
							if(specMap.containsKey(secArr[0])){
								if(!specMap.get(secArr[0]).contains(secArr[1])){//判断原有集合中是否存在secArr[1],不存在就添加值
									specMap.get(secArr[0]).addAll(specList);
								}
							}else{
								specMap.put(secArr[0], specList);//初始化map集合数据
							}
							specList=null;
						}
					}
				}
			}
		}
		return specMap;
	}
	//返回处理过的set集合视图
	private Set<Entry<String, List<String>>> disposeRenderingMap(Map<String,List<String>> specMap){
		Set<Entry<String, List<String>>> tempSet=new HashSet<Entry<String, List<String>>>();
		if(specMap!=null){
			tempSet=specMap.entrySet();
		}
//		for(Entry<String, List<String>> f:tempSet){
//			f.getKey();
//			f.getValue();
//		}
		return tempSet;
	}
	//依据指定的字符串创建相应的模板
	private ModelAndView exceptionReturnH5(HttpServletRequest request,
			HttpServletResponse response,ModelAndView mv,String path,String op_title,String url){
		mv=new JModelAndView(path,
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 5, request, response);
		mv.addObject("op_title", op_title);
		mv.addObject("url", url);
		return mv;
	}
	//判断某个商品的规格是否存在
	private Map<String,Object> specIsExist(String specInfo,String ppRecordId){
		Map<String,Object>  returnMap=new HashMap<String, Object>();
		String[] specInfoArr=null;
		String specifiStr="";
		boolean ret=false;
		boolean flag=false;
		Specifi specifi=null;
		PlusPriceGoodsRecordEntity ppRecordEntity=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(ppRecordId.trim()));
		if(ppRecordEntity!=null){
			Goods goods=ppRecordEntity.getPricegoods();
			if(goods!=null){
				List<Specifi> speifiList=goods.getSpeifi_list();
				if(speifiList.size()>0){
					specInfoArr=specInfo.split(",");
					Iterator<Specifi> speifiItor=speifiList.iterator();
					while(speifiItor.hasNext()){
						specifi=speifiItor.next();
						specifiStr=specifi.getSpecifi().toUpperCase();
						for(String temp:specInfoArr){
							ret=specifiStr.indexOf(temp.trim().toUpperCase())==-1;//是否能在字符串中查找到，找不到返回-1
							if(ret){//如果查不到
								break;
							}
						}
						if(!ret){//如果在字符串中查到，马上停止
							returnMap.put("specifiStr", specifiStr);
							returnMap.put("specifiMoney", specifi.getPrice()+ppRecordEntity.getPlusAfterPrice());
							returnMap.put("specPrice", specifi.getPrice());//规格的价钱
							break;
						}
					}
					if(!ret){
						flag=true;
					}else{
						flag=false;
					}
					returnMap.put("flag", flag);
					return returnMap;
				}
			}
		}
		return returnMap;
	}
	//生成商品购物车
	private StoreCart generateStoreCart(Goods goods,User user){
		StoreCart storeCart=new StoreCart();
		storeCart.setAddTime(new Date());
		storeCart.setStore(goods.getGoods_store());
		storeCart.setUser(user);
		storeCart.setSc_status(1);
		storeCart.setTotal_price(BigDecimal.valueOf(0));
		return storeCart;
	}
	//生成店铺购物车
	private GoodsCart generateGoodsCart(String specInfo,String shoppingCount,
			Goods goods,StoreCart storeCart){
		GoodsCart goodsCart=new GoodsCart();
		goodsCart.setAddTime(new Date());
		goodsCart.setSpec_info_key(specInfo);
		goodsCart.setCount(CommUtil.null2Int(shoppingCount));
		Specifi specifi = (Specifi) this.commonService.getByWhere("Specifi",
				"goods_id=" + goods.getId() + " and specifi='" + specInfo+ "'");
		specifi = specifi == null ? new Specifi() : specifi;
		goodsCart.setCount(CommUtil.null2Int(shoppingCount));
		goodsCart.setPrice(BigDecimal.valueOf(CommUtil.formatDouble(CommUtil.null2Double(specifi.getPrice() == 0 ? goods
						  .getStore_price() : specifi.getPrice()), 2)));
		goodsCart.setSettlement_price(CommUtil.formatDouble(specifi.getSettlement_price() == 0 ? goods
									 .getSettlement_price().doubleValue() : specifi.getSettlement_price(), 2));
		goodsCart.setGoods(goods);
		goodsCart.setSc(storeCart);
		goodsCart.setDeleteStatus(true);
		if(!"".equals(specInfo)){
			goodsCart.setSpec_info(this.parseSepc(specInfo));
		}
		return goodsCart;
	}
	//解析快递信息
	private List<JSONObject> parseTransPortInfo(List<Transport> transportList){
		List<JSONObject> retList=null;
		if(transportList!=null&&transportList.size()>0){
			retList=new ArrayList<JSONObject>();
			for(Transport temTSP:transportList){
				JSONObject obj=new JSONObject();
//				System.out.println(temTSP.getTrans_name());
				obj.put("trans_name", temTSP.getTrans_name());
//				System.out.println(temTSP.getTrans_express_info());
				obj.put("city_name",JSONObject.parseArray(temTSP.getTrans_express_info(),JSONObject.class).get(0).get("city_name"));
				obj.put("trans_fee", JSONObject.parseArray(temTSP.getTrans_express_info(),JSONObject.class).get(0).get("trans_fee"));
				obj.put("id", temTSP.getId());
				retList.add(obj);
			}
		}
		return retList;
	}
	//解析规格
	private String parseSepc(String specInfo){
		specInfo=CommUtil.null2String(specInfo);
		if(!"".equals(specInfo)){
			specInfo=specInfo.replace("<:>", ":").replace("<,>", ",");
		}
		return specInfo;
	}
	//检验价钱是否相等，是否被恶意篡改
	private boolean checkoutMoneyIsEqual(Transport transport,StoreCart storeCart,
			Double totalPrice,PlusPriceGoodsRecordEntity ppRecordEntity,int count){
		boolean ret=false;
		Double transFee=0D;
		Double allMoney=0D;
		Double plusRange=0.0D;
		if(storeCart!=null){
			plusRange=ppRecordEntity.getPlusAfterPrice()*count;
			allMoney=storeCart.getTotal_price().doubleValue()+plusRange;
			if(transport!=null){
				transFee=CommUtil.null2Double(JSONObject.parseArray(transport.getTrans_express_info(),
						JSONObject.class).get(0).get("trans_fee"));
				allMoney=CommUtil.formatDouble(transFee+allMoney, 2);
			}
			if(allMoney.doubleValue()==totalPrice.doubleValue()){
				ret=true;
			}
		}
		return ret;
	}
	//判断是否微信浏览器
	private boolean judgeIsWeChatBrowser(HttpServletRequest request){
		boolean ret=false;
		String userAgent = request.getHeader("user-agent").toLowerCase();
		if(userAgent.indexOf("micromessenger")>-1){//微信客户端
			ret=true;
		}
		return ret;
	}
}
