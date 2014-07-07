package com.raritan.at.workflow.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

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

import org.json.simple.*;
import org.json.simple.parser.*;

import com.raritan.at.util.*;
import com.raritan.at.workflow.bean.*;
import static com.raritan.at.workflow.service.Constant.*;

public class ViewService extends RallyService {

	private static ViewService instance=new ViewService();
	
	private static Map statusMap=new HashMap();
		
	public ViewService() {

	}
	
	public ViewService(String confFileName) {
		super(confFileName);

	}
	
	public static ViewService getInstance() {
		return instance;
	}
	
	public List getUserList() {
		List<Map> list=new ArrayList<Map>();
                
		try {
			
			/*
			String apiUrl=rallyApiHost+"/user?query="+
				"((TeamMemberships%20%3D%20https%3A%2F%2Frally1.rallydev.com%2Fslm%2Fwebservice%2F1.34%2Fproject%2F6169133135)%20or%20"+
				"(TeamMemberships%20%3D%20https%3A%2F%2Frally1.rallydev.com%2Fslm%2Fwebservice%2F1.34%2Fproject%2F6083311244))"+
				"&fetch=true&order=Name&start=1&pagesize=100";
			*/
			
			String apiUrl=rallyApiHost+"/user?query=(Disabled%20=%20false)"+
				"&fetch=true&order=Name&start=1&pagesize=100";

			log.info("apiUrl="+apiUrl);
			
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
				
				String userRef=item.getAttribute("ref").getValue();
				String userName=item.getAttribute("refObjectName").getValue();
				String userObjectId=item.getChildText("ObjectID");
									
				map.put("userRef",userRef);
				map.put("userObjectId",userObjectId);
				map.put("userName",userName);
			
				list.add(map);
				
			}
			
		} catch (Exception ex) {
			log.error("",ex);
		}
    
    	return list;
    }
    
    
	public List getUserListAT() {
	
		List<Map> list=new ArrayList<Map>();
		
		String[][] data={
			{"3537255778","Alex Chen"},
			{"2110989338","Brian Wang"},
			{"3537640807","David Hsieh"},
			{"5764816553","James Yu"},
			{"3756404948","K.C."},
			{"2110994764","Neil Weinstock"},
			{"6797798390","Owen Chen"},
			{"3831206627","Randy Chen"},
			{"6312460903","Tony Shen"},
			{"2110993498","Yee Liaw"}
		};
		
		for(int i=0;i<data.length;i++) {
		
			Map map=new HashMap();
		
			String userRef=data[i][0];
			String userName=data[i][1];
		
			map.put("userObjectId",userRef);
			map.put("userName",userName);
			
			list.add(map);
		}
		
		return list;
	}
	
	public List getUserTimeSpentByDate(String userObjectId,String startDate,String endDate) {
		List list=new ArrayList();
		
		log.info("userObjectId="+userObjectId);
		log.info("startDate="+startDate);
		log.info("endDate="+endDate);
                
		try {
    	    				
			String apiUrl=rallyApiHost+"/timeentryvalue?query=((TimeEntryItem.User.ObjectId%20=%20"+userObjectId+")"+
							"%20and%20((DateVal%20%3E=%20"+startDate+")%20and%20(DateVal%20%3C=%20"+endDate+")))"+
							"&start=1&pagesize=100&fetch=true";
							
			log.info("apiUrl="+apiUrl);
							
			String responseXML=getRallyXML(apiUrl);
			
			log.info("responseXML="+responseXML);
				
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
			
				//Map map=new HashMap();
			
				Element item = (Element) iter.next();
				
				String hours=item.getChildText("Hours");
				
				Element timeEntryItemElement=item.getChild("TimeEntryItem");
				String timeEntryItemRef=timeEntryItemElement.getAttributeValue("ref");
				
				Map map=getUserStoryTaskMap(timeEntryItemRef);

				String checkTaskId=(String)map.get("taskFormattedId");
				
				boolean isExist=false;
				for(int i=0;i<list.size();i++) {
					Map existMap=(Map)list.get(i);
					
					log.info("existMap="+existMap);
					
					String existTaskId=(String)existMap.get("taskFormattedId");
					
					log.info("existTaskId="+existTaskId);
					log.info("checkTaskId="+checkTaskId);
					
					if(existTaskId!=null && existTaskId.equals(checkTaskId)) {
						isExist=true;
						String existHours=(String)existMap.get("hours");
						double eHour=0.0D;
						if(!"".equals(existHours)) {
							eHour=Double.parseDouble(existHours);
						}
						double nHour=0.0D;
						
						if(!"".equals(hours)) {
							nHour=Double.parseDouble(hours);
						}
						
						log.info("nHour="+nHour);
						log.info("eHour="+eHour);
						
						nHour+=eHour;
						log.info("2 nHour="+nHour);
						existMap.put("hours",""+nHour);

						break;
					}
					
				}
					
				if(!isExist) {
					map.put("hours",hours);
					list.add(map);
				}
				
				log.info("hours="+hours);
				log.info("timeEntryItemRef="+timeEntryItemRef);
			
				//list.add(map);
				
			}
			
			Collections.sort(list,new Comparator<Map<String, String>>() {
				public int compare(Map<String, String> m1, Map<String, String> m2) {
					if(m1.get("projectName")==null || m2.get("projectName")==null) return -1;
					return m1.get("projectName").compareTo(m2.get("projectName"));
				}
			});
								
			//Sum up the total time
			double totalTaskEstimate=0.0D;
			double totalTaskRemaining=0.0D;
			double totalHours=0.0D;
			for(int i=0;i<list.size();i++) {
				Map map=(Map)list.get(i);
				
				log.info("taskEstimate="+(String)map.get("taskEstimate"));
				log.info("taskRemaining="+(String)map.get("taskRemaining"));
				log.info("hours="+(String)map.get("hours"));
				
				log.info("map=="+map);
				
				try {
					double taskEstimate=Double.parseDouble((String)map.get("taskEstimate"));
					double taskRemaining=Double.parseDouble((String)map.get("taskRemaining"));
					double hours=Double.parseDouble((String)map.get("hours"));
					
					totalTaskEstimate+=taskEstimate;
					totalTaskRemaining+=taskRemaining;
					totalHours+=hours;
				} catch (Exception e) {
					log.info("ERROR in parsing number"+e);
				}
			
			}
			
			Map firstMap=new HashMap();
			
			firstMap.put("taskFormattedId","");
			firstMap.put("taskName","");
			firstMap.put("taskState","");
			firstMap.put("owner","");
			firstMap.put("taskEstimate",""+totalTaskEstimate);
			firstMap.put("taskRemaining",""+totalTaskRemaining);
			firstMap.put("hours",""+totalHours);
			firstMap.put("projectName","");
			firstMap.put("iterationName","");
			
			list.add(0,firstMap);
			
		} catch (Exception ex) {
			log.error("",ex);
		}
    
    	return list;
    }
    
    public Map getUserStoryTaskMap(String timeEntryItemRef) throws Exception {
    	Map taskMap=new HashMap();
    	
		String[] objectIdArr=timeEntryItemRef.split("/");
		String objectId=objectIdArr[objectIdArr.length-1];
		
		log.info("objectId="+objectId);
		
		String apiURL="https://rally1.rallydev.com/slm/webservice/1.34/adhoc";
		
		String requestJSON="{"+
			"\"timeentryitem\" : \"/timeentryitem?query=(ObjectID%20=%20"+objectId+")&fetch=true\","+
			"\"task\" : \"/task?query=(ObjectID%20=%20${timeentryitem.Task.ObjectID})&fetch=true\","+
			"\"userstory\" : \"/hierarchicalrequirement?query=(ObjectID%20=%20${task.WorkProduct.ObjectID})&fetch=true\","+
			"\"defect\" : \"/defect?query=(ObjectID%20=%20${task.WorkProduct.ObjectID})&fetch=true\""+
		"}";

		log.info("apiURL="+apiURL);
		log.info("requestJSON="+requestJSON);
	
		String responseJSON=postRallyXML(apiURL,requestJSON);
		
		//Bypass"%;" to avoid exception
		responseJSON=responseJSON.replace("%;",";");
		responseJSON=responseJSON.replace("%","");
		
		Map jsonMap=JsonUtil.jsonToMap(responseJSON);
				
		String usRef="";
		String usName="";
		String usFormattedId="";
		String usPlanEstimate="";
		String usTaskEstimateTotal="";
		String usTaskRemainingTotal="";
		String usState="";
		String usOwner="";
		
		Map usMap=new HashMap();
		
		//Get user story info
		JSONObject userstoryMap=(JSONObject)jsonMap.get("userstory");
		JSONArray userstoryArray=(JSONArray)userstoryMap.get("Results");
		if(userstoryArray==null || userstoryArray.size()==0) {
			userstoryMap=(JSONObject)jsonMap.get("defect");
			userstoryArray=(JSONArray)userstoryMap.get("Results");
		} 

		if(userstoryArray!=null && userstoryArray.size()>0) {
			JSONObject userstoryInfo=(JSONObject)userstoryArray.get(0);
			//log.info("userstoryInfo="+userstoryInfo);
			usRef=(userstoryInfo.get("_ref")).toString();
			usFormattedId=(userstoryInfo.get("FormattedID")).toString();
			usName=(userstoryInfo.get("Name")).toString();
			usState=(userstoryInfo.get("ScheduleState")).toString();
			
			if(userstoryInfo.get("PlanEstimate")!=null) 
				usPlanEstimate=(userstoryInfo.get("PlanEstimate")).toString();
				
			if(userstoryInfo.get("TaskEstimateTotal")!=null) 
				usTaskEstimateTotal=(userstoryInfo.get("TaskEstimateTotal")).toString();
			
			if(userstoryInfo.get("TaskRemainingTotal")!=null)
				usTaskRemainingTotal=(userstoryInfo.get("TaskRemainingTotal")).toString();
				
			JSONObject ownerMap=(JSONObject)userstoryInfo.get("Owner");
			if(ownerMap!=null) {
				usOwner=(String)ownerMap.get("_refObjectName");
				if(usOwner==null) {
					usOwner="";
				}
			}
				
		}		
		
		Map usDetailMap=new HashMap();
		
		usDetailMap.put("usFormattedId",usFormattedId);
		usDetailMap.put("usName",usName);
		usDetailMap.put("usPlanEstimate",usPlanEstimate);
		usDetailMap.put("usTaskEstimateTotal",usTaskEstimateTotal);
		usDetailMap.put("usTaskRemainingTotal",usTaskRemainingTotal);
		usDetailMap.put("usOwner",usOwner);
		usDetailMap.put("usState",usState);
		
		usMap.put(usRef,usDetailMap);
		
		//log.info("usMap="+usMap);
		
		String taskObjId="";
		String taskFormattedId="";
		String taskName="";
		String estimate="";
		String toDo="";
		String taskState="";
		String taskOwner="";
		String projectName="";
		String iterationName="";
		String workProductRef="";
		
		List taskList=new ArrayList();
		
		//Get task info
		JSONObject taskJsonMap=(JSONObject)jsonMap.get("task");
		JSONArray taskArray=(JSONArray)taskJsonMap.get("Results");
		if(taskArray!=null && taskArray.size()>0) {
		
			for(int i=0;i<taskArray.size();i++) {
			
				JSONObject taskInfo=(JSONObject)taskArray.get(0);
				//log.info("taskMap="+taskMap);
				//log.info("taskInfo="+taskInfo);
				
				taskObjId=(taskInfo.get("ObjectID")).toString();
				taskFormattedId=(taskInfo.get("FormattedID")).toString();
				taskState=(taskInfo.get("State")).toString();
				
				Object taskNameObj=taskInfo.get("Name");
				taskName=taskNameObj==null ? "" : taskNameObj.toString();
				
				Object estimateObject=taskInfo.get("Estimate");
				estimate=estimateObject==null ? "" : estimateObject.toString();
				
				Object toDoObject=taskInfo.get("ToDo");
				toDo=toDoObject==null ? "" : toDoObject.toString();
				
				JSONObject ownerMap=(JSONObject)taskInfo.get("Owner");
				//log.info("ownerMap="+ownerMap);
				if(ownerMap!=null) {
					taskOwner=(String)ownerMap.get("_refObjectName");
					if(taskOwner==null) {
						taskOwner="";
					}
				}
				
				JSONObject workProductMap=(JSONObject)taskInfo.get("WorkProduct");
				//log.info("workProductMap="+workProductMap);
				if(workProductMap!=null) {
					workProductRef=(String)workProductMap.get("_ref");
					if(workProductRef==null) {
						workProductRef="";
					}
				}
				
				JSONObject projectMap=(JSONObject)taskInfo.get("Project");
				//log.info("projectMap="+projectMap);
				if(projectMap!=null) {
					projectName=(String)projectMap.get("_refObjectName");
					if(projectName==null) {
						projectName="";
					}
				}
				
				JSONObject iterationMap=(JSONObject)taskInfo.get("Iteration");
				//log.info("iterationMap="+iterationMap);
				if(iterationMap!=null) {
					iterationName=(String)iterationMap.get("_refObjectName");
					if(iterationName==null) {
						iterationName="";
					}
				}
				
				taskMap.put("taskFormattedId",taskFormattedId);
				taskMap.put("taskName",taskName);
				taskMap.put("taskState",taskState);
				taskMap.put("owner",taskOwner);
				taskMap.put("taskEstimate",estimate);
				taskMap.put("taskRemaining",toDo);
				taskMap.put("projectName",projectName);
				taskMap.put("iterationName",iterationName);
				
				Map map=(Map)usMap.get(workProductRef);
				taskMap.put("usName",map.get("usFormattedId")+" "+map.get("usName"));
								
				log.info("taskMap="+taskMap);
				
			} //for taskArray
				
		}
    
    	return taskMap;
    }
    
	public List getProjectList() {
		List<Map> list=new ArrayList<Map>();
                
		try {
			
			String apiUrl=rallyApiHost+
				"/project?"+
				"fetch=true&order=Name&start=1&pagesize=200";
							
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
								
				map.put("objId",objId);
				map.put("name",name);
				map.put("state",state);
			
				list.add(map);
				
			}
			
		} catch (Exception ex) {
			log.error("ERROR: ",ex);
		}
    
    	return list;
    }
	
	public List getIterationList(String projectId) {
		List<Map> list=new ArrayList<Map>();
                
		try {

			String apiUrl=rallyApiHost+
				"/iteration?"+
				"project="+rallyApiHost+"/project/"+projectId+"&fetch=true&order=Name%20desc&start=1&pagesize=100";
				
			String checkProjectRef=rallyApiHost+"/project/"+projectId;
			
			log.info("rallyApiUrl:"+apiUrl);
			log.info("checkProjectRef:"+checkProjectRef);
			
			String responseXML=getRallyXML(apiUrl);
			
			SimpleDateFormat ISO8601FORMAT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			Date currentDate=new Date();
				
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
				
				String startDateStr=item.getChildText("StartDate");
				String endDateStr=item.getChildText("EndDate");
				
				Date startDate = ISO8601FORMAT.parse(startDateStr);
				Date endDate = ISO8601FORMAT.parse(endDateStr);
				
				boolean isCurrent=false;

				int startCheck=startDate.compareTo(currentDate);
				int endCheck=endDate.compareTo(currentDate);
				
				if(startCheck<0 && endCheck>0) {
					isCurrent=true;
				}
				
				log.info("name="+name+" isCurrent="+isCurrent);
				
				String releaseRef=item.getAttribute("ref").getValue();
				
				//In child project, parent object have to be filiered 				
				Element projectElement=item.getChild("Project");
				String projectRef=projectElement.getAttributeValue("ref");
				
				if(projectRef.equals(checkProjectRef)) {
					
					map.put("objId",objId);
					map.put("rallyRef",releaseRef);
					map.put("name",name);
					map.put("state",state);
					map.put("isCurrent",""+isCurrent);
				
					list.add(map);
				}
				
			}
			
			log.info("-----------");
			
		} catch (Exception ex) {
			log.error("ERROR: ",ex);
		}
    
    	return list;
    }
    
	//public List getUserStoryList(String sessionId,String iterationId,ServletOutputStream out) {
	public List getUserStoryList(String sessionId,String iterationId,PrintWriter out) {
	
		List<Map> list=new ArrayList<Map>();
		
		statusMap.put(sessionId,"0");
                
		try {

			String apiURL=rallyApiHost+"/hierarchicalrequirement?"+
									"query=(Iteration%20=%20"+rallyApiHost+"/iteration/"+iterationId+")&fetch=true&start=1&pagesize=100";
																		
			log.info("getUserStoryList apiURL="+apiURL);
			
			String responseXML=getRallyXML(apiURL);
			
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
			
			XPath xpath = XPath.newInstance("//Object");
			List xlist = xpath.selectNodes(root);
			
			int totalSteps=xlist.size()+1;
			int currentStep=0;
			
			List taskRefLink=new ArrayList();
			
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				double totalTimeSpent=0.0D;
							
				Map map=new HashMap();
			
				Element item = (Element) iter.next();
				String objId=item.getChildText("ObjectID");
				String name=item.getChildText("Name");
				String planEstimate=item.getChildText("PlanEstimate");
				String formattedId=item.getChildText("FormattedID");
				
				String taskActualTotal=item.getChildText("TaskActualTotal");
				String taskEstimateTotal=item.getChildText("TaskEstimateTotal");
				String taskRemainingTotal=item.getChildText("TaskRemainingTotal");
				String scheduleState=item.getChildText("ScheduleState");
				
				Element ownerElement=item.getChild("Owner");
				
				String owner="";
				String ownerRef="";
				
				if(ownerElement!=null) {
					owner=ownerElement.getAttributeValue("refObjectName");
				}				
				
				Element taskElements=item.getChild("Tasks");
				//List taskElementList=taskElements.getContent();
				List taskElementList=taskElements.getChildren();
				
				List taskList=new ArrayList();
				
				log.info("taskElements.getChildren="+taskElements);
				log.info("taskList="+taskElementList);
										
				for(int i=0;i<taskElementList.size();i++) {
					Element taskElement=(Element)taskElementList.get(i);
				
					String taskRef=taskElement.getAttributeValue("ref");
					
					String[] objectIdArr=taskRef.split("/");
					String objectId=objectIdArr[objectIdArr.length-1];
					
					log.info("objectId="+objectId);

					//Map taskMap=getTaskMap(taskRef);
					Map taskMap=getTaskMapBatch(objectId);

					double taskTimeSpentTotal=Double.parseDouble((String)taskMap.get("taskTimeSpentTotal"));
					totalTimeSpent+=taskTimeSpentTotal;
					taskList.add(taskMap);
				}
				
				map.put("type","userstory");
				map.put("formattedId",formattedId);
				map.put("name",name);
				map.put("taskStatus",scheduleState);
				map.put("owner",owner);
				map.put("planEstimate",planEstimate);
				map.put("taskEstimateTotal",taskEstimateTotal);
				map.put("taskRemainingTotal",taskRemainingTotal);
				map.put("taskTimeSpentTotal",""+totalTimeSpent);
			
				list.add(map);
				list.addAll(taskList);
				
				++currentStep;
				double percentage=100.0D*currentStep/totalSteps;
				String status=""+Math.round(percentage);
				statusMap.put(sessionId,status);
				
				out.println("<script>parent.updateProcessStatus('"+status+"%')</script>"+status);
				out.flush();
				log.info("out.flush..."+status);
				
				//log.info("status="+status+" sessionId="+sessionId);
				//log.info("L1 statusMap="+statusMap+" "+statusMap.hashCode());
				
			}
			
			double planEstimate=0.0D;
			double taskEstimateTotal=0.0D;
			double taskRemainingTotal=0.0D;
			double taskTimeSpentTotal=0.0D;
			Map iterationMap=new HashMap();
			for(Map map:list) {
				String type=(String)map.get("type");
				
				String planEstimateStr=(String)map.get("planEstimate");
				
				log.info("planEstimateStr="+planEstimateStr);
				
				if("userstory".equals(type)) {
					
					if(planEstimateStr!=null) {
						planEstimate+=Double.parseDouble(planEstimateStr);
					}
					taskEstimateTotal+=Double.parseDouble((String)map.get("taskEstimateTotal"));
					taskRemainingTotal+=Double.parseDouble((String)map.get("taskRemainingTotal"));
					taskTimeSpentTotal+=Double.parseDouble((String)map.get("taskTimeSpentTotal"));
				}
			
			}
			
			apiURL=rallyApiHost+
						"/iteration/"+iterationId+"?fetch=true";
			log.info("iteration apiURL="+apiURL);
			responseXML=getRallyXML(apiURL);
			
			bSAX = new org.jdom.input.SAXBuilder(); 
			doc = bSAX.build(new StringReader(responseXML));
			root = doc.getRootElement(); 		
			
			xpath = XPath.newInstance("//Iteration");
			xlist = xpath.selectNodes(root);
			
			String projName="";
			String iterName="";
			String iterState="";
			
			iter = xlist.iterator();
			while(iter.hasNext()) {
				Element item = (Element) iter.next();

				iterName=item.getChildText("Name");
				iterState=item.getChildText("State");
				Element projElement=item.getChild("Project");
				projName=projElement.getAttributeValue("refObjectName");
				
			}
			
			iterationMap.put("type","iteration");
			iterationMap.put("formattedId","");
			iterationMap.put("name",projName+" - "+iterName);
			iterationMap.put("taskStatus",iterState);
			iterationMap.put("owner","");
			
			iterationMap.put("planEstimate",""+planEstimate);
			iterationMap.put("taskEstimateTotal",""+taskEstimateTotal);
			iterationMap.put("taskRemainingTotal",""+taskRemainingTotal);
			iterationMap.put("taskTimeSpentTotal",""+taskTimeSpentTotal);
			
			list.add(0,iterationMap);
			
			statusMap.put(sessionId,"100");
			
			log.info("L2 statusMap="+statusMap);
			
			log.info("L2 verify="+getProcessStatus(sessionId));
			log.info("-----------");
			
			//String jsonData=JsonUtil.encodeObj(list);
			String jsonData = JSONValue.toJSONString(list);
			
			out.println("<script>parent.tableResult="+jsonData+"</script>");
			out.println("<script>parent.showTableResult()</script>");
			
		} catch (Exception ex) {
			log.error("ERROR: ",ex);
		}
    
    	return list;
    }

	/*
	public Map getTaskMap(String taskRef) throws Exception {
	
		Map map=new HashMap();
	
		String responseXML=getRallyXML(taskRef);
	
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		
	
		XPath xpath = XPath.newInstance("//Task");
		List xlist = xpath.selectNodes(root);
	
		Iterator iter = xlist.iterator();
		if(iter.hasNext()) {
			Element taskElement=(Element)iter.next();
		
			String taskObjId=taskElement.getChildText("ObjectID");
			String taskFormattedId=taskElement.getChildText("FormattedID");
			String taskName=taskElement.getChildText("Name");
			String estimate=taskElement.getChildText("Estimate");
			String toDo=taskElement.getChildText("ToDo");
			String taskState=taskElement.getChildText("State");
			String taskActuals=taskElement.getChildText("Actuals");
			
			if(taskActuals==null) 
				taskActuals="";
			
			Element ownerElement=taskElement.getChild("Owner");
			String taskOwner="";
			if(ownerElement!=null) {
				taskOwner=ownerElement.getAttributeValue("refObjectName");
			}
		
			//double timeSpent=getTimeSpent(taskFormattedId);
			double timeSpent=getTimeSpentBatch(taskFormattedId);
			//double timeSpent=0.0D;
						
			map.put("type","task");
			map.put("formattedId",taskFormattedId);
			map.put("name",taskName);
			map.put("taskStatus",taskState);
			map.put("owner",taskOwner);
			map.put("taskEstimateTotal",estimate);
			map.put("taskRemainingTotal",toDo);
			map.put("taskTimeSpentTotal",""+timeSpent);
			
		}
		
		return map;
	}

	
	public double getTimeSpent(String taskFormattedId) throws Exception {
		double timeSpent=0D;
	
		String apiURL="https://rally1.rallydev.com/slm/webservice/1.31/timeentryitem?"+
								"query=(Task.FormattedID = "+taskFormattedId+")&fetch=true";
	
		String responseXML=getRallyXML(apiURL);
	
		org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
		org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
		Element root = doc.getRootElement(); 		
		
		XPath xpath = XPath.newInstance("//Object/Values/TimeEntryValue");
		List xlist = xpath.selectNodes(root);
		
		List taskRefLink=new ArrayList();
		
		Iterator iter = xlist.iterator();
		while(iter.hasNext()) {
		
			Element item = (Element) iter.next();
			
			String timeValueRef=item.getAttributeValue("ref");
			
			log.info("timeValueRef="+timeValueRef);
		
			responseXML=getRallyXML(timeValueRef);
		
			org.jdom.input.SAXBuilder bSAX2 = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc2 = bSAX2.build(new StringReader(responseXML));
			Element root2 = doc2.getRootElement(); 		
			
			XPath xpath2 = XPath.newInstance("//TimeEntryValue");
			List xlist2 = xpath2.selectNodes(root2);
					
			Iterator iter2 = xlist2.iterator();
			while(iter2.hasNext()) {
				Element item2 = (Element) iter2.next();
	
				String hours=item2.getChildText("Hours");
				
				log.info("hours="+hours);
				timeSpent+=Double.parseDouble(hours);
				
			}
			
			
		}
	
		return timeSpent;
	}
	*/
	
	public Map getTaskMapBatch(String objectId) throws Exception {
	
		Map map=new HashMap();
		
		String apiURL="https://rally1.rallydev.com/slm/webservice/1.34/adhoc";
		
		String requestJSON="{"+
			"\"task\" : \"/task?query=(ObjectID%20=%20"+objectId+")&fetch=true\","+
			"\"userstory\" : \"/hierarchicalrequirement?query=(ObjectID%20=%20${task.WorkProduct.ObjectID})&fetch=FormattedID\","+
			"\"timeentryitem\":\"/timeentryitem?query=(Task.ObjectID%20=%20"+objectId+")&fetch=Values\","+
			"\"timespent\":\"${timeentryitem.Values.Hours}\""+
		"}";
			
		log.info("apiURL="+apiURL);
		log.info("requestJSON="+requestJSON);
	
		String responseJSON=postRallyXML(apiURL,requestJSON);
		
		//log.info("responseJSON="+responseJSON);
		
		//Map jsonMap=JsonUtil.jsonToMap(responseJSON);
		JSONParser parser=new JSONParser();
		Map jsonMap=(Map)parser.parse(responseJSON);
		
		//log.info("jsonMap="+jsonMap);
		
		String taskObjId="";
		String taskFormattedId="";
		String taskName="";
		String estimate="";
		String toDo="";
		String taskState="";
		String taskOwner="";
		String userstoryFormattedId="";
		
		//Get task info
		JSONObject taskMap=(JSONObject)jsonMap.get("task");
		JSONArray taskArray=(JSONArray)taskMap.get("Results");
		if(taskArray!=null && taskArray.size()>0) {
			JSONObject taskInfo=(JSONObject)taskArray.get(0);
			//log.info("taskMap="+taskMap);
			//log.info("taskInfo="+taskInfo);
			
			taskObjId=(taskInfo.get("ObjectID")).toString();
			taskFormattedId=(taskInfo.get("FormattedID")).toString();
			taskState=(taskInfo.get("State")).toString();
			
			Object taskNameObj=taskInfo.get("Name");
			taskName=taskNameObj==null ? "" : taskNameObj.toString();
			
			Object estimateObject=taskInfo.get("Estimate");
			estimate=estimateObject==null ? "" : estimateObject.toString();
			
			Object toDoObject=taskInfo.get("ToDo");
			toDo=toDoObject==null ? "" : toDoObject.toString();
			
			JSONObject ownerMap=(JSONObject)taskInfo.get("Owner");
			log.info("ownerMap="+ownerMap);
			if(ownerMap!=null) {
				taskOwner=(String)ownerMap.get("_refObjectName");
				if(taskOwner==null) {
					taskOwner="";
				}
			}
		}
		
		//Get user story info
		JSONObject userstoryMap=(JSONObject)jsonMap.get("userstory");
		JSONArray userstoryArray=(JSONArray)userstoryMap.get("Results");
		if(userstoryArray!=null && userstoryArray.size()>0) {
			JSONObject userstoryInfo=(JSONObject)userstoryArray.get(0);
			
			userstoryFormattedId=(userstoryInfo.get("FormattedID")).toString();
			log.info("userstoryFormattedId="+userstoryFormattedId);
		}
		
		//Calculate timeSpent
		JSONArray timeSpentList=(JSONArray)jsonMap.get("timespent");
		log.info("timeSpentList="+timeSpentList);
		
		double timeSpent=0.0;
		for(int i=0;i<timeSpentList.size();i++) {
			String timeSpentString=(String)timeSpentList.get(i);
			
			if(timeSpentString!=null) {
				timeSpent+=Double.parseDouble(timeSpentString);
			}
			
		}
		
		map.put("type","task");
		map.put("formattedId",taskFormattedId);
		map.put("usId",userstoryFormattedId);
		map.put("name",taskName);
		map.put("taskStatus",taskState);
		map.put("owner",taskOwner);
		map.put("taskEstimateTotal",estimate);
		map.put("taskRemainingTotal",toDo);
		map.put("taskTimeSpentTotal",""+timeSpent);
	
	
		
		return map;
	}
	
	/*
	public double getTimeSpentBatch(String taskFormattedId) throws Exception {
		double timeSpent=0D;
								
		String apiURL="https://rally1.rallydev.com/slm/webservice/1.34/adhoc";
								
		String requestJSON="{"+
			"\"timeentryitem\":\"/timeentryitem?query=(Task.FormattedID = "+taskFormattedId+")&fetch=Values\","+
			"\"timespent\":\"${timeentryitem.Values.Hours}\""+
		"}";
	
		log.info("apiURL="+apiURL);
		log.info("requestJSON="+requestJSON);
	
		String responseXML=postRallyXML(apiURL,requestJSON);
		
		log.info("responseXML="+responseXML);
	
		return timeSpent;
	}
	*/
	
	public double getTimeSpentByTask(String taskNo) throws Exception {
	
		double timeSpent=0.0D;
		
		String apiURL="https://rally1.rallydev.com/slm/webservice/1.34/adhoc";
		
		String requestJSON="{"+
			"\"task\" : \"/task?query=(FormattedID%20=%20"+taskNo+")&fetch=true\","+
			"\"timeentryitem\":\"/timeentryitem?query=(Task.FormattedID%20=%20"+taskNo+")&fetch=Values\","+
			"\"timespent\":\"${timeentryitem.Values.Hours}\""+
		"}";
			
		log.info("apiURL="+apiURL);
		log.info("requestJSON="+requestJSON);
	
		String responseJSON=postRallyXML(apiURL,requestJSON);
		
		//log.info("responseJSON="+responseJSON);
		
		//Map jsonMap=JsonUtil.jsonToMap(responseJSON);
		JSONParser parser=new JSONParser();
		Map jsonMap=(Map)parser.parse(responseJSON);
		
		/*
		//log.info("jsonMap="+jsonMap);
		
		String taskObjId="";
		String taskFormattedId="";
		String taskName="";
		String estimate="";
		String toDo="";
		String taskState="";
		String taskOwner="";
		String userstoryFormattedId="";
		
		//Get task info
		JSONObject taskMap=(JSONObject)jsonMap.get("task");
		JSONArray taskArray=(JSONArray)taskMap.get("Results");
		if(taskArray!=null && taskArray.size()>0) {
			JSONObject taskInfo=(JSONObject)taskArray.get(0);
			//log.info("taskMap="+taskMap);
			//log.info("taskInfo="+taskInfo);
			
			taskObjId=(taskInfo.get("ObjectID")).toString();
			taskFormattedId=(taskInfo.get("FormattedID")).toString();
			taskState=(taskInfo.get("State")).toString();
			
			Object taskNameObj=taskInfo.get("Name");
			taskName=taskNameObj==null ? "" : taskNameObj.toString();
			
			Object estimateObject=taskInfo.get("Estimate");
			estimate=estimateObject==null ? "" : estimateObject.toString();
			
			Object toDoObject=taskInfo.get("ToDo");
			toDo=toDoObject==null ? "" : toDoObject.toString();
			
			JSONObject ownerMap=(JSONObject)taskInfo.get("Owner");
			log.info("ownerMap="+ownerMap);
			if(ownerMap!=null) {
				taskOwner=(String)ownerMap.get("_refObjectName");
				if(taskOwner==null) {
					taskOwner="";
				}
			}
		}
		
		//Get user story info
		JSONObject userstoryMap=(JSONObject)jsonMap.get("userstory");
		JSONArray userstoryArray=(JSONArray)userstoryMap.get("Results");
		if(userstoryArray!=null && userstoryArray.size()>0) {
			JSONObject userstoryInfo=(JSONObject)userstoryArray.get(0);
			
			userstoryFormattedId=(userstoryInfo.get("FormattedID")).toString();
			log.info("userstoryFormattedId="+userstoryFormattedId);
		}
		*/
		
		//Calculate timeSpent
		JSONArray timeSpentList=(JSONArray)jsonMap.get("timespent");
		log.info("timeSpentList="+timeSpentList);
		
		//double timeSpent=0.0;
		for(int i=0;i<timeSpentList.size();i++) {
			String timeSpentString=(String)timeSpentList.get(i);
			
			if(timeSpentString!=null) {
				timeSpent+=Double.parseDouble(timeSpentString);
			}
			
		}	
		
		return timeSpent;
	}
	
	public StringBuilder listToCSVForUserView(List list) {
		StringBuilder csv=new StringBuilder();
				
		csv.append("Project,Iteration,Work Product,Name,State,Owner,Task Estimate,Task Remaining,Time Spent\n");
			
		for(int i=0;i<list.size();i++) {
			Map map=(Map)list.get(i);
			String projectName=(String)map.get("projectName");
			String iterationName=(String)map.get("iterationName");
			String formattedId=(String)map.get("taskFormattedId");
			String taskName=(String)map.get("taskName");
			String taskState=(String)map.get("taskState");
			String owner=(String)map.get("owner");
			String taskEstimate=(String)map.get("taskEstimate");
			String taskRemaining=(String)map.get("taskRemaining");
			String taskTimeSpent=(String)map.get("hours");
			
			if(taskEstimate==null) {
				taskEstimate="";
			}
			
			if(taskRemaining==null) {
				taskRemaining="";
			}

			csv.append(""+projectName+",");
			csv.append("\""+iterationName+"\",");
			csv.append(formattedId+",");
			csv.append(taskName+",");
			csv.append(taskState+",");
			csv.append(owner+",");
			csv.append(taskEstimate+",");
			csv.append(taskRemaining+",");
			csv.append(taskTimeSpent+"\n");
		}
	
		return csv;
	}
	
	public StringBuilder listToCSV(List list) {
		StringBuilder csv=new StringBuilder();
		csv.append("Work Product,,Name,State,Owner,Plan Estimate,Task Estimate,Task Remaining,Time Spent\n");
			
		for(int i=0;i<list.size();i++) {
			Map map=(Map)list.get(i);
			String type=(String)map.get("type");
			String formattedId=(String)map.get("formattedId");
			String name=(String)map.get("name");
			String taskStatus=(String)map.get("taskStatus");
			String owner=(String)map.get("owner");
			String planEstimate=(String)map.get("planEstimate");
			String taskEstimateTotal=(String)map.get("taskEstimateTotal");
			String taskRemainingTotal=(String)map.get("taskRemainingTotal");
			String taskTimeSpentTotal=(String)map.get("taskTimeSpentTotal");
			
			if("task".equals(type)) {
				csv.append(","+formattedId+",");
			} else if("userstory".equals(type)) {
				csv.append(formattedId+",,");
			} else if("iteration".equals(type)) {
				csv.append(",,");
			}
			
			if(planEstimate==null) {
				planEstimate="";
			}
			
			if(taskEstimateTotal==null) {
				taskEstimateTotal="";
			}
			
			if(taskRemainingTotal==null) {
				taskRemainingTotal="";
			}
			
			csv.append("\""+name+"\",");
			csv.append(taskStatus+",");
			csv.append(owner+",");
			csv.append(planEstimate+",");
			csv.append(taskEstimateTotal+",");
			csv.append(taskRemainingTotal+",");
			csv.append(taskTimeSpentTotal+"\n");
		}
	
		return csv;
	}
	
	public String getProcessStatus(String sessionId) {
		String status=(String)statusMap.get(sessionId);
		//log.info("M statusMap="+statusMap+" "+statusMap.hashCode());
		log.info("status="+status+" sessionId="+sessionId);
		if(status==null) {
			return "0%";
		} else {
			return status+"%";
		}
	}

}