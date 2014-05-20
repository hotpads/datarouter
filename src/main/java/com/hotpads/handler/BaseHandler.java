package com.hotpads.handler;

import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.handler.encoder.MavEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.types.DefaultDecoder;
import com.hotpads.handler.types.HandlerDecoder;
import com.hotpads.handler.types.HandlerTypingHelper;
import com.hotpads.handler.user.authenticate.AdminEditUserHandler;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.collections.Pair;
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
	protected HandlerDecoder paramDeserializer;
	
	//returns url match regex.  dispatcher servlet calls this on container startup to build url mappings
	//..could also map the url's externally so they're in a centralized place
//	abstract String handles();
	
	protected static final String DEFAULT_HANDLER_METHOD_NAME = "handleDefault";
	
	protected BaseHandler(){
		this.paramDeserializer = new DefaultDecoder();
	}
	
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
		Class<?> encoder() default MavEncoder.class;
		Class<?> decoder() default Object.class;
	}
	
	void handleWrapper(){//dispatcher servlet calls this
		try{
			permitted();
			Method method = null;
			List<Object> args = ListTool.create();
			try{
				String methodName = handlerMethodName();
				if (!StringTool.isNullOrEmpty(methodName)) {
					Pair<Method, List<Object>> pair = HandlerTypingHelper.findMethodByName(this, methodName, paramDeserializer);
					method = pair.getLeft();
					args = pair.getRight();
				}
				if (method == null) {
					methodName = DEFAULT_HANDLER_METHOD_NAME;
					method = ReflectionTool.getDeclaredMethodFromHierarchy(getClass(), methodName);
				}
				if (method == null || !(method.isAnnotationPresent(Handler.class) || matchesDefaultHandlerMethod(methodName))) {
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
				result = method.invoke(this, args.toArray());
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
				encoder = (HandlerEncoder) method.getAnnotation(Handler.class).encoder().newInstance();
			}catch(Exception e){
				encoder = new MavEncoder();
			}
			encoder.finishRequest(result, servletContext, response, request);
			
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
		return params.optional(handlerMethodParamName(), getLastPathSegment(params.request.getPathInfo()));
	}
	
	String getLastPathSegment(final String uri) {
		return uri.replaceAll("[^?]*/([^/?]+)[/?]?.*", "$1");
	}
	
	boolean matchesDefaultHandlerMethod(String methodName){
		return DEFAULT_HANDLER_METHOD_NAME.equals(methodName);
	}
	
	public String handlerMethodParamName(){
		return "submitAction";
	}

	Long slowMs(){ 
		return 100L; 
	}
	
	protected void p(String s){
//		try{
			out.write(s);
//		}catch(IOException e){
//			throw new RuntimeException(e);
//		}
	}
	
	/****************** get/set 
	 * @return *******************************************/
	
	public Params getParams(){
		return params;
	}
	
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
	
	public void setParamSerializer(HandlerDecoder paramDeserializer){
		this.paramDeserializer = paramDeserializer;
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
