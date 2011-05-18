package com.hotpads.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.util.core.exception.PermissionException;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * a dispatcher servlet sets necessary parameters and then calls "handle()"
 */
public abstract class BaseHandler{
	protected Logger logger = Logger.getLogger(getClass());
	
	//these are available to all handlers without passing them around
	protected ServletContext servletContext;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Params params;
	protected PrintWriter out;
	
	//returns url match regex.  dispatcher servlet calls this on container startup to build url mappings
	//..could also map the url's externally so they're in a centralized place
//	abstract String handles();
	
	protected static final String DEFAULT_HANDLER_METHOD_NAME = "handleDefault";
	@Handler protected Mav handleDefault(){
		return new MessageMav().addObject("message", "no default handler method found, please specify "
				+handlerMethodParamName());
	}
	
	
	/*
	 * handler methods in sub-classes will need this annotation as a security measure, 
	 *   otherwise all methods would be callable
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Handler{
	}
	
	
	void handleWrapper(){//dispatcher servlet calls this
		try{
			permitted();
			Method method;
			try{
				method = ReflectionTool.getDeclaredMethodFromHierarchy(getClass(), handlerMethodName());
				if(method==null || !method.isAnnotationPresent(Handler.class)){
					throw new PermissionException("no such handler "+handlerMethodParamName()+"="+handlerMethodName());
				}
//			}catch(NoSuchMethodException e){
//				throw new RuntimeException(e);
			}catch(IllegalArgumentException e){
				throw new RuntimeException(e);
			}catch(SecurityException e){
				throw new RuntimeException(e);
			}
			Mav resultMav;
			try{
				resultMav = (Mav)method.invoke(this, new Object[]{});
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}catch(InvocationTargetException e){
//				throw new RuntimeException(e);
				throw (RuntimeException)e.getCause();
			}
			finishRequest(resultMav);
		}catch(Exception e){
			if(e instanceof RuntimeException){ throw (RuntimeException)e; }
			throw new RuntimeException(e);
		}
	}
	

	/****************** optionally override these *************************/
	
	boolean permitted(){  
		//allow everyone by default
		return true;
		//override if necessary
		//could also have a filter with more authentication somewhere else
	}
	
	String handlerMethodName(){
		return params.optional(handlerMethodParamName(), DEFAULT_HANDLER_METHOD_NAME);
	}
	
	public String handlerMethodParamName(){
		return "submitAction";
	}

	Long slowMs(){ 
		return 100L; 
	}
	
	void finishRequest(Mav mav) throws ServletException{
		try{
			if(mav==null){
				
			}else if(mav.isRedirect()){
				response.sendRedirect(mav.getRedirectUrl());
				
			}else{
				response.setContentType(mav.getContentType());
				//add the model variables as request attributes
				appendMavToRequest(request, mav);
				
				//forward to the jsp
				String targetContextName = mav.getContext();
				String viewName = mav.getViewName();
				ServletContext targetContext = servletContext;
				if(targetContextName != null){
					targetContext = servletContext.getContext(targetContextName);
					if(targetContext==null){
						logger.error("Could not acquire servletContext="+targetContextName
								+".  Make sure context has crossContext=true enabled.");
					}
				}
				RequestDispatcher dispatcher = targetContext.getRequestDispatcher(viewName);
				dispatcher.include(request, response);
			}
			
			//the container should take care of this...
//			out.flush();
//			out.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	protected void p(String s){
//		try{
			out.write(s);
//		}catch(IOException e){
//			throw new RuntimeException(e);
//		}
	}


	protected void appendMavToRequest(HttpServletRequest request, Mav mav){
		for(String key : mav.getModel().keySet()){
			request.setAttribute(key, mav.getModel().get(key));
		}
	}
	
	/****************** get/set *******************************************/
	
	public void setParams(Params params){
		this.params = params;
	}

	public void setOut(PrintWriter out){
		this.out = out;
	}

	public void setServletContext(ServletContext context){
		this.servletContext = context;
	}

	public void setRequest(HttpServletRequest request){
		this.request = request;
	}

	public void setResponse(HttpServletResponse response){
		this.response = response;
	}
	
	

	
}
