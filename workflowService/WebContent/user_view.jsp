<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/pageinit.jsp"%>
<script src="scripts/jquery.ui.ufd.js"></script>
<link href="css/ufd/ufd-base.css" rel="stylesheet" type="text/css" />
<!--  plain css skin  -->    
<link href="css/ufd/plain/plain.css" rel="stylesheet" type="text/css" />
<style>

img {
	vertical-align:middle;
}
</style>
<script>
	var userSel;
	var taskTable;
	var processStatus;
	var queryButton;
	var exportButton;
	var tableResult;
	var timer;
	
	var iconPlus="<img src=\"images/icon_plus.gif\">";
	var iconMinus="<img src=\"images/icon_minus.gif\">";
	var iconBlank="";
	
	function onLoadPage() {
		taskTable=$("#taskTable");
		userSel=$("#userSel");
		processStatus=$("#processStatus");
		queryButton=$("#queryButton");
		exportButton=$("#exportButton");

		getUserList();

		exportButton.attr("disabled", true);
				
		$("#startDate").datepicker(
			{ 
				altField: 'input#startDate', 
				altFormat: 'yy-mm-dd',
				rangeSelect: true,
				showOn: 'button', 
				buttonImageOnly: true,
				buttonImage: 'images/icon_calendar.gif'
			}
		);
		
		$("#endDate").datepicker(
			{ 
				altField: 'input#endDate', 
				altFormat: 'yy-mm-dd',
				showOn: 'button', 
				buttonImageOnly: true,
				buttonImage: 'images/icon_calendar.gif' 
			}
		);
		
	}
	
	function getUserList() {
		var listData;
		
		jQuery.getJSON("/workflowService/at/webservice/1.0/getUserList",
		
			function(jsonData) {
				var optionStr;
				optionStr+="<option value=''></option>"; // for first row
				$.each(jsonData, function(key, val) {
					optionStr+="<option value='"+val.userObjectId+"'>"+val.userName+"</option>";
				  });
				userSel.html(optionStr);
				
				
				userSel.ufd(
					{
						log:true,
						infix: false
					}
				);
			
		});
	
	}
	
	function showProcessStatus() {
	
		$.get("/workflowService/at/webservice/1.0/getProcessStatus", {}, function(result) {
				processStatus.html("<img src='images/ajax-loader.gif'>"+result.msg);
				//timer=setTimeout("showProcessStatus()", 1000);
				//console.info("showProcessStatus.."+timer);
		 });
	
	}

	function getUserStoryList() {
		
		var userObjectId=$("#userSel").val();
		var startDate=form1.startDate.value;
		var endDate=form1.endDate.value;
		
		processStatus.css({visibility:"visible"});
		queryButton.attr("disabled", true);
		exportButton.attr("disabled", true);
		 processStatus.html("<img src='images/ajax-loader.gif'>");
		//showProcessStatus();
		
		jQuery.getJSON("/workflowService/at/webservice/1.0/"+userObjectId+"/getUserTimeSpentByDate?startDate="+startDate+"&endDate="+endDate,
			function(jsonData) {
			
				processStatus.css({visibility:"hidden"});
				queryButton.attr("disabled", false);
				exportButton.attr("disabled", false);
			
				var tableStr="";
				
				tableStr+="<tr>";
				tableStr+="<th>Project</th>";
				tableStr+="<th>Iteration</th>";
				tableStr+="<th>Work Product</th>";
				tableStr+="<th>Name</th>";
				tableStr+="<th>State</th>";
				tableStr+="<th>Owner</th>";
				//tableStr+="<th>Plan<br/>Estimate</th>";
				tableStr+="<th>Task<br/>Estimate</th>";
				tableStr+="<th>Task<br/>Remaining</th>";
				tableStr+="<th>Time<br/>Spent</th>";
				tableStr+="</tr>";
				
				tableResult=jsonData;
				
				console.info("tableResult.."+tableResult);
				
				$.each(jsonData, function(key, val) {
				
					var projectName=val.projectName;
					var iterationName=val.iterationName;
					var usName=val.usName;
					var taskName=val.taskName ? val.taskFormattedId+" "+val.taskName : "";
					
					var status=getStateStyle("task",val.taskState);
					
					if(projectName) {
						if(projectName.length>20) {
							projectName=projectName.substr(0,20)+"...";
						}
					} else {
						projectName="";
					}
					
					if(iterationName) {
						if(iterationName.length>20) {
							iterationName=iterationName.substr(0,20)+"...";
						}
					} else {
						iterationName="";
					}
					
					if(usName) {
						if(usName.length>40) {
							usName=usName.substr(0,40)+"...";
						}
					} else {
						usName="";
					}
					
					if(taskName.length>60) {
						taskName=taskName.substr(0,60)+"...";
					}
					
					tableStr+="<tr class='taTr'>";
					tableStr+="<td align='left'>"+projectName+"</td>";
					tableStr+="<td align='left'>"+iterationName+"</td>";
					tableStr+="<td align='left'>"+usName+"</td>";
					tableStr+="<td align='left'>"+taskName+"</td>";
					tableStr+="<td align='center'>"+status+"</td>";
					tableStr+="<td align='center'>"+val.owner+"</td>";
					//tableStr+="<td align='right'>"+usPlanEstimate+"</td>";
					tableStr+="<td align='right'>"+val.taskEstimate+"</td>";
					tableStr+="<td align='right'>"+val.taskRemaining+"</td>";
					tableStr+="<td align='right'>"+val.hours+"</td>";
					tableStr+="</tr>";
					
				  });
				  
				console.info("tableStr.."+tableStr);
				
				taskTable.html(tableStr);
				//clearTimeout(timer);
				timer=null;
				//console.info("clearTimeout.."+timer);
		});
	
	}

     function getStateStyle(type,state) {
		console.log("getStateStyle type="+type+" state="+state);
     	if(type=="iteration")
     		return state;
     
     	var style="<center>";
     	
     	var styleD="state_off";
     	var styleP="state_off";
     	var styleC="state_off";
     	var styleA="state_off";
     	
     	if(state) {
			if(state.toUpperCase()=="DEFINED") {
				styleD="state_on";
			} else if(state.toUpperCase()=="IN-PROGRESS" || state.toUpperCase()=="IN_PROGRESS") {
				styleD="state_on";
				styleP="state_on";
			} if(state.toUpperCase()=="COMPLETED") {
				styleD="state_on";
				styleP="state_on";
				styleC="state_on";
			} else if(state.toUpperCase()=="ACCEPTED") {
				styleD="state_on";
				styleP="state_on";
				styleC="state_on";
				styleA="state_on";
			}
			
			style+="<span class='"+styleD+"'>D</span> ";
			style+="<span class='"+styleP+"'>P</span> ";
			style+="<span class='"+styleC+"'>C</span> ";
			
			if(type=="userstory") {
				style+="<span class='"+styleA+"'>A</span> ";
			}
			
			style+="</center>";
     	}
     
     	return style;
     }

	function exportToCSV() {
	
		window.location="/workflowService/at/webservice/1.0/exportToCSVForUserView";
		
	}
	
	function expand(id) {
		var iconSpan=$("#"+id);
		
		var html=iconSpan.html();
		
		//console.info("iconSpan="+html);
		//console.info("indexOf="+html.indexOf("icon_minus"));
		
		if((iconSpan.html().indexOf("icon_minus"))!=-1) {
			
			$("[id*=_"+id+"]").each(function(index){
				console.info("this="+this.id);
				$(this).hide();
			});
			$("#"+id).html(iconPlus);
			
		} else {
		
			$("[id*=_"+id+"]").each(function(index){
				console.info("this="+this.id);
				$(this).show();
			});
			$("#"+id).html(iconMinus);
		
			/*
			for (var i=0; i < tableResult.length; i++) {
				var m=tableResult[i];
				
				if(id==m.formattedId) {
				
					alert("id="+id+" m.formattedId="+m.formattedId);
				
					$("#task_"+m.formattedId).show();
					$("#"+id).html(iconMinus);
				}
			}
			*/

		}
		
	}
	
	function expandAll() {
		if(!tableResult) return;
		for (var i=0; i < tableResult.length; i++) {
			var m=tableResult[i];
			if(m.type=="task") {
				var div=$("#task_"+m.formattedId+"_"+m.usId).show();
			}
			if(m.type=="userstory") {
				$("#"+m.formattedId).html(iconMinus);
			}
		}
		
	}
	
	function closeAll() {
		if(!tableResult) return;
		for (var i=0; i < tableResult.length; i++) {
			var m=tableResult[i];
			if(m.type=="task") {
				var div=$("#task_"+m.formattedId+"_"+m.usId).hide();
			}
			if(m.type=="userstory") {
				$("#"+m.formattedId).html(iconPlus);
			}
		}
	}

</script>
<%


%>

<html>
	<head>
	</head>
	<body onload="onLoadPage()">
		<div class="canvas">
			<form name="form1" action="user_view.jsp" method="get">
			<table width="100%" align="center">
				<tr>
					<td align="left" width="150">
						<img src="images/logo-with-tag-130x39.gif" />
					</td>
					<td align="center" width="200">
						Member Time Spent
					</td>
					<td align="left" width="320">
						Date:
						<input type="text" name="startDate" id="startDate" size="11" readonly="true" /> ~ 
						<input type="text" name="endDate" id="endDate" size="11" readonly="true" />
					</td>
					<td align="right" width="400">
						User:
						<select name="user" id="userSel">
							<option value="">Getting, please wait...</option>
						</select>
						<input type="button" value="Query" id="queryButton" onclick="getUserStoryList()"/>
						<input type="button" value="Export CSV" id="exportButton" onclick="exportToCSV()"/>						
					</td>
					<td align="left" valign="top" width="70">
					
						<span id="processStatus" style="visibility:hidden"><img src="images/ajax-loader.gif"></span>
						
					</td>
				</tr>
			</table>
			</form>
			<hr size="1">
			<table width="100%" align="center" class='taskTable' id='taskTable'>

			</table>
			
		</div>
	</body>
</html>