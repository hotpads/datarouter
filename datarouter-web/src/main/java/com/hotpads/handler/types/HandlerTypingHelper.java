package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseHandler.Handler;
import com.hotpads.util.core.collections.Pair;

@Singleton
public class HandlerTypingHelper{

	@Inject
	private DatarouterInjector injector;

	/**
	 * This methods goes through all methods who are named like methodName and tries to find the one that has the
	 * largest number of parameters. It generates the array of arguments at the same time.
	 */
	public Pair<Method, Object[]> findMethodByName(Collection<Method> possibleMethods, HttpServletRequest request){
		Method method = null;
		Object[] args = new Object[]{};
		for(Method possibleMethod : possibleMethods){
			try{
				Class<? extends HandlerDecoder> decoderClass = possibleMethod.getAnnotation(Handler.class).decoder();
				HandlerDecoder decoder = injector.getInstance(decoderClass);
				Object[] newArgs = decoder.decode(request, possibleMethod);
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
		return new Pair<>(method, args);
	}

}
