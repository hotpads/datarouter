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
package io.datarouter.scanner;

import java.util.function.Function;

public abstract class PrimitiveArrayScanner<T> implements Scanner<T>{

	private final int length;
	private final Function<Integer,T> valueExtractor;
	private int index = -1;

	public PrimitiveArrayScanner(int length, Function<Integer,T> valueExtractor){
		this.length = length;
		this.valueExtractor = valueExtractor;
	}

	@Override
	public boolean advance(){
		++index;
		return index < length;
	}

	@Override
	public T current(){
		return valueExtractor.apply(index);
	}

	/*-------- implementations ---------*/

	public static class BooleanArrayScanner extends PrimitiveArrayScanner<Boolean>{
		public BooleanArrayScanner(boolean[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class ByteArrayScanner extends PrimitiveArrayScanner<Byte>{
		public ByteArrayScanner(byte[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class CharacterArrayScanner extends PrimitiveArrayScanner<Character>{
		public CharacterArrayScanner(char[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class ShortArrayScanner extends PrimitiveArrayScanner<Short>{
		public ShortArrayScanner(short[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class IntegerArrayScanner extends PrimitiveArrayScanner<Integer>{
		public IntegerArrayScanner(int[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class FloatArrayScanner extends PrimitiveArrayScanner<Float>{
		public FloatArrayScanner(float[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class LongArrayScanner extends PrimitiveArrayScanner<Long>{
		public LongArrayScanner(long[] array){
			super(array.length, i -> array[i]);
		}
	}

	public static class DoubleArrayScanner extends PrimitiveArrayScanner<Double>{
		public DoubleArrayScanner(double[] array){
			super(array.length, i -> array[i]);
		}
	}

}
