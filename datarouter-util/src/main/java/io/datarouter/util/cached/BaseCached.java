/**
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
package io.datarouter.util.cached;

import java.util.function.Supplier;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.lang.ObjectTool;

public abstract class BaseCached<T> implements Supplier<T>{

	protected T value;
	protected volatile long cachedAtMs = -1;

	protected abstract T reload();

	@Override
	public T get(){
		updateIfExpired();
		return value;
	}

	/* todo make this async in case the reload method is slow */
	protected boolean updateIfExpired(){
		if(!isExpired()){
			return false;
		}
		T original;
		try(var $ = TracerTool.startSpan("BaseCached reload")){
			TracerTool.appendToSpanInfo(toString());
			synchronized(this){
				original = value;
				if(!isExpired()){
					return false;
				}
				value = reload();
				cachedAtMs = System.currentTimeMillis();
			}
		}
		return ObjectTool.notEquals(original, value);

	}

	protected abstract boolean isExpired();

}
