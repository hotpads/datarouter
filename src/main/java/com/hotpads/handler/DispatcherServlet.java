package com.hotpads.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.hotpads.util.core.ListTool;

@SuppressWarnings("serial")
@Singleton
public abstract class DispatcherServlet extends HttpServlet {
	
	protected String servletContextPath;
	protected Injector injector;
	
	protected List<BaseDispatcher> dispatchers = ListTool.createArrayList();;
	//...add more dispatchers
	
	
	@Override
	public void init() throws ServletException{
		servletContextPath = getServletContext().getContextPath();
		injector = Preconditions.checkNotNull(
				(Injector)getServletContext().getAttribute(Injector.class.getName()));
		registerDispatchers();
	}
	
	public abstract void registerDispatchers();
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		
		boolean handled = false;
		for(BaseDispatcher dispatcher : dispatchers){
			handled = dispatcher.handleRequestIfUrlMatch(getServletContext(), request, response);
			if(handled){ break; }
		}
		
		if(!handled){
			response.setStatus(404);
			out.println("path not found");
		}
		
		out.flush();
		out.close();
		
	}

	

}
