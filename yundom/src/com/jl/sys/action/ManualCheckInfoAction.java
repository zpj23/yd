package com.jl.sys.action;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;



import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jl.common.IAction;
import com.jl.sys.pojo.CheckInfo;
import com.jl.sys.pojo.LogInfo;
import com.jl.sys.pojo.UserInfo;
import com.jl.sys.service.LogInfoService;
import com.jl.sys.service.ManualInfoService;
import com.jl.sys.service.UserInfoService;
import com.jl.util.DateHelper;
import com.jl.util.PingyinTool;
import com.jl.util.Role;

/**
 * @Description:微信考勤导入
 * @ClassName: ManualCheckInfoAction
 * @author zpj 
 * @date 2017-6-17 下午1:49:20
 *
 */
@Namespace("/")
@Scope("prototype")
@Component("jlManualCheckInfoAction")
@ParentPackage("json-default")
public class ManualCheckInfoAction extends IAction{
	
	Logger logger=Logger.getLogger(ManualCheckInfoAction.class);
	@Autowired
	public LogInfoService jlLogInfoService;
	
	@Autowired
	private UserInfoService jlUserInfoService;
	
	private CheckInfo cinfo;
	
	private UserInfo user;
	
	
	public CheckInfo getCinfo() {
		return cinfo;
	}
	public void setCinfo(CheckInfo cinfo) {
		this.cinfo = cinfo;
	}

	@Autowired
	private ManualInfoService mService;
	
	@Action(value="jlManualCheckInfoAction_toList",results={
			@Result(name="success",location="sys/manualcheck/list.jsp"),
			@Result(name="error",location="/login.jsp")
	})
	public String toList(){
		return "success";
	}
	@Action(value="jlManualCheckInfoAction_toAdd",results={
			@Result(name="success",location="sys/manualcheck/add.jsp"),
			@Result(name="error",location="/login.jsp")
	})
	public String toAdd(){
		user = getCurrentUser();
		String id=request.getParameter("id");
		if(id!=null&&!id.equalsIgnoreCase("")){
			cinfo=mService.findById(id);
		}else{
			cinfo=new CheckInfo();
			cinfo.setId(UUID.randomUUID().toString());
			cinfo.setDepartmentcode(user.getDepartmentcode());
		}
		return "success";
	}
	/**
	 * 批量插入 后台批量保存功能
	 * @Title doManageAdd
	 * @author zpj
	 * @time 2017-9-20 下午2:33:19
	 */
	@Action(value="jlManualCheckInfoAction_doManageAdd",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void doManageAdd(){
		user = getCurrentUser();
		String detailfields=request.getParameter("detailfileds");
		String workdate=request.getParameter("workdate");
		String departmentcode=request.getParameter("departmentcode");
		String departmentname=request.getParameter("departmentname");
		
		/************记录数据***开始*******/
		try{
			request.getMethod();
	    	Map map = request.getParameterMap();
	        String reqType=request.getMethod();
	        JSONArray json = JSONArray.fromObject(map);
			LogInfo loginfo=new LogInfo();
			loginfo.setId(UUID.randomUUID().toString());
			loginfo.setCreatetime(new Date());
			loginfo.setType("jlManualCheckInfoAction_doManageAdd");
			loginfo.setDescription(("操作类型：电脑端批量保存考勤信息, 请求类型："+reqType+", 数据："+json.toString()));
			loginfo.setUserid(user.getId());
			loginfo.setUsername(user.getUsername());
			jlLogInfoService.logInfo(loginfo);
		}catch (Exception e) {
			System.out.println("日志记录失败");
			e.printStackTrace();
		}
		/************记录数据***结束*******/
		
		
		JSONArray jsonArr = JSONArray.fromObject(detailfields); 
		for(int m=0;m<jsonArr.size();m++){
			try{
				net.sf.json.JSONObject job=(net.sf.json.JSONObject)jsonArr.get(m);
				CheckInfo tmpci=new CheckInfo();
				tmpci.setId(UUID.randomUUID().toString());
				tmpci.setStaffname(job.getString("staffname"));
				tmpci.setWorkdate(DateHelper.getDateFromString(workdate, "yyyy-MM-dd"));
//				if(((Integer)job.get("workduringtime"))!=0){
//					tmpci.setWorkduringtime(0);	
//				}else{
					tmpci.setWorkduringtime(job.getDouble("workduringtime"));
//				}
				tmpci.setDepartmentname(departmentname);
				tmpci.setDepartmentcode(departmentcode);
				tmpci.setWorkcontent(job.getString("workcontent"));
				tmpci.setAdddate(new Date());
//				if(((String)job.get("overtime")).equalsIgnoreCase("")){
//					tmpci.setOvertime(0);	
//				}else{
					tmpci.setOvertime(job.getDouble("overtime"));
//				}
				tmpci.setRemark(job.getString("remark"));
				tmpci.setCreateuserid(user.getId());
				tmpci.setSgxm(job.getString("sgxm"));
				tmpci.setSgqy(job.getString("sgqy"));
				if(user.getIsAdmin().equalsIgnoreCase(Role.ZUZHANG.toString())){
					//组长录入的状态是未审核
					tmpci.setShenhe("0");
				}else if(user.getIsAdmin().equalsIgnoreCase(Role.CHEJIANZHUREN.toString())){
					//车间主任 审核状态改成已审核
					tmpci.setShenhe("1");
					tmpci.setShenheTime(new Date());
					tmpci.setShenherenId(user.getId());
				}else if(user.getIsAdmin().equalsIgnoreCase(Role.RENSHI.toString())){
					//人事审核
					tmpci.setShenhe2("1");
					tmpci.setShenhe2Time(new Date());
					tmpci.setShenheren2Id(user.getId());
				}
				mService.saveInfo(tmpci);
			}catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
		try {
			JSONObject job=new JSONObject();
			job.put("status","y");
			job.put("statusText", "保存成功");
			job.put("readyState", "true");
			job.put("responseText", "true");
			this.jsonWrite(job);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 后台保存功能
	 * @Title doAdd
	 * @author zpj
	 * @time 2020年6月12日 下午3:05:12
	 */
	@Action(value="jlManualCheckInfoAction_doAdd",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void doAdd(){
		user = getCurrentUser();
		String staffnames=cinfo.getStaffname();
		cinfo.setAdddate(new Date());
		CheckInfo temp=mService.findById(cinfo.getId());
		boolean editFlag=false;
		if(null!=temp){
			//编辑状态
			editFlag=true;
		}
		
		/************记录数据***开始*******/
		try{
			request.getMethod();
	    	Map map = request.getParameterMap();
	        String reqType=request.getMethod();
	        JSONArray json = JSONArray.fromObject(map);
			LogInfo loginfo=new LogInfo();
			loginfo.setId(UUID.randomUUID().toString());
			loginfo.setCreatetime(new Date());
			loginfo.setType("jlManualCheckInfoAction_doAdd");
			loginfo.setDescription(("操作类型：电脑端保存考勤信息，"+editFlag+", 请求类型："+reqType+", 数据："+json.toString()));
			loginfo.setUserid(user.getId());
			loginfo.setUsername(user.getUsername());
			jlLogInfoService.logInfo(loginfo);
		}catch (Exception e) {
			System.out.println("日志记录失败");
			e.printStackTrace();
		}
		/************记录数据***结束*******/
		
		
		CheckInfo tmpci=null;
		//判断是否是多人同时输入的
		if(staffnames.contains("，")){
			String[] nameArr=staffnames.split("，");
			for(int i=0;i<nameArr.length;i++){
				tmpci=new CheckInfo();
				tmpci.setId(UUID.randomUUID().toString());
				tmpci.setStaffname(nameArr[i]);
				tmpci.setWorkdate(cinfo.getWorkdate());
				tmpci.setWorkduringtime(cinfo.getWorkduringtime());
				tmpci.setDepartmentname(cinfo.getDepartmentname());
				tmpci.setDepartmentcode(cinfo.getDepartmentcode());
				tmpci.setWorkcontent(cinfo.getWorkcontent());
				tmpci.setAdddate(cinfo.getAdddate());
				tmpci.setOvertime(cinfo.getOvertime());	
				tmpci.setRemark(cinfo.getRemark());
				tmpci.setSgxm(cinfo.getSgxm());
				tmpci.setSgqy(cinfo.getSgqy());
				if(editFlag){
					tmpci.setCreateuserid(temp.getCreateuserid());
					tmpci.setShenhe(temp.getShenhe());
				}else{
					tmpci.setCreateuserid(user.getId());
					
//					if(user.getIsAdmin().equalsIgnoreCase("1")){
//						//管理员  审核状态改成已审核
//						tmpci.setShenhe("1");
//					}else{
//						//普通人 录入的状态是未审核
//						tmpci.setShenhe("0");
//					}
					if(user.getIsAdmin().equalsIgnoreCase(Role.ZUZHANG.toString())){
						//组长录入的状态是未审核
						tmpci.setShenhe("0");
					}else if(user.getIsAdmin().equalsIgnoreCase(Role.CHEJIANZHUREN.toString())){
						//车间主任 审核状态改成已审核
						tmpci.setShenhe("1");
						tmpci.setShenheTime(new Date());
						tmpci.setShenherenId(user.getId());
					}else if(user.getIsAdmin().equalsIgnoreCase(Role.RENSHI.toString())){
						//人事审核
						tmpci.setShenhe2("1");
						tmpci.setShenhe2Time(new Date());
						tmpci.setShenheren2Id(user.getId());
					}
				}
				mService.saveInfo(tmpci);
			}
			if(editFlag){
				//编辑的时候 如果是有分隔符 说明是需要分割这条数据  ，在分割保存完这些数据以后要删除之前未分割的数据
				mService.delInfo(temp.getId());
			}
		}else{
			
			if(editFlag){
				cinfo.setCreateuserid(temp.getCreateuserid());
				cinfo.setShenhe(temp.getShenhe());
			}else{
				cinfo.setCreateuserid(user.getId());
				if(user.getIsAdmin().equalsIgnoreCase(Role.ZUZHANG.toString())){
					//组长录入的状态是未审核
					cinfo.setShenhe("0");
				}else if(user.getIsAdmin().equalsIgnoreCase(Role.CHEJIANZHUREN.toString())){
					//车间主任 审核状态改成已审核
					cinfo.setShenhe("1");
					cinfo.setShenheTime(new Date());
					cinfo.setShenherenId(user.getId());
				}else if(user.getIsAdmin().equalsIgnoreCase(Role.RENSHI.toString())){
					//人事审核
					cinfo.setShenhe2("1");
					cinfo.setShenhe2Time(new Date());
					cinfo.setShenheren2Id(user.getId());
				}
//				if(user.getIsAdmin().equalsIgnoreCase("1")){
//					//管理员  审核状态改成已审核
//					cinfo.setShenhe("1");
//				}else{
//					//普通人 录入的状态是未审核
//					cinfo.setShenhe("0");
//				}
			}
			mService.saveInfo(cinfo);
		}
		try {
			JSONObject job=new JSONObject();
			job.put("status","y");
			job.put("statusText", "保存成功");
			job.put("readyState", "true");
			job.put("responseText", "true");
			this.jsonWrite(job);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 管理员审核功能
	 * @Title shenhe
	 * @author zpj
	 * @time 2017-7-1 下午2:14:36
	 */
	@Action(value="jlManualCheckInfoAction_doshenhe",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void shenhe(){
		String id=request.getParameter("id");
		user = getCurrentUser();
		int r=mService.saveShenhe(id,user);
		try {
			this.jsonWrite(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Action(value="jlManualCheckInfoAction_del",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void del(){
		String id=request.getParameter("id");
		if(null!=id&&!id.equalsIgnoreCase("")){
			mService.delInfo(id);
			try {
				this.jsonWrite(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Action(value="jlManualCheckInfoAction_toiframe",results={
			@Result(name="zuzhang",location="sys/manualcheck/zuzhang/list_iframe.jsp"),
			@Result(name="chejianzhuren",location="sys/manualcheck/chejianzhuren/list_iframe.jsp"),
			@Result(name="renshi",location="sys/manualcheck/renshi/list_iframe.jsp"),
			@Result(name="admin",location="sys/manualcheck/list_iframe.jsp"),
			@Result(name="404",location="/404.jsp")
	})
	public String toiframe(){
		user=getCurrentUser();
		String role=user.getIsAdmin();
		if(role.contains(Role.ZUZHANG.toString())){
			return "zuzhang";
		}else if(role.contains(Role.CHEJIANZHUREN.toString())){
			return "chejianzhuren";
		}else if(role.contains(Role.RENSHI.toString())){
			return "renshi";
		}else if(role.contains(Role.ADMIN.toString())){
			return "admin";
		}
		return "404";
	}
	@Action(value="jlManualCheckInfoAction_managerIframe",results={
			@Result(name="success",location="sys/manualcheck/manager_iframe.jsp"),
			@Result(name="error",location="/login.jsp")
	})
	public String managerIframe(){
		return "success";
	}
	@Action(value="jlManualCheckInfoAction_toManagerAdd",results={
			@Result(name="success",location="sys/manualcheck/manager_add.jsp"),
			@Result(name="error",location="/login.jsp")
	})
	public String toManagerAdd(){
		return "success";
	}
	
	
	@Action(value="jlManualCheckInfoAction_getListJson",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void getListJson(){
		user = getCurrentUser();
		String datemin=request.getParameter("datemin");//开始时间
		String datemax=request.getParameter("datemax");//结束时间
		String username=request.getParameter("username");//用户名称
		String departmentid=request.getParameter("departmentid");//部门id
		String sgxm = request.getParameter("sgxm");//施工项目
		String sgqy=request.getParameter("sgqy");//施工区域
		String workcontent =request.getParameter("workcontent");//工作内容
		String shenhe=request.getParameter("shenhe");//审核状态
		String tpage=request.getParameter("page");
		String trows=request.getParameter("rows");
		String lrrname=request.getParameter("lrrname");//录入人
		String cfsj=request.getParameter("cfsj");//是否查询重复数据1为是，其他为否
		if(null!=tpage&&!"".equalsIgnoreCase(tpage)){
			page=Integer.parseInt(tpage);
		}
		if(null!=trows&&!"".equalsIgnoreCase(trows)){
			rows=Integer.parseInt(trows);
		}
		Map<String,String> param=new HashMap<String,String>();
		param.put("datemin", datemin);
		param.put("datemax", datemax);
		param.put("username", username);
		param.put("departmentid", departmentid);
		param.put("sgxm", sgxm);
		param.put("sgqy", sgqy);
		param.put("workcontent", workcontent);
		param.put("shenhe", shenhe);
		param.put("lrrname", lrrname);
		if(null!=cfsj&&!"".equalsIgnoreCase(cfsj)){
			param.put("cfsj", cfsj);
		}else{
			param.put("cfsj", "");
		}
		Map map=mService.findList(user,page,rows,param);
		List<UserInfo> list=(List<UserInfo>)map.get("list");
		int countNumber=(Integer)map.get("count");
		request.setAttribute("zgs", (Double)map.get("zgs"));
		if(list!=null&&list.size()>0){
			  StringBuffer str =new StringBuffer();
			  str.append("{\"total\":\"").append(countNumber).append("\",\"rows\":");
//			  JSONArray jsonArray = JSONArray.fromObject(list);
			  String lstr=gson.toJson(list);
			  str.append(lstr);
//			  if(user.getIsAdmin().equalsIgnoreCase("1")){
			  str.append(",\"footer\":[{\"id\":\"1\",\"departmentname\":\""+(Double)map.get("zgs")+"\",\"workdate\":\"\",\"workduringtime\":\""+(Double)map.get("wzgs")+"\",\"workcontent\":\"合计\",\"overtime\":\""+(Double)map.get("ozgs")+"\",\"staffname\":\"\",\"remark\":\"\"}]");
//			  }
			  str.append("}");
			  jsonData= str.toString();
		}else{
			StringBuffer str =new StringBuffer();
			str.append("{\"total\":0,\"rows\":");
			str.append("[]");
			str.append(",\"footer\":[{\"id\":\"1\",\"departmentname\":\"0\",\"workdate\":\"\",\"workduringtime\":\"0\",\"workcontent\":\"合计\",\"overtime\":\"0\",\"staffname\":\"\",\"remark\":\"\"}]");
			str.append("}");
			jsonData= str.toString();
		}
		try {
			this.jsonWrite(jsonData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Action(value="jlManualCheckInfoAction_exportExcel",results={
			@Result(name="success",type="json", params={"root","jsonData"})
	})
	public void exportExcel(){
		user = getCurrentUser();
		String datemin=request.getParameter("datemin");//开始时间
		String datemax=request.getParameter("datemax");//结束时间
		String username=request.getParameter("username");//用户名称
		String departmentid=request.getParameter("departmentid");//部门id
		String address = request.getParameter("address");//施工项目及区域
		String workcontent=request.getParameter("workcontent");//工作内容
		Map<String,String> param=new HashMap<String,String>();
		param.put("datemin", datemin);
		param.put("datemax", datemax);
		param.put("username", username);
		param.put("departmentid", departmentid);
		param.put("address", address);
		param.put("workcontent", workcontent);
		mService.exportExcel(param,request,response,user);
		
	}
	
	/**
	 * 根据用户展示一年根据月分的工时记录柱状图（分正常和加班两项）
	 * @Title toShowByType
	 * @return
	 * @author zpj
	 * @time 2017-7-12 下午4:18:44
	 */
	@Action(value="jlManualCheckInfoAction_toShowByType",results={
			@Result(name="success",location="sys/manualcheck/analysis.jsp"),//fashionHome.jsp
			@Result(name="error",location="/login.jsp")
	})
	public String toShowByType(){
		return "success";
	}
	
	@Action(value="jlManualCheckInfoAction_initChart",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void  jlDepartmentInfoAction_initDu(){
		String year=request.getParameter("datemin");//年份
		String yuefen=request.getParameter("yuefen");//月份
		String username=request.getParameter("username");//用户名称
		String departmentid=request.getParameter("departmentid");//部门id
		String sgxm = request.getParameter("sgxm");//施工项目
		String sgqy = request.getParameter("sgqy");//施工区域
		String workcontent=request.getParameter("workcontent");//工作内容
		Map<String,String> param=new HashMap<String,String>();
		param.put("datemin", year);
		param.put("username", username);
		param.put("departmentid", departmentid);
		param.put("sgxm", sgxm);
		param.put("sgqy", sgqy);
		param.put("workcontent", workcontent);
		param.put("yuefen", yuefen);
		List list=mService.findChartByUser(param);
		try {
			this.jsonWrite(list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化首页柱状图
	 * @Title initChartByArr
	 * @author zpj
	 * @time 2018-1-23 下午4:28:55
	 */
	@Action(value="jlManualCheckInfoAction_initChartByArr",
			results={
			@Result(type="json", params={"root","jsonData"})})
	public void  initChartByArr(){
		String datemin=request.getParameter("datemin");//开始时间
		String username=request.getParameter("username");//用户名称
		String departmentid=request.getParameter("departmentid");//部门id
		String departmentname=request.getParameter("departmentname");//部门名称
		String sgxm = request.getParameter("sgxm");//施工项目
		String sgqy = request.getParameter("sgqy");//施工区域
		String workcontent=request.getParameter("workcontent");//工作内容
		Map<String,String> param=new HashMap<String,String>();
		param.put("datemin", datemin);
		param.put("username", username);
		//param.put("departmentid", departmentid);
		param.put("sgxm", sgxm);
		param.put("sgqy", sgqy);
		param.put("workcontent", workcontent);
		String[] arr=departmentid.split(",");
		String[] names=departmentname.split(",");
		Map retMap=new HashMap();
		for(int i=0;i<arr.length;i++){
			param.put("departmentid", arr[i]);
			retMap.put(arr[i]+"|"+names[i], mService.findChartByUser(param));
		}
		try {
			this.jsonWrite(retMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
