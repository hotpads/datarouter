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
package io.datarouter.bytes.blockfile.block.decoded;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.block.BlockfileHeaderKey;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressors;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormat;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormat;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormats;

public record BlockfileHeaderBlock(
		BinaryDictionary userDictionary,
		BlockfileValueBlockFormat valueBlockFormat,
		BlockfileIndexBlockFormat indexBlockFormat,
		BlockfileCompressor compressor,
		BlockfileChecksummer checksummer){

	private BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(BlockfileHeaderKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(BlockfileHeaderKey.VALUE_BLOCK_FORMAT.bytes, valueBlockFormat.nameToBytes())
				.put(BlockfileHeaderKey.INDEX_BLOCK_FORMAT.bytes, indexBlockFormat.nameToBytes())
				.put(BlockfileHeaderKey.COMPRESSOR.bytes, compressor.nameToBytes())
				.put(BlockfileHeaderKey.CHECKSUM_ALGORITHM.bytes, checksummer.nameToBytes());
	}

	public static class BlockfileHeaderCodec implements Codec<BlockfileHeaderBlock,byte[]>{
		private final BlockfileValueBlockFormats registeredValueBlockFormats;
		private final BlockfileIndexBlockFormats registeredIndexBlockFormats;
		private final BlockfileCompressors registeredCompressors;
		private final BlockfileChecksummers registeredChecksummers;

		public BlockfileHeaderCodec(
				BlockfileValueBlockFormats registeredValueBlockFormats,
				BlockfileIndexBlockFormats registeredIndexBlockFormats,
				BlockfileCompressors registeredCompressors,
				BlockfileChecksummers registeredChecksummers){
			this.registeredValueBlockFormats = registeredValueBlockFormats;
			this.registeredIndexBlockFormats = registeredIndexBlockFormats;
			this.registeredCompressors = registeredCompressors;
			this.registeredChecksummers = registeredChecksummers;
		}

		@Override
		public byte[] encode(BlockfileHeaderBlock value){
			return value.toBinaryDictionary().encode();
		}

		@Override
		public BlockfileHeaderBlock decode(byte[] valueBytes){
			var dictionary = BinaryDictionary.decode(valueBytes);
			return new BlockfileHeaderBlock(
					parseUserDictionary(dictionary),
					parseValueBlockFormat(dictionary),
					parseIndexBlockFormat(dictionary),
					parseCompressor(dictionary),
					parseChecksummer(dictionary));
		}

		private BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
			byte[] userDictionaryBytes = dictionary.get(BlockfileHeaderKey.USER_DICTIONARY.bytes);
			return BinaryDictionary.decode(userDictionaryBytes);
		}

		private BlockfileValueBlockFormat parseValueBlockFormat(BinaryDictionary dictionary){
			byte[] blockFormatNameBytes = dictionary.get(BlockfileHeaderKey.VALUE_BLOCK_FORMAT.bytes);
			String blockFormatName = BlockfileValueBlockFormat.bytesToName(blockFormatNameBytes);
			return registeredValueBlockFormats.getForEncodedName(blockFormatName);
		}

		private BlockfileIndexBlockFormat parseIndexBlockFormat(BinaryDictionary dictionary){
			byte[] blockFormatNameBytes = dictionary.get(BlockfileHeaderKey.INDEX_BLOCK_FORMAT.bytes);
			String blockFormatName = BlockfileIndexBlockFormat.bytesToName(blockFormatNameBytes);
			return registeredIndexBlockFormats.getForEncodedName(blockFormatName);
		}

		private BlockfileCompressor parseCompressor(BinaryDictionary dictionary){
			byte[] compressorNameBytes = dictionary.get(BlockfileHeaderKey.COMPRESSOR.bytes);
			String compressorName = BlockfileCompressor.bytesToName(compressorNameBytes);
			return registeredCompressors.getForEncodedName(compressorName);
		}

		private BlockfileChecksummer parseChecksummer(BinaryDictionary dictionary){
			byte[] checksummerNameBytes = dictionary.get(BlockfileHeaderKey.CHECKSUM_ALGORITHM.bytes);
			String checksummerName = BlockfileChecksummer.bytesToName(checksummerNameBytes);
			return registeredChecksummers.getForEncodedName(checksummerName);
		}
	}

}
