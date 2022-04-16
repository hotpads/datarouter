package io.datarouter.client.redis.node;

import java.io.InputStream;
import java.time.Duration;
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
	private final RedisBlobCodec codec;

	public RedisBlobNode(
			NodeParams<PathbeanKey,Pathbean,PathbeanFielder> params,
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
	public boolean exists(PathbeanKey key){
		return Optional.of(key)
				.map(codec::encodeKey)
				.map(lazyClient.get()::exists)
				.orElseThrow();
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(lazyClient.get()::find)
				.map(value -> value.length)
				.map(Integer::longValue);
	}

	@Override
	public byte[] read(PathbeanKey key){
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(lazyClient.get()::find)
				.orElse(null);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		int from = (int)offset;
		int to = from + length;
		return Optional.of(key)
				.map(codec::encodeKey)
				.flatMap(lazyClient.get()::find)
				.map(bytes -> Arrays.copyOfRange(bytes, from, to))
				.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys){
		return Scanner.of(keys)
				.map(codec::encodeKey)
				.listTo(lazyClient.get()::mget)
				.include(KeyValue::hasValue)
				.toMap(codec::decodeKey, KeyValue::getValue);
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
		Twin<byte[]> kv = new Twin<>(codec.encodeKey(key), value);
		config.findTtl()
				.map(Duration::toMillis)
				.ifPresentOrElse(
						ttlMs -> lazyClient.get().psetex(kv, ttlMs),
						() -> lazyClient.get().set(kv));
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		byte[] value = chunks.listTo(ByteTool::concat);
		write(key, value);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		byte[] value = InputStreamTool.toArray(inputStream);
		write(key, value);
	}

	@Override
	public void delete(PathbeanKey key){
		Optional.of(key)
				.map(codec::encodeKey)
				.ifPresent(lazyClient.get()::del);
	}

	@Override
	public void deleteAll(Subpath subpath){
		throw new UnsupportedOperationException();
	}

}
