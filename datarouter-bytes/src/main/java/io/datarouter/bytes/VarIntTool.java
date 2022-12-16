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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Encodes a positive long value to a variable number of bytes.
 * The leftmost bit (the sign bit) of each byte indicates if there are more values.
 * A "1" bit (negative java byte value) indicates there is at least one more value byte.
 * The leftmost byte is the least significant byte, each of which is negative until the last byte which is positive.
 * Note the bitwise ordering of the byte arrays does not match Integer/Long ordering.
 *
 * Each byte contains 1 metadata/continuation bit and 7 data bits:
 *   - Values 0 to 127 are encoded in 1 byte.
 *   - Values 128 to 16_383 are encoded in 2 bytes.
 *   - Values 16_384 to 2_097_151 are encoded in 3 bytes.
 *   - Values 2_097_152 to 268_435_455 are encoded in 4 bytes.
 *   - Etc.
 *   - Long.MAX_VALUE is encoded in 9 bytes.
 *
 * The primary use case is for encoding many values that usually fit in 1 or sometimes 2 bytes.
 * This can save 75% of space versus encoding ints to 4 bytes while still allowing big values.
 *
 * Calling length(value) calculates the encoded length quickly, faster than encoding the value to obtain the length.
 * Encoding can be written to an OutputStream without heap allocations.
 * Decoding can be read from an InputStream without heap allocations.
 */
public class VarIntTool{

	public static final String ERROR_MESSAGE_INVALID_OFFSET = "InvalidOffset";
	public static final String ERROR_MESSAGE_INCOMPLETE_VALUE = "Incomplete value";
	public static final String ERROR_MESSAGE_INTEGER_OVERFLOW = "Integer overflow";
	public static final String ERROR_MESSAGE_IO = "IO Error";
	public static final String ERROR_MESSAGE_MAX_SIZE_EXCEEDED = "Max size exceeded";
	public static final String ERROR_MESSAGE_NEGATIVE_VALUE = "Cannot be negative";
	public static final String ERROR_MESSAGE_UNEXPECTED_END_OF_INPUT_STREAM = "Unexpected end of InputStream";

	public static final byte[] INTEGER_MAX_ENCODED_VALUE = {-1, -1, -1, -1, 7};
	public static final byte[] LONG_MAX_ENCODED_VALUE = {-1, -1, -1, -1, -1, -1, -1, -1, 127};

	private static final byte BYTE_7_RIGHT_BITS_SET = 127;
	private static final long LONG_7_RIGHT_BITS_SET = 127;
	private static final long LONG_8TH_BIT_SET = 128;
	private static final int MAX_LENGTH = 9;

	private static void validate(long value){
		if(value < 0){
			throw new IllegalArgumentException(ERROR_MESSAGE_NEGATIVE_VALUE);
		}
	}

	public static int length(long value){
		validate(value);
		if(value == 0){// doesn't work with the formula below
			return 1;
		}
		return (70 - Long.numberOfLeadingZeros(value)) / 7;// 70 comes from 64+(7-1)
	}

	/*------------ to bytes ---------------*/

	public static byte[] encode(long value){
		int length = length(value);
		byte[] bytes = new byte[length];
		encode(bytes, 0, value);
		return bytes;
	}

	// Returns the length of the value encoded, which is the number of bytes written to the array
	public static int encode(byte[] bytes, int offset, long value){
		validate(value);
		int length = length(value);
		long remainder = value;
		int cursor = offset;
		for(int i = 0; i < length - 1; ++i){
			bytes[cursor] = (byte)(remainder & LONG_7_RIGHT_BITS_SET | LONG_8TH_BIT_SET);// set the left bit
			++cursor;
			remainder >>= 7;
		}
		bytes[cursor] = (byte)(remainder & LONG_7_RIGHT_BITS_SET);// do not set the left bit
		++cursor;
		return length;
	}

	public static int encode(OutputStream outputStream, long value){
		validate(value);
		int length = length(value);
		long remainder = value;
		try{
			for(int i = 0; i < length - 1; ++i){
				byte bite = (byte)(remainder & LONG_7_RIGHT_BITS_SET | LONG_8TH_BIT_SET);// set the left bit
				outputStream.write(bite);
				remainder >>= 7;
			}
			byte bite = (byte)(remainder & LONG_7_RIGHT_BITS_SET);// do not set the left bit
			outputStream.write(bite);
		}catch(IOException e){
			throw new UncheckedIOException(ERROR_MESSAGE_IO, e);
		}
		return length;
	}

	/**
	 * @deprecated inline me
	 */
	@Deprecated
	public static void writeBytes(long value, OutputStream os){
		encode(os, value);
	}

	/*------------- from bytes ----------------*/

	public static long decodeLong(byte[] bytes, int offset){
		if(offset >= bytes.length){
			throw new IllegalArgumentException(ERROR_MESSAGE_INVALID_OFFSET);
		}
		long value = 0;
		for(int i = 0; ; ++i){
			if(i == MAX_LENGTH){
				throw new IllegalArgumentException(ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
			}
			byte byteVar = bytes[i + offset];
			long shifted = BYTE_7_RIGHT_BITS_SET & byteVar;// kill leftmost bit
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

	public static long decodeLong(InputStream inputStream){
		return decodeLongInternal(inputStream);
	}

	// returns null if the end of the InputStream was found
	public static Long decodeLongOrNull(InputStream inputStream){
		try{
			return decodeLongInternal(inputStream);
		}catch(IllegalArgumentException e){
			return null;
		}
	}

	// all operations inside a single try/catch
	private static long decodeLongInternal(InputStream inputStream){
		long value = 0;
		try{
			for(int i = 0; ; ++i){
				if(i == MAX_LENGTH){
					throw new IllegalArgumentException(ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
				}
				int byteInt = inputStream.read();
				if(byteInt == -1){
					throw new IllegalArgumentException(ERROR_MESSAGE_UNEXPECTED_END_OF_INPUT_STREAM);
				}
				byte bite = (byte)byteInt;
				long shifted = BYTE_7_RIGHT_BITS_SET & bite;// kill leftmost bit
				shifted <<= 7 * i;
				value |= shifted;
				if(bite >= 0){// first bit was 0, so that's the last byte in the VarLong
					break;
				}
			}
		}catch(IOException e){
			throw new UncheckedIOException(ERROR_MESSAGE_IO, e);
		}
		return value;
	}

	/*--------- decode to Optional -----------*/

	/**
	 * Convenience methods for either:
	 * - returning a value
	 * - returning Optional.empty() if the end of the InputStream was found
	 */
	//TODO rename decodeLongOptional
	public static Optional<Long> fromInputStream(InputStream is){
		return nextBytes(is)
				.map(VarIntTool::decodeLong);
	}

	//TODO rename decodeIntOptional
	public static Optional<Integer> fromInputStreamInt(InputStream is){
		return nextBytes(is)
				.map(VarIntTool::decodeInt);
	}

	private static Optional<byte[]> nextBytes(InputStream is){
		byte[] buffer = new byte[MAX_LENGTH];
		int cursor = 0;
		boolean foundFirstByte = false;
		try{
			int byteVar;
			do{
				if(cursor == MAX_LENGTH){
					throw new IllegalArgumentException(ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
				}
				byteVar = is.read();
				if(byteVar == -1){
					if(foundFirstByte){
						throw new IllegalArgumentException(ERROR_MESSAGE_INCOMPLETE_VALUE);
					}
					return Optional.empty();
				}else{
					buffer[cursor] = (byte)byteVar;
					++cursor;
					foundFirstByte = true;
				}
			}while(byteVar >= 128);
		}catch(IOException e){
			throw new UncheckedIOException(ERROR_MESSAGE_IO, e);
		}
		return Optional.of(buffer);
	}

	/*--------- decode to int -----------*/

	public static int decodeInt(byte[] bytes, int offset){
		return downcastToInt(decodeLong(bytes, offset));
	}

	public static int decodeInt(byte[] bytes){
		return downcastToInt(decodeLong(bytes, 0));
	}

	public static long decodeInt(InputStream inputStream){
		return downcastToInt(decodeLongInternal(inputStream));
	}

	public static Integer decodeIntOrNull(InputStream inputStream){
		Long value = decodeLongOrNull(inputStream);
		return value == null ? null : Math.toIntExact(value);
	}

	private static int downcastToInt(long value){
		if(value > Integer.MAX_VALUE){
			throw new IllegalArgumentException(ERROR_MESSAGE_INTEGER_OVERFLOW);
		}
		return (int)value;
	}

}
