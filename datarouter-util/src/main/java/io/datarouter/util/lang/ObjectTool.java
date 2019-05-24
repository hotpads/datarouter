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
package io.datarouter.util.lang;

import java.util.Arrays;
import java.util.Objects;

public class ObjectTool{

	/**
	 * Note this method may hide the IDEs ability to warn about unlikely comparisons
	 */
	public static boolean notEquals(Object first, Object second){
		return !Objects.equals(first, second);
	}

	public static boolean isOneNullButNotTheOther(Object first, Object second){
		return first == null ^ second == null;
	}

	public static boolean bothNull(Object first, Object second){
		return first == null && second == null;
	}

	public static <T> T nullSafe(T object, T returnIfNull){
		return object != null ? object : returnIfNull;
	}

	public static String nullSafeToString(Object object){
		return Objects.toString(object, null);
	}

	public static void requireNonNulls(Object... objects){
		Arrays.stream(objects)
				.forEach(Objects::requireNonNull);
	}

	public static boolean anyNull(Object... objects){
		for(Object o : objects){
			if(o == null){
				return true;
			}
		}
		return false;
	}

}
