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
package io.datarouter.client.redis.node;

import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.client.RedisKeyValue;
import io.datarouter.client.redis.client.RedisRequestConfig;
import io.datarouter.client.redis.codec.RedisBlobCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;
import io.lettuce.core.KeyValue;

public class RedisBlobNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final Supplier<DatarouterRedisClient> lazyClient;
	private final String bucket;
	private final Subpath rootPath;
	private final RedisBlobCodec codec;

	public RedisBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			RedisBlobCodec codec,
			Supplier<DatarouterRedisClient> lazyClient){
		super(params, clientType);
		this.codec = codec;
		this.lazyClient = lazyClient;
		this.bucket = params.getPhysicalName();
		this.rootPath = params.getPath();
	}

	/*------------- BlobStorageReader --------------*/

	@Override
	public String getBucket(){
		return bucket;
	}

	@Override
	public Subpath getRootPath(){
		return rootPath;
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(codec::encodeKey)
				.map(encodedKey -> lazyClient.get().exists(
						encodedKey,
						RedisRequestConfig.forRead(getName(), config)))
				.orElseThrow();
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(encodedKey -> lazyClient.get().find(
						encodedKey,
						RedisRequestConfig.forRead(getName(), config)))
				.map(value -> value.length)
				.map(Integer::longValue);
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(encodedKey -> lazyClient.get().find(
						encodedKey,
						RedisRequestConfig.forRead(getName(), config)));
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		int from = (int)offset;
		int to = from + length;
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(encodedKey -> lazyClient.get().find(
						encodedKey,
						RedisRequestConfig.forRead(getName(), config)))
				.map(bytes -> Arrays.copyOfRange(bytes, from, to));
	}

	@Override
	public Optional<byte[]> readEnding(PathbeanKey key, int length, Config config){
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(encodedKey -> lazyClient.get().find(
						encodedKey,
						RedisRequestConfig.forRead(getName(), config)))
				.map(bytes -> Arrays.copyOfRange(
						bytes,
						Math.max(0, bytes.length - length),
						bytes.length));
	}

	@Override
	public Map<PathbeanKey,byte[]> readMulti(List<PathbeanKey> keys, Config config){
		return Scanner.of(keys)
				.map(codec::encodeKey)
				.listTo(encodedKeys -> lazyClient.get().mget(
						encodedKeys,
						RedisRequestConfig.forRead(getName(), config)))
				.include(KeyValue::hasValue)
				.toMap(codec::decodeKey, KeyValue::getValue);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		throw new UnsupportedOperationException();
	}

	/*------------- BlobStorageWriter --------------*/

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		RedisKeyValue kv = new RedisKeyValue(codec.encodeKey(key), value);
		config.findTtl()
				.map(Duration::toMillis)
				.ifPresentOrElse(
						ttlMs -> lazyClient.get().psetex(
								kv,
								ttlMs,
								RedisRequestConfig.forWrite(getName(), config)),
						() -> lazyClient.get().set(
								kv,
								RedisRequestConfig.forWrite(getName(), config)));
	}

	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value, config);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		Optional.of(key)
				.map(codec::encodeKey)
				.ifPresent(encodedKey -> lazyClient.get().del(
						encodedKey,
						RedisRequestConfig.forWrite(getName(), config)));
	}

	@Override
	public void deleteMulti(List<PathbeanKey> keys, Config config){
		lazyClient.get().del(
				codec.encodeKeys(keys),
				RedisRequestConfig.forWrite(getName(), config));
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
