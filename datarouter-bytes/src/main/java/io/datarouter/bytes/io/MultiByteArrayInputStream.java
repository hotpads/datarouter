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
package io.datarouter.bytes.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class MultiByteArrayInputStream extends InputStream{

	// This should drop references to the input arrays as they're consumed to free memory.
	private final Scanner<byte[]> scanner;
	private byte[] current;
	private int position;

	public MultiByteArrayInputStream(Scanner<byte[]> arrays){
		this.scanner = arrays;
		this.current = EmptyArray.BYTE;
		this.position = 0;
	}

	public MultiByteArrayInputStream(List<byte[]> arrays){
		this(MemoryFreeingScanner.of(arrays));
	}

	@Override
	public int read(){
		while(remainingInCurrent() == 0 && advance()){
		}
		if(remainingInCurrent() == 0){
			return -1;
		}
		byte value = current[position];
		++position;
		return Byte.toUnsignedInt(value);
	}

	@Override
	public int read(byte[] destination, int off, int len){
		if(len == 0){
			return 0;
		}
		int count = 0;
		int destinationCursor = off;
		while(count < len){
			int numCurrent = remainingInCurrent();
			if(numCurrent > 0){
				int numPending = len - count;
				int copySize = Math.min(numPending, numCurrent);
				System.arraycopy(current, position, destination, destinationCursor, copySize);
				position += copySize;
				count += copySize;
				destinationCursor += copySize;
			}
			if(count == len){
				break;
			}
			if(!advance()){
				break;
			}
		}
		return count == 0 ? -1 : count;
	}

	@Override
	public byte[] readAllBytes(){
		var buffer = new ByteArrayOutputStream();
		buffer.write(current, position, remainingInCurrent());
		while(advance()){
			buffer.write(current, position, remainingInCurrent());
		}
		current = EmptyArray.BYTE;
		position = 0;
		return buffer.toByteArray();
	}

	@Override
	public byte[] readNBytes(int len){
		byte[] buffer = new byte[len];
		int numRead = readNBytes(buffer, 0, len);
		return numRead == len
				? buffer
				: Arrays.copyOf(buffer, numRead);
	}

	@Override
	public int readNBytes(byte[] buffer, int off, int len){
		int numBytesRead = read(buffer, off, len);
		return numBytesRead == -1 ? 0 : numBytesRead;
	}

	@Override
	public long transferTo(OutputStream out) throws IOException{
		long count = 0;
		count += remainingInCurrent();
		out.write(current, position, remainingInCurrent());
		while(advance()){
			count += remainingInCurrent();
			out.write(current, position, remainingInCurrent());
		}
		current = EmptyArray.BYTE;
		position = 0;
		return count;
	}

	private int remainingInCurrent(){
		return current.length - position;
	}

	private boolean advance(){
		position = 0;
		if(scanner.advance()){
			current = scanner.current();
			return true;
		}else{
			current = EmptyArray.BYTE;
			return false;
		}
	}

	/**
	 * Hold a list of items to scan, dropping each item when the next item is loaded.
	 * Useful if the input collection consumes a lot of memory and the items aren't needed after being scanned.
	 * The caller is responsible for freeing the memory of the input collection after creating this object.
	 */
	private static class MemoryFreeingScanner<T> extends BaseScanner<T>{

		private final ArrayList<T> list;
		private int index = -1;

		/**
		 * Makes a copy of the input list to avoid mutating the input list.
		 */
		public MemoryFreeingScanner(Collection<T> list){
			this.list = new ArrayList<>(list);// ArrayList for efficient set(i, null)
		}

		public static <T> Scanner<T> of(Collection<T> list){
			return new MemoryFreeingScanner<>(list);
		}

		@Override
		public boolean advance(){
			if(index >= 0 && index < list.size()){
				list.set(index, null);// Drop the current item from memory
			}
			++index;
			return index < list.size();
		}

		@Override
		public T current(){
			return list.get(index);
		}

	}

}
