<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/common/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta charset="utf-8">
<!--[if lt IE 9]>
<script type="text/javascript" src="lib/html5.js"></script>
<script type="text/javascript" src="lib/respond.min.js"></script>
<script type="text/javascript" src="lib/PIE_IE678.js"></script>
<![endif]-->
<link href="newUI/css/H-ui.min.css" rel="stylesheet" type="text/css" />
<link href="newUI/css/H-ui.admin.css" rel="stylesheet" type="text/css" />
<link href="newUI/css/style.css" rel="stylesheet" type="text/css" />
<link href="newUI/lib/Hui-iconfont/1.0.6/iconfont.css" rel="stylesheet" type="text/css" />
<!-- <script type="text/javascript" src="newUI/lib/jquery/1.9.1/jquery.min.js"></script>   -->
<script type="text/javascript" src="newUI/lib/layer/2.1/layer.js"></script> 
<script type="text/javascript" src="newUI/lib/laypage/1.2/laypage.js"></script> 
<script type="text/javascript" src="newUI/lib/My97DatePicker/WdatePicker.js"></script> 
<script type="text/javascript" src="newUI/js/H-ui.js"></script> 
<script type="text/javascript" src="newUI/js/H-ui.admin.js"></script> 

<!--[if IE 6]>
<script type="text/javascript" src="lib/DD_belatedPNG_0.0.8a-min.js" ></script>
<script>DD_belatedPNG.fix('*');</script>
<![endif]-->
<title>维修列表</title>
<script type="text/javascript">


$(document).ready(function(){
	$.ajax({
		   type: "POST",
		   url: "jlDepartmentInfoAction_getDep",
		   async:false,
		   success: function(data1){
			  var str="";
			  var data = $.parseJSON(data1);
			  str="<option value='' >请选择部门</option>";
			  for(var i=0;i<data.length;i++){
				 str+="<option value='"+data[i].id+"' >"+data[i].name+"</option>";
			  }
			  $("#state").html(str);
			 
		   }
	});
});



function searchInfo(){
	list_iframe.contentWindow.load($('#datemin').val(),$('#datemax').val(),$('#username').val(),$('#state').val(),$("#examinestate").val());
}
function tolist(){
// 	form1.action="jlUserInfoAction_toList";
// 	form1.submit();
	list_iframe.contentWindow.load($('#datemin').val(),$('#datemax').val(),$('#username').val(),$('#state').val(),$("#examinestate").val());
}
function examineP(){
	list_iframe.contentWindow.selectData();
	
}
function examineShow(id){
	layer_show('审核','jlMaintainAction_examineMaintain?id='+id,'800','650');
}
</script>
</head>
<body>
<nav class="breadcrumb"><i class="Hui-iconfont">&#xe67f;</i> 首页 <span class="c-gray en">&gt;</span> 物资统计 <span class="c-gray en">&gt;</span> 报损维修统计审核<a class="btn btn-success radius r mr-20" style="line-height:1.6em;margin-top:3px" href="javascript:location.replace(location.href);" title="刷新" ><i class="Hui-iconfont">&#xe68f;</i></a></nav>
<div class="pd-20">
<form action="" name="form1" method="post"  id="form1">
 
	<div class="text-c"> 日期范围：
		<input type="text" onfocus="WdatePicker({maxDate:'#F{$dp.$D(\'datemax\')}'})" id="datemin" name="datemin" class="input-text Wdate" style="width:120px;">
		-
		<input type="text" onfocus="WdatePicker({minDate:'#F{$dp.$D(\'datemin\')}'})" id="datemax" name="datemax" class="input-text Wdate" style="width:120px;">
		<input type="text" class="input-text" style="width:250px" placeholder="申请人" id="username" name="username">
		<span class="select-box inline">
		<select class="select"  name="state" id="state" value=""  >
        </select>
        </span>
        <span class="select-box inline">
		<select class="select"  name="examinestate" id="examinestate" value=""  >
			<option value="" selected>审核状态</option>
			<option value="1">已申请</option>
			<option value="2">已审核</option>
        </select>
        </span>
		<button type="button"  class="btn btn-success" onclick="searchInfo();" id="" name=""><i class="Hui-iconfont">&#xe665;</i> 搜报损</button>
	</div>
	<div class="cl pd-5 bg-1 bk-gray mt-20">
	 <span class="l"><a style="display:none;" href="javascript:;" onclick="datadel()" class="btn btn-danger radius"><i class="Hui-iconfont">&#xe6e2;</i> 批量删除</a> <a style="color:white;" href="javascript:;" onclick="examineP()" class="btn btn-primary radius"><i class="Hui-iconfont">&#xe600;</i> 审核</a></span> <!-- <span class="r">共有数据：<strong>${count}</strong> 条</span> -->
	</div>
	</form>
		
<iframe id="list_iframe" name="list_frame" src="jlMaintainAction_toiframe" width="100%" height="80%" frameborder="0"></iframe>
</div>

</body>
</html>