package com.hotpads.handler.exception;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.exception.http.HttpException;
import com.hotpads.util.core.exception.http.imp.Http500InternalServerErrorException;

@Singleton public class ExceptionHandlingFilter implements Filter{
	Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	private static final String ERROR = "/error";

	@Override
	public void init(FilterConfig arg0) throws ServletException{
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		try{
			fc.doFilter(req, res);

		}catch(Exception e){
			HttpException httpException;
			if(e instanceof HttpException){
				httpException = (HttpException)e;
			}else{
				httpException = new Http500InternalServerErrorException(null, e);
			}
			logger.warn(ExceptionTool.getStackTraceAsString(httpException));
			HttpSession session = request.getSession();
			session.setAttribute("statusCode", httpException.getStatusCode());

			// something else needs to set this, like an AuthenticationFilter
//			Object displayExceptionInfo = request.getAttribute(PARAM_DISPLAY_EXCEPTION_INFO);
//			if(displayExceptionInfo != null && ((Boolean)displayExceptionInfo)){
				String message = httpException.getClass().getSimpleName() + ": " + e.getMessage();
				session.setAttribute("message", message);

				session.setAttribute("stackTrace", httpException.getStackTrace());
				session.setAttribute("stackTraceString", ExceptionTool
						.getStackTraceStringForHtmlPreBlock(httpException));
//			}
			// RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/generic/exception.jsp");
			// dispatcher.forward(request, response);
			response.sendRedirect(request.getContextPath() + ERROR);

		}finally{
		}
	}

	@Override
	public void destroy(){
	}

}