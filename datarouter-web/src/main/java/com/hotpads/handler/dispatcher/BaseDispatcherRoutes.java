package com.hotpads.handler.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseHandler;

public abstract class BaseDispatcherRoutes{

	public static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";
	public static final String REGEX_TWO_DIRECTORY_PLUS = "/\\w+/\\w+[/]?.*";
	public static final String MATCHING_ANY = ".*";

	private final String urlPrefix;
	private final List<DispatchRule> dispatchRules;
	private Class<? extends BaseHandler> defaultHandlerClass;

	public BaseDispatcherRoutes(String urlPrefix){
		this.urlPrefix = urlPrefix;
		this.dispatchRules = new ArrayList<>();
	}

	/**
	 * @deprecated use {@link BaseDispatcherRoutes#BaseDispatcherRoutes(String)}
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public BaseDispatcherRoutes(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		this(urlPrefix);
	}

	/*---------------- create DispatchRules -----------------*/

	protected DispatchRule handle(String regex){
		DispatchRule rule = applyDefault(new DispatchRule(regex));
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

	protected BaseDispatcherRoutes handleOthers(Class<? extends BaseHandler> defaultHandlerClass){
		this.defaultHandlerClass = defaultHandlerClass;
		return this;
	}

	protected DispatchRule applyDefault(DispatchRule rule){
		return rule;
	}

	/*------------------ getters -------------------*/

	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}

	public String getUrlPrefix(){
		return urlPrefix;
	}

	public Class<? extends BaseHandler> getDefaultHandlerClass(){
		return defaultHandlerClass;
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
