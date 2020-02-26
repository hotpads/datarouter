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

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutoConfigResponseTool{

	public static String buildResponse(String configurationStep){
		return String.format("  -%s\n", configurationStep);
	}

	public static String buildResponse(String configurationStep, String description){
		return String.format("  -%s: %s\n", configurationStep, description);
	}

	public static <A> String buildResponse(String configurationStep, Collection<A> items, Function<A,String> mapper){
		return items.stream()
				.map(mapper)
				.map(description -> buildResponse(configurationStep, description))
				.collect(Collectors.joining());
	}

}
