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
package io.datarouter.virtualnode.writebehind.base;

import java.util.ArrayList;
import java.util.Collection;

import io.datarouter.storage.config.Config;

public class WriteWrapper<T>{

	private final String op;
	private final Collection<T> objects;
	private final Config config;

	public WriteWrapper(String op, Collection<T> objects, Config config){
		this.op = op;
		this.objects = new ArrayList<>(objects);
		this.config = config;
	}

	@Override
	public WriteWrapper<T> clone(){
		return new WriteWrapper<>(op, objects, config);
	}

	public String getOp(){
		return op;
	}

	public Collection<T> getObjects(){
		return objects;
	}

	public Config getConfig(){
		return config;
	}

	@Override
	public String toString(){
		return String.format("WriteWrapper {hashCode=%s}[op=%s, numObjects=%s, config=%s]",
				Integer.toHexString(hashCode()),
				op,
				objects.size(),
				config);
	}

}
