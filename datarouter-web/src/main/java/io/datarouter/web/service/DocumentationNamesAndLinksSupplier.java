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
package io.datarouter.web.service;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;

public interface DocumentationNamesAndLinksSupplier extends Supplier<Map<String,Pair<String,Boolean>>>{

	 default Map<String,String> getSystemDocs(){
		 return Scanner.of(get().entrySet())
				.include(entry -> entry.getValue().getRight())
				.toMap(Entry::getKey, entry -> entry.getValue().getLeft());
	 }

	 default Map<String,String> getReadmeDocs(){
		 return Scanner.of(get().entrySet())
				.exclude(entry -> entry.getValue().getRight())
				.toMap(Entry::getKey, entry -> entry.getValue().getLeft());
	 }

	@Singleton
	class NoOpDocumentationNamesAndLinks implements DocumentationNamesAndLinksSupplier{

		@Override
		public Map<String,Pair<String,Boolean>> get(){
			return Collections.emptyMap();
		}

	}

	@Singleton
	class DefaultDocumentationNamesAndLinks implements DocumentationNamesAndLinksSupplier{

		private final Map<String,Pair<String,Boolean>> documentationNamesAndLinks;

		public DefaultDocumentationNamesAndLinks(Map<String,Pair<String,Boolean>> documentationNamesAndLinks){
			this.documentationNamesAndLinks = documentationNamesAndLinks;
		}

		@Override
		public Map<String,Pair<String,Boolean>> get(){
			return documentationNamesAndLinks;
		}

	}

}
