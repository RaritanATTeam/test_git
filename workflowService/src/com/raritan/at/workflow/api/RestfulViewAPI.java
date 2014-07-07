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

import org.springframework.web.servlet.ModelAndView;

import org.apache.commons.codec.binary.Base64;

//import org.springframework.http.converter.*;
//import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

import org.ow2.bonita.util.*;

import static com.raritan.at.workflow.service.Constant.*;
import com.raritan.at.util.*;
import com.raritan.at.workflow.bean.*;
import com.raritan.at.workflow.service.*;

@Controller("RestfulViewAPI")
public class RestfulViewAPI extends BaseAPI {

	private ViewService service;
	public static String REPORT_SESSION_BEAN="report_session_bean";
	public static String REPORT_USER_SESSION_BEAN="report_user_session_bean";
	
	public RestfulViewAPI()  {
		
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getProjectList
	*/
	@RequestMapping(value="/getProjectList",method=RequestMethod.GET)
	@ResponseBody
	public List getProjectList(HttpServletRequest request) {
	
		init(request);
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
								
		List list=service.getProjectList();
		
		return list;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/4681584283/getIterationList
	*/
	@RequestMapping(value="/{projectId}/getIterationList",method=RequestMethod.GET)
	@ResponseBody
	public List getIterationList(HttpServletRequest request,
												@PathVariable String projectId) {
	
		init(request);
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
				
		log.info("00 service="+service);
				
		List list=service.getIterationList(projectId);
		
		return list;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/4681584283/getUserStoryList
	*/
	@RequestMapping(value="/{iterationId}/getUserStoryList",method=RequestMethod.GET)
	//@ResponseBody
	public void getUserStoryList(HttpServletRequest request,HttpServletResponse response,
												@PathVariable String iterationId) {
	
		init(request);
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
				
		HttpSession session=request.getSession();
		String sessionId=session.getId();
		
		List list=new ArrayList();
		
		try {
			response.setContentType("text/html");
			PrintWriter out=response.getWriter();
			//ServletOutputStream out = response.getOutputStream();
			
			log.info("1 response.getBufferSize="+response.getBufferSize());
			for(int i=0;i<820;i++) {
				out.println("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
				out.flush();
			}
			out.flush();
			
			list=service.getUserStoryList(sessionId,iterationId,out);
		} catch(Exception e) {
			log.error("",e);
		}
		
		session.setAttribute(REPORT_SESSION_BEAN,list);
		
		//return null;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getProcessStatus
	*/
	@RequestMapping(value="/getProcessStatus",method=RequestMethod.GET)
	@ResponseBody
	public Map getProcessStatus(HttpServletRequest request) {
	
		init(request);
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
		
		HttpSession session=request.getSession();
		String sessionId=session.getId();
		
		log.info("getProcessStatus..sessionId="+sessionId);
		log.info("22 service="+service);
		
		String status=service.getProcessStatus(sessionId);
		
		if("100%".equals(status)) {
			log.info("status has been 100%");
			status="0%";
		}
		
		Map map=new HashMap();
		
		map.put("ret","0");
		map.put("msg",status);
		
		log.info("map="+map);
		
		return map;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/exportToCSV
	*/
	@RequestMapping(value="/exportToCSV",method=RequestMethod.GET)
	//@ResponseBody
	public void exportToCSV(HttpServletRequest request,HttpServletResponse response) {
		StringBuilder csv=new StringBuilder();
		
		//request.addHeader("Accept", "plain/text");
		//response.setContentType("plain/text");
		
		String fileName=String.format("report_%1$tY%1$tm%1$te%1$tH%1$tM%1$tS", new Date())+".csv";
		
		try {
		
			HttpSession session=request.getSession();
			List list=(List)session.getAttribute(REPORT_SESSION_BEAN);
			
			service=ViewService.getInstance();
			service.setConfigFile(configFile);

			csv=service.listToCSV(list);
			
			PrintWriter out=response.getWriter();
			response.setContentType("plain/text");
			response.setContentLength(csv.length());
			response.addHeader("Content-Disposition", "attachment; filename="+fileName);
			
			out.println(csv.toString());
		
		} catch(Exception e) {
			log.error("",e);
		}

	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/exportToCSVForUserView
	*/
	@RequestMapping(value="/exportToCSVForUserView",method=RequestMethod.GET)
	public void exportToCSVForUserView(HttpServletRequest request,HttpServletResponse response) {
		StringBuilder csv=new StringBuilder();
		
		String fileName=String.format("user_report_%1$tY%1$tm%1$te%1$tH%1$tM%1$tS", new Date())+".csv";
		
		try {
		
			HttpSession session=request.getSession();
			List list=(List)session.getAttribute(REPORT_USER_SESSION_BEAN);
			
			service=ViewService.getInstance();
			service.setConfigFile(configFile);

			csv=service.listToCSVForUserView(list);
			
			PrintWriter out=response.getWriter();
			response.setContentType("plain/text");
			response.setContentLength(csv.length());
			response.addHeader("Content-Disposition", "attachment; filename="+fileName);
			
			out.println(csv.toString());
		
		} catch(Exception e) {
			log.error("",e);
		}

	}
	
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getUserList
	*/
	@RequestMapping(value="/getUserList",method=RequestMethod.GET)
	@ResponseBody
	public List getUserList(HttpServletRequest request) {
	
		init(request);
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
				
		List list=service.getUserList();
		
		//List list=service.getUserListAT();
		
		log.info("00 service="+service);
		log.info("00 list="+list);
		
		return list;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getUserTimeSpentByDate
	*/
	@RequestMapping(value="/{userObjectId}/getUserTimeSpentByDate",method=RequestMethod.GET)
	@ResponseBody
	public List getUserTimeSpentByDate(HttpServletRequest request,
											@PathVariable String userObjectId) {
	
		init(request);
		
		HttpSession session=request.getSession();
		String sessionId=session.getId();
		
		String startDate=request.getParameter("startDate");
		String endDate=request.getParameter("endDate");
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
				
		//List list=service.getUserList();
		
		List list=service.getUserTimeSpentByDate(userObjectId,startDate,endDate);
		
		session.setAttribute(REPORT_USER_SESSION_BEAN,list);
	
		return list;
	}

	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/TA1234/getTimeSpent
	*/
	@RequestMapping(value="/{taskNo}/getTimeSpent",method=RequestMethod.GET)
	//@ResponseBody
	public ModelAndView getTimeSpent(HttpServletRequest request,
											@PathVariable String taskNo) {
		init(request);
		
		double timeSpent=0.0D;
		
		HttpSession session=request.getSession();
		
		service=ViewService.getInstance();
		service.setConfigFile(configFile);
		
		try {
						
			timeSpent=service.getTimeSpentByTask(taskNo);
		
		} catch(Exception e) {
			log.error("",e);
		}
		
		request.setAttribute("timeSpent",new Double(timeSpent).toString());
		
		//return new Double(timeSpent).toString();
		
		return new ModelAndView("/timespent_view.jsp");
	}

}

