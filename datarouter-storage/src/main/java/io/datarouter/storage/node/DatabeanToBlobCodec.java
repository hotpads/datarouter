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
package io.datarouter.storage.node;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

public class DatabeanToBlobCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanToBlobCodec.class);

	public static final String CODEC_VERSION = "1";

	private final String clientTypeName;
	private final DatabeanFielder<PK,D> fielder;
	private final Supplier<D> databeanSupplier;
	private final Map<String,Field<?>> fieldByPrefixedName;
	private final Subpath path;
	private final int clientMaxValueLength;
	private final int pathLength;
	private final int maxKeyLength;

	public DatabeanToBlobCodec(
			String clientTypeName,
			DatabeanFielder<PK,D> fielder,
			Supplier<D> databeanSupplier,
			Map<String,Field<?>> fieldByPrefixedName,
			Subpath path,
			int clientMaxKeyLength,
			int clientMaxValueLength){
		this.clientTypeName = clientTypeName;
		this.fielder = fielder;
		this.databeanSupplier = databeanSupplier;
		this.fieldByPrefixedName = fieldByPrefixedName;
		this.path = path;
		this.clientMaxValueLength = clientMaxValueLength;
		pathLength = path.toString().length();
		maxKeyLength = clientMaxKeyLength - pathLength;
	}

	public Optional<PathBbeanKeyAndValue> encodeDatabeanIfValid(D databean){
		Optional<PathbeanKey> pathbeanKey = encodeKeyIfValid(databean.getKey());
		if(pathbeanKey.isEmpty()){
			return Optional.empty();
		}
		byte[] value = encodeDatabean(databean);
		if(value.length > clientMaxValueLength){
			logger.warn("object too big for {} length={} key={}", clientTypeName, value.length, databean.getKey());
			return Optional.empty();
		}
		return Optional.of(new PathBbeanKeyAndValue(pathbeanKey.orElseThrow(), value));
	}

	public record PathBbeanKeyAndValue(
			PathbeanKey pathbeanKey,
			byte[] value){
	}

	public Optional<PathbeanKey> encodeKeyIfValid(PK pk){
		PathbeanKey pathbeanKey = encodeKey(pk);
		String encodedKey = pathbeanKey.getPathAndFile();
		if(pathbeanKey.getPathAndFile().length() > maxKeyLength){
			logger.warn("key too long for {} length={} nodeSubpathLength={} maxKeyLength={} key={}"
					+ " encodedKey={}",
					clientTypeName,
					encodedKey.length(),
					pathLength,
					maxKeyLength,
					pk,
					path + encodedKey);
			return Optional.empty();
		}
		return Optional.of(pathbeanKey);
	}

	public D decodeDatabean(byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(databeanSupplier, fieldByPrefixedName, bytes);
	}

	private PathbeanKey encodeKey(PK pk){
		byte[] bytes = FieldTool.getConcatenatedValueBytes(pk.getFields());
		String string = Base64.getUrlEncoder().encodeToString(bytes);
		return PathbeanKey.of(string);
	}

	private byte[] encodeDatabean(D databean){
		return DatabeanTool.getBytes(databean, fielder);
	}

}
