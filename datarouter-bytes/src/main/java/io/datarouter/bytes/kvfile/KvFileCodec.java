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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;

public class KvFileCodec<T>{

	private static final ByteLength DEFAULT_BLOCK_SIZE = ByteLength.ofKiB(64);

	public final Codec<T,KvFileEntry> codec;
	public final ByteLength blockSize;

	public KvFileCodec(Codec<T,KvFileEntry> codec, ByteLength blockSize){
		this.codec = codec;
		this.blockSize = blockSize;
	}

	public KvFileCodec(Codec<T,KvFileEntry> codec){
		this(codec, DEFAULT_BLOCK_SIZE);
	}

	/*--------- encode -----------*/

	public Scanner<byte[]> toByteArrays(Scanner<T> items){
		return items
				.map(codec::encode)
				.batchByMinSize(blockSize.toBytes(), KvFileEntry::length)
				.map(KvFileBlock::new)
				.map(KvFileBlock::toBytes);
	}

	public byte[] toByteArray(Collection<T> items){
		return Scanner.of(items)
				.apply(this::toByteArrays)
				.listTo(ByteTool::concat);
	}

	public InputStream toInputStream(Scanner<T> items){
		return toByteArrays(items)
				.apply(MultiByteArrayInputStream::new);
	}

	/*--------- decode -----------*/

	public T decode(KvFileEntry entry){
		return codec.decode(entry);
	}

	public Scanner<T> decodeMulti(byte[] bytes){
		return decodeMulti(new ByteArrayInputStream(bytes));
	}

	public Scanner<T> decodeMulti(InputStream inputStream){
		return new KvFileReader(inputStream).scanBlockEntries()
				.map(codec::decode);
	}

	public Scanner<T> decodeBlockToScanner(KvFileBlock block){
		return block.scanEntries()
				.map(codec::decode);
	}

	public List<T> decodeBlockToList(KvFileBlock block){
		return decodeBlockToScanner(block)
				.collect(() -> new ArrayList<>(block.entries().size()));
	}

	public List<List<T>> decodeBlocksToLists(List<KvFileBlock> blocks){
		return Scanner.of(blocks)
				.map(this::decodeBlockToList)
				.collect(() -> new ArrayList<>(blocks.size()));
	}

}
