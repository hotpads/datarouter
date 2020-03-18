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
package io.datarouter.client.mysql.ddl.generate.imp;

import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableEngine;

public class SqlTableMetadata{

	public final MysqlTableEngine engine;
	public final MysqlRowFormat rowFormat;
	public final MysqlCollation collation;
	public final MysqlCharacterSet characterSet;

	public SqlTableMetadata(
			MysqlTableEngine engine,
			MysqlRowFormat rowFormat,
			MysqlCollation collation,
			MysqlCharacterSet characterSet){
		this.engine = engine;
		this.rowFormat = rowFormat;
		this.collation = collation;
		this.characterSet = characterSet;
	}

}
