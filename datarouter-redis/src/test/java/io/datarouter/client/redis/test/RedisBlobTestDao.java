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
package io.datarouter.client.redis.test;

import java.nio.charset.StandardCharsets;

import io.datarouter.client.redis.RedisTestClientIds;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RedisBlobTestDao extends BaseDao implements TestDao{

	public static final Subpath SUBPATH = new Subpath("app");

	private final PhysicalBlobStorageNode node;

	@Inject
	public RedisBlobTestDao(
			Datarouter datarouter,
			BlobNodeFactory blobNodeFactory){
		super(datarouter);
		node = blobNodeFactory.create(
				RedisTestClientIds.REDIS,
				"nomatter",
				SUBPATH);
		datarouter.register(node);
	}

	public void write(String key, String content){
		node.write(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key), content.getBytes());
	}

	public boolean exists(String key){
		return node.exists(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key));
	}

	public String read(String key){
		byte[] testBytes = node.read(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key));
		return new String(testBytes, StandardCharsets.UTF_8);
	}

	public Long length(String key){
		return node.length(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key)).get();
	}

	public void delete(String key){
		node.delete(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key));
	}

	public void writeScannerOfBytes(String key, Scanner<byte[]> content){
		node.writeChunks(PathbeanKey.of(node.getBucket() + "/" + SUBPATH + key), content);
	}

}