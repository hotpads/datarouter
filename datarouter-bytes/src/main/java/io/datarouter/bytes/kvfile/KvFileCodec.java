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
package io.datarouter.bytes.kvfile;

import java.io.InputStream;
import java.util.Collection;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;

public class KvFileCodec<T>{

	public final Codec<T,KvFileEntry> codec;

	public KvFileCodec(Codec<T,KvFileEntry> codec){
		this.codec = codec;
	}

	/*--------- encode -----------*/

	public KvFileEntry encode(T item){
		return codec.encode(item);
	}

	public Scanner<byte[]> toByteArrays(Scanner<T> items){
		return items
				.map(codec::encode)
				.map(KvFileEntry::bytes);
	}

	public byte[] toByteArray(Collection<T> items){
		return Scanner.of(items)
				.map(codec::encode)
				.map(KvFileEntry::bytes)
				.listTo(ByteTool::concat);
	}

	public InputStream toInputStream(Scanner<T> items){
		return items
				.map(codec::encode)
				.map(KvFileEntry::bytes)
				.apply(MultiByteArrayInputStream::new);
	}

	/*--------- decode -----------*/

	public T decode(KvFileEntry entry){
		return codec.decode(entry);
	}

	public T decode(byte[] bytes){
		KvFileEntry entry = KvFileEntrySerializer.fromBytes(bytes);
		return codec.decode(entry);
	}

	public Scanner<T> decodeMulti(byte[] bytes){
		return KvFileEntrySerializer.decodeMulti(bytes)
				.map(codec::decode);
	}

	public Scanner<T> decodeMulti(InputStream inputStream){
		return KvFileEntrySerializer.decodeMulti(inputStream)
				.map(codec::decode);
	}

}
