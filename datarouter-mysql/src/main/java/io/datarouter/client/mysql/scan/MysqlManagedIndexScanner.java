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
package io.datarouter.client.mysql.scan;

import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.node.MysqlReaderOps;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class MysqlManagedIndexScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseNodeScanner<IK,IE>{

	private final MysqlReaderOps<PK,D,F> mysqlReaderOps;
	private final DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo;

	public MysqlManagedIndexScanner(MysqlReaderOps<PK,D,F> mysqlReaderOps,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges, Config config){
		super(ranges, config);
		this.mysqlReaderOps = mysqlReaderOps;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
	}

	@Override
	protected IK getPrimaryKey(IE fieldSet){
		return fieldSet.getKey();
	}

	@Override
	protected List<IE> doLoad(Collection<Range<IK>> ranges, Config config){
		return mysqlReaderOps.getIndexRanges(ranges, config, indexEntryFieldInfo);
	}

	@Override
	protected void setCurrentFromResult(IE result){
		current = result;
	}

}
