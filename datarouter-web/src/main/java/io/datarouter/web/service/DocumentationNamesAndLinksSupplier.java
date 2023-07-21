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
package io.datarouter.web.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier.DocDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DocumentationNamesAndLinksSupplier implements Supplier<List<DocDto>>{

	@Inject
	private PluginInjector pluginInjector;

	@Override
	public List<DocDto> get(){
		return pluginInjector.getInstances(DocDto.KEY);
	}

	public Map<String,String> getSystemDocs(){
		return Scanner.of(get())
				.include(dto -> dto.type == DocType.SYSTEM_DOCS)
				.sort(Comparator.comparing(dto -> dto.name))
				.toMap(dto -> dto.name, dto -> dto.link);
	}

	public Map<String,String> getReadmeDocs(){
		return Scanner.of(get())
				.include(dto -> dto.type == DocType.README)
				.sort(Comparator.comparing(dto -> dto.name))
				.toMap(dto -> dto.name, dto -> dto.link);
	}

	public enum DocType{
		README,
		SYSTEM_DOCS,
		;
	}

	public static class DocDto implements PluginConfigValue<DocDto>{

		public static final PluginConfigKey<DocDto> KEY = new PluginConfigKey<>(
				"docDtos",
				PluginConfigType.INSTANCE_LIST);

		public final String name;
		public final String link;
		public final DocType type;

		public DocDto(String name, String link, DocType type){
			this.name = name;
			this.link = link;
			this.type = type;
		}

		@Override
		public PluginConfigKey<DocDto> getKey(){
			return KEY;
		}

	}

}
