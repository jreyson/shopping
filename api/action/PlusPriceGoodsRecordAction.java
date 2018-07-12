package com.shopping.api.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.PlusPriceGoodsRecordEntity;
import com.shopping.api.service.IPlusPriceGoodsRecordService;
import com.shopping.api.tools.ApiUtils;
import com.shopping.config.SystemResPath;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.FileUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.FenPei;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.OrderFormLog;
import com.shopping.foundation.domain.Payment;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.Transport;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IFenPeiService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IOrderFormLogService;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPaymentService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.ITransportService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.pay.tools.PayTools;
import com.swetake.util.Qrcode;
/***
 *@author:akangah
 *@description:h5加价购买的控制器
 ***/
@Controller
public class PlusPriceGoodsRecordAction {
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private PayTools payTools;
	@Autowired
	private IFenPeiService fenPeiService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IStoreCartService storeCartService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private IPlusPriceGoodsRecordService plusPriceGoodsRecordService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICommonService commonService;
	@Autowired
	private IAccessoryService accessoryService;
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:在这里生成PlusPriceGoodsRecordEntity类的记录,返回给
	 *			      前台,让其生成带有PlusPriceGoodsRecordEntityId的二维码图片
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_generate_plusGoodsPoster.htm", method = RequestMethod.POST)
	public void app_generate_plusGoodsPoster(HttpServletRequest request,
			HttpServletResponse response){
		PlusPriceGoodsRecordEntity plusPriceGoodsRecord=new PlusPriceGoodsRecordEntity();
		plusPriceGoodsRecord.setAddTime(new Date());
		plusPriceGoodsRecord.setDeleteStatus(false);
		plusPriceGoodsRecord.setPhotos(null);
		plusPriceGoodsRecord.setUser(null);
		plusPriceGoodsRecord.setPlusAfterPrice(0.00);
		plusPriceGoodsRecord.setPricegoods(null);
		boolean ret=this.plusPriceGoodsRecordService.save(plusPriceGoodsRecord);
		if(ret){
			ApiUtils.json(response, plusPriceGoodsRecord.getId(), "海报生成成功", 0);
		}
	}
	/***
	 *@author:akangah
	 *@return:void
	 *@param:**
	 *@description:在这里生成加价海报,并且将图片路径存入数据库,后续再查找
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	@RequestMapping(value = "/app_update_plusGoodsPoster.htm", method = RequestMethod.POST)
	public void app_update_plusGoodsPoster(HttpServletRequest request,
			HttpServletResponse response,String plusGoodsPhotoContent,
			Long userId,Double plusAfterPrice,Long goodsId,Long plusPriceGoodsRecordId){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		PlusPriceGoodsRecordEntity plusPriceGoodsRecord=this.plusPriceGoodsRecordService.getObjById(plusPriceGoodsRecordId);
		User user=this.userService.getObjById(userId);
		Goods goods=this.goodsService.getObjById(goodsId);
		if(plusPriceGoodsRecord!=null){
			//String FilePath=request.getSession().getServletContext()
			String FilePath=SystemResPath.imgUploadUrl+"\\upload\\appSavePlusGoodsPoster";
			String image_type=".jpg";
			String plusGoodsPhotoName=System.currentTimeMillis()+"";
			//生成了一张图片
			try{
				if(user!=null&&goods!=null){
					CommUtil.saveImage(FilePath, image_type, plusGoodsPhotoContent,
							plusGoodsPhotoName);
					Accessory photo=new Accessory();
					photo.setWidth(132);
					photo.setHeight(132);
					photo.setAddTime(new Date());
					photo.setName(plusGoodsPhotoName+image_type);
					photo.setExt("jpg");
					photo.setPath("upload/appSavePlusGoodsPoster");
					this.accessoryService.save(photo);//将图片保存到数据库
					plusPriceGoodsRecord.setAddTime(new Date());
					plusPriceGoodsRecord.setDeleteStatus(false);
					plusPriceGoodsRecord.setPhotos(photo);
					plusPriceGoodsRecord.setUser(user);
					plusPriceGoodsRecord.setPlusAfterPrice(CommUtil.formatDouble(plusAfterPrice, 2));
					plusPriceGoodsRecord.setPricegoods(goods);
					boolean ret=this.plusPriceGoodsRecordService.update(plusPriceGoodsRecord);
					if(ret){
						ApiUtils.json(response, photo, "海报生成成功", 0);
						return;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@RequestMapping({"/appH5_add_address.htm"})
	public ModelAndView appH5_add_address(HttpServletRequest request,
			HttpServletResponse response,
			Long plusPriceGoodsRecordId){
		ModelAndView mv=null;
		PlusPriceGoodsRecordEntity plusPriceGoodsRecord=this.plusPriceGoodsRecordService.getObjById(plusPriceGoodsRecordId);
		if(plusPriceGoodsRecord!=null){
			mv = new JModelAndView("posterGenerate/appH5_addr_generate.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request, response);
			mv.addObject("userId", plusPriceGoodsRecord.getUser().getId());
			mv.addObject("plusPriceGoodsRecordId",plusPriceGoodsRecordId);
		}
		return mv;
	}
	@RequestMapping({"/appH5_choose_specification.htm"})
	public ModelAndView appH5_choose_specification(HttpServletRequest request,
			HttpServletResponse response,String plusPriceGoodsRecordId,
			String userId,String consigneeName,String contactMobile,
			String addressInfo,String remarksMessage){
		if("".equals(CommUtil.null2String(userId))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return null;
		}
		ModelAndView mv=null;
		Goods goods=null;
		Long goodsId=0L;
		List<Specifi> speifi_list=null;
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		PlusPriceGoodsRecordEntity plusPriceGoodsRecord=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(plusPriceGoodsRecordId));
		if(plusPriceGoodsRecord!=null){
			goods=plusPriceGoodsRecord.getPricegoods();
			speifi_list=goods.getSpeifi_list();
			goodsId=goods.getId();
		}
		if(user!=null){
			Address addr=new Address();
			addr.setAddTime(new Date());
			addr.setProvince("");
			addr.setCity("");
			addr.setCounty("");
			addr.setArea_info(addressInfo);
			addr.setTrueName(consigneeName);
			addr.setMobile(contactMobile);
			addr.setUser(user);
			boolean ret=this.addressService.save(addr);
			if(ret){
				mv = new JModelAndView("posterGenerate/appH5_choose_specification.html",
						this.configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request, response);
				mv.addObject("plusPriceGoodsRecordId", plusPriceGoodsRecordId);
				mv.addObject("remarksMessage", remarksMessage);//订单的备注
				mv.addObject("addrId", addr.getId());
				mv.addObject("speifi_list", speifi_list);
				mv.addObject("goodsId", goodsId);//方便查找商品的规格
				return mv;
			}
		}
		return mv;
	}
	@RequestMapping({"/appH5_online_payment.htm"})
	public ModelAndView appH5_online_payment(HttpServletRequest request,
			HttpServletResponse response,String orderId,String payType){
		ModelAndView mv=null;
		OrderForm of = this.orderFormService.getObjById(CommUtil
				.null2Long(orderId));
		if(of!=null){
			List<Payment> payments = new ArrayList<Payment>();
			Map<String, String> params = new HashMap<String, String>();
			params.put("mark", payType);
			params.put("type", "admin");
			payments =this.paymentService
						.query("select obj from Payment obj where obj.mark=:mark and obj.type=:type",
								params, -1, -1);
			of.setPayment(payments.get(0));
			boolean ret=this.orderFormService.update(of);
			if(ret){
				mv = new JModelAndView("posterGenerate/appH5_online_payment.html",
						this.configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request, response);
				mv.addObject("orderId", orderId);
				mv.addObject("payType", payType);
				mv.addObject("url", CommUtil.getURL(request));
				mv.addObject("payTools", this.payTools);
				mv.addObject("type", "goods");
				mv.addObject("paymentId", of.getPayment().getId());
			}
		}
		return mv;
	}
	@RequestMapping({"/appH5_choose_payment.htm"})
	public ModelAndView appH5_choose_payment(HttpServletRequest request,
			HttpServletResponse response,String speifiInfo,String priceGoodsCount,
			String remarksMessage,String goodsId,String addrId,
			String plusPriceGoodsRecordId){
		ModelAndView mv=null;
		PlusPriceGoodsRecordEntity plusPriceGoodsRecord=this.plusPriceGoodsRecordService.getObjById(CommUtil.null2Long(plusPriceGoodsRecordId));
		plusPriceGoodsRecord.setPriceGoodsCount(CommUtil.null2Int(priceGoodsCount));
		boolean ret=this.plusPriceGoodsRecordService.update(plusPriceGoodsRecord);
		if(ret){
			String userId=plusPriceGoodsRecord.getUser().getId()+"";
			String tuijian_id="";
			String plusRange=CommUtil.formatDouble(plusPriceGoodsRecord.getPlusAfterPrice(), 2)+"";
			String orderId=this.appH5_plus_buy(userId,goodsId, speifiInfo,tuijian_id, priceGoodsCount, addrId, remarksMessage, plusRange);
			mv = new JModelAndView("posterGenerate/appH5_choose_payment.html",
					this.configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request, response);
			mv.addObject("orderId", orderId);
		}
		return mv;
	}
	@RequestMapping({ "/appH5_weixin_payment.htm" })
	public ModelAndView appH5_weixin_payment(HttpServletRequest request,
			HttpServletResponse response, String url, String id) {
		ModelAndView mv = new JModelAndView("posterGenerate/appH5_weixin_payment.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if ("".equals(url)) {
			url = null;
		}
		mv.addObject("url", url);
		mv.addObject("id", id);
		return mv;
	}
	@RequestMapping({ "/appH5_weiXInPayment_success.htm" })
	public ModelAndView appH5_weiXInPayment_success(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("posterGenerate/appH5_weiXInPayment_success.html",
				this.configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}
	private String appH5_plus_buy(String userId,String goodsId,
			String speifiInfo,String tuijian_id,String priceGoodsCount,
			String addressId,String remarksMessage,String plusRange){
		String orderId=new String();
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		Goods goods=this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		StoreCart storeCart=new StoreCart();
		GoodsCart goodsCart=new GoodsCart();
		storeCart=this.generateStoreCart(storeCart,goods,user);
		storeCart.setSc_status(1);
		boolean ret1=this.storeCartService.save(storeCart);
		goodsCart=this.generateGoodsCart(tuijian_id, goodsCart, 
				speifiInfo, priceGoodsCount, goods, storeCart);
		goodsCart.setDeleteStatus(true);
		boolean ret=this.goodsCartService.save(goodsCart);
		if(ret&&ret1){
			JSONObject params=new JSONObject();
			params.put("goodsCartId", goodsCart.getId());
			params.put("storeCartId", storeCart.getId());
			params.put("addressId", addressId);
			params.put("storeId", goods.getGoods_store().getId());
			params.put("remarksMessage", remarksMessage);//订单信息
			params.put("plusRange", plusRange);//加价幅度
			orderId=this.appH5_generate_order(params, userId);
		}
		return orderId;
	}
	private StoreCart generateStoreCart(StoreCart storeCart,Goods goods,User user){
		storeCart.setAddTime(new Date());
		storeCart.setStore(goods.getGoods_store());
		storeCart.setUser(user);
		return storeCart;
	}
	private GoodsCart generateGoodsCart(String tuijian_id,GoodsCart goodsCart,String gsp,String count,
			Goods goods,StoreCart storeCart){
		goodsCart.setAddTime(new Date());
		goodsCart.setSpec_info_key(gsp);
		goodsCart.setCount(CommUtil.null2Int(count));
		Specifi specifi = (Specifi) commonService.getByWhere("Specifi",
				"goods_id=" + goods.getId() + " and specifi='" + gsp
						+ "'");
		//goodsCart.setSpecification(specifi);
		specifi = specifi == null ? new Specifi() : specifi;
		goodsCart.setCount(CommUtil.null2Int(count));
		goodsCart.setPrice(BigDecimal.valueOf(CommUtil
				.null2Double(specifi.getPrice() == 0 ? goods
						.getStore_price() : specifi.getPrice())));
		goodsCart.setSettlement_price(specifi
				.getSettlement_price() == 0 ? goods
				.getSettlement_price().doubleValue() : specifi
				.getSettlement_price());
		goodsCart.setGoods(goods);
		goodsCart.setSc(storeCart);
		return goodsCart;
	}
	//[{"storeCartId":"8474","goodsCartIds":"966,782",msg:"哈哈","addsId":"9939","transId":"8737","totalPrice":"38477","storeId":"38883"}]
	private String  appH5_generate_order(JSONObject params,String userId){
		if("".equals(CommUtil.null2String(userId))){
			return "";
		}
		User user=this.userService.getObjById(CommUtil.null2Long(userId));
		OrderForm of=new OrderForm();
		of.setAddTime(new Date());
		of.setOrder_status(10);
		of.setUser(user);
		of.setMsg(params.getString("remarksMessage"));
		Address addr = this.addressService.getObjById(CommUtil
				.null2Long(params.getString("addressId")));
		of.setAddr(addr);
		Store store=this.storeService.getObjById(CommUtil
				.null2Long(params.getString("storeId")));
		String order_id=store.getId()
				+ CommUtil.formatTime("MMddHHmmss", new Date());
		of.setOrder_id(order_id);
		of.setStore(store);
		of.setOrder_type("web_h5_pp");
		of.setTransport(null);
		this.orderFormService.save(of);
		GoodsCart goodsCart=this.goodsCartService.getObjById(CommUtil.null2Long(params.getString("goodsCartId")));
		goodsCart.setOf(of);
		this.goodsCartService.update(goodsCart);
		
		double tranfee=0.0D;
		int count=goodsCart.getCount();
		double plusRange=CommUtil.null2Double(params.getString("plusRange"))*count;
		double price=goodsCart.getPrice().doubleValue();//得到店铺价
		double goodsCart_total_price=count*price;//商品购物车总价
		double settlement=count*goodsCart.getSettlement_price();//得到结算价
		double changtuijin=count*goodsCart.getGoods().getCtj();//得到厂推金
		double zhanlue_price=count*goodsCart.getGoods().getZhanlue_price().doubleValue();//得到战略金
		
		FenPei fenPei = this.fenPeiService.getObjById(Long.valueOf(1).longValue());
		double base_gold=goodsCart_total_price-settlement-changtuijin-zhanlue_price;
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
		of.setGoods_amount(new BigDecimal(CommUtil.formatDouble(goodsCart_total_price,2)));
		of.setTotalPrice(new BigDecimal(goodsCart_total_price+plusRange+tranfee));
		boolean ret=this.orderFormService.update(of);
		if(ret){
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setOf(of);
			ofl.setLog_info("产生订单");
			ofl.setLog_user(user);
			this.orderFormLogService.save(ofl);
		}
		return of.getId()+"";
	}
	public static void main(String[] args) {
		PlusPriceGoodsRecordAction recordAction=new PlusPriceGoodsRecordAction();
		recordAction.generateAndUpdateImage(
				"D:\\downLoad\\tmpRes\\writeImage\\a5.png",
				"D:\\downLoad\\tmpRes\\writeImage\\a5_test22.png", 
				"D:\\downLoad\\tmpRes\\writeImage\\header.png",
				"Spring",
				"低温脱水，纯绿色、无添加、无防腐剂的一款营养果蔬，含多种维生素，有紫薯、胡萝卜、青萝卜、土豆、",
				"￥40.80","D:/downLoad/tmpRes/writeImage/code.png");
		
//		String test = "abcdefghigk";
//		  int z = 0;
//		  for(int i=0;i<test.length()/3;i++){
//		   String a = test.substring(z,z+3);
//		   System.out.println(a);
//		   z = z+3;
//		  
//		  }
//		PlusPriceGoodsRecordAction recordAction=new PlusPriceGoodsRecordAction();
//		recordAction.generateQRCode("http://www.d1sc.com",
//				"D:/downLoad/tmpRes/writeImage/code.png",
//				"D:/downLoad/tmpRes/writeImage/ic_launcher1.png");
	}
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
}
