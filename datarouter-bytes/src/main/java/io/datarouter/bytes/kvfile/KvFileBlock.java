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
import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.scanner.Scanner;

public record KvFileBlock(
		List<KvFileEntry> entries){

	/*------- length -------*/

	public int headerLength(){
		int total = 0;
		total += VarIntTool.length(entries.size());
		return total;
	}

	public int dataLength(){
		return entries.stream()
				.mapToInt(KvFileEntry::length)
				.sum();
	}

	public int length(){
		return headerLength() + dataLength();
	}

	public Scanner<KvFileEntry> scanEntries(){
		return Scanner.of(entries);
	}

	/*---------- write ----------*/

	public byte[] toBytes(){
		int numBlockBytes = length();
		int numLengthPrefixBytes = VarIntTool.length(numBlockBytes);
		int bytes = numLengthPrefixBytes + numBlockBytes;
		byte[] blockBytes = new byte[bytes];
		VarIntTool.encode(blockBytes, 0, numBlockBytes);
		bodyToBytes(blockBytes, numLengthPrefixBytes);
		return blockBytes;
	}

	private void bodyToBytes(byte[] bytes, int offset){
		int cursor = offset;

		//size (numEntries)
		cursor += VarIntTool.encode(bytes, cursor, entries.size());

		//entries
		for(KvFileEntry entry : entries){
			System.arraycopy(entry.backingBytes(), entry.offset(), bytes, cursor, entry.length());
			cursor += entry.length();
		}
	}

	/*----------- read ----------*/

	public static KvFileBlock fromBytes(byte[] bytes){
		int cursor = 0;

		int size = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(size);

		List<KvFileEntry> entries = new ArrayList<>(size);
		for(int i = 0; i < size; ++i){
			KvFileEntry entry = KvFileEntrySerializer.fromBytes(bytes, cursor);
			cursor += entry.length();
			entries.add(entry);
		}

		return new KvFileBlock(entries);
	}

	public static byte[] blockBytesFromInputStream(InputStream inputStream){
		Integer numDataBytes = VarIntTool.decodeIntOrNull(inputStream);
		return numDataBytes == null
				? null
				: InputStreamTool.readNBytes(inputStream, numDataBytes);
	}

}
