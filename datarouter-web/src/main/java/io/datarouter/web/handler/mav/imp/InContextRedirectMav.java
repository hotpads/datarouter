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
package io.datarouter.web.handler.mav.imp;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.pathnode.PathNode;

public class InContextRedirectMav extends GlobalRedirectMav{

	public InContextRedirectMav(String url, boolean shouldAppendModelQueryParams){
		super(url, shouldAppendModelQueryParams);
	}

	public InContextRedirectMav(HttpServletRequest request, String inContextUrl){
		super(request.getContextPath() + inContextUrl);
	}

	public InContextRedirectMav(HttpServletRequest request, PathNode pathNode){
		super(request.getContextPath() + pathNode.toSlashedString());
	}

	public InContextRedirectMav(HttpServletRequest request, String inContextUrl, Map<String, Object> model){
		super(request.getContextPath() + inContextUrl, model);
	}

	public InContextRedirectMav(HttpServletRequest request, PathNode pathNode, Map<String,Object> model){
		super(request.getContextPath() + pathNode.toSlashedString(), model);
	}

}
