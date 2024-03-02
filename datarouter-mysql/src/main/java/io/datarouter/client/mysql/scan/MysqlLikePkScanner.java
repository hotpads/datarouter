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
package io.datarouter.client.mysql.scan;

import java.util.List;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.read.MysqlLikePathKeyOp;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;

public class MysqlLikePkScanner
extends BaseScanner<List<DatabaseBlobKey>>{

	private final DatarouterClients datarouterClients;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final Subpath path;
	private final Config config;
	private final long nowMs;

	private String startKey;
	private SessionExecutor sessionExecutor;

	public MysqlLikePkScanner(
			DatarouterClients datarouterClients,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlFieldCodecFactory fieldCodecFactory,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			Subpath path,
			Config config,
			SessionExecutor sessionExecutor,
			long nowMs){
		this.datarouterClients = datarouterClients;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldCodecFactory = fieldCodecFactory;
		this.fieldInfo = fieldInfo;
		this.path = path;
		this.config = config;
		this.sessionExecutor = sessionExecutor;
		this.nowMs = nowMs;
	}

	@Override
	public boolean advance(){
		var likeOp = new MysqlLikePathKeyOp<>(
				datarouterClients,
				mysqlSqlFactory,
				fieldCodecFactory,
				fieldInfo,
				path,
				startKey,
				config,
				nowMs);
		this.current = sessionExecutor.runWithoutRetries(likeOp);
		if(this.current.isEmpty()){
			return false;
		}else{
			this.startKey = this.current.getLast().getPathAndFile();
		}
		return true;
	}

}
