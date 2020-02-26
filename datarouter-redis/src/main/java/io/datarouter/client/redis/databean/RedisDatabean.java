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
package io.datarouter.client.redis.databean;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class RedisDatabean extends BaseDatabean<RedisDatabeanKey,RedisDatabean>{

	private String data;

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class RedisDatabeanFielder extends BaseDatabeanFielder<RedisDatabeanKey,RedisDatabean>{

		public RedisDatabeanFielder(){
			super(RedisDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RedisDatabean databean){
			return Arrays.asList(new StringField(FieldKeys.data, databean.data));
		}

	}

	public RedisDatabean(){
		this(null, null);
	}

	public RedisDatabean(String id, String data){
		super(new RedisDatabeanKey(id));
		this.data = data;
	}

	@Override
	public Class<RedisDatabeanKey> getKeyClass(){
		return RedisDatabeanKey.class;
	}

}
