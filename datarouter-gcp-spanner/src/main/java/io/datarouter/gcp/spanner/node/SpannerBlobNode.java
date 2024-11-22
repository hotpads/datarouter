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
package io.datarouter.gcp.spanner.node;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.SpannerVacuum;
import io.datarouter.gcp.spanner.op.read.SpannerGetBlobOp;
import io.datarouter.gcp.spanner.op.write.SpannerDeleteOp;
import io.datarouter.gcp.spanner.op.write.SpannerPutBlobOp;
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

public class SpannerBlobNode
extends BasePhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>
implements PhysicalBlobStorageNode{

	private final SpannerClientManager clientManager;
	private final SpannerFieldCodecs fieldCodecs;
	private final Subpath rootPath;
	private final String bucket;

	public SpannerBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> params,
			ClientType<?,?> clientType,
			SpannerClientManager clientManager,
			SpannerFieldCodecs fieldCodecs){
		super(params, clientType);
		this.clientManager = clientManager;
		this.fieldCodecs = fieldCodecs;
		this.rootPath = params.getPath();
		this.bucket = params.getPhysicalName();
	}

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		var putOp = new SpannerPutBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				key,
				value,
				config);
		putOp.wrappedCall();
	}

	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value, config);
	}

	@Override
	public void delete(PathbeanKey key, Config config){
		var op = new SpannerDeleteOp<>(
				clientManager.getDatabaseClient(getFieldInfo().getClientId()),
				getFieldInfo(),
				List.of(new DatabaseBlobKey(key)),
				config,
				fieldCodecs);
		op.wrappedCall();
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
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				List.of(key),
				config,
				fields);
		List<DatabaseBlob> blobs = op.wrappedCall();
		return !blobs.isEmpty();
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		List<String> fields = List.of(
				DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
				DatabaseBlob.FieldKeys.size.getColumnName());
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				List.of(key),
				config,
				fields);
		List<DatabaseBlob> blobs = op.wrappedCall();
		return blobs.isEmpty() ? Optional.empty() : Optional.of(blobs.getFirst().getSize());
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				List.of(key),
				config,
				getFieldInfo().getFieldColumnNames());
		List<DatabaseBlob> blobs = op.wrappedCall();
		return blobs.isEmpty()
				? Optional.empty()
				: Optional.of(blobs.getFirst().getData());
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				List.of(key),
				config,
				getFieldInfo().getFieldColumnNames());
		int from = (int)offset;
		int to = from + length;
		List<DatabaseBlob> blobs = op.wrappedCall();
		return blobs.isEmpty()
				? Optional.empty()
				: Optional.of(Arrays.copyOfRange(blobs.getFirst().getData(), from, to));
	}

	@Override
	public Optional<byte[]> readEnding(PathbeanKey key, int length, Config config){
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				List.of(key),
				config,
				getFieldInfo().getFieldColumnNames());
		List<DatabaseBlob> blobs = op.wrappedCall();
		if(blobs.isEmpty()){
			return Optional.empty();
		}
		byte[] fullBytes = blobs.getFirst().getData();
		int offset = Math.max(0, fullBytes.length - length);
		byte[] lastBytes = Arrays.copyOfRange(fullBytes, offset, fullBytes.length);
		return Optional.of(lastBytes);
	}

	@Override
	public Map<PathbeanKey,byte[]> readMulti(List<PathbeanKey> keys, Config config){
		var op = new SpannerGetBlobOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				getFieldInfo().getFieldColumnNames());
		List<DatabaseBlob> blobs = op.wrappedCall();
		return Scanner.of(blobs)
				.toMap(blob -> PathbeanKey.of(blob.getKey().getPathAndFile()),
						DatabaseBlob::getData);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		long nowMs = System.currentTimeMillis();
		Scanner<List<DatabaseBlobKey>> scanner = new SpannerLikePkScanner(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				subpath,
				config,
				nowMs);
		return scanner
				.map(blobs -> Scanner.of(blobs)
				.map(blob -> PathbeanKey.of(blob.getPathAndFile()))
				.list());
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		long nowMs = System.currentTimeMillis();
		Scanner<List<DatabaseBlob>> scanner = new SpannerLikeScanner(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				subpath,
				config,
				nowMs);
		return scanner.map(blobs -> Scanner.of(blobs)
				.map(blob -> new Pathbean(PathbeanKey.of(blob.getKey().getPathAndFile()), blob.getSize()))
				.list());
	}

	@Override
	public void vacuum(Config config){
		var vacuum = new SpannerVacuum<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
				config);
		vacuum.vacuum();
	}

}
