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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import io.datarouter.scanner.Scanner;

public class OutputStreamTool{

	public static void write(OutputStream outputStream, byte[] bytes){
		try{
			outputStream.write(bytes);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static void write(OutputStream outputStream, byte[] bytes, int offset, int length){
		try{
			outputStream.write(bytes, offset, length);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static void write(OutputStream outputStream, Scanner<byte[]> byteArrays){
		try{
			for(byte[] bytes : byteArrays.iterable()){
				outputStream.write(bytes);
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static void close(OutputStream outputStream){
		try{
			outputStream.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
