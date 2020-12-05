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
package io.datarouter.client.memcached.codec;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.util.tuple.Pair;

public class MemcachedDatabeanCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedDatabeanCodec.class);

	private static final int MEGABYTE = 1024 * 1024;

	private final String nodeName;
	private final int schemaVersion;
	private final DatabeanFielder<PK,D> fielder;
	private final Supplier<D> databeanSupplier;
	private final Map<String,Field<?>> fieldByPrefixedName;

	public MemcachedDatabeanCodec(
			String nodeName,
			int schemaVersion,
			DatabeanFielder<PK,D> fielder,
			Supplier<D> databeanSupplier,
			Map<String,Field<?>> fieldByPrefixedName){
		this.nodeName = nodeName;
		this.schemaVersion = schemaVersion;
		this.fielder = fielder;
		this.databeanSupplier = databeanSupplier;
		this.fieldByPrefixedName = fieldByPrefixedName;
	}

	public String encodeKey(PK pk){
		return MemcachedKey.encode(nodeName, schemaVersion, pk);
	}

	public byte[] encode(D databean){
		return DatabeanTool.getBytes(databean, fielder);
	}

	public Optional<Pair<String,byte[]>> encodeKeyValueIfValid(D databean){
		//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
		//  could be big savings for small or key-only databeans
		byte[] value = encode(databean);
		if(value.length > 2 * MEGABYTE){
			//memcached max size is 1mb for a compressed object, so don't PUT things that won't compress well
			logger.warn("object too big for memcached length={} key={}", value.length, databean.getKey());
			return Optional.empty();
		}
		String key = encodeKey(databean.getKey());
		return Optional.of(new Pair<>(key, value));
	}

	public D decodeResultValue(Pair<String,Object> result){
		byte[] byteValue = (byte[])result.getRight();
		return decodeBytes(byteValue);
	}

	private D decodeBytes(byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(
				databeanSupplier,
				fieldByPrefixedName,
				bytes);
	}

}
