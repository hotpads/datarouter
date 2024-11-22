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
package io.datarouter.nodewatch.config;

import io.datarouter.storage.file.Directory;
import io.datarouter.storage.util.Subpath;

public interface DatarouterStorageStatsDirectorySupplier{

	//storageStats subpaths
	final Subpath SUBPATH_CLIENT_TYPE = new Subpath("clientType");
	final Subpath SUBPATH_SERVICE = new Subpath("service");
	final Subpath SUBPATH_TABLE = new Subpath("table");

	Directory getDirectory7d();

	default Directory getStorageStatsClientTypeDirectory(){
		return getDirectory7d().subdirectory(SUBPATH_CLIENT_TYPE);
	}

	default Directory getStorageStatsServiceDirectory(){
		return getDirectory7d().subdirectory(SUBPATH_SERVICE);
	}

	default Directory getStorageStatsTableDirectory(){
		return getDirectory7d().subdirectory(SUBPATH_TABLE);
	}


	class NoOpStorageStatsDirectorySupplier
	implements DatarouterStorageStatsDirectorySupplier{

		@Override
		public Directory getDirectory7d(){
			throw new UnsupportedOperationException();
		}

	}

}
