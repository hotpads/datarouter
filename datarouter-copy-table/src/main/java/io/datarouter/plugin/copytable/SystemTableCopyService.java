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
package io.datarouter.plugin.copytable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tag.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SystemTableCopyService{
	private static final Logger logger = LoggerFactory.getLogger(SystemTableCopyService.class);

	@Inject
	private DatarouterNodes datarouterNodes;

	public List<PhysicalNode<?,?,?>> getSystemTables(ClientId clientId){
		return Scanner.of(listSortedStorageWriterNodes(clientId))
				.include(SystemTableCopyService::isSystemTable)
				.list();
	}


	private List<PhysicalNode<?,?,?>> listSortedStorageWriterNodes(ClientId clientId){
		return Scanner.of(datarouterNodes.getWritableNodes(List.of(clientId)))
				.include(SortedStorageWriter.class::isInstance)
				.list();
	}

	private static boolean isSystemTable(PhysicalNode<?,?,?> node){
		String tag = node.getFieldInfo().findTag().map(Tag::displayLowerCase).orElse("unknown");
		return !tag.equals(Tag.APP.displayLowerCase());
	}

}
