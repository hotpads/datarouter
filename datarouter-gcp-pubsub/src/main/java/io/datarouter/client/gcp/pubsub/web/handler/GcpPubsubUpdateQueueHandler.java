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
package io.datarouter.client.gcp.pubsub.web.handler;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.i;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class GcpPubsubUpdateQueueHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(GcpPubsubUpdateQueueHandler.class);

	public static final String PARAM_clientName = "clientName";
	public static final String PARAM_queueName = "queueName";
	public static final String PARAM_referer = "referer";

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private GcpPubsubClientManager gcpPubsubClientManager;

	@Handler
	private Mav purgeQueue(
			@Param(PARAM_clientName) String clientName,
			@Param(PARAM_queueName) String queueName,
			@Param(PARAM_referer) String referer){
		ClientId clientId = datarouterClients.getClientId(clientName);
		var timer = new PhaseTimer();
		gcpPubsubClientManager.seek(clientId, queueName);
		timer.add("seekSubscription");
		logger.warn("{}", timer);
		String message = "Purged pubsub queue: " + queueName;
		return buildPage(referer, message);
	}

	private Mav buildPage(String href, String message){
		DivTag backButton = div(a(i().withClass("fas fa-angle-left"))
				.withText("Go back to client details")
				.withHref(href)
				.withClass("btn btn-primary"));

		DivTag content = div(
				backButton,
				div(message).withClass("my-4"))
				.withClass("container my-4");

		return pageFactory.startBuilder(request)
				.withContent(content)
				.withTitle("Updated Pubsub Queue")
				.buildMav();
	}

}
