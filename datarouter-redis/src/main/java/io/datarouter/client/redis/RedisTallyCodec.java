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
package io.datarouter.client.redis;

import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.IntegerByteTool;

public class RedisTallyCodec{

	private static final TallyFielder SAMPLE_FIELDER = new TallyFielder();

	private final int version;

	public RedisTallyCodec(int version){
		this.version = version;
	}

	public byte[] encodeKey(TallyKey pk){
		byte[] key = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] schemaVersion = IntegerByteTool.getRawBytes(version);
		return ByteTool.concatenate(schemaVersion, key);
	}

	public byte[] encode(Tally databean){
		return DatabeanTool.getBytes(databean, SAMPLE_FIELDER);
	}

	public <D> D decode(Supplier<D> supplier, Map<String,Field<?>> fieldByPrefixedName, byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(supplier, fieldByPrefixedName, bytes);
	}

}
