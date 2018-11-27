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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.StreamTool;
import io.datarouter.util.collection.CollectionTool;

public class ArrayTool{

	@SafeVarargs
	public static <A,T> Set<T> mapToSet(Function<A,T> mapper, A... values){
		if(values == null || values.length == 0 || values.length == 1 && values[0] == null){
			return new HashSet<>();
		}
		return StreamTool.mapToSet(Stream.of(values), mapper);
	}

	public static byte[] clone(byte[] in){
		if(in == null){
			return null;
		}
		byte[] out = new byte[in.length];
		System.arraycopy(in, 0, out, 0, in.length);
		return out;
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

	public static boolean notEmpty(byte[] array){
		return !isEmpty(array);
	}

	public static byte[] concatenate(byte[]... arrays){
		int totalLength = 0;
		for(int i = 0; i < length(arrays); ++i){
			totalLength += length(arrays[i]);
		}
		byte[] result = new byte[totalLength];
		int nextStartIndex = 0;
		for(int i = 0; i < length(arrays); ++i){
			int argArrayLength = length(arrays[i]);
			if(argArrayLength > 0){
				System.arraycopy(arrays[i], 0, result, nextStartIndex, argArrayLength);
				nextStartIndex += argArrayLength;
			}
		}
		return result;
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
		if(CollectionTool.isEmpty(ins)){
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

	public static class Tests{

		@Test
		public void simpleCompare(){
			Double one = 1.0;
			Double two = 2.0;
			Assert.assertEquals(one.compareTo(one), 0);
			Assert.assertEquals(one.compareTo(two), -1);
			Assert.assertEquals(two.compareTo(one), 1);
		}

		@Test
		public void testConcatenateVarargBytes(){
			byte[] concat = concatenate(new byte[]{0, 1}, new byte[]{2}, new byte[]{3, 4});
			Assert.assertEquals(concat, new byte[]{0, 1, 2, 3, 4});
		}

		@Test
		public void testMapToSet(){
			Assert.assertEquals(mapToSet(null, (Object)null), new HashSet<>());
			Object nothing = null;
			Assert.assertEquals(mapToSet(null, nothing), new HashSet<>());
			String[] empty = new String[0];
			Assert.assertEquals(mapToSet(null, empty), new HashSet<>());
			String[] arr = {"hi", "hello", "hi"};
			Assert.assertEquals(mapToSet(str -> str + "s", arr), new HashSet<>(Arrays.asList("hellos", "his")));
		}

		@Test
		public void testTrimToSize(){
			byte[] array = new byte[]{0, 1, 2, 3, 4};
			Assert.assertEquals(trimToSize(new byte[]{}, 2), new byte[2]);
			Assert.assertEquals(trimToSize(array, 2), new byte[]{0, 1});
			Assert.assertEquals(trimToSize(array, 5), new byte[]{0, 1, 2, 3, 4});
			Assert.assertEquals(trimToSize(array, 6), new byte[]{0, 1, 2, 3, 4});
		}

	}

}
