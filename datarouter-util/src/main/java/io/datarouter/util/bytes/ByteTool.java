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
import java.util.Arrays;
import java.util.List;

import io.datarouter.util.array.ArrayTool;

public class ByteTool{

	public static final byte[] EMPTY_ARRAY = new byte[0];
	public static final byte[][] EMPTY_ARRAY_2 = new byte[0][];

	public static ArrayList<Byte> getArrayList(byte[] ins){
		ArrayList<Byte> outs = new ArrayList<>(ArrayTool.length(ins));
		for(byte in : ArrayTool.nullSafe(ins)){
			outs.add(in);
		}
		return outs;
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

	public static String getIntString(byte[] bytes){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < bytes.length; ++i){
			if(i > 0){
				sb.append(",");
			}
			sb.append(Byte.toUnsignedInt(bytes[i]));
		}
		sb.append("]");
		return sb.toString();
	}

	public static byte[] copyOfRange(byte[] in, int offset, int length){
		byte[] out = new byte[length];
		System.arraycopy(in, offset, out, 0, length);
		return out;
	}

	public static byte[] unsignedIncrement(byte[] in){
		if(in.length == 0){
			throw new IllegalArgumentException("cannot increment empty array");
		}
		byte[] copy = Arrays.copyOf(in, in.length);
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
		byte[] out = Arrays.copyOf(in, in.length);
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

	public static int totalLength(byte[]... arrays){
		int total = 0;
		for(int i = 0; i < arrays.length; ++i){
			byte[] array = arrays[i];
			total += array == null ? 0 : array.length;
		}
		return total;
	}

	public static byte[] concatenate(List<byte[]> ins){
		byte[][] arrays = new byte[ins.size()][];
		for(int i = 0; i < ins.size(); ++i){
			arrays[i] = ins.get(i);
		}
		return concatenate(arrays);
	}

	public static byte[] concatenate(byte[]... ins){
		if(ins == null){
			return new byte[0];
		}
		byte[] out = new byte[totalLength(ins)];
		int startIndex = 0;
		for(int i = 0; i < ins.length; ++i){
			byte[] in = ins[i];
			if(in != null){
				System.arraycopy(in, 0, out, startIndex, in.length);
				startIndex += in.length;
			}
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