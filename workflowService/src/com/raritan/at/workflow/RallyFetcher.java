package com.raritan.at.workflow;

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

public class RallyFetcher {

	private String fileName="checklist.xml";
	private String confFileName="config.xml";
	private Logger log = Logger.getLogger(this.getClass());
	
	private String bonitaApiHost;
	private int bonitaApiPort;
	private String bonitaApiProtocol;
	
	private String bonitaImageServerHost;
	private int bonitaImageServerPort;
	private String bonitaImageServerProtocol;
	
	private String bonitaApiUsername;
	private String bonitaApiHttpUsername;
	private String bonitaApiHttpPassword;
	private String bonitaProcessId;
	private String bonitaApiPath;
	private String bonitaFormPath;
    
	private String rallyApiHost;
	private int rallyApiPort;
	private String rallyApiProtocol;
	private String rallyApiHttpUsername;
	private String rallyApiHttpPassword;
	private String rallyProjectIds;
	private String rallyApiPath_ProjectUS;
	private String rallyApiPath_USUpdate;

	public final static String DEFINED="Defined";
	public final static String IN_PROGRESS="In-Progress";
	public final static String COMPLETED="Completed";
	public final static String ACCEPTED="Accepted";

	public RallyFetcher() {
	
	}
	
	public void loadProperty() {
	
		try {
				
			Properties properties=new Properties();
    		properties.loadFromXML(new FileInputStream(confFileName));
    		
    		bonitaApiHost=properties.getProperty("bonita.api.host");
    		bonitaApiPort=Integer.parseInt(properties.getProperty("bonita.api.port"));
    		bonitaApiProtocol=properties.getProperty("bonita.api.protocol");
    		
			bonitaImageServerHost=properties.getProperty("bonita.imageserver.host");
			bonitaImageServerPort=Integer.parseInt(properties.getProperty("bonita.imageserver.port"));
			bonitaImageServerProtocol=properties.getProperty("bonita.imageserver.protocol");
    		
    		bonitaApiUsername=properties.getProperty("bonita.api.username");
    		bonitaApiHttpUsername=properties.getProperty("bonita.api.http.username");
    		bonitaApiHttpPassword=properties.getProperty("bonita.api.http.password");
    		bonitaProcessId=properties.getProperty("bonita.process.id");
    		bonitaApiPath=properties.getProperty("bonita.api.path");
    		bonitaFormPath=properties.getProperty("bonita.form.path");
    		
    		rallyApiHost=properties.getProperty("rally.api.host");
    		rallyApiPort=Integer.parseInt(properties.getProperty("rally.api.port"));
    		rallyApiProtocol=properties.getProperty("rally.api.protocol");
    		rallyApiHttpUsername=properties.getProperty("rally.api.http.username");
    		rallyApiHttpPassword=properties.getProperty("rally.api.http.password");
    		rallyProjectIds=properties.getProperty("rally.project.ids");
    		rallyApiPath_ProjectUS=properties.getProperty("rally.api.path.project_us");
    		rallyApiPath_USUpdate=properties.getProperty("rally.api.path.us_update");
    		
		} catch (Exception e) {
			log.error("",e);
		}
		
	}
	
	public void doProcess() {
	
		loadProperty();
	
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewUSFromRally(projectId);
		}
		
	}
	    
    public void checkNewUSFromRally(String projectId) {
    
        boolean ok = false;
        File file=new File(fileName);
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
		
			String apiUrl=rallyApiProtocol+"://"+rallyApiHost+":"+rallyApiPort+URLDecoder.decode(rallyApiPath_ProjectUS+projectId);
			
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
				
				//log.info("responseXML="+responseXML);
				
				list=parseXML(projectId,responseXML);
				
			}
			
			for(Map map:list) {
			
				log.info("map="+map);
			
				String objId=(String)map.get("objId");
				String name=(String)map.get("name");
				String state=(String)map.get("state");
				String usId=(String)map.get("usId");
				String owner=(String)map.get("owner");
				String refUrl=(String)map.get("refUrl");
																
				if(!"".equals(usId) && !"".equals(state)) {
					
					boolean stateChanged=false;
					
					String existState=properties.getProperty(usId);
					
					//Check state change
					if(existState==null && DEFINED.equals(state)) 
						stateChanged=true; //else //None->D
					/*
					if(!DEFINED.equals(state) && DEFINED.equals(existState)) 
						stateChanged=true; else //D->P || C || A
					if(!IN_PROGRESS.equals(state) && IN_PROGRESS.equals(existState)) 
						stateChanged=true; else //P->D || C || A
					if(!COMPLETED.equals(state) && COMPLETED.equals(existState)) 
						stateChanged=true; else //C->D || P || A
					if(!ACCEPTED.equals(state) && ACCEPTED.equals(existState))
						stateChanged=true; //A->D || P || C
					*/
					
					if(stateChanged) {
						
						//Get owner's email
						
						if("NO_USER".equals(owner)) {
						
							updateRallyState(objId,"DONT_ADD_BONITA",projectId);
						
						} else {
						
							String ownerRef=(String)map.get("ownerRef");
							String ownerEmail="NO_EMAIL";
							if(!"NO_REF".equals(ownerRef)) {
								ownerEmail=getOwnerEmail(ownerRef);
							}
							
							String bonitaId=sendToBonitaAPI(usId,state,name,owner,objId,refUrl,ownerEmail);
						
							if(bonitaId!=null) {
													
								updateRallyState(objId,bonitaId,projectId);
							
								properties.setProperty(usId,state);
								log.info("setProperty:"+usId+" "+state);
							}
							
							
						} //if("NO_USER".equals(owner))
							
					
					}
					
				}
				
				log.info("---------------------------");
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("ERROR: "+ex.getMessage());
		}
    
    }
    
    public void updateRallyState(String objId,String bonitaId,String projectId) {
                    
		try {
				
			String apiUrl=rallyApiProtocol+"://"+rallyApiHost+":"+rallyApiPort+URLDecoder.decode(rallyApiPath_USUpdate+"/"+objId);
			
			log.info("rallyApiUrl:"+apiUrl);
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			Base64 base64 = new Base64();
			String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
			StringBuffer requestXML=new StringBuffer();
		
			if("DONT_ADD_BONITA".equals(bonitaId)) {
			
				requestXML.append("<HierarchicalRequirement ref=\"https://rally1.rallydev.com/slm/webservice/1.28/hierarchicalrequirement/"+objId+"\">");
				requestXML.append("<WorkflowState>No owner assigned</WorkflowState>");
				requestXML.append("</HierarchicalRequirement>");
 			
 			} else {
 							
				String bonitaUrl=bonitaApiProtocol+"://"+bonitaApiHost+":"+bonitaApiPort+bonitaFormPath;
				bonitaUrl=bonitaUrl.replace("${bonita.process.id}",bonitaProcessId);
				bonitaUrl=bonitaUrl.replace("${bonita.uuid}",bonitaId);
				
				String workflowUILink = bonitaImageServerProtocol+
														"://"+bonitaImageServerHost+":"+
														bonitaImageServerPort
														+"/show?info=" + bonitaId ;
				
				requestXML.append("<HierarchicalRequirement ref=\"https://rally1.rallydev.com/slm/webservice/1.28/hierarchicalrequirement/"+objId+"\">");
				requestXML.append("<WorkflowLink>"+bonitaUrl+"</WorkflowLink>");
				requestXML.append("<WorkflowUILink>"+workflowUILink+"</WorkflowUILink>");
				requestXML.append("<usobjectid >"+objId+"</usobjectid>");
				requestXML.append("<projectId>"+projectId+"</projectId>");
				
				requestXML.append("</HierarchicalRequirement>");
 			
 			}
 			
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
	
	public String getOwnerEmail(String ownerRef) throws Exception {
		
		String ownerEmail="";
		
		Base64 base64 = new Base64();
		String encodeString = new String(base64.encode((rallyApiHttpUsername+":"+rallyApiHttpPassword).getBytes()));
		
		DefaultHttpClient httpClient = new DefaultHttpClient();		
		
		HttpGet httpGet = new HttpGet(ownerRef);
		httpGet.addHeader("Authorization","Basic "+encodeString);
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
				
		String responseXML=getEntityString(entity);
				
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//EmailAddress");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		if(iter.hasNext()) {
			Element item = (Element) iter.next();
			ownerEmail=item.getText();

		}
				
		return ownerEmail;
	}
	
	public List<Map> parseXML(String projectId,String xml) throws Exception {
		List<Map> list=new ArrayList();
				
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		Document doc = bSAX.build(new StringReader(xml));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		while(iter.hasNext()) {
		
			Map map=new HashMap();
		
			Element item = (Element) iter.next();
			String objId=item.getChildText("ObjectID");
			String name=item.getChildText("Name");
			String state=item.getChildText("ScheduleState");
			String usId=item.getChildText("FormattedID");
			
			Element ownerElement=item.getChild("Owner");
			
			String owner="NO_USER";
			String ownerRef="NO_REF";
			
			if(ownerElement!=null) {
				owner=ownerElement.getAttributeValue("refObjectName");
				ownerRef=ownerElement.getAttributeValue("ref");
			}
						
			String refUrl="https://rally1.rallydev.com/slm/rally.sp#/"+projectId+"/detail/userstory/"+objId;
						
			map.put("objId",objId);
			map.put("name",name);
			map.put("state",state);
			map.put("usId",usId);
			map.put("owner",owner);
			map.put("ownerRef",ownerRef);
			map.put("refUrl",refUrl);
			
			list.add(map);
		}
				
		return list;
	}
	
	public String sendToBonitaAPI(String usId,String state,String usName,String usOwner,String objId,String url,String ownerEmail) {
			
		boolean sendOK=false;
		String bonitaId=null;
       
       DefaultHttpClient httpClient = new DefaultHttpClient();
              
       String apiUrl=bonitaApiProtocol+"://"+bonitaApiHost+":"+bonitaApiPort+bonitaApiPath+bonitaProcessId;
       
        try {
        
			HttpHost targetHost = new HttpHost(bonitaApiHost, bonitaApiPort, bonitaApiProtocol);
			
            httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(bonitaApiHttpUsername, bonitaApiHttpPassword));
                    
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
			
        	StringBuffer variables=new StringBuffer();        	
			variables.append("<map>");
			variables.append("<entry>");
			variables.append("<string>user_Story_ID</string>");
			variables.append("<string>"+usId+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>state</string>");
			variables.append("<string>"+state+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>title</string>");
			variables.append("<string>"+usName+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>owner</string>");
			variables.append("<string>"+usOwner+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>email</string>");
			variables.append("<string>"+ownerEmail+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>obj_id</string>");
			variables.append("<string>"+objId+"</string>");
			variables.append("</entry>");
			variables.append("<entry>");
			variables.append("<string>url</string>");
			variables.append("<string>"+url+"</string>");
			variables.append("</entry>");
			variables.append("</map>");
			        
            HttpPost httpPost = new HttpPost(apiUrl);
            
            log.info("executing request " + httpPost.getURI());
            log.info("variables="+variables);
            
            HttpParams params=new BasicHttpParams();
                        
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();  
			nvps.add(new BasicNameValuePair("options", "user:"+bonitaApiUsername));
			nvps.add(new BasicNameValuePair("variables", variables.toString()));  
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseXML = httpClient.execute(httpPost, responseHandler,localcontext);
            log.info("----------------------------------------");
            log.info(responseXML);
            log.info("----------------------------------------");
            
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//ProcessInstanceUUID");
			List xlist = xpath.selectNodes(root);
			
			log.info("xlist.size()="+xlist.size());
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				Element item = (Element) iter.next();
				bonitaId=item.getChildText("value");
			}
            
            log.info("bonitaId="+bonitaId);
        	
        	sendOK=true;
            
        } catch(Exception e) {
        	e.printStackTrace();
			log.error("",e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
        if(sendOK)
	        return bonitaId;
        else
        	return null;
	
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

	public static void main(String[] argv) {
	
		RallyFetcher fetcher=new RallyFetcher();

		//fetcher.checkNewUSFromRally();
		fetcher.doProcess();
	
	}
}  