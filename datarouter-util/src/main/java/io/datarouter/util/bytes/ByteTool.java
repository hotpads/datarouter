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
package io.datarouter.util.bytes;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.util.array.ArrayTool;

public class ByteTool{

	public static ArrayList<Byte> getArrayList(byte[] ins){
		ArrayList<Byte> outs = new ArrayList<>(ArrayTool.length(ins));
		for(byte in : ArrayTool.nullSafe(ins)){
			outs.add(in);
		}
		return outs;
	}

	public static byte toUnsignedByte(int intValue){
		// Assert.assertTrue(i >=0 && i <=255);
		// if(i < 128){ return (byte)i; }
		int ib = intValue - 128;
		return (byte)ib;// subtract 256
	}

	// not really sure what this method means anymore
	public static byte fromUnsignedInt0To255(int unsignedIntValue){
		if(unsignedIntValue > 127){
			return (byte)(unsignedIntValue - 0x100);// subtract 256
		}
		return (byte)unsignedIntValue;
	}

	public static int bitwiseCompare(byte[] bytesA, byte[] bytesB){
		int lengthA = ArrayTool.length(bytesA);
		int lengthB = ArrayTool.length(bytesB);
		for(int i = 0, j = 0; i < lengthA && j < lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static int bitwiseCompare(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB){
		for(int i = offsetA, j = offsetB; i < offsetA + lengthA && j < offsetB + lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static boolean equals(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB){
		if(lengthA != lengthB){
			return false;
		}
		for(int i = offsetA + lengthA - 1, j = offsetB + lengthB - 1; i >= 0 && j >= 0; --i, --j){
			if(bytesA[i] != bytesB[j]){
				return false;
			}
		}
		return true;
	}

	public static byte[] getComparableBytes(byte value){
		if(value >= 0){
			return new byte[]{(byte)(value + Byte.MIN_VALUE)};
		}
		return new byte[]{(byte)(value - Byte.MIN_VALUE)};
	}

	public static byte getComparableByte(byte value){
		if(value >= 0){
			return (byte)(value + Byte.MIN_VALUE);
		}
		return (byte)(value - Byte.MIN_VALUE);
	}

	public static byte[] flipToAndFromComparableByteArray(byte[] ins){
		return flipToAndFromComparableByteArray(ins, 0, ins.length);
	}

	// basically a copyOfRange that also flips the bytes
	public static byte[] flipToAndFromComparableByteArray(byte[] ins, int offset, int length){
		byte[] outs = new byte[length];
		for(int i = 0; i < length; ++i){
			outs[i] = getComparableByte(ins[offset + i]);
		}
		return outs;
	}

	public static String getBinaryStringBigEndian(byte[] ba){
		StringBuilder sb = new StringBuilder();
		int len = ArrayTool.length(ba);
		for(int n = 0; n < len; ++n){
			for(int i = 7; i >= 0; --i){
				sb.append(ba[n] >> i & 1);
			}
		}
		return sb.toString();
	}

	public static String getHexString(byte[] bytes){
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for(byte word : bytes){
			String hex = Integer.toHexString(word & 0xff);
			if(hex.length() == 1){
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static byte[] copyOfRange(byte[] in, int offset, int length){
		byte[] out = new byte[length];
		System.arraycopy(in, offset, out, 0, length);
		return out;
	}

	public static byte[] unsignedIncrement(byte[] in){
		byte[] copy = ArrayTool.clone(in);
		if(copy == null){
			throw new IllegalArgumentException("cannot increment null array");
		}
		for(int i = copy.length - 1; i >= 0; --i){
			if(copy[i] == -1){// -1 is all 1-bits, which is the unsigned maximum
				copy[i] = 0;
			}else{
				++copy[i];
				return copy;
			}
		}
		// we maxed out the array
		byte[] out = new byte[copy.length + 1];
		out[0] = 1;
		System.arraycopy(copy, 0, out, 1, copy.length);
		return out;
	}

	public static byte[] unsignedIncrementOverflowToNull(byte[] in){
		byte[] out = ArrayTool.clone(in);
		for(int i = out.length - 1; i >= 0; --i){
			if(out[i] == -1){// -1 is all 1-bits, which is the unsigned maximum
				out[i] = 0;
			}else{
				++out[i];
				return out;
			}
		}
		return null;
	}

	/*------------------------- byte arrays ---------------------------------*/

	public static byte[] concatenate(byte[]... ins){
		if(ins == null){
			return new byte[0];
		}
		int totalLength = 0;
		for(byte[] in : ins){
			totalLength += ArrayTool.length(in);
		}
		byte[] out = new byte[totalLength];
		int startIndex = 0;
		for(byte[] in : ins){
			if(in == null){
				continue;
			}
			System.arraycopy(in, 0, out, startIndex, in.length);
			startIndex += in.length;
		}
		return out;
	}

	public static byte[] padPrefix(byte[] in, int finalWidth){
		byte[] out = new byte[finalWidth];
		int numPaddingBytes = finalWidth - in.length;
		System.arraycopy(in, 0, out, numPaddingBytes, in.length);
		return out;
	}

	/*------------------------- serialize -----------------------------------*/

	public static byte[] getUInt7Bytes(List<Byte> values){
		if(values == null || values.isEmpty()){
			return new byte[0];
		}
		byte[] out = new byte[values.size()];
		int index = 0;
		for(Byte value : values){
			if(value < 0){
				throw new IllegalArgumentException("no negatives");
			}
			out[index] = value;
			++index;
		}
		return out;
	}

	public static byte[] fromUInt7ByteArray(byte[] bytes, int offset, int length){
		// validate?
		return copyOfRange(bytes, offset, length);
	}

}
