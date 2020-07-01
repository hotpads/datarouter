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
package io.datarouter.gcp.spanner.web;

import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.p;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.gcp.spanner.SpannerClientType;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;

public class SpannerWebInspector implements DatarouterClientWebInspector{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private ClientOptions clientOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, SpannerClientType.class);
		if(clientParams.getClientId() == null){
			return new MessageMav("Client not found");
		}

		String clientName = clientParams.getClientId().getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var clientOptionsTable = buildClientOptionsTable(allClientOptions);
		var content = div(
				h2("Datarouter " + clientName),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientName),
				h3("Client Summary"),
				p(b("Client Name: " + clientName)),
				clientOptionsTable)
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - GCP Spanner")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

}
