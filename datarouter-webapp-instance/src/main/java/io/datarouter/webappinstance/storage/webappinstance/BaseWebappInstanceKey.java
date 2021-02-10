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
package io.datarouter.webappinstance.storage.webappinstance;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public abstract class BaseWebappInstanceKey<PK extends RegularPrimaryKey<PK>> extends BaseRegularPrimaryKey<PK>{

	private final String webappName;
	private final String serverName;

	public static class FieldKeys{
		public static final StringFieldKey webappName = new StringFieldKey("webappName");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public BaseWebappInstanceKey(){
		this(null, null);
	}

	public BaseWebappInstanceKey(String webappName, String serverName){
		this.webappName = webappName;
		this.serverName = serverName;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.webappName, webappName),
				new StringField(FieldKeys.serverName, serverName));
	}

	public String getWebappName(){
		return webappName;
	}

	public String getServerName(){
		return serverName;
	}

}
