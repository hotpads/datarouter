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
package io.datarouter.web.html.react.bootstrap3;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.handler.mav.MavPropertiesFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class Bootstrap3ReactPageFactory{

	@Inject
	private MavPropertiesFactory mavPropertiesFactory;

	public Bootstrap3ReactPage startBuilder(HttpServletRequest request){
		return new Bootstrap3ReactPage()
				.withMavProperties(mavPropertiesFactory.buildAndSet(request))
				.withJsStringConstant("CONTEXT_PATH", request.getContextPath());
	}

}
