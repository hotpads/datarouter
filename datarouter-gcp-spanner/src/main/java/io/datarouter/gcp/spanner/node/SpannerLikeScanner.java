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
package io.datarouter.gcp.spanner.node;

import java.util.List;

import com.google.cloud.spanner.DatabaseClient;

import io.datarouter.scanner.BaseScanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;

public class SpannerLikeScanner extends BaseScanner<List<DatabaseBlob>>{

	private final DatabaseClient client;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final Subpath path;
	private final Config config;
	private final long nowMs;

	private String startKey;

	public SpannerLikeScanner(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			Subpath path,
			Config config,
			long nowMs){
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.path = path;
		this.config = config;
		this.nowMs = nowMs;
	}

	@Override
	public boolean advance(){
		var likeOp = new SpannerLikeOp<>(client, fieldInfo, startKey, config, path, nowMs);
		this.current = likeOp.wrappedCall();
		if(this.current.isEmpty()){
			return false;
		}else{
			this.startKey = this.current.getLast().getKey().getPathAndFile();
		}
		return true;
	}

}
