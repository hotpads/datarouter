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
package io.datarouter.bytes;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;

/**
 * Encodes key/value pairs to a flattened byte[].
 * Keys are sorted via Arrays::compareUnsigned.
 * Duplicate keys are rejected.
 * Size and lengths are encoded with variable lenght ints.
 *
 * Format:
 * [size] (number of entries)
 * [keyLen0][key0][valLen0][val0]
 * [keyLen1][key1][valLen1][val1]
 * [etc]
 */
public class BinaryDictionary{

	public static final Codec<BinaryDictionary,byte[]> CODEC = Codec.of(
			BinaryDictionary::encode,
			BinaryDictionary::decode);

	private final SortedMap<byte[],byte[]> dictionary;

	public BinaryDictionary(){
		dictionary = new TreeMap<>(Arrays::compareUnsigned);
	}

	/*----------- write ----------*/

	public BinaryDictionary put(byte[] key, byte[] value){
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		if(dictionary.containsKey(key)){
			String message = String.format(
					"cannot overwrite existing key=%s and value=%s",
					HexByteStringCodec.INSTANCE.encode(key),
					HexByteStringCodec.INSTANCE.encode(value));
			throw new IllegalArgumentException(message);
		}
		dictionary.put(key, value);
		return this;
	}

	public BinaryDictionary put(String key, byte[] value){
		return put(StringCodec.UTF_8.encode(key), value);
	}

	public BinaryDictionary put(String key, String value){
		return put(StringCodec.UTF_8.encode(key), StringCodec.UTF_8.encode(value));
	}

	/*---------- read -------------*/

	public int size(){
		return dictionary.size();
	}

	public byte[] get(byte[] key){
		return dictionary.get(key);
	}

	public byte[] get(String key){
		return dictionary.get(StringCodec.UTF_8.encode(key));
	}

	public Optional<byte[]> find(byte[] key){
		return Optional.ofNullable(get(key));
	}

	public Optional<byte[]> find(String key){
		return Optional.ofNullable(get(key));
	}

	public Optional<String> findStringValue(String key){
		return find(key)
				.map(StringCodec.UTF_8::decode);
	}

	/*--------- Object ------------*/

	@Override
	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(!(obj instanceof BinaryDictionary)){
			return false;
		}
		BinaryDictionary that = (BinaryDictionary)obj;
		if(size() != that.size()){
			return false;
		}
		var thisKvs = dictionary.entrySet().iterator();
		var thatKvs = that.dictionary.entrySet().iterator();
		while(thisKvs.hasNext()){
			var thisKv = thisKvs.next();
			var thatKv = thatKvs.next();
			if(!Arrays.equals(thisKv.getKey(), thatKv.getKey())
					|| !Arrays.equals(thisKv.getValue(), thatKv.getValue())){
				return false;
			}
		}
		return true;
	}


	@Override
	public int hashCode(){
		int hash = 1;
		for(var kv : dictionary.entrySet()){
			hash += 31 * hash + Arrays.hashCode(kv.getKey());
			hash += 31 * hash + Arrays.hashCode(kv.getValue());
		}
		return hash;
	}

	/*--------- encode ------------*/

	public int encodedLength(){
		int length = VarIntTool.length(dictionary.size());
		for(var kv : dictionary.entrySet()){
			length += VarIntTool.length(kv.getKey().length);
			length += kv.getKey().length;
			length += VarIntTool.length(kv.getValue().length);
			length += kv.getValue().length;
		}
		return length;
	}

	public byte[] encode(){
		return ObjectScanner.of(VarIntTool.encode(dictionary.size()))
				.append(Scanner.of(dictionary.entrySet())
						.concat(kv -> Scanner.of(
								VarIntTool.encode(kv.getKey().length),
								kv.getKey(),
								VarIntTool.encode(kv.getValue().length),
								kv.getValue())))
				.listTo(ByteTool::concat);
	}

	public static BinaryDictionary decode(byte[] bytes){
		var blockfileDictionary = new BinaryDictionary();
		int cursor = 0;
		int numEntries = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(numEntries);
		for(int i = 0; i < numEntries; ++i){
			int keyLength = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(keyLength);
			byte[] key = Arrays.copyOfRange(bytes, cursor, cursor + keyLength);
			cursor += keyLength;
			int valueLength = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(valueLength);
			byte[] value = Arrays.copyOfRange(bytes, cursor, cursor + valueLength);
			cursor += valueLength;
			blockfileDictionary.put(key, value);
		}
		return blockfileDictionary;
	}

}
