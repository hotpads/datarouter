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
package io.datarouter.util;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamTool{

	public static <V> BinaryOperator<V> throwingMerger(){
		return (v1, v2) -> {
			throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
		};
	}

	/**
	 * For filtering by and mapping stream to stream of a subtype. Example:
	 * <pre>animals.stream()
	 * 		.flatMap(StreamTool.instancesOf(Cat.class))
	 * 		.forEach(Cat::meow);
	 * </pre>
	 */
	public static <E> Function<Object,Stream<E>> instancesOf(Class<E> clazz){
		return obj -> clazz.isInstance(obj) ? Stream.of(clazz.cast(obj)) : Stream.empty();
	}

}
