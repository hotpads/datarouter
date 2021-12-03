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
package io.datarouter.storage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.scanner.Scanner;

public class MultiByteArrayInputStream extends InputStream{

	private final Scanner<byte[]> scanner;
	private byte[] current;
	private int position;

	public MultiByteArrayInputStream(Iterable<byte[]> arrays){
		this(Scanner.of(arrays));
	}

	public MultiByteArrayInputStream(Scanner<byte[]> arrays){
		this.scanner = arrays;
		this.current = EmptyArray.BYTE;
		this.position = 0;
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
		while(count < len){
			int numCurrent = remainingInCurrent();
			if(numCurrent > 0){
				int numPending = len - count;
				int copySize = Math.min(numPending, numCurrent);
				System.arraycopy(current, position, destination, count, copySize);
				position += copySize;
				count += copySize;
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
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
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

}
