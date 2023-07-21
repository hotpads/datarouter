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

import java.util.List;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public record KvFileHeader(
		BinaryDictionary userDictionary,
		String blockFormat){//TODO more formal type

	public static final String BLOCK_FORMAT_PLACEHOLDER = "SEQUENTIAL";

	public BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(KvFileHeaderKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(KvFileHeaderKey.KV_BLOCK_FORMAT.bytes, StringCodec.UTF_8.encode(blockFormat));
	}

	public static class KvFileHeaderCodec implements Codec<KvFileHeader,BinaryDictionary>{
		private final List<String> registeredBlockFormats;//TODO more formal type

		public KvFileHeaderCodec(
				List<String> registeredBlockFormats){
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

		private String parseBlockFormat(BinaryDictionary dictionary){
			byte[] blockFormatBytes = dictionary.get(KvFileHeaderKey.KV_BLOCK_FORMAT.bytes);
			return StringCodec.UTF_8.decode(blockFormatBytes);
		}

	}

}
