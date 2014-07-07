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

import org.ow2.bonita.util.*;

import static com.raritan.at.workflow.service.Constant.*;
import com.raritan.at.util.*;
import com.raritan.at.workflow.bean.*;
import com.raritan.at.workflow.service.*;

public class BaseAPI {

	protected final Logger log = Logger.getLogger(this.getClass());
	
	protected String configFile;
	protected String taskListFile;
	protected String usListFile;
	
	protected Properties confProperties;
			
	@Autowired
    protected ServletContext application;
			
	public BaseAPI()  {
		
	}

	protected void init(HttpServletRequest request) {
		configFile=application.getRealPath("/")+"/WEB-INF/conf/config.xml";
		taskListFile=application.getRealPath("/")+"/data/task_list.xml";
		usListFile=application.getRealPath("/")+"/data/us_list.xml";
		
		try {
			confProperties=new Properties();
    		confProperties.loadFromXML(new FileInputStream(configFile));
    	} catch(Exception e) {
    		log.error("",e);
    	}
		
		String JAAS_FILE_PATH = application.getRealPath("/")+"/WEB-INF/classes/jaas-standard.cfg";
		System.setProperty(BonitaConstants.JAAS_PROPERTY, JAAS_FILE_PATH); 
		
		System.setProperty(BonitaConstants.API_TYPE_PROPERTY, "REST");
		System.setProperty(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY, "http://localhost:9080/bonita-server-rest/");
				
	}
	
	protected AuthUser getUser(HttpServletRequest request) {
	
		AuthUser user=null;
		
		HttpSession session=request.getSession();
		
		log.info("session="+session);
		
		if(session!=null) {
			user=(AuthUser)session.getAttribute(USER_BEAN);
		}
		
		log.info("getUser.. user="+user);
		
		return user;
	}
	
	protected AuthUser getUser(HttpServletRequest request,boolean required) {
	
		AuthUser user=new AuthUser();

		if(required) {
			String credentials=request.getHeader("Authorization");
	
			credentials=credentials.replace("Basic","");		
			String decodedStr=credentials.trim();
			decodedStr = new String(Base64.decodeBase64(decodedStr));
			String[] splitArr=decodedStr.split(":");
			
			String userName=splitArr[0];
			String password=splitArr[1];
						
			BonitaService service=new BonitaService(configFile);
	
			boolean isPass=service.validateUser(userName,password);
			
			log.info("userName="+userName);
			log.info("password="+password);
			log.info("isPass="+isPass);
			
			if(isPass) {			
				HttpSession session=request.getSession();
				
				user.setName(userName);
				user.setPassword(password);
				
			} else {
				user=null;
			}
		
		} else {
		
    		String bonitaApiHttpUsername=confProperties.getProperty("bonita.api.http.username");
    		String bonitaApiHttpPassword=confProperties.getProperty("bonita.api.http.password");
		
			user.setName(bonitaApiHttpUsername);
			user.setPassword(bonitaApiHttpPassword);
		
		}
		
		log.info("getUser.. user="+user);
		
		return user;
	}
	
	protected Map getNoUserResponse() {
	
		Map map=new HashMap();
				
		map.put("ret",RESULT_FAIL);
		map.put("msg","User not login");

		return map;
	}
	


}
