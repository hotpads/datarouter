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
package io.datarouter.bytes;

import java.util.Arrays;

public class ToStringTool{

	public static String toString(Object value){
		if(value == null){
			return null;
		}
		if(value.getClass().isArray()){
			Class<?> clazz = value.getClass();
			if(clazz == byte[].class){
				return Arrays.toString((byte[])value);
			}
			if(clazz == boolean[].class){
				return Arrays.toString((boolean[])value);
			}
			if(clazz == short[].class){
				return Arrays.toString((short[])value);
			}
			if(clazz == char[].class){
				return Arrays.toString((char[])value);
			}
			if(clazz == int[].class){
				return Arrays.toString((int[])value);
			}
			if(clazz == float[].class){
				return Arrays.toString((float[])value);
			}
			if(clazz == long[].class){
				return Arrays.toString((long[])value);
			}
			if(clazz == double[].class){
				return Arrays.toString((double[])value);
			}
			return Arrays.deepToString((Object[])value);
		}
		return value.toString();
	}

}
