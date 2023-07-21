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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import io.datarouter.bytes.codec.booleancodec.RawBooleanCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;
import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

public class ByteWriter{

	private static final RawBooleanCodec RAW_BOOLEAN_CODEC = RawBooleanCodec.INSTANCE;
	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;
	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	private final int pageSize;
	private final List<byte[]> pages;
	private int lastPageSize;

	public ByteWriter(int pageSize){
		if(pageSize <= 0){
			throw new IllegalArgumentException("pageSize must be > 0");
		}
		this.pageSize = pageSize;
		this.pages = new ArrayList<>();
		this.lastPageSize = 0;
	}

	public int length(){
		int numFullPages = pages.isEmpty() ? 0 : pages.size() - 1;
		return numFullPages * pageSize + lastPageSize;
	}

	public byte[][] trimmedPages(){
		if(pages.isEmpty()){
			return new byte[][]{};
		}
		var trimmedPages = new byte[pages.size()][];
		int nextPageIndex = 0;
		for(int i = 0; i < pages.size() - 1; ++i){//full pages
			trimmedPages[nextPageIndex++] = pages.get(i);
		}
		if(lastPageSize == pageSize){
			trimmedPages[nextPageIndex++] = lastPage();
		}else{
			var trimmedLastPage = new byte[lastPageSize];
			System.arraycopy(lastPage(), 0, trimmedLastPage, 0, lastPageSize);
			trimmedPages[nextPageIndex++] = trimmedLastPage;
		}
		return trimmedPages;
	}

	public byte[] concat(){
		return concat(0, length());
	}

	public byte[] concat(int from, int to){
		int pageIndex = from / pageSize;
		int byteIndex = from % pageSize;
		int copied = 0;
		int remaining = to - from;
		var output = new byte[remaining];
		while(remaining > 0){
			int remainingInPage = getPageSize(pageIndex) - byteIndex;
			int copyLength = Math.min(remaining, remainingInPage);
			System.arraycopy(pages.get(pageIndex), byteIndex, output, copied, copyLength);
			copied += copyLength;
			remaining -= copyLength;
			++pageIndex;
			byteIndex = 0;
		}
		return output;
	}

	private int getPageSize(int index){
		if(index == pages.size() - 1){
			return lastPageSize;
		}
		return pageSize;
	}

	private byte[] lastPage(){
		return pages.get(pages.size() - 1);
	}

	private int lastPageFreeSpace(){
		if(pages.isEmpty()){
			return 0;
		}
		return pageSize - lastPageSize;
	}

	private void addPageIfFull(){
		if(pages.isEmpty() || lastPageSize == pageSize){
			pages.add(new byte[pageSize]);
			lastPageSize = 0;
		}
	}

	public long crc32(){
		Checksum checksum = new CRC32();
		Scanner.iterate(0, i -> i + 1)
				.limit(pages.size())
				.forEach(pageIndex -> checksum.update(pages.get(pageIndex), 0, getPageSize(pageIndex)));
		return checksum.getValue();
	}

	@Override
	public String toString(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(pages.size())
				.map(pageIndex -> Arrays.copyOf(pages.get(pageIndex), getPageSize(pageIndex)))
				.map(Arrays::toString)
				.collect(Collectors.joining("\n"));
	}

	/*----------- append bytes -------------*/

	public ByteWriter bytes(byte[] value){
		return bytes(value, 0, value.length);
	}

	public ByteWriter bytes(byte[] value, int from, int to){
		int length = to - from;
		if(length == 0){
			return this;
		}
		int inputOffset = from;
		int inputRemaining = length;
		while(inputRemaining > 0){
			addPageIfFull();
			int chunkLength = Math.min(inputRemaining, lastPageFreeSpace());
			System.arraycopy(value, inputOffset, lastPage(), lastPageSize, chunkLength);
			inputOffset += chunkLength;
			inputRemaining -= chunkLength;
			lastPageSize += chunkLength;
		}
		return this;
	}

	public ByteWriter varBytes(byte[] value){
		bytes(VarIntTool.encode(value.length));
		bytes(value);
		return this;
	}

	public ByteWriter booleanByte(boolean value){
		bytes(RAW_BOOLEAN_CODEC.encode(value));
		return this;
	}

	public ByteWriter rawInt(int value){
		if(lastPageFreeSpace() < Integer.BYTES){
			bytes(RAW_INT_CODEC.encode(value));
		}else{
			RAW_INT_CODEC.encode(value, lastPage(), lastPageSize);
			lastPageSize += Integer.BYTES;
		}
		return this;
	}

	public ByteWriter rawInts(int[] values){
		for(int i = 0; i < values.length; ++i){
			rawInt(values[i]);
		}
		return this;
	}

	//could use varLong, but dedicated method is for readability
	public ByteWriter varInt(int value){
		bytes(VarIntTool.encode(value));
		return this;
	}

	public ByteWriter comparableLong(long value){
		if(lastPageFreeSpace() < Long.BYTES){
			bytes(COMPARABLE_LONG_CODEC.encode(value));
		}else{
			COMPARABLE_LONG_CODEC.encode(value, lastPage(), lastPageSize);
			lastPageSize += Long.BYTES;
		}
		return this;
	}

	public ByteWriter rawLong(long value){
		if(lastPageFreeSpace() < Long.BYTES){
			bytes(RAW_LONG_CODEC.encode(value));
		}else{
			RAW_LONG_CODEC.encode(value, lastPage(), lastPageSize);
			lastPageSize += Long.BYTES;
		}
		return this;
	}

	public ByteWriter rawLongs(long[] values){
		for(int i = 0; i < values.length; ++i){
			rawLong(values[i]);
		}
		return this;
	}

	public ByteWriter varLong(long value){
		bytes(VarIntTool.encode(value));
		return this;
	}

	public ByteWriter varUtf8(String value){
		byte[] bytes = StringCodec.UTF_8.encode(value);
		varBytes(bytes);
		return this;
	}

	public ByteWriter comparableUtf8(String value){
		byte[] bytes = StringCodec.UTF_8.encode(value);
		bytes(bytes);
		bytes(new byte[]{0});
		return this;
	}

}
