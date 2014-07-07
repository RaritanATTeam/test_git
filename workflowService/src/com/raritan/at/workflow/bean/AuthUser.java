package com.raritan.at.workflow.bean;

public class AuthUser {

	protected String name;
	protected String password;
	
	public AuthUser() {}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password=password;
	}
	
	public String toString() {
		return "userName:"+name;
	}

}