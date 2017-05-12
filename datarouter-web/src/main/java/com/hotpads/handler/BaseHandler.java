package com.hotpads.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.handler.encoder.MavEncoder;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.handler.exception.HandledException;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.params.Params;
import com.hotpads.handler.types.DefaultDecoder;
import com.hotpads.handler.types.HandlerDecoder;
import com.hotpads.handler.types.HandlerTypingHelper;
import com.hotpads.handler.user.authenticate.AdminEditUserHandler;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.ResponseTool;

/*
 * a dispatcher servlet sets necessary parameters and then calls "handle()"
 */
public abstract class BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);

	private static final String DEFAULT_HANDLER_METHOD_NAME = "handleDefault";
	private static final String MISSING_PARAMETERS_HANDLER_METHOD_NAME = "handleMissingParameters";

	@Inject
	private HandlerTypingHelper handlerTypingHelper;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private HandlerCounters handlerCounters;

	//these are available to all handlers without passing them around
	protected ServletContext servletContext;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Params params;
	/**
	 * @deprecated instead return a Object that will be serialized by the {@link HandlerEncoder}.
	 */
	@Deprecated
	protected Lazy<PrintWriter> out;


	/**
	 * @deprecated  Replaced with @Handler(defaultHandler = true) annotation
	 */
	@Deprecated
	@Handler
	protected Object handleDefault() throws Exception{
		MessageMav mav = new MessageMav("no default handler method found, please specify " + handlerMethodParamName());
		mav.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
		return mav;
	}

	@Handler
	protected Object handleMissingParameters(List<String> missingParameters, String methodName){
		MessageMav mav = new MessageMav("missing parameters for " + methodName + ": "
				+ String.join(", ", missingParameters));
		mav.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
		return mav;
	}

	/*
	 * handler methods in sub-classes will need this annotation as a security measure,
	 *   otherwise all methods would be callable
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Handler{
		Class<?>[] expectedParameterClasses() default {};
		Class<?> expectedParameterClassesProvider() default Object.class;
		String description() default "";
		Class<? extends HandlerEncoder> encoder() default MavEncoder.class;
		Class<? extends HandlerDecoder> decoder() default DefaultDecoder.class;
		boolean defaultHandler() default false;
	}

	public void handleWrapper(){//dispatcher servlet calls this
		try{
			permitted();
			String methodName = handlerMethodName();
			Collection<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(getClass(), methodName)
					.stream()
					.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
					.collect(Collectors.toList());
			Pair<Method, Object[]> pair = handlerTypingHelper.findMethodByName(possibleMethods, request);
			Method method = pair.getLeft();
			Object[] args = pair.getRight();
			if(method == null){
				Optional<Method> defaultHandlerMethod = getDefaultHandlerMethod();
				if(defaultHandlerMethod.isPresent()){
					pair = handlerTypingHelper.findMethodByName(Arrays.asList(defaultHandlerMethod.get()), request);
					method = pair.getLeft();
					args = pair.getRight();
				}
			}
			if(method == null){
				if(possibleMethods.size() > 0){
					Method desiredMethod = DrCollectionTool.getFirst(possibleMethods);
					List<String> missingParameters = getMissingParameterNames(desiredMethod);
					args = new Object[]{missingParameters, desiredMethod.getName()};

					methodName = MISSING_PARAMETERS_HANDLER_METHOD_NAME;
					method = ReflectionTool.getDeclaredMethodFromHierarchy(getClass(), methodName, List.class,
							String.class);
				}else{
					methodName = DEFAULT_HANDLER_METHOD_NAME;
					method = ReflectionTool.getDeclaredMethodFromHierarchy(getClass(), methodName);
				}
			}

			HandlerEncoder encoder;
			if(method.isAnnotationPresent(Handler.class)){
				encoder = injector.getInstance(method.getAnnotation(Handler.class).encoder());
			}else{
				encoder = new MavEncoder();
			}
			Object result;
			if(args == null){
				args = new Object[]{};
			}
			handlerCounters.incMethodInvocation(this, method);
			try{
				result = method.invoke(this, args);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}catch(InvocationTargetException e){
				Throwable cause = e.getCause();
				if(cause instanceof HandledException){
					Exception handledException = (Exception)cause;//don't allow HandledExceptions to be Throwable
					String exceptionLocation = getClass().getName() + "/" + method.getName();
					exceptionRecorder.tryRecordException(handledException, exceptionLocation);
					encoder.sendExceptionResponse((HandledException)cause, servletContext, response, request);
					logger.warn(e.getMessage());
					return;
				}else if(cause instanceof RuntimeException){
					throw (RuntimeException)cause;
				}else{
					throw new RuntimeException(cause);
				}
			}
			encoder.finishRequest(result, servletContext, response, request);
		}catch(IOException | ServletException e){
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
		String fullPath = request.getServletPath() + DrStringTool.nullSafe(request.getPathInfo());
		String lastPathSegment = getLastPathSegment(fullPath);
		return params.optional(handlerMethodParamName()).orElse(lastPathSegment);
	}

	private Optional<Method> getDefaultHandlerMethod(){
		Optional<Method> defaultHandlerMethod = Stream.of(getClass().getDeclaredMethods())
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.filter(possibleMethod -> possibleMethod.getDeclaredAnnotation(Handler.class).defaultHandler())
				.findFirst();
		if(defaultHandlerMethod.isPresent()){
			defaultHandlerMethod.get().setAccessible(true);
		}
		return defaultHandlerMethod;
	}

	private String getLastPathSegment(String uri){
		if(uri == null){
			return "";
		}
		return uri.replaceAll("[^?]*/([^/?]+)[/?]?.*", "$1");
	}

	protected String handlerMethodParamName(){
		return "submitAction";
	}

	private List<String> getMissingParameterNames(Method method){
		return Stream.of(method.getParameters())
				.map(Parameter::getName)
				.filter(param -> !params.toMap().keySet().contains(param))
				.collect(Collectors.toList());
	}

	/****************** get/set *******************************************/

	public HttpServletRequest getRequest(){
		return request;
	}

	public void setParams(Params params){
		this.params = params;
	}

	public void setServletContext(ServletContext context){
		this.servletContext = context;
	}

	public void setRequest(HttpServletRequest request){
		this.request = request;
	}

	public void setResponse(HttpServletResponse response){
		this.response = response;
		this.out = Lazy.of(() -> ResponseTool.getWriter(response));
	}

	public static class BaseHandlerTests{

		private BaseHandler test;

		@Before
		public void setup(){
			test = new AdminEditUserHandler();
		}

		@Test
		public void testGetLastPathSegment(){
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
