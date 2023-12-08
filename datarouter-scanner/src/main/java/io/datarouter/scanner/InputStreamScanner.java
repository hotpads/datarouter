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
package io.datarouter.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert an InputStream to a Scanner of byte arrays.
 * Unlike BufferedInputStream the byte arrays are not reused, meaning more memory is allocated while scanning.
 */
public class InputStreamScanner extends BaseScanner<byte[]>{
	private static final Logger logger = LoggerFactory.getLogger(InputStreamScanner.class);

	private static final int DEFAULT_CHUNK_SIZE = 8192;// Same as BufferedInputStream buffer size

	private final InputStream inputStream;
	private final int chunkSize;
	private boolean closed;

	private InputStreamScanner(InputStream inputStream, int chunkSize){
		this.inputStream = inputStream;
		this.chunkSize = chunkSize;
		closed = false;
	}

	public static InputStreamScanner of(InputStream inputStream){
		return new InputStreamScanner(inputStream, DEFAULT_CHUNK_SIZE);
	}

	public static InputStreamScanner of(InputStream inputStream, int chunkSize){
		return new InputStreamScanner(inputStream, chunkSize);
	}

	@Override
	public boolean advance(){
		if(closed){
			return false;
		}
		current = readNBytes(inputStream, chunkSize);
		// Try to close the inputStream slightly more eagerly, and in case downstream scanners aren't closing properly.
		if(current.length < chunkSize){
			tryCloseInputStream(inputStream);
			closed = true;
		}
		return current.length > 0;
	}

	@Override
	public void close(){
		tryCloseInputStream(inputStream);
	}

	/*
	 * 3 scenarios:
	 *  - we find the full N bytes
	 *  - we find less than N bytes
	 *  - we find zero bytes (InputStream was still open without any data remaining)
	 */
	private static byte[] readNBytes(InputStream inputStream, int len){
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

	private static void tryCloseInputStream(InputStream inputStream){
		try{
			inputStream.close();
		}catch(IOException e){
			logger.warn("Error closing InputStream {}", e);
		}
	}

}
