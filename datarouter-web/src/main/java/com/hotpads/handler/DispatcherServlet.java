package com.hotpads.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
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
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException{

		response.setContentType("text/plain");

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
			out.print("path not found");
		}

		out.close();
	}

}
