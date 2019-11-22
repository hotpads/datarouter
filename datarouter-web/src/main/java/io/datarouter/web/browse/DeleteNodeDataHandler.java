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

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;

public class DeleteNodeDataHandler extends InspectNodeDataHandler{
	private static final Logger logger = LoggerFactory.getLogger(DeleteNodeDataHandler.class);

	@Override
	protected PathNode getFormPath(){
		return files.jsp.admin.deleteNodeDataJsp;
	}

	@Override
	protected List<Field<?>> getFields(){
		return node.getFieldInfo().getFields();
	}

	@Override
	protected List<Field<?>> getKeyFields(){
		return node.getFieldInfo().getSamplePrimaryKey().getFields();
	}

	@Handler
	private <PK extends PrimaryKey<PK>> Mav doDeletion(String nodeName){
		@SuppressWarnings("unchecked")
		Node<PK,?,?> node = (Node<PK,?,?>)nodes.getNode(nodeName);

		List<String> fieldValues = getFieldValues(node);

		String encodedPk = PercentFieldCodec.encode(fieldValues.stream());
		PK primaryKey = PrimaryKeyPercentCodec.decode(node.getFieldInfo().getPrimaryKeyClass(), encodedPk);
		MapStorageNode<PK,?,?> mapStorageNode = (MapStorageNode<PK,?,?>)node;
		if(!mapStorageNode.exists(primaryKey)){
			return new MessageMav("databean does not exist");
		}
		mapStorageNode.delete(primaryKey);
		logger.warn("deleted databean {}", encodedPk);
		return new MessageMav("deleted databean " + encodedPk);
	}

}
