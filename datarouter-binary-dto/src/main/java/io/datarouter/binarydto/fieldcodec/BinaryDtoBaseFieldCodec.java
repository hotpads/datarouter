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
package io.datarouter.binarydto.fieldcodec;

import java.util.Arrays;

public abstract class BinaryDtoBaseFieldCodec<T>{

	public boolean isFixedLength(){
		return false;
	}

	public boolean isVariableLength(){
		return !isFixedLength();
	}

	public int fixedLength(){
		throw new RuntimeException("Fixed length hasn't been specified.");
	}

	/**
	 * Override with true if the codec is suitable for comparable encoding.
	 */
	public abstract boolean supportsComparableCodec();

	public abstract byte[] encode(T value);

	public abstract T decode(byte[] bytes, int offset, int length);

	public T decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	/**
	 * Override this with optimized implementations that avoid encoding.
	 */
	public int compareAsIfEncoded(T left, T right){
		byte[] leftBytes = encode(left);
		byte[] rightBytes = encode(right);
		return Arrays.compareUnsigned(leftBytes, rightBytes);
	}

}
