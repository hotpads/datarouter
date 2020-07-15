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
package io.datarouter.trace.storage.trace;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.trace.storage.entity.BaseTraceEntityKey;

public abstract class BaseTraceKey<
		EK extends BaseTraceEntityKey<EK>,
		PK extends BaseTraceKey<EK,PK>>
extends BaseEntityPrimaryKey<EK,PK>{

	protected EK entityKey;

	public BaseTraceKey(){
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return List.of();
	}

	@Override
	public EK getEntityKey(){
		return entityKey;
	}

	public String getId(){
		return entityKey.getTraceEntityId();
	}

}
