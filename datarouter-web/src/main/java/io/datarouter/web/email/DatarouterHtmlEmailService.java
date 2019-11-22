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
package io.datarouter.web.email;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.html.J2HtmlDatarouterEmail;
import io.datarouter.web.html.J2HtmlDatarouterEmailBuilder;
import j2html.tags.ContainerTag;

@Singleton
public class DatarouterHtmlEmailService{

	@Inject
	private DatarouterWebFiles files;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private DatarouterEmailService datarouterEmailService;

	public void trySend(String fromEmail, String toEmail, String subject, String body){
		datarouterEmailService.trySend(fromEmail, toEmail, subject, body, true);
	}

	public void trySendJ2Html(
			String fromEmail,
			String toEmail,
			String subject,
			J2HtmlDatarouterEmailBuilder emailBuilder){
		J2HtmlDatarouterEmail email = emailBuilder.build();
		ContainerTag body = email.build();
		String bodyString = body.renderFormatted();
		trySend(fromEmail, toEmail, subject, bodyString);
	}

	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return datarouterEmailService.startLinkBuilder();
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilder(){
		String logoImgSrc = datarouterEmailService.startLinkBuilder()
				.withLocalPath(files.jeeAssets.datarouterLogoPng)
				.build();
		String logoHref = datarouterEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter)
				.build();
		return new J2HtmlDatarouterEmailBuilder()
				.withLogoImgSrc(logoImgSrc)
				.withLogoHref(logoHref);
	}

}
