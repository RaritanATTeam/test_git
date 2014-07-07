package com.raritan.at.filter;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.security.*;

import org.bonitasoft.console.security.server.api.*;
import org.bonitasoft.console.security.server.api.impl.*;
import org.bonitasoft.console.security.server.*;

import static com.raritan.at.workflow.service.Constant.*;
import com.raritan.at.util.*;
import com.raritan.at.workflow.bean.*;
import com.raritan.at.workflow.service.*;

import org.apache.log4j.Logger;

public class WebAuthenticationFilter extends org.jasig.cas.client.util.AbstractCasFilter {

	protected final Logger log = Logger.getLogger(this.getClass());
      
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpSession session = request.getSession();
		
		CredentialsEncryptionAPIImpl credEncAPI = CredentialsEncryptionAPIImpl.getInstance();
		String username = request.getRemoteUser();
		String password="";
		 log.info("WebAuthenticationFilter username="+username);
		 log.info("WebAuthenticationFilter password="+password);
		
		String encryptedCredentials;
		try {
			encryptedCredentials = credEncAPI.encryptCredential(username);
			session.setAttribute(LoginServlet.USER_CREDENTIALS_SESSION_PARAM_KEY, encryptedCredentials);
			
			AuthUser user=new AuthUser();
			user.setName(username);
			user.setPassword(password);
		
			session.setAttribute(USER_BEAN,user);			
			
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		chain.doFilter(servletRequest, servletResponse);
    }
}
