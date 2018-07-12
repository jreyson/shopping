package com.shopping.api.output;

import java.io.Serializable;
import java.util.List;

import com.shopping.api.domain.materialCircle.LabelManage;
import com.shopping.api.domain.materialCircle.MaterialItems;

public class McTempOut implements Serializable{
	/**
	 * @author:akangah
	 * @description:输出app端的素材圈的首页数据
	 * @classType:中转类
	 */
	private static final long serialVersionUID = 1L;
	private List<MaterialItems> mcItems;
	private List<LabelManage> lb;
	public List<MaterialItems> getMcItems() {
		return mcItems;
	}
	public void setMcItems(List<MaterialItems> mcItems) {
		this.mcItems = mcItems;
	}
	public List<LabelManage> getLb() {
		return lb;
	}
	public void setLb(List<LabelManage> lb) {
		this.lb = lb;
	}
}
