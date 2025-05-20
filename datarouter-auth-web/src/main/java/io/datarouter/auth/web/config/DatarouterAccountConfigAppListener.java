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
package io.datarouter.auth.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.web.service.DatarouterAccountConfigService;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.listener.DatarouterAppListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountConfigAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountConfigAppListener.class);

	@Inject
	private DatarouterAccountConfigService service;

	@Override
	public void onStartUp(){
		Scanner.of(service.createDefaultAccountRecords())
				.forEach(logger::warn);
	}

}
