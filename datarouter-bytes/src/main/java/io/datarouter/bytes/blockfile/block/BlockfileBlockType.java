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
package io.datarouter.bytes.blockfile.block;

public enum BlockfileBlockType{

	HEADER('H'),
	VALUE('V'),
	INDEX('I'),
	FOOTER('F');

	public static final int NUM_BYTES = 1;

	public final char code;
	public final byte codeByte;
	public final byte[] codeBytes;

	BlockfileBlockType(char code){
		this.code = code;
		this.codeByte = (byte)code;
		this.codeBytes = new byte[]{codeByte};
	}

	public static BlockfileBlockType decode(byte codeByte){
		return decode((char)codeByte);
	}

	public static BlockfileBlockType decode(char code){
		return switch(code){
			case 'H' -> HEADER;
			case 'V' -> VALUE;
			case 'I' -> INDEX;
			case 'F' -> FOOTER;
			default -> throw new IllegalArgumentException(String.format("unknown code=%s", code));
		};
	}

}
