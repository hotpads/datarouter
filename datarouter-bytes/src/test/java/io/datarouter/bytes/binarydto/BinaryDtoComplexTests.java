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
package io.datarouter.bytes.binarydto;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.binarydto.dto.BinaryDto;

/**
 * Try to break the codecs in every way.
 */
public class BinaryDtoComplexTests{

	public static class ByteDto extends BinaryDto<ByteDto>{
		public final byte f1;
		public final Byte f2;
		public final byte[] f3;
		public final List<Byte> f4;

		public ByteDto(byte f1, Byte f2, byte[] f3, List<Byte> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class BooleanDto extends BinaryDto<BooleanDto>{
		public final boolean f1;
		public final Boolean f2;
		public final boolean[] f3;
		public final List<Boolean> f4;

		public BooleanDto(boolean f1, Boolean f2, boolean[] f3, List<Boolean> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class ShortDto extends BinaryDto<ShortDto>{
		public final short f1;
		public final Short f2;
		public final short[] f3;
		public final List<Short> f4;

		public ShortDto(short f1, Short f2, short[] f3, List<Short> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class CharDto extends BinaryDto<CharDto>{
		public final char f1;
		public final Character f2;
		public final char[] f3;
		public final List<Character> f4;

		public CharDto(char f1, Character f2, char[] f3, List<Character> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class IntDto extends BinaryDto<IntDto>{
		public final int f1;
		public final Integer f2;
		public final int[] f3;
		public final List<Integer> f4;

		public IntDto(int f1, Integer f2, int[] f3, List<Integer> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class FloatDto extends BinaryDto<FloatDto>{
		public final float f1;
		public final Float f2;
		public final float[] f3;
		public final List<Float> f4;

		public FloatDto(float f1, Float f2, float[] f3, List<Float> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class LongDto extends BinaryDto<LongDto>{
		public final long f1;
		public final Long f2;
		public final long[] f3;
		public final List<Long> f4;

		public LongDto(long f1, Long f2, long[] f3, List<Long> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class DoubleDto extends BinaryDto<DoubleDto>{
		public final double f1;
		public final Double f2;
		public final double[] f3;
		public final List<Double> f4;

		public DoubleDto(double f1, Double f2, double[] f3, List<Double> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class StringDto extends BinaryDto<StringDto>{
		public final String f1;
		public final String f2;
		public final String[] f3;
		public final List<String> f4;

		public StringDto(String f1, String f2, String[] f3, List<String> f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	public static class RootDto extends BinaryDto<RootDto>{
		public final ByteDto bytes;
		public final BooleanDto booleans;
		public final ShortDto shorts;
		public final CharDto chars;
		public final IntDto ints;
		public final FloatDto floats;
		public final LongDto longs;
		public final DoubleDto doubles;
		public final StringDto strings;

		public RootDto(
				ByteDto bytes,
				BooleanDto booleans,
				ShortDto shorts,
				CharDto chars,
				IntDto ints,
				FloatDto floats,
				LongDto longs,
				DoubleDto doubles,
				StringDto strings){
			this.bytes = bytes;
			this.booleans = booleans;
			this.shorts = shorts;
			this.chars = chars;
			this.ints = ints;
			this.floats = floats;
			this.longs = longs;
			this.doubles = doubles;
			this.strings = strings;
		}
	}


	@Test
	public void testCodec(){
		var dto = new RootDto(
				new ByteDto((byte)9, (byte)9, new byte[]{1, 3, 5}, Arrays.asList((byte)2, null, (byte)4)),
				new BooleanDto(true, false, new boolean[]{false, true}, Arrays.asList(false, null, true)),
				new ShortDto((short)7, (short)7, new short[]{6, 12, 3}, Arrays.asList(null, (short)-9, null)),
				new CharDto('z', 'y', new char[]{'x', 'w', 'v'}, Arrays.asList('-', null, '7')),
				new IntDto(8, 5, new int[]{4, 4}, Arrays.asList(null, -99_999, 0)),
				new FloatDto(2f, 3f, new float[]{4, 5}, Arrays.asList(Float.MIN_VALUE, Float.MAX_VALUE, null)),
				new LongDto(3L, 1L, new long[]{9, 6}, Arrays.asList(-1L, null, null, Long.MIN_VALUE)),
				new DoubleDto(6d, 2d, new double[]{18, 5}, Arrays.asList(null, Double.MIN_NORMAL, -.01)),
				new StringDto("asdf", null, new String[]{"a", "b", null}, Arrays.asList("c", null, "d")));
		Assert.assertEquals(dto.cloneIndexed(), dto);
	}

}
