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

	/**
	 * @deprecated use {@link #register(BaseRouteSet)}
	 */
	@Deprecated
	protected List<BaseRouteSet> dispatchers = new ArrayList<>();
	/**
	 * @deprecated you should not need that
	 */
	@Deprecated
	protected String servletContextPath;
	/**
	 * @deprecated you should not need that
	 */
	@Deprecated
	protected DatarouterInjector injector;

	private Dispatcher dispatcher;

	@Override
	public void init(){
		DatarouterInjector injector = getInjector(getServletContext());
		this.injector = injector;
		dispatcher = injector.getInstance(Dispatcher.class);
		registerDispatchers();
	}

	protected abstract void registerDispatchers();

	protected final void register(BaseRouteSet routeSet){
		dispatchers.add(routeSet);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException{

		response.setContentType("text/plain");
		response.setHeader("X-Frame-Options", "SAMEORIGIN"); //clickjacking protection

		boolean handled = false;
		for(BaseRouteSet dispatcherRoutes : dispatchers){
			handled = dispatcher.handleRequestIfUrlMatch(request, response, dispatcherRoutes);
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
