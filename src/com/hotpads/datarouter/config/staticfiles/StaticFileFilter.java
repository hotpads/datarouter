package com.hotpads.datarouter.config.staticfiles;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.hotpads.util.core.FileUtils;

@Singleton
public class StaticFileFilter implements Filter {
	static Logger logger = Logger.getLogger(StaticFileFilter.class);
	protected FilterConfig config;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		String contextPath = request.getContextPath();
		String path = request.getRequestURI().substring(contextPath.length());
		if (FileUtils.hasAStaticFileExtension(path)) {
			Enumeration<String> e = config.getInitParameterNames();
			while (e.hasMoreElements()) {
				String headerName = e.nextElement();
				String headerValue = config.getInitParameter(headerName);
				response.addHeader(headerName, headerValue);
			 }
		    request.getRequestDispatcher(contextPath + path).forward(request, response);
		    return;
		}
			//continue
			filterChain.doFilter(request, response);
	}
	
	@Override
	public void destroy() {
	}

}