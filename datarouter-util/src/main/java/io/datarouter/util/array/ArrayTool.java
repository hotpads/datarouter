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
package io.datarouter.util.array;

import java.util.Arrays;
import java.util.Collection;

public class ArrayTool{

	public static byte[] clone(byte[] in){
		if(in == null){
			return null;
		}
		return Arrays.copyOf(in, in.length);
	}

	public static byte[] nullSafe(byte[] array){
		if(array == null){
			return new byte[0];
		}
		return array;
	}

	public static String[] nullSafe(String[] array){
		if(array == null){
			return new String[0];
		}
		return array;
	}

	public static int length(byte[] array){
		if(array == null){
			return 0;
		}
		return array.length;
	}

	public static int length(Object[] array){
		if(array == null){
			return 0;
		}
		return array.length;
	}

	public static boolean isEmpty(byte[] array){
		if(array == null){
			return true;
		}
		if(array.length == 0){
			return true;
		}
		return false;
	}

	public static boolean isEmpty(Object[] array){
		if(array == null){
			return true;
		}
		if(array.length == 0){
			return true;
		}
		return false;
	}

	public static boolean notEmpty(Object[] array){
		return !isEmpty(array);
	}

	public static boolean containsUnsorted(byte[] array, byte key){
		if(isEmpty(array)){
			return false;
		}
		for(int i = 0; i < array.length; ++i){
			if(array[i] == key){
				return true;
			}
		}
		return false;
	}

	public static long[] primitiveLongArray(Collection<Long> ins){
		if(ins == null || ins.isEmpty()){
			return new long[0];
		}
		long[] array = new long[ins.size()];
		int index = 0;
		for(long i : ins){
			array[index++] = i;
		}
		return array;
	}

	public static byte[] trimToSize(byte[] ins, int size){
		if(isEmpty(ins)){
			return new byte[size];
		}
		if(ins.length <= size){
			return ins;
		}
		return Arrays.copyOf(ins, size);
	}

}
