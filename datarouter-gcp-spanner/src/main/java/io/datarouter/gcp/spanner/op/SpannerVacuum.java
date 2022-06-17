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
package io.datarouter.gcp.spanner.op;

import java.util.Optional;

import com.google.cloud.spanner.DatabaseClient;

import io.datarouter.gcp.spanner.op.read.SpannerVacuumFindOp;
import io.datarouter.gcp.spanner.op.write.SpannerVacuumOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerVacuum<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final Config config;

	public SpannerVacuum(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Config config){
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.config = config;
	}

	public void vacuum(){
		long nowMs = System.currentTimeMillis();
		var vacuumFindOp = new SpannerVacuumFindOp<>(client, fieldInfo, null, true, config);
		String startKey = null;
		Optional<String> endKey = vacuumFindOp.wrappedCall();
		while(startKey != null || endKey.isPresent()){
			var vacuumOp = new SpannerVacuumOp<>(client, fieldInfo, startKey, endKey.orElse(null), nowMs);
			vacuumOp.wrappedCall();
			vacuumFindOp = new SpannerVacuumFindOp<>(client, fieldInfo, endKey.orElse(null), false, config);
			startKey = endKey.orElse(null);
			endKey = vacuumFindOp.wrappedCall();
		}
	}

}
