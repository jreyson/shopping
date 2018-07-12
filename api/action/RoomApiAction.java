package com.shopping.api.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shopping.api.domain.RespApi;
import com.shopping.api.tools.ApiUtils;
import com.shopping.api.tools.QinJiaApiCall;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.service.ICommonService;
import com.shopping.foundation.service.IUserService;

@Controller
public class RoomApiAction {

	@Autowired
	IUserService userService;

	@Autowired
	ICommonService commonService;


	@RequestMapping({ "/create_room.htm" })
	public void create_room(HttpServletRequest request, HttpServletResponse response,String title,String user_id) throws Exception {
		if("".equals(CommUtil.null2String(user_id))){
			ApiUtils.json(response, "", "参数错误，该用户不存在", 1);
			return;
		}
		JSONObject obj = QinJiaApiCall.createRoom(title, user_id);
		RespApi resp = new RespApi();
		if(obj != null){
			JSONObject entity = obj.getJSONObject("entity");
			resp.setResult(entity);
			resp.setMsg("创建成功");
			resp.setStatus(0);
			ApiUtils.json(response, resp);
		}else{
			resp.setMsg("创建失败");
			resp.setStatus(1);
			ApiUtils.json(response, resp);
		}
	}
	
	@RequestMapping({ "/delete_room.htm" })
	public void delete_room(HttpServletRequest request, HttpServletResponse response,String roomId) throws Exception {
		JSONObject obj = QinJiaApiCall.delete_room(roomId);
		RespApi resp = new RespApi();
		if(obj != null){
			resp.setResult(obj);
			resp.setMsg("删除成功");
			resp.setStatus(0);
			ApiUtils.json(response, resp);
		}else{
			resp.setMsg("删除失败");
			resp.setStatus(1);
			ApiUtils.json(response, resp);
		}
	}
	@RequestMapping({ "/get_rooms.htm" })
	public void get_rooms(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject obj = QinJiaApiCall.getRooms();
		RespApi resp = new RespApi();
		if(obj != null){
			JSONArray entities = obj.getJSONArray("entities");
			resp.setResult(entities);
			resp.setMsg("查询成功");
			resp.setStatus(0);
			ApiUtils.json(response, resp);
		}else{
			resp.setMsg("查询失败");
			resp.setStatus(1);
			ApiUtils.json(response, resp);
		}
	}

}
