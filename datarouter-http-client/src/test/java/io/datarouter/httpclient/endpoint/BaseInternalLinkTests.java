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
package io.datarouter.httpclient.endpoint;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.endpoint.UrlLinkRoot.DefaultUrlLinkRoot;
import io.datarouter.pathnode.PathNode;

public class BaseInternalLinkTests{

	public static class ExampleUrlLinkRoot extends DefaultUrlLinkRoot{
		public ExampleUrlLinkRoot(){
			super("localhost:8443", "example");
		}
	}

	public static class ExampleInternalLink1 extends BaseInternalLink{

		public static final String TEST = "test";

		public final String name;
		public final int age;
		public final boolean isTall;
		public final Optional<String> abc;

		public ExampleInternalLink1(String name, int age, boolean isTall, Optional<String> abc){
			super(new ExampleUrlLinkRoot(), new PathNode().leaf("/api/v1//test"));
			this.name = name;
			this.age = age;
			this.isTall = isTall;
			this.abc = abc;
		}

	}

	@Test
	public void testGetParamsAsString(){
		ExampleInternalLink1 link1 = new ExampleInternalLink1("Jack", 20, false, Optional.of("abc"));
		ExampleInternalLink1 link2 = new ExampleInternalLink1("Jill", 20, true, Optional.empty());

		Assert.assertEquals(link1.getParamsAsString(), "?name=Jack&age=20&isTall=false&abc=abc");
		Assert.assertEquals(link2.getParamsAsString(), "?name=Jill&age=20&isTall=true");
	}

}
