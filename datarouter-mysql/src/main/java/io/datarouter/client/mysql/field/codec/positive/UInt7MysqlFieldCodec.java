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
package io.datarouter.client.mysql.field.codec.positive;

import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.field.codec.primitive.BaseByteMysqlFieldCodec;
import io.datarouter.model.field.imp.positive.UInt7Field;

public class UInt7MysqlFieldCodec extends BaseByteMysqlFieldCodec<UInt7Field>{

	public UInt7MysqlFieldCodec(){//no-arg for reflection
		this(null);
	}

	public UInt7MysqlFieldCodec(UInt7Field field){
		super(field);
	}

	@Override
	protected Integer getMaxColumnLength(){
		return 3;
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.TINYINT;
	}

	@Override
	protected Integer getJavaSqlType(){
		return Types.TINYINT;
	}

}
