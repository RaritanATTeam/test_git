package com.raritan.at.workflow.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.*;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.protocol.BasicHttpContext;

import org.apache.http.*;
import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.xpath.*;

import com.raritan.at.workflow.bean.*;
import static com.raritan.at.workflow.service.Constant.*;

public class BaseService {

	protected Logger log = Logger.getLogger(this.getClass());
	
	protected int resultCode;
	protected String resultMessage="";
	
	protected String bonitaApiHost;	
	protected String bonitaApiUsername;
	protected String bonitaApiPassword;
	protected String bonitaApiHttpUsername;
	protected String bonitaApiHttpPassword;
	protected String bonitaApiPath;
	//protected String bonitaProcessId;
	
	protected String workflowServiceUrl;
    
	protected String rallyApiHost;
	protected String rallyApiHttpUsername;
	protected String rallyApiHttpPassword;
	protected String rallyProjectIds;
	
	protected Properties confProperties;
	
	protected String confFileName;
	
	protected AuthUser user;
	
	public BaseService() {
		
	}
	
	public BaseService(String confFileName) {
		this.confFileName=confFileName;
	
		loadProperty();
	
	}
	
	public void setUser(AuthUser user) {
		this.user=user;
	}
	
	public void setConfigFile(String confFileName) {
		this.confFileName=confFileName;
		
		loadProperty();
	}
	
	public void loadProperty() {
	
		try {
		
			File file=new File(confFileName);
			
			log.debug("file="+file.getAbsolutePath());
				
			confProperties=new Properties();
    		confProperties.loadFromXML(new FileInputStream(file));
    		
    		bonitaApiHost=confProperties.getProperty("bonita.api.host");    		
    		bonitaApiUsername=confProperties.getProperty("bonita.api.username");
    		bonitaApiPassword=confProperties.getProperty("bonita.api.password");
    		bonitaApiHttpUsername=confProperties.getProperty("bonita.api.http.username");
    		bonitaApiHttpPassword=confProperties.getProperty("bonita.api.http.password");
    		bonitaApiPath=confProperties.getProperty("bonita.api.path");
    		
    		rallyApiHost=confProperties.getProperty("rally.api.host");
    		rallyApiHttpUsername=confProperties.getProperty("rally.api.http.username");
    		rallyApiHttpPassword=confProperties.getProperty("rally.api.http.password");
    		rallyProjectIds=confProperties.getProperty("rally.project.ids");
    		
    		//bonitaProcessId=properties.getProperty("bonita.process.xp_code.id");
    		
    		workflowServiceUrl=confProperties.getProperty("service.url");
    		
		} catch (Exception e) {
			log.error("",e);
		}
		
	}
	
	protected String getRallyXML(String apiUrl) throws Exception {
		String responseXML="";
	
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
	

	
	protected String postRallyXML(String apiUrl,String requestXML) throws Exception {
		String responseXML="";
	
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		Base64 base64 = new Base64();
		String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
		HttpPost httpPost = new HttpPost(apiUrl);
		httpPost.addHeader("Authorization","Basic "+encodeString);
		
		httpPost.setEntity(new StringEntity(requestXML));
		
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();		
		
		responseXML=getEntityString(entity);
		
		return responseXML;
	}
	
	protected String getRallyAPIError(String responseXML) {
		String errorMsg="";
		
		try {
		
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Errors");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				
				Element item = (Element) iter.next();
				errorMsg=item.getChildText("OperationResultError");
	
			}
		
		} catch(Exception e) {
			errorMsg=e.toString();
		}

		return errorMsg;
	}
	
	public String getEntityString(HttpEntity entity) throws Exception {
		StringBuilder sb=new StringBuilder();
	
		if(entity!=null) {
			InputStreamReader reader = new InputStreamReader(entity.getContent());
			BufferedReader br = new BufferedReader(reader);	
			
			String line ="";
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
			
		return sb.toString();
	}

	public String loadStream(InputStream is) {
		StringBuilder sb=new StringBuilder();

		try {
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(reader);	
				
			String line ="";
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch(Exception e) {
			log.error("",e);
		}

		return sb.toString(); 
	}
	
    public int getResultCode() {
    	return resultCode;
    }
    
    public String getResultMessage() {
    	return resultMessage;
    }

	public static void main(String[] argv) {
	
	
	}
}  