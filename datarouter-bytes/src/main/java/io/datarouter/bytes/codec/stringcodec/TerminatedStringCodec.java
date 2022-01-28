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
		byte[] encodedBytes = stringCodec.encode(value);
		return escapeAndTerminate(encodedBytes);
	}

	public LengthAndValue<String> decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public LengthAndValue<String> decode(byte[] bytes, int offset){
		NumEscapedAndTerminalIndex escapedCountAndTerminalIndex = findEscapedCountAndTerminalIndex(bytes, offset);
		int numEscaped = escapedCountAndTerminalIndex.numEscaped;
		int terminalIndex = escapedCountAndTerminalIndex.terminalIndex;
		int consumedLength = terminalIndex - offset + 1;
		if(numEscaped == 0){// Common case: value does not contain a zero byte
			int escapedLength = terminalIndex - offset;
			String value = stringCodec.decode(bytes, offset, escapedLength);
			return new LengthAndValue<>(consumedLength, value);
		}
		byte[] unescapedAndUnterminatedBytes = unescapeAndUnterminate(bytes, offset, numEscaped, terminalIndex);
		String value = stringCodec.decode(unescapedAndUnterminatedBytes);
		return new LengthAndValue<>(consumedLength, value);
	}

	/*------------- private -----------------*/

	private static boolean needsEscaping(byte bite){
		return bite == TERMINAL_BYTE || bite == ESCAPE_BYTE;
	}

	private static int numToEscape(byte[] encodedBytes){
		int numToEscape = 0;
		for(int i = 0; i < encodedBytes.length; ++i){
			if(needsEscaping(encodedBytes[i])){
				++numToEscape;
			}
		}
		return numToEscape;
	}

	private static byte[] escapeAndTerminate(byte[] encodedBytes){
		int numToEscape = numToEscape(encodedBytes);
		if(numToEscape == 0){// Common case: use System.arraycopy instead of checking each byte
			return terminate(encodedBytes);
		}
		int escapedLength = encodedBytes.length + numToEscape;
		int terminatedLength = escapedLength + 1;
		byte[] result = new byte[terminatedLength];
		int encodedIndex = 0;
		int escapedIndex = 0;
		while(escapedIndex < escapedLength){
			byte byteI = encodedBytes[encodedIndex];
			if(needsEscaping(encodedBytes[encodedIndex])){
				result[escapedIndex] = ESCAPE_BYTE;
				++escapedIndex;
				result[escapedIndex] = (byte)(byteI + ESCAPE_SHIFT);
			}else{
				result[escapedIndex] = encodedBytes[encodedIndex];
			}
			++encodedIndex;
			++escapedIndex;
		}
		result[result.length - 1] = TERMINAL_BYTE;
		return result;
	}

	private static byte[] terminate(byte[] encodedBytes){
		int terminatedLength = encodedBytes.length + 1;
		byte[] result = new byte[terminatedLength];
		System.arraycopy(encodedBytes, 0, result, 0, encodedBytes.length);
		result[result.length - 1] = TERMINAL_BYTE;
		return result;
	}

	private static NumEscapedAndTerminalIndex findEscapedCountAndTerminalIndex(byte[] bytes, int offset){
		int numEscaped = 0;
		int index = offset;
		while(true){
			if(bytes[index] == ESCAPE_BYTE){
				++numEscaped;
			}else if(bytes[index] == TERMINAL_BYTE){
				break;
			}
			++index;
		}
		return new NumEscapedAndTerminalIndex(numEscaped, index);
	}

	private static byte[] unescapeAndUnterminate(byte[] escapedBytes, int offset, int numEscaped, int terminalIndex){
		int escapedLength = terminalIndex - offset;
		int unescapedLength = escapedLength - numEscaped;
		var unescapedBytes = new byte[unescapedLength];
		int escapedIndex = offset;
		int unescapedIndex = 0;
		while(escapedIndex < terminalIndex){
			if(escapedBytes[escapedIndex] == ESCAPE_BYTE){
				++escapedIndex;
				unescapedBytes[unescapedIndex] = (byte)(escapedBytes[escapedIndex] - ESCAPE_SHIFT);
			}else{
				unescapedBytes[unescapedIndex] = escapedBytes[escapedIndex];
			}
			++escapedIndex;
			++unescapedIndex;
		}
		return unescapedBytes;
	}

	private static class NumEscapedAndTerminalIndex{
		final int numEscaped;
		final int terminalIndex;

		public NumEscapedAndTerminalIndex(int numEscaped, int terminalIndex){
			this.numEscaped = numEscaped;
			this.terminalIndex = terminalIndex;
		}
	}

}
