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
package io.datarouter.exception.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.config.DatarouterExceptionPaths;
import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.web.config.ServletContextSupplier;

@Singleton
public class ExceptionRecordService{

	@Inject
	private DatarouterExceptionPaths paths;
	@Inject
	private DatarouterExceptionSettingRoot settings;
	@Inject
	private ServletContextSupplier servletContext;

	public String buildExceptionLinkForCurrentServer(String exceptionRecordId){
		String domainAndContext = buildDomainAndContext();
		return buildExceptionLink(domainAndContext, exceptionRecordId);
	}

	public String buildExceptionLinkForCurrentServer(ExceptionRecord exceptionRecord){
		return buildExceptionLinkForCurrentServer(exceptionRecord.getKey().getId());
	}

	public String buildDomainAndContext(){
		return settings.exceptionRecorderDomainName.get() + servletContext.get().getContextPath();
	}

	public String buildExceptionLink(String domainAndContext, String exceptionRecordId){
		return "https://" + domainAndContext + paths.datarouter.exception.details.toSlashedString()
				+ "?exceptionRecord=" + exceptionRecordId;
	}

	public String buildExceptionFormAction(String domainAndContext){
		return "https://" + domainAndContext + paths.datarouter.exception.browse.toSlashedString();
	}

}
