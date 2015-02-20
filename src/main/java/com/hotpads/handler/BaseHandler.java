package com.hotpads.handler;

import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.DatarouterInjector;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.handler.encoder.MavEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.types.DefaultDecoder;
import com.hotpads.handler.types.HandlerDecoder;
import com.hotpads.handler.types.HandlerTypingHelper;
import com.hotpads.handler.user.authenticate.AdminEditUserHandler;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.exception.PermissionException;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * a dispatcher servlet sets necessary parameters and then calls "handle()"
 */
public abstract class BaseHandler{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private HandlerTypingHelper handlerTypingHelper;
	@Inject
	private DatarouterInjector injector;
	
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
	
	@Handler
	protected Mav handleDefault() throws Exception {
		return new MessageMav("no default handler method found, please specify " + handlerMethodParamName());
	}
	
	/*
	 * handler methods in sub-classes will need this annotation as a security measure, 
	 *   otherwise all methods would be callable
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Handler {
		Class<?>[] expectedParameterClasses() default {};
		Class<?> expectedParameterClassesProvider() default Object.class;
		String description() default "";
		Class<? extends HandlerEncoder> encoder() default MavEncoder.class;
		Class<? extends HandlerDecoder> decoder() default DefaultDecoder.class;
	}
	
	protected void handleWrapper(){//dispatcher servlet calls this
		try{
			permitted();
			Method method = null;
			Object[] args = null;
			try{
				String methodName = handlerMethodName();
				if (!StringTool.isNullOrEmpty(methodName)) {
					Pair<Method, Object[]> pair = handlerTypingHelper.findMethodByName(this, methodName);
					method = pair.getLeft();
					args = pair.getRight();
				}
				if (method == null) {
					methodName = DEFAULT_HANDLER_METHOD_NAME;
					method = ReflectionTool.getDeclaredMethodFromHierarchy(getClass(), methodName);
				}
				if (method == null || !(method.isAnnotationPresent(Handler.class) 
						|| matchesDefaultHandlerMethod(methodName))) {
					throw new PermissionException("no such handler " + handlerMethodParamName() + "=" + methodName);
				}
//			}catch(NoSuchMethodException e){
//				throw new RuntimeException(e);
			}catch(IllegalArgumentException e){
				throw new RuntimeException(e);
			}catch(SecurityException e){
				throw new RuntimeException(e);
			}

			Object result;
			try{
				if(args == null){
					args = new Object[]{};
				}
				result = method.invoke(this, args);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}catch(InvocationTargetException e){
				Throwable cause = e.getCause();
				if(cause instanceof RuntimeException){ 
					throw (RuntimeException)cause; 
				}
				throw new RuntimeException(cause);
			}
			
			HandlerEncoder encoder;
			try{
				encoder = injector.getInstance(method.getAnnotation(Handler.class).encoder());
			}catch(Exception e){
				encoder = new MavEncoder();
			}
			encoder.finishRequest(result, servletContext, response, request);
			
		}catch(Exception e){
			if(e instanceof RuntimeException){ 
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
	}
	

	/****************** optionally override these *************************/
	
	private boolean permitted(){  
		//allow everyone by default
		return true;
		//override if necessary
		//could also have a filter with more authentication somewhere else
	}
	
	private String handlerMethodName(){
		return params.optional(handlerMethodParamName(), getLastPathSegment(params.request.getPathInfo()));
	}
	
	private String getLastPathSegment(String uri) {
		if(uri == null){
			return "";
		}
		return uri.replaceAll("[^?]*/([^/?]+)[/?]?.*", "$1");
	}
	
	private boolean matchesDefaultHandlerMethod(String methodName){
		return DEFAULT_HANDLER_METHOD_NAME.equals(methodName);
	}
	
	protected String handlerMethodParamName(){
		return "submitAction";
	}
	
	protected void p(String string){
		out.write(string);
	}
	
	/****************** get/set *******************************************/
	
	public HttpServletRequest getRequest(){
		return request;
	}
	
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
	
	public static class BaseHandlerTests {
		
		BaseHandler test;
		
		@Before
		public void setup() {
			test = new AdminEditUserHandler();
		}
		
		@Test
		public void testGetLastPathSegment() {
			Assert.assertEquals("something", test.getLastPathSegment("/something"));
			Assert.assertEquals("something", test.getLastPathSegment("~/something"));
			Assert.assertEquals("viewUsers", test.getLastPathSegment("/admin/edit/reputation/viewUsers"));
			Assert.assertEquals("viewUsers", test.getLastPathSegment("/admin/edit/reputation/viewUsers/"));
			Assert.assertEquals("editUser", test.getLastPathSegment("/admin/edit/reputation/editUser?u=10"));
			Assert.assertEquals("editUser", test.getLastPathSegment("/admin/edit/reputation/editUser/?u=10"));
			Assert.assertEquals("rep", test.getLastPathSegment("https://fake.url/t/rep?querystring=path/path"));
			Assert.assertEquals("rep", test.getLastPathSegment("https://fake.url/t/rep/?querystring=path/path"));
		}
	}
	
}
