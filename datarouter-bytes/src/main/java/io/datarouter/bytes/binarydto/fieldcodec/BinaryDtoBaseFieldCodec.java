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
package io.datarouter.bytes.binarydto.fieldcodec;

import io.datarouter.bytes.LengthAndValue;

public abstract class BinaryDtoBaseFieldCodec<T>{

	public boolean isFixedLength(){
		return false;
	}

	public int fixedLength(){
		throw new RuntimeException("Fixed length hasn't been specified.");
	}

	public abstract byte[] encode(T value);

	public T decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value;
	}

	public abstract LengthAndValue<T> decodeWithLength(byte[] bytes, int offset);

}
