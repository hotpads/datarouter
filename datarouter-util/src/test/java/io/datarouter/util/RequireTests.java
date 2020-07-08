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
package io.datarouter.util;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RequireTests{

	@Test
	public void testIsNull(){
		Require.isNull(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIsNullThrowsException(){
		Require.isNull("");
	}

	@Test
	public void testEquals(){
		Require.equals("apple", "apple");
		Require.equals("apple", "apple", "failure");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEqualsThrowsException(){
		Require.equals("apple", "orange");
	}

	@Test
	public void testEqualsExceptionMessages(){
		try{
			Require.equals("apple", "orange");
		}catch(IllegalArgumentException e){
			Assert.assertEquals(e.getMessage(), "apple does not equal orange");
		}
		try{
			Require.equals("apple", "orange", "message");
		}catch(IllegalArgumentException e){
			Assert.assertEquals(e.getMessage(), "message");
		}
	}

	@Test
	public void testNotEquals1(){
		Require.notEquals("apple", "orange");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNotEqualsThrowsException(){
		Require.notEquals("apple", "apple");
	}

	@Test
	public void testIsTrue(){
		Require.isTrue(true);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIsTrueThrowsException(){
		Require.isTrue(false);
	}

	@Test
	public void testGreaterThan(){
		int validated = Require.greaterThan(3, 1);
		Assert.assertEquals(validated, 3);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGreaterThanThrowsException(){
		Require.greaterThan(5, 5);
	}

	@Test
	public void testLessThan(){
		int validated = Require.lessThan(5, 7);
		Assert.assertEquals(validated, 5);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testLessThanThrowsException(){
		Require.lessThan(5, 5);
	}

	@Test
	public void testContains(){
		Require.contains(List.of("a", "b"), "a");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testContainsThrowsException(){
		Require.contains(List.of("a", "b"), "c");
	}

	@Test
	public void testNotContains(){
		Require.notContains(List.of("a", "b"), "c");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNotContainsThrowsException(){
		Require.notContains(List.of("a", "b"), "a");
	}

	@Test
	public void testNotEmpty(){
		Require.notEmpty(List.of("a", "b"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNotEmptyThrowsException(){
		Require.notEmpty(List.of(), "empty list");
	}

}
