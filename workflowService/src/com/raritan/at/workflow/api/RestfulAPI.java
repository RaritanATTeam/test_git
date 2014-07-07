package com.raritan.at.workflow.api;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.springframework.web.context.*;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.apache.commons.codec.binary.Base64;

//import org.springframework.http.converter.*;
//import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

import org.ow2.bonita.util.*;

import static com.raritan.at.workflow.service.Constant.*;
import com.raritan.at.util.*;
import com.raritan.at.workflow.bean.*;
import com.raritan.at.workflow.service.*;

@Controller("RestfulAPI")
public class RestfulAPI extends BaseAPI {
		
	public RestfulAPI() {
		
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/validateUser <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/validateUser",method=RequestMethod.GET)
	@ResponseBody
	public Map validateUser(HttpServletRequest request,
				@RequestHeader(value = "Authorization", required=false) String credentials) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		if(credentials!=null) {
			credentials=credentials.replace("Basic","");		
			String decodedStr=credentials.trim();
			decodedStr = new String(Base64.decodeBase64(decodedStr));
			String[] splitArr=decodedStr.split(":");
			
			String userName=splitArr[0];
			String password=splitArr[1];
			
			log.info("userName="+userName);
			log.info("password="+password);
			
			BonitaService service=new BonitaService(configFile);
	
			boolean isPass=service.validateUser(userName,password);
			
			log.info("isPass="+isPass);
	
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();
			
			HttpSession session=request.getSession();
			
			AuthUser user=new AuthUser();
			user.setName(userName);
			user.setPassword(password);
		
			session.setAttribute(USER_BEAN,user);
			
		} else {
			resultMessage="No credentials";
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/logout <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/logout",method=RequestMethod.GET)
	@ResponseBody
	public Map logout(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
			
		try {
			AuthUser user=getUser(request,false);
			user=null;
			
			HttpSession session=request.getSession();
			session.setAttribute(USER_BEAN,null);
			
			resultCode=RESULT_OK;
			
		} catch(Exception e) {
			resultCode=-1;
			log.error("",e);
		}

		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getCurrentUser <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/getCurrentUser",method=RequestMethod.GET)
	@ResponseBody
	public Map getCurrentUser(HttpServletRequest request) {
	
		init(request);
		AuthUser user=getUser(request);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		String userAccount=user.getName();
			
		resultCode=RESULT_OK;
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("user",userAccount);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getCurrentUserList <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/getCurrentUserList",method=RequestMethod.GET)
	@ResponseBody
	public Map getCurrentUserList(HttpServletRequest request) {
		
		init(request);

		AuthUser user=getUser(request,false);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List userList=new ArrayList();

		log.info("getCurrentUserList()");
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			userList=service.getCurrentUserList();
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("user_list",userList);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getProcessNameList <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/getProcessNameList",method=RequestMethod.GET)
	@ResponseBody
	public Map getProcessNameList(HttpServletRequest request) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List processList=new ArrayList();

		log.info("getProcessNameList()");
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			processList=service.getProcessNameList();
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("process_list",processList);

		return map;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getAssignedProcessNameList <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/getAssignedProcessNameList",method=RequestMethod.GET)
	@ResponseBody
	public Map getAssignedProcessNameList(HttpServletRequest request) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List processList=new ArrayList();

		log.info("getAssignedProcessNameList()");
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			processList=service.getAssignedProcessNameList();
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("process_list",processList);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getAssignedTaskList <br/>
	* Authentication required: true
	*/
	@RequestMapping(value={"getAssignedTaskList","/getAssignedTaskList/"},method=RequestMethod.GET)
	@ResponseBody
	public Map getAssignedTaskList(HttpServletRequest request,
												@RequestParam(value="fromIndex", required=false,defaultValue="0") int fromIndex,
												@RequestParam(value="pageSize", required=false,defaultValue="20") int pageSize,
												@RequestParam(value="sort", required=false,defaultValue="DEFAULT") String sort
	) {
	
		return getAssignedTaskList(request,"",fromIndex,pageSize,sort);
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/createInstance <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/createInstance",method=RequestMethod.POST)
	@ResponseBody
	public Map createInstance(HttpServletRequest request,
			                                    @RequestBody String requestBody)
    {
	
		return _createInstance(requestBody);
	}
	public Map _createInstance(String requestBody) {
		
	    int resultCode;
	    String resultMessage;
		Map varMap=new HashMap();;
				
		try {
	
			String[] cmd={
				"python",
				"/home/atuser/workflow_connector/createWfInstance.py",
				requestBody
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
			
			log.info("...createWfInstance.py....process="+process);
			
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
				if(responseMessage!=null && responseMessage.length()>0) 
					varMap=JsonUtil.jsonToMap(responseMessage);
			}
								
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		return varMap;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/notifyWikiUpdate <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/notifyWikiUpdate",method=RequestMethod.POST)
	@ResponseBody
	public Map notifyWikiUpdate(HttpServletRequest request,
			                                    @RequestBody String requestBody)
    {
	
		return _notifyWikiUpdate(requestBody);
	}
	public Map _notifyWikiUpdate(String requestBody) {
		
	    int resultCode;
	    String resultMessage;
		Map varMap=new HashMap();;
				
		try {
	
			String[] cmd={
				"python",
				"/home/atuser/workflow_connector/notifyWikiUpdate.py",
				requestBody
			};
			
			log.info("cmd:");
			for(int i=0;i<cmd.length;i++) {
				log.info("cmd["+i+"]="+cmd[i]);
			}

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
			
			log.info("...notifyWikiUpdate.py....process="+process);
			
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
				if(responseMessage!=null && responseMessage.length()>0) 
					varMap=JsonUtil.jsonToMap(responseMessage);
			}
								
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage=e.getMessage();
		}
				
		return varMap;
	}
	/* 
	* http://localhost:8080/workflowService/at/webservice/1.0/getAssignedTaskList <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/getAssignedTaskList/{processName:.+}",method=RequestMethod.GET)
	@ResponseBody
	public Map getAssignedTaskList(HttpServletRequest request,
												@PathVariable String processName,
												@RequestParam(value="fromIndex", required=false,defaultValue="0") int fromIndex,
												@RequestParam(value="pageSize", required=false,defaultValue="20") int pageSize,
												@RequestParam(value="sort", required=false,defaultValue="DEFAULT") String sort
	) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List instanceList=new ArrayList();

		log.info("getAssignedTaskList()..processName="+processName);
		
		log.info("fromIndex="+fromIndex);
		log.info("pageSize="+pageSize);
		log.info("sort="+sort);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			service.setPageAndSortOrder(fromIndex,pageSize,sort);
			instanceList=service.getAssignedTaskList(processName);
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("instance_list",instanceList);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getOwnedTaskList <br/>
	* Authentication required: true
	*/
	@RequestMapping(value={"getOwnedTaskList","/getOwnedTaskList/"},method=RequestMethod.GET)
	@ResponseBody
	public Map getOwnedTaskList(HttpServletRequest request) {
	
		return getOwnedTaskList(request,"");
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getOwnedTaskList
	*/
	@RequestMapping(value="/getOwnedTaskList/{processName:.+}",method=RequestMethod.GET)
	@ResponseBody
	public Map getOwnedTaskList(HttpServletRequest request,
												@PathVariable String processName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List instanceList=new ArrayList();

		log.info("getOwnedTaskList()..processName="+processName);
				
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			instanceList=service.getOwnedTaskList(processName);
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("instance_list",instanceList);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/getGlobalVar <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/getGlobalVar",method=RequestMethod.GET)
	@ResponseBody
	public Map getGlobalVar(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("getGlobalVar()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			varMap=service.getGlobalVar(instanceName);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
		
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		
		map.putAll(varMap);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/getGlobalVar <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/getLocalVar",method=RequestMethod.GET)
	@ResponseBody
	public Map getLocalVar(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("getGlobalVar()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			varMap=service.getLocalVar(instanceName);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		
		map.putAll(varMap);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/setGlobalVar <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/setGlobalVar",method=RequestMethod.POST)
	@ResponseBody
	public Map setGlobalVar(HttpServletRequest request,
												@PathVariable String instanceName,
												@RequestBody String requestBody) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		log.info("setGlobalVar()..instanceName="+instanceName);
		log.info("requestBody="+requestBody);
		
		Map jsonMap=JsonUtil.jsonToMap(requestBody);

		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			boolean isSuccess=service.setGlobalVar(instanceName,jsonMap);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
		
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/setLocalVar <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/setLocalVar",method=RequestMethod.POST)
	@ResponseBody
	public Map setLocalVar(HttpServletRequest request,
												@PathVariable String instanceName,
												@RequestBody String requestBody) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		log.info("setLocalVar()..instanceName="+instanceName);
		log.info("requestBody="+requestBody);
		
		Map jsonMap=JsonUtil.jsonToMap(requestBody);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			boolean isSuccess=service.setLocalVar(instanceName,jsonMap);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/getExternalUrlLink <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/getExternalUrlLink",method=RequestMethod.GET)
	@ResponseBody
	public Map getExternalUrlLink(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		String extrenalUrl="";

		log.info("getExternalUrlLink()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			extrenalUrl=service.getExternalUrlLink(instanceName);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		map.put("url",extrenalUrl);

		return map;
	}
		
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/getWorkflowUIInfo <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/getWorkflowUIInfo",method=RequestMethod.GET)
	@ResponseBody
	public Map getWorkflowUIInfo(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("getWorkflowUIInfo()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			varMap=service.getWorkflowUIInfo(application.getRealPath("/"),instanceName);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();
			
		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
				
		Map map=new HashMap();
		
		map.putAll(varMap);
		
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		log.info("map="+map);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/checkRallyDescription <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/checkRallyDescription",method=RequestMethod.GET)
	@ResponseBody
	public Map checkRallyDescription(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		Map responseMap=new HashMap();
		
		String extrenalUrl="";

		log.info("checkRallyDescription()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			responseMap=service.checkRallyDescription(application.getRealPath("/"),instanceName);
			
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);
		
		map.putAll(responseMap);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{bonita_instanceName}/runTask <br/>
	* Authentication required: true
	*/
	@RequestMapping(value="/{instanceName:.+}/runTask",method=RequestMethod.GET)
	@ResponseBody
	public Map runTask(HttpServletRequest request,
												@PathVariable String instanceName) {
		
		init(request);
		AuthUser user=getUser(request,true);
		if(user==null) {
			return getNoUserResponse();
		}
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		log.info("runTask()..instanceName="+instanceName);
		
		try {
			BonitaService service=new BonitaService(configFile);
			service.setUser(user);
			
			boolean isSuccess=service.runTask(instanceName);
						
			log.info("runTask isSuccess="+isSuccess);
						
			resultCode=service.getResultCode();
			resultMessage=service.getResultMessage();

		} catch(Exception e) {
			log.error("",e);
			resultCode=RESULT_EXCEPTION;
			resultMessage="exception:"+e.toString();
		}
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewUSFromRally <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/checkNewUSFromRally",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewUSFromRally(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		RallyService service=new RallyService(configFile);
		service.checkNewUSFromRally(usListFile);

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewUS <br/>
	* processes is a property key, comma separated format
	* Authentication required: false
	*/
	@RequestMapping(value="/checkNewUS",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewUS(HttpServletRequest request,
		@RequestParam(value="processes", required=true) String processes
	) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		RallyService service=new RallyService(configFile);
		
		String[] processesArr=processes.split(",");
		for(int i=0;i<processesArr.length;i++) {
			String process=processesArr[i];
			service.checkNewUS(application.getRealPath("/data"),process);
		}

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewTAFromRally <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/checkNewTAFromRally",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewTAFromRally(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		RallyService service=new RallyService(configFile);
		service.checkNewTAFromRally(taskListFile);

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}

}
