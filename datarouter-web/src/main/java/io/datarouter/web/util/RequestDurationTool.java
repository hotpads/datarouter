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
package io.datarouter.web.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler;

public class RequestDurationTool{

	private static Optional<Long> getRequestElapsedMs(HttpServletRequest request){
		return RequestAttributeTool.get(request, BaseHandler.REQUEST_RECEIVED_AT)
				.map(receivedAt -> System.currentTimeMillis() - receivedAt.getTime());
	}

	public static Optional<String> getRequestElapsedDurationString(HttpServletRequest request){
		return getRequestElapsedMs(request)
				.map(durationMs -> new DatarouterDuration(durationMs, TimeUnit.MILLISECONDS))
				.map(DatarouterDuration::toString);
	}

}
