<%@ page contentType="text/plain; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.net.*" %>
<%@ page import="com.raritan.at.workflow.api.*" %>
<%@ page import="org.apache.log4j.*" %>


<%!
	private Logger log = Logger.getLogger(this.getClass());
%>
<%

out.clear();

String action=request.getParameter("action");
String taskNo=request.getParameter("task_no");
String testCaseNo=request.getParameter("test_case_no");

	
String resultCode="";
String resultMessage="";

StringBuilder xml=new StringBuilder();

log.info("input parameter:");
log.info("action:"+action);
log.info("task_no:"+taskNo);
log.info("test_case_no:"+testCaseNo);

String configFile=request.getRealPath("/")+"/WEB-INF/conf/config.xml";
RallyAPI api=new RallyAPI(configFile);

if("checkTestCase".equals(action)) {

	//http://localhost:9080/workflowService/api2.jsp?action=checkTestCase&task_no=TA531&test_case_no=TC158
	
	api.checkRallyTestCase(testCaseNo,taskNo);
	
	resultCode=api.getResultCode();
	resultMessage=api.getResultMessage();
	
	if(!"0".equals(resultCode)) {
		resultCode=resultMessage;
	}

} else if("checkTestResult".equals(action)) {
	
	//http://localhost:9080/workflowService/api2.jsp?action=checkTestResult&task_no=TA531&test_case_no=TC158
	
	api.checkRallyTestResult(testCaseNo,taskNo);
	
	resultCode=api.getResultCode();
	resultMessage=api.getResultMessage();
	
	if(!"0".equals(resultCode)) {
		resultCode=resultMessage;
	}
	
}

log.info("resultCode="+resultCode);
log.info("resultMessage="+resultMessage);

%><%=resultCode%>