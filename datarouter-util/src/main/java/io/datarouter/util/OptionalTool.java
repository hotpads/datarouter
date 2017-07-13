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

import java.util.Optional;
import java.util.function.Function;

import io.datarouter.util.lang.ObjectTool;

public class OptionalTool{
	public static <I,O> O mapOrElse(Optional<I> in, Function<I,O> mapper, O orElse){
		in = ObjectTool.nullSafe(in, Optional.empty());
		return in.map(mapper).orElse(orElse);
	}
}
