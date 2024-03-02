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
package io.datarouter.client.mysql.node;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.io.InputStreamTool;
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

public class MysqlBlobNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final Subpath rootPath;
	private final MysqlNodeManager manager;
	private final String bucket;

	public MysqlBlobNode(
			MysqlNodeManager manager,
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		this.manager = manager;
		this.bucket = params.getPhysicalName();
		this.rootPath = params.getPath();
	}

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		manager.putBlobOp(getFieldInfo(), key, value, config);
	}

	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value, config);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		manager.delete(this.getFieldInfo(), new DatabaseBlobKey(key), config);
	}

	@Override
	public void deleteMulti(List<PathbeanKey> keys, Config config){
		// TODO delete multiple keys in one operation
		keys.forEach(key -> delete(key, config));
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		scanKeys(subpath).forEach(this::delete);
	}

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
		List<String> fields = List.of(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName());
		List<DatabaseBlob> beans = manager.getBlob(
				this.getFieldInfo(),
				List.of(key),
				fields,
				config);
		return !beans.isEmpty();
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		List<String> fields = List.of(
				DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
				DatabaseBlob.FieldKeys.size.getColumnName());
		List<DatabaseBlob> beans = manager.getBlob(
				this.getFieldInfo(),
				List.of(key),
				fields,
				config);
		return beans.isEmpty() ? Optional.empty() : Optional.ofNullable(beans.getFirst().getSize());
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		List<DatabaseBlob> beans = manager.getBlob(
				getFieldInfo(),
				List.of(key),
				getFieldInfo().getFieldColumnNames(),
				config);
		return beans.isEmpty()
				? Optional.empty()
				: Optional.of(beans.getFirst().getData());
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		List<DatabaseBlob> beans = manager.getBlob(
				getFieldInfo(),
				List.of(key),
				getFieldInfo().getFieldColumnNames(),
				config);
		int from = (int)offset;
		int to = from + length;
		return beans.isEmpty()
				? Optional.empty()
				: Optional.of(Arrays.copyOfRange(beans.getFirst().getData(), from, to));
	}

	@Override
	public Map<PathbeanKey,byte[]> readMulti(List<PathbeanKey> keys, Config config){
		List<DatabaseBlob> beans = manager.getBlob(
				getFieldInfo(),
				keys,
				getFieldInfo().getFieldColumnNames(),
				config);
		return Scanner.of(beans)
				.toMap(
						left -> PathbeanKey.of(left.getKey().getPathAndFile()),
						DatabaseBlob::getData);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		return manager.likePathKey(subpath, getFieldInfo(), config)
				.map(blobs -> Scanner.of(blobs)
						.map(blob -> PathbeanKey.of(blob.getPathAndFile()))
						.list());
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		return manager.likePath(subpath, getFieldInfo(), config)
				.map(blobs -> Scanner.of(blobs)
						.map(blob -> new Pathbean(
								PathbeanKey.of(blob.getKey().getPathAndFile()),
								blob.getSize()))
						.list());
	}

	@Override
	public void vacuum(Config config){
		manager.vacuum(
				getFieldInfo(),
				DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
				DatabaseBlob.FieldKeys.expirationMs.getColumnName(),
				config);
	}

}
