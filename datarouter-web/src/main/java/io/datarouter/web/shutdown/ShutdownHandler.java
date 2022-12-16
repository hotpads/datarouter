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
package io.datarouter.web.shutdown;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.handler.BaseHandler;

public class ShutdownHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);

	@Inject
	private ShutdownService shutdownService;
	@Inject
	private DatarouterWebSettingRoot settingRoot;

	@Handler(defaultHandler = true)
	protected Integer shutdown(String secret){
		//prevent shutting down with empty secret param (even if that is the shutdownSecret's actual value)
		if(StringTool.isEmptyOrWhitespace(secret)){
			logger.error("please pass a secret string matching the value stored in Setting {}",
					settingRoot.shutdownSecret.getName());
			return null;
		}
		if(secret.equals(settingRoot.shutdownSecret.get())){
			return shutdownService.advance();
		}
		logger.error("invalid secret={}", secret);
		return null;
	}

}
