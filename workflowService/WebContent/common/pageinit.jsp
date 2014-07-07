<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.net.*"%>
<%@ page import="javax.security.auth.login.*" %>
<%@ page import="org.apache.log4j.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.xpath.*"%>
<%@ page import="org.apache.commons.codec.binary.Base64"%>
<%@ page import="org.apache.http.auth.*" %>
<%@ page import="org.apache.http.client.*" %>
<%@ page import="org.apache.http.client.entity.*" %>
<%@ page import="org.apache.http.entity.*" %>
<%@ page import="org.apache.http.client.methods.*" %>
<%@ page import="org.apache.http.client.protocol.*" %>
<%@ page import="org.apache.http.impl.auth.*" %>
<%@ page import="org.apache.http.impl.client.*" %>
<%@ page import="org.apache.http.message.*" %>
<%@ page import="org.apache.http.params.*" %>
<%@ page import="org.apache.http.protocol.*" %>
<%@ page import="org.apache.http.*" %>
<%!
	Logger log = Logger.getLogger(this.getClass());
	
	public String getRallyXML(String apiUrl) throws Exception {
		String responseXML="";
		String rallyApiHttpUsername="randy.chen@raritan.com";
		String rallyApiHttpPassword="raritanat1219";
	
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		Base64 base64 = new Base64();
		String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
		HttpGet httpGet = new HttpGet(apiUrl);
		httpGet.addHeader("Authorization","Basic "+encodeString);
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		if(entity != null) {

			InputStreamReader reader = new InputStreamReader(entity.getContent());
			BufferedReader br = new BufferedReader(reader);	
	
			StringBuilder sb=new StringBuilder();
			String line ="";
			while((line = br.readLine()) != null) {
				sb.append(line);
			}			
			
			responseXML=sb.toString();

		}
		
		log.debug("responseXML="+responseXML);
		
		return responseXML;
	}

%>
<%	
	response.addHeader("Expires",   "Mon, 26 Jul 1991 05:00:00 GMT");
	response.addHeader("Last-Modified",   "Mon, 26 Mar 2007 04:33:08 GMT");
	response.addHeader("Cache-Control",   "no-store, no-cache, must-revalidate");
	response.addHeader("Cache-Control",   "post-check=0, pre-check=0");
	response.addHeader("Pragma",   "no-cache");
	
	response.setDateHeader("Expires", 0);
	
	request.setCharacterEncoding("UTF-8");
	response.setContentType("text/html;charset=UTF-8");
		
%>
<title>Workflow Service</title>
<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" type="text/css" href="css/main.css" />
<script src="scripts/jquery-1.7.1.min.js"></script>
