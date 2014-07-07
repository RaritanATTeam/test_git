<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/pageinit.jsp"%>
<%@ include file="../common/check_session.jsp"%>
<%@ page import="javax.security.auth.login.*" %>
<%@ page import="org.ow2.bonita.light.*" %>
<%@ page import="org.ow2.bonita.facade.*" %>
<%@ page import="org.ow2.bonita.facade.runtime.*" %>
<%@ page import="org.ow2.bonita.facade.uuid.*" %>
<%@ page import="org.ow2.bonita.facade.identity.*" %>
<%@ page import="org.ow2.bonita.facade.paging.*" %>
<%@ page import="org.ow2.bonita.facade.def.majorElement.*" %>
<%@ page import="org.ow2.bonita.util.*" %>
<%

int pageNo=0;
int rowsPerPage=100;
int pageIndex=0;

String pageNStr=request.getParameter("pageNo");

if(pageNStr==null) {
	pageNo=1;
} else {
	pageNo=Integer.parseInt(pageNStr);
}

pageIndex=(pageNo-1) * rowsPerPage+1;

QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();

AuthUser user=(AuthUser)session.getAttribute(Constant.USER_BEAN);
String userName=user.getName();
String password=user.getPassword();

log.info("userName="+userName);
log.info("password="+password);

LoginContext loginContext=doLogin(userName,password);
	
Collection<TaskInstance> tasks = queryRuntimeAPI.getTaskList(ActivityState.READY);


List<LightProcessInstance> processList=
											queryRuntimeAPI.getLightProcessInstances(
											pageIndex,rowsPerPage,
											ProcessInstanceCriterion.INSTANCE_UUID_DESC );

StringBuilder html=new StringBuilder();


	
for(LightProcessInstance processInstance : processList) {
		
	ProcessInstanceUUID instanceUUID=processInstance.getProcessInstanceUUID();
	ProcessDefinitionUUID processDefinitionUUID=processInstance.getProcessDefinitionUUID();
	
	InstanceState instanceState=processInstance.getInstanceState();
	
	if(instanceState==InstanceState.STARTED) {
	
		ActivityInstanceUUID activityInstanceUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
		
		if(activityInstanceUUID!=null) {
			ActivityInstance activityInstance=queryRuntimeAPI.getActivityInstance(activityInstanceUUID);
			//log.info("activityInstance="+activityInstance);
			
			log.info("processList="+processList);
			
			String activityName=activityInstance.getActivityName();
			ActivityState activityState=activityInstance.getState();
			
			html.append("<tr>");
			html.append("<td>"+processDefinitionUUID+"</td>");
			html.append("<td><a href='view_activity.jsp?uuid="+instanceUUID+"'>"+instanceUUID+"</a></td>");
			html.append("<td><a href='view_activity.jsp?uuid="+instanceUUID+"'>"+activityName+"</a></td>");
			html.append("<td>"+activityState+"</td>");
			html.append("<td>"+instanceState+"</td>");
			html.append("</tr>");				
			
		}
	
	
	}
 
}

loginContext.logout();

log.info("loginContext.logout()...");
  
%>
<html>
	<head>
	</head>
	<body>
		<div class="canvas">
			<div align="left">
				<img src="images/logo-with-tag-130x39.gif" />
			</div>
			<div align="center" style="font-size:18px;">
				Process Instance List
			</div>
			<hr size="1">
			<table width=" 800" align="center" class='taskTable'>
				<tr>
					<th>Process Name</th>
					<th>Process Instance Name</th>
					<th>Process Activity Name</th>
					<th>Activity State</th>
					<th>Instance State</th>
				</tr>
				<%=html%>
			</table>
		</div>
	</body>
</html>