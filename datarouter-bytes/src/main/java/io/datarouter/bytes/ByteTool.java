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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteTool{

	public static final byte[][] EMPTY_ARRAY_2 = new byte[0][];

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
		var outs = new byte[length];
		for(int i = 0; i < length; ++i){
			outs[i] = getComparableByte(ins[offset + i]);
		}
		return outs;
	}

	public static byte[] copyOfRange(byte[] original, int from, int length){
		int to = from + length;
		return Arrays.copyOfRange(original, from, to);
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
		var out = new byte[copy.length + 1];
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
			total += array == null ? 0 : array.length;//TODO remove null check
		}
		return total;
	}

	public static int totalLength(List<byte[]> arrays){
		int total = 0;
		for(int i = 0; i < arrays.size(); ++i){
			byte[] array = arrays.get(i);
			total += array == null ? 0 : array.length;//TODO remove null check
		}
		return total;
	}

	public static byte[] concat(byte[]... arrays){
		var result = new byte[totalLength(arrays)];
		int cursor = 0;
		for(int i = 0; i < arrays.length; ++i){
			byte[] array = arrays[i];
			if(array != null){//TODO remove null check
				int length = array.length;
				System.arraycopy(array, 0, result, cursor, length);
				cursor += length;
			}
		}
		return result;
	}

	public static byte[] concat(List<byte[]> arrays){
		var result = new byte[totalLength(arrays)];
		int cursor = 0;
		for(int i = 0; i < arrays.size(); ++i){
			byte[] array = arrays.get(i);
			if(array != null){//TODO remove null check
				int length = array.length;
				System.arraycopy(array, 0, result, cursor, length);
				cursor += length;
			}
		}
		return result;
	}

	public static byte[] padPrefix(byte[] in, int finalWidth){
		var out = new byte[finalWidth];
		int numPaddingBytes = finalWidth - in.length;
		System.arraycopy(in, 0, out, numPaddingBytes, in.length);
		return out;
	}

	/*------------------------- boxed bytes -----------------------------------*/

	@Deprecated//don't box bytes
	public static ArrayList<Byte> toBoxedBytes(byte[] ins){
		ArrayList<Byte> outs = new ArrayList<>(ins.length);
		for(int i = 0; i < ins.length; ++i){
			outs.add(ins[i]);
		}
		return outs;
	}

	@Deprecated//don't box bytes
	public static byte[] fromBoxedBytesNoNegatives(List<Byte> values){
		if(values.isEmpty()){
			return EmptyArray.BYTE;
		}
		var out = new byte[values.size()];
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

}
