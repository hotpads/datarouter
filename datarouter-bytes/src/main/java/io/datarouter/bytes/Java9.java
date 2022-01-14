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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Java9{

	public static final Comparator<byte[]> UNSIGNED_BYTE_ARRAY_COMPARATOR = (a, b) -> compareUnsigned(a, b);

	@SafeVarargs
	public static <T> List<T> listOf(T... items){
		return List.of(items);
	}

	public static boolean isOptionalEmpty(Optional<?> optional){
		return optional.isEmpty();
	}

	public static int compareUnsigned(byte[] bytesA, byte[] bytesB){
		return Arrays.compareUnsigned(bytesA, bytesB);
	}

	public static int checkFromIndexSize(int fromIndex, int size, int length){
		return Objects.checkFromIndexSize(fromIndex, size, length);
	}

	public static int readNBytes(InputStream inputStream, byte[] bytes, int off, int len) throws IOException{
		return inputStream.readNBytes(bytes, off, len);
	}

	public static long transferTo(InputStream inputStream, OutputStream out) throws IOException{
		return inputStream.transferTo(out);
	}

}
