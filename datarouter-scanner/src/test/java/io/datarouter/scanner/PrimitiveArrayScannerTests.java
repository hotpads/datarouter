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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveArrayScannerTests{

	@Test
	public void testNull(){
		float[] nullArray = null;// Needs a specific type of array to avoid method ambiguity.
		Assert.assertThrows(NullPointerException.class, () -> Scanner.ofArray(nullArray).list());
	}

	@Test
	public void testEmpty(){
		Assert.assertEquals(Scanner.ofArray(new boolean[]{}).list(), List.of());
	}

	@Test
	public void testValues(){
		Assert.assertEquals(Scanner.ofArray(new boolean[]{false, true}).list(), List.of(false, true));
		Assert.assertEquals(Scanner.ofArray(new byte[]{-1, 0, 1}).list(), List.of((byte)-1, (byte)0, (byte)1));
		Assert.assertEquals(Scanner.ofArray(new char[]{'a', 'b'}).list(), List.of('a', 'b'));
		Assert.assertEquals(Scanner.ofArray(new short[]{-1, 0, 1}).list(), List.of((short)-1, (short)0, (short)1));
		Assert.assertEquals(Scanner.ofArray(new int[]{-1, 0, 1}).list(), List.of(-1, 0, 1));
		Assert.assertEquals(Scanner.ofArray(new float[]{-.1F, 0F, .1F}).list(), List.of(-.1F, 0F, .1F));
		Assert.assertEquals(Scanner.ofArray(new long[]{-1, 0, 1}).list(), List.of(-1L, 0L, 1L));
		Assert.assertEquals(Scanner.ofArray(new double[]{-.1D, 0D, .1D}).list(), List.of(-.1D, 0D, .1D));
	}

}
