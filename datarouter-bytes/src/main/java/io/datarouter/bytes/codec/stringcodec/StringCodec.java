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
package io.datarouter.bytes.codec.stringcodec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Encodes a String without any length information or terminal bytes.
 */
public class StringCodec{

	public static final StringCodec US_ASCII = new StringCodec(StandardCharsets.US_ASCII);
	public static final StringCodec ISO_8859_1 = new StringCodec(StandardCharsets.ISO_8859_1);
	public static final StringCodec UTF_8 = new StringCodec(StandardCharsets.UTF_8);

	//specifying charset name in String.getBytes leverages ThreadLocal encoder caching
	private final String charsetName;

	public StringCodec(String charsetName){
		if(!Charset.isSupported(charsetName)){
			String message = String.format("Charset %s not supported", charsetName);
			throw new IllegalArgumentException(message);
		}
		this.charsetName = charsetName;
	}

	public StringCodec(Charset charset){
		this.charsetName = charset.name();
	}

	public byte[] encode(String value){
		try{
			return value.getBytes(charsetName);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}

	public int encode(String value, byte[] bytes, int offset){
		byte[] encodedBytes = encode(value);
		System.arraycopy(encodedBytes, 0, bytes, offset, encodedBytes.length);
		return encodedBytes.length;
	}

	public String decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public String decode(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return decode(bytes, offset, length);
	}

	public String decode(byte[] bytes, int offset, int length){
		try{
			return new String(bytes, offset, length, charsetName);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}

}
