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
package io.datarouter.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Java9{

	@SafeVarargs
	public static <T> List<T> listOf(T... items){
		// return List.of(items);

		for(int i = 0; i < items.length; ++i){
			Objects.requireNonNull(items[i]);
		}
		return Collections.unmodifiableList(Arrays.asList(items));
	}

	@SafeVarargs
	public static <T> Set<T> setOf(T... items){
		// return Set.of(items);

		Set<T> modifiableSet = new HashSet<>();
		for(int i = 0; i < items.length; ++i){
			Objects.requireNonNull(items[i]);
			modifiableSet.add(items[i]);
		}
		return Collections.unmodifiableSet(modifiableSet);
	}

	public static int compareUnsigned(byte[] bytesA, byte[] bytesB){
		// return Arrays.compareUnsigned(bytesA, bytesB);

		if(bytesA == bytesB){
			return 0;
		}
		if(bytesA == null || bytesB == null){
			return bytesA == null ? -1 : 1;
		}

		int lengthA = bytesA.length;
		int lengthB = bytesB.length;
		for(int i = 0, j = 0; i < lengthA && j < lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static boolean canAccess(Field field, @SuppressWarnings("unused") Object object){
		// return field.canAccess(object));
		return field.isAccessible();
	}

}
