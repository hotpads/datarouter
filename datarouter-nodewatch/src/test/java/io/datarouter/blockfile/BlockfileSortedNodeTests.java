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
package io.datarouter.blockfile;

import org.testng.annotations.Guice;

import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.blockfile.BlockfileNodeParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.reader.SortedMapStorageReader;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeReaderIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;
import jakarta.inject.Inject;

@Guice(moduleFactory = BlockfileTestNgModuleFactory.class)
public class BlockfileSortedNodeTests
extends BaseSortedNodeReaderIntegrationTests{

	private static final BlockfileStorage STORAGE
			= new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/sortedNode/");
	private static final String FILENAME = "file";

	@Inject
	public BlockfileSortedNodeTests(NodeFactory nodeFactory){
		super(makeNode(nodeFactory));
		createTestFile();
	}

	private static SortedMapStorageReader<SortedBeanKey,SortedBean> makeNode(NodeFactory nodeFactory){
		BlockfileGroup<SortedBean> blockfileGroup
				= new BlockfileGroupBuilder<SortedBean>(STORAGE).build();
		BlockfileNodeParams<SortedBeanKey,SortedBean,SortedBeanFielder> blockfileNodeParams
				= new BlockfileNodeParams<>(blockfileGroup, FILENAME);
		return nodeFactory.create(
				BlockfileTestClientIds.BLOCKFILE,
				SortedBeanEntityKey::new,
				SortedBean::new,
				SortedBeanFielder::new)
				.withBlockfileNodeParams(blockfileNodeParams)
				.buildAndRegister();
	}

	private static void createTestFile(){
		BlockfileGroup<SortedBean> blockfileGroup
				= new BlockfileGroupBuilder<SortedBean>(STORAGE).build();
		NodeParams<SortedBeanKey,SortedBean,SortedBeanFielder> nodeParams
				= new NodeParamsBuilder<>(SortedBean::new, SortedBeanFielder::new).build();
		DatabeanFieldInfo<SortedBeanKey,SortedBean,SortedBeanFielder> fieldInfo
				= new DatabeanFieldInfo<>(nodeParams);
		BlockfileDatabeanCodec<SortedBeanKey,SortedBean,SortedBeanFielder> codec
				= new BlockfileDatabeanCodec<>(fieldInfo);
		BlockfileWriter<SortedBean> writer = blockfileGroup.newWriterBuilder(FILENAME).build();
		Scanner.of(SortedBeans.generatedSortedBeans())
				.sort()// they are provided shuffled
				.map(codec::encode)
				.batch(100)
				.then(writer::writeBlocks);
	}

}
