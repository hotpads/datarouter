package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.HandledException;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.http.ResponseTool;

public class MavEncoder implements HandlerEncoder{
	
	private static Logger logger = LoggerFactory.getLogger(MavEncoder.class);
	
	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException{
		Mav mav = (Mav) result;

		try{
			if(mav==null){
				
			}else if(mav.isRedirect()){
				response.sendRedirect(mav.getRedirectUrl());
				
			}else{
				response.setContentType(mav.getContentType());
				response.setStatus(mav.getStatusCode());
				//add the model variables as request attributes
				appendMavToRequest(request, mav);
				
				//forward to the jsp
				String targetContextName = mav.getContext();
				String viewName = mav.getViewName();
				ServletContext targetContext = servletContext;
				if(targetContextName != null){
					targetContext = servletContext.getContext(targetContextName);
					if(targetContext==null){
						logger.error("Could not acquire servletContext="+targetContextName
								+".  Make sure context has crossContext=true enabled.");
					}
				}
				RequestDispatcher dispatcher = targetContext.getRequestDispatcher(viewName);
				dispatcher.include(request, response);
			}
		
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	protected void appendMavToRequest(HttpServletRequest request, Mav mav){
		for(String key : mav.getModel().keySet()){
			request.setAttribute(key, mav.getModel().get(key));
		}
	}
	
	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext, 
			HttpServletResponse response, HttpServletRequest request) throws ServletException, IOException{
		ResponseTool.sendError(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());		
	}

}