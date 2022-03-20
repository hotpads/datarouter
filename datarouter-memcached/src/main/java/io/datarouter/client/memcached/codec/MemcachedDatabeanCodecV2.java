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
package io.datarouter.client.memcached.codec;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.util.tuple.Pair;

public class MemcachedDatabeanCodecV2<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedDatabeanCodecV2.class);

	private static final int SKIP_IF_LARGER_THAN = ByteUnitType.MiB.toBytesInt(2);

	private final DatabeanFielder<PK,D> fielder;
	private final Supplier<D> databeanSupplier;
	private final Map<String,Field<?>> fieldByPrefixedName;
	private final int nodeSubpathLength;
	private final int maxKeyLength;

	public MemcachedDatabeanCodecV2(
			DatabeanFielder<PK,D> fielder,
			Supplier<D> databeanSupplier,
			Map<String,Field<?>> fieldByPrefixedName,
			int nodeSubpathLength){
		this.fielder = fielder;
		this.databeanSupplier = databeanSupplier;
		this.fieldByPrefixedName = fieldByPrefixedName;
		this.nodeSubpathLength = nodeSubpathLength;
		this.maxKeyLength = CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH - nodeSubpathLength;
	}

	public Optional<Pair<PathbeanKey,byte[]>> encodeDatabeanIfValid(D databean){
		Optional<PathbeanKey> pathbeanKey = encodeKey(databean.getKey());
		if(pathbeanKey.isEmpty()){
			return Optional.empty();
		}
		//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
		//  could be big savings for small or key-only databeans
		byte[] value = encodeDatabean(databean);
		if(value.length > SKIP_IF_LARGER_THAN){
			//memcached max size is 1mb for a compressed object, so don't PUT things that won't compress well
			logger.warn("object too big for memcached length={} key={}", value.length, databean.getKey());
			return Optional.empty();
		}
		return Optional.of(new Pair<>(pathbeanKey.orElseThrow(), value));
	}

	public Optional<PathbeanKey> encodeKey(PrimaryKey<?> pk){
		PathbeanKey pathbeanKey = MemcachedKeyV2.encodeDatabeanKey(pk);
		String encodedKey = pathbeanKey.getPathAndFile();
		int encodedKeyLength = encodedKey.length();
		if(pathbeanKey.getPathAndFile().length() > maxKeyLength){
			logger.warn("key too long for memcached length={} nodeSubpathLength={} maxKeyLength={} key={}"
					+ " encodedKey={}",
					encodedKeyLength,
					nodeSubpathLength,
					maxKeyLength,
					pk,
					encodedKey);
			return Optional.empty();
		}
		return Optional.of(pathbeanKey);
	}

	private byte[] encodeDatabean(D databean){
		return DatabeanTool.getBytes(databean, fielder);
	}

	public D decodeDatabean(byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(
				databeanSupplier,
				fieldByPrefixedName,
				bytes);
	}

}
