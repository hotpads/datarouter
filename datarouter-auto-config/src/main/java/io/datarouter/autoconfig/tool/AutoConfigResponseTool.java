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
package io.datarouter.autoconfig.tool;

import java.util.List;
import java.util.function.Function;

import io.datarouter.util.iterable.IterableTool;

public class AutoConfigResponseTool{

	public static String buildResponse(String configurationStep){
		StringBuilder sb = new StringBuilder();
		sb.append("  -").append(configurationStep).append("\n");
		return sb.toString();
	}

	public static String buildResponse(String configurationStep, String description){
		StringBuilder sb = new StringBuilder();
		sb.append("  -").append(configurationStep).append(": ").append(description).append("\n");
		return sb.toString();
	}

	public static <A> String buildResponse(String configurationStep, Iterable<A> iterable, Function<A,String> mapper){
		List<String> strings = IterableTool.map(iterable, mapper);
		StringBuilder sb = new StringBuilder();
		strings.forEach(str -> sb.append("  -").append(configurationStep).append(": ").append(str).append("\n"));
		return sb.toString();
	}

}
