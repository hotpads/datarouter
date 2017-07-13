/**
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
package io.datarouter.util.varint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

public class VarInt{

	private final VarLong varLong;

	public VarInt(int value){
		this(new VarLong(value));
	}

	private VarInt(VarLong varLong){
		this.varLong = varLong;
	}

	public int getValue(){
		return (int) varLong.getValue();
	}

	public int getNumBytes(){
		return varLong.getNumBytes();
	}

	public byte[] getBytes(){
		return varLong.getBytes();
	}

	//factories

	public static VarInt fromByteArray(byte[] bytes){
		return new VarInt(VarLong.fromByteArray(bytes));
	}

	public static VarInt fromByteArray(byte[] bytes, int offset){
		return new VarInt(VarLong.fromByteArray(bytes, offset));
	}

	public static VarInt fromInputStream(InputStream is) throws IOException{
		return new VarInt(VarLong.fromInputStream(is));
	}

	public static VarInt fromReadableByteChannel(ReadableByteChannel fs) throws IOException{
		return new VarInt(VarLong.fromReadableByteChannel(fs));
	}

}
