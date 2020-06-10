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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import io.datarouter.util.bytes.ByteTool;

public class VarLong{

	private static final byte BYTE_7_RIGHT_BITS_SET = 127;

	private static final long LONG_7_RIGHT_BITS_SET = 127, LONG_8TH_BIT_SET = 128;

	private final long value;

	public VarLong(long value){
		if(value < 0){
			throw new IllegalArgumentException("must be positive");
		}
		this.value = value;
	}

	public long getValue(){
		return value;
	}

	public byte[] getBytes(){
		int numBytes = getNumBytes();
		byte[] bytes = new byte[numBytes];
		long remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			bytes[i] = (byte)(remainder & LONG_7_RIGHT_BITS_SET | LONG_8TH_BIT_SET);// set the left bit
			remainder >>= 7;
		}
		bytes[numBytes - 1] = (byte)(remainder & LONG_7_RIGHT_BITS_SET);// do not set the left bit
		return bytes;
	}

	public int getNumBytes(){
		if(value == 0){// doesn't work with the formula below
			return 1;
		}
		return (70 - Long.numberOfLeadingZeros(value)) / 7;// 70 comes from 64+(7-1)
	}

	// factories

	public static VarLong fromByteArray(byte[] bytes){
		return fromByteArray(bytes, 0);
	}

	public static VarLong fromByteArray(byte[] bytes, int offset){
		if(offset >= bytes.length){
			throw new IllegalArgumentException("invalid bytes " + ByteTool.getBinaryStringBigEndian(bytes));
		}
		long value = 0;
		for(int i = 0;; ++i){
			byte byteVar = bytes[i + offset];
			long shifted = BYTE_7_RIGHT_BITS_SET & bytes[i + offset];// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(byteVar >= 0){// first bit was 0, so that's the last byte in the VarLong
				break;
			}
		}
		return new VarLong(value);
	}

	public static VarLong fromInputStream(InputStream is) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int byteVar;
		do{
			byteVar = is.read();
			// FieldSetTool relies on this IllegalArgumentException to know it's hit the end of a databean
			if(byteVar == -1){// unexpectedly hit the end of the input stream
				throw new IllegalArgumentException("end of InputStream");
			}
			baos.write(byteVar);
		}while(byteVar >= 128);
		return fromByteArray(baos.toByteArray());
	}

	public static VarLong fromReadableByteChannel(ReadableByteChannel fs) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1);
		while(true){
			byteBuffer.clear();
			if(fs.read(byteBuffer) == -1){// unexpectedly hit the end of the input stream
				throw new IllegalArgumentException("end of InputStream");
			}
			int byteVar = byteBuffer.get(0);
			baos.write(byteVar);
			if(byteVar < 128){
				break;
			}
		}
		return fromByteArray(baos.toByteArray());
	}

}
