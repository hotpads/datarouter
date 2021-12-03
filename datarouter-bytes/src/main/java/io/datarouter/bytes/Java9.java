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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Java9{

	// InputStream.DEFAULT_BUFFER_SIZE
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	@SafeVarargs
	public static <T> List<T> listOf(T... items){
		// return List.of(items);

		for(int i = 0; i < items.length; ++i){
			Objects.requireNonNull(items[i]);
		}
		return Collections.unmodifiableList(Arrays.asList(items));
	}

	public static int compareUnsigned(byte[] bytesA, byte[] bytesB){
		// return Arrays.compareUnsigned(bytesA, bytesB);

		if(bytesA == bytesB){
			return 0;
		}
		if(bytesA == null || bytesB == null){
			return bytesA == null ? -1 : 1;
		}

		int lengthA = bytesA.length;
		int lengthB = bytesB.length;
		for(int i = 0, j = 0; i < lengthA && j < lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static int checkFromIndexSize(int fromIndex, int size, int length){
		// return Objects.checkFromIndexSize(int fromIndex, int size, int length);

		if(fromIndex < 0){
			throw new IllegalArgumentException("fromIndex < 0");
		}
		if(size < 0){
			throw new IllegalArgumentException("size < 0");
		}
		if(fromIndex + size > length){
			throw new IllegalArgumentException("fromIndex + size > length");
		}
		if(length < 0){
			throw new IllegalArgumentException("length < 0");
		}
		return fromIndex;
	}

	public static int readNBytes(InputStream inputStream, byte[] bytes, int off, int len) throws IOException{
		// return inputStream.readNBytes(byte[] bytes, int off, int len);

		checkFromIndexSize(off, len, bytes.length);

		int num = 0;
		while(num < len){
			int count = inputStream.read(bytes, off + num, len - num);
			if(count < 0){
				break;
			}
			num += count;
		}
		return num;
	}

	public static long transferTo(InputStream inputStream, OutputStream out) throws IOException{
		// return inputStream.transferTo(out);

		Objects.requireNonNull(out, "out");
		long transferred = 0;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;
		while((read = inputStream.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0){
			out.write(buffer, 0, read);
			transferred += read;
		}
		return transferred;
	}

}
