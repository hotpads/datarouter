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
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.util.http.RequestTool;

public class DeleteNodeDataHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DeleteNodeDataHandler.class);

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebFiles files;


	@Handler(defaultHandler = true)
	private Mav showForm(){
		Mav mav = new Mav(files.jsp.admin.deleteNodeDataJsp);
		String nodeName = RequestTool.get(request, "nodeName");
		Node<?,?,?> node = nodes.getNode(nodeName);
		if(node == null){
			return new MessageMav("Cannot find node " + nodeName);
		}
		mav.put("node", node);
		List<Field<?>> fields = node.getFieldInfo().getSamplePrimaryKey().getFields();
		mav.put("fields", fields);
		return mav;
	}

	@Handler
	private <PK extends PrimaryKey<PK>> Mav doDeletion(String nodeName){
		@SuppressWarnings("unchecked")
		Node<PK,?,?> node = (Node<PK,?,?>)nodes.getNode(nodeName);

		List<String> fieldValues = node.getFieldInfo().getSamplePrimaryKey().getFields().stream()
				.map(Field::getKey)
				.map(FieldKey::getName)
				.map(params::required)
				.map(StringTool::nullSafe)
				.map(StringTool::toLowerCase)
				.collect(Collectors.toList());

		String encodedPk = PercentFieldCodec.encode(fieldValues.stream());
		PK primaryKey;
		try{
			primaryKey = PrimaryKeyPercentCodec.decode(node.getPrimaryKeyType(), encodedPk);
		}catch(NumberFormatException e){
			return new MessageMav("NumberFormatException: " + e);
		}
		MapStorageNode<PK,?,?> mapStorageNode = (MapStorageNode<PK,?,?>)node;
		if(!mapStorageNode.exists(primaryKey, null)){
			return new MessageMav("databean does not exist");
		}
		mapStorageNode.delete(primaryKey, null);
		logger.warn("deleted databean {}", encodedPk);
		return new MessageMav("deleted databean " + encodedPk);
	}

}
