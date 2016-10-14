package com.hotpads.handler;

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

@SuppressWarnings("serial")
@Singleton
public abstract class DispatcherServlet extends HttpServlet implements InjectorRetriever{

	protected String servletContextPath;
	protected DatarouterInjector injector;

	protected List<BaseDispatcher> dispatchers = new ArrayList<>();
	// ...add more dispatchers

	@Override
	public void init(){
		servletContextPath = getServletContext().getContextPath();
		injector = getInjector(getServletContext());
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
			handled = dispatcher.handleRequestIfUrlMatch(getServletContext(), request, response);
			if(handled){
				break;
			}
		}

		PrintWriter out = response.getWriter();
		if(!handled){
			response.setStatus(404);
			out.print(getClass().getCanonicalName() + " could not find Handler for " + request.getRequestURI());
		}

		out.close();
	}

}
