<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/pageinit.jsp"%>
<%@ include file="../common/check_session.jsp"%>
<%@ page import="javax.security.auth.login.*" %>
<%@ page import="org.ow2.bonita.facade.*" %>
<%@ page import="org.ow2.bonita.facade.runtime.*" %>
<%@ page import="org.ow2.bonita.facade.uuid.*" %>
<%@ page import="org.ow2.bonita.facade.identity.*" %>
<%@ page import="org.ow2.bonita.facade.def.majorElement.*" %>
<%@ page import="org.ow2.bonita.util.*" %>
<%

RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();

String uuid=request.getParameter("activityInstanceUUID");

String userName=(String)session.getAttribute("userName");
String password=(String)session.getAttribute("password");

log.info("userName="+userName);
log.info("password="+password);
log.info("uuid="+uuid);

LoginContext loginContext=doLogin(userName,password);
	
StringBuffer html=new StringBuffer();

ActivityInstanceUUID activityInstanceUUID=new ActivityInstanceUUID(uuid);

Enumeration parameterNames=request.getParameterNames();
while(parameterNames.hasMoreElements()) {
	String name=(String)parameterNames.nextElement();
	String value=request.getParameter(name);
	log.info("name="+name+" value="+value);
	if(!"activityInstanceUUID".equals(name)) {
		runtimeAPI.setVariable(activityInstanceUUID,name,value);
	}
}

//Verify the variables change
Map<String,Object> variables=queryRuntimeAPI.getVariables(activityInstanceUUID);
log.info("do_activity getVariables="+variables);

runtimeAPI.executeTask(activityInstanceUUID,true);
log.info("executeTask..finished");

loginContext.logout();

log.info("loginContext.logout()...");
  
%>
<script>
	parent.doForward();
</script>