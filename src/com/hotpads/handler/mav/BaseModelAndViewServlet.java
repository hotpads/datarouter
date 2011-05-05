package com.hotpads.handler.mav;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.hotpads.util.core.ExceptionTool;

@SuppressWarnings("serial")
@Singleton
public abstract class BaseModelAndViewServlet extends HttpServlet {
	protected Logger logger = Logger.getLogger(getClass());
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		
		try{
			
			//run the subclass's code to build the ModelAndView
			ModelAndView mav = handleRequestInternal(request, response);
			
			if(mav.isRedirect()){
				response.sendRedirect(mav.getRedirectUrl());
				
			}else{
				//add the model variables as request attributes
				appendMavToRequest(request, mav);
				
				//forward to the jsp
				String viewContext = mav.getContext();
				String viewName = mav.getViewName();
				ServletContext servletContext = getServletContext();
				if(viewContext != null){
					servletContext = getServletContext().getContext(viewContext);
					if(servletContext==null){
						logger.error("Could not acquire servletContext="+viewContext+".  Make sure context has crossContext=true enabled.");
					}
				}
				RequestDispatcher dispatcher = servletContext.getRequestDispatcher(viewName);
				dispatcher.include(request, response);
			}

		}catch(Exception e){
			out.print("We're sorry, but an error has occurred.<br/><br/>");
			out.print(ExceptionTool.getStackTraceAsHtmlString(e));
		}
		
		out.flush();
		out.close();
		
	}
	

	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	throws Exception;

	protected void appendMavToRequest(HttpServletRequest request, ModelAndView mav){
		for(String key : mav.getModel().keySet()){
			request.setAttribute(key, mav.getModel().get(key));
		}
	}
}
