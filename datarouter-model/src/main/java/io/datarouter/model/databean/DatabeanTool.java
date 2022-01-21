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
package io.datarouter.model.databean;

import io.datarouter.bytes.ByteTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;

public class DatabeanTool{

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> byte[] getBytes(
			D databean,
			DatabeanFielder<PK,D> fielder){
		// always include zero-length fields in key bytes
		byte[] keyBytes = FieldTool.getSerializedKeyValues(fielder.getKeyFields(databean), true, false);
		// skip zero-length fields in non-key bytes
		// TODO should this distinguish between null and empty Strings?
		byte[] nonKeyBytes = FieldTool.getSerializedKeyValues(fielder.getNonKeyFields(databean), true, true);
		return ByteTool.concat(keyBytes, nonKeyBytes);
	}

}
