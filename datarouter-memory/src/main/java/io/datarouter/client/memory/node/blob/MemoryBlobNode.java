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
package io.datarouter.client.memory.node.blob;

import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.scanner.OptionalScanner;
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
import io.datarouter.util.tuple.Range;

public class MemoryBlobNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final MemoryBlobStorage storage;
	private final MemoryBlobKeyCodec keyCodec;

	public MemoryBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		storage = new MemoryBlobStorage();
		keyCodec = new MemoryBlobKeyCodec();
	}

	/*------------- BlobStorageReader --------------*/

	@Override
	public String getBucket(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Subpath getRootPath(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.isPresent();
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.map(MemoryBlob::getLength);
	}

	@Override
	public byte[] read(PathbeanKey key, Config config){
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.map(MemoryBlob::getValue)
				.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys, Config config){
		return Scanner.of(keys)
				.map(keyCodec::encode)
				.map(storage::find)
				.concat(OptionalScanner::of)
				.toMap(keyCodec::decode, MemoryBlob::getValue);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length, Config config){
		int from = (int)offset;
		int to = from + length;
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.map(MemoryBlob::getValue)
				.map(bytes -> Arrays.copyOfRange(bytes, from, to))
				.orElse(null);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		Range<byte[]> bytesRange = keyCodec.encodeSubpathToRange(subpath);
		return storage.scan(bytesRange)
				.map(MemoryBlob::getKey)
				.map(keyCodec::decode)
				.batch(100);
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		Range<byte[]> bytesRange = keyCodec.encodeSubpathToRange(subpath);
		return storage.scan(bytesRange)
				.map(blob -> new Pathbean(keyCodec.decode(blob), blob.getLength()))
				.batch(100);
	}

	/*------------- BlobStorageWriter --------------*/

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		byte[] bytesKey = keyCodec.encode(key);
		Long ttlMs = config.findTtl()
				.map(Duration::toMillis)
				.orElse(null);
		storage.write(bytesKey, value, ttlMs);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream, Config config){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		byte[] bytesKey = keyCodec.encode(key);
		storage.delete(bytesKey);
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		storage.deleteAll();
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
