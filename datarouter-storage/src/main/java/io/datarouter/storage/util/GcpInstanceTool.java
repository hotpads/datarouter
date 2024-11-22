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
package io.datarouter.storage.util;

import io.datarouter.util.net.NetTool;
import io.datarouter.util.net.NetTool.Header;

public class GcpInstanceTool{

	private static final String
			GCP_METADATA_INSTANCE_ID = "http://metadata.google.internal/computeMetadata/v1/instance/id";
	private static final Header
			GCP_METADATA_HEADER = new Header("Metadata-Flavor", "Google");

	public static boolean isGcp(){
		return NetTool.curl("GET", GCP_METADATA_INSTANCE_ID, false, GCP_METADATA_HEADER).isPresent();
	}

}
