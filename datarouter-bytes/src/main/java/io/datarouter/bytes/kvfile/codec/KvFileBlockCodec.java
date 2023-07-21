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
package io.datarouter.bytes.kvfile.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

/**
 * Translates a List of user-provided items to a byte[].
 * Intended for use in Blockfiles of Kvs.
 * Use the identity() method for things like merging Kvs from multiple files without decoding to the user-provided type.
 */
public class KvFileBlockCodec<T> implements Codec<List<T>,byte[]>{

	private static final Codec<List<KvFileEntry>,byte[]> IDENTITY = new KvFileBlockCodec<>(Codec.identity());

	private final Codec<List<T>,byte[]> blockCodec;

	public KvFileBlockCodec(Codec<T,KvFileEntry> kvCodec){
		var encoder = new KvFileBlockEncoder<>(kvCodec::encode);
		var decoder = new KvFileBlockDecoder<>(kvCodec::decode);
		blockCodec = Codec.of(encoder::encode, decoder::decode);
	}

	public static Codec<List<KvFileEntry>,byte[]> identity(){
		return IDENTITY;
	}

	@Override
	public byte[] encode(List<T> value){
		return blockCodec.encode(value);
	}

	@Override
	public List<T> decode(byte[] encodedValue){
		return blockCodec.decode(encodedValue);
	}

	public static class KvFileBlockEncoder<T>{
		private final Function<T,KvFileEntry> encoder;

		public KvFileBlockEncoder(Function<T,KvFileEntry> encoder){
			this.encoder = encoder;
		}

		public byte[] encode(List<T> items){
			List<KvFileEntry> kvs = Scanner.of(items)
					.map(encoder::apply)
					.collect(() -> new ArrayList<>(items.size()));// init capacity
			int numBytes = encodedLength(kvs);
			byte[] bytes = new byte[numBytes];
			int cursor = 0;
			cursor += VarIntTool.encode(bytes, cursor, kvs.size());
			for(KvFileEntry kv : kvs){
				System.arraycopy(kv.backingBytes(), kv.offset(), bytes, cursor, kv.length());
				cursor += kv.length();
			}
			return bytes;
		}
	}

	public static class KvFileBlockDecoder<T>{
		private final Function<KvFileEntry,T> decoder;

		public KvFileBlockDecoder(Function<KvFileEntry,T> decoder){
			this.decoder = decoder;
		}

		public List<T> decode(byte[] bytes){
			int cursor = 0;
			int size = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(size);
			List<T> items = new ArrayList<>(size);
			for(int i = 0; i < size; ++i){
				KvFileEntry kv = KvFileEntrySerializer.fromBytes(bytes, cursor);
				cursor += kv.length();
				items.add(decoder.apply(kv));
			}
			return items;
		}
	}

	private static int encodedLength(List<KvFileEntry> kvs){
		int sizeBytesLength = VarIntTool.length(kvs.size());
		int dataBytesLength = 0;
		for(KvFileEntry kv : kvs){
			dataBytesLength += kv.length();
		}
		return sizeBytesLength + dataBytesLength;
	}

}
