package io.datarouter.client.redis.node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.InputStreamTool;
import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.codec.RedisBlobCodec;
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
import io.datarouter.util.tuple.Twin;
import io.lettuce.core.KeyValue;

public class RedisBlobNode
extends BasePhysicalNode<PathbeanKey,Pathbean,PathbeanFielder>
implements PhysicalBlobStorageNode{

	private final Supplier<DatarouterRedisClient> lazyClient;
	private final String bucket;
	private final Subpath rootPath;
	private final Integer schemaVersion;
	private final RedisBlobCodec codec;

	public RedisBlobNode(
			NodeParams<PathbeanKey,Pathbean,PathbeanFielder> params,
			ClientType<?,?> clientType,
			Supplier<DatarouterRedisClient> lazyClient){
		super(params, clientType);
		this.lazyClient = lazyClient;
		this.bucket = params.getPhysicalName();
		this.rootPath = params.getPath();
		this.schemaVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new RedisBlobCodec(schemaVersion);
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
		return lazyClient.get().exists(codec.encodeKey(key));
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		byte[] byteKey = codec.encodeKey(key);
		return lazyClient.get().find(byteKey)
				.map(value -> value.length)
				.map(Integer::longValue);
	}

	@Override
	public byte[] read(PathbeanKey key){
		byte[] byteKey = codec.encodeKey(key);
		return lazyClient.get().find(byteKey).orElse(null);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		int intOffset = (int)offset;
		byte[] byteKey = codec.encodeKey(key);
		return lazyClient.get().mget(List.of(byteKey))
				.findFirst()
				.map(KeyValue::getValue)
				.map(bytes -> Arrays.copyOfRange(bytes, intOffset, intOffset + length))
				.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys){
		return Map.of();
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		lazyClient.get().set(Twin.of(codec.encodeKey(key), value));
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		byte[] bytes = chunks.listTo(ByteTool::concat);
		lazyClient.get().set(Twin.of(codec.encodeKey(key), bytes));
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		var baos = new ByteArrayOutputStream();
		InputStreamTool.transferTo(inputStream, baos);
		write(key, baos.toByteArray());
	}

	@Override
	public void delete(PathbeanKey key){
		lazyClient.get().del(codec.encodeKey(key));
	}

	@Override
	public void deleteAll(Subpath subpath){
		throw new UnsupportedOperationException();
	}

}
