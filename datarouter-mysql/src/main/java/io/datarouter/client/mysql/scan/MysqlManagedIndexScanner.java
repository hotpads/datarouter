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

import io.datarouter.client.mysql.node.MysqlNodeManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.sanitization.sanitizer.PrimaryKeySanitizer;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class MysqlManagedIndexScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseNodeScanner<IK,IE>{

	private final MysqlNodeManager mysqlNodeManager;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public MysqlManagedIndexScanner(
			MysqlNodeManager mysqlNodeManager,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			boolean caseInsensitive){
		super(ranges, config, caseInsensitive);
		this.mysqlNodeManager = mysqlNodeManager;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.fieldInfo = fieldInfo;
	}

	@Override
	protected IK getPrimaryKey(IE fieldSet){
		IK key = fieldSet.getKey();
		PrimaryKeySanitizer.logForNullPrimaryKeyValues(key);
		return key;
	}

	@Override
	protected List<IE> loadRanges(Collection<Range<IK>> ranges, Config config){
		return mysqlNodeManager.getIndexRanges(fieldInfo, ranges, config, indexEntryFieldInfo);
	}

}
