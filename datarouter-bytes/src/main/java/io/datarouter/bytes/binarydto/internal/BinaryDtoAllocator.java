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
package io.datarouter.bytes.binarydto.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;

/**
 * Borrowed from Gson's UnsafeAllocator
 *
 * Allows instantiation of BinaryDtos without a no-arg constructor. A no-arg constructor complicates the encouraged use
 * of final fields.
 */
public class BinaryDtoAllocator{

	private static final UnsafeAllocator ALLOCATOR = new UnsafeAllocator();

	public static <T extends BaseBinaryDto<T>> T allocate(Class<T> cls){
		return ALLOCATOR.allocate(cls);
	}

	private static class UnsafeAllocator{
		private final Object unsafe;
		private final Method allocateInstance;

		public UnsafeAllocator(){
			try{
				Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
				Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
				theUnsafeField.setAccessible(true);
				unsafe = theUnsafeField.get(null);
				allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T allocate(Class<T> cls){
			try{
				return (T)allocateInstance.invoke(unsafe, cls);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}

}
