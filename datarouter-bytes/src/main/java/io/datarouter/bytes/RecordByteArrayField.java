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
import java.util.Objects;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;

/**
 * Explicitly for storing byte arrays as java record fields.
 * Please do not add other functionality.
 *
 * Java records use shallow hashCode/equals on byte arrays.
 * Using this wrapper in a record will result in deep hashCode/equals.
 */
public record RecordByteArrayField(
		byte[] bytes){

	public static final RecordByteArrayField EMPTY = new RecordByteArrayField(EmptyArray.BYTE);

	public RecordByteArrayField{
		Objects.requireNonNull(bytes);
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(bytes);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		RecordByteArrayField other = (RecordByteArrayField)obj;
		return Arrays.equals(bytes, other.bytes);
	}

	@Override
	public String toString(){
		return "[" + HexByteStringCodec.INSTANCE.encode(bytes) + "]";
	}

}
