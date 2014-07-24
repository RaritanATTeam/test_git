<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/pageinit.jsp"%>
<%@ include file="../common/check_session.jsp"%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.net.*" %>
<%@ page import="org.ow2.bonita.facade.*" %>
<%@ page import="org.ow2.bonita.facade.runtime.*" %>
<%@ page import="org.ow2.bonita.facade.uuid.*" %>
<%@ page import="org.ow2.bonita.facade.identity.*" %>
<%@ page import="org.ow2.bonita.facade.def.majorElement.*" %>
<%@ page import="org.ow2.bonita.util.*" %>
<%@ page import="org.apache.log4j.*" %>

<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.*" %>
<%@ page import="org.jdom.xpath.*" %>
	
<%!

List<Map> getForm(String fileName,String activityDefinitionUUID) throws Exception {

	List formList=new ArrayList();
	
	log.info("fileName="+fileName);

	org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
	org.jdom.Document doc = bSAX.build(new FileReader(fileName));
	Element root = doc.getRootElement();
	
	XPath xpath = XPath.newInstance("//form[@id='"+activityDefinitionUUID+"$entry']/pages/page/widgets/widget");
	
	List xlist = xpath.selectNodes(root);
	
	log.info("xlist="+xlist);
	
	Iterator iter = xlist.iterator();
	while(iter.hasNext()) {
		Map map=new HashMap();
	
		Element element = (Element) iter.next();
		
		log.info("element="+element);
		
		String name=element.getChildText("label");
		String variableBound=element.getChildText("variable-bound");
		//String field=element.getAttributeValue("id");
		String type=element.getAttributeValue("type");
		String readonly=element.getChildText("readonly");
		
		String field=null;
		
		if(variableBound!=null) {
			field=variableBound.substring(2,variableBound.length()-1);
		}
	
		map.put("name",name);
		map.put("field",field);
		map.put("type",type);
		map.put("readonly",readonly);
		
		formList.add(map);
	}
	
	log.info("formList="+formList);
	
	return formList;
}
%>
<%

String taskUUID=request.getParameter("uuid");

QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
QueryDefinitionAPI queryDefinitionAPI=AccessorUtil.getQueryDefinitionAPI();

AuthUser user=(AuthUser)session.getAttribute(Constant.USER_BEAN);
String userName=user.getName();
String password=user.getPassword();

log.info("userName="+userName);
log.info("password="+password);

String returnURL="https://rally1.rallydev.com/slm/tab/html.sp?projectScopeUp=false&projectScopeDown=false&_hash=%2Fcustom%2F5035793448&id=5035793448";

LoginContext loginContext=doLogin(userName,password);

StringBuffer html=new StringBuffer();

ProcessInstanceUUID instanceUUID=null;
Collection<TaskInstance> tasks=null;

String processName="";
String workflowLink="";

boolean instanceExists=true;

try {
	instanceUUID=new ProcessInstanceUUID(taskUUID);
	tasks=queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);
	
	if(tasks.size()==0) {
		Collection<TaskInstance> finishedTasks=queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.FINISHED);
		log.info("finishedTasks="+finishedTasks);
		if(finishedTasks.size()>0) {
		
			for(TaskInstance finishedTask:finishedTasks) {
			
				ProcessDefinitionUUID processDefinitionUUID=finishedTask.getProcessDefinitionUUID();
				
				ProcessDefinition processDefinition=queryDefinitionAPI.getProcess(finishedTask.getProcessDefinitionUUID());
				processName=processDefinition.getName();
				log.info("processName="+processName);
			
			}
		
			html.append("<table class='taskTable'>");
			html.append("<tr>");
			html.append("<td><a href='"+returnURL+"'>Back</a></td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td>"+instanceUUID+": <font color='red'>Task finished</font></td>");
			html.append("</tr>");
			html.append("</table>");
		}
	}
	
} catch(Exception e) {
	instanceExists=false;
	html.append("<table class='taskTable'>");
	html.append("<tr>");
	html.append("<td><a href='"+returnURL+"'>Back</a></td>");
	html.append("</tr>");
	html.append("<tr>");
	html.append("<td>"+instanceUUID+": <font color='red'>Instance not found</font></td>");
	html.append("</tr>");
	html.append("</table>");
}
	
//log.info("0 tasks="+tasks);

if(tasks!=null) {

	for(TaskInstance task: tasks) {
			
		ActivityState state=task.getState();
		
		ActivityInstanceUUID activityInstanceUUID=task.getUUID();
		log.info("activityInstanceUUID="+activityInstanceUUID);
		Map<String,Object> variables=queryRuntimeAPI.getVariables(activityInstanceUUID);
		log.info("getVariables="+variables);
		
		html.append("<input type='hidden' name='activityInstanceUUID' value='"+activityInstanceUUID+"' />");
		
		String activityName=task.getActivityName();
		
		ActivityDefinitionUUID activityDefinitionUUID=task.getActivityDefinitionUUID();
				
		html.append("<table class='mainTable'>");
		/*
		html.append("<tr>");
		html.append("<td>activityDefinitionUUID:<td>"+activityDefinitionUUID+"</td>");
		html.append("</tr>");
		*/
		
		html.append("<tr>");
		html.append("<td><a href='"+returnURL+"'>Back</a></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td>"+instanceUUID+" "+activityName+"</td>");
		html.append("</tr>");
		html.append("</table>");
		
		html.append("<br/>");
		
		log.info("getVariablesBeforeStarted="+task.getVariablesBeforeStarted());
		log.info("getType="+task.getType());
				
		html.append("<table width='500' class='taskTable'>");
		
		ProcessDefinitionUUID processDefinitionUUID=task.getProcessDefinitionUUID();
		
		ProcessDefinition processDefinition=queryDefinitionAPI.getProcess(task.getProcessDefinitionUUID());
		processName=processDefinition.getName();
		log.info("processName="+processName);
		
		String fileName=request.getRealPath("/definition/"+processDefinitionUUID.toString())+"/forms.xml";
		
		List<Map> formList=getForm(fileName,activityDefinitionUUID.toString());
		
		for(Map formMap:formList) {
			String formName=(String)formMap.get("name");
			String formType=(String)formMap.get("type");
			String formField=(String)formMap.get("field");
			
			String formValue=(String)variables.get(formField);
			if(formValue==null) {
				formValue="";
			}
			
			if("TEXT".equals(formType)) {
				html.append("<tr>");
				html.append("<td width='40%'>"+formName+"</td>");
				html.append("<td width='60%'>"+formValue+"</td>");
				html.append("</tr>");
			} else if("TEXTBOX".equals(formType)) {
				html.append("<tr>");
				html.append("<td width='40%'>"+formName+"</td>");
				html.append("<td width='60%'><input size='50' type='text' name='"+formField+"' value='"+formValue+"'></td>");
				html.append("</tr>");
			} else if("HIDDEN".equals(formType)) {
				html.append("<input type='hidden' name='"+formField+"' value='"+formValue+"'>");
			} else if("MESSAGE".equals(formType)) {
				html.append("<tr>");
				html.append("<td colspan='2'>");
				html.append(formValue+"</td>");
				html.append("</tr>");
			} else if("BUTTON_SUBMIT".equals(formType)) {
				html.append("<tr>");
				html.append("<td colspan='2' align='center'>");
				html.append("<input id='submitButton' type='submit' name='"+formField+"' value='"+formName+"'></td>");
				html.append("</tr>");
			} else if("RADIOBUTTON_GROUP".equals(formType)) {
			
				DataFieldDefinition dataFieldDefinition=null;
				
				try {
					dataFieldDefinition=queryDefinitionAPI.getProcessDataField(processDefinitionUUID,formField);
				} catch(Exception e) {
					log.info("Global field definition not found");
				}
				if(dataFieldDefinition==null) {
					dataFieldDefinition=queryDefinitionAPI.getActivityDataField(activityDefinitionUUID,formField);
				}
			
				if(dataFieldDefinition.isEnumeration()) {
				
					html.append("<tr>");
					html.append("<td width='40%'>"+formName+"</td>");
					html.append("<td width='60%'>");
					
					Set enumerationValues=dataFieldDefinition.getEnumerationValues();
				
					for(Iterator iter=enumerationValues.iterator();iter.hasNext();) {
				
						String value=(String)iter.next();
						html.append("<input type='radio' name='"+formField+" value='"+value+"' id='"+value+"'><label for='"+value+"'>"+value+"</label><br/>");
					}
					html.append("</td>");
					html.append("</tr>");
				
				}
				log.info("dataFieldDefinition.getInitialValue()="+dataFieldDefinition.getInitialValue());
			} 
			
			
			html.append("</tr>");
			
		}
	
	}
	
	html.append("<tr>");
	html.append("<td colspan='2' align='center'>");
	html.append("<div id='taskStatus'>Processing... <img src='images/ajax-loader.gif'></div>");
	html.append("</td>");
	html.append("</tr>");
	
	html.append("</table>");
}
	
loginContext.logout();
	
log.info("loginContext.logout()...");

String serverName=request.getServerName();
int serverPort=request.getServerPort();

String instanceURL="http://"+serverName+":"+serverPort+"/workflowService/view_activity.jsp?uuid="+taskUUID;
String instanceName=taskUUID;
String refURL=URLEncoder.encode(returnURL);
String rallyFormattedID="TAXXX";
String bpmnName=processName+".bpmn";
String pngName=processName+".png";
String toolTip="click to view details";

String reqInfo="{"+
						"\"instance_url\":\"\","+
						"\"instance_name\":\""+instanceName+"\","+
						"\"ref_url\":\"\","+
						"\"rally_formatted_id\":\"\","+
						"\"bpmn\":\""+bpmnName+"\","+
						"\"png\":\""+pngName+"\","+
						"\"tooltip\":\""+toolTip+"\""+
						"}";
		
if(instanceExists) {

	String showURL=(String)properties.get("imageserver.api.host");
	showURL=showURL.replace("${req}",reqInfo);

	workflowLink = showURL;
} else {
	workflowLink="";
}
										
log.info("workflowLink="+workflowLink);
						
%>
<html>
	<head>
	<script>
	
		function onLoad() {
			$("#taskStatus").hide();
		}
	
		function doSubmit() {
			
			var submitButton=$("#submitButton");
			submitButton.attr("disabled","");
			
			//.removeAttr('disabled');	
			var taskStatus=$("#taskStatus");
			taskStatus.show();
			
			return false;
		}
		
		function doForward() {
			location.reload();
		}
	
	</script>
	</head>
	
	<body onload="onLoad()">
		<div class="canvas">
			<form action="do_activity.jsp" target="hiddenFrame" onsubmit="doSubmit()">
				<%=html%>
			</form>
		</div>
		<div class="canvas">
			<iframe name="diagramFrame" src='<%=workflowLink%>' width="1100" height="500" frameborder="0"></iframe>
		</div>
		<iframe name="hiddenFrame" width="800" height="400" style="visibility:hidden"></iframe>
	</body>
	
</html>