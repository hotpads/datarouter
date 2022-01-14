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

import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;

public class BinaryDtoObjectTests{

	public static class TestDto extends BaseBinaryDto{
		public final Integer crab;
		public final String bear;
		public final short[] duck;

		public TestDto(Integer crab, String bear, short[] duck){
			this.crab = crab;
			this.bear = bear;
			this.duck = duck;
		}
	}

	@Test
	public void testScanFieldNames(){
		List<String> expected = Java9.listOf("bear", "crab", "duck");
		List<String> actual = new TestDto(null, null, null).scanFieldNames().list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testScanFieldValues(){
		List<Object> expected = Java9.listOf("x", 5, new short[]{1, 2});
		List<Object> actual = new TestDto(5, "x", new short[]{1, 2}).scanFieldValues().list();
		Assert.assertEquals(actual.get(0), expected.get(0));
		Assert.assertEquals(actual.get(1), expected.get(1));
		Assert.assertEquals(actual.get(2), expected.get(2));
	}

	@Test
	public void testEquals(){
		TestDto d1 = new TestDto(2, "asdf", new short[]{1, 2});
		TestDto d2 = new TestDto(2, "asdf", new short[]{1, 2});
		Assert.assertEquals(d1, d2);
		Assert.assertNotSame(d1, d2);
		TestDto d3 = new TestDto(3, "v", new short[]{3, 4});
		Assert.assertNotEquals(d1, d3);
	}

	@Test
	public void testHashCode(){
		int expected = Arrays.deepHashCode(new Object[]{"a", 1, new short[]{1, 2}});
		int actual = new TestDto(1, "a", new short[]{1, 2}).hashCode();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testToString(){
		String expected = "TestDto [bear=asdf, crab=2, duck=[7, 8]]";
		String actual = new TestDto(2, "asdf", new short[]{7, 8}).toString();
		Assert.assertEquals(actual, expected);
	}

}
