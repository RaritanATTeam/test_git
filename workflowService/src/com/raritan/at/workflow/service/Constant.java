package com.raritan.at.workflow.service;

public class Constant {

	public final static String RESULT_CODE="RESULT_CODE";
	public final static String RESULT_MESSAGE="RESULT_MESSAGE";
	public final static int RESULT_OK=0;
	public final static int RESULT_FAIL=1;
	public final static int RESULT_EXCEPTION=-1;
	
	public final static String PLANNING="Planning"; //Release,Iteration
	public final static String ACTIVE="Active"; //Release
	public final static String COMMITTED="Committed"; //Iteration
	
	public final static String DEFINED="Defined";
	public final static String IN_PROGRESS="In-Progress";
	public final static String COMPLETED="Completed";
	public final static String ACCEPTED="Accepted";	 //Release,Iteration,User Story
	
	public final static String NO_USER="NO_USER";
	public final static String NO_REF="NO_REF";
	public final static String NO_EMAIL="NO_EMAIL";
	
	public final static String DONT_ADD_BONITA="DONT_ADD_BONITA";
	
	public final static String USER_BEAN="userBean";
	
	public Constant() {}
	
}