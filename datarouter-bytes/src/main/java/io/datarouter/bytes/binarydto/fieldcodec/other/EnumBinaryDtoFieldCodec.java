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
package io.datarouter.bytes.binarydto.fieldcodec.other;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.stringcodec.TerminatedStringCodec;

public class EnumBinaryDtoFieldCodec<E extends Enum<E>> extends BinaryDtoBaseFieldCodec<E>{

	private static final TerminatedStringCodec CODEC = TerminatedStringCodec.US_ASCII;

	private final Class<E> type;

	public EnumBinaryDtoFieldCodec(Class<E> type){
		this.type = type;
	}

	@Override
	public byte[] encode(E value){
		return CODEC.encode(value.name());
	}

	@Override
	public E decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value;
	}

	@Override
	public LengthAndValue<E> decodeWithLength(byte[] bytes, int offset){
		LengthAndValue<String> stringLengthAndValue = CODEC.decode(bytes, offset);
		int length = stringLengthAndValue.length;
		E value = Enum.valueOf(type, stringLengthAndValue.value);
		return new LengthAndValue<>(length, value);
	}

}
