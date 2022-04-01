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
package io.datarouter.client.memcached.node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.InputStreamTool;
import io.datarouter.client.memcached.client.DatarouterMemcachedClient;
import io.datarouter.client.memcached.codec.MemcachedBlobCodec;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Pair;

public class MemcachedBlobNode
extends BasePhysicalNode<PathbeanKey,Pathbean,PathbeanFielder>
implements PhysicalBlobStorageNode{

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);
	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

	private final Subpath rootPath;
	private final MemcachedBlobCodec blobCodec;
	private final Supplier<DatarouterMemcachedClient> lazyClient;

	public MemcachedBlobNode(
			NodeParams<PathbeanKey,Pathbean,PathbeanFielder> params,
			ClientType<?,?> clientType,
			Supplier<DatarouterMemcachedClient> lazyClient){
		super(params, clientType);
		this.lazyClient = lazyClient;
		rootPath = params.getPath();
		blobCodec = new MemcachedBlobCodec(rootPath);
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
	public boolean exists(PathbeanKey key){
		return scanMultiKeysInternal(List.of(key))
				.hasAny();
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		//TODO avoid fetching the bytes?
		return scanMultiInternal(List.of(key))
				.map(Pair::getRight)
				.map(bytes -> bytes.length)
				.map(Integer::longValue)
				.findFirst();
	}

	@Override
	public byte[] read(PathbeanKey key){
		return scanMultiInternal(List.of(key))
				.findFirst()
				.map(Pair::getRight)
				.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys){
		return scanMultiInternal(keys)
				.toMap(Pair::getLeft, Pair::getRight);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		return scanMultiInternal(List.of(key))
				.findFirst()
				.map(Pair::getRight)
				.map(bytes -> ByteTool.copyOfRange(bytes, (int)offset, length))
				.orElse(null);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
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
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		byte[] bytes = chunks
				.listTo(ByteTool::concat);
		write(key, bytes);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		var baos = new ByteArrayOutputStream();
		InputStreamTool.transferTo(inputStream, baos);
		write(key, baos.toByteArray());
	}

	@Override
	public void delete(PathbeanKey key){
		lazyClient.get().delete(getName(), blobCodec.encodeKey(key), Duration.ofSeconds(3));
	}

	@Override
	public void deleteAll(Subpath subpath){
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
				.map(Pair::getLeft)
				.map(blobCodec::decodeKey);
	}

	private Scanner<Pair<PathbeanKey,byte[]>> scanMultiInternal(Collection<PathbeanKey> keys){
		return Scanner.of(keys)
				.map(blobCodec::encodeKey)
				.listTo(memcachedStringKeys -> lazyClient.get().scanMultiBytes(
						getName(),
						memcachedStringKeys,
						DEFAULT_TIMEOUT.toMillis(),
						DEFAULT_IGNORE_EXCEPTION))
				.map(blobCodec::decodeResult);
	}

}
