package com.hotpads.handler.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.params.MultipartParams;
import com.hotpads.handler.params.Params;

public abstract class BaseDispatcher{

	public static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";
	public static final String REGEX_TWO_DIRECTORY_PLUS = "/\\w+/\\w+[/]?.*";
	public static final String MATCHING_ANY = ".*";

	private final DatarouterInjector injector;
	private final String servletContextPath;
	private final String combinedPrefix;
	private final List<DispatchRule> dispatchRules;
	private Class<? extends BaseHandler> defaultHandlerClass;

	public BaseDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		this.injector = injector;
		this.servletContextPath = servletContextPath;
		this.combinedPrefix = servletContextPath + urlPrefix;
		this.dispatchRules = new ArrayList<>();
	}

	/*---------------- create DispatchRules -----------------*/

	protected DispatchRule handle(String regex){
		DispatchRule rule = new DispatchRule(regex);
		this.dispatchRules.add(rule);
		return rule;
	}

	protected DispatchRule handleDir(String regex){
		return handle(regex + REGEX_ONE_DIRECTORY);
	}

	protected DispatchRule handleAnySuffix(String suffix){
		return handle(MATCHING_ANY + suffix);
	}

	protected DispatchRule handleAnyPrefix(String prefix){
		return handle(prefix + MATCHING_ANY);
	}

	protected BaseDispatcher handleOthers(Class<? extends BaseHandler> defaultHandlerClass){
		this.defaultHandlerClass = defaultHandlerClass;
		return this;
	}

	/*------------------ handle -------------------*/

	public boolean handleRequestIfUrlMatch(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) throws ServletException{
		String uri = request.getRequestURI();
		if(!uri.startsWith(combinedPrefix)){
			return false;
		}
		BaseHandler handler = null;
		String afterContextPath = uri.substring(servletContextPath.length());
		for(DispatchRule rule : dispatchRules){
			if(rule.getPattern().matcher(afterContextPath).matches()){
				if(!rule.apply(request)){
					// TODO improve this. Right now it returns 404 and log "dispatcher could not find Handler for /uri"
					// while it's more an "access denied"
					return false;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				break;
			}
		}

		if(handler == null){
			if(defaultHandlerClass == null){
				return false;// url not found
			}
			handler = injector.getInstance(defaultHandlerClass);
		}

		handler.setRequest(request);
		handler.setResponse(response);
		handler.setServletContext(servletContext);
		if(isMultipart(request)){
			try{
				handler.setParams(new MultipartParams(request));
			}catch(FileUploadException e){
				throw new ServletException(e);
			}
		} else {
			handler.setParams(new Params(request));
		}
		handler.handleWrapper();
		return true;
	}

	private boolean isMultipart(HttpServletRequest request){
		return request.getContentType() != null
				&& request.getContentType().toLowerCase().contains("multipart/form-data");
	}

	/*------------------ getters -------------------*/

	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}

	/*--------------------- tests -------------------*/

	public static class BaseDispatcherTests{
		@Test
		public void testMatches(){
			String prefix = "fjalfdja";
			String suffix = "dfadfqeq";

			Pattern prefixPattern = Pattern.compile(prefix + MATCHING_ANY);
			Assert.assertTrue(prefixPattern.matcher(prefix + "qefadfaf").matches());
			Assert.assertTrue(prefixPattern.matcher(prefix + "/qefadfaf").matches());
			Assert.assertTrue(prefixPattern.matcher(prefix + "/qef/adfaf").matches());

			Assert.assertFalse(prefixPattern.matcher("/asae" + prefix + "/qef/adfaf").matches());
			Assert.assertFalse(prefixPattern.matcher("/asae/" + prefix + "/qef/adfaf").matches());

			Pattern suffixPattern = Pattern.compile(MATCHING_ANY + suffix);
			Assert.assertTrue(suffixPattern.matcher("fjalfdja" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjalfdja" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjalfdja/" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjal/fdja" + suffix).matches());

			Assert.assertFalse(suffixPattern.matcher(suffix + "adfa").matches());
			Assert.assertFalse(suffixPattern.matcher("fjalfdja" + suffix + "adfa").matches());

			Pattern oneDirectoryPattern = Pattern.compile(REGEX_ONE_DIRECTORY);
			Assert.assertTrue(oneDirectoryPattern.matcher("").matches());
			Assert.assertTrue(oneDirectoryPattern.matcher("abcd").matches());
			Assert.assertTrue(oneDirectoryPattern.matcher("/").matches());
			Assert.assertTrue(oneDirectoryPattern.matcher("/abcd").matches());

			Assert.assertFalse(oneDirectoryPattern.matcher("//abcd").matches());
			Assert.assertFalse(oneDirectoryPattern.matcher("/abcd/").matches());
			Assert.assertFalse(oneDirectoryPattern.matcher("/abc/efg").matches());

		}
	}
}
