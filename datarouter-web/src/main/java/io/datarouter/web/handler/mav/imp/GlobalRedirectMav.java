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
package io.datarouter.web.handler.mav.imp;

import java.util.Map;

import io.datarouter.web.handler.mav.Mav;

public class GlobalRedirectMav extends Mav{

	public GlobalRedirectMav(String url, boolean shouldAppendModelQueryParams){
		setGlobalRedirectUrl(url, shouldAppendModelQueryParams);
	}

	public GlobalRedirectMav(String url, Map<String,Object> model){
		this(url, true);
		putAll(model);
	}

	public GlobalRedirectMav(String url){
		this(url, false);
	}

}
