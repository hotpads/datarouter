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
package io.datarouter.httpclient.client;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType;
import io.datarouter.pathnode.PathNode;

public class LinkClientTests{

	public static class LinkClientMock<
			T extends LinkType>
	implements LinkClient<T>{

		@Override
		public String toUrl(BaseLink<T> link){
			return "https://test.hotpads.com/context" + link.pathNode.toSlashedString() + "?test1=abc%204test2=def";
		}

		@Override
		public void shutdown(){
		}

		@Override
		public void initUrlPrefix(BaseLink<T> link){

		}

	}

	public static class MockLink<T extends LinkType> extends BaseLink<T>{
		public MockLink(){
			super(new PathNode().variable("app").variable("path"));
		}
	}

	@Test
	public void testToUrl(){
		LinkClient<?> linkClient = new LinkClientMock<>();
		String url = linkClient.toUrl(new MockLink<>());
		Assert.assertEquals(url, "https://test.hotpads.com/context/app/path?test1=abc%204test2=def");
	}

	@Test
	public void testToInternalUrl(){
		LinkClient<?> linkClient = new LinkClientMock<>();
		String url = linkClient.toInternalUrl(new MockLink<>());
		Assert.assertEquals(url, "/context/app/path?test1=abc%204test2=def");
	}

	@Test
	public void testToInternalUrlWithoutContext(){
		LinkClient<?> linkClient = new LinkClientMock<>();
		String url = linkClient.toInternalUrlWithoutContext(new MockLink<>());
		Assert.assertEquals(url, "/app/path?test1=abc%204test2=def");
	}

}
