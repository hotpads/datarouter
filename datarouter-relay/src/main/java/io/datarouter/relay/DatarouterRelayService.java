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
package io.datarouter.relay;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.instrumentation.relay.dto.RelayMessageResponseDto;
import io.datarouter.instrumentation.relay.dto.RelayOldEmailMessageRequestDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterRelayService{

	@Inject
	private DatarouterRelaySender sender;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;

	@Deprecated
	public RelayMessageResponseDto send(RelayOldEmailMessageRequestDto oldEmailRequest){
		var email = new J2HtmlDatarouterEmailBuilder()
				.withSubject(oldEmailRequest.request().subject())
				.withContent(DatarouterRelayJ2HtmlRenderTool.render(oldEmailRequest.request().content()))
				.from(oldEmailRequest.fromEmail())
				.to(oldEmailRequest.toEmails());

		htmlEmailService.trySendJ2Html(email);

		return sender.startThread(oldEmailRequest.request());
	}

}
