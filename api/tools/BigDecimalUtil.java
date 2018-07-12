package com.shopping.api.tools;

import java.math.BigDecimal;

public class BigDecimalUtil {

	private BigDecimalUtil() {

	}
	/***
	 *@return:BigDecimal
	 *@param:**
	 *@description:两数相加
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static BigDecimal add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2);
	}
	/***
	 *@return:BigDecimal
	 *@param:**
	 *@description:两数相减
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static BigDecimal sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2);
	}
	/***
	 *@return:BigDecimal
	 *@param:**
	 *@description:两数相乘
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static BigDecimal mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2);
	}
	/***
	 *@return:BigDecimal
	 *@param:**
	 *@description:两数相除
	 *@function:**
	 *@exception:*******
	 *@method_detail:***
	 *@variable:*******
	 ***/
	public static BigDecimal div(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP);// 四舍五入,保留3位小数
	}
}
