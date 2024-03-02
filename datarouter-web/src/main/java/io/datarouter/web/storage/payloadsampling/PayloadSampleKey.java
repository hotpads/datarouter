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
package io.datarouter.web.storage.payloadsampling;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class PayloadSampleKey extends BaseRegularPrimaryKey<PayloadSampleKey>{

	private String path;
	private String serverName;

	public static class FieldKeys{
		public static final StringFieldKey path = new StringFieldKey("path");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public PayloadSampleKey(){
	}

	public PayloadSampleKey(String path, String serverName){
		this.path = path;
		this.serverName = serverName;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.path, path),
				new StringField(FieldKeys.serverName, serverName));
	}

	public String getPath(){
		return path;
	}

	public String getServerName(){
		return serverName;
	}

}
