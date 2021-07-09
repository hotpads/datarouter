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
import java.util.Optional;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedOps;
import io.datarouter.client.memcached.codec.MemcachedBlobCodec;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.InputStreamTool;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.tuple.Pair;

public class MemcachedBlobNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalBlobStorageNode<PK,D,F>{

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);
	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

	private final ClientId clientId;
	private final String bucket;
	private final Subpath rootPath;
	private final Integer schemaVersion;
	private final MemcachedBlobCodec codec;
	private final MemcachedOps ops;

	public MemcachedBlobNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			MemcachedClientManager memcachedClientManager){
		super(params, clientType);
		clientId = params.getClientId();
		bucket = params.getPhysicalName();
		rootPath = params.getPath();
		schemaVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		codec = new MemcachedBlobCodec(getName(), schemaVersion);
		ops = new MemcachedOps(memcachedClientManager);
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
	public boolean exists(PathbeanKey key){
		return scanMultiInternal(List.of(key))
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
	public void write(PathbeanKey key, byte[] value){
		ops.set(clientId, getName(), codec.encodeKey(key), MemcachedExpirationTool.MAX, value);
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		byte[] bytes = chunks
				.listTo(ByteTool::concatenate);
		write(key, bytes);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		var baos = new ByteArrayOutputStream();
		InputStreamTool.transfer(inputStream, baos);
		write(key, baos.toByteArray());
	}

	@Override
	public void delete(PathbeanKey key){
		ops.delete(clientId, getName(), codec.encodeKey(key), Duration.ofSeconds(3));
	}

	@Override
	public void deleteAll(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	/*------------- private --------------*/

	private Scanner<Pair<PathbeanKey,byte[]>> scanMultiInternal(Collection<PathbeanKey> keys){
		return Scanner.of(keys)
				.map(codec::encodeKey)
				.listTo(memcachedStringKeys -> ops.fetch(
						clientId,
						getName(),
						memcachedStringKeys,
						DEFAULT_TIMEOUT.toMillis(),
						DEFAULT_IGNORE_EXCEPTION))
				.map(codec::decodeResult);
	}

}
