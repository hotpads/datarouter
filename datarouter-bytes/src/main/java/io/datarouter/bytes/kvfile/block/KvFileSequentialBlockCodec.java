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
package io.datarouter.bytes.kvfile.block;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.kv.KvFileEntryCodec;
import io.datarouter.bytes.varint.VarIntTool;

/**
 * Translates a List of user-provided items to a byte[].
 * Intended for use in Blockfiles of Kvs.
 * Use the identity() method for things like merging Kvs from multiple files without decoding to the user-provided type.
 */
public class KvFileSequentialBlockCodec<T>
implements KvFileBlockCodec<T>{

	private static final Codec<List<KvFileEntry>,byte[]> IDENTITY = Codec.of(
			new KvFileSequentialBlockCodec<>(Codec.identity())::encodeAll,
			new KvFileSequentialBlockCodec<>(Codec.identity())::decodeAll);

	public static Codec<List<KvFileEntry>,byte[]> identity(){
		return IDENTITY;
	}

	private final Codec<T,KvFileEntry> kvCodec;

	public KvFileSequentialBlockCodec(Codec<T,KvFileEntry> kvCodec){
		this.kvCodec = kvCodec;
	}

	@Override
	public byte[] encodeAll(List<T> items){
		int numSizeBytes = VarIntTool.length(items.size());
		int numDataBytes = 0;
		List<KvFileEntry> kvs = new ArrayList<>(items.size());
		for(T item : items){
			KvFileEntry kv = kvCodec.encode(item);
			kvs.add(kv);
			numDataBytes += kv.length();
		}
		int numBytes = numSizeBytes + numDataBytes;
		byte[] bytes = new byte[numBytes];
		int cursor = 0;
		cursor += VarIntTool.encode(bytes, cursor, kvs.size());
		for(KvFileEntry kv : kvs){
			System.arraycopy(kv.backingBytes(), kv.offset(), bytes, cursor, kv.length());
			cursor += kv.length();
		}
		return bytes;
	}

	@Override
	public List<T> decodeAll(byte[] bytes){
		int cursor = 0;
		int size = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(size);
		List<T> items = new ArrayList<>(size);
		for(int i = 0; i < size; ++i){
			KvFileEntry kv = KvFileEntryCodec.fromBytes(bytes, cursor);
			cursor += kv.length();
			items.add(kvCodec.decode(kv));
		}
		return items;
	}

}
