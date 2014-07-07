package com.raritan.at.workflow.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import org.json.simple.*;
import org.json.simple.parser.*;
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

import javax.security.auth.login.*;
import org.ow2.bonita.light.*;
import org.ow2.bonita.facade.*;
import org.ow2.bonita.facade.runtime.*;
import org.ow2.bonita.facade.uuid.*;
import org.ow2.bonita.facade.identity.*;
import org.ow2.bonita.facade.def.majorElement.*;
import org.ow2.bonita.facade.paging.*;
import org.ow2.bonita.util.*;
import org.ow2.bonita.facade.exception.*;

import com.raritan.at.workflow.bean.*;
import com.raritan.at.util.*;
import static com.raritan.at.workflow.service.Constant.*;

public class BonitaService extends BaseService {
	
	protected IdentityAPI identityAPI;
	protected ManagementAPI managementAPI;
	protected RuntimeAPI runtimeAPI;
	protected QueryDefinitionAPI queryDefinitionAPI;
	protected QueryRuntimeAPI queryRuntimeAPI;

	protected int fromIndex=0;
	protected int pageSize=20;
	protected String sortOrder="DEFAULT";

	public BonitaService() {
	
	}
	
	public BonitaService(String confFileName) {
		super(confFileName);
		
		identityAPI = AccessorUtil.getIdentityAPI();
		managementAPI = AccessorUtil.getManagementAPI();
		runtimeAPI = AccessorUtil.getRuntimeAPI();
		queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
		queryDefinitionAPI=AccessorUtil.getQueryDefinitionAPI();
	}
	
	public void setPageAndSortOrder(int fromIndex,int pageSize,String sortOrder) {
		this.fromIndex=fromIndex;
		this.pageSize=pageSize;
		this.sortOrder=sortOrder;
	}
			
	public boolean validateUser(String userName,String password) {
	
		boolean isPass=false;
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
					
			isPass=managementAPI.checkUserCredentials(userName,password);
			
			loginContext.logout();
			
			if(isPass) {
				resultCode=RESULT_OK;
				resultMessage="Validate success";
			} else {
				resultCode=RESULT_FAIL;
				resultMessage="Validate fail";
			}
		
		} catch(Exception e) {
			isPass=false;
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="Validate error";
		}
		
		return isPass;
	}
	
	public List getCurrentUserList() {
	
		log.info("getCurrentUserList user="+user);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		List list=new ArrayList();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
			
			
			List<User> userList=identityAPI.getAllUsers();
			
			for(User bonitaUser:userList) {
				String bonitaUserName=bonitaUser.getUsername();
				list.add(bonitaUserName);
			}
						
			loginContext.logout();
		
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="";
		}
		
		return list;
	}
	
	public List getProcessNameList() {
	
		log.info("getProcessNameList user="+user);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		List list=new ArrayList();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
		
			Set<ProcessDefinition> processSet=queryDefinitionAPI.getProcesses(ProcessDefinition.ProcessState.ENABLED);
			
			log.info("processSet="+processSet);
			for(ProcessDefinition processDefinition:processSet) {
				String uuid=processDefinition.getUUID().toString();
				//log.info("processDefinition.getName()="+processDefinition.getName());
				//log.info("processDefinition.getUUID()="+processDefinition.getUUID());
				list.add(uuid);
			}
						
			loginContext.logout();
		
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="";
		}
		
		return list;
	}

	public List getAssignedProcessNameList() {
	
		log.info("getAssignedProcessNameList user="+user);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		List list=new ArrayList();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
		
			Set<ProcessDefinition> processSet=queryDefinitionAPI.getProcesses(ProcessDefinition.ProcessState.ENABLED);
			
			log.info("processSet="+processSet);
			
			for(ProcessDefinition processDefinition:processSet) {
				String uuid=processDefinition.getUUID().toString();
				
				Map infoMap=new HashMap();
				
				Set set=new HashSet();
				set.add(processDefinition.getUUID());
				
				String name=processDefinition.getUUID().toString();
				String description=processDefinition.getDescription();
				
				Integer count=queryRuntimeAPI.getNumberOfParentProcessInstancesWithActiveUser(userName,set);
				
				log.info("processDefinition.getName()="+processDefinition.getName());
				log.info("processDefinition.getUUID()="+processDefinition.getUUID());
				log.info("processDefinition.getDescription()="+processDefinition.getDescription());
				log.info("processDefinition.count()="+count);
				
				if(count.intValue()==0) {
					continue;
				}
				
				if("".equals(description)) {
					description=name;
				}
				
				infoMap.put("name",name);
				infoMap.put("description",description);
				infoMap.put("count",count);
				
				list.add(infoMap);
			}
						
			loginContext.logout();
		
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="";
		}
		
		return list;
	}

	
	public List getAssignedTaskList(String processName) {
	
		log.info("getAssignedTaskList user="+user+" processName="+processName+" sortOrder="+sortOrder);		
	
		String userName=user.getName();;
		String password=user.getPassword();
	
		List<Map> list=new ArrayList<Map>();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
			
						
			List<LightProcessInstance> processList=null;

			if(processName==null || "".equals(processName)) {
													
				processList=queryRuntimeAPI.getLightParentProcessInstancesWithActiveUser(
										userName,
										fromIndex,pageSize,
										ProcessInstanceCriterion.valueOf(sortOrder)
									);			
												
			} else {
				
				Set<ProcessDefinitionUUID> set=new HashSet<ProcessDefinitionUUID>();
				set.add(new ProcessDefinitionUUID(processName));

				processList=queryRuntimeAPI.getLightParentProcessInstancesWithActiveUser(
										userName,
										fromIndex,pageSize,
										set,
										ProcessInstanceCriterion.valueOf(sortOrder)
									);
			}
			
			log.info("processList.size()="+processList.size());
			
			for(LightProcessInstance processInstance : processList) {
							
				list=getAssignedProcessList(list,processInstance);
							
			}
			
			list=processDescriptionAndLink(list);

			loginContext.logout();
		
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="";
		}
		
		return list;
	}
	
	public List getOwnedTaskList(String processName) {
	
		log.info("getOwnedTaskList user="+user+" processName="+processName+" sortOrder="+sortOrder);		
	
		String userName=user.getName();;
		String password=user.getPassword();
	
		List<Map> list=new ArrayList<Map>();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
		
			loginContext.login();
			
						
			List<LightProcessInstance> processList=null;

			if(processName==null || "".equals(processName)) {
				
				/*
				processList=queryRuntimeAPI.getLightParentProcessInstancesWithActiveUser(
										userName,
										fromIndex,pageSize,
										ProcessInstanceCriterion.valueOf(sortOrder)
									);
				*/
							
				processList=queryRuntimeAPI.getLightParentProcessInstances(
						fromIndex,pageSize,
						ProcessInstanceCriterion.valueOf(sortOrder)
					);
												
			} else {
				
				Set<ProcessDefinitionUUID> set=new HashSet<ProcessDefinitionUUID>();
				set.add(new ProcessDefinitionUUID(processName));

				processList=queryRuntimeAPI.getLightParentProcessInstancesWithActiveUser(
										userName,
										fromIndex,pageSize,
										set,
										ProcessInstanceCriterion.valueOf(sortOrder)
									);
			}
			
			log.info("processList.size()="+processList.size());
			
			for(LightProcessInstance processInstance : processList) {
							
				list=getOwnedProcessList(list,processInstance);
				
				//log.info("processInstance="+processInstance);
							
			}
			
			/*
			List list1=queryRuntimeAPI.getLightParentProcessInstancesWithActiveUser(
										userName,
										0,100,
										ProcessInstanceCriterion.valueOf(sortOrder)
									);
			log.info("list1.size()="+list1.size());
			log.info("list1="+list1);

			List list2=queryRuntimeAPI.getLightProcessInstances(0,100);
			log.info("list2.size()="+list2.size());
			log.info("list2="+list2);
			*/

			list=processDescriptionAndLink(list);
						
			loginContext.logout();
		
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage="";
		}
		
		return list;
	}
	
	protected List<Map> getAssignedProcessList(List processList,LightProcessInstance processInstance) throws Exception {
	
		Map map=new HashMap();
						
		ProcessInstanceUUID instanceUUID=processInstance.getProcessInstanceUUID();
		ProcessDefinitionUUID processDefinitionUUID=processInstance.getProcessDefinitionUUID();
		
		InstanceState instanceState=processInstance.getInstanceState();
				
		String userName=user.getName();
		String startUserName=processInstance.getStartedBy();
		
		String activityName="";
		long nActivityCreated = 0;
		long nActivityEnded = 0;
		String instanceDescription="";
		String activityDescription = "";
		ActivityState activityState=null;
		
		String startedUser=processInstance.getStartedBy();
		
		log.info("instanceUUID="+instanceUUID+" instanceState="+instanceState+" startedUser="+startedUser+" userName="+userName);
				
		if(instanceState==InstanceState.STARTED) {
		
			log.info("-----------------------");
		
			boolean hasActivityInstance=false;
			boolean hasOwnInstance=false;
		
			log.info("processInstance="+processInstance);
			
			ProcessInstance instance=queryRuntimeAPI.getProcessInstance(instanceUUID);
		
			ActivityInstanceUUID activityInstanceUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			log.info("instanceUUID="+instanceUUID);
			log.info("activityInstanceUUID="+activityInstanceUUID);
			
			Set<ProcessInstanceUUID> childInstanceUUIDs=instance.getChildrenInstanceUUID();
						
			log.info("childInstanceUUIDs="+childInstanceUUIDs);
						
			if(activityInstanceUUID!=null || 
				(activityInstanceUUID==null && childInstanceUUIDs.size()>0)
				) {
				
				StringBuffer subActivityState=new StringBuffer();
				
				if(activityInstanceUUID!=null) {
					ActivityInstance activityInstance=queryRuntimeAPI.getActivityInstance(activityInstanceUUID);
					
					activityName=activityInstance.getActivityName();
					//Date endDate = activityInstance.getExpectedEndDate();
					//nActivityEnded = endDate.getTime();
					Date activityCreatedTime = activityInstance.getReadyDate();
					nActivityCreated = activityCreatedTime.getTime();
					activityDescription = activityInstance.getActivityDescription();
					activityState=activityInstance.getState();
					
					instanceDescription=processDefinitionUUID.toString();
										
					hasActivityInstance=true;
					hasOwnInstance=true;
										
				} else if(childInstanceUUIDs.size()>0) {
					
					hasActivityInstance=true;
				
					for(ProcessInstanceUUID childInstanceUUID : childInstanceUUIDs) {
													
						activityInstanceUUID=queryRuntimeAPI.getOneTask(childInstanceUUID,ActivityState.READY);
						
						//log.info("child activityInstanceUUID="+activityInstanceUUID);
						
						if(activityInstanceUUID!=null) {
						
							ActivityInstance activityInstance=queryRuntimeAPI.getActivityInstance(activityInstanceUUID);
							//activityName=activityInstance.getActivityName()+activityInstanceUUID.toString();
							activityName=activityInstance.getActivityName();
							//Date endDate = activityInstance.getExpectedEndDate();
							//nActivityEnded = endDate.getTime();
							Date activityCreatedTime = activityInstance.getReadyDate();
							nActivityCreated = activityCreatedTime.getTime();
							activityDescription = activityInstance.getActivityDescription();
							activityState=activityInstance.getState();
							
							hasOwnInstance=true;
							
							instanceDescription=childInstanceUUID.toString();
							
						}

					}
				
				}
				
				if(hasActivityInstance) {
					String instanceLink="";
					if(hasOwnInstance) {
						instanceLink="findBonitaForm.html?req={\"bonita_instanceName\": \""+instanceUUID+"\", \"cur_user\": \""+userName+"\"}";
					}
					String workflowUIView="wf_ui_view.html?instance_name="+instanceUUID.toString();
				
					if(hasOwnInstance==false) {
						activityName=subActivityState.toString();
					}
					String strDescription = "";
					String descriptionLink = "";
					String instanceVar = "";
					String rallyRef="";
					ProcessInstanceUUID _instanceUUID =new ProcessInstanceUUID(instanceUUID.toString());
					Map varMap=new HashMap();
					try{
						varMap = queryRuntimeAPI.getProcessInstanceVariables(_instanceUUID);
												
						rallyRef = (String)varMap.get("rally_ref");
						
						strDescription = (String)varMap.get("_description_");
						log.info("0strDescription="+strDescription);
						
						if(strDescription==null) {
							strDescription="";
						}
						
						descriptionLink = (String)varMap.get("_description_link");
						log.info("0descriptionLink="+descriptionLink);
						
						if(descriptionLink==null) {
							descriptionLink="";
						}
						
					} catch(Exception e){
						log.info(e.toString());
					}
								
					String processActivityName=instanceDescription+"/"+activityName;
					
					if(activityDescription==null || "".equals(activityDescription)) {
						activityDescription=activityName;
					}
								
					log.info("processActivityName="+processActivityName);
					log.info("instanceUUID="+instanceUUID);
					log.info("processDefinitionUUID="+processDefinitionUUID);
					log.info("activityName="+activityName);
					log.info("activityState="+activityState);
					log.info("instanceLink="+instanceLink);
					log.info("rallyRef="+rallyRef);
										
					map.put("instance_name",processActivityName);
					map.put("form_link",instanceLink);	
					map.put("instance_link",instanceLink);
					map.put("wf_ui_view_link",workflowUIView);
					map.put("process_name",processDefinitionUUID.toString());					
					map.put("activity_name",activityDescription);
					map.put("activity_state",activityState);
					map.put("created_timestamp", nActivityCreated);
					map.put("due_timestamp", nActivityEnded);
					map.put("description", strDescription);
					map.put("description_link", descriptionLink);
					map.put("rallyRef", rallyRef);
										
					processList.add(map);		
				}
			
			}
			
		
		}
		
		return processList;
	}
	
	protected List<Map> getOwnedProcessList(List processList,LightProcessInstance processInstance) throws Exception {
	
		Map map=new HashMap();
						
		ProcessInstanceUUID instanceUUID=processInstance.getProcessInstanceUUID();
		ProcessDefinitionUUID processDefinitionUUID=processInstance.getProcessDefinitionUUID();
		
		InstanceState instanceState=processInstance.getInstanceState();
				
		String userName=user.getName();
		
		long nActivityCreated = 0;
		long nActivityEnded = 0;

		ActivityState activityState=null;
		
		String startedUser=processInstance.getStartedBy();
		
		log.info("instanceUUID="+instanceUUID+" instanceState="+instanceState+" startedUser="+startedUser+" userName="+userName);

		
		if(instanceState==InstanceState.STARTED && 
			startedUser.equals(userName)) {
		
			log.info("-----------------------");
				
			log.info("processInstance="+processInstance);
			
			ProcessInstance instance=queryRuntimeAPI.getProcessInstance(instanceUUID);
		
			ActivityInstanceUUID activityInstanceUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			Set<ProcessInstanceUUID> childInstanceUUIDs=instance.getChildrenInstanceUUID();
						
			log.info("activityInstanceUUID="+activityInstanceUUID);
			log.info("childInstanceUUIDs="+childInstanceUUIDs);
						
			if(activityInstanceUUID==null && childInstanceUUIDs.size()>0) {
			
				String resultUUID=(String)queryRuntimeAPI.getProcessInstanceVariable(instanceUUID, "uuid");
				log.info("resultUUID="+resultUUID);
			
				Map resultMap=getVoteResultMap(resultUUID);
				
				log.info("resultMap="+resultMap);
			
				String childDescription="";
			
				nActivityCreated=instance.getStartedDate().getTime();
				
				StringBuffer subActivityState=new StringBuffer();
				
				List childList=new ArrayList();
			
				for(ProcessInstanceUUID childInstanceUUID : childInstanceUUIDs) {
				
					String childResultUUID=(String)queryRuntimeAPI.getProcessInstanceVariable(childInstanceUUID, "uuid");
					
					//Filter outdated child processes
					if(!childResultUUID.equals(resultUUID)) {
						continue;
					}
				
					Map childMap=new HashMap();
										
					ProcessInstance childProcessInstance=queryRuntimeAPI.getProcessInstance(childInstanceUUID);
										
					Set<TaskInstance> childTasks=childProcessInstance.getTasks();
					
					String activityDescription="";
					String assignedUser="";
					String childInstanceName=childInstanceUUID.toString();
					for(TaskInstance childTask:childTasks) {
					
						String activityName=childTask.getActivityName();
						
						activityDescription=childTask.getActivityDescription();						
						if(activityDescription==null || "".equals(activityDescription)) {
							activityDescription=activityName;
						}
												
						Set<String> candidates=childTask.getTaskCandidates();
						
						for(String candidate:candidates) {
							assignedUser=candidate;
						}
					
					}
					
					String instanceLink="findBonitaForm.html?req={\"bonita_instanceName\": \""+instanceUUID+"\", \"cur_user\": \""+userName+"\"}";
					String workflowUIView="wf_ui_view.html?instance_name="+instanceUUID.toString();
					
					childDescription="pass";
										
					String result=(String)resultMap.get(assignedUser);
					
					if(result!=null) {
						if("pass".equals(result)) {
							childDescription="pass";
						} else {
							childDescription="fail";
						}
					} else {
						childDescription="not vote";
					}
					
					childMap.put("process_name",childInstanceName);
					childMap.put("owner",assignedUser);
					childMap.put("wf_ui_view_link",workflowUIView);
					childMap.put("description",childDescription);
					childMap.put("activity_name",activityDescription);
				
					log.info("childMap="+childMap);
					
					childList.add(childMap);

				}
				
				String strDescription = "";
				String descriptionLink = "";
				String instanceVar = "";
				String rallyRef="";
				ProcessInstanceUUID _instanceUUID =new ProcessInstanceUUID(instanceUUID.toString());
				Map varMap=new HashMap();
				try{
					varMap = queryRuntimeAPI.getProcessInstanceVariables(_instanceUUID);
										
					rallyRef = (String)varMap.get("rally_ref");
					
					strDescription = (String)varMap.get("_description_");
					log.info("1strDescription="+strDescription);
					
					if(strDescription==null) {
						strDescription="";
					}
					
					descriptionLink = (String)varMap.get("_description_link");
					log.info("1descriptionLink="+descriptionLink);
					
					if(descriptionLink==null) {
						descriptionLink="";
					}
					
				} catch(Exception e){

				}
				
				map.put("process_name",processDefinitionUUID.toString());
				map.put("created_timestamp", nActivityCreated);
				map.put("due_timestamp", nActivityEnded);
				map.put("description", strDescription);
				map.put("description_link", descriptionLink);
				map.put("sub_list", childList);
				
				processList.add(map);		
			}

			
		
		}
		
		return processList;
	}

	public List<Map> processDescriptionAndLink(List<Map> processList) {
	
		for(Map map:processList) {
			String desc=(String)map.get("description");
	
			String formattedID=RallyService.getUSFormattedID(desc);
			
			if(!"".equals(formattedID)) {
			
				RallyService service=new RallyService(confFileName);
				Map usMap=service.getUserStory(formattedID);
				String usName=(String)usMap.get("name");
				String usLink=(String)usMap.get("link");
				String projectName=(String)usMap.get("project");
				
				map.put("description",formattedID+" "+usName);
				map.put("description_link",usLink);
				map.put("project",projectName);
								
			}
		
		}
	
		return processList;
	}

	/*
	public List<Map> getLinkByFormattedID(List<Map> processList) {
	
		log.info("getLinkByFormattedID....");
		
		Set<String> formattedIDSet=new HashSet<String>();
		
		for(Map map:processList) {
			String desc=(String)map.get("description");
			if(!"".equals(desc)) {
				formattedIDSet.add(desc);
			}
		}
		
		log.info("formattedIDSet="+formattedIDSet);
		
		Map descMap=new HashMap();
		Map descLinkMap=new HashMap();
		for(String formattedID:formattedIDSet) {
		
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

					String link="https://rally1.rallydev.com/#/detail/userstory/"+objectID;
					descMap.put(formattedID,formattedID+" "+name);
					descLinkMap.put(formattedID,link);
					
				}
			
			} catch(Exception e) {
				log.error("",e);
			}
		
		}
		
		if(formattedIDSet.size()>0) {
			for(Map map:processList) {
				String formattedID=(String)map.get("description");
				String name=(String)descMap.get(formattedID);
				String link=(String)descLinkMap.get(formattedID);
				if(name!=null) {
					map.put("description",name);
				} else if(!"".equals(formattedID)) {
					map.put("description",formattedID+" (not found)");
				}
				if(link!=null) {
					map.put("description_link",link);
				}
			}
		} else {
					
			for(Map map:processList) {
				String rallyRef=(String)map.get("rallyRef");
				if(!"".equals(rallyRef)) {
							
					String apiUrl=rallyRef+"?fetch=true";
					log.info("apiUrl:"+apiUrl);
			
					try {
			
						String responseXML=getRallyXML(apiUrl);
						
						org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
						org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
						Element root = doc.getRootElement(); 		
				
						Element nameElement = (Element)XPath.selectSingleNode(root,"//Name");
						
						String name="";
						if(nameElement!=null) {
							name=nameElement.getValue();
							log.info("name="+name);
						}
						
						String projectName="";
						
						Element projectNameElement = (Element)XPath.selectSingleNode(root,"//Project");
						if(projectNameElement!=null) {
							projectName=projectNameElement.getAttribute("refObjectName").getValue();
							log.info("projectName="+projectName);
						}
						
						map.put("description",projectName+" - "+name);
						map.put("description_link","");
						
					} catch(Exception e) {
						log.error("",e);
					}
					
				}
			}
		
		}
			
		return processList;
	}
	*/
	
	public Map getGlobalVar(String instanceName) {
	
		log.info("getGlobalVar user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		Map varMap=new HashMap();
		Map outputMap=new HashMap();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			varMap = queryRuntimeAPI.getProcessInstanceVariables(instanceUUID);
						
			String instanceVar=(String)varMap.get("instance_var");	
			if(instanceVar!=null) {
				try {
					Map instanceVarMap=JsonUtil.jsonToMap(instanceVar);
					varMap.put("instance_var",instanceVarMap);
				} catch(Exception e) {
					//if instanceVar is not JSON format
					varMap.put("instance_var",instanceVar);
				}
			}
			
			loginContext.logout();
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		return varMap;
	}
	
	public Map getLocalVar(String instanceName) {
	
		log.info("getLocalVar user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		Map varMap=new HashMap();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			ActivityInstanceUUID activityUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			varMap = queryRuntimeAPI.getActivityInstanceVariables(activityUUID);
			
			loginContext.logout();
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
		
		return varMap;
	}
	
	public boolean setGlobalVar(String instanceName,Map varMap) {
	
		boolean isSuccess=false;
	
		log.info("setGlobalVar user="+user+" instanceName="+instanceName);
		log.info("varMap="+varMap);
		
		log.info("varMap.size()="+varMap.size());
		log.info("varMap.get()="+varMap.get("list"));
	
		String userName=user.getName();
		String password=user.getPassword();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			log.info("setProcessInstanceVariables..started");
			
			//runtimeAPI.setProcessInstanceVariables(instanceUUID,varMap);
			
			Set keyset = varMap.entrySet();
			for (Iterator iter = keyset.iterator(); iter.hasNext();) {
				Map.Entry mapentry = (Map.Entry) iter.next();
				String key = (String) mapentry.getKey();
				String value = (String) mapentry.getValue();
				log.info("key="+key+" value="+value);
				runtimeAPI.setProcessInstanceVariable(instanceUUID,key,value);
			}
			
			log.info("setProcessInstanceVariables..finished");
			
			String listText = (String)queryRuntimeAPI.getProcessInstanceVariable(instanceUUID,"list");
			log.info("listText1="+listText);
			
			/*
			runtimeAPI.setProcessInstanceVariable(instanceUUID,"list","randy2.chen@raritan.com");
			listText = (String)queryRuntimeAPI.getProcessInstanceVariable(instanceUUID,"list");
			log.info("listText2="+listText);
			*/
			
			loginContext.logout();
			
			isSuccess=true;
			
		} catch(org.ow2.bonita.facade.exception.VariableNotFoundException e1) {
			log.error("InstanceNotFoundException",e1);
		} catch(org.ow2.bonita.facade.exception.InstanceNotFoundException e2) {
			log.error("InstanceNotFoundException",e2);
		} catch(Exception e) {
			log.info("e1="+e.toString());
			log.info("e2="+e.getMessage());
			e.printStackTrace();

			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
			
			isSuccess=false;
		}
		
		return isSuccess;
	}
	
	public boolean setLocalVar(String instanceName,Map varMap) {
	
		boolean isSuccess=false;
	
		log.info("setLocalVar user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			ActivityInstanceUUID activityInstanceUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			log.info("activityInstanceUUID="+activityInstanceUUID);

			if(activityInstanceUUID!=null) {
			
				//runtimeAPI.setActivityInstanceVariables(activityInstanceUUID,varMap);
				
				Set keyset = varMap.entrySet();
				for (Iterator iter = keyset.iterator(); iter.hasNext();) {
					Map.Entry mapentry = (Map.Entry) iter.next();
					String key = (String) mapentry.getKey();
					String value = (String) mapentry.getValue();
					log.info("key="+key+" value="+value);
					runtimeAPI.setActivityInstanceVariable(activityInstanceUUID,key,value);
				}
				
			} else {
			
				ProcessInstance processInstance=queryRuntimeAPI.getProcessInstance(instanceUUID);
			
				Set<ProcessInstanceUUID> childInstanceUUIDs=processInstance.getChildrenInstanceUUID();
				
				log.info("childInstanceUUIDs="+childInstanceUUIDs);
			
				if(activityInstanceUUID==null && childInstanceUUIDs.size()>0) {				
					
					for(ProcessInstanceUUID childInstanceUUID : childInstanceUUIDs) {
					
						activityInstanceUUID=queryRuntimeAPI.getOneTask(childInstanceUUID,ActivityState.READY);
					
						log.info("child activityInstanceUUID="+activityInstanceUUID);
					
						//runtimeAPI.setActivityInstanceVariables(activityInstanceUUID,varMap);
						
						if(activityInstanceUUID!=null) {
						
							Set keyset = varMap.entrySet();
							for (Iterator iter = keyset.iterator(); iter.hasNext();) {
								Map.Entry mapentry = (Map.Entry) iter.next();
								String key = (String) mapentry.getKey();
								String value = (String) mapentry.getValue();
								log.info("key="+key+" value="+value);
								runtimeAPI.setActivityInstanceVariable(activityInstanceUUID,key,value);
							}
						
						}
					
					}
				
				}				
			
			}
			
			
			loginContext.logout();
			
			isSuccess=true;
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
			
			isSuccess=false;
		}
		
		return isSuccess;
	}
	
	public String getExternalUrlLink(String instanceName) {
	
		log.info("getExternalUrlLink user="+user+" instanceName="+instanceName);
		
		String externalUrl="";
	
		String userName=user.getName();
		String password=user.getPassword();
	
		Map varMap=new HashMap();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			log.info("instanceUUID="+instanceUUID);
			
			ProcessInstance instance=queryRuntimeAPI.getProcessInstance(instanceUUID);
			
			Set<ProcessInstanceUUID> childInstanceUUIDs=instance.getChildrenInstanceUUID();
			
			log.info("childInstanceUUIDs="+childInstanceUUIDs);
			
			ActivityInstanceUUID activityUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			log.info("activityUUID="+activityUUID);
			
			if(activityUUID!=null) {
			
				varMap = queryRuntimeAPI.getActivityInstanceVariables(activityUUID);
			
			} else if(childInstanceUUIDs.size()>0) {
			
				StringBuffer subActivityState=new StringBuffer();
			
				for(ProcessInstanceUUID childInstanceUUID : childInstanceUUIDs) {
			
					ActivityInstanceUUID childActivityUUID=queryRuntimeAPI.getOneTask(childInstanceUUID,ActivityState.READY);
					
					log.info("childActivityUUID="+childActivityUUID);
					
					if(childActivityUUID!=null) {
					
						varMap = queryRuntimeAPI.getActivityInstanceVariables(childActivityUUID);
						
					} else {
					
						log.info("no externalUrlLink");
						
						Set<TaskInstance> tasks=queryRuntimeAPI.getTasks(childInstanceUUID);
						log.info("tasks="+tasks);
												
						for(TaskInstance task:tasks) {
							Set candidates=task.getTaskCandidates();
							
							ActivityState activityState=task.getState();
							
							if(activityState!=ActivityState.FINISHED) {
								subActivityState.append(task.getActivityName()+" by "+candidates);
								//resultMessage=task.getActivityName()+" by "+candidates;
							}
						}
						
					
					}
					
				}
				
				if(subActivityState.length()>0) {
					resultMessage=subActivityState.toString();
				}
			
			}
			
			loginContext.logout();
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		externalUrl=(String)varMap.get("externalUrl");
		
		log.info("map="+varMap);
		log.info("externalUrl="+externalUrl);
				
		return externalUrl;
	}
	
	public Map getWorkflowUIInfo(String contextPath,String instanceName) {
	
		log.info("getWorkflowUIInfo user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		Map varMap=new HashMap();
				
		String argument="{"+
								"\"instance_name\": \""+instanceName+"\","+
								"\"username\": \""+userName+"\","+
								"\"password\": \""+password+"\""+
								"}";

		try {
	
			String[] cmd={
				"python",
				contextPath+"/scripts/parserBpmnExV2.py",
				argument
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
			
			log.info("...doProcessDwg....process="+process);
			
			//loadStream(process.getInputStream());
			//loadStream(process.getErrorStream());
			
			String responseMessage="";
			String thisLine = "";
			InputStream is=process.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				responseMessage=thisLine;
			}

			String errorMessage="";
			InputStream is2=process.getErrorStream();
			BufferedReader br2=new BufferedReader(new InputStreamReader(is2));
			while ((thisLine = br2.readLine()) != null) {
				errorMessage=thisLine;
			}

			int exitValue=process.waitFor();
			
			log.info("exitValue="+exitValue);
			log.info("responseMessage="+responseMessage);
			log.info("errorMessage="+errorMessage);
			
			if(exitValue!=0) {
				resultCode=RESULT_FAIL;
				resultMessage=errorMessage;
			} else {
			
				if(responseMessage!=null && responseMessage.length()>0) {
					varMap=JsonUtil.jsonToMap(responseMessage);
					
					int ret=((Long)varMap.get("ret")).intValue();
					
					if(ret!=0) {
						resultCode=RESULT_FAIL;
						resultMessage=(String)varMap.get("msg");
					}
					
					String tooltip="";
					String externalUrl=getExternalUrlLink(instanceName);
					String clickableUrl="";
										
					String[] defNames=instanceName.split("--");
					String defName=defNames[0];
					
					String requestUrl=workflowServiceUrl+"/instanceList.html";
					
					if(externalUrl==null) {
						clickableUrl="";
						tooltip=getResultMessage();
					} else {
						clickableUrl=workflowServiceUrl+externalUrl+"?req="+URLEncoder.encode("{\"bonita_instanceName\":\""+instanceName+"\"}");
						tooltip="mouse over will showed to user";
					}
					
					String rallyFormattedId="";
					
					String imgPath=workflowServiceUrl+"/png/"+defName+".png";
							
					varMap.put("requster_url",requestUrl);
					varMap.put("clickable_url",clickableUrl);
					varMap.put("rally_formatted_id",rallyFormattedId);
					varMap.put("tooltip",tooltip);
					varMap.put("imgPath",imgPath);
					
				}
				
				
			}
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		return varMap;
	}
	
	public Map checkRallyDescription(String contextPath,String instanceName) {
	
		log.info("checkRallyDescription user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		Map returnMap=new HashMap();
				
		try {
	
			String[] cmd={
				"python",
				contextPath+"/scripts/chk_rally_acceptance_criteria.py",
				instanceName
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
						
			//loadStream(process.getInputStream());
			//loadStream(process.getErrorStream());
			
			String responseMessage="";
			String thisLine = "";
			InputStream is=process.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				responseMessage=thisLine;
			}

			String errorMessage="";
			InputStream is2=process.getErrorStream();
			BufferedReader br2=new BufferedReader(new InputStreamReader(is2));
			while ((thisLine = br2.readLine()) != null) {
				errorMessage=thisLine;
			}

			int exitValue=process.waitFor();
			
			log.info("exitValue="+exitValue);
			log.info("responseMessage="+responseMessage);
			log.info("errorMessage="+errorMessage);
			
			if(exitValue!=0) {
				resultCode=RESULT_FAIL;
				resultMessage=errorMessage;
			} else {
			
				if(responseMessage!=null && responseMessage.length()>0) {
					returnMap=JsonUtil.jsonToMap(responseMessage);
				}
				
			}
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		return returnMap;
	}

	
	public Map getVoteResultMap(String resultUUID) {
	
		Map resultMap=new HashMap();
	
		try {

			String[] cmd={
				"python",
				"/home/atuser/workflow_connector/updateUserData.py",
				resultUUID,
				"get"
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
			
			String responseMessage="";
			String thisLine = "";
			InputStream is=process.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				responseMessage=thisLine;
			}

			String errorMessage="";
			InputStream is2=process.getErrorStream();
			BufferedReader br2=new BufferedReader(new InputStreamReader(is2));
			while ((thisLine = br2.readLine()) != null) {
				errorMessage=thisLine;
			}

			int exitValue=process.waitFor();
			
			log.info("exitValue="+exitValue);
			log.info("responseMessage="+responseMessage);
			log.info("errorMessage="+errorMessage);

			/* for DEBUG
			int exitValue=0;
			String responseMessage="{\"vote\": [{\"input\": \"pass\", \"name\": \"kc.chen@raritan.com\"}]}";
			String errorMessage="";
			*/

			if(exitValue==0 && !"".equals(responseMessage) && "".equals(errorMessage)) {
				Map map=JsonUtil.jsonToMap(responseMessage);
				JSONArray voteArray=(JSONArray)map.get("vote");
				for(int i=0;i<voteArray.size();i++) {
					JSONObject obj=(JSONObject)voteArray.get(i);
					
					String name=(String)obj.get("name");
					String input=(String)obj.get("input");
					
					resultMap.put(name,input);
					
				}				
				
			}

		} catch(Exception e) {
			log.error("",e);
		}
	
		return resultMap;
	}
	
	public boolean runTask(String instanceName) {
	
		boolean isSuccess=false;
	
		log.info("runTask user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			ActivityInstanceUUID activityInstanceUUID=queryRuntimeAPI.getOneTask(instanceUUID,ActivityState.READY);
			
			if(activityInstanceUUID!=null) {
			
				runtimeAPI.executeTask(activityInstanceUUID,false);
			
			} else {
			
				ProcessInstance instance=queryRuntimeAPI.getProcessInstance(instanceUUID);
			
				Set<ProcessInstanceUUID> childInstanceUUIDs=instance.getChildrenInstanceUUID();
				
				log.info("childInstanceUUIDs="+childInstanceUUIDs);
				
				if(childInstanceUUIDs.size()>0) {
			
					for(ProcessInstanceUUID childInstanceUUID : childInstanceUUIDs) {
					
						ActivityInstanceUUID childActivityUUID=queryRuntimeAPI.getOneTask(childInstanceUUID,ActivityState.READY);

						if(childActivityUUID!=null) {
							runtimeAPI.executeTask(childActivityUUID,false);
						}
						
					}
				}
			
			}
			
			loginContext.logout();
			
			isSuccess=true;
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
			
			isSuccess=false;
		}
		
		return isSuccess;
	}
	
	public String createInstance(String bonitaProcessId,String userEmail,Map varMap) {
			
		String uuid="";
		
		String userName=userEmail;
		String password=bonitaApiPassword;
		
		log.info("bonitaProcessId="+bonitaProcessId);
		log.info("createInstance userName="+userName+" password="+password);
		log.info("varMap="+varMap);
	
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessDefinitionUUID  processDefinitionUUID =new ProcessDefinitionUUID (bonitaProcessId);
			
			ProcessInstanceUUID processInstanceUUID=runtimeAPI.instantiateProcess(processDefinitionUUID,varMap);
				
			uuid=processInstanceUUID.toString();
			
			loginContext.logout();
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
			
		}
		
		log.info("uuid="+uuid);
		
		return uuid;
	
	}
	/*
	public String updateState(String taskId,String state) {
			
		boolean sendOK=false;
		
		String processInstanceUUID="";
		
       String apiUrl=bonitaApiHost+bonitaApiPath;
       
       apiUrl=apiUrl.replace("${processUUID}",bonitaProcessId);
              
        try {
        
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			Base64 base64 = new Base64();
			String encodeString = new String(base64.encode((bonitaApiHttpUsername+":"+bonitaApiHttpPassword).getBytes()));
			
			HttpPost httpPost = new HttpPost(apiUrl);
			httpPost.addHeader("Authorization","Basic "+encodeString);
			
        	StringBuffer variables=new StringBuffer();        	
			variables.append("<map>");
			variables.append("<entry>");
			variables.append("<string>task_no</string>");
			variables.append("<string>"+taskId+"</string>");
			variables.append("</entry>");
			//variables.append("<entry>");
			//variables.append("<string>state</string>");
			//variables.append("<string>"+state+"</string>");
			//variables.append("</entry>");
			variables.append("</map>");
			        
            HttpParams params=new BasicHttpParams();
                        
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();  
			nvps.add(new BasicNameValuePair("options", "user:"+bonitaApiUsername));
			nvps.add(new BasicNameValuePair("variables", variables.toString()));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
						
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			String responseXML=getEntityString(entity);

            log.info("----------------------------------------");
            log.info(responseXML);
            log.info("----------------------------------------");
            
			org.jdom.input.SAXBuilder bSAX = new org.jdom.input.SAXBuilder(); 
			org.jdom.Document doc = bSAX.build(new StringReader(responseXML));
			Element root = doc.getRootElement(); 		
	
			XPath xpath = XPath.newInstance("//ProcessInstanceUUID");
			List xlist = xpath.selectNodes(root);
						
			Iterator iter = xlist.iterator();
			while(iter.hasNext()) {
				Element item = (Element) iter.next();
				processInstanceUUID=item.getChildText("value");
			}

            log.info("processInstanceUUID="+processInstanceUUID);
        	
        	sendOK=true;
            
        } catch(Exception e) {
        	e.printStackTrace();
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
        }
        
        if(sendOK) {
        
			resultCode=RESULT_OK;
        
	        return processInstanceUUID;
        } else {
        
        	resultCode=RESULT_FAIL;
        
        	return null;
        }
	
	}
	*/
	
	public String getRallyRef(String instanceName) {
		String rallyRef="";
	
		log.info("getRallyRef user="+user+" instanceName="+instanceName);
	
		String userName=user.getName();
		String password=user.getPassword();
		
		try {
	
			LoginContext loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(userName, password));
			loginContext.login();
			
			ProcessInstanceUUID instanceUUID=new ProcessInstanceUUID(instanceName);
			
			Map varMap = queryRuntimeAPI.getProcessInstanceVariables(instanceUUID);
			
			if(varMap!=null) {
				rallyRef=(String)varMap.get("rally_ref");
			}
			
			loginContext.logout();
								
		} catch(Exception e) {
			log.error("",e);
			
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
	
		return rallyRef;
	}
    
	public static void main(String[] argv) {
	

	}
}  