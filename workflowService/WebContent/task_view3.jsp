<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/pageinit.jsp"%>
<%
String parentUrl=request.getParameter("parentUrl");
System.out.println("parentUrl="+parentUrl);

String currentProjectId="";

if(parentUrl!=null) {
	int indexOfP=parentUrl.indexOf("#");
	int indexOfS=parentUrl.indexOf("/",indexOfP+2);
	
	//System.out.println("indexOfP="+indexOfP+" indexOfS="+indexOfS);
	currentProjectId=parentUrl.substring(indexOfP+2,indexOfS);
}

System.out.println("currentProjectId="+currentProjectId);
   
%>
<script>
	var projectSel;
	var iterationSel;
	var taskTable;
	var processStatus;
	var queryButton;
	var exportButton;
	var tableResult;
	var timer;
	var currentProjectId="<%=currentProjectId%>";
	
	var iconPlus="<img src=\"images/icon_plus.gif\">";
	var iconMinus="<img src=\"images/icon_minus.gif\">";
	var iconBlank="";
	
	function onLoadPage() {
		taskTable=$("#taskTable");
		projectSel=$("#projectSel");
		iterationSel=$("#iterationSel");
		processStatus=$("#processStatus");
		queryButton=$("#queryButton");
		exportButton=$("#exportButton");
		
		getProjectList();
		
		exportButton.attr("disabled", true);
	}
	
	function getProjectList() {
		var listData;
				
		jQuery.getJSON("/workflowService/at/webservice/1.0/getProjectList",
			function(jsonData) {
				var optionStr;
				$.each(jsonData, function(key, val) {
					var selected=(currentProjectId==val.objId) ? "selected" : "";
					optionStr+="<option value='"+val.objId+"' "+selected+">"+val.name+"</option>";
				  });
				projectSel.html(optionStr);
				
				var projectId=$("#projectSel").val();
				if(projectId) {
					getIterationList(projectId);
				}
				
			
		});
	

	
	}
	
	function getIterationList(projectId) {
		var listData;
		
		jQuery.getJSON("/workflowService/at/webservice/1.0/"+projectId+"/getIterationList",
			function(jsonData) {
				var optionStr;
				$.each(jsonData, function(key, val) {
					var selected=(val.isCurrent=="true") ? "selected" : "";
					optionStr+="<option value='"+val.objId+"' "+selected+">"+val.name+"</option>";
				  });
				iterationSel.html(optionStr);
			
		});
	
	}
	
	function showProcessStatus() {
	
		$.get("/workflowService/at/webservice/1.0/getProcessStatus", {}, function(result) {
				processStatus.html("<img src='images/ajax-loader.gif'>"+result.msg);
				timer=setTimeout("showProcessStatus()", 1000);
				//console.info("showProcessStatus.."+timer);
		 });
	
	}
	
	function updateProcessStatus(msg) {
	
		console.info("updateProcessStatus msg="+msg);
		processStatus.html("<img src='images/ajax-loader.gif'>"+msg);
	
	}


	function getUserStoryList() {
		
		var iterationId=$("#iterationSel").val();
		
		processStatus.css({visibility:"visible"});
		queryButton.attr("disabled", true);
		exportButton.attr("disabled", true);
		 processStatus.html("<img src='images/ajax-loader.gif'>---%");
		showProcessStatus();

		//jQuery.getJSON("/workflowService/at/webservice/1.0/"+iterationId+"/getUserStoryList",
		$.ajax({
			url: "/workflowService/at/webservice/1.0/"+iterationId+"/getUserStoryList",
			dataType: "json",
			timeout: 300000, //300 second timeout
			success: function(jsonData) {
			
				processStatus.css({visibility:"hidden"});
				queryButton.attr("disabled", false);
				exportButton.attr("disabled", false);
			
				var tableStr="";
				
				tableStr+="<tr>";
				tableStr+="<th>Work Product</th>";
				tableStr+="<th>Name</th>";
				tableStr+="<th>State</th>";
				tableStr+="<th>Owner</th>";
				tableStr+="<th>Plan<br/>Estimate</th>";
				tableStr+="<th>Task<br/>Estimate</th>";
				tableStr+="<th>Task<br/>Remaining</th>";
				tableStr+="<th>Time<br/>Spent</th>";
				tableStr+="</tr>";
				
				tableResult=jsonData;

				$.each(jsonData, function(key, val) {
				
					var type=val.type;
					var name=(type=="task") ? "&nbsp;&nbsp;"+val.name : val.name;
					var formattedId=val.formattedId;
					var planEstimate=(val.planEstimate) ? val.planEstimate : "";
					var trClass=(type=="task") ? "taTr" : "usTr";
					
					if(type=="iteration") {
						formattedId="<span onclick='expandAll()'>"+iconPlus+"</span>&nbsp;&nbsp;";
						formattedId+="<span onclick='closeAll()'>"+iconMinus+"</span>";
					}
					
					var status=getStateStyle(type,val.taskStatus);
					
					if(name.length>90) {
						name=name.substr(0,90)+"...";
					}
					
					if(type=="task") {
						var usId=val.usId;
						tableStr+="<tr id='task_"+formattedId+"_"+usId+"' class='"+trClass+"' style='display:none'>";
						console.info("usId="+usId);
					} else {
						tableStr+="<tr class='"+trClass+"'>";
					}
				
					//tableStr+="<tr class='"+trClass+"'>";
					
					
					if(type=="userstory") {
						tableStr+="<td align='left'>";
						tableStr+="<span id='"+formattedId+"' onclick='expand(this.id)'>"+iconPlus+"</span> ";
						tableStr+=formattedId+"</td>";
					} else if(type=="task") {
						tableStr+="<td align='left' style='padding-left:30px'>";
						tableStr+=formattedId+"</td>";
					} else {
						tableStr+="<td align='left' >";
						tableStr+=formattedId+"</td>";
					}
					
					tableStr+="<td align='left'>"+name+"</td>";
					tableStr+="<td align='center'>"+status+"</td>";
					tableStr+="<td align='center'>"+val.owner+"</td>";
					tableStr+="<td align='right'>"+planEstimate+"</td>";
					tableStr+="<td align='right'>"+val.taskEstimateTotal+"</td>";
					tableStr+="<td align='right'>"+val.taskRemainingTotal+"</td>";
					tableStr+="<td align='right'>"+val.taskTimeSpentTotal+"</td>";
					tableStr+="</tr>";
					
					//if(type=="userstory") {
					//	tableStr+="</div>";
					//}
					
				  });
				
				taskTable.html(tableStr);
				clearTimeout(timer);
				timer=null;
				//console.info("clearTimeout.."+timer);
		}
		
		});
	
	}

	function getUserStoryListV2() {
		
		var iterationId=$("#iterationSel").val();
		
		processStatus.css({visibility:"visible"});
		queryButton.attr("disabled", true);
		exportButton.attr("disabled", true);
		processStatus.html("<img src='images/ajax-loader.gif'>---%");
		updateProcessStatus("processing...");
		
		form1.action="/workflowService/at/webservice/1.0/"+iterationId+"/getUserStoryList",
		form1.submit();
		
	}

	function showTableResult() {
			
		processStatus.css({visibility:"hidden"});
		queryButton.attr("disabled", false);
		exportButton.attr("disabled", false);
	
		var tableStr="";
		
		tableStr+="<tr>";
		tableStr+="<th>Work Product</th>";
		tableStr+="<th>Name</th>";
		tableStr+="<th>State</th>";
		tableStr+="<th>Owner</th>";
		tableStr+="<th>Plan<br/>Estimate</th>";
		tableStr+="<th>Task<br/>Estimate</th>";
		tableStr+="<th>Task<br/>Remaining</th>";
		tableStr+="<th>Time<br/>Spent</th>";
		tableStr+="</tr>";

		$.each(tableResult, function(key, val) {
		
			var type=val.type;
			var name=(type=="task") ? "&nbsp;&nbsp;"+val.name : val.name;
			var formattedId=val.formattedId;
			var planEstimate=(val.planEstimate) ? val.planEstimate : "";
			var trClass=(type=="task") ? "taTr" : "usTr";
			
			if(type=="iteration") {
				formattedId="<span onclick='expandAll()'>"+iconPlus+"</span>&nbsp;&nbsp;";
				formattedId+="<span onclick='closeAll()'>"+iconMinus+"</span>";
			}
			
			var status=getStateStyle(type,val.taskStatus);
			
			if(name.length>90) {
				name=name.substr(0,90)+"...";
			}
			
			if(type=="task") {
				var usId=val.usId;
				tableStr+="<tr id='task_"+formattedId+"_"+usId+"' class='"+trClass+"' style='display:none'>";
				console.info("usId="+usId);
			} else {
				tableStr+="<tr class='"+trClass+"'>";
			}
			
			if(type=="userstory") {
				tableStr+="<td align='left'>";
				tableStr+="<span id='"+formattedId+"' onclick='expand(this.id)'>"+iconPlus+"</span> ";
				tableStr+=formattedId+"</td>";
			} else if(type=="task") {
				tableStr+="<td align='left' style='padding-left:30px'>";
				tableStr+=formattedId+"</td>";
			} else {
				tableStr+="<td align='left' >";
				tableStr+=formattedId+"</td>";
			}
			
			tableStr+="<td align='left'>"+name+"</td>";
			tableStr+="<td align='center'>"+status+"</td>";
			tableStr+="<td align='center'>"+val.owner+"</td>";
			tableStr+="<td align='right'>"+planEstimate+"</td>";
			tableStr+="<td align='right'>"+val.taskEstimateTotal+"</td>";
			tableStr+="<td align='right'>"+val.taskRemainingTotal+"</td>";
			tableStr+="<td align='right'>"+val.taskTimeSpentTotal+"</td>";
			tableStr+="</tr>";
			
			//if(type=="userstory") {
			//	tableStr+="</div>";
			//}
			
		  });
		
		taskTable.html(tableStr);

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
     
     	return style;
     }

	function exportToCSV() {
	
		window.location="/workflowService/at/webservice/1.0/exportToCSV";
		
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
			<form name="form1" action="" method="get" target="hiddenFrame">
			<table width="100%" align="center">
				<tr>
					<td align="left" width="150">
						<img src="images/logo-with-tag-130x39.gif" />
					</td>
					<td align="center" width="350">
						User Story and Task Time Spent
					</td>
					<td align="left" valign="middle" width="640">
							Project:
							<select name="project" id="projectSel" onchange="getIterationList(this.value)">
								<option value="">Getting......</option>
								<!--
								<option value="4681584283">Workflow Engine</option>
								<option value="3874572825">Thermal Maps</option>
								<option value="5228438773">WSO2</option>
								-->
								<!--
								<option value="6169133135">dcTrack - RT Group</option>
								-->
								<!--
								<option value="6083311244">dcTrack</option>
								-->
							</select>
							<input type="button" value="Query" id="queryButton" onclick="getUserStoryListV2()"/>
							<input type="button" value="Export CSV" id="exportButton" onclick="exportToCSV()"/>
							<span id="processStatus" style="visibility:hidden"><img src="images/ajax-loader.gif">---%</span>
							<br/>
							Iteration:
							<select name="project" id="iterationSel">
								<option value="">Getting...</option>
							</select>
					</td>
					<!--
					<td align="left" valign="top" width="70">
						<span id="processStatus" style="visibility:hidden"><img src="images/ajax-loader.gif">---%</span>
					</td>
					-->
				</tr>
			</table>
			</form>
			<hr size="1">
			<table width="100%" align="center" class='taskTable' id='taskTable'>

			</table>
		</div>
		<iframe name="hiddenFrame" width="800" height="600" style="visibility:hidden"></iframe>
	</body>
</html>