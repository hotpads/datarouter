package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;

public class MavEncoder implements Encoder{
	
	private Logger logger = Logger.getLogger(MavEncoder.class);
	
	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException{
		Mav mav;
		try{
			mav = (Mav) result;
		}catch(ClassCastException e){
			mav = new MessageMav("Please specify an encoder for your @Handler.");
		}
		try{
			if(mav==null){
				
			}else if(mav.isRedirect()){
				response.sendRedirect(mav.getRedirectUrl());
				
			}else{
				response.setContentType(mav.getContentType());
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

}
