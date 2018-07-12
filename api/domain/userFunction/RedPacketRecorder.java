package com.shopping.api.domain.userFunction;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;
import com.shopping.foundation.domain.User;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_redPacketRecorder")
public class RedPacketRecorder extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>红包领取记录
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne(fetch = FetchType.LAZY)
	private RedPacket redPacket;//红包
	@ManyToOne(fetch = FetchType.LAZY)
	private User receiveUser;//接收红包用户
	public RedPacketRecorder() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RedPacketRecorder(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public RedPacket getRedPacket() {
		return redPacket;
	}
	public void setRedPacket(RedPacket redPacket) {
		this.redPacket = redPacket;
	}
	public User getReceiveUser() {
		return receiveUser;
	}
	public void setReceiveUser(User receiveUser) {
		this.receiveUser = receiveUser;
	}
}
