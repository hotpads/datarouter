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
package io.datarouter.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import io.datarouter.scanner.Scanner;

public class InputStreamTool{

	public static final long count(InputStream inputStream, int bufferSize){
		long count = 0;
		try{
			byte[] buffer = new byte[bufferSize];
			while(true){
				int numRead = inputStream.read(buffer);
				if(numRead == -1){
					break;
				}
				count += numRead;
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return count;
	}

	public static final long countByte(InputStream inputStream, int bufferSize, byte matchByte){
		long count = 0;
		try{
			byte[] buffer = new byte[bufferSize];
			while(true){
				int numRead = inputStream.read(buffer);
				if(numRead == -1){
					break;
				}
				for(int i = 0; i < numRead; ++i){
					if(buffer[i] == matchByte){
						++count;
					}
				}
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return count;
	}

	public static final byte[] readThroughByte(InputStream inputStream, byte throughByte){
		int throughInt = Byte.toUnsignedInt(throughByte);
		var result = new ByteArrayOutputStream();
		try{
			while(true){
				int in = inputStream.read();
				result.write(in);
				if(in == throughInt){
					break;
				}
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return result.toByteArray();
	}

	public static final byte[] readNBytes(InputStream inputStream, int len){
		byte[] bytes = new byte[len];
		try{
			//avoid InputStream.readNBytes(int) which builds an ArrayList of small chunks then concatenates
			int numRead = inputStream.readNBytes(bytes, 0, len);
			if(numRead == len){
				return bytes;
			}
			return Arrays.copyOfRange(bytes, 0, numRead);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static final Scanner<byte[]> scanChunks(InputStream inputStream, int chunkSize){
		return Scanner.generate(() -> InputStreamTool.readNBytes(inputStream, chunkSize))
				.advanceWhile(chunk -> chunk.length > 0);
	}

}