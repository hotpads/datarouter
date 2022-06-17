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
package io.datarouter.bytes.codec.array.bytearray;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.TerminatedByteArrayTool;
import io.datarouter.bytes.TerminatedByteArrayTool.NumEscapedAndTerminalIndex;

public class TerminatedByteArrayCodec{

	public static final TerminatedByteArrayCodec INSTANCE = new TerminatedByteArrayCodec();

	public byte[] encode(byte[] value){
		return TerminatedByteArrayTool.escapeAndTerminate(value);
	}

	public LengthAndValue<byte[]> decode(byte[] bytes, int offset){
		NumEscapedAndTerminalIndex numEscapedAndTerminalIndex = TerminatedByteArrayTool
				.findEscapedCountAndTerminalIndex(bytes, offset);
		int numEscaped = numEscapedAndTerminalIndex.numEscaped;
		int terminalIndex = numEscapedAndTerminalIndex.terminalIndex;
		int consumedLength = terminalIndex - offset + 1;//includes terminal byte
		byte[] value = TerminatedByteArrayTool.unescapeAndUnterminate(bytes, offset, numEscaped, terminalIndex);
		return new LengthAndValue<>(consumedLength, value);
	}

	public int lengthWithTerminalIndex(byte[] bytes, int offset){
		return TerminatedByteArrayTool.lengthWithTerminator(bytes, offset);
	}

}
