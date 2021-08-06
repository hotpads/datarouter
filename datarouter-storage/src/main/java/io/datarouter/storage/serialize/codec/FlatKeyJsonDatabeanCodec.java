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
package io.datarouter.storage.serialize.codec;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.model.serialize.codec.JsonDatabeanCodec;
import io.datarouter.model.serialize.fielder.DatabeanFielder;

public class FlatKeyJsonDatabeanCodec extends JsonDatabeanCodec{

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	String toString(D databean, F fielder){
		return JsonDatabeanTool.databeanToJsonString(databean, fielder, true);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D fromString(String string, F fielder, Supplier<D> databeanSupplier){
		return JsonDatabeanTool.databeanFromJson(databeanSupplier, fielder, string, true);
	}

}
