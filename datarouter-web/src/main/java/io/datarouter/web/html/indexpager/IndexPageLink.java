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
package io.datarouter.web.html.indexpager;

import org.apache.http.client.utils.URIBuilder;

public class IndexPageLink{

	public final String text;
	public final String href;

	public IndexPageLink(
			String path,
			IndexPagerParamNames paramNames,
			IndexPagerParams params,
			String text,
			long linkPage){
		this.text = text;
		this.href = makeHref(path, paramNames, params, linkPage);
	}

	private static String makeHref(
			String path,
			IndexPagerParamNames paramNames,
			IndexPagerParams params,
			long page){
		var uriBuilder = new URIBuilder()
				.setPath(path)
				.addParameter(paramNames.paramSort, params.sort)
				.addParameter(paramNames.paramPageSize, params.pageSize + "")
				.addParameter(paramNames.paramPage, page + "");
		params.retainedParams.forEach(retainedParam -> uriBuilder.addParameter(
				retainedParam.name(),
				retainedParam.value()));
		return uriBuilder.toString();
	}
}
