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
package io.datarouter.email.email;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.config.DatarouterEmailSettingsProvider;
import io.datarouter.email.html.J2HtmlDatarouterEmail;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.string.StringTool;
import j2html.tags.ContainerTag;

@Singleton
public class DatarouterHtmlEmailService{
	private static Logger logger = LoggerFactory.getLogger(DatarouterHtmlEmailService.class);

	@Inject
	private DatarouterEmailFiles files;
	@Inject
	private DatarouterEmailPaths paths;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private DatarouterEmailSettingsProvider datarouterEmailSettingsProvider;

	public void trySend(String fromEmail, String toEmail, String subject, String body){
		datarouterEmailService.trySend(fromEmail, toEmail, subject, body, true);
	}

	public void trySendJ2Html(
			String fromEmail,
			String toEmail,
			J2HtmlDatarouterEmailBuilder emailBuilder){
		J2HtmlDatarouterEmail email = emailBuilder.build();
		ContainerTag body = email.build();
		String bodyString = body.render();
		trySend(fromEmail, toEmail, emailBuilder.getSubject(), bodyString);
	}

	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return datarouterEmailService.startLinkBuilder();
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilder(){
		boolean includeLogo = datarouterEmailSettingsProvider.get().includeLogo.get();
		var emailBuilder = new J2HtmlDatarouterEmailBuilder()
				.withWebappName(datarouterService.getServiceName())
				.withEnvironment(datarouterProperties.getEnvironment())
				.withIncludeLogo(includeLogo);
		if(includeLogo){
			String logoHref = datarouterEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter)
					.build();
			emailBuilder
					.withLogoImgSrc(getEmailLogoHref())
					.withLogoHref(logoHref);
			logger.warn("building email for service={}, environment={}, includelogo={}, logoHref={}",
					datarouterService.getServiceName(), datarouterProperties.getEnvironment(), includeLogo, logoHref);
		}
		return emailBuilder;
	}

	private String getEmailLogoHref(){
		String configuredHref = datarouterEmailSettingsProvider.get().logoImgSrc.get();
		if(StringTool.notEmpty(configuredHref)){
			return configuredHref;
		}
		return datarouterEmailService.startLinkBuilder()
				.withLocalPath(files.jeeAssets.datarouterLogoPng)
				.build();
	}

}
