package com.raritan.at.workflow.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

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

public class XPService extends RallyService {
		
	public XPService() {
	

	}
	
	public XPService(String confFileName) {
		super(confFileName);

	}

	public void checkNewRelease(String filePath) {
	
		String process="xp_project";
	
		String bonitaProcessId=(String)confProperties.get("bonita.process."+process+".id");
		String fileName=(String)confProperties.get("file.checklist."+process+".id");
		rallyProjectIds=(String)confProperties.get("rally.project."+process+".ids");
		
		if(bonitaProcessId==null || fileName==null || rallyProjectIds==null) {
			log.info("bonitaProcessId="+bonitaProcessId);
			log.info("fileName="+fileName);
			log.info("rallyProjectIds="+rallyProjectIds);
			return;
		}
		
		String filePathName=filePath+"/"+fileName;
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewRelease(filePathName,bonitaProcessId,projectId);
		}
	
	}
	
   public void checkNewRelease(String filePath,String bonitaProcessId,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
                
    	log.info("file="+file.getAbsolutePath());
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
    	    		
			String apiUrl=rallyApiHost+
				"/release?"+
				"project="+rallyApiHost+"/project/"+projectId;
			
			int totalResultCount=getTotalResultCount(apiUrl);
			
			log.info("totalResultCount="+totalResultCount);
			
			apiUrl=apiUrl+"&fetch=true&start=1&pagesize="+totalResultCount;

			log.info("rallyApiUrl:"+apiUrl);
			
			String responseXML=getRallyXML(apiUrl);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
			
				Map map=new HashMap();
			
				Element item = (Element) iter.next();
				String objId=item.getChildText("ObjectID");
				String name=item.getChildText("Name");
				String state=item.getChildText("State");
				
				String releaseRef=item.getAttribute("ref").getValue();
				
				Element projectElement=item.getChild("Project");
				String projectName=projectElement.getAttribute("refObjectName").getValue();
							
				map.put("objId",objId);
				map.put("rallyRef",releaseRef);
				map.put("projectName",projectName);
				map.put("name",name);
				map.put("state",state);
				
				list.add(map);
			}
			
			for(Map map:list) {
			
				log.debug("map="+map);
			
				String rallyRef=(String)map.get("rallyRef");
				String objId=(String)map.get("objId");
				String name=(String)map.get("name");
				String projectName=(String)map.get("projectName");
				String state=(String)map.get("state");
				
				boolean stateChanged=false;
				
				String existState=properties.getProperty(objId);
				
				//Check state change
				if(existState==null && 
					(PLANNING.equals(state))) {
					stateChanged=true; //else //None->P
				}
				
				log.info("existState="+existState+" state="+state+" stateChanged="+stateChanged);
				
				if(stateChanged) {
								
					Map varMap=new HashMap();
					
					varMap.put("rally_ref",rallyRef);
					//varMap.put("_description_",name);
					varMap.put("_description_",projectName+" - "+name);
					
					String ownerEmail="randy.chen@raritan.com";
																			
					BonitaService bonitaService=new BonitaService(confFileName);
					String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
				
					if(bonitaId!=null && !"".equals(bonitaId)) {
											
						//updateRallyUSState(projectId,objId,bonitaId);
						
						//updateRallyUSName(objId,"New workflow created");
					
						properties.setProperty(objId,state);
						log.info("setProperty:"+objId+" "+state);
					}
					
					log.info("---------------------------");
				
				}
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("ERROR: "+ex.getMessage());
		}
    
    }
    
	public void checkIterationsExist(String processName) throws Exception {

		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List iterationList=getIterationList(rallyRef);

		log.info("iteration list="+iterationList);
				
		if(iterationList.size()>0) {
			resultCode=RESULT_OK;
			resultMessage="iterations:"+iterationList.size();
		} else {
			resultCode=RESULT_FAIL;
			resultMessage="no iteration";
		}
				
	}
	
	public List getIterationList(String rallyRef) {
	
		List<Map> iterationList=new ArrayList<Map>();
		
		String apiUrl=rallyRef+"?fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		try {
		
			//1.Get this release schedule
			//https://rally1.rallydev.com/slm/webservice/1.31/release/4707865875
		
			String responseXML=getRallyXML(apiUrl);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Release");
			List xlist = xpath.selectNodes(root);
			
			long releaseStartDate=0L;
			long releaseEndDate=0L;
			
			String releaseName="";
			String projectId="";
			
			SimpleDateFormat ISO8601FORMAT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			
			Iterator iter = xlist.iterator();
			if(iter.hasNext()) {
			
				Element item = (Element) iter.next();
				releaseName=item.getChildText("Name");
				String startDateStr=item.getChildText("ReleaseStartDate");
				String endDateStr=item.getChildText("ReleaseDate");
				
				log.info("startDateStr="+startDateStr);
				log.info("endDateStr="+endDateStr);
				
				Date startDate = ISO8601FORMAT.parse(startDateStr);
				Date endDate = ISO8601FORMAT.parse(endDateStr);
				
				log.info("startDate="+startDate);
				log.info("endDate="+endDate);
				
				releaseStartDate=startDate.getTime();
				releaseEndDate=endDate.getTime();
	
				Element child=item.getChild("Project");
				String projectRef=child.getAttribute("ref").getValue();
				
				String[] refs=projectRef.split("/");
				projectId=refs[refs.length-1];
			}
			
	
			log.info("projectId="+projectId);
			log.info("releaseStartDate="+releaseStartDate);
			log.info("releaseEndDate="+releaseEndDate);
			
			//2.Get all iteration schedule
			//https://rally1.rallydev.com/slm/webservice/1.31/iteration?project=https://rally1.rallydev.com/slm/webservice/1.31/project/4681584283&fetch=true
			
			apiUrl=rallyApiHost+
								"/iteration?"+
								"project="+rallyApiHost+"/project/"+projectId+"&fetch=true&start=1&pagesize=100";
								
			log.info("apiUrl="+apiUrl);
			
			responseXML=getRallyXML(apiUrl);
				
			bSAX = new org.jdom.input.SAXBuilder(); 
			doc = bSAX.build(new StringReader(responseXML));
			root = doc.getRootElement(); 		
	
			xpath = XPath.newInstance("//Object");
			xlist = xpath.selectNodes(root);
			iter = xlist.iterator();
			while(iter.hasNext()) {
				Map map=new HashMap();

				Element item = (Element) iter.next();
				String name=item.getChildText("Name");
				String startDateStr=item.getChildText("StartDate");
				String endDateStr=item.getChildText("EndDate");
				String state=item.getChildText("State");
				
				Date startDate = ISO8601FORMAT.parse(startDateStr);
				Date endDate = ISO8601FORMAT.parse(endDateStr);
				
				log.info("iteration name="+name);
				log.info("startDateStr="+startDateStr);
				log.info("endDateStr="+endDateStr);
				log.info("startDate="+startDate);
				log.info("endDate="+endDate);
				
				long iterationStartDate=startDate.getTime();
				long iterationEndDate=endDate.getTime();
				
				//3.Compare the schedules to determine the iterations inside this release
				
				if(iterationEndDate>=releaseStartDate && iterationStartDate<=releaseEndDate) {
				
					log.info("iteration name="+name+" is in release:"+releaseName);
					
					map.put("iterationStartDate",iterationStartDate);
					map.put("iterationEndDate",iterationEndDate);
					map.put("iterationName",name);
					map.put("iterationState",state);
					
					iterationList.add(map);
				
				}
				
			}
		
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
	
		return iterationList;
	}
	
	public void checkIterationsState(String processName) throws Exception {
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List<Map> iterationList=getIterationList(rallyRef);
		
		boolean isAllAccepted=true;
		StringBuilder sb=new StringBuilder();
		sb.append("not accepted iteration(s):");
		for(Map map:iterationList) {
		
			String state=(String)map.get("iterationState");
		
			if(!state.equals(ACCEPTED)) {
				isAllAccepted=false;
				String name=(String)map.get("iterationName");
				sb.append(name+":"+state+" ");
			}
		
		}
		
		if(isAllAccepted) {
			resultCode=RESULT_OK;
			resultMessage="";
		} else {
			resultCode=RESULT_FAIL;
			resultMessage=sb.toString();
		} 
		
	}

	public void changeReleaseState(String processName,String state) throws Exception {
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		try {

			String apiUrl=rallyRef;
			
			log.info("rallyApiUrl:"+apiUrl);
		
			StringBuffer requestXML=new StringBuffer();
			
			String updateState="";
			if(state.equalsIgnoreCase(ACTIVE)) updateState=ACTIVE; else;
			if(state.equalsIgnoreCase(ACCEPTED)) updateState=ACCEPTED;
			
			log.info("updateState="+updateState);
			
			if(!"".equals(updateState)) {
				requestXML.append("<Release ref=\""+apiUrl+"\">");
				requestXML.append("<State>"+updateState+"</State>");
				requestXML.append("</Release>");
				
				log.info("requestXML="+requestXML);
				
				String responseXML=postRallyXML(apiUrl,requestXML.toString());
				String errorMsg=getRallyAPIError(responseXML);
				log.info("responseXML="+responseXML);
				log.info("errorMsg="+errorMsg);
				
				if(errorMsg==null || "".equals(errorMsg)) {
					resultCode=RESULT_OK;
					resultMessage="";			
				} else {
					resultCode=RESULT_FAIL;
					resultMessage=errorMsg;
				}
				
			}


		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
		
	}


	public void checkNewIteration(String filePath) {
	
		String process="xp_iteration";
	
		String bonitaProcessId=(String)confProperties.get("bonita.process."+process+".id");
		String fileName=(String)confProperties.get("file.checklist."+process+".id");
		rallyProjectIds=(String)confProperties.get("rally.project."+process+".ids");
		
		if(bonitaProcessId==null || fileName==null || rallyProjectIds==null) {
			log.info("bonitaProcessId="+bonitaProcessId);
			log.info("fileName="+fileName);
			log.info("rallyProjectIds="+rallyProjectIds);
			return;
		}
		
		String filePathName=filePath+"/"+fileName;
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewIteration(filePathName,bonitaProcessId,projectId);
		}
	
	}
	
   public void checkNewIteration(String filePath,String bonitaProcessId,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
                
    	log.info("file="+file.getAbsolutePath());
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
    	    		
			String apiUrl=rallyApiHost+
				"/iteration?"+
				"project="+rallyApiHost+"/project/"+projectId;
			
			int totalResultCount=getTotalResultCount(apiUrl);
			
			log.info("totalResultCount="+totalResultCount);
			
			apiUrl=apiUrl+"&fetch=true&start=1&pagesize="+totalResultCount;

			log.info("rallyApiUrl:"+apiUrl);
			
			String responseXML=getRallyXML(apiUrl);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
			
				Map map=new HashMap();
			
				Element item = (Element) iter.next();
				String objId=item.getChildText("ObjectID");
				String name=item.getChildText("Name");
				String state=item.getChildText("State");
				
				String releaseRef=item.getAttribute("ref").getValue();
				
				Element projectElement=item.getChild("Project");
				String projectName=projectElement.getAttribute("refObjectName").getValue();
							
				map.put("objId",objId);
				map.put("rallyRef",releaseRef);
				map.put("projectName",projectName);
				map.put("name",name);
				map.put("state",state);
				
				list.add(map);
			}
			
			for(Map map:list) {
			
				log.debug("map="+map);
			
				String rallyRef=(String)map.get("rallyRef");
				String objId=(String)map.get("objId");
				String name=(String)map.get("name");
				String projectName=(String)map.get("projectName");
				String state=(String)map.get("state");
				
				boolean stateChanged=false;
				
				String existState=properties.getProperty(objId);
				
				//Check state change
				if(existState==null && 
					(PLANNING.equals(state))) {
					stateChanged=true; //else //None->P
				}
				
				log.info("existState="+existState+" state="+state+" stateChanged="+stateChanged);
				
				if(stateChanged) {
								
					Map varMap=new HashMap();
					
					varMap.put("rally_ref",rallyRef);
					varMap.put("_description_",projectName+" - "+name);
					
					String ownerEmail="randy.chen@raritan.com";
																			
					BonitaService bonitaService=new BonitaService(confFileName);
					String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
				
					if(bonitaId!=null && !"".equals(bonitaId)) {
					
						properties.setProperty(objId,state);
						log.info("setProperty:"+objId+" "+state);
					}
					
					log.info("---------------------------");
				
				}
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("ERROR: "+ex.getMessage());
		}
    
    }
    
	public void checkUserStoriesExist(String processName) throws Exception {

		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List userStoryList=getUserStoryList(rallyRef);

		log.info("userStoryList list="+userStoryList);
				
		if(userStoryList.size()>0) {
			resultCode=RESULT_OK;
			resultMessage="user stories:"+userStoryList.size();
		} else {
			resultCode=RESULT_FAIL;
			resultMessage="no user story";
		}
				
	}
	
	public List getUserStoryList(String rallyRef) {
	
		List<Map> userStoryList=new ArrayList<Map>();
		
		//String apiUrl=rallyRef+"?fetch=true";
		String apiUrl=rallyApiHost+"/hierarchicalrequirement?"+
								"query=(Iteration%20=%20"+rallyRef+")&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		try {
				
			String responseXML=getRallyXML(apiUrl);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
									
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				Map map=new HashMap();
				Element item = (Element) iter.next();
				
				String objId=item.getChildText("ObjectID");
				String formattedID=item.getChildText("FormattedID");
				String name=item.getChildText("Name");
				String state=item.getChildText("ScheduleState");
				
				String userStoryRef=item.getAttribute("ref").getValue();
				
				map.put("objId",objId);
				map.put("rallyRef",userStoryRef);
				map.put("formattedID",formattedID);
				map.put("name",name);
				map.put("state",state);
				
				userStoryList.add(map);

			}

		
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
	
		return userStoryList;
	}
	
	public void checkUserStoriesState(String processName) throws Exception {
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List<Map> userStoryList=getUserStoryList(rallyRef);
		
		boolean isAllAccepted=true;
		StringBuilder sb=new StringBuilder();
		sb.append("not accepted user story(s):");
		for(Map map:userStoryList) {
		
			String state=(String)map.get("state");
		
			if(!state.equals(ACCEPTED)) {
				isAllAccepted=false;
				String name=(String)map.get("name");
				sb.append(name+":"+state+" ");
			}
		
		}
		
		if(isAllAccepted) {
			resultCode=RESULT_OK;
			resultMessage="";
		} else {
			resultCode=RESULT_FAIL;
			resultMessage=sb.toString();
		} 
		
	}
	
	public void changeIterationState(String processName,String state) throws Exception {
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		try {

			String apiUrl=rallyRef;
			
			log.info("rallyApiUrl:"+apiUrl);
		
			StringBuffer requestXML=new StringBuffer();
			
			String updateState="";
			if(state.equalsIgnoreCase(COMMITTED)) updateState=COMMITTED; else;
			if(state.equalsIgnoreCase(ACCEPTED)) updateState=ACCEPTED;
			
			log.info("updateState="+updateState);
			
			if(!"".equals(updateState)) {
				requestXML.append("<Iteration ref=\""+apiUrl+"\">");
				requestXML.append("<State>"+updateState+"</State>");
				requestXML.append("</Iteration>");
				
				log.info("requestXML="+requestXML);
				
				String responseXML=postRallyXML(apiUrl,requestXML.toString());
				String errorMsg=getRallyAPIError(responseXML);
				log.info("responseXML="+responseXML);
				log.info("errorMsg="+errorMsg);
				
				if(errorMsg==null || "".equals(errorMsg)) {
					resultCode=RESULT_OK;
					resultMessage="";			
				} else {
					resultCode=RESULT_FAIL;
					resultMessage=errorMsg;
				}
				
			}


		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
		
	}

	public void changeUserStoryState(String processName, String state)
			throws Exception {

		BonitaService service = new BonitaService(confFileName);
		service.setUser(user);

		String rallyRef = service.getRallyRef(processName);
		log.info("rallyRef=" + rallyRef);

		try {
			String apiUrl = rallyRef;
			log.info("rallyApiUrl:" + apiUrl);

			String updateState = "";
			if (state.equalsIgnoreCase(IN_PROGRESS)) {
				updateState = IN_PROGRESS;
			} else if (state.equalsIgnoreCase(COMPLETED)) {
				updateState = COMPLETED;
			} else if (state.equalsIgnoreCase(ACCEPTED)) {
				updateState = ACCEPTED;
			} else {
				// TODO
			}

			log.info("updateState=" + updateState);

			StringBuffer requestXML = new StringBuffer();

			if (!"".equals(updateState.trim())) {
				requestXML.append("<HierarchicalRequirement ref=\"" + apiUrl + "\">");
				requestXML.append("<ScheduleState>" + updateState + "</ScheduleState>");
				requestXML.append("</HierarchicalRequirement>");

				log.info("requestXML=" + requestXML);

				String responseXML = postRallyXML(apiUrl, requestXML.toString());
				String errorMsg = getRallyAPIError(responseXML);
				log.info("responseXML=" + responseXML);
				log.info("errorMsg=" + errorMsg);

				if (errorMsg == null || "".equals(errorMsg.trim())) {
					resultCode = RESULT_OK;
					resultMessage = "";
				} else {
					resultCode = RESULT_FAIL;
					resultMessage = errorMsg;
				}
			}

		} catch (Exception e) {
			log.error(e);
			resultCode = RESULT_EXCEPTION;
			resultMessage = e.toString();
		}
	}

	public void checkNewUserStory(String filePath) {
	
		String process="xp_development";
	
		String bonitaProcessId=(String)confProperties.get("bonita.process."+process+".id");
		String fileName=(String)confProperties.get("file.checklist."+process+".id");
		rallyProjectIds=(String)confProperties.get("rally.project."+process+".ids");
		
		if(bonitaProcessId==null || fileName==null || rallyProjectIds==null) {
			log.info("bonitaProcessId="+bonitaProcessId);
			log.info("fileName="+fileName);
			log.info("rallyProjectIds="+rallyProjectIds);
			return;
		}
		
		String filePathName=filePath+"/"+fileName;
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewUserStory(filePathName,bonitaProcessId,projectId);
		}
	
	}
	
   public void checkNewUserStory(String filePath,String bonitaProcessId,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
                
    	log.info("file="+file.getAbsolutePath());
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
    	    		
			String apiUrl=rallyApiHost+
				"/hierarchicalrequirement?"+
				"project="+rallyApiHost+"/project/"+projectId;
			
			int totalResultCount=getTotalResultCount(apiUrl);
			
			log.info("totalResultCount="+totalResultCount);
			
			apiUrl=apiUrl+"&fetch=true&start=1&pagesize="+totalResultCount;

			log.info("rallyApiUrl:"+apiUrl);
			
			String responseXML=getRallyXML(apiUrl);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
			
				Map map=new HashMap();
			
				Element item = (Element) iter.next();
				String objId=item.getChildText("ObjectID");
				String formattedID=item.getChildText("FormattedID");
				String name=item.getChildText("Name");
				String state=item.getChildText("ScheduleState");
				
				String iterationName="";
				Element iterationElement=item.getChild("Iteration");
				if(iterationElement!=null) {
					iterationName=iterationElement.getAttribute("refObjectName").getValue();
				}
				
				String releaseRef=item.getAttribute("ref").getValue();
				
				Element projectElement=item.getChild("Project");
				String projectName=projectElement.getAttribute("refObjectName").getValue();
				

				Element ownerElement=item.getChild("Owner");
				
				String owner="NO_USER";
				String ownerRef="NO_REF";
				
				if(ownerElement!=null) {
					owner=ownerElement.getAttributeValue("refObjectName");
					ownerRef=ownerElement.getAttributeValue("ref");
				}
							
				map.put("objId",objId);
				map.put("formattedID",formattedID);
				map.put("rallyRef",releaseRef);
				map.put("projectName",projectName);
				map.put("name",name);
				map.put("state",state);
				map.put("iterationName",iterationName);
				map.put("ownerRef",ownerRef);
				
				list.add(map);
			}
			
			for(Map map:list) {
			
				log.debug("map="+map);
			
				String rallyRef=(String)map.get("rallyRef");
				String rallyUrl=(String)map.get("rallyUrl");
				String objId=(String)map.get("objId");
				String formattedID=(String)map.get("formattedID");
				String name=(String)map.get("name");
				String projectName=(String)map.get("projectName");
				String state=(String)map.get("state");
				String iterationName=(String)map.get("iterationName");
				
				boolean stateChanged=false;
				
				String existState=properties.getProperty(formattedID);
				
				//Check state change
				//Except US in backlog
				if(existState==null && 
					!"".equals(iterationName) && 
					(DEFINED.equals(state) || (IN_PROGRESS.equals(state)))
					) {
					stateChanged=true; //else //None->P
				}
				
				log.info(formattedID+" existState="+existState+" state="+state+" iterationName="+iterationName+" stateChanged="+stateChanged);
				
				if(stateChanged) {
								
					String ownerRef=(String)map.get("ownerRef");
					String ownerEmail="NO_EMAIL";
					if(!"NO_REF".equals(ownerRef)) {
						ownerEmail=getOwnerEmail(ownerRef);
					}
								
					Map varMap=new HashMap();
					
					varMap.put("rally_ref",rallyRef);
					//varMap.put("_description_",projectName+" - "+name);
					varMap.put("_description_",formattedID);
					
					//String ownerEmail="randy.chen@raritan.com";
																			
					BonitaService bonitaService=new BonitaService(confFileName);

					String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
				
					if(bonitaId!=null && !"".equals(bonitaId)) {
					
						properties.setProperty(formattedID,state);
						log.info("setProperty:"+formattedID+" "+state);
					}
					
					log.info("---------------------------");
				
				}
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception e) {
			log.error("ERROR: ",e);
		}
    
    }

	public void checkTasksExist(String processName) throws Exception {

		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List taskList=getTaskList(rallyRef);

		log.info("taskList list="+taskList);
				
		if(taskList.size()>0) {
			resultCode=RESULT_OK;
			resultMessage="tasks:"+taskList.size();
		} else {
			resultCode=RESULT_FAIL;
			resultMessage="no task";
		}
				
	}
	
	public List getTaskList(String rallyRef) {
	
		List<Map> userStoryList=new ArrayList<Map>();
		
		//String apiUrl=rallyRef+"?fetch=true";
		String apiUrl=rallyApiHost+"/task?"+
								"query=(WorkProduct%20=%20"+rallyRef+")&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		try {
				
			String responseXML=getRallyXML(apiUrl);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
									
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				Map map=new HashMap();
				Element item = (Element) iter.next();
				
				String objId=item.getChildText("ObjectID");
				String formattedID=item.getChildText("FormattedID");
				String name=item.getChildText("Name");
				String state=item.getChildText("State");
				
				String userStoryRef=item.getAttribute("ref").getValue();
				
				map.put("objId",objId);
				map.put("rallyRef",userStoryRef);
				map.put("formattedID",formattedID);
				map.put("name",name);
				map.put("state",state);
				
				userStoryList.add(map);

			}

		
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
	
		return userStoryList;
	}
	
	public void checkTasksAndTestResult(String processName) {
	
		try {
	
			checkTasksState(processName);
			
			if(resultCode==RESULT_OK) {
				checkTestCaseState(processName);
			}
		
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.toString();
		}
	
	}

	public void checkTasksState(String processName) throws Exception {
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		log.info("rallyRef="+rallyRef);
		
		List<Map> taskList=getTaskList(rallyRef);
		
		boolean isAllCompleted=true;
		StringBuilder sb=new StringBuilder();
		sb.append("not completed task(s):");
		for(Map map:taskList) {
		
			String state=(String)map.get("state");
		
			if(!state.equals(COMPLETED)) {
				isAllCompleted=false;
				String formattedID=(String)map.get("formattedID");
				sb.append(formattedID+":"+state+" ");
			}
		
		}
		
		if(isAllCompleted) {
			resultCode=RESULT_OK;
			resultMessage="";
		} else {
			resultCode=RESULT_FAIL;
			resultMessage=sb.toString();
		} 
		
	}

	public void checkTestCaseState(String processName) throws Exception {
	
		//Possible condition:
		//1. Test case exist: true /false
		//2. Test result exist: true /false
		//3. Test resul pass: true /false
	
		BonitaService service=new BonitaService(confFileName);
		service.setUser(user);
		
		String rallyRef=service.getRallyRef(processName);
		
		//https://rally1.rallydev.com/slm/webservice/1.31/testcase?query=(WorkProduct%20=%20https://rally1.rallydev.com/slm/webservice/1.31/hierarchicalrequirement/5754096633)&fetch=true
		//https://rally1.rallydev.com/slm/webservice/1.31/testcase?query=(WorkProduct%20=%20https://rally1.rallydev.com/slm/webservice/1.31/hierarchicalrequirement/5754096804)&fetch=true
		
		
		log.info("rallyRef="+rallyRef);
	
		String apiUrl=rallyApiHost+"/testcase?query=(WorkProduct%20=%20"+rallyRef+")&fetch=true";
				
		log.info("rallyApiUrl:"+apiUrl);
		
		String responseXML=getRallyXML(apiUrl);

		resultCode=RESULT_OK;
		
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		StringBuilder testResult=new StringBuilder();
		
		Iterator iter = xlist.iterator();
		while(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			String formattedID=item.getChildText("FormattedID");
			String lastVerdict=item.getChildText("LastVerdict");
									
			if(lastVerdict==null) {
				testResult.append("no test result:"+formattedID+" ");
			} else if("Fail".equals(lastVerdict)) {
				testResult.append("test fail:"+formattedID+" ");
			}
			
		}
		
		if(xlist==null || xlist.size()==0) {
			//no test case
			resultCode=RESULT_FAIL;
			resultMessage="no test case";
		
		} else {
			//no test result or test fail
			if(testResult.length()>0) {
				resultCode=RESULT_FAIL;
				resultMessage=testResult.toString();
			}		
		
		}
		
	
	}

}