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
package io.datarouter.bytes.binarydto.fieldcodec.string;

import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class Utf8BinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<String>{

	private static final StringCodec CODEC = StringCodec.UTF_8;

	@Override
	public boolean supportsComparableCodec(){
		return true;
	}

	@Override
	public byte[] encode(String value){
		return CODEC.encode(value);
	}

	@Override
	public String decode(byte[] bytes, int offset, int length){
		return CODEC.decode(bytes, offset, length);
	}

	@Override
	public int compareAsIfEncoded(String left, String right){
		return left.compareTo(right);
	}

}
