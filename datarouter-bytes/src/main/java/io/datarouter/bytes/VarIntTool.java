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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import io.datarouter.bytes.codec.bytestringcodec.Base2ByteStringCodec;

public class VarIntTool{

	private static final byte BYTE_7_RIGHT_BITS_SET = 127;
	private static final long LONG_7_RIGHT_BITS_SET = 127;
	private static final long LONG_8TH_BIT_SET = 128;

	public static int length(long value){
		if(value < 0){
			throw new IllegalArgumentException("must be positive");
		}
		if(value == 0){// doesn't work with the formula below
			return 1;
		}
		return (70 - Long.numberOfLeadingZeros(value)) / 7;// 70 comes from 64+(7-1)
	}

	/*------------ to bytes ---------------*/

	public static byte[] encode(long value){
		if(value < 0){
			throw new IllegalArgumentException("must be positive");
		}
		int numBytes = length(value);
		byte[] bytes = new byte[numBytes];
		long remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			bytes[i] = (byte)(remainder & LONG_7_RIGHT_BITS_SET | LONG_8TH_BIT_SET);// set the left bit
			remainder >>= 7;
		}
		bytes[numBytes - 1] = (byte)(remainder & LONG_7_RIGHT_BITS_SET);// do not set the left bit
		return bytes;
	}

	/*------------- from bytes ----------------*/

	public static long decodeLong(byte[] bytes, int offset){
		if(offset >= bytes.length){
			throw new IllegalArgumentException("invalid bytes " + Base2ByteStringCodec.INSTANCE.encode(bytes));
		}
		long value = 0;
		for(int i = 0; ; ++i){
			byte byteVar = bytes[i + offset];
			long shifted = BYTE_7_RIGHT_BITS_SET & bytes[i + offset];// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(byteVar >= 0){// first bit was 0, so that's the last byte in the VarLong
				break;
			}
		}
		return value;
	}

	public static long decodeLong(byte[] bytes){
		return decodeLong(bytes, 0);
	}

	public static int decodeInt(byte[] bytes, int offset){
		return (int)decodeLong(bytes, offset);
	}

	public static int decodeInt(byte[] bytes){
		return decodeInt(bytes, 0);
	}

	public static Optional<Long> fromInputStream(InputStream is){
		return nextBytes(is).map(VarIntTool::decodeLong);
	}

	private static Optional<byte[]> nextBytes(InputStream is){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int byteVar;
		do{
			try{
				byteVar = is.read();
			}catch(IOException e){
				throw new RuntimeException(e);
			}
			// FieldSetTool relies on this IllegalArgumentException to know it's hit the end of a databean
			if(byteVar == -1){// unexpectedly hit the end of the input stream
				return Optional.empty();
			}
			baos.write(byteVar);
		}while(byteVar >= 128);
		return Optional.of(baos.toByteArray());
	}

}
