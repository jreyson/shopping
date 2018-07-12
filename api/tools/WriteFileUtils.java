package com.shopping.api.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Specifi;
import com.shopping.foundation.service.ICommonService;

public class WriteFileUtils {
	//写入文件到服务器
	public static final boolean writeFile(String filePathName,CommonsMultipartFile file,float fileSize){
		DataOutputStream out=null;
		InputStream in=null;
		boolean ret=false;
		try{//FileOutputStream 文件输出流是用于将数据写入 File,创建一个内存中的file对象
			out=new DataOutputStream(new FileOutputStream(filePathName));//依据给定的流创建指定的输出流
			in = file.getInputStream();
			int size = (int) fileSize;
			byte[] buffer = new byte[size];
			while (in.read(buffer) > 0){
				out.write(buffer);
			}
			ret=true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret=false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret=false;
		}finally{
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
		return ret;
	}
	//生成图片
	public static final boolean writePhotoToServer(String srcImgPath,String distImgPath,
			int width,int height,boolean isScale){
		 Graphics2D graphics=null;
		 BufferedImage image = null;
		 BufferedImage buffImg = null;
		 boolean isWriteSuccess=false;
		 String subfix =srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1,srcImgPath.length());
		 File srcFile = new File(srcImgPath);
		 try{
			image = ImageIO.read(srcFile);
			if(!isScale){//这里判断是否要进行缩放
				width=image.getWidth();
				height=image.getHeight();
			}
			if(subfix.equals("png")) {
				buffImg = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
			}else{
				buffImg = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
			}
			graphics= buffImg.createGraphics();
			graphics.setBackground(Color.WHITE);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			graphics.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0,0, null);
			ImageIO.write(buffImg, subfix, new File(distImgPath));
			isWriteSuccess=true;
		} catch (IOException e1) {
			e1.printStackTrace();
			isWriteSuccess=false;
		}
		return isWriteSuccess;
	}
	//颜色<:>红色<,>尺码<:>L<,><->0<->10000<->100<->000900<_>颜色<:>红色<,>尺码<:>XL<,><->0<->10000<->100<->00011900<_>颜色<:>红色<,>尺码<:>XXL<,><->0<->10000<->100<->000000000900<_>
	public static final List<Specifi> specStrToList(String str, Goods goods,ICommonService commonService) {
		List<Specifi> spec_list = new ArrayList<Specifi>();
		if (!CommUtil.isNotNull(str)) {
			return spec_list;
		}
		String[] spec_price_arr = str.split("<_>");
		for (int i = 0; i < spec_price_arr.length; i++) {
			String[] arr1 = spec_price_arr[i].split("<->");
			Specifi spec = new Specifi();
			spec.setSpecifi(arr1[0]);//规格信息==><:>黑色<,>方法<:>ss<,>chima<:>g<,>ff方法<:>方法<,>
			spec.setInventory(Integer.parseInt(arr1[1]));//规格库存<->0<->
			if (CommUtil.null2Double(arr1[2])<=0) {//规格价钱s<->0<->
				spec.setPrice(CommUtil.null2Double(goods.getGoods_price()));
			}else {
				spec.setPrice(Double.parseDouble(arr1[2]));
			}
			if (CommUtil.null2Double(arr1[3])<=0) {//该规格对应的结算价钱
				spec.setSettlement_price(CommUtil.null2Double(goods.getSettlement_price()));
			}else {
				spec.setSettlement_price(Double.parseDouble(arr1[3]));
			}			
			spec.setNumber(arr1[4]);//货号==><->000000
			spec.setGoods(goods);//绑定商品
			commonService.save(spec);
			spec_list.add(spec);
		}
		return spec_list;
	}
	//判断视频格式
	public static String detectionFileFormat(String fileSuffix){
		String[] videoSuffix={"MOV","MP4"};
		String[] voiceSuffix={"MP3"};
		String[] pictureSuffix={"PNG","JPG","GIF"};
		if(Arrays.asList(videoSuffix).contains(fileSuffix)){
			return "videoFile";
		}else if(Arrays.asList(voiceSuffix).contains(fileSuffix)){
			return "voiceFile";
		}else if(Arrays.asList(pictureSuffix).contains(fileSuffix)){
			return "pictureFile";
		}else{
			return "";
		}
	}
	//判断商品是否已经上传过视频和音频
	public static  boolean judgeIsContainVv(Map<String,String> viewMap,Short flag){
		boolean isLawful=false;
		String keyFileName="";
		String fileType="";
		String extendName="";
		if(viewMap==null){
			isLawful=false;
		}else{
			Set<Entry<String, String>> mapEntry=viewMap.entrySet();
			Iterator<Entry<String, String>>  mapEntryIterator=mapEntry.iterator();
			while(mapEntryIterator.hasNext()){
				keyFileName=mapEntryIterator.next().getKey();
				extendName=keyFileName.substring(keyFileName.lastIndexOf(".") + 1).toUpperCase();
				fileType=WriteFileUtils.detectionFileFormat(extendName);
				if(flag==0){
					if("videoFile".equals(fileType)){
						isLawful=true;
					}
				}else{
					if("voiceFile".equals(fileType)){
						isLawful=true;
					}
				}
			}
		}
		return isLawful;
	}
	//生成封面图
	public static boolean generateSurfacePlot(String upFilePath,String mediaPicPath,String spec){
		boolean ret=false;
		List<String> cutpic = new ArrayList<String>(); 
		cutpic.add("ffmpeg.exe"); //视频提取工具的位置 
		cutpic.add("-i"); //添加参数＂-i＂，该参数紧跟要上传文件的路径 
		cutpic.add(upFilePath); // 视频文件路径 
		cutpic.add("-y"); 
		cutpic.add("-f"); 
		cutpic.add("image2"); 
		cutpic.add("-ss"); // 添加参数＂-ss＂，该参数紧跟要文件的截取时间 
		cutpic.add("1"); // 添加起始时间为第1秒 
		cutpic.add("-t"); // 添加参数＂-t＂，该参数指定持续时间 
		cutpic.add("0.001"); // 添加持续时间为1毫秒 
		cutpic.add("-s"); // 添加参数＂-s＂，该参数指定截取的图片大小 
		cutpic.add(spec); //添加截取的图片大小为800*600 
		cutpic.add(mediaPicPath); // 添加截取的图片的保存路径
	    ProcessBuilder builder = new ProcessBuilder();  
	    try{  
	         builder.command(cutpic);  
	         builder.redirectErrorStream(true);  
	         //如果此属性为 true，则任何由通过此对象的 start() 方法启动的后续子进程生成的错误输出都将与标准输出合并，  
	         //因此两者均可使用 Process.getInputStream() 方法读取。这使得关联错误消息和相应的输出变得更容易  
	         builder.start();  
	         ret=true;
	       }catch (Exception e) {  
	    	   ret=false;
	          System.out.println(e);  
	          e.printStackTrace();  
	     }
	    return ret;
	}	
}
