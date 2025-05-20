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
package io.datarouter.email.link;

import io.datarouter.email.config.DatarouterEmailSettingsProvider;
import io.datarouter.email.email.DatarouterEmailLinkBuilder;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.instrumentation.web.ContextName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterEmailLinkClient{

	@Inject
	private DatarouterEmailSettingsProvider emailSettingsProvider;
	@Inject
	private ContextName contextName;

	public String toUrl(DatarouterLink link){
		var builder = new DatarouterEmailLinkBuilder()
				.withProtocol(UrlScheme.HTTPS.getStringRepresentation())
				.withHostPort(emailSettingsProvider.get().emailLinkHostPort.get())
				.withContextPath(contextName.getContextPath())
				.withLocalPath(link.pathNode.toSlashedString());
		LinkTool.getParamFields(link).forEach(builder::withParam);
		return builder.build();
	}

}
