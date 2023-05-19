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
package io.datarouter.plugin.dataexport.service;

import static j2html.TagCreator.body;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.DivTag;

@Singleton
public class DatabeanExportEmailService{

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DatarouterDataExportPaths paths;

	public void trySendExportCompleteEmail(
			String toEmail,
			DivTag commonContent){
		DivTag header = standardDatarouterEmailHeaderService.makeStandardHeader();
		BodyTag body = body(header, commonContent);
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.dataExport.exportDatabeans.singleTable)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Databean Export Complete")
				.withTitleHref(primaryHref)
				.withContent(body)
				.fromAdmin()
				.to(toEmail);
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

}
