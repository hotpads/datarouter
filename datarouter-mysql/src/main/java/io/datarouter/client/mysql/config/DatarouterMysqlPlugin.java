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

import java.util.HashMap;
import java.util.Map;

import io.datarouter.client.mysql.config.MysqlSchemaProvider.GenericMysqlSchemaProvider;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.field.codec.factory.StandardMysqlFieldCodecFactory;
import io.datarouter.client.mysql.web.MysqlAppListener;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.model.field.Field;

public class DatarouterMysqlPlugin extends BaseJobPlugin{

	private final Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByField;
	private final boolean isPrimarySchema;

	private DatarouterMysqlPlugin(
			Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additionalCodecClassByField,
					boolean isPrimarySchema){
		this.additionalCodecClassByField = additionalCodecClassByField;
		this.isPrimarySchema = isPrimarySchema;
		addAppListener(MysqlAppListener.class);
		addSettingRoot(DatarouterMysqlSettingRoot.class);
		addTriggerGroup(DatarouterMysqlTriggerGroup.class);
		addDatarouterGithubDocLink("datarouter-mysql");
	}

	@Override
	public void configure(){
		bindDefaultInstance(MysqlFieldCodecFactory.class, new StandardMysqlFieldCodecFactory(
				additionalCodecClassByField));
		bind(MysqlSchemaProvider.class).toInstance(new GenericMysqlSchemaProvider(isPrimarySchema));
	}

	public static class DatarouterMysqlPluginBuilder{

		private Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> codecsByField = new HashMap<>();
		private boolean isPrimarySchema = true;

		public DatarouterMysqlPluginBuilder addMysqlFieldCodec(
				Class<? extends Field<?>> field,
				Class<? extends MysqlFieldCodec<?>> codec){
			codecsByField.put(field, codec);
			return this;
		}

		public DatarouterMysqlPluginBuilder setPrimarySchema(boolean isPrimarySchema){
			this.isPrimarySchema = isPrimarySchema;
			return this;
		}

		public DatarouterMysqlPlugin build(){
			return new DatarouterMysqlPlugin(codecsByField, isPrimarySchema);
		}

	}

}
