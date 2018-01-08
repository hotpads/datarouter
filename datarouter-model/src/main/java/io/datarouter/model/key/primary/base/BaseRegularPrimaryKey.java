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
package io.datarouter.model.key.primary.base;

import java.util.Collections;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.BasePrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;

public abstract class BaseRegularPrimaryKey<PK extends RegularPrimaryKey<PK>>
extends BasePrimaryKey<PK>
implements RegularPrimaryKey<PK>{

	@SuppressWarnings("unchecked")
	@Override
	public PK getEntityKey(){
		return (PK)this;
	}

	@Override
	public PK prefixFromEntityKey(PK entityKey){
		return entityKey;
	}

	@Override
	public List<Field<?>> getEntityKeyFields(){
		return getFields();
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Collections.emptyList();
	}

}
