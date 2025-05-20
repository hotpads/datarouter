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
package io.datarouter.auth.web.util;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.util.string.StringTool;

public class DatarouterAuthPathUtil{

	// TODO better way to get this? Maybe it's this way due to direct server logins
	public static String getSignInUrl(HttpServletRequest request, DatarouterAuthPaths paths){
		String requestUrlWithoutContext = StringTool.getStringBeforeLastOccurrence(
				request.getRequestURI(),
				request.getRequestURL().toString());
		return requestUrlWithoutContext + request.getContextPath() + paths.signin.toSlashedString();
	}

}
