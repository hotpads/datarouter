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
package io.datarouter.bytes.blockfile.encoding.checksum;

import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

/**
 * Checksum each block for the purposes of hunting down corruption and potentially skipping invalid blocks.
 */
public record BlockfileChecksummer(
		String encodedName,
		int numBytes,
		Supplier<Function<byte[],byte[]>> encoderSupplier){

	private static final Codec<String,byte[]> NAME_CODEC = StringCodec.UTF_8;
	private static final Codec<Integer,byte[]> LENGTH_CODEC = Codec.of(
			RawIntCodec.INSTANCE::encode,
			RawIntCodec.INSTANCE::decode);

	/*--------- encode length --------*/

	public byte[] lengthToBytes(){
		return LENGTH_CODEC.encode(numBytes);
	}

	public static int bytesToLength(byte[] bytes){
		return LENGTH_CODEC.decode(bytes);
	}

	/*--------- encode name --------*/

	public byte[] nameToBytes(){
		return NAME_CODEC.encode(encodedName);
	}

	public static String bytesToName(byte[] bytes){
		return NAME_CODEC.decode(bytes);
	}

	public Function<byte[],byte[]> newEncoder(){
		return encoderSupplier.get();
	}

}
