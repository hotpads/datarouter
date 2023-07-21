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
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressors;
import io.datarouter.bytes.blockfile.enums.BlockfileHeaderKey;

public record BlockfileHeader(
		BinaryDictionary userDictionary,
		BlockfileCompressor compressor,
		int checksumLength,
		BlockfileChecksummer checksummer){

	private BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(BlockfileHeaderKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(BlockfileHeaderKey.COMPRESSOR.bytes, compressor.nameToBytes())
				.put(BlockfileHeaderKey.CHECKSUM_LENGTH.bytes, checksummer.lengthToBytes())
				.put(BlockfileHeaderKey.CHECKSUM_ALGORITHM.bytes, checksummer.nameToBytes());
	}

	public static class BlockfileHeaderCodec implements Codec<BlockfileHeader,byte[]>{
		private final BlockfileCompressors registeredCompressors;
		private final BlockfileChecksummers registeredChecksummers;

		public BlockfileHeaderCodec(
				BlockfileCompressors registeredCompressors,
				BlockfileChecksummers registeredChecksummers){
			this.registeredCompressors = registeredCompressors;
			this.registeredChecksummers = registeredChecksummers;
		}

		@Override
		public byte[] encode(BlockfileHeader value){
			return value.toBinaryDictionary().encode();
		}

		@Override
		public BlockfileHeader decode(byte[] valueBytes){
			var dictionary = BinaryDictionary.decode(valueBytes);
			return new BlockfileHeader(
					parseUserDictionary(dictionary),
					parseCompressor(dictionary),
					parseChecksumLength(dictionary),
					parseChecksummer(dictionary));
		}

		private BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
			byte[] userDictionaryBytes = dictionary.get(BlockfileHeaderKey.USER_DICTIONARY.bytes);
			return BinaryDictionary.decode(userDictionaryBytes);
		}

		private BlockfileCompressor parseCompressor(BinaryDictionary dictionary){
			byte[] compressorNameBytes = dictionary.get(BlockfileHeaderKey.COMPRESSOR.bytes);
			String compressorName = BlockfileCompressor.bytesToName(compressorNameBytes);
			return registeredCompressors.getForEncodedName(compressorName);
		}

		private int parseChecksumLength(BinaryDictionary dictionary){
			byte[] checksumLengthBytes = dictionary.get(BlockfileHeaderKey.CHECKSUM_LENGTH.bytes);
			return BlockfileChecksummer.bytesToLength(checksumLengthBytes);
		}

		private BlockfileChecksummer parseChecksummer(BinaryDictionary dictionary){
			byte[] checksummerNameBytes = dictionary.get(BlockfileHeaderKey.CHECKSUM_ALGORITHM.bytes);
			String checksummerName = BlockfileChecksummer.bytesToName(checksummerNameBytes);
			return registeredChecksummers.getForEncodedName(checksummerName);
		}
	}

}
