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
package io.datarouter.client.mysql.config;

import java.util.Collections;
import java.util.Map;

import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.field.codec.factory.StandardMysqlFieldCodecFactory;
import io.datarouter.client.mysql.web.MysqlAppListener;
import io.datarouter.model.field.Field;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterMysqlPlugin extends BaseWebPlugin{

	private final Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByFieldClass;

	private DatarouterMysqlPlugin(
			Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByFieldClass){
		this.additionalCodecClassByFieldClass = additionalCodecClassByFieldClass;
		addAppListener(MysqlAppListener.class);
	}

	@Override
	public void configure(){
		if(additionalCodecClassByFieldClass != null){
			bindDefaultInstance(MysqlFieldCodecFactory.class,
					new StandardMysqlFieldCodecFactory(additionalCodecClassByFieldClass));
		}else{
			bindDefaultInstance(MysqlFieldCodecFactory.class,
					new StandardMysqlFieldCodecFactory(Collections.emptyMap()));
		}
	}

	public static class DatarouterMysqlPluginBuilder{

		private Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByFieldClass;

		public DatarouterMysqlPluginBuilder setAdditionalCodecClassesByFieldClass(
				Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByFieldClass){
			this.additionalCodecClassByFieldClass = additionalCodecClassByFieldClass;
			return this;
		}

		public DatarouterMysqlPlugin build(){
			return new DatarouterMysqlPlugin(additionalCodecClassByFieldClass);
		}

	}

}
