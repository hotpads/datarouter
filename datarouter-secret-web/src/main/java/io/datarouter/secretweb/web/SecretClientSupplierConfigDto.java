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
package io.datarouter.secretweb.web;

import java.util.List;
import java.util.Map;

import io.datarouter.secretweb.web.SecretHandlerOpRequestDto.SecretOpDto;

public class SecretClientSupplierConfigDto{

	public final String configName;
	public final String supplierClass;
	public final Map<SecretOpDto,SecretOpDto> allowedOps;//no Set in JS
	public final Map<String,String> allowedNames;//no Set in JS

	public SecretClientSupplierConfigDto(String configName, String supplierClass,
			Map<SecretOpDto,SecretOpDto> allowedOps, Map<String,String> allowedNames){
		this.configName = configName;
		this.supplierClass = supplierClass;
		this.allowedOps = allowedOps;
		this.allowedNames = allowedNames;
	}

	public static class SecretClientSupplierConfigsDto{

		public final List<String> configNames;
		public final Map<String,SecretClientSupplierConfigDto> configs;

		public SecretClientSupplierConfigsDto(List<String> configNames,
				Map<String,SecretClientSupplierConfigDto> configs){
			this.configNames = configNames;
			this.configs = configs;
		}

	}

}