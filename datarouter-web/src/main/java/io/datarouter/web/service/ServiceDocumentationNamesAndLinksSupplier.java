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
import java.util.function.Supplier;

public interface ServiceDocumentationNamesAndLinksSupplier extends Supplier<Map<String,String>>{

	class NoOpServiceDocumentationNamesAndLinks implements ServiceDocumentationNamesAndLinksSupplier{

		@Override
		public Map<String,String> get(){
			return Collections.emptyMap();
		}

	}

	class DatarouterServiceDocumentationNamesAndLinks implements ServiceDocumentationNamesAndLinksSupplier{

		private final Map<String,String> serviceDocumentationNamesAndLinks;

		public DatarouterServiceDocumentationNamesAndLinks(Map<String,String> serviceDocumentationNamesAndLinks){
			this.serviceDocumentationNamesAndLinks = serviceDocumentationNamesAndLinks;
		}

		@Override
		public Map<String,String> get(){
			return serviceDocumentationNamesAndLinks;
		}

	}

}
