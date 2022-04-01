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

import java.util.Arrays;

/**
 * Terminated arrays can be embedded in a bigger array knowing that 0 is the terminal character.  We escape away the 0
 * bytes inside the array.
 *
 * Two escaped arrays will compare equivalently to two unescaped arrays.
 *
 * To escape:
 *   - replace 0 with 1,2.
 *   - replace 1 with 1,3.
 *   - append 0 at the end.
 */
public class TerminatedByteArrayTool{

	public static final byte TERMINAL_BYTE = 0;
	public static final byte ESCAPE_BYTE = 1;
	public static final int ESCAPE_SHIFT = 2;// 0 becomes 2; 1 becomes 3

	/*------------- encode -----------------*/

	public static byte[] escapeAndTerminate(byte[] input){
		int numToEscape = numToEscape(input);
		if(numToEscape == 0){// Common case: use System.arraycopy instead of checking each byte
			return terminate(input);
		}
		int escapedLength = input.length + numToEscape;
		int terminatedLength = escapedLength + 1;
		var result = new byte[terminatedLength];
		int inputIndex = 0;
		int escapedIndex = 0;
		while(escapedIndex < escapedLength){
			byte byteI = input[inputIndex];
			if(needsEscaping(input[inputIndex])){
				result[escapedIndex] = ESCAPE_BYTE;
				++escapedIndex;
				result[escapedIndex] = (byte)(byteI + ESCAPE_SHIFT);
			}else{
				result[escapedIndex] = input[inputIndex];
			}
			++inputIndex;
			++escapedIndex;
		}
		result[result.length - 1] = TERMINAL_BYTE;
		return result;
	}

	private static boolean needsEscaping(byte bite){
		return bite == TERMINAL_BYTE || bite == ESCAPE_BYTE;
	}

	private static int numToEscape(byte[] bytes){
		int numToEscape = 0;
		for(int i = 0; i < bytes.length; ++i){
			if(needsEscaping(bytes[i])){
				++numToEscape;
			}
		}
		return numToEscape;
	}

	private static byte[] terminate(byte[] bytes){
		int terminatedLength = bytes.length + 1;
		var terminatedBytes = new byte[terminatedLength];
		System.arraycopy(bytes, 0, terminatedBytes, 0, bytes.length);
		terminatedBytes[terminatedBytes.length - 1] = TERMINAL_BYTE;
		return terminatedBytes;
	}

	/*------------- decode -----------------*/

	public static class NumEscapedAndTerminalIndex{
		public final int numEscaped;
		public final int terminalIndex;

		public NumEscapedAndTerminalIndex(int numEscaped, int terminalIndex){
			this.numEscaped = numEscaped;
			this.terminalIndex = terminalIndex;
		}
	}

	public static NumEscapedAndTerminalIndex findEscapedCountAndTerminalIndex(byte[] bytes, int offset){
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

	public static byte[] unescapeAndUnterminate(byte[] escapedBytes, int offset, int numEscaped, int terminalIndex){
		if(numEscaped == 0){
			return Arrays.copyOfRange(escapedBytes, offset, terminalIndex);
		}
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

}
