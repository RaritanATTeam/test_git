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

@Controller("RestfulAgileAPI")
public class RestfulAgileAPI extends BaseAPI {
			
	public RestfulAgileAPI()  {
		
	}
	
	@Override
	protected AuthUser getUser(HttpServletRequest request) {
	
		AuthUser user=new AuthUser();
		
		String credentials=request.getHeader("Authorization");

		credentials=credentials.replace("Basic","");		
		String decodedStr=credentials.trim();
		decodedStr = new String(Base64.decodeBase64(decodedStr));
		String[] splitArr=decodedStr.split(":");

		String userName="";
		
		if("".equals(userName)) {
			userName=splitArr[0];
		}	
		
		if(userName!=null && !"".equals(userName)) {

			user.setName(userName);
			user.setPassword("");
	
		} else {

    		String bonitaApiHttpUsername=confProperties.getProperty("bonita.api.http.username");
    		String bonitaApiHttpPassword=confProperties.getProperty("bonita.api.http.password");
		
			user.setName(bonitaApiHttpUsername);
			user.setPassword(bonitaApiHttpPassword);
	
		}
		
		log.info("getUser.. user="+user);
		
		return user;
	}
	
	/**
	* http://localhost:8080/workflowService/at/webservice/1.0/getAgileUserList <br/>
	* Authentication required: false
	*/
	@RequestMapping(value="/getAgileUserList",method=RequestMethod.GET)
	@ResponseBody
	public Map getAgileUserList(HttpServletRequest request) {
	
		log.info("getAgileUserList()");
		
		init(request);

		AuthUser user=getUser(request);
		if(user==null) {
			return getNoUserResponse();
		}
		
		log.info("user="+user);
		
		int resultCode=RESULT_EXCEPTION;
		String resultMessage="";
		
		List userList=new ArrayList();
		
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


}
