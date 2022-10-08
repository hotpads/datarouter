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
package io.datarouter.storage.test.node.queue;

import java.util.Objects;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.booleancodec.RawBooleanCodec;
import io.datarouter.bytes.codec.charcodec.ComparableCharCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;

public class BloqQueueStorageTestDto{

	public final char chr;
	public final int number;
	public final boolean bool;

	public BloqQueueStorageTestDto(char chr, int number, boolean bool){
		this.chr = chr;
		this.number = number;
		this.bool = bool;
	}

	@Override
	public int hashCode(){
		return Objects.hash(bool, number, chr);
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
		BloqQueueStorageTestDto other = (BloqQueueStorageTestDto)obj;
		return bool == other.bool && number == other.number && chr == other.chr;
	}

	public static class BloqQueueStorageTestDtoCodec implements Codec<BloqQueueStorageTestDto,byte[]>{

		@Override
		public byte[] encode(BloqQueueStorageTestDto value){
			return ByteTool.concat(
					ComparableCharCodec.INSTANCE.encode(value.chr),
					RawIntCodec.INSTANCE.encode(value.number),
					RawBooleanCodec.INSTANCE.encode(value.bool));
		}

		@Override
		public BloqQueueStorageTestDto decode(byte[] encodedValue){
			int cursor = 0;
			char chr = ComparableCharCodec.INSTANCE.decode(encodedValue);
			cursor += ComparableCharCodec.INSTANCE.length();
			int number = RawIntCodec.INSTANCE.decode(encodedValue, cursor);
			cursor += RawIntCodec.INSTANCE.length();
			boolean bool = RawBooleanCodec.INSTANCE.decode(encodedValue, cursor);
			return new BloqQueueStorageTestDto(chr, number, bool);
		}

		public static int length(){
			return ComparableCharCodec.INSTANCE.length()
					+ RawIntCodec.INSTANCE.length()
					+ RawBooleanCodec.INSTANCE.length();
		}

	}

}
