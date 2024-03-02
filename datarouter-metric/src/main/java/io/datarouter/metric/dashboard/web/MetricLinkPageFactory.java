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
package io.datarouter.metric.dashboard.web;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageBuilder;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4SubnavHtml;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricLinkPageFactory extends Bootstrap4PageFactory{

	@Inject
	private MetricNamesSubnavFactory subnavFactory;

	@Override
	public Bootstrap4PageBuilder startBuilder(HttpServletRequest request){
		return super.startBuilder(request)
				.withNavbar(Bootstrap4SubnavHtml.render(subnavFactory.build(request.getContextPath())));
	}

}
