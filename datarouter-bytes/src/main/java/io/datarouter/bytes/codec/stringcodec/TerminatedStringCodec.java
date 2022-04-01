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
import io.datarouter.bytes.TerminatedByteArrayTool;
import io.datarouter.bytes.TerminatedByteArrayTool.NumEscapedAndTerminalIndex;

public class TerminatedStringCodec{

	public static final TerminatedStringCodec US_ASCII = new TerminatedStringCodec(StringCodec.US_ASCII);
	public static final TerminatedStringCodec ISO_8859_1 = new TerminatedStringCodec(StringCodec.ISO_8859_1);
	public static final TerminatedStringCodec UTF_8 = new TerminatedStringCodec(StringCodec.UTF_8);

	private final StringCodec stringCodec;

	public TerminatedStringCodec(StringCodec stringCodec){
		this.stringCodec = stringCodec;
	}

	public byte[] encode(String value){
		byte[] encodedBytes = stringCodec.encode(value);
		return TerminatedByteArrayTool.escapeAndTerminate(encodedBytes);
	}

	public LengthAndValue<String> decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public LengthAndValue<String> decode(byte[] bytes, int offset){
		NumEscapedAndTerminalIndex numEscapedAndTerminalIndex = TerminatedByteArrayTool
				.findEscapedCountAndTerminalIndex(bytes, offset);
		int numEscaped = numEscapedAndTerminalIndex.numEscaped;
		int terminalIndex = numEscapedAndTerminalIndex.terminalIndex;
		int consumedLength = terminalIndex - offset + 1;
		if(numEscaped == 0){// Common case: value does not contain a zero byte
			int escapedLength = terminalIndex - offset;
			String value = stringCodec.decode(bytes, offset, escapedLength);
			return new LengthAndValue<>(consumedLength, value);
		}
		byte[] unescapedAndUnterminatedBytes = TerminatedByteArrayTool.unescapeAndUnterminate(bytes, offset, numEscaped,
				terminalIndex);
		String value = stringCodec.decode(unescapedAndUnterminatedBytes);
		return new LengthAndValue<>(consumedLength, value);
	}

}
