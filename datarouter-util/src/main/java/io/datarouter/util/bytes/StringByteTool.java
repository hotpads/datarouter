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
package io.datarouter.util.bytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringByteTool{

	public static byte[] getByteArray(String str, Charset charset){
		if(str == null){
			return null;
		}
		return str.getBytes(charset);
	}

	public static int numUtf8Bytes(String str){
		return getUtf8Bytes(str).length;
	}

	public static byte[] getUtf8Bytes(String str){
		if(str == null){
			return null;
		}
		return str.getBytes(StandardCharsets.UTF_8);
	}

	public static int toUtf8Bytes(String str, byte[] destination, int offset){
		byte[] bytes = getUtf8Bytes(str);
		System.arraycopy(bytes, 0, destination, offset, bytes.length);
		return bytes.length;
	}

	public static String fromUtf8Bytes(byte[] bytes, int offset, int length){
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	public static String fromUtf8Bytes(byte[] bytes){
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static String fromUtf8BytesOffset(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

}
