package com.hotpads.handler.https;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.util.core.StringTool;

public abstract class AbstractHttpsFilter implements Filter {
	protected Logger logger = Logger.getLogger(getClass());

	
	protected abstract UrlScheme getRequiredScheme(String path);

	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		/********************** parse request ****************************/

		UrlScheme scheme = UrlScheme.fromRequest(req);
		String path = request.getServletPath();
        String pathInfo = StringTool.nullSafe(request.getPathInfo());
        
		/******* catch bogus http requests and redirect if necessary***********/

        UrlScheme requiredScheme = getRequiredScheme(path+pathInfo);
        	
        if(requiredScheme != null 
        		&& requiredScheme != UrlScheme.ANY 
        		&& requiredScheme != scheme){
        	String redirectUrl = UrlScheme.getUriWithScheme(requiredScheme,req);
        	
        	response.sendRedirect(response.encodeRedirectURL(redirectUrl));
        	return;
        }
		
		/******* no bogus requests found... continue normally *****************/
		fc.doFilter(req, res);
	}
		
	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
	
	@Override
	public void destroy() {
	}

}