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
package io.datarouter.util.lang;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassToolTests{

	@Test
	public void testIsEquivalentBoxedType(){
		Assert.assertTrue(ClassTool.isEquivalentBoxedType(int.class, int.class));
		Assert.assertTrue(ClassTool.isEquivalentBoxedType(int.class, Integer.class));
		Assert.assertTrue(ClassTool.isEquivalentBoxedType(Integer.class, int.class));
		Assert.assertTrue(ClassTool.isEquivalentBoxedType(Integer.class, Integer.class));

		Assert.assertFalse(ClassTool.isEquivalentBoxedType(long.class, int.class));
		Assert.assertFalse(ClassTool.isEquivalentBoxedType(long.class, Integer.class));
		Assert.assertFalse(ClassTool.isEquivalentBoxedType(String.class, String.class));// not primitive
	}

	static class A{
	}
	static class B extends A{
	}
	static class C extends B{
	}
	static class D extends A{
	}

	@Test
	public void testCastIfPossible(){
		Class<? extends A> candidate;
		Optional<Class<? extends B>> result;

		candidate = C.class;
		result = ClassTool.castIfPossible(B.class, candidate);
		Assert.assertTrue(result.isPresent());

		candidate = D.class;
		result = ClassTool.castIfPossible(B.class, candidate);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void testGetBoxedFromPrimitive(){
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(boolean.class), Boolean.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(byte.class), Byte.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(char.class), Character.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(double.class), Double.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(float.class), Float.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(int.class), Integer.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(long.class), Long.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(short.class), Short.class);
		Assert.assertEquals(ClassTool.getBoxedFromPrimitive(void.class), Void.class);

		Assert.assertNull(ClassTool.getBoxedFromPrimitive(Long.class));
		Assert.assertNull(ClassTool.getBoxedFromPrimitive(Optional.class));
	}

}
