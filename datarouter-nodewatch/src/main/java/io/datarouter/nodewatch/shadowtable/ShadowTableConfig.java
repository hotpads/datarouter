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
package io.datarouter.nodewatch.shadowtable;

import java.util.Comparator;
import java.util.List;

import io.datarouter.scanner.Scanner;

public interface ShadowTableConfig{

	List<ShadowTableExport> exports();

	default int numExports(){
		return exports().size();
	}

	default ShadowTableExport exportWithIndex(int id){
		return Scanner.of(exports())
				.sort(Comparator.comparing(ShadowTableExport::clientName))
				.skip(id)
				.findFirst()
				.orElseThrow();
	}


	public static class GenericShadowTableConfig implements ShadowTableConfig{

		private final List<ShadowTableExport> exports;

		public GenericShadowTableConfig(List<ShadowTableExport> exports){
			this.exports = exports;
		}

		@Override
		public List<ShadowTableExport> exports(){
			return exports;
		}
	}


}
