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

public class RallyService extends BaseService {
	
	public RallyService() {
	

	}
	
	public RallyService(String confFileName) {
		super(confFileName);

	}

	public Map getRallyTask(String taskNo) throws Exception {
			
		Map map=new HashMap();
	
		String apiUrl=rallyApiHost+"/task?query=(FormattedID%20=%20"+taskNo+")&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		String responseXML=getRallyXML(apiUrl);

		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		if(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			String objId=item.getChildText("ObjectID");
			String name=item.getChildText("Name");
			String state=item.getChildText("State");
			taskNo=item.getChildText("FormattedID");
			
			Element ownerElement=item.getChild("Owner");
			
			String owner=NO_USER;
			String ownerRef=NO_REF;
			
			if(ownerElement!=null) {
				owner=ownerElement.getAttributeValue("refObjectName");
				ownerRef=ownerElement.getAttributeValue("ref");
			}
									
			map.put("objId",objId);
			map.put("name",name);
			map.put("state",state);
			map.put("taskNo",taskNo);
			map.put("ownerRef",ownerRef);
			
		}
			
		return map;
	}
	
	public String getOwnerEmail(String ownerRef) throws Exception {
		
		String ownerEmail="";
		
		String responseXML=getRallyXML(ownerRef);
				
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
	
	public void checkRallyTestCase(String testCaseId,String taskNo) throws Exception {
			
		List<Map> list=new ArrayList<Map>();
	
		String apiUrl=rallyApiHost+"/testcase?query=(FormattedID%20=%20"+testCaseId+")&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		String responseXML=getRallyXML(apiUrl);

		Map map=new HashMap();
		
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		if(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			String objId=item.getChildText("ObjectID");
			String name=item.getChildText("Name");
			String type=item.getChildText("Type");
			String tcId=item.getChildText("FormattedID");
			
			Element ownerElement=item.getChild("Owner");
			
			String owner=NO_USER;
			String ownerRef=NO_REF;
			
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
		
		log.debug("map="+map);
		
		String objId=(String)map.get("objId");
		String type=(String)map.get("type");
		
		if(map==null || map.size()==0) {
		
			resultCode=RESULT_FAIL;
			resultMessage="Invalid TestCase No";
			
		} else if(type==null || !"Unit test".equals(type)) {
		
			resultCode=RESULT_FAIL;
			resultMessage="TestCase Type is not 'Unit Test'";
			
		} else {
			
			updateRallyTCState(objId,taskNo);
			
			resultCode=RESULT_OK;
			resultMessage="testCaseMap="+map;				
			
		}
				
	}
	    
	public void checkRallyTestResult(String testCaseId,String taskNo) throws Exception {
			
		List<Map> list=new ArrayList<Map>();
	
		String apiUrl=rallyApiHost+
							"/testcaseresult"+
							"?query=(TestCase.FormattedID%20=%20"+testCaseId+")"+
							"&order=Date%20desc"+
							"&fetch=true";
		
		log.info("rallyApiUrl:"+apiUrl);
		
		String responseXML=getRallyXML(apiUrl);
							
		Map testCaseResultMap=new HashMap();
		
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		

		XPath xpath = XPath.newInstance("//Object");
		List xlist = xpath.selectNodes(root);
		
		Iterator iter = xlist.iterator();
		if(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			String objId=item.getChildText("ObjectID");
			String verdict=item.getChildText("Verdict");
			
			Element ownerElement=item.getChild("Tester");
			
			String tester=NO_USER;
			String testerRef=NO_REF;
			
			if(ownerElement!=null) {
				tester=ownerElement.getAttributeValue("refObjectName");
				testerRef=ownerElement.getAttributeValue("ref");
			}
									
			testCaseResultMap.put("objId",objId);
			testCaseResultMap.put("verdict",verdict);
			testCaseResultMap.put("tester",tester);
			testCaseResultMap.put("testerRef",testerRef);
			
		}		
		
		Map taskMap=getRallyTask(taskNo);
		
		String taskObjId=(String)taskMap.get("objId");
		String currentTaskState=(String)taskMap.get("state");
		
		if(testCaseResultMap==null || testCaseResultMap.size()==0) {
		
			resultCode=RESULT_FAIL;
			resultMessage="No test case result";
			updateRallyTAState(taskObjId,IN_PROGRESS,currentTaskState);
		
		} else {
			String objId=(String)testCaseResultMap.get("objId");
			String verdict=(String)testCaseResultMap.get("verdict");
			
			log.info("testCaseResultMap="+testCaseResultMap);
			
			if("Pass".equals(verdict)) {
			
				resultCode=RESULT_OK;
				resultMessage="Test result: pass";
				updateRallyTAState(taskObjId,COMPLETED,currentTaskState);
				
			} else {
							
				resultCode=RESULT_FAIL;
				resultMessage="Test result: fail";
				updateRallyTAState(taskObjId,IN_PROGRESS,currentTaskState);
				
			}
			
		} //if(testCaseResultMap
		
	}
	
	public void checkNewTAFromRally(String filePath) {
	
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewTAFromRally(filePath,projectId);
		}
	
	}
	
    public void checkNewTAFromRally(String filePath,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
        
        log.info("file="+file.getAbsolutePath());
                
		try {
		
	        Properties checkList=new Properties();
    	    checkList.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
		
			String apiUrl=rallyApiHost+
								"/task?"+
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
				String taskNo=item.getChildText("FormattedID");
				
				Element ownerElement=item.getChild("Owner");
				
				String owner=NO_USER;
				String ownerRef=NO_REF;
				
				if(ownerElement!=null) {
					owner=ownerElement.getAttributeValue("refObjectName");
					ownerRef=ownerElement.getAttributeValue("ref");
				}
										
				map.put("objId",objId);
				map.put("name",name);
				map.put("state",state);
				map.put("taskNo",taskNo);
				map.put("ownerRef",ownerRef);
				
				list.add(map);
			}
			
			for(Map map:list) {
			
				log.debug("map="+map);
			
				String objId=(String)map.get("objId");
				String name=(String)map.get("name");
				String state=(String)map.get("state");
				String taskNo=(String)map.get("taskNo");
				String owner=(String)map.get("owner");
																
				if(!"".equals(taskNo) && !"".equals(state)) {
					
					boolean stateChanged=false;
					
					String existState=checkList.getProperty(taskNo);
					
					//Check state change
					//if(existState==null && IN_PROGRESS.equals(state) ) 
					if(existState==null && 
						(DEFINED.equals(state) || IN_PROGRESS.equals(state))) {
						stateChanged=true; //else //None->P
					}
					
					log.info("taskNo="+taskNo+" existState="+existState+" state="+state+" stateChanged="+stateChanged);

					if(stateChanged) {
						
						//Get owner's email
						
						if(NO_USER.equals(owner)) {
						
							updateRallyTAState(objId,DONT_ADD_BONITA);
						
						} else {
						
							String ownerRef=(String)map.get("ownerRef");
							String ownerEmail=NO_EMAIL;
							if(!"NO_REF".equals(ownerRef)) {
								ownerEmail=getOwnerEmail(ownerRef);
							}
							
							log.info("owner="+owner);
							log.info("ownerRef="+ownerRef);
							
							Map varMap=new HashMap();
							varMap.put("task_no",taskNo);
							
							String bonitaProcessId=confProperties.getProperty("bonita.process.xp_code.id");
							
							BonitaService bonitaService=new BonitaService(confFileName);
							String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
						
							if(bonitaId!=null && !"".equals(bonitaId)) {
								
								updateRallyTAState(objId,bonitaId);
							
								checkList.setProperty(taskNo,state);
								log.info("setProperty:"+taskNo+" "+state);
							}
							
							
						} //if("NO_USER".equals(owner))
							
					
					}
					
				}
				
				log.debug("---------------------------");
				
			}

			checkList.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception e) {
			ok = false;
			log.error("",e);
		}
		
		if(ok) {
			resultCode=RESULT_OK;
			resultMessage="Check success";
		} else {
			resultCode=RESULT_FAIL;
			resultMessage="Check failed";
		}
    
    }
    
	public void checkNewUSFromRally(String filePath) {
	
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewUSFromRally(filePath,projectId);
		}
	
	}
    
    public void checkNewUSFromRally(String filePath,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
                
    	log.debug("file="+file.getAbsolutePath());
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
    	    		
			String apiUrl=rallyApiHost+
				"/hierarchicalrequirement?"+
				"project="+rallyApiHost+"/project/"+projectId;
			
			int totalResultCount=getTotalResultCount(apiUrl);
			
			log.debug("totalResultCount="+totalResultCount);
			
			apiUrl=apiUrl+"&fetch=true&start=1&pagesize="+totalResultCount;

			log.debug("rallyApiUrl:"+apiUrl);
			
			String responseXML=getRallyXML(apiUrl);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			Document doc = bSAX.build(new StringReader(responseXML));
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

			
			for(Map map:list) {
			
				log.debug("map="+map);
			
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
					if(existState==null && 
						(DEFINED.equals(state) || IN_PROGRESS.equals(state))) {
						stateChanged=true; //else //None->P
					}
					
					//if(existState==null && DEFINED.equals(state)) 
					//	stateChanged=true; //else //None->D
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
					
					log.debug("usId="+usId+" existState="+existState+" state="+state+" stateChanged="+stateChanged);
					
					if(stateChanged) {
						
						//Get owner's email
						
						if("NO_USER".equals(owner)) {
						
							updateRallyUSState(projectId,objId,DONT_ADD_BONITA);
						
						} else {
						
							String ownerRef=(String)map.get("ownerRef");
							String ownerEmail="NO_EMAIL";
							if(!"NO_REF".equals(ownerRef)) {
								ownerEmail=getOwnerEmail(ownerRef);
							}
							
							log.debug("owner="+owner);
							log.debug("ownerRef="+ownerRef);
							
							Map varMap=new HashMap();

							//String strDescription = usId + ":" + name;
							String strDescription = usId;
							String instanceVar="{\"instance_var\": "+
							"{\"owner_email\": \""+ownerEmail+"\", "+
							"\"rally_formatted_id\": \""+usId+"\", "+
							"\"rally_obj_id\": \""+objId+"\", "+
							"\"description\": \""+strDescription+"\", "+
							"\"rally_url_link\": \""+refUrl+"\"}}";
							;
							varMap.put("instance_var",instanceVar);
							varMap.put("_description_", strDescription);;
							
							
							log.debug("confProperties="+confProperties);							
							
							String bonitaProcessId=confProperties.getProperty("bonita.process.review_rally_us_process.id");
							
							log.debug("bonitaProcessId="+bonitaProcessId);
														
							BonitaService bonitaService=new BonitaService(confFileName);
							String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
						
							if(bonitaId!=null && !"".equals(bonitaId)) {
													
								updateRallyUSState(projectId,objId,bonitaId);
								
								updateRallyUSName(objId,"N");
							
								properties.setProperty(usId,state);
								log.debug("setProperty:"+usId+" "+state);
							}
							
							
						} //if("NO_USER".equals(owner))
							
					
					}
					
				}
				
				log.debug("---------------------------");
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.debug("ERROR: "+ex.getMessage());
		}
    
    }
    
	public void checkNewUS(String filePath,String process) {
	
		String bonitaProcessId=(String)confProperties.get("bonita.process."+process+".id");
		String fileName=(String)confProperties.get("file.checklist."+process+".id");
		rallyProjectIds=(String)confProperties.get("rally.project."+process+".ids");
		
		if(bonitaProcessId==null || fileName==null || rallyProjectIds==null) {
			log.debug("bonitaProcessId="+bonitaProcessId);
			log.debug("fileName="+fileName);
			log.debug("rallyProjectIds="+rallyProjectIds);
			return;
		}
		
		String filePathName=filePath+"/"+fileName;
		String[] projectIds=rallyProjectIds.split(",");
		
		for(int i=0;i<projectIds.length;i++) {
			String projectId=projectIds[i];
			checkNewUS(filePathName,bonitaProcessId,projectId);
		}
	
	}
	
    public void checkNewUS(String filePath,String bonitaProcessId,String projectId) {
    
        boolean ok = false;
        File file=new File(filePath);
                
    	log.debug("file="+file.getAbsolutePath());
                
		try {
		
	        Properties properties=new Properties();
    	    properties.loadFromXML(new FileInputStream(file));
    	    
    	    List<Map> list=new ArrayList<Map>();
    	    		
			String apiUrl=rallyApiHost+
				"/hierarchicalrequirement?"+
				"project="+rallyApiHost+"/project/"+projectId;
			
			int totalResultCount=getTotalResultCount(apiUrl);
			
			log.debug("totalResultCount="+totalResultCount);
			
			apiUrl=apiUrl+"&fetch=true&start=1&pagesize="+totalResultCount;

			log.debug("rallyApiUrl:"+apiUrl);
			
			String responseXML=getRallyXML(apiUrl);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			Document doc = bSAX.build(new StringReader(responseXML));
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

			
			for(Map map:list) {
			
				log.debug("map="+map);
			
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
					if(existState==null && 
						(DEFINED.equals(state) || IN_PROGRESS.equals(state))) {
						stateChanged=true; //else //None->P
					}
					
					log.debug("usId="+usId+" existState="+existState+" state="+state+" stateChanged="+stateChanged);
					
					if(stateChanged) {
						
						//Get owner's email
						
						if("NO_USER".equals(owner)) {
						
							updateRallyUSState(projectId,objId,DONT_ADD_BONITA);
						
						} else {
						
							String ownerRef=(String)map.get("ownerRef");
							String ownerEmail="NO_EMAIL";
							if(!"NO_REF".equals(ownerRef)) {
								ownerEmail=getOwnerEmail(ownerRef);
							}
							
							log.debug("owner="+owner);
							log.debug("ownerRef="+ownerRef);
							
							Map varMap=new HashMap();
							
							String strDescription = usId;
							String instanceVar=""+
							"{\"owner_email\": \""+ownerEmail+"\", "+
							"\"rally_formatted_id\": \""+usId+"\", "+
							"\"rally_obj_id\": \""+objId+"\", "+
							"\"description\": \""+strDescription+"\", "+
							"\"rally_url_link\": \""+refUrl+"\"}";
							
							varMap.put("instance_var",instanceVar);
							varMap.put("_description_", strDescription);;
																					
							BonitaService bonitaService=new BonitaService(confFileName);
							String bonitaId=bonitaService.createInstance(bonitaProcessId,ownerEmail,varMap);
						
							if(bonitaId!=null && !"".equals(bonitaId)) {
													
								updateRallyUSState(projectId,objId,bonitaId);
								
								updateRallyUSName(objId,"N");
							
								properties.setProperty(usId,state);
								log.debug("setProperty:"+usId+" "+state);
							}
							
							
						} //if("NO_USER".equals(owner))
							
					
					}
					
				}
				
				log.debug("---------------------------");
				
			}

			properties.storeToXML(new FileOutputStream(file),"comment");

			ok = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.debug("ERROR: "+ex.getMessage());
		}
    
    }
    
    public void updateRallyTCState(String objId,String taskNo) {
                    
		try {
						
			String apiUrl=rallyApiHost+"/testcase/"+objId;
			
			log.info("rallyApiUrl:"+apiUrl);
			
			StringBuffer requestXML=new StringBuffer();
			
			requestXML.append("<TestCase ref=\""+apiUrl+"\">");
			requestXML.append("<TaskNo>"+taskNo+"</TaskNo>");
			requestXML.append("</TestCase>");
			
			log.info("requestXML="+requestXML);
			
			String responseXML=postRallyXML(apiUrl,requestXML.toString());
			
		} catch (Exception e) {
			log.error("",e);
		}
    
    }
    
    public void updateRallyTAState(String objId,String updateTaskState,String currentState) {
                    
		try {
		
			boolean sendEmail=false;
		
			String apiUrl=rallyApiHost+"/task/"+objId;
			
			log.info("rallyApiUrl:"+apiUrl);			
		
			StringBuffer requestXML=new StringBuffer();
			
			requestXML.append("<Task ref=\""+apiUrl+"\">");
			
			if(updateTaskState.equals(COMPLETED)) {
				
				requestXML.append("<ToDo>0</ToDo>");
				
			} else if(updateTaskState.equals(IN_PROGRESS)) {
				if(currentState.equals(DEFINED) || currentState.equals(COMPLETED)) {
					sendEmail=true;
					log.info("sendEmail="+sendEmail);
				}
			}
			
			requestXML.append("<State>"+updateTaskState+"</State>");
			requestXML.append("</Task>");
 			
 			log.info("requestXML="+requestXML);
 			
			String responseXML=postRallyXML(apiUrl,requestXML.toString());
			
		} catch (Exception e) {
			log.error("",e);
		}
    
    }
    
	public void updateRallyTAState(String rallyObjId,String bonitaInstanceUUID) {
	
		try {
				
			String apiUrl=rallyApiHost+"/task/"+rallyObjId;
			
			log.info("rallyApiUrl:"+apiUrl);
		
			StringBuffer requestXML=new StringBuffer();
		
			if(DONT_ADD_BONITA.equals(bonitaInstanceUUID)) {
			
				requestXML.append("<Task ref='"+apiUrl+"'>");
				requestXML.append("<WorkflowState>No owner assigned</WorkflowState>");
				requestXML.append("</Task>");
 			
 			} else {
				
				requestXML.append("<Task ref='"+apiUrl+"'>");
				requestXML.append("<WorkflowLink>"+bonitaInstanceUUID+"</WorkflowLink>");
				requestXML.append("</Task>");
 			
 			}
 			
 			log.info("requestXML="+requestXML);

			String responseXML=postRallyXML(apiUrl,requestXML.toString());
			
		} catch (Exception e) {
			log.error("",e);
		}
    
    }

    
	public void updateRallyUSState(String projectId,String rallyObjId,String bonitaInstanceUUID) {
	
		try {
				
			String apiUrl=rallyApiHost+"/hierarchicalrequirement/"+rallyObjId;
			
			log.info("rallyApiUrl:"+apiUrl);
		
			StringBuffer requestXML=new StringBuffer();
		
			if(DONT_ADD_BONITA.equals(bonitaInstanceUUID)) {
			
				requestXML.append("<HierarchicalRequirement ref='"+apiUrl+"'>");
				requestXML.append("<WorkflowState>No owner assigned</WorkflowState>");
				requestXML.append("</HierarchicalRequirement>");
 			
 			} else {
 			
				requestXML.append("<HierarchicalRequirement ref='"+apiUrl+"'>");
				requestXML.append("<projectId>"+projectId+"</projectId>");
				//requestXML.append("<us_object_id>"+rallyObjId+"</us_object_id>");
				requestXML.append("<usobjectid>"+rallyObjId+"</usobjectid>");
				requestXML.append("<WorkflowLink>"+bonitaInstanceUUID+"</WorkflowLink>");
				requestXML.append("</HierarchicalRequirement>");
 			
 			}
 			
 			log.info("requestXML="+requestXML);

			String responseXML=postRallyXML(apiUrl,requestXML.toString());
			
		} catch (Exception e) {
			log.error("",e);
		}
    
    }
        
	protected int getTotalResultCount(String apiUrl) {
		int count=0;
	
		try {
		
			String responseXML=getRallyXML(apiUrl);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//QueryResult");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
			
				Map map=new HashMap();
			
				Element item = (Element) iter.next();
				String totalResultCount=item.getChildText("TotalResultCount");
				count=Integer.parseInt(totalResultCount);
			}
		
		} catch(Exception e) {
			log.error("",e);
		}
		
		return count;
	}
	
	public void updateRallyUSName(String objId,String statusStr) {
	
		log.info("updateRallyUSName user="+user+" objId="+objId);
						
		try {
	
			String[] cmd={
				"python",
				"/home/atuser/workflow_connector/updateRally.py",
				objId,
				statusStr
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
						
			String responseMessage=loadStream(process.getInputStream());
			String errorMessage=loadStream(process.getErrorStream());

			int exitValue=process.waitFor();
			
			log.info("exitValue="+exitValue);
			log.info("responseMessage="+responseMessage);
			log.info("errorMessage="+errorMessage);
			
			if(exitValue!=0) {
				resultCode=RESULT_FAIL;
				resultMessage=errorMessage;
			} else {
			
				/*
				if(responseMessage!=null && responseMessage.length()>0) {
					returnMap=JsonUtil.jsonToMap(responseMessage);
				}
				*/
				
			}
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
	
	}
	
    public static String getUSFormattedID(String comment) {
    	String usNo="";

		if(comment==null)
			return "";

		Pattern p=Pattern.compile("US\\d{1,}",Pattern.CASE_INSENSITIVE);		
		Matcher m=p.matcher(comment);

		while (m.find()) {
			 usNo=m.group();
		 }

    	return usNo.toUpperCase();
    }
    
    public static String getTAFormattedID(String comment) {
    	String taNo="";

		Pattern p=Pattern.compile("TA\\d{1,}",Pattern.CASE_INSENSITIVE);		
		Matcher m=p.matcher(comment);

		while (m.find()) {
			 taNo=m.group();
		 }

    	return taNo.toUpperCase();
    }
    
    public static String getDEFormattedID(String comment) {
    	String deNo="";

		Pattern p=Pattern.compile("DE\\d{1,}",Pattern.CASE_INSENSITIVE);		
		Matcher m=p.matcher(comment);

		while (m.find()) {
			 deNo=m.group();
		 }

    	return deNo.toUpperCase();
    }
    
    /**
    * Get single US info
    */
    public Map getUserStory(String formattedID) {
    
    	Map map=new HashMap();
    	
    	try {
    
			String apiUrl=rallyApiHost+"/HierarchicalRequirement?query=(FormattedID%20=%20"+formattedID+")&fetch=true";
	
			log.info("rallyApiUrl:"+apiUrl);
	
			String responseXML=getRallyXML(apiUrl);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			if(iter.hasNext()) {
			
				Element item = (Element) iter.next();
				String objectID=item.getChildText("ObjectID");
				String name=item.getChildText("Name");
				
				String ref=item.getAttributeValue("ref");
				
				Element projectElement=item.getChild("Project");
				String projectName=projectElement.getAttributeValue("refObjectName");
	
				String link="https://rally1.rallydev.com/#/detail/userstory/"+objectID;
				map.put("formattedID",formattedID);
				map.put("objectID",objectID);
				map.put("name",name);
				map.put("ref",ref);
				map.put("link",link);
				map.put("project",projectName);
		
			}
		
		} catch(Exception e) {
			log.error("",e);
		}
				
		return map;
    }
    
	public static void main(String[] argv) {
	

	}
}  