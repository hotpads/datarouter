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
package io.datarouter.autoconfig.config;

import io.datarouter.autoconfig.web.DatarouterAutoConfigHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.encoder.RawStringEncoder;
import io.datarouter.web.handler.types.DefaultDecoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAutoConfigRawStringRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterAutoConfigRawStringRouteSet(DatarouterAutoConfigPaths paths){
		handle(paths.datarouter.autoConfig).withHandler(DatarouterAutoConfigHandler.class);
		handle(paths.datarouter.autoConfigs.runForName).withHandler(DatarouterAutoConfigHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.withTag(Tag.DATAROUTER)
				.allowAnonymous()
				.withDefaultHandlerEncoder(RawStringEncoder.class)
				.withDefaultHandlerDecoder(DefaultDecoder.class);
	}

}
