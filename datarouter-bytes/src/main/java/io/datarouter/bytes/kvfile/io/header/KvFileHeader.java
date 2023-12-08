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
package io.datarouter.bytes.kvfile.io.header;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormat;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormats;

public record KvFileHeader(
		BinaryDictionary userDictionary,
		KvFileBlockFormat blockFormat){

	public BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(KvFileHeaderKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(KvFileHeaderKey.KV_BLOCK_FORMAT.bytes,
						StringCodec.UTF_8.encode(blockFormat.encodedName()));
	}

	public static class KvFileHeaderCodec implements Codec<KvFileHeader,BinaryDictionary>{
		private final KvFileBlockFormats registeredBlockFormats;

		public KvFileHeaderCodec(
				KvFileBlockFormats registeredBlockFormats){
			this.registeredBlockFormats = registeredBlockFormats;
		}

		@Override
		public BinaryDictionary encode(KvFileHeader value){
			return value.toBinaryDictionary();
		}

		@Override
		public KvFileHeader decode(BinaryDictionary dictionary){
			return new KvFileHeader(
					parseUserDictionary(dictionary),
					parseBlockFormat(dictionary));
		}

		private BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
			byte[] userDictionaryBytes = dictionary.get(KvFileHeaderKey.USER_DICTIONARY.bytes);
			return BinaryDictionary.decode(userDictionaryBytes);
		}

		private KvFileBlockFormat parseBlockFormat(BinaryDictionary dictionary){
			byte[] blockFormatBytes = dictionary.get(KvFileHeaderKey.KV_BLOCK_FORMAT.bytes);
			String blockFormatString = StringCodec.UTF_8.decode(blockFormatBytes);
			return registeredBlockFormats.getForEncodedName(blockFormatString);
		}

	}

}
