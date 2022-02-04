/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.handler.documentation;

import java.util.Optional;

import io.datarouter.httpclient.response.exception.DocumentedServerError;
import io.datarouter.httpclient.response.exception.ServerErrorDoc;
import io.datarouter.util.lang.ReflectionTool;

public class HttpDocumentedExceptionTool{

	public static Optional<DocumentedServerError> findDocumentation(Class<?> clazz){
		ServerErrorDoc annotation = clazz.getAnnotation(ServerErrorDoc.class);
		if(annotation != null){
			return Optional.of(ReflectionTool.createNullArgsWithUnsafeAllocator(annotation.value()));
		}
		if(DocumentedServerError.class.isAssignableFrom(clazz)){
			return Optional.of((DocumentedServerError)ReflectionTool.createNullArgsWithUnsafeAllocator(clazz));
		}
		return Optional.empty();
	}

	public static Optional<DocumentedServerError> findDocumentationInChain(Throwable exception){
		while(exception != null){
			Optional<DocumentedServerError> opt = findDocumentation(exception.getClass());
			if(opt.isPresent()){
				return opt;
			}
			exception = exception.getCause();
		}
		return Optional.empty();
	}

}
