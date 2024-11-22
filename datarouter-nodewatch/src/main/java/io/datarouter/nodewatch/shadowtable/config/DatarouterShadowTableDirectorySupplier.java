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
package io.datarouter.nodewatch.shadowtable.config;

import io.datarouter.storage.file.Directory;
import io.datarouter.storage.util.Subpath;

public interface DatarouterShadowTableDirectorySupplier{

	// Range: data is dumped here from the table in parallel chunks
	final Subpath SUBPATH_RANGE = new Subpath("range");

	Directory getDirectory7d();

	default Directory getRangeDirectory(){
		return getDirectory7d().subdirectory(SUBPATH_RANGE);
	}


	class NoOpShadowTableDirectorySupplier
	implements DatarouterShadowTableDirectorySupplier{

		@Override
		public Directory getDirectory7d(){
			throw new UnsupportedOperationException();
		}

	}

}
