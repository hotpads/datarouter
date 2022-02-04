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
package io.datarouter.autoconfig.web;

import javax.inject.Inject;

import io.datarouter.autoconfig.service.AutoConfigService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.RawStringEncoder;
import io.datarouter.web.handler.types.Param;

public class DatarouterAutoConfigHandler extends BaseHandler{

	public static final String P_name = "name";

	@Inject
	private AutoConfigService autoConfigService;

	@Handler(defaultHandler = true, encoder = RawStringEncoder.class)
	private String home(){
		return autoConfigService.runAutoConfigAll();
	}

	@Handler(encoder = RawStringEncoder.class)
	public String runForName(@Param(P_name) String name) throws Exception{
		return autoConfigService.runAutoConfigForName(name);
	}

}
