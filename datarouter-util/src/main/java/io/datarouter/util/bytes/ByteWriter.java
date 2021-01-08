/**
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
package io.datarouter.util.bytes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.collection.ListTool;

public class ByteWriter{

	private final int pageSize;
	private final List<byte[]> pages;
	private int lastPageSize;

	public ByteWriter(int pageSize){
		this.pageSize = pageSize;
		this.pages = new ArrayList<>();
		this.lastPageSize = 0;
	}

	public int length(){
		int numFullPages = pages.isEmpty() ? 0 : pages.size() - 1;
		return numFullPages * pageSize + lastPageSize;
	}

	public List<byte[]> trimmedPages(){
		if(pages.isEmpty()){
			return List.of();
		}
		List<byte[]> trimmedPages = new ArrayList<>();
		for(int i = 0; i < pages.size() - 1; ++i){//full pages
			trimmedPages.add(pages.get(i));
		}
		byte[] lastPage = pages.get(pages.size() - 1);
		if(lastPageSize == pageSize){
			trimmedPages.add(lastPage);
		}else{
			byte[] trimmedLastPage = new byte[lastPageSize];
			System.arraycopy(lastPage, 0, trimmedLastPage, 0, lastPageSize);
			trimmedPages.add(trimmedLastPage);
		}
		return trimmedPages;
	}

	public byte[] concat(){
		byte[] output = new byte[length()];
		int startIndex = 0;
		for(int i = 0; i < pages.size(); ++i){
			int num = getPageSize(i);
			System.arraycopy(pages.get(i), 0, output, startIndex, num);
			startIndex += num;
		}
		return output;
	}

	private int getPageSize(int index){
		if(index == pages.size() - 1){
			return lastPageSize;
		}
		return pageSize;
	}

	private int lastPageFreeSpace(){
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
		if(value.length == 0){
			return this;
		}
		int inputOffset = 0;
		int inputRemaining = value.length;
		while(inputRemaining > 0){
			addPageIfFull();
			int chunkLength = Math.min(inputRemaining, lastPageFreeSpace());
			System.arraycopy(value, inputOffset, ListTool.getLast(pages), lastPageSize, chunkLength);
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
		bytes(BooleanByteTool.getBytes(value));
		return this;
	}

	public ByteWriter rawInt(int value){
		bytes(IntegerByteTool.getRawBytes(value));
		return this;
	}

	//could use varLong, but dedicated method is for readability
	public ByteWriter varInt(int value){
		bytes(VarIntTool.encode(value));
		return this;
	}

	public ByteWriter rawLong(long value){
		bytes(LongByteTool.getRawBytes(value));
		return this;
	}

	public ByteWriter varLong(long value){
		bytes(VarIntTool.encode(value));
		return this;
	}

	public ByteWriter varUtf8(String value){
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		varBytes(bytes);
		return this;
	}

}
