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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ToStringToolTests{

	@Test
	public void testNull(){
		Assert.assertNull(ToStringTool.toString(null));
		// Leave a truly null string as a true null.  Let callers replace it if appropriate for the specific case.
		Assert.assertNotEquals(ToStringTool.toString(null), "null");
	}

	@Test
	public void testNullString(){
		String nullString = "null";
		Assert.assertEquals(ToStringTool.toString(nullString), "null");
	}

	@Test
	public void testBooleanArray(){
		boolean[] booleans = {false, true};
		String booleansExpected = "[false, true]";
		Assert.assertEquals(ToStringTool.toString(booleans), booleansExpected);
	}

	@Test
	public void testEmptyIntArray(){
		int[] ints = {};
		String intsExpected = "[]";
		Assert.assertEquals(ToStringTool.toString(ints), intsExpected);
	}

	@Test
	public void testIntArray(){
		int[] ints = {-3, 2};
		String intsExpected = "[-3, 2]";
		Assert.assertEquals(ToStringTool.toString(ints), intsExpected);
	}

	@Test
	public void testFloatArray(){
		float[] floats = {-3.56f, 2.000f};
		String floatsExpected = "[-3.56, 2.0]";
		Assert.assertEquals(ToStringTool.toString(floats), floatsExpected);
	}

	@Test
	public void testObjectArray(){
		LocalDate[] localDates = {LocalDate.of(2000, 1, 1), LocalDate.of(2022, 4, 1)};
		String localDatesExpected = "[2000-01-01, 2022-04-01]";
		Assert.assertEquals(ToStringTool.toString(localDates), localDatesExpected);
	}

	@Test
	public void testEmptyCollectionObject(){
		Collection<Integer> object = List.of();
		String objectExpected = "[]";
		Assert.assertEquals(ToStringTool.toString(object), objectExpected);
	}

	@Test
	public void testCollectionObject(){
		Collection<Integer> object = List.of(1, 2);
		String objectExpected = "[1, 2]";
		Assert.assertEquals(ToStringTool.toString(object), objectExpected);
	}

	@Test
	public void testStringObject(){
		String object = "hi";
		String objectExpected = "hi";
		Assert.assertEquals(ToStringTool.toString(object), objectExpected);
	}

}
