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
package io.datarouter.client.redis;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.tuple.Twin;

public class RedisDatabeanCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(RedisDatabeanCodec.class);

	private static final int CODEC_VERSION = 1;

	//redis can handle a max keys size of 32 megabytes
	private static final int MAX_REDIS_KEY_SIZE = 1024 * 64;

	private final int version;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public RedisDatabeanCodec(int version, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo){
		this.version = version;
		this.fieldInfo = fieldInfo;
	}

	public byte[] encodeKey(PK pk){
		byte[] codecVersion = IntegerByteTool.getRawBytes(CODEC_VERSION);
		byte[] key = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] schemaVersion = IntegerByteTool.getRawBytes(version);
		return ByteTool.concatenate(codecVersion, schemaVersion, key);
	}

	public List<byte[]> encodeKeys(Collection<PK> pks){
		return Scanner.of(pks)
				.map(this::encodeKey)
				.list();
	}

	public byte[] encode(D databean){
		return DatabeanTool.getBytes(databean, fieldInfo.getSampleFielder());
	}

	public D decode(byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(
				fieldInfo.getDatabeanSupplier(),
				fieldInfo.getFieldByPrefixedName(),
				bytes);
	}

	public Optional<Twin<byte[]>> encodeIfValid(D databean){
		byte[] keyBytes;
		try{
			keyBytes = encodeKey(databean.getKey());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		if(keyBytes.length > MAX_REDIS_KEY_SIZE){
			logBigKey(keyBytes.length, databean.getKey());
			return Optional.empty();
		}
		return Optional.of(new Twin<>(keyBytes, encode(databean)));
	}

	private void logBigKey(int length, PK pk){
		String message = String.format("skipping, key too big for redis! length=%s, type=%s, key=%s",
				length,
				pk.getClass().getSimpleName(),
				pk);
		logger.error(message);
	}

}
