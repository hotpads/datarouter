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
package io.datarouter.inject;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

public class InjectionToolTests{

	private static class Child extends Parent{

		@SuppressWarnings("unused") // here for reflection test
		@Inject
		Integer anInteger;

		@Inject
		Child(@SuppressWarnings("unused") String aString){
		}

		@Inject
		void setLong(@SuppressWarnings("unused") Long aLong){
		}

	}

	private static class Parent{

		@SuppressWarnings("unused") // here for reflection test
		@Inject
		Float aFloat;

		@Inject
		void setDouble(@SuppressWarnings("unused") Double aDouble){
		}

	}

	@Test
	public void testFindInjectableClasses(){
		List<Class<?>> actual = InjectionTool.findInjectableClasses(Child.class).list();
		Assert.assertEquals(actual, List.of(
				String.class,
				Integer.class,
				Float.class,
				Long.class,
				Double.class));
	}

}
