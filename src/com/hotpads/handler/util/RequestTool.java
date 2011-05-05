package com.hotpads.handler.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.Predicate;

public class RequestTool {

	public static final String SUBMIT_ACTION = "submitAction";
	
	public static String getSubmitAction(HttpServletRequest request){
		String submitAction = request.getParameter(SUBMIT_ACTION);
		if(submitAction != null){ return submitAction; }
		throw new NullPointerException("param "+SUBMIT_ACTION+" not found");
	}
	
	public static String getSubmitAction(HttpServletRequest request, String defaultAction){
		String action = get(request, SUBMIT_ACTION, defaultAction);
		return action;
	}

	public static List<String> getSlashedUriParts(HttpServletRequest request){
		List<String> uriVars = Arrays.asList(request.getRequestURI().split("/"));
		uriVars = CollectionTool.filter( //get rid of blanks
					new Predicate<String>() {
						public boolean check(String t) {
							return t != null && !"".equals(t); 
						} 
					},
					uriVars);
		if(uriVars==null){ uriVars = new LinkedList<String>(); }
		return uriVars;
	}
	
	public static String get(HttpServletRequest request, String paramName){
		String stringVal = request.getParameter(paramName);
		if(stringVal == null){ throw new NullPointerException(paramName+" was not found in the request."); }
		return stringVal;
	}
	
	public static String get(HttpServletRequest request, String paramName, String defaultValue){
		String stringVal = request.getParameter(paramName);
		return stringVal==null ? defaultValue : stringVal;
	}
	
		
}
