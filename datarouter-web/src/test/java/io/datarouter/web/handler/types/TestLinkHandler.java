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
package io.datarouter.web.handler.types;

import java.util.Optional;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType.NoOpLinkType;
import io.datarouter.pathnode.PathNode;
import io.datarouter.web.handler.BaseHandler.Handler;

public class TestLinkHandler{

	public static class LinkTest extends BaseLink<NoOpLinkType>{
		public final Integer requiredNum;
		public Optional<Boolean> isTest;
		public Optional<Long> optionalNum;

		public LinkTest(Integer requiredNum){
			super(new PathNode().variable("linkTest"));
			this.requiredNum = requiredNum;
		}
	}

	@Handler
	public LinkTestDto linkTest(LinkTest endpoint){
		return new LinkTestDto(endpoint.requiredNum, endpoint.isTest, endpoint.optionalNum);
	}

	public record LinkTestDto(
			Integer requiredNum,
			Optional<Boolean> isTest,
			Optional<Long> num){
	}
}
