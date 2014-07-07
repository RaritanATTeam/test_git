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

@Controller("RestfulXPAPI")
public class RestfulXPAPI extends BaseAPI {
			
	public RestfulXPAPI()  {
		
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewRelease
	*/
	@RequestMapping(value="/checkNewRelease",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewRelease(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		XPService service=new XPService(configFile);
		
		service.checkNewRelease(application.getRealPath("/data"));

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkIterationsExist
	*/
	@RequestMapping(value="/{instanceName:.+}/checkIterationsExist",method=RequestMethod.GET)
	@ResponseBody
	public Map checkIterationsExist(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		log.info("checkIterationsExist()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkIterationsExist(instanceName);
			
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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkIterationsState
	*/
	@RequestMapping(value="/{instanceName:.+}/checkIterationsState",method=RequestMethod.GET)
	@ResponseBody
	public Map checkIterationsState(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("checkIterationsState()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkIterationsState(instanceName);

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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/changeReleaseState
	*/
	@RequestMapping(value="/{instanceName:.+}/changeReleaseState",method=RequestMethod.GET)
	@ResponseBody
	public Map changeReleaseState(HttpServletRequest request,
												@PathVariable String instanceName,
												@RequestParam(value="state", required=true) String state
												) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("changeReleaseState()..instanceName="+instanceName+" state="+state);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.changeReleaseState(instanceName,state);

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
		
		log.info("map="+map);
		
		return map;
	}


	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewIteration
	*/
	@RequestMapping(value="/checkNewIteration",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewIteration(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		XPService service=new XPService(configFile);
		
		service.checkNewIteration(application.getRealPath("/data"));

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkUserStoriesExist
	*/
	@RequestMapping(value="/{instanceName:.+}/checkUserStoriesExist",method=RequestMethod.GET)
	@ResponseBody
	public Map checkUserStoriesExist(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		log.info("checkUserStoriesExist()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkUserStoriesExist(instanceName);
			
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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkUserStoriesState
	*/
	@RequestMapping(value="/{instanceName:.+}/checkUserStoriesState",method=RequestMethod.GET)
	@ResponseBody
	public Map checkUserStoriesState(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("checkUserStoriesState()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkUserStoriesState(instanceName);

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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/changeIterationState
	*/
	@RequestMapping(value="/{instanceName:.+}/changeIterationState",method=RequestMethod.GET)
	@ResponseBody
	public Map changeIterationState(HttpServletRequest request,
												@PathVariable String instanceName,
												@RequestParam(value="state", required=true) String state
												) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("changeIterationState()..instanceName="+instanceName+" state="+state);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.changeIterationState(instanceName,state);

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
		
		log.info("map="+map);
		
		return map;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/checkNewUserStory
	*/
	@RequestMapping(value="/checkNewUserStory",method=RequestMethod.GET)
	@ResponseBody
	public Map checkNewUserStory(HttpServletRequest request) {
	
		init(request);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";

		XPService service=new XPService(configFile);
		
		service.checkNewUserStory(application.getRealPath("/data"));

		resultCode=service.getResultCode();
		resultMessage=service.getResultMessage();
		
		Map map=new HashMap();
				
		map.put("ret",resultCode);
		map.put("msg",resultMessage);

		return map;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkTasksExist
	*/
	@RequestMapping(value="/{instanceName:.+}/checkTasksExist",method=RequestMethod.GET)
	@ResponseBody
	public Map checkTasksExist(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		log.info("checkTasksExist()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkTasksExist(instanceName);
			
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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/checkTasksState
	*/
	@RequestMapping(value="/{instanceName:.+}/checkTasksState",method=RequestMethod.GET)
	@ResponseBody
	public Map checkTasksState(HttpServletRequest request,
												@PathVariable String instanceName) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("checkUserStoriesState()..instanceName="+instanceName);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.checkTasksAndTestResult(instanceName);

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
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/{instanceName}/changeUserStoryState
	*/
	@RequestMapping(value="/{instanceName:.+}/changeUserStoryState",method=RequestMethod.GET)
	@ResponseBody
	public Map changeUserStoryState(HttpServletRequest request,
												@PathVariable String instanceName,
												@RequestParam(value="state", required=true) String state
												) {
		init(request);
		
		AuthUser user=new AuthUser();
		user.setName("randy.chen@raritan.com");
		user.setPassword("bpm");
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		Map varMap=new HashMap();

		log.info("changeUserStoryState()..instanceName="+instanceName+" state="+state);
		
		try {
			XPService service=new XPService(configFile);
			service.setUser(user);

			service.changeUserStoryState(instanceName,state);

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
		
		log.info("map="+map);
		
		return map;
	}


}
