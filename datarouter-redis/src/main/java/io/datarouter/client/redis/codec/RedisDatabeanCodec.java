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
package io.datarouter.client.redis.codec;

import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Twin;

public class RedisDatabeanCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(RedisDatabeanCodec.class);

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final int clientMaxKeyLength;
	private final int clientMaxValueLength;
	private final int nodeSubpathLength;
	private final int maxKeyLength;
	private final byte[] pathBytes;

	public RedisDatabeanCodec(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			int clientMaxKeyLength,
			int clientMaxValueLength,
			int nodeSubpathLength,
			Subpath path){//TODO remove when blob node prepends the path
		this.fieldInfo = fieldInfo;
		this.clientMaxKeyLength = clientMaxKeyLength;
		this.clientMaxValueLength = clientMaxValueLength;
		this.nodeSubpathLength = nodeSubpathLength;
		maxKeyLength = clientMaxKeyLength;//TOOD subtract nodeSubPathLength when blob node prepends the path
		pathBytes = StringCodec.UTF_8.encode(path.toString());
	}

	public byte[] encodeKey(PK pk){
		byte[] rawPkBytes = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] base64PkBytes = Base64.getUrlEncoder().encode(rawPkBytes);
		return ByteTool.concat(pathBytes, base64PkBytes);
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
		if(keyBytes.length > maxKeyLength){
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
