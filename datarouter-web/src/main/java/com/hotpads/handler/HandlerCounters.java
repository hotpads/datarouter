package com.hotpads.handler;

import java.lang.reflect.Method;

import javax.inject.Singleton;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.util.DRCounters;

@Singleton
public class HandlerCounters{

	private static final String
			PREFIX = DRCounters.PREFIX,
			HANDLER = "handler",
			CLASS = "class",
			PACKAGED_CLASS = "packagedClass",
			METHOD = "method",
			PACKAGED_METHOD = "packagedMethod";


	public void incMethodInvocation(BaseHandler handler, Method method){
		incInternal(CLASS, handler.getClass().getSimpleName());
		incInternal(PACKAGED_CLASS, handler.getClass().getName());
		incInternal(METHOD, handler.getClass().getSimpleName() + " " + method.getName());
		incInternal(PACKAGED_METHOD, handler.getClass().getName() + " " + method.getName());
	}

	private void incInternal(String format, String suffix){
		Counters.inc(PREFIX + " " + HANDLER + " " + format + " " + suffix);
	}

}
