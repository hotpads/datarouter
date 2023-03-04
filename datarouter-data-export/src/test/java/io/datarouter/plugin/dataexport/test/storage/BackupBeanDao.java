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
package io.datarouter.plugin.dataexport.test.storage;

import java.util.Collection;

import javax.inject.Singleton;

import io.datarouter.plugin.dataexport.test.storage.BackupBean.BackupBeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;

@Singleton
public class BackupBeanDao extends BaseDao implements TestDao{

	public final SortedMapStorageNode<BackupBeanKey,BackupBean,BackupBeanFielder> node;

	public BackupBeanDao(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter);
		node = nodeFactory.create(clientId, BackupBean::new, BackupBeanFielder::new)
				.withTag(Tag.DATAROUTER)
				.buildAndRegister();
	}

	public SortedMapStorageNode<BackupBeanKey,BackupBean,BackupBeanFielder> getNode(){
		return node;
	}

	public void deleteAll(){
		node.deleteAll();
	}

	public Scanner<BackupBean> scan(){
		return node.scan();
	}

	public void putMultiOrBust(Collection<BackupBean> databeans){
		node.putMulti(databeans, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

}
