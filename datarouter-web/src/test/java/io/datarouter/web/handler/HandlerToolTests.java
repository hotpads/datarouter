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
package io.datarouter.web.handler;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HandlerToolTests{

	public static class TestDto1{

		public final String foo;
		public final Optional<String> bar;

		public TestDto1(String foo, Optional<String> bar){
			this.foo = foo;
			this.bar = bar;
		}
	}

	public static class TestDto2{

		public final String foo;
		public Optional<String> bar = Optional.empty();

		public TestDto2(String foo){
			this.foo = foo;
		}
	}

	@Test
	public void testValidateOptionalFields(){
		Assert.assertThrows(() -> HandlerTool.validateOptionalFields(TestDto1.class));
		HandlerTool.validateOptionalFields(TestDto2.class);
	}

}
