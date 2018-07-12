package com.shopping.api.tools;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.easyjf.beans.BeanUtils;
import com.easyjf.beans.BeanWrapper;
import com.shopping.core.annotation.Lock;
import com.shopping.core.tools.CommUtil;

public class FormAssemblyUtils {
	//<T>是一种形式，表示诉系统要用泛型编程，不受类型的约束
	//Class<T>这个则表示声明的是T的类型对象，Class<Goods> classType===>声明商品类型    Goods goods表示声明商品
	//Class<Goods> classType=Goods.class
	public static final <T> T assemblyForm(HttpServletRequest request, Class<T> classType,T existObj){
		String rKey="";
		String rVal="";
		Object obj=null;
		Enumeration<?> reqKeys=request.getParameterNames();
		Map<Object, Object> reqKVMap=new HashMap<Object, Object>();//创建key,value的集合
		try {
			while(reqKeys.hasMoreElements()){
				rKey=(String) reqKeys.nextElement();
				rVal=request.getParameter(rKey);
				reqKVMap.put(rKey, rVal);
			}
			if(existObj==null){//如果existObj为空,那么就创建新对象
				obj=classType.newInstance();//创建新goods对象
			}else{//不为空的话,不需要重新创建新对象
				obj=existObj;
			}
			FormAssemblyUtils.renderObjValue(obj, reqKVMap);//引用类型的传递,不需要检测返回值,因为已经修改了obj里面的东西,来自于同于引用
		} catch (Exception e) {
			// TODO: handle exception
		}
		return (T)obj;
	}
	public static final void renderObjValue(Object obj,Map<Object, Object> reqKVMap){
//		BeanWrapper(bean包装类)提供了设置和获取对应bean的属性值(单个的或者是批量的)获取属性描述信息、查询只读或者可写属性等功能*/
	    BeanWrapper wrapper = new  BeanWrapper(obj);
//	    PropertyDesciptor是(属性描述符的类)用于描述 Java Bean 通过一对存储器方法导出的一个属性,wrapper.getPropertyDescriptors返回一个PropertyDescriptor数组*/
	    PropertyDescriptor[] propertys = wrapper.getPropertyDescriptors();
	    String name="";
	    Object propertyValue = null;
	    Iterator<Object> keyIter=null;
	    String key="";
	    Lock lock = null;
	    Field filed=null;
	    for(int i = 0; i < propertys.length; i++){
		    //propertys[i].getName()得到该属性的名字
		    name = propertys[i].getName();
		    if (!wrapper.isWritableProperty(name)||propertys[i].getWriteMethod() == null){
		    	 continue;//如果属性是不可写的或者属性对应的set方法没有的话,则跳过
		    }
		    keyIter = reqKVMap.keySet().iterator();
		    while (keyIter.hasNext()){
		    	key = (String)keyIter.next();
		    	if(key.equals(CommUtil.null2String(propertys[i].getName()))){
//			    	1,在线程争夺资源不太激烈的时候,用synchronized性能较好,但是并发的厉害时,Lock的性能要优于synchronized, 而且要好几十倍,但是Lock锁的性能能保持常态
//			       	2,Lock不易产生死锁的情况必须要在
//			       	3,synchronized是在jvm层面上实现的,锁定的情况可以通过监测工具看到,发生异常时jvm会自动释放该锁, 而Lock是在代码层面实现的,会有异常产生,所以必须要在finally中手动的关闭该锁
//			    	propertys[i].getWriteMethod()返回一个Method实例,得到该方法的一些信息
//	             	propertys[i].getWriteMethod().getAnnotation(Lock.class)判断该方法中是否含有Lock类型的一些注释,如果有的话就返回注释,没有的话就返回null
//			        propertys[i].getWriteMethod().getDeclaringClass()得到包含该方法的Class对象,
//			        propertys[i].getWriteMethod().getDeclaringClass().getDeclaredField 返回一个Field类的实例(构造出Class对象指定属性的一个Field对象)
			    	lock = (Lock)propertys[i].getWriteMethod().getAnnotation(Lock.class);
			    	if(lock == null){//如果没有加锁,则要进去赋值装配
			    		try{
			    			filed = propertys[i].getWriteMethod().getDeclaringClass().getDeclaredField(name);
			                //类型转化,转化成Lock类型
			                lock = (Lock)filed.getAnnotation(Lock.class);
			                if(lock==null){
			                	propertyValue = BeanUtils.convertType(reqKVMap.get(key), propertys[i].getPropertyType());
			                }else{
			                	continue;
			                }
			    		}catch (Exception e) {
//			                e.printStackTrace();
			                if(propertys[i].getPropertyType().toString().equals("int")) {
			                        propertyValue = Integer.valueOf(0);
			                }
			                if(propertys[i].getPropertyType().toString().toLowerCase().indexOf("boolean") >= 0) {
			                        propertyValue = Boolean.valueOf(false);
			                }
			            }finally{
			            	 wrapper.setPropertyValue(propertys[i].getName(), propertyValue);
			            }
			    	}
		    	}
		    }
	    }
	}
}
