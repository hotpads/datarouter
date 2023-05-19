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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import io.datarouter.scanner.Scanner;

public class InputStreamTool{

	public static void close(InputStream inputStream){
		try{
			inputStream.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static long count(InputStream inputStream, int bufferSize){
		long count = 0;
		try{
			var buffer = new byte[bufferSize];
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

	public static long countByte(InputStream inputStream, int bufferSize, byte matchByte){
		long count = 0;
		try{
			var buffer = new byte[bufferSize];
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

	/**
	 * Read 1 byte, throwing if it wasn't found.
	 */
	public static byte readRequiredByte(InputStream inputStream){
		try{
			int value = inputStream.read();
			if(value == -1){
				throw new IllegalStateException("Unexpected end of stream");
			}
			return (byte)value;
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Keeps reading from the InputStream until length bytes are found, or we're out of data.
	 * Can be used on InputStreams that return data incrementally but you prefer to block until length bytes are found.
	 */
	public static int readUntilLength(InputStream inputStream, byte[] buffer, int offset, int length){
		try{
			int cursor = offset;
			int remaining = length;
			int totalRead = 0;
			while(remaining > 0){
				int numRead = inputStream.read(buffer, cursor, remaining);
				if(numRead == -1){
					return totalRead > 0 ? totalRead : -1;
				}
				cursor += numRead;
				remaining -= numRead;
				totalRead += numRead;
			}
			return totalRead;
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static byte[] readNBytes(InputStream inputStream, int len){
		var bytes = new byte[len];
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

	public static Scanner<byte[]> scanChunks(InputStream inputStream, int chunkSize){
		return Scanner.generate(() -> InputStreamTool.readNBytes(inputStream, chunkSize))
				.advanceWhile(chunk -> chunk.length > 0);
	}

	public static byte[] toArray(InputStream inputStream){
		var baos = new ByteArrayOutputStream();
		transferTo(inputStream, baos);
		return baos.toByteArray();
	}

	public static long transferTo(InputStream inputStream, OutputStream outputStream){
		try{
			return inputStream.transferTo(outputStream);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static long transferToAndClose(InputStream inputStream, OutputStream outputStream){
		try(var closeInput = inputStream; var closeOutput = outputStream){
			return inputStream.transferTo(outputStream);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

}
