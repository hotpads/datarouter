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
package io.datarouter.client.memcached.node;

import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.client.memcached.client.DatarouterMemcachedClient;
import io.datarouter.client.memcached.codec.MemcachedBlobCodec;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.client.memcached.util.MemcachedPathbeanResult;
import io.datarouter.client.memcached.util.MemcachedResult;
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

public class MemcachedBlobNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private static final boolean DEFAULT_IGNORE_EXCEPTION = true;
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);

	private final Subpath rootPath;
	private final MemcachedBlobCodec blobCodec;
	private final Supplier<DatarouterMemcachedClient> lazyClient;

	public MemcachedBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			MemcachedBlobCodec blobCodec,
			Supplier<DatarouterMemcachedClient> lazyClient){
		super(params, clientType);
		this.blobCodec = blobCodec;
		this.lazyClient = lazyClient;
		rootPath = params.getPath();
	}

	/*------------- BlobStorageReader --------------*/

	@Override
	public String getBucket(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Subpath getRootPath(){
		return rootPath;
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return scanMultiKeysInternal(List.of(key))
				.hasAny();
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		//TODO avoid fetching the bytes?
		return scanMultiInternal(List.of(key))
				.map(MemcachedPathbeanResult::value)
				.map(bytes -> bytes.length)
				.map(Integer::longValue)
				.findFirst();
	}

	@Override
	public byte[] read(PathbeanKey key, Config config){
		return scanMultiInternal(List.of(key))
				.findFirst()
				.map(MemcachedPathbeanResult::value)
				.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys, Config config){
		return scanMultiInternal(keys)
				.toMap(MemcachedPathbeanResult::key, MemcachedPathbeanResult::value);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length, Config config){
		int intOffset = (int)offset;
		return scanMultiInternal(List.of(key))
				.findFirst()
				.map(MemcachedPathbeanResult::value)
				.map(bytes -> Arrays.copyOfRange(bytes, intOffset, intOffset + length))
				.orElse(null);
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
		lazyClient.get().set(
				getName(),
				blobCodec.encodeKey(key),
				MemcachedExpirationTool.getExpirationSeconds(config),
				value);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream, Config config){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value, config);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		lazyClient.get().delete(getName(), blobCodec.encodeKey(key), Duration.ofSeconds(3));
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		throw new UnsupportedOperationException();
	}

	/*------------- private --------------*/

	// We're fetching the keys+values but only parsing the keys
	// TODO check if there's a way to fetch only the keys from memcached
	private Scanner<PathbeanKey> scanMultiKeysInternal(Collection<PathbeanKey> keys){
		return Scanner.of(keys)
				.map(blobCodec::encodeKey)
				.listTo(memcachedStringKeys -> lazyClient.get().scanMultiBytes(
						getName(),
						memcachedStringKeys,
						DEFAULT_TIMEOUT.toMillis(),
						DEFAULT_IGNORE_EXCEPTION))
				.map(MemcachedResult::key)
				.map(blobCodec::decodeKey);
	}

	private Scanner<MemcachedPathbeanResult> scanMultiInternal(Collection<PathbeanKey> keys){
		return Scanner.of(keys)
				.map(blobCodec::encodeKey)
				.listTo(memcachedStringKeys -> lazyClient.get().scanMultiBytes(
						getName(),
						memcachedStringKeys,
						DEFAULT_TIMEOUT.toMillis(),
						DEFAULT_IGNORE_EXCEPTION))
				.map(blobCodec::decodeResult);
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
