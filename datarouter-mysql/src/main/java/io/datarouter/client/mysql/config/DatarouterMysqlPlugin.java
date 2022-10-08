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
package io.datarouter.client.mysql.config;

import java.util.HashMap;
import java.util.Map;

import io.datarouter.client.mysql.config.MysqlSchemaProvider.GenericMysqlSchemaProvider;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.field.codec.factory.StandardMysqlFieldCodecFactory;
import io.datarouter.client.mysql.web.MysqlAppListener;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.model.field.Field;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterMysqlPlugin extends BaseWebPlugin{

	private final Map<
			Class<? extends Field<?>>,
			MysqlFieldCodec<?,?>> additionalCodecByField;
	private final boolean isPrimarySchema;

	private DatarouterMysqlPlugin(Map<
			Class<? extends Field<?>>,
			MysqlFieldCodec<?,?>> additionalCodecClassByField,
			boolean isPrimarySchema){
		this.additionalCodecByField = additionalCodecClassByField;
		this.isPrimarySchema = isPrimarySchema;
		addAppListener(MysqlAppListener.class);
		addSettingRoot(DatarouterMysqlSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterMysqlTriggerGroup.class);
		addDatarouterGithubDocLink("datarouter-mysql");
	}

	@Override
	public void configure(){
		bindDefaultInstance(MysqlFieldCodecFactory.class, new StandardMysqlFieldCodecFactory(
				additionalCodecByField));
		bind(MysqlSchemaProvider.class).toInstance(new GenericMysqlSchemaProvider(isPrimarySchema));
	}

	public static class DatarouterMysqlPluginBuilder{

		private final Map<
				Class<? extends Field<?>>,
				MysqlFieldCodec<?,?>> codecsByField = new HashMap<>();
		private boolean isPrimarySchema = true;

		public <F extends Field<?>,C extends MysqlFieldCodec<?,?>> DatarouterMysqlPluginBuilder addCodec(
				Class<F> fieldClass,
				C codecSupplier){
			codecsByField.put(fieldClass, codecSupplier);
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
