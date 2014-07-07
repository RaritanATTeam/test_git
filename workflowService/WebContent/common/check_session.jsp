<%@ page import="com.raritan.at.workflow.bean.*" %>
<%@ page import="com.raritan.at.workflow.service.Constant" %>
<%

if(session==null || session.getAttribute(Constant.USER_BEAN)==null) {
	response.sendRedirect("/workflowService/index.jsp");
	return;
}

%>