package com.shopping.api.domain;

import java.io.Serializable;

/**
 * @author Administrator
 *
 */
public class RespApi implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	private int status;
	private Object result;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	
}
