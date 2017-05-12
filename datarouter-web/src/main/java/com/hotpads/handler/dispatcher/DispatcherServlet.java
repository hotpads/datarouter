package com.hotpads.handler.dispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.InjectorRetriever;
import com.hotpads.pontoon.config.Dispatcher;

@SuppressWarnings("serial")
@Singleton
public abstract class DispatcherServlet extends HttpServlet implements InjectorRetriever{

	protected String servletContextPath;
	protected DatarouterInjector injector;

	protected List<BaseDispatcher> dispatchers = new ArrayList<>();

	private Dispatcher dizpatcher;

	@Override
	public void init(){
		servletContextPath = getServletContext().getContextPath();
		injector = getInjector(getServletContext());
		dizpatcher = injector.getInstance(Dispatcher.class);
		registerDispatchers();
	}

	public abstract void registerDispatchers();

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException{

		response.setContentType("text/plain");
		response.setHeader("X-Frame-Options", "SAMEORIGIN"); //clickjacking protection

		boolean handled = false;
		for(BaseDispatcher dispatcher : dispatchers){
			handled = dizpatcher.handleRequestIfUrlMatch(request, response, dispatcher);
			if(handled){
				break;
			}
		}

		if(!handled){
			response.setStatus(404);
			PrintWriter out = response.getWriter();
			out.print(getClass().getCanonicalName() + " could not find Handler for " + request.getRequestURI());
			out.close(); // Tomcat is already closing this for us.
		}
	}

}
