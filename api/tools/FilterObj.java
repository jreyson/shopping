package com.shopping.api.tools;

public class FilterObj {

	private Class clazz;
	private String str;
	
	public FilterObj() {
		super();
	} 
	public FilterObj(Class clazz, String str) {
		super();
		this.clazz = clazz;
		this.str = str;
	}
	public Class getClazz() {
		return clazz;
	}
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	
}
