package com.hotpads.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.inject.DatarouterInjector;

public abstract class BaseDispatcher{

	private static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";
	private static final String MATCHING_ANY = ".*";
	private DatarouterInjector injector;
	private String servletContextPath;
	private String combinedPrefix;
	private Class<? extends BaseHandler> defaultHandlerClass;
	private List<DispatchRule> dispatchRules;

	public BaseDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		this.injector = injector;
		this.servletContextPath = servletContextPath;
		this.combinedPrefix = servletContextPath + urlPrefix;
		this.dispatchRules = new ArrayList<>();
	}

	protected BaseDispatcher handleOthers(Class<? extends BaseHandler> defaultHandlerClass){
		this.defaultHandlerClass = defaultHandlerClass;
		return this;
	}

	protected DispatchRule handleDir(String regex){
		return handle(regex + REGEX_ONE_DIRECTORY);
	}

	protected DispatchRule handle(String regex){
		DispatchRule rule = new DispatchRule(regex);
		this.dispatchRules.add(rule);
		return rule;
	}

	protected DispatchRule handleAnySuffix(String suffix){
		return handle(MATCHING_ANY + suffix);
	}

	protected DispatchRule handleAnyPrefix(String prefix){
		return handle(prefix + MATCHING_ANY);
	}

	public boolean handleRequestIfUrlMatch(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response){
		String uri = request.getRequestURI();
		if (!uri.startsWith(combinedPrefix)){
			return false;
		}
		BaseHandler handler = null;
		String afterPrefix = uri.substring(servletContextPath.length());
		for (DispatchRule rule : dispatchRules){
			if (rule.getPattern().matcher(afterPrefix).matches()){
				if (!rule.apply(request)){
					return false;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				break;
			}
		}

		if (handler == null){
			if (defaultHandlerClass == null){
				return false;// url not found
			}
			handler = injector.getInstance(defaultHandlerClass);
		}

		handler.setRequest(request);
		handler.setResponse(response);
		try{
			handler.setOut(response.getWriter());
		}catch (IOException e){
			throw new RuntimeException(e);
		}
		handler.setServletContext(servletContext);
		handler.setParams(new Params(request, response));
		handler.handleWrapper();
		return true;
	}

	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}

	public static class BaseDispatcherTester{
		@Test
		public void testMatches(){
			String prefix = "fjalfdja";
			String suffix = "dfadfqeq";

			Pattern prefixPattern = Pattern.compile(prefix + ".*");
			Assert.assertTrue(prefixPattern.matcher(prefix + "qefadfaf").matches());
			Assert.assertTrue(prefixPattern.matcher(prefix + "/qefadfaf").matches());
			Assert.assertTrue(prefixPattern.matcher(prefix + "/qef/adfaf").matches());

			Assert.assertFalse(prefixPattern.matcher("/asae" + prefix + "/qef/adfaf").matches());
			Assert.assertFalse(prefixPattern.matcher("/asae/" + prefix + "/qef/adfaf").matches());


			Pattern suffixPattern = Pattern.compile(".*" + suffix);
			Assert.assertTrue(suffixPattern.matcher("fjalfdja" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjalfdja" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjalfdja/" + suffix).matches());
			Assert.assertTrue(suffixPattern.matcher("/fjal/fdja" + suffix).matches());

			Assert.assertFalse(suffixPattern.matcher(suffix +"adfa").matches());
			Assert.assertFalse(suffixPattern.matcher("fjalfdja"+ suffix +"adfa").matches());


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
