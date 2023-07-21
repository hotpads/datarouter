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
package io.datarouter.bytes.blockfile.section;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.enums.BlockfileFooterKey;
import io.datarouter.bytes.varint.VarIntTool;

public record BlockfileFooter(
		BinaryDictionary userDictionary,
		long blockCount){

	public static final Codec<BlockfileFooter,byte[]> VALUE_CODEC = Codec.of(
			footer -> footer.toBinaryDictionary().encode(),
			bytes -> {
				var dictionary = BinaryDictionary.decode(bytes);
				return new BlockfileFooter(
						parseUserDictionary(dictionary),
						parseBlockCount(dictionary));
			});

	/*----------- encode ------------*/

	private BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(BlockfileFooterKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(BlockfileFooterKey.BLOCK_COUNT.bytes, VarIntTool.encode(blockCount));
	}

	/*----------- decode ------------*/

	private static BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
		byte[] userDictionaryBytes = dictionary.get(BlockfileFooterKey.USER_DICTIONARY.bytes);
		return BinaryDictionary.decode(userDictionaryBytes);
	}

	private static long parseBlockCount(BinaryDictionary dictionary){
		byte[] blockCountBytes = dictionary.get(BlockfileFooterKey.BLOCK_COUNT.bytes);
		return VarIntTool.decodeLong(blockCountBytes);
	}

}
