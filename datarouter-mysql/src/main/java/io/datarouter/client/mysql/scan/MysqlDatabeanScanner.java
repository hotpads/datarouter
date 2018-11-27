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
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.util.tuple.Range;

public class MysqlDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseNodeScanner<PK,D>{

	private final MysqlReaderOps<PK,D,?> mysqlReaderOps;

	public MysqlDatabeanScanner(MysqlReaderOps<PK,D,?> mysqlReaderOps, Collection<Range<PK>> ranges, Config config){
		super(ranges, config);
		this.mysqlReaderOps = mysqlReaderOps;
	}

	@Override
	protected List<D> doLoad(Collection<Range<PK>> ranges, Config config){
		return mysqlReaderOps.getRanges(ranges, config);
	}

	@Override
	protected PK getPrimaryKey(D databean){
		return databean.getKey();
	}

	@Override
	protected void setCurrentFromResult(D result){
		current = result;
	}

}