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
package io.datarouter.storage.test.blob;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;

@Singleton
public class DatarouterBlobTestDao extends BaseDao implements TestDao{

	public static final Subpath SUBPATH = new Subpath("app");

	public final PhysicalBlobStorageNode node;

	public DatarouterBlobTestDao(Datarouter datarouter, BlobNodeFactory nodeFactory, ClientId clientId){
		super(datarouter);
		node = nodeFactory.create(clientId,
				DatabaseBlob::new,
				DatabaseBlobFielder::new)
				.buildAndRegister();
	}

	public void write(String key, String content){
		node.write(PathbeanKey.of(SUBPATH + key), content.getBytes());
	}

	public void write(String key, String content, Config config){
		node.write(PathbeanKey.of(SUBPATH + key), content.getBytes(), config);
	}

	public boolean exists(String key){
		return node.exists(PathbeanKey.of(SUBPATH + key));
	}

	public String read(String key){
		byte[] testBytes = node.read(PathbeanKey.of(SUBPATH + key));
		return new String(testBytes, StandardCharsets.UTF_8);
	}

	public Long length(String key){
		return node.length(PathbeanKey.of(SUBPATH + key)).get();
	}

	public void delete(String key){
		node.delete(PathbeanKey.of(SUBPATH + key));
	}

	public void writeScannerOfBytes(String key, Scanner<byte[]> content){
		node.write(PathbeanKey.of(SUBPATH + key), content);
	}

	public Scanner<List<Pathbean>> scan(Subpath path){
		return node.scanPaged(path, new Config().setResponseBatchSize(5));
	}

	public Scanner<List<PathbeanKey>> scanKeys(Subpath path){
		return node.scanKeysPaged(path, new Config().setResponseBatchSize(5));
	}

	public void vacuum(){
		node.vacuum(new Config());
	}

}
