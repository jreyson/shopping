package com.shopping.api.domain.motto;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.shopping.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "shopping_user_motto")
public class Motto extends IdEntity implements Serializable{

	/**
	 * @author:gaohao
	 * @description:app==>用户格言例句
	 */
	private static final long serialVersionUID = 1L;
	private String sentence;
	
	public Motto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Motto(Date addTime) {
		super(addTime);
		// TODO Auto-generated constructor stub
	}
	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	
}
