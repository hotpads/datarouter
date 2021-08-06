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
package io.datarouter.filesystem.snapshot.block;

public class BlockSizeCalculator{

	private static final int WORD_WIDTH = 8;

	private int size;

	public static int pad(int inputSize){
		int overflow = inputSize % WORD_WIDTH;
		int padding = WORD_WIDTH - overflow;
		return inputSize + padding;
	}

	public int calculate(){
		return pad(size);
	}

	public BlockSizeCalculator addObjectHeaders(int num){
		size += num * 16;
		return this;
	}

	public BlockSizeCalculator addRefs(int num){
		size += num * 8;
		return this;
	}

	public BlockSizeCalculator addArrays(int num){
		size += num * 24;
		return this;
	}

	public BlockSizeCalculator addLongs(int num){
		size += num * 8;
		return this;
	}

	public BlockSizeCalculator addDoubles(int num){
		size += num * 8;
		return this;
	}

	public BlockSizeCalculator addInts(int num){
		size += num * 4;
		return this;
	}

	public BlockSizeCalculator addFloats(int num){
		size += num * 4;
		return this;
	}

	public BlockSizeCalculator addChars(int num){
		size += num * 2;
		return this;
	}

	public BlockSizeCalculator addShorts(int num){
		size += num * 2;
		return this;
	}

	public BlockSizeCalculator addBytes(int num){
		size += num;
		return this;
	}

	public BlockSizeCalculator addBooleans(int num){
		size += num;
		return this;
	}

	// excludes the reference to the byte[]
	public BlockSizeCalculator addByteArrayValue(byte[] array){
		addArrays(1);
		addBytes(pad(array.length));
		return this;
	}

	// excludes the reference to the int[]
	public BlockSizeCalculator addIntArrayValue(int[] array){
		addArrays(1);
		addBytes(pad(4 * array.length));
		return this;
	}

	// excludes the reference to the String
	public BlockSizeCalculator addStringValue(String string){
		addObjectHeaders(1);// the String Object
		addArrays(1);// String.value reference
		addBytes(pad(string.length()));// String.value body
		addBytes(1);// String.coder
		addInts(1);// String.hash
		addBooleans(1);// String.hashIsZero
		return this;
	}

}
