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
package io.datarouter.web.user.authenticate.saml;

import java.util.List;

import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.user.authenticate.saml.CustomSamlConfigParamsSupplier.CustomSamlConfigParam;

public class CustomSamlConfigPlugin extends BaseWebPlugin{

	private final List<CustomSamlConfigParam> params;

	public CustomSamlConfigPlugin(List<CustomSamlConfigParam> params){
		this.params = params;
	}

	@Override
	protected void configure(){
		bind(CustomSamlConfigParamsSupplier.class).toInstance(() -> params);
		bind(SamlConfigService.class).to(CustomSamlConfigService.class);
	}

}
