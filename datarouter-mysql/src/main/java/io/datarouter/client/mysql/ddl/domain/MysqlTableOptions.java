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
package io.datarouter.client.mysql.ddl.domain;

import io.datarouter.model.serialize.fielder.DatabeanFielder;

public class MysqlTableOptions{

	private static final MysqlCharacterSet DEFAULT_CHARACTER_SET = MysqlCharacterSet.utf8mb4;
	private static final MysqlCollation DEFAULT_COLLATION = MysqlCollation.utf8mb4_bin;
	private static final MysqlRowFormat DEFAULT_ROW_FORMAT = MysqlRowFormat.DYNAMIC;

	public static final MysqlTableOptions DEFAULT = new MysqlTableOptions(DEFAULT_CHARACTER_SET, DEFAULT_COLLATION,
			DEFAULT_ROW_FORMAT);

	private final MysqlCharacterSet characterSet;
	private final MysqlCollation collation;
	private final MysqlRowFormat rowFormat;

	private MysqlTableOptions(MysqlCharacterSet characterSet, MysqlCollation collation, MysqlRowFormat rowFormat){
		this.characterSet = characterSet;
		this.collation = collation;
		this.rowFormat = rowFormat;
	}

	public MysqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public MysqlCollation getCollation(){
		return collation;
	}

	public MysqlRowFormat getRowFormat(){
		return rowFormat;
	}

	public static class MysqlTableOptionsBuilder{

		private MysqlCharacterSet characterSet = DEFAULT_CHARACTER_SET;
		private MysqlCollation collation = DEFAULT_COLLATION;
		private MysqlRowFormat rowFormat = DEFAULT_ROW_FORMAT;

		public MysqlTableOptionsBuilder withCharacterSet(MysqlCharacterSet characterSet){
			this.characterSet = characterSet;
			return this;
		}

		public MysqlTableOptionsBuilder withCollation(MysqlCollation collation){
			this.collation = collation;
			return this;
		}

		public MysqlTableOptionsBuilder withRowFormat(MysqlRowFormat rowFormat){
			this.rowFormat = rowFormat;
			return this;
		}

		public MysqlTableOptions build(){
			return new MysqlTableOptions(characterSet, collation, rowFormat);
		}

	}

	public static MysqlTableOptions make(DatabeanFielder<?,?> fielder){
		MysqlTableOptionsBuilder builder = new MysqlTableOptionsBuilder();
		fielder.getOption(MysqlCharacterSet.KEY).ifPresent(builder::withCharacterSet);
		fielder.getOption(MysqlCollation.KEY).ifPresent(builder::withCollation);
		fielder.getOption(MysqlRowFormat.KEY).ifPresent(builder::withRowFormat);
		return builder.build();
	}

}
