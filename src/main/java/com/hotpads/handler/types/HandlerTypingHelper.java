package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.BaseHandler.Handler;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class HandlerTypingHelper{
	
	@Inject
	private Injector injector;

	/**
	 * This methods goes through all methods who are named like methodName and tries to find the one that has the
	 * largest number of parameters. It generates the array of arguments at the same time.
	 * @param handler
	 * @param methodName
	 * @param decoder
	 * @return
	 */
	public Pair<Method, Object[]> findMethodByName(BaseHandler handler, String methodName){
		Method method = null;
		Object[] args = new Object[]{};
		Collection<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(handler.getClass(), methodName);
		for(Method possibleMethod : possibleMethods){
			if(!possibleMethod.isAnnotationPresent(Handler.class)){
				continue;
			}
			try{
				Class<? extends HandlerDecoder> decoderClass = possibleMethod.getAnnotation(Handler.class).decoder();
				HandlerDecoder decoder = injector.getInstance(decoderClass);
				Object[] newArgs = decoder.decode(handler.getRequest(), possibleMethod);
				if(newArgs == null){
					continue;
				}
				if(args.length < newArgs.length || args.length == 0){
					args = newArgs;
					method = possibleMethod;
				}
			}catch(Exception e){
				continue;
			}
		}
		return new Pair<Method, Object[]>(method, args);
	}

}
