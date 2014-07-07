<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
String timeSpent=(String)request.getAttribute("timeSpent");
%>
<html>
	<body style="margin:0px; font-family: arial, helvetica, sans-serif; overflow:hidden;">
		<span id="timespent" style="font-size:11px">
			<%=timeSpent%>
		</span>
	</body>
</html>