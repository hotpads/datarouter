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
package io.datarouter.enums;

import java.util.function.Function;

/**
 * Avoid this class if you don't need it, preferring a normal MappedEnum of Strings with case-sensitive logic.
 *
 * If you can't avoid the need for case-insensitive mapping, this class helps with it.
 */
public class CaseInsensitiveStringMappedEnum<E>
extends MappedEnum<E,String>{

	public CaseInsensitiveStringMappedEnum(E[] values, Function<E,String> keyExtractor){
		super(values, keyExtractor, String::toLowerCase);
	}

}
