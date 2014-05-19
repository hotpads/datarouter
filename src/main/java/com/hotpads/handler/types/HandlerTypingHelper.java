package com.hotpads.handler.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.hotpads.handler.BaseHandler;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.client.json.JsonSerializer;

public class HandlerTypingHelper{

	/**
	 * This methods goes through all methods who are named like methodName and tries to find the one that has the
	 * largest number of parameters. It generates the array of arguments at the same time.
	 * @param handler
	 * @param methodName
	 * @param jsonSerializer
	 * @return
	 */
	public static Pair<Method, List<Object>> findMethodByName(BaseHandler handler, String methodName, JsonSerializer jsonSerializer){
		Method method = null;
		List<Object> args = ListTool.create();
		for(Method possibleMethod : ReflectionTool.getDeclaredMethodsWithName(handler.getClass(), methodName)){
			List<Object> currentArgs = ListTool.create();
			int i = 0;
			for(Annotation[] paramAnnotations : possibleMethod.getParameterAnnotations()){
				boolean validParam = false;
				for(Annotation paramAnnotation : paramAnnotations){
					if(paramAnnotation instanceof P){
						String param = handler.getParams().optional(((P)paramAnnotation).value(), null);
						if(param != null){
							Type paramClass = possibleMethod.getParameterTypes()[i];
							if(((P)paramAnnotation).typeProvider() != null){
								try{
									TypeProvider typeProvider = (TypeProvider) ((P)paramAnnotation).typeProvider().newInstance();
									if(typeProvider.get() != null){
										paramClass = typeProvider.get();
									}
								}catch(Exception e){
									throw new RuntimeException("Make sure your type provider is static and has a no-arg constructor", e);
								}
							}
							currentArgs.add(jsonSerializer.deserialize(param, paramClass));
							validParam = true;
						}
						break;
					}
				}
				if(!validParam){
					possibleMethod = null;
					break;
				}
				i++;
			}
			if(possibleMethod == null){
				continue;
			}
			if(currentArgs.size() > args.size() || (args.size() == 0)){
				method = possibleMethod;
				args = currentArgs;
			}
		}
		return new Pair<Method, List<Object>>(method, args);
	}

}
