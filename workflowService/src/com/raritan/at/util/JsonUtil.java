package com.raritan.at.util;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class JsonUtil {
	
	public static String encodeObj(JSONObject obj) {
		StringWriter out = new StringWriter();
   	  	try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
   	  	String jsonText = out.toString();
   	  	System.out.print(jsonText);
		return jsonText;
	}

	public static Map jsonToMap(String jsonText) {
		Map map=new LinkedHashMap();
	
		jsonText=URLDecoder.decode(jsonText);
		
		//System.out.println("jsonText="+jsonText);
	
		try {
			JSONParser parser=new JSONParser();
			map=(Map)parser.parse(jsonText);
		} catch(Exception e) {
			e.printStackTrace();
		}
	
		return map;
	}
}
