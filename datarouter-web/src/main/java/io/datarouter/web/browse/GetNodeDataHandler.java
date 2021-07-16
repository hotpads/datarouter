/**
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
package io.datarouter.web.browse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;

public class GetNodeDataHandler extends InspectNodeDataHandler{
	private static final Logger logger = LoggerFactory.getLogger(GetNodeDataHandler.class);

	@Override
	protected PathNode getFormPath(){
		return files.jsp.admin.getNodeDataJsp;
	}

	@Override
	protected List<Field<?>> getFields(){
		return node.getFieldInfo().getFields();
	}

	@Override
	protected List<Field<?>> getKeyFields(){
		return node.getFieldInfo().getPrimaryKeyFields();
	}

	@Handler
	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav get(String nodeName){
		Mav mav = showForm();
		@SuppressWarnings("unchecked")
		Node<PK,?,?> node = (Node<PK,?,?>)nodes.getNode(nodeName);

		limit = mav.put(PARAM_limit, params.optionalInteger(PARAM_limit).orElse(100));

		List<String> fieldValues = getFieldValues(node);

		String encodedPk = PercentFieldCodec.encode(fieldValues.stream());
		PK primaryKey = PrimaryKeyPercentCodecTool.decode(node.getFieldInfo().getPrimaryKeySupplier(), encodedPk);
		@SuppressWarnings("unchecked")
		MapStorageReader<PK,D> mapStorageNode = (MapStorageReader<PK,D>)node;
		if(!mapStorageNode.exists(primaryKey)){
			return new MessageMav("databean does not exist");
		}
		D mapDatabean = mapStorageNode.get(primaryKey);
		List<D> databeans = Scanner.ofNullable(mapDatabean).list();
		addDatabeansToMav(mav, databeans);
		logger.warn("Retrieved databean {}", encodedPk);
		return mav;
	}

}
