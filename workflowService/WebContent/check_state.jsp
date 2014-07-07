<%@ page contentType="text/xml; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.net.*" %>

<%@ page import="javax.security.auth.login.*" %>
<%@ page import="org.ow2.bonita.facade.*" %>
<%@ page import="org.ow2.bonita.facade.runtime.*" %>
<%@ page import="org.ow2.bonita.facade.uuid.*" %>
<%@ page import="org.ow2.bonita.facade.identity.*" %>
<%@ page import="org.ow2.bonita.facade.def.majorElement.*" %>
<%@ page import="org.ow2.bonita.util.*" %>

<%@ page import="org.apache.http.auth.*" %>
<%@ page import="org.apache.http.entity.*" %> 
<%@ page import="org.apache.http.client.*" %>
<%@ page import="org.apache.http.client.entity.*" %>
<%@ page import="org.apache.http.client.methods.*" %>
<%@ page import="org.apache.http.client.protocol.*" %>

<%@ page import="org.apache.http.impl.client.*" %>
<%@ page import="org.apache.http.impl.auth.*" %>
<%@ page import="org.apache.org.apache.http.impl.client.*" %>
<%@ page import="org.apache.org.apache.http.message.*" %>
<%@ page import="org.apache.org.apache.http.params.*" %>
<%@ page import="org.apache.http.protocol.BasicHttpContext" %>

<%@ page import="org.apache.log4j.*" %>

<%@ page import="org.apache.http.*" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>


<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.*" %>
<%@ page import="org.jdom.xpath.*" %>

<%!
	private Logger log = Logger.getLogger(this.getClass());
	
	private String resultCode="";
	private String resultMessage="";
	
	String rallyApiHttpUsername="randy.chen@raritan.com";
	String rallyApiHttpPassword="raritanat1219";
	String projectId="3874572825";
	
	public void checkRallyTestCase(String testCaseId,String taskNo) throws Exception {
			
		List<Map> list=new ArrayList<Map>();
	
		//String apiUrl=rallyApiProtocol+"://"+rallyApiHost+":"+rallyApiPort+URLDecoder.decode(rallyApiPath_ProjectUS+projectId);
		String apiUrl="https://rally1.rallydev.com:443/slm/webservice/1.29/testcase?query=(FormattedID%20=%20"+testCaseId+")&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		Base64 base64 = new Base64();
		String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
		HttpGet httpGet = new HttpGet(apiUrl);
		httpGet.addHeader("Authorization","Basic "+encodeString);
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		if (entity != null) {
		
			String responseXML=getEntityString(entity);
			
			log.info("responseXML="+responseXML);
			
			int resultCount=getResultCount(responseXML);
			
			if(resultCount==0) {
				resultCode="1";
				resultMessage="Invaild Test Case No";
			} else {
			
				
				Map testCaseMap=parseXML(responseXML);
				
				String objId=(String)testCaseMap.get("objId");
				
				updateRallyTCState(objId,taskNo);
				
				resultCode="0";
				resultMessage="testCaseMap="+testCaseMap;				
				
			}
			
			log.info("resultCount="+resultCount);
			
		}
		
	}

	public Map parseXML(String xml) throws Exception {
		Map map=new HashMap();
				
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(xml));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		while(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			String objId=item.getChildText("ObjectID");
			String name=item.getChildText("Name");
			String type=item.getChildText("Type");
			String tcId=item.getChildText("FormattedID");
			
			Element ownerElement=item.getChild("Owner");
			
			String owner="NO_USER";
			String ownerRef="NO_REF";
			
			if(ownerElement!=null) {
				owner=ownerElement.getAttributeValue("refObjectName");
				ownerRef=ownerElement.getAttributeValue("ref");
			}
									
			map.put("objId",objId);
			map.put("name",name);
			map.put("type",type);
			map.put("tcId",tcId);
			map.put("ownerRef",ownerRef);
			
		}
				
		return map;
	}
	
	public int getResultCount(String xml) throws Exception {
	
		int resultCount=0;
				
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(xml));
		Element root = doc.getRootElement(); 		

		String totalResultCount=root.getChildText("TotalResultCount");
		
		resultCount=Integer.parseInt(totalResultCount);
			
		return resultCount;
	}
	
    public void updateRallyTCState(String objId,String taskNo) {
                    
		try {
				
			//String apiUrl=rallyApiProtocol+"://"+rallyApiHost+":"+rallyApiPort+URLDecoder.decode(rallyApiPath_USUpdate+"/"+objId);
		
			String apiUrl="https://rally1.rallydev.com/slm/webservice/1.29/testcase/"+objId;
			
			log.info("rallyApiUrl:"+apiUrl);
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			Base64 base64 = new Base64();
			String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
			StringBuffer requestXML=new StringBuffer();
			
			requestXML.append("<TestCase ref=\""+apiUrl+"\">");
			requestXML.append("<TaskNo>"+taskNo+"</TaskNo>");
			requestXML.append("</TestCase>");
 			
 			log.info("requestXML="+requestXML);

			HttpPost httpPost = new HttpPost(apiUrl);
			httpPost.addHeader("Authorization","Basic "+encodeString);
			
			httpPost.setEntity(new StringEntity(requestXML.toString()));
			
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			String responseXML="";
			
			if (entity != null) {
				InputStreamReader reader = new InputStreamReader(entity.getContent());
				BufferedReader br = new BufferedReader(reader);	
	
				StringBuilder sb=new StringBuilder();
				String line ="";
				while((line = br.readLine()) != null) {
					sb.append(line);
				}			
				
				responseXML=sb.toString();
				
				log.info("responseXML="+responseXML);
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("ERROR: "+ex.getMessage());
		}
    
    }
	
	private String getEntityString(HttpEntity entity) throws Exception {
		String responseXML="";
	
		InputStreamReader reader = new InputStreamReader(entity.getContent());
		BufferedReader br = new BufferedReader(reader);	

		StringBuilder sb=new StringBuilder();
		String line ="";
		while((line = br.readLine()) != null) {
			sb.append(line);
		}			
		
		responseXML=sb.toString();
			
		return responseXML;
	}

%>
<%

out.clear();

String action=request.getParameter("action");
String task_no=request.getParameter("task_no");
String test_case_no=request.getParameter("test_case_no");
String test_case_no2=request.getParameter("test_case_no2");

StringBuilder html=new StringBuilder();
html.append("0");

log.info("action:"+action);
log.info("task_no:"+task_no);
log.info("test_case_no:"+test_case_no);

log.info("html feedback "+html);

if("checkTestCase".equals(action)) {

	checkRallyTestCase(test_case_no,task_no);

}

%><result>
<resultCode><%=resultCode%></resultCode>
<resultMessage><%=resultMessage%></resultMessage>
</result>