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

import io.datarouter.bytes.LengthAndValue;

/**
 * Encodes a String as UTF-8 bytes, adding byte=0 as a terminal character.  To escape 0s inside the String, it
 * replaces 0s with 02, and 1s with 03.
 */
public class TerminatedStringCodec{

	public static final TerminatedStringCodec US_ASCII = new TerminatedStringCodec(StringCodec.US_ASCII);
	public static final TerminatedStringCodec ISO_8859_1 = new TerminatedStringCodec(StringCodec.ISO_8859_1);
	public static final TerminatedStringCodec UTF_8 = new TerminatedStringCodec(StringCodec.UTF_8);

	private static final byte TERMINAL_BYTE = 0;
	private static final byte ESCAPE_BYTE = 1;
	private static final int ESCAPE_SHIFT = 2;// 0 becomes 2; 1 becomes 3

	private final StringCodec stringCodec;

	public TerminatedStringCodec(StringCodec stringCodec){
		this.stringCodec = stringCodec;
	}

	public byte[] encode(String value){
		byte[] utf8Bytes = stringCodec.encode(value);
		int numToEscape = 0;
		for(int i = 0; i < utf8Bytes.length; ++i){
			byte byteI = utf8Bytes[i];
			if(byteI == TERMINAL_BYTE || byteI == ESCAPE_BYTE){
				++numToEscape;
			}
		}
		int escapedLength = utf8Bytes.length + numToEscape;
		byte[] encodedBytes = new byte[escapedLength + 1];//+1 for terminal byte
		if(numToEscape == 0){
			System.arraycopy(utf8Bytes, 0, encodedBytes, 0, utf8Bytes.length);
		}else{
			int utf8Index = 0;
			int escapedIndex = 0;
			while(escapedIndex < escapedLength){
				byte byteI = utf8Bytes[utf8Index];
				if(byteI == TERMINAL_BYTE || byteI == ESCAPE_BYTE){
					encodedBytes[escapedIndex] = ESCAPE_BYTE;
					++escapedIndex;
					encodedBytes[escapedIndex] = (byte)(byteI + ESCAPE_SHIFT);
				}else{
					encodedBytes[escapedIndex] = utf8Bytes[utf8Index];
				}
				++utf8Index;
				++escapedIndex;
			}
		}
		encodedBytes[encodedBytes.length - 1] = TERMINAL_BYTE;
		return encodedBytes;
	}

	public LengthAndValue<String> decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public LengthAndValue<String> decode(byte[] bytes, int offset){
		int terminalIndex = offset;
		while(true){
			if(bytes[terminalIndex] == TERMINAL_BYTE){
				break;
			}
			++terminalIndex;
		}
		int numEscaped = 0;
		for(int i = offset; i < terminalIndex - 1; ++i){
			if(bytes[i] == ESCAPE_BYTE){
				++numEscaped;
			}
		}
		if(numEscaped == 0){
			int escapedLength = terminalIndex - offset;
			int consumedLength = escapedLength + 1;
			String value = stringCodec.decode(bytes, offset, escapedLength);
			return new LengthAndValue<>(consumedLength, value);
		}
		int encodedLength = terminalIndex - offset;
		int decodedLength = encodedLength - numEscaped;
		var decodedBytes = new byte[decodedLength];
		int encodedIndex = offset;
		int decodedIndex = 0;
		while(encodedIndex < terminalIndex){
			if(bytes[encodedIndex] == ESCAPE_BYTE){
				++encodedIndex;
				decodedBytes[decodedIndex] = (byte)(bytes[encodedIndex] - ESCAPE_SHIFT);
			}else{
				decodedBytes[decodedIndex] = bytes[encodedIndex];
			}
			++encodedIndex;
			++decodedIndex;
		}
		int consumedLength = terminalIndex - offset + 1;
		String value = stringCodec.decode(decodedBytes, 0, decodedLength);
		return new LengthAndValue<>(consumedLength, value);
	}

}
