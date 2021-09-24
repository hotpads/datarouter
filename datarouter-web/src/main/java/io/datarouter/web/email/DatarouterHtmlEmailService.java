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
package io.datarouter.web.email;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.config.DatarouterEmailSettingsProvider;
import io.datarouter.email.email.DatarouterEmailLinkBuilder;
import io.datarouter.email.email.IDatarouterEmailService;
import io.datarouter.email.html.EmailDto;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.web.config.service.ContextName;
import io.datarouter.web.config.service.ServiceName;

@Singleton
public class DatarouterHtmlEmailService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterEmailSettingsProvider datarouterEmailSettingsProvider;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterSubscribersSupplier subscribersEmail;
	@Deprecated // push this logic down to each email
	@Inject
	private DatarouterEmailSubscriberSettings subscriberSettings;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private ContextName contextName;
	@Inject
	private IDatarouterEmailService emailService;

	public void trySend(EmailDto email){
		emailService.trySend(
				email,
				adminEmail.get(),
				subscriberSettings.includeSubscribers.get(),
				subscribersEmail.get());
	}

	public void trySendJ2Html(J2HtmlDatarouterEmailBuilder emailBuilder){
		emailService.trySendJ2Html(
				emailBuilder,
				adminEmail.get(),
				subscriberSettings.includeSubscribers.get(),
				subscribersEmail.get());
	}

	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return emailService.startLinkBuilder(
				datarouterEmailSettingsProvider.get().emailLinkHostPort.get(),
				contextName.getContextPath());
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilder(){
		boolean includeLogo = datarouterEmailSettingsProvider.get().includeLogo.get();
		if(includeLogo){
			return emailService.startEmailBuilderWithLogo(
					datarouterEmailSettingsProvider.get().emailLinkHostPort.get(),
					contextName.getContextPath(),
					serviceName.get(),
					environmentName.get(),
					datarouterEmailSettingsProvider.get().logoImgSrc.get());
		}
		return emailService.startEmailBuilderWithOutLogo(
				serviceName.get(),
				environmentName.get());
	}

}
