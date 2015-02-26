package com.hotpads.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.DatarouterInjector;
import com.hotpads.datarouter.util.core.DrListTool;

@SuppressWarnings("serial")
@Singleton
public abstract class DispatcherServlet extends HttpServlet {
	
	protected String servletContextPath;
	protected DatarouterInjector injector;
	
	protected List<BaseDispatcher> dispatchers = DrListTool.createArrayList();
	//...add more dispatchers
	
	
	@Override
	public void init() throws ServletException{
		servletContextPath = getServletContext().getContextPath();
		injector = getInjector();
		registerDispatchers();
	}
	
	protected abstract DatarouterInjector getInjector();

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
