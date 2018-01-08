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
package io.datarouter.model.serialize.fielder;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.model.databean.VersionedDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.PrimaryKey;

public abstract class BaseVersionedDatabeanFielder<PK extends PrimaryKey<PK>,D extends VersionedDatabean<PK,D>>
extends BaseDatabeanFielder<PK,D>{

	public static class FieldKeys{
		public static final LongFieldKey version = new LongFieldKey("version");
	}

	public BaseVersionedDatabeanFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
		super(primaryKeyFielderClass);
	}

	@Override
	public final List<Field<?>> getNonKeyFields(D databean){
		List<Field<?>> fields = new ArrayList<>();
		fields.addAll(getVersionedNonKeyFields(databean));
		fields.add(new LongField(FieldKeys.version, databean.getVersion()));
		return fields;
	}

	public abstract List<Field<?>> getVersionedNonKeyFields(D databean);

	@Override
	public boolean isVersioned(){
		return true;
	}

}
