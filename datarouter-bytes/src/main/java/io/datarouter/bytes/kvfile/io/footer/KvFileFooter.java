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
package io.datarouter.bytes.kvfile.io.footer;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.varint.VarIntTool;

public record KvFileFooter(
		BinaryDictionary userDictionary,
		long kvCount){

	public static final Codec<KvFileFooter,BinaryDictionary> DICTIONARY_CODEC = Codec.of(
			KvFileFooter::toBinaryDictionary,
			KvFileFooter::fromBinaryDictionary);

	/*----------- encode ------------*/

	private BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(KvFileFooterKey.USER_DICTIONARY.bytes, userDictionary.encode())
				.put(KvFileFooterKey.KV_COUNT.bytes, VarIntTool.encode(kvCount));
	}

	/*----------- decode ------------*/

	private static KvFileFooter fromBinaryDictionary(BinaryDictionary dictionary){
		return new KvFileFooter(
				parseUserDictionary(dictionary),
				parseKvCount(dictionary));
	}

	private static BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
		byte[] userDictionaryBytes = dictionary.get(KvFileFooterKey.USER_DICTIONARY.bytes);
		return BinaryDictionary.decode(userDictionaryBytes);
	}

	private static long parseKvCount(BinaryDictionary dictionary){
		byte[] kvCountBytes = dictionary.get(KvFileFooterKey.KV_COUNT.bytes);
		return VarIntTool.decodeLong(kvCountBytes);
	}

}
